package com.example.navdrawer.function

import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.*

class SystemLog {
    private val basePath: String = System.getProperty("java.io.tmpdir")
    lateinit var dir: File
    lateinit var file: File

    //private val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss:")

    constructor(pathName: String, fileName: String) {
        var dir = File("${basePath}/$pathName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        file = File("$dir/$fileName")
    }

    fun print(str: String) {
        var now = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
            .withZone(ZoneOffset.UTC)
            .format(Instant.now())

        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
        // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
        try {
            file.appendText("[$now] $str\n")
//            Log.d("[ADS] ", "calendar.timeInMillis : ${now}")
//            Log.d("[ADS] ", "File saved at : $file")
        } catch (e: FileNotFoundException) {
            Log.d("[ADS] ", "FileNotFound: $file")
        }
    }
}