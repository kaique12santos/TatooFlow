import { execSync } from 'node:child_process'
import { copyFileSync, existsSync, mkdirSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const run = (cmd, cwd = root) => execSync(cmd, { cwd, stdio: 'inherit' })
const isWin = process.platform === 'win32'

run('npm run build-only')
run('npx cap sync android')

// Gradle debug build (assinado automaticamente com a debug key, instalável direto).
// Para release, configure um keystore em android/app/build.gradle.
run(isWin ? 'gradlew.bat assembleDebug' : './gradlew assembleDebug', join(root, 'android'))

const src = join(root, 'android', 'app', 'build', 'outputs', 'apk', 'debug', 'app-debug.apk')
if (!existsSync(src)) {
  console.error(`APK não encontrado em ${src}`)
  process.exit(1)
}

const now = new Date()
const p = (n) => String(n).padStart(2, '0')
const date = `${now.getFullYear()}-${p(now.getMonth() + 1)}-${p(now.getDate())}`
const time = `${p(now.getHours())}-${p(now.getMinutes())}-${p(now.getSeconds())}`

const outDir = resolve(root, 'builds')
mkdirSync(outDir, { recursive: true })
const dest = join(outDir, `TatooFlow-${date}-${time}.apk`)
copyFileSync(src, dest)
console.log(`\nAPK gerado: ${dest}`)

