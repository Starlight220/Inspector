package io.starlight.inspector

import com.github.starlight220.actions.debug
import com.github.starlight220.actions.raw.getEnvOrNull
import java.io.File
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

fun main() {
    /*
    Process:
    1. Recursively walk files; for each file:
        1. Find all RLIs; for each RLI:
            1. Destructure into source, line range, version, and file location
            2. Check if version is up-to-date
               - if yes, fold (**up-to-date**)
            3. Compare responses for line ranges of both current and latest versions
                - if identical, autofix (**outdated**)
                - else, require manual attention (**invalid**)
     */
    getEnv()
        .also { println(it) }
        .asContext {
            val rliFiles: Sequence<RliFile> = fileWalk().toRliFileWalk()
            context.sources
                .asSequence()
                .asEachContext { rliFiles.filesToRlis().rliToStatus() }
                .toList() // Eagerly evaluate everything before fixing
                .asSequence()
                .apply()

            Report()
        }
}

/** Read from config JSON if it exists, otherwise read from inputs. */
private fun getEnv(): EnvSet =
    getEnvOrNull("INSPECTOR_CONFIG")
        ?.let { File(getEnvOrNull("GITHUB_WORKSPACE"), it) }
        ?.let { Json.decodeFromString<EnvSet>(it.readText()) } ?: EnvSet.fromInputs()

/** Iterate over files. */
context(EnvContext)
private fun fileWalk(): Sequence<File> =
    context.root.walkDir { it.extension == "rst" || it.extension == "md" }

/** Convert the file walk to RliFile objects. */
context(EnvContext)
private fun Sequence<File>.toRliFileWalk(): Sequence<RliFile> =
    map { RliFile(it) }.filter { !it.isIgnored }

/** Scan files for RLIs. */
context(RliContext)
private fun Sequence<RliFile>.filesToRlis(): Sequence<LocatedRli> = flatMap { file ->
    debug("Processing file $file...")
    file.findRlis()
}

/** Check status of each RLI. */
context(RliContext)
private fun Sequence<LocatedRli>.rliToStatus(): Sequence<RliStatus> = this.map { it.toStatus() }

/** Apply/report RLI status. */
context(EnvContext)
private fun Sequence<RliStatus>.apply(): Unit = forEach { it() }
