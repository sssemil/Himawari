package sssemil.com

import com.google.gson.GsonBuilder
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.utils.Logger
import sssemil.com.utils.WallpaperChanger
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO

class Himawari(var checkMobileNet: Boolean = false,
               var singleLoop: Boolean = false,
               var setDesktopBg: Boolean = false,
               var setLockScreenBg: Boolean = false,
               var outFileName: String? = null,
               var level: Int = 4,
               var delay: Int = 1000,
               var rootFolder: File = File(".")) {
    private var previousInfo: HimawariLatest? = null
    private val gson = GsonBuilder().setDateFormat("yyyy-mm-dd HH:mm:ss").create()

    fun run() = runBlocking {
        while (true) {
            val latestInfo = getLatestInfo()

            Logger.i("LatestInfo: ${latestInfo.toString()}")

            if (latestInfo?.equals(previousInfo) == false) {
                previousInfo = latestInfo
                if (checkMobileNet && isOnMobile()) {
                    val matrix: Array<Array<Deferred<BufferedImage>>> = Array(level, { x ->
                        Array(level, { y ->
                            async {
                                getImage(level, latestInfo.date, x, y)
                            }
                        })
                    })

                    val type = BufferedImage.TYPE_3BYTE_BGR
                    val chunkWidth = matrix[0][0].await().width
                    val chunkHeight = matrix[0][0].await().height

                    val finalImg = BufferedImage(chunkWidth * matrix.size, chunkHeight * matrix[0].size, type)

                    matrix.forEachIndexed({ x, array ->
                        array.forEachIndexed({ y, chunk ->
                            chunk.await().let {
                                finalImg.createGraphics().drawImage(it, it.width * x, it.height * y, null)
                            }
                        })
                    })

                    rootFolder.mkdirs()

                    val outFile = File(rootFolder, outFileName ?: latestInfo.file)
                    Logger.i("Wring downloaded image to file: ${outFile.toURI()}")
                    if (outFile.exists()) {
                        Logger.i("File exists, removing first.")
                        outFile.delete()
                    }
                    ImageIO.write(finalImg, "png", outFile)

                    val wallpaperChanger = WallpaperChanger()

                    if (setDesktopBg) {
                        Logger.i("Setting it as the desktop background.")
                        wallpaperChanger.setDesktop(outFile)
                    }

                    if (setLockScreenBg) {
                        Logger.i("Setting it as the lock-screen background.")
                        wallpaperChanger.setLockScreen(outFile)
                    }
                }
            }

            if (singleLoop) {
                Logger.i("Single loop parameter specified, exiting.")
                break
            }

            delay(delay)
        }
    }

    private fun getLatestInfo(): HimawariLatest? {
        val obj = URL(urlLatest)

        return with(obj.openConnection() as HttpURLConnection) {
            Logger.i("URL: ${urlLatest}; Response Code: $responseCode")

            BufferedReader(InputStreamReader(inputStream)).use {
                val response = StringBuilder()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                return@with gson.fromJson(response.toString(), HimawariLatest::class.java)
            }
        }
    }

    companion object {
        private fun isOnMobile(): Boolean {
            val ipData = getIpInfo()
            Logger.i("ISP: $ipData")

            return !mobileIsps.contains(ipData)
        }

        private fun getImage(size: Int = 8, date: Date, x: Int, y: Int): BufferedImage {
            val dateFormatted = SimpleDateFormat("yyyy/MM/dd/HHmmss").format(date)
            val url = "http://himawari8.nict.go.jp/img/D531106/${size}d/550/${dateFormatted}_${x}_$y.png"
            val obj = URL(url)

            return with(obj.openConnection() as HttpURLConnection) {
                Logger.i("URL: $url; Response Code: $responseCode")
                ImageIO.read(inputStream)
            }
        }

        private fun getIpInfo(): String {
            val obj = URL(urlIpInfo)

            return with(obj.openConnection() as HttpURLConnection) {
                println("URL: $url; Response Code: $responseCode")

                BufferedReader(InputStreamReader(inputStream)).use {
                    val response = StringBuilder()

                    var inputLine = it.readLine()
                    while (inputLine != null) {
                        response.append(inputLine)
                        inputLine = it.readLine()
                    }
                    return@with response.toString()
                }
            }
        }

        private val mobileIsps = arrayOf("O2 Deutschland")
        private const val urlIpInfo = "http://ip-api.com/line/?fields=isp"
        private const val urlLatest = "http://himawari8-dl.nict.go.jp/himawari8/img/D531106/latest.json"
    }
}

data class HimawariLatest(val date: Date, val file: String)