package com.example.navdrawer.function

import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

enum class PacketType{RX, TX}

class SystemLog {
    private val basePath: String = System.getProperty("java.io.tmpdir")
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    
    lateinit var dir: File
    lateinit var file: File
    var prefix: String = ""
    var isEnabled:Boolean=false

    constructor(folderName: String, fileName: String) {
        var dir = File("${basePath}/$folderName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        file = File("$dir/$fileName")
    }

    constructor(folderName: String, fileName: String, enabled: Boolean) {
        var dir = File("${basePath}/$folderName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        file = File("$dir/$fileName")
        this.isEnabled = enabled
    }

    constructor(folderName: String, fileName: String, prefix: String) {
        var dir = File("${basePath}/$folderName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        this.file = File("$dir/$fileName")
        this.prefix = "[$prefix]"
    }

    constructor(folderName: String, fileName: String, prefix: String, enabled:Boolean) {
        var dir = File("${basePath}/$folderName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        this.file = File("$dir/$fileName")
        this.prefix = "[$prefix]"
        this.isEnabled = enabled
    }

    fun print(str: String) {
        if (isEnabled) {
            val current = LocalDateTime.now()
            val formatted = current.format(formatter)   //"yyyy-MM-dd HH:mm:ss.SSS"

            // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
            // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
            try {
                file.appendText("[$formatted] $prefix $str\n")
                Log.d("[ADS] ", "Log saved at $file")
            } catch (e: FileNotFoundException) {
                Log.d("[ADS] ", "FileNotFound: $file")
            }
        }
    }

    fun printPacket(type:PacketType, str: String) {
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
        }
    }
}