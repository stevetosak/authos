// Vanilla-JS entry — built as an IIFE exposing `window.Duster`.
//   <script src="https://unpkg.com/@authoss/duster-core/dist/duster.global.js"></script>
//   <script>
//     const duster = Duster.getOrCreateDusterClient({ clientId: 'app_123' })
//     duster.init().then(() => console.log(duster.getSnapshot()))
//   </script>
export { createDusterClient } from './client.js'
export { getOrCreateDusterClient, resetDusterRegistry } from './registry.js'
export { normalizeUser } from './normalize.js'
export { buildUrl, buildMeUrl, buildStartUrl, buildLogoutUrl } from './urls.js'
export { readDusterError } from './errors.js'
