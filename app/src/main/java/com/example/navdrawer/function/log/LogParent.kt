package com.example.navdrawer.function

import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.navdrawer.Global
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
    //private val basePath: String = System.getProperty("java.io.tmpdir")
    lateinit var basePath :File

    open val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
    open lateinit var file: File
    var prefix: String = ""
    var isEnabled:Boolean=false

    open var folderName:String=""
    open var fileName:String=""

    constructor(context: Context, folderName: String, fileName: String) {
        createFile(context, folderName, fileName)
    }

    constructor(context: Context, folderName: String, fileName: String, enabled: Boolean) {
        createFile(context, folderName, fileName)
        this.isEnabled = enabled
    }

    constructor(context: Context, folderName: String, fileName: String, prefix: String, enabled:Boolean) {
        createFile(context, folderName, fileName)
        this.prefix = "[$prefix]"
        this.isEnabled = enabled
    }

    fun createFile(context:Context, folderName: String, fileName: String) {
        val externalStorageVolumes: Array<out File> =
            ContextCompat.getExternalFilesDirs(context, null)
        basePath = externalStorageVolumes[0]

        this.folderName = folderName
        this.fileName = fileName
        var dir = File("${basePath}/$folderName")
        if (!dir.exists()) {
            dir.mkdirs()
            Log.d("[ADS] ", "Folder created at $dir")
        }
        file = File("$dir/$fileName")
    }
}