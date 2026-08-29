# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

`dstr-cli` is a Kotlin CLI tool for configuring and managing [Duster](../duster) clients within the Authos ecosystem. Authos is an OpenID-compliant Identity Provider (IDP); Duster is a BFF proxy that handles OAuth flows for apps registered with Authos. This CLI communicates with Duster and (for the `sync` command) with the Authos server.

## Build & Run

```bash
# Build distribution zip
./gradlew distZip

# Build and run directly
./gradlew run --args="<subcommand> [options]"

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.tosak.authos.duster.SomeTestClass"
```

Install the built distribution locally via `install.sh`, which places the binary at `~/.local/bin/dstr`.

## Server Configuration

The CLI resolves the Duster and Authos base URLs in this priority order (highest to lowest):

1. **CLI flags**: `--host` (Duster), `--authos-host` (Authos)
2. **Environment variables**: `DUSTER_BASE_URL`, `AUTHOS_BASE_URL`
3. **Config file**: `~/.dstr/dstr.config` (Java properties format)
4. **Defaults**: `http://localhost:8785` (Duster), `http://localhost:8080` (Authos)

**Config file format** (`~/.dstr/dstr.config`):
```properties
duster_base_url=http://myduster.example.com
authos_base_url=http://myauthos.example.com
```

`install.sh` prompts to write this file on first install. The `DusterConfig` singleton in `DusterCLI.kt` loads and merges all sources at startup.

## Architecture

The app uses [Clikt](https://ajalt.github.io/clikt/) for CLI parsing with `SuspendingCliktCommand` throughout (all commands are coroutine-based). A single shared Ktor `HttpClient` (Apache5 engine, Jackson content negotiation) lives in `DusterCLI.kt` and is used across all commands.

**Command tree:**
```
dstr [--host URL] [--authos-host URL]
├── apps [-cid | -n]          # List all apps or look up by clientId/name (Mordant table/card)
│   └── configure -cid        # PATCH /internal/apps/config — one or more of:
│       --success-url --logout-url --error-url --webhook-secret
│       --session-ttl --allowed-origins a,b   ("" clears --allowed-origins / --error-url)
├── sync [-cid | -n]          # Pull app config from Authos → save to Duster
└── credentials               # Show stored credentials
    └── save -cid -cs         # Save client credentials to Duster
```

**URL resolution:** Commands read `DusterConfig.dusterBaseUrl` and `DusterConfig.authosBaseUrl` (both are `var` on the `DusterConfig` singleton). The root `DusterCli.run()` initialises `DusterConfig` before any subcommand runs, so the resolved URLs are always ready.

**Service ports (defaults):**
- `localhost:8785` — Duster internal API (credentials, apps)
- `localhost:8080` — Authos server (sync pull endpoint: `/duster/pull`)

## Key Files

- `Main.kt` — entry point, wires command tree
- `DusterCLI.kt` — root `DusterCli` command + shared `HttpClient` + `DusterConfig` singleton
- `DusterAppDto.kt` — Jackson-mapped DTO for app data (snake_case JSON ↔ camelCase Kotlin)
- `commands/` — one file per command; `Apps`/`Credentials` are parent groups, `ConfigureApp` (`apps configure`) / `Sync` / `SaveCredentials` are leaf commands
