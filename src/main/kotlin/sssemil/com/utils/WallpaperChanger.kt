package sssemil.com.utils

import java.io.File

class WallpaperChanger {

    private val osName = System.getProperty("os.name")

    fun setDesktop(file: File) {
        when {
            osName.startsWith("Windows") -> {
                val result = SPI.setDesktopWallpaper(file)
                Logger.i("Setting desktop wallpaper result: $result")
            }
            osName.startsWith("Linux") -> {
                // TODO not all Linux is gnome
                Runtime.getRuntime().exec("gsettings setDesktop org.gnome.desktop.background picture-uri ${file.toURI()}")
            }
            else -> onUnsupportedDesktop(osName)
        }
    }

    fun setLockScreen(file: File) {
        when {
            osName.startsWith("Windows") -> {
                onUnsupportedLockScreen(osName)
            }
            osName.startsWith("Linux") -> {
                // TODO not all Linux is gnome
                Runtime.getRuntime().exec("gsettings setDesktop org.gnome.desktop.screensaver picture-uri ${file.toURI()}")
            }
            else -> onUnsupportedLockScreen(osName)
        }
    }

    companion object {
        private fun onUnsupportedDesktop(osName: String) {
            Logger.w("Setting desktop wallpaper is not supported on $osName.")
        }

        private fun onUnsupportedLockScreen(osName: String) {
            Logger.w("Setting lock-screen wallpaper is not supported on $osName.")
        }
    }

    init {
        Logger.i("Running on: $osName")
    }
}