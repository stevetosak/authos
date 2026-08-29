export { provideDuster } from './provide-duster.js'
export { DusterService } from './duster.service.js'
export { dusterAuthGuard, resolveDusterAccess } from './auth-guard.js'

// Re-exported from the core so apps don't need to depend on it directly.
export {
  buildStartUrl,
  buildLogoutUrl,
  buildMeUrl,
  readDusterError,
  normalizeUser,
} from '@authoss/duster-core'
export type {
  DusterClient,
  DusterConfig,
  DusterSnapshot,
  DusterStatus,
  DusterUser,
  DusterError,
  DusterErrorKind,
  OnUnauthenticated,
  UnauthenticatedContext,
  UnauthenticatedReason,
} from '@authoss/duster-core'
