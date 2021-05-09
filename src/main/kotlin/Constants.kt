const val latestVersion = "v2021.3.1"
const val versionScheme = """v\d{4}\.\d\.\d(?:-beta-\d)?"""
const val rliRegex =
  """\.\. (?:rli)|(?:remoteliteralinclude):: https:\/\/raw\.githubusercontent\.com\/wpilibsuite\/allwpilib\/($versionScheme)\/([\/\w.]+)\r?\n.*\r?\n[ ]{9}:lines: (\d*-\d*)"""
const val baseURL = "https://raw.githubusercontent.com/wpilibsuite/allwpilib"
