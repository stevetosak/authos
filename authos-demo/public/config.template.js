// Rendered at container start by entrypoint.sh (envsubst). The Duster app's client_id is minted at
// provisioning time (scripts/bootstrap.ts), never baked into the image.
window.__DEMO__ = {
  clientId: '${DEMO_DUSTER_CLIENT_ID}',
  dusterBasePath: '${DEMO_DUSTER_BASE_PATH}',
  authosOrigin: '${DEMO_AUTHOS_ORIGIN}',
  proofSpecUrl: '${DEMO_PROOF_SPEC_URL}',
  proofCiUrl: '${DEMO_PROOF_CI_URL}',
}
