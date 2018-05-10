package sssemil.com.utils

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.win32.W32APIFunctionMapper
import com.sun.jna.win32.W32APITypeMapper

interface SPI : StdCallLibrary {

    fun SystemParametersInfo(
            uiAction: WinDef.UINT_PTR,
            uiParam: WinDef.UINT_PTR,
            pvParam: String,
            fWinIni: WinDef.UINT_PTR
    ): Boolean

    companion object {

        //from MSDN article
        const val SPI_SETDESKWALLPAPER: Long = 20
        const val SPIF_UPDATEINIFILE: Long = 0x01
        const val SPIF_SENDWININICHANGE: Long = 0x02

        val INSTANCE = Native.loadLibrary("user32", SPI::class.java, hashMapOf(
                Library.OPTION_TYPE_MAPPER to W32APITypeMapper.UNICODE,
                Library.OPTION_FUNCTION_MAPPER to W32APIFunctionMapper.UNICODE
        )) as SPI
    }
}