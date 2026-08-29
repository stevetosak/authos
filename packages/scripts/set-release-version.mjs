// Stamp a release version onto the publishable SDK packages. Run by `.github/workflows/
// sdk-release.yaml` after `npm ci`, before `npm run build` / `npm publish`.
//
// - sets `version` on core / react / vue / angular (lockstep — one version per release)
// - rewrites the adapters' `"@authoss/duster-core": "*"` workspace range to `"^<version>"`
//   (npm has no `workspace:` protocol, so the committed range is `*` and gets pinned here)
//
// Usage:  node scripts/set-release-version.mjs 1.4.0
import { readFileSync, writeFileSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const version = process.argv[2]
if (!version || !/^\d+\.\d+\.\d+(?:-[0-9A-Za-z.-]+)?$/.test(version)) {
  console.error(`set-release-version: expected a semver argument, got ${JSON.stringify(version)}`)
  process.exit(1)
}

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const PACKAGES = ['core', 'react', 'vue', 'angular']
const CORE_DEP = '@authoss/duster-core'

for (const name of PACKAGES) {
  const file = join(root, name, 'package.json')
  const pkg = JSON.parse(readFileSync(file, 'utf8'))

  pkg.version = version
  if (pkg.dependencies?.[CORE_DEP] === '*') {
    pkg.dependencies[CORE_DEP] = `^${version}`
  }

  writeFileSync(file, JSON.stringify(pkg, null, 2) + '\n')
  const pinned = pkg.dependencies?.[CORE_DEP] ? ` (${CORE_DEP} ${pkg.dependencies[CORE_DEP]})` : ''
  console.log(`${pkg.name} -> ${version}${pinned}`)
}
