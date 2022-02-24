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

open class LogParent {
    private val basePath: String = System.getProperty("java.io.tmpdir")
    open val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    open lateinit var file: File
    var prefix: String = ""
    var isEnabled:Boolean=false

    open var folderName:String=""
    open var fileName:String=""

    constructor(folderName: String, fileName: String) {
        createFile(folderName, fileName)
    }

    constructor(folderName: String, fileName: String, enabled: Boolean) {
        createFile(folderName, fileName)
        this.isEnabled = enabled
    }

    constructor(folderName: String, fileName: String, prefix: String) {
        createFile(folderName, fileName)
        this.prefix = "[$prefix]"
    }

    constructor(folderName: String, fileName: String, prefix: String, enabled:Boolean) {
        createFile(folderName, fileName)
        this.prefix = "[$prefix]"
        this.isEnabled = enabled
    }

    fun createFile(folderName: String, fileName: String) {
        this.folderName=folderName
        this.fileName=fileName
        var dir = File("${basePath}/$folderName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        file = File("$dir/$fileName")
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

//    fun printPacket(type:PacketType, str: String) {
//        if (isEnabled) {
//            val current = LocalDateTime.now()
//            val formatted = current.format(formatter)   //"yyyy-MM-dd HH:mm:ss.SSS"
//
//            // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
//            // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
//            try {
//                file.appendText("[$formatted] [$type] $str\n")
//                Log.d("[ADS] ", "Log saved at $file")
//            } catch (e: FileNotFoundException) {
//                Log.d("[ADS] ", "FileNotFound: $file")
//            }
//        }
//    }
//
//    fun printError(ex: Exception) {
//        val current = LocalDateTime.now()
//        val formatted = current.format(formatter)   //"yyyy-MM-dd HH:mm:ss.SSS"
//
//        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
//        // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
//        try {
//            file.appendText("[$formatted] $prefix ${ex.message}\n")
//            file.appendText("[$formatted] $prefix ${ex.printStackTrace()}\n")
//            Log.d("[ADS] ", "Log saved at $file")
//        } catch (e: FileNotFoundException) {
//            Log.d("[ADS] ", "FileNotFound: $file")
//        }
//    }
}