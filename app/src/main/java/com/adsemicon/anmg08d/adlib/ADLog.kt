package com.adsemicon.anmg08d.adlib

import android.util.Log
//import kr.co.blue.adlib.Thread.ADThread
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


enum class LogKind { None, Log, Error, PKLogRx, PKLogTx }

class LogMsg {

    var kind : LogKind = LogKind.None
        private set
    private var time : String = ""
    private var msg : String = ""

    constructor(logKind:LogKind, logMsg:String) {
        kind = logKind
        time = getTimeStr()
        msg = logMsg
    }

    private fun getTimeStr() : String {
        var curDate = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("[HH:mm:ss:SSS]")
        return dateFormat.format(curDate)
    }

    public fun get() : String {
        return String.format("%s %s\n", time, msg)
    }
}

class ADLog : ADThread {
    companion object {
        private const val BASE_LOG_PATH = "./Log/"

        const val LOGOUT_FLAG_NONE : Int = 0x00
        const val LOGOUT_FLAG_LOG : Int = 0x01
        const val LOGOUT_FLAG_PKLOG : Int = 0x02

        private var logQueue: Queue<LogMsg> = LinkedList()
        private var isLogFileHour = false                   //시간단위 Log파일 생성 여부
        var outFlag = LOGOUT_FLAG_NONE
            private set
        var logPath = ""
            private set
        var appName = ""
            private set

        private var isLogOut = false
        private var isPKLogOut = false

        private var prefixName = ""
        private var fileDateStr = ""
        private var logName = ""
        private var errName = ""
        private var rxPKName = ""
        private var txPKName = ""

        private fun getFileDateStr(): String {
            val curTime = System.currentTimeMillis()
            var df : SimpleDateFormat
            if(isLogFileHour)   df = SimpleDateFormat("yyyyMMdd_hh")
            else                df = SimpleDateFormat("yyyyMMdd")
            return df.format(curTime)
        }

        private fun setFileName() {
            fileDateStr = getFileDateStr()
            logName = String.format("%s%s.log", prefixName, fileDateStr)
            errName = String.format("%s%s.err", prefixName, fileDateStr)
            rxPKName = String.format("%s%s.rpk", prefixName, fileDateStr)
            txPKName = String.format("%s%s.tpk", prefixName, fileDateStr)
        }

        private fun setInfo() {
            //Directory 확인
            val dir = File("$logPath")
            if(!dir.exists()) dir.mkdirs()
            prefixName = String.format("%s/%s_", logPath, appName)
            //File 이름 설정
            setFileName()
        }

        private fun chkDateChg() {
            val curTime = getFileDateStr()
            if(!curTime.equals(fileDateStr)) setFileName()
        }

        fun chgPath(path:String) {
            logPath = path
            setInfo()
        }

        fun chgAppName(applicationName:String) {
            appName = applicationName
            setInfo()
        }

        fun chgOutFlag(flag:Int) {
            isLogOut = ((flag and LOGOUT_FLAG_LOG) == LOGOUT_FLAG_LOG)
            isPKLogOut = ((flag and LOGOUT_FLAG_PKLOG) == LOGOUT_FLAG_PKLOG)
        }

        fun print(kind:LogKind, message:String) {
            if((kind == LogKind.Log) and (!isLogOut)) return
            else if(((kind == LogKind.PKLogRx) or (kind == LogKind.PKLogTx)) and (!isPKLogOut)) return

            val lm = LogMsg(kind, message)
            synchronized(logQueue) {
                logQueue.add(lm)
            }
        }
    }

    constructor(applicationName:String, OutFlag:Int = LOGOUT_FLAG_NONE, hourFlag:Boolean = false) {
        logPath = BASE_LOG_PATH
        appName = applicationName
        chgOutFlag(OutFlag)
        isLogFileHour = hourFlag
        setInfo()
    }

    constructor(path:String, applicationName:String, OutFlag:Int = LOGOUT_FLAG_NONE, hourFlag:Boolean = false) {
        logPath = path
        appName = applicationName
        chgOutFlag(OutFlag)
        isLogFileHour = hourFlag
        setInfo()
    }

    override fun doWork() {
        var msgCnt = 0
        var msg : LogMsg

        try{
            synchronized(logQueue) { msgCnt = logQueue.count() }
            if(msgCnt == 0) return

            chkDateChg()

            synchronized(logQueue) { msg = logQueue.remove() }

            var fName = when(msg.kind) {
                LogKind.Error -> errName
                LogKind.Log -> {
                    if(!isLogOut) return
                    logName
                }
                LogKind.PKLogRx -> {
                    if(!isPKLogOut) return
                    rxPKName
                }
                LogKind.PKLogTx -> {
                    if(!isPKLogOut) return
                    txPKName
                }
                else ->
                    throw NoSuchFieldException()
            }

            val file = File("$fName")
            file.appendText(msg.get())

        }
        catch(e:Exception) {
            Log.d("Error", "[ADLog] LogKind Error")
        }
    }
}