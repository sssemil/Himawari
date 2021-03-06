package sssemil.com

import org.apache.commons.cli.*
import sssemil.com.utils.Logger
import java.io.File

fun main(args: Array<String>) {
    val options = Options()

    val helpOption = Option("h", "help", false, "show help.")
    helpOption.isRequired = false
    options.addOption(helpOption)

    val levelOption = Option("l", "level", true, "increases the quality (and the size) of each tile. Possible values are 4, 8, 16, 20")
    levelOption.isRequired = false
    options.addOption(levelOption)

    val delayOption = Option("d", "delay", true, "delay in ms between each check for a new image. Default is 1 second.")
    delayOption.isRequired = false
    options.addOption(delayOption)

    val checkForModuleNetworkOption = Option("m", "check-mobile", false, "downloads only on a not cellular network.")
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

    val setDesktopBgOption = Option("db", "set-desktop", false, "set as desktop background.")
    setDesktopBgOption.isRequired = false
    options.addOption(setDesktopBgOption)

    val setLockScreenBgOption = Option("sb", "set-lock-screen", false, "set as lock-screen background.")
    setLockScreenBgOption.isRequired = false
    options.addOption(setLockScreenBgOption)

    val saveLogsOption = Option("w", "save-logs", false, "save logs to file.")
    saveLogsOption.isRequired = false
    options.addOption(saveLogsOption)

    val noGuiOption = Option("ng", "no-gui", false, "don't start GUI.")
    noGuiOption.isRequired = false
    options.addOption(noGuiOption)

    val parser = DefaultParser()
    val formatter = HelpFormatter()
    val cmd: CommandLine

    try {
        cmd = parser.parse(options, args)
    } catch (e: ParseException) {
        e.message?.let { Logger.e(it) }
        formatter.printHelp(EXECUTABLE_NAME, options)

        System.exit(1)
        return
    }

    if (cmd.hasOption(helpOption.longOpt)) {
        formatter.printHelp(EXECUTABLE_NAME, options)

        System.exit(1)
        return
    }

    Logger.writeLogsToFile = cmd.hasOption(saveLogsOption.longOpt)

    val himawari = Himawari(checkMobileNet = cmd.hasOption(checkForModuleNetworkOption.longOpt),
            singleLoop = cmd.hasOption(singleLoopOption.longOpt),
            setDesktopBg = cmd.hasOption(setDesktopBgOption.longOpt),
            setLockScreenBg = cmd.hasOption(setLockScreenBgOption.longOpt))

    if (cmd.hasOption(levelOption.longOpt)) {
        himawari.level = cmd.getOptionValue(levelOption.longOpt).toInt()
    }

    if (cmd.hasOption(delayOption.longOpt)) {
        himawari.delay = cmd.getOptionValue(delayOption.longOpt).toInt()
    }

    if (cmd.hasOption(outFileNameOption.longOpt)) {
        himawari.outFileName = cmd.getOptionValue(outFileNameOption.longOpt)
    }

    if (cmd.hasOption(outDirOption.longOpt)) {
        himawari.rootFolder = File(cmd.getOptionValue(outDirOption.longOpt))
    }

    Logger.setOutputDirectory(himawari.rootFolder)

    himawari.run()
}

private const val EXECUTABLE_NAME = "himawari"