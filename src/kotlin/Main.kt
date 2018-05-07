import com.google.gson.GsonBuilder
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.apache.commons.cli.*
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
    val options = Options()

    val levelOption = Option("l", "level", true, "increases the quality (and the size) of each tile. Possible values are 4, 8, 16, 20")
    levelOption.isRequired = false
    options.addOption(levelOption)

    val delayOption = Option("d", "delay", true, "delay in ms between each check for a new image. Default is 1 second.")
    delayOption.isRequired = false
    options.addOption(delayOption)

    val checkForModuleNetworkOption = Option("m", "check-mobile", false, "downloads only on a not cellular network. For now only \"O2 Deutschland\" is supported.")
    checkForModuleNetworkOption.isRequired = false
    options.addOption(checkForModuleNetworkOption)

    val singleLoopOption = Option("s", "single-loop", false, "image will be downloaded only once.")
    singleLoopOption.isRequired = false
    options.addOption(singleLoopOption)

    val outDirOption = Option("o", "out-dir", true, "out directory for downloaded images and logs. \"output/\" is default.")
    outDirOption.isRequired = false
    options.addOption(outDirOption)

    val outFileNameOption = Option("f", "image-file-name", true, "file name for downloaded image. Default from the server by default")
    outFileNameOption.isRequired = false
    options.addOption(outFileNameOption)

    val parser = DefaultParser()
    val formatter = HelpFormatter()
    val cmd: CommandLine

    try {
        cmd = parser.parse(options, args)
    } catch (e: ParseException) {
        e.message?.let { Logger.e(it) }
        formatter.printHelp("himawari", options)

        System.exit(1)
        return@runBlocking
    }

    var rootFolder = File("output/")
    if (cmd.hasOption(outDirOption.longOpt)) {
        rootFolder = File(cmd.getOptionValue(outDirOption.longOpt))
    }
    Logger.setOutputDirectory(rootFolder)

    var level = 4
    if (cmd.hasOption(levelOption.longOpt)) {
        level = cmd.getOptionValue(levelOption.longOpt).toInt()
    }

    var delay = 1000
    if (cmd.hasOption(delayOption.longOpt)) {
        delay = cmd.getOptionValue(delayOption.longOpt).toInt()
    }

    while (true) {
        val latestInfo = getLatestInfo()

        Logger.i("LatestInfo: ${latestInfo.toString()}")

        if (latestInfo?.equals(previousInfo) == false) {
            previousInfo = latestInfo
            if (cmd.hasOption(checkForModuleNetworkOption.longOpt) && isOnMobile()) {
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

                val outFile = File(rootFolder,
                        if (cmd.hasOption(outFileNameOption.longOpt))
                            cmd.getOptionValue(outFileNameOption.longOpt)
                        else
                            latestInfo.file)

                Logger.i("Wring downloaded image to file: ${outFile.path}")
                ImageIO.write(finalImg, "png", outFile)
            }
        }

        if (cmd.hasOption(singleLoopOption.longOpt)) {
            Logger.i("${singleLoopOption.longOpt} parameter specified, exiting.")
            break
        }

        delay(delay)
    }
}

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

private val gson = GsonBuilder().setDateFormat("yyyy-mm-dd HH:mm:ss").create()
private val mobileIsps = arrayOf("O2 Deutschland")
private const val urlIpInfo = "http://ip-api.com/line/?fields=isp"
private const val urlLatest = "http://himawari8-dl.nict.go.jp/himawari8/img/D531106/latest.json"
private var previousInfo: HimawariLatest? = null

data class HimawariLatest(val date: Date, val file: String)