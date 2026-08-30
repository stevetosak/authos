#!/bin/sh
set -eu

# Render the runtime config from env (the client_id from scripts/bootstrap.ts, injected at deploy
# time) and start nginx. Mirrors authos-frontend's entrypoint.
: "${DEMO_DUSTER_CLIENT_ID:?set DEMO_DUSTER_CLIENT_ID (run scripts/bootstrap.ts to mint one)}"
export DEMO_DUSTER_BASE_PATH="${DEMO_DUSTER_BASE_PATH:-/duster/api/v1}"
export DEMO_AUTHOS_ORIGIN="${DEMO_AUTHOS_ORIGIN:-https://authos-api.tosak.net}"
export DEMO_PROOF_SPEC_URL="${DEMO_PROOF_SPEC_URL:-https://github.com/stevetosak/authos/blob/master/packages/e2e/specs/login-refresh-logout.spec.ts}"
export DEMO_PROOF_CI_URL="${DEMO_PROOF_CI_URL:-https://github.com/stevetosak/authos/actions/workflows/sdk.yaml}"

mkdir -p /usr/share/nginx/html/config
envsubst '${DEMO_DUSTER_CLIENT_ID} ${DEMO_DUSTER_BASE_PATH} ${DEMO_AUTHOS_ORIGIN} ${DEMO_PROOF_SPEC_URL} ${DEMO_PROOF_CI_URL}' \
  < /usr/share/nginx/html/config.template.js \
  > /usr/share/nginx/html/config/config.js

exec nginx -g 'daemon off;'
