package ciyin.file.picker

class PickerResultLauncher(private val onLaunch: () -> Unit) {
    fun launch() {
        onLaunch()
    }
}
