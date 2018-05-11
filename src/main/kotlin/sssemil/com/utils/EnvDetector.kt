package sssemil.com.utils

class EnvDetector {
    companion object {

        private val osName = System.getProperty("os.name")?.toLowerCase()
        private val sessionName = System.getenv("DESKTOP_SESSION")?.toLowerCase()

        val environment: Env
            get() {
                if (osName == null) return Env.UNSUPPORTED

                when {
                    osName.startsWith("windows") -> {
                        Env.WINDOWS
                    }
                    osName.startsWith("darwin") -> {
                        Env.DARWIN
                    }
                    else -> {
                        sessionName?.let {
                            if (sessionName in arrayOf("gnome", "unity", "cinnamon", "mate", "xfce4", "lxde", "fluxbox",
                                            "blackbox", "openbox", "icewm", "jwm", "afterstep", "trinity", "kde", "pantheon",
                                            "gnome-classic", "i3")) {
                                return Env.valueOf(sessionName)
                            }

                            if (sessionName.contains("xfce") || sessionName.startsWith("xubuntu")) {
                                return Env.XFCE4
                            }

                            if (sessionName.startsWith("ubuntu")) {
                                return Env.UNITY
                            }

                            if (sessionName.startsWith("lubuntu")) {
                                return Env.LXDE
                            }

                            if (sessionName.startsWith("kubuntu")) {
                                return Env.KDE
                            }

                            if (sessionName.startsWith("razor")) {
                                return Env.RAZOR_QT
                            }

                            if (sessionName.startsWith("wmaker")) {
                                return Env.WINDOW_MAKER
                            }

                            if (sessionName.startsWith("peppermint")) {
                                return Env.GNOME
                            }
                        }

                        if (System.getenv("KDE_FULL_SESSION")?.toBoolean() == true) {
                            return Env.KDE
                        }

                        System.getenv("GNOME_DESKTOP_SESSION_ID")?.let {
                            if (!it.contains("deprecated")) {
                                return Env.GNOME2
                            }
                        }

                        if (isRunning("xfce-mcs-manage")) {
                            return Env.XFCE4
                        }

                        if (isRunning("ksmserver")) {
                            return Env.KDE
                        }
                    }
                }

                return Env.UNSUPPORTED
            }

        fun isRunning(processName: String) =
                Runtime.getRuntime().exec(arrayOf("pidof", "--", processName)).waitFor() == 0
    }

    enum class Env {
        WINDOWS,
        DARWIN,
        GNOME,
        UNITY,
        CINNAMON,
        MATE,
        XFCE4,
        LXDE,
        FLUXBOX,
        BLACKBOX,
        OPENBOX,
        ICEWM,
        JWM,
        AFTERSTEP,
        TRINITY,
        KDE,
        PANTHEON,
        GNOME_CLASSIC,
        I3,
        RAZOR_QT,
        UNSUPPORTED,
        WINDOW_MAKER,
        GNOME2;

        fun summary(): String {
            return "$osName $sessionName "
        }
    }
}