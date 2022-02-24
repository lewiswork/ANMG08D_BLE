package com.example.navdrawer.function.log

import android.util.Log
import com.example.navdrawer.function.LogParent
import java.io.FileNotFoundException
import java.lang.Exception
import java.time.LocalDateTime

class MonitoringLog:LogParent {

    var headerText: String = ""

    constructor(folderName: String, fileName: String) : super(folderName, fileName)

    constructor(folderName: String, fileName: String, enabled: Boolean) : super(
        folderName,
        fileName,
        enabled)

    fun printMonData(str: String) {
        if (isEnabled) {
            val current = LocalDateTime.now()
            val formatted = current.format(formatter)   //"yyyy-MM-dd HH:mm:ss.SSS"

            // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
            // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
            try {
                // Write Header Text
                if (!file.exists()){
                    file.appendText("$headerText\n")
                }

                file.appendText("[$formatted]$str\n")
                Log.d("[ADS] ", "Log saved at $file")
            } catch (ex: FileNotFoundException) {
                Log.d("[ADS] ", "FileNotFound: $file")
                createFile(this.folderName, this.fileName)
            }
            catch (ex:Exception){
                Log.d("[ADS] ", ex.toString())
            }
        }
    }
}