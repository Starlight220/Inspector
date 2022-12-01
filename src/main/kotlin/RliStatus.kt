package io.starlight.inspector

sealed class RliStatus(protected val base: LocatedRli) {
    val rli: Rli by base::rli
    val location: Location by base::location

    class UpToDate(base: LocatedRli) : RliStatus(base) {
        override fun invoke() {
            Report.upToDate(base)
        }
    }

    class Outdated(base: LocatedRli) : RliStatus(base) {
        override fun invoke() {
            Report.outdated(rli, location)
            location.file.replaceRange(location.indexRange, Constants.latestVersion)
        }
    }

    class Invalid(base: LocatedRli, val diff: Diff) : RliStatus(base) {
        override fun invoke() {
            Report.invalid(this)
        }
    }

    abstract operator fun invoke()
}

fun LocatedRli.toStatus(): RliStatus {
    if (rli.version == Constants.latestVersion) {
        return RliStatus.UpToDate(this)
    }
    val latest = rli.copy(version = Constants.latestVersion)
    if (rli.response == latest.response) {
        return RliStatus.Outdated(this)
    }
    return RliStatus.Invalid(this, Diff(old = this.rli, new = latest))
}
