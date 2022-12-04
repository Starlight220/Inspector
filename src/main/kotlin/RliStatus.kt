package io.starlight.inspector

sealed class RliStatus(protected val base: LocatedRli) {
    val rli: Rli by base::rli
    val location: Location by base::location

    class UpToDate(base: LocatedRli) : RliStatus(base) {
        override fun invoke() {
            Report.upToDate(base)
        }
    }

    class Outdated(base: LocatedRli, private val latestVersion: String) : RliStatus(base) {
        override fun invoke() {
            Report.outdated(rli, location)
            location.file.replaceRange(location.indexRange, latestVersion)
        }
    }

    class Invalid(base: LocatedRli, val diff: Diff) : RliStatus(base) {
        override fun invoke() {
            Report.invalid(this)
        }
    }

    abstract operator fun invoke()
}

context(RliContext)
fun LocatedRli.toStatus(): RliStatus {
    if (rli.version == context.latestVersion) {
        return RliStatus.UpToDate(this)
    }
    val latest = rli.copy(version = context.latestVersion)
    if (rli.response == latest.response) {
        return RliStatus.Outdated(this, context.latestVersion)
    }
    return RliStatus.Invalid(this, Diff(old = this.rli, new = latest))
}
