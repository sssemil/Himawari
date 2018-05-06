import com.google.gson.GsonBuilder
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO


fun main(args: Array<String>) = runBlocking {
    while (true) {
        val ipData = getIpInfo()
        val latestInfo = getLatestInfo()

        Logger.i("ISP: $ipData")
        Logger.i("latestInfo: ${latestInfo.toString()}")

        if (latestInfo?.equals(previousInfo) == false) {
            previousInfo = latestInfo
            if (!mobileIsps.contains(ipData)) {
                val size = 4
                val matrix: Array<Array<Deferred<BufferedImage>>> = Array(size, { x ->
                    Array(size, { y ->
                        async {
                            getImage(size, latestInfo.date, x, y)
                        }
                    })
                })

                val type = matrix[0][0].await().type
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

                ImageIO.write(finalImg, "png", File(rootFolder, latestInfo.file))
            }
        }

        delay(1000)
    }
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


private fun getLatestInfo(): HimawariLatest? {
    val obj = URL(urlLatest)

    return with(obj.openConnection() as HttpURLConnection) {
        Logger.i("URL: $urlLatest; Response Code: $responseCode")

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


private fun getIpInfo(): String {
    val url = "http://ip-api.com/line/?fields=isp"
    val obj = URL(url)

    return with(obj.openConnection() as HttpURLConnection) {
        println("Sending 'GET' request to URL : $url")
        println("Response Code : $responseCode")

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

private val rootFolder = File("img/")
private val gson = GsonBuilder().setDateFormat("yyyy-mm-dd HH:mm:ss").create()
private val mobileIsps = arrayOf("O2 Deutschland")
private const val urlLatest = "http://himawari8-dl.nict.go.jp/himawari8/img/D531106/latest.json"
private var previousInfo: HimawariLatest? = null

data class HimawariLatest(val date: Date, val file: String)