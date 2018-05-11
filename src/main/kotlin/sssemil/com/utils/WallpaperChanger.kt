package sssemil.com.utils

import sssemil.com.utils.windows.SPI
import java.io.File

class WallpaperChanger {

    private val detectedEnv = EnvDetector.environment

    fun setDesktop(file: File) {
        when (detectedEnv) {
            EnvDetector.Env.WINDOWS -> {
                val result = SPI.setDesktopWallpaper(file)
                Logger.i("Setting desktop wallpaper result: $result")
            }
            EnvDetector.Env.GNOME,
            EnvDetector.Env.UNITY,
            EnvDetector.Env.CINNAMON,
            EnvDetector.Env.PANTHEON,
            EnvDetector.Env.GNOME_CLASSIC -> {
                if (detectedEnv == EnvDetector.Env.UNITY) {
                    Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.background", "draw-background", "false"))
                }

                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.background", "picture-uri", file.toURI().toString()))
                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.background", "picture-options", "scaled"))
                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.background", "primary-color", "#000000"))

                if (detectedEnv == EnvDetector.Env.UNITY) {
                    Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.background", "draw-background", "true"))
                }
            }
            EnvDetector.Env.MATE -> {
                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.mate.background", "picture-filename", file.absolutePath))
            }
            EnvDetector.Env.I3 -> {
                Runtime.getRuntime().exec(arrayOf("feh", "--bg-max", file.absolutePath))
            }
            EnvDetector.Env.XFCE4 -> {
                val process = Runtime.getRuntime().exec("xfconf-query --channel xfce4-desktop --list | grep last-image")
                process.inputStream.bufferedReader().lines().forEach {
                    it.split(" ").forEach { display ->
                        Runtime.getRuntime().exec(arrayOf("xfconf-query", "--channel", "xfce4-desktop", "--property", display, "--set", file.absolutePath))
                    }
                }
            }
            EnvDetector.Env.LXDE -> {
                Runtime.getRuntime().exec(arrayOf("pcmanfm", "--set-wallpaper", file.absolutePath, "--wallpaper-mode=fit"))
            }
            EnvDetector.Env.DARWIN -> {
                Runtime.getRuntime().exec(arrayOf(
                        "osascript", "-e",
                        "tell application \"System Events\"\n" +
                                "set theDesktops to a reference to every desktop\n" +
                                "repeat with aDesktop in theDesktops\n" +
                                "set the picture of aDesktop to \"${file.absolutePath}\"\nend repeat\nend tell"))

            }
            else -> {
                if (hasProgram("feh")) {
                    Logger.i("Using 'feh'...")
                    val processBuilder = ProcessBuilder()
                    processBuilder.environment()["DISPLAY"] = ":0"
                    processBuilder.command("feh", "--bg-max", file.absolutePath).start().waitFor()
                } else {
                    onUnsupportedDesktop()
                }
            }
        }
    }

    private fun hasProgram(program: String) =
            Runtime.getRuntime().exec(arrayOf("which", "--", program)).waitFor() == 0

    fun setLockScreen(file: File) {
        when (detectedEnv) {
            EnvDetector.Env.GNOME,
            EnvDetector.Env.UNITY,
            EnvDetector.Env.CINNAMON,
            EnvDetector.Env.PANTHEON,
            EnvDetector.Env.GNOME_CLASSIC -> {
                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.screensaver", "picture-uri", file.toURI().toString()))
                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.screensaver", "picture-options", "scaled"))
                Runtime.getRuntime().exec(arrayOf("gsettings", "set", "org.gnome.desktop.screensaver", "primary-color", "#000000"))
            }
            else -> onUnsupportedLockScreen()
        }
    }

    private fun onUnsupportedDesktop() {
        Logger.w("Setting desktop wallpaper is not supported on ${detectedEnv.summary()}.")
    }

    private fun onUnsupportedLockScreen() {
        Logger.w("Setting lock-screen wallpaper is not supported on ${detectedEnv.summary()}.")
    }

    init {
        Logger.i("Running on: ${detectedEnv.name}")
    }
}