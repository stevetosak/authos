#!/usr/bin/env bash

echo "Starting installation"
set -e

VERSION="0.1.0-alpha"
INSTALL_ROOT="$HOME/.local"
BIN_DIR="$INSTALL_ROOT/bin"
APP_DIR="$INSTALL_ROOT/dstr-$VERSION"
CONFIG_DIR="$HOME/.dstr"
CONFIG_FILE="$CONFIG_DIR/dstr.config"

echo "Release version: $VERSION"
echo "Fetching release zip..."
URL="https://github.com/stevetosak/authos/releases/download/$VERSION/dstr-$VERSION.zip"
curl -LO "$URL"
echo "Fetched release zip!"

echo "Unpacking to $APP_DIR"
unzip -q "dstr-$VERSION.zip" -d "$INSTALL_ROOT"

echo "Ensuring $BIN_DIR exists"
mkdir -p "$BIN_DIR"

echo "Creating symlink: $BIN_DIR/dstr → $APP_DIR/bin/dstr"
ln -sf "$APP_DIR/bin/dstr" "$BIN_DIR/dstr"
chmod +x "$APP_DIR/bin/dstr"

if [[ ":$PATH:" != *":$BIN_DIR:"* ]]; then
  echo "⚠️ $BIN_DIR is not in your PATH"
  SHELL_RC="$HOME/.bashrc"
  if [[ "$SHELL" =~ zsh ]]; then
    SHELL_RC="$HOME/.zshrc"
  fi
  echo "Adding export to $SHELL_RC"
  echo "export PATH=\"$BIN_DIR:\$PATH\"" >> "$SHELL_RC"
  echo "✅ Added ~/.local/bin to PATH. Please restart your terminal or run:"
  echo "source $SHELL_RC"
else
  echo "✅ $BIN_DIR is already in your PATH"
fi

# Server configuration
echo ""
echo "Configure Duster server (press Enter to keep defaults):"
read -r -p "  Duster base URL [http://localhost:8785]: " INPUT_DUSTER_URL
read -r -p "  Authos base URL [http://localhost:8080]: " INPUT_AUTHOS_URL

mkdir -p "$CONFIG_DIR"
cat > "$CONFIG_FILE" <<EOF
# dstr configuration
# Edit this file to point dstr at your Duster and Authos instances.
# These values can also be overridden at runtime via:
#   Environment variables: DUSTER_BASE_URL, AUTHOS_BASE_URL
#   CLI flags:             --host, --authos-host

# Base URL of your Duster instance
duster_base_url=${INPUT_DUSTER_URL:-http://localhost:8785}

# Base URL of your Authos IDP (used by the sync command)
authos_base_url=${INPUT_AUTHOS_URL:-http://localhost:8080}
EOF
echo "Config written to $CONFIG_FILE"

echo ""
echo "✅ Installation complete!"
echo "Run: dstr --help"
