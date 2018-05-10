package sssemil.com.utils

import com.sun.jna.platform.win32.WinDef.UINT_PTR
import java.io.File

class WallpaperChanger {

    private val osName = System.getProperty("os.name")

    fun setDesktop(file: File) {
        when {
            osName.startsWith("Windows") -> SPI.INSTANCE.SystemParametersInfo(
                    UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
                    UINT_PTR(0),
                    file.absolutePath,
                    UINT_PTR(SPI.SPIF_UPDATEINIFILE or SPI.SPIF_SENDWININICHANGE))
            osName.startsWith("Linux") -> // TODO not all Linux is gnome
                Runtime.getRuntime().exec("gsettings setDesktop org.gnome.desktop.background picture-uri ${file.toURI()}")
            else -> onUnsupportedDesktop(osName)
        }
    }

    fun setLockScreen(file: File) {
        when {
            osName.startsWith("Windows") -> onUnsupportedLockScreen(osName)
            osName.startsWith("Linux") -> // TODO not all Linux is gnome
                Runtime.getRuntime().exec("gsettings setDesktop org.gnome.desktop.screensaver picture-uri ${file.toURI()}")
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