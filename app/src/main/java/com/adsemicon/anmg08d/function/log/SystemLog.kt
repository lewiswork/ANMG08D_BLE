package com.adsemicon.anmg08d.function.log

import android.content.Context
import android.util.Log
import com.adsemicon.anmg08d.function.LogParent
import java.io.FileNotFoundException
import java.time.LocalDateTime

enum class PacketType{RX, TX}

class SystemLog : LogParent {
    constructor(context: Context, folderName: String, fileName: String) : super(context,
        folderName,
        fileName)

    constructor(
        context: Context,
        folderName: String,
        fileName: String,
        prefix: String,
        enabled: Boolean
    ) : super(
        context,
        folderName,
        fileName,
        prefix,
        enabled)

    fun printPacket(type: PacketType, str: String) {
        if (isEnabled) {
            val current = LocalDateTime.now()
            val formatted = current.format(formatter)   //"yyyy-MM-dd HH:mm:ss.SSS"

            // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
            // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
            try {
                file.appendText("[$formatted] [$type] $str\n")
                Log.d("[ADS] ", "Log saved at $file")
            } catch (e: FileNotFoundException) {
                Log.d("[ADS] ", "FileNotFound: $file")
                createFile(com.adsemicon.anmg08d.GlobalVariables.contextMain, this.folderName, this.fileName)
            } catch (ex: java.lang.Exception) {
                Log.d("[ADS] ", ex.toString())
            }
        }
    }

    fun printError(ex: Exception) {
        val current = LocalDateTime.now()
        val formatted = current.format(formatter)   //"yyyy-MM-dd HH:mm:ss.SSS"

        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
        // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
        try {
            file.appendText("[$formatted] $prefix ${ex.message}\n")
            file.appendText("[$formatted] $prefix ${ex.printStackTrace()}\n")
            Log.d("[ADS] ", "Log saved at $file")
        } catch (e: FileNotFoundException) {
            Log.d("[ADS] ", "FileNotFound: $file")
            createFile(com.adsemicon.anmg08d.GlobalVariables.contextMain, this.folderName, this.fileName)
        } catch (ex: java.lang.Exception) {
            Log.d("[ADS] ", ex.toString())
        }
    }
}