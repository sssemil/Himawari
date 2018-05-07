import java.io.File
import java.io.PrintWriter

class Logger {
    companion object {
        private const val DEBUG = false

        private const val logFileName = "himawari.log"

        private var logOut = PrintWriter(File(logFileName))
        private val logLock = Object()

        fun d(msg: String) {
            if (DEBUG) {
                val msgWithInfo = "[DEBUG][${System.currentTimeMillis()}]: $msg"
                synchronized(logLock) {
                    System.out.println(msgWithInfo)
                    logOut.println(msgWithInfo)
                    logOut.flush()
                }
            }
        }

        fun i(msg: String) {
            val msgWithInfo = "[INFO][${System.currentTimeMillis()}]: $msg"
            synchronized(logLock) {
                System.out.println(msgWithInfo)
                logOut.println(msgWithInfo)
                logOut.flush()
            }
        }

        fun w(msg: String) {
            val msgWithInfo = "[WARNING][${System.currentTimeMillis()}]: $msg"
            synchronized(logLock) {
                System.out.println(msgWithInfo)
                logOut.println(msgWithInfo)
                logOut.flush()
            }
        }

        fun e(msg: String) {
            val msgWithInfo = "[ERROR][${System.currentTimeMillis()}]: $msg"
            synchronized(logLock) {
                System.err.println(msgWithInfo)
                logOut.println(msgWithInfo)
                logOut.flush()
            }
            //throw RuntimeException(msgWithInfo)
        }

        fun getTimeString(deltaTime: Long = System.currentTimeMillis()): String {
            var holder = deltaTime
            val second = holder / 1000 % 60
            holder -= second * 1000
            val minute = holder / (1000 * 60) % 60
            holder -= minute * 1000 * 60
            val hour = holder / (1000 * 60 * 60) % 24
            holder -= hour * 1000 * 60 * 24

            return String.format("%02dh:%02dm:%02ds:%04dms", hour, minute, second, holder)
        }

        fun setOutputDirectory(rootFolder: File) {
            logOut = PrintWriter(File(rootFolder, logFileName))
        }
    }
}