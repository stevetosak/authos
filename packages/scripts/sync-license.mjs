// Copies packages/LICENSE into every publishable package directory so `npm publish`
// ships a LICENSE file (npm resolves `files` relative to each package, not the workspace root).
// Run from packages/: `npm run sync-license`. CI checks the copies are in sync.
import { readFileSync, writeFileSync, existsSync } from 'node:fs'
import { readdirSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const canonical = readFileSync(join(root, 'LICENSE'), 'utf8')

const check = process.argv.includes('--check')
let drift = false

for (const entry of readdirSync(root, { withFileTypes: true })) {
  if (!entry.isDirectory()) continue
  const pkgJson = join(root, entry.name, 'package.json')
  if (!existsSync(pkgJson)) continue
  const pkg = JSON.parse(readFileSync(pkgJson, 'utf8'))
  if (pkg.private) continue
  const target = join(root, entry.name, 'LICENSE')
  const current = existsSync(target) ? readFileSync(target, 'utf8') : null
  if (current === canonical) continue
  if (check) {
    console.error(`LICENSE drift in ${entry.name}/ — run \`npm run sync-license\``)
    drift = true
  } else {
    writeFileSync(target, canonical)
    console.log(`synced ${entry.name}/LICENSE`)
  }
}

process.exit(drift ? 1 : 0)
