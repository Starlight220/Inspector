sealed class RliStatus(val rli: Rli, val location: Location) {
  constructor(base: LocatedRli) : this(base.second, base.first)

  class Valid(base: LocatedRli) : RliStatus(base) {
    override fun invoke() {
      report.outdated(rli.url, rli.lines, location)
      location.file.replaceRange(location.indexRange, latestVersion)
    }

  }

  class Invalid(base: LocatedRli, val diff: Diff) : RliStatus(base) {
    override fun invoke() {
      report.invalid(this)
    }
  }

  abstract operator fun invoke()
}

val LocatedRli.status: RliStatus
  get() = with(second) {
    val new = Rli(
      url.replace(Regex(versionScheme), latestVersion),
      lines
    )
    val diff = this diff new
    if (diff == null) {
      return@with RliStatus.Valid(this@status)
    } else {
      return@with RliStatus.Invalid(this@status, diff)
    }
  }