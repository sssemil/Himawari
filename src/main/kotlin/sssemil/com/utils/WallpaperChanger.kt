package sssemil.com.utils

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef.UINT_PTR
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIFunctionMapper
import com.sun.jna.win32.W32APITypeMapper
import java.io.File

class WallpaperChanger {

    companion object {
        fun setDesktop(file: File) {
            val osName = System.getProperty("os.name")
            Logger.i("Running on: $osName")

            when {
                osName.startsWith("Windows") -> SPI.INSTANCE.SystemParametersInfo(
                        UINT_PTR(SPI.SPI_SETDESKWALLPAPER),
                        UINT_PTR(0),
                        file.absolutePath,
                        UINT_PTR(SPI.SPIF_UPDATEINIFILE or SPI.SPIF_SENDWININICHANGE))
                osName.startsWith("Linux") -> // TODO not all Linux is gnome
                    Runtime.getRuntime().exec("gsettings setDesktop org.gnome.desktop.background picture-uri ${file.toURI()}")
                else -> onUnsupportedDesktop()
            }
        }

        fun setLockScreen(file: File) {
            val osName = System.getProperty("os.name")
            Logger.i("Running on: $osName")

            when {
                osName.startsWith("Windows") -> onUnsupportedLockScreen()
                osName.startsWith("Linux") -> // TODO not all Linux is gnome
                    Runtime.getRuntime().exec("gsettings setDesktop org.gnome.desktop.screensaver picture-uri ${file.toURI()}")
                else -> onUnsupportedLockScreen()
            }
        }

        private fun onUnsupportedDesktop() {
            Logger.w("Setting desktop wallpaper is not supported on this platform.")
        }

        private fun onUnsupportedLockScreen() {
            Logger.w("Setting lock-screen wallpaper is not supported on this platform.")
        }
    }

    interface SPI : StdCallLibrary {

        fun SystemParametersInfo(
                uiAction: UINT_PTR,
                uiParam: UINT_PTR,
                pvParam: String,
                fWinIni: UINT_PTR
        ): Boolean

        companion object {

            //from MSDN article
            val SPI_SETDESKWALLPAPER: Long = 20
            val SPIF_UPDATEINIFILE: Long = 0x01
            val SPIF_SENDWININICHANGE: Long = 0x02

            val INSTANCE = Native.loadLibrary("user32", SPI::class.java, hashMapOf(
                    Library.OPTION_TYPE_MAPPER to W32APITypeMapper.UNICODE,
                    Library.OPTION_FUNCTION_MAPPER to W32APIFunctionMapper.UNICODE
            )) as SPI
        }
    }
}