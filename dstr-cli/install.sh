#!/usr/bin/env bash

echo "Starting installation"
set -e

VERSION="0.1.0-alpha"
INSTALL_ROOT="$HOME/.local"
BIN_DIR="$INSTALL_ROOT/bin"
APP_DIR="$INSTALL_ROOT/dstr-$VERSION"

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

echo "✅ Installation complete!"
echo "Run: dstr --help"
