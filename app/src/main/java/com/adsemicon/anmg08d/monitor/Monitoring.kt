package com.adsemicon.anmg08d.monitor

import com.adsemicon.anmg08d.function.Function
import java.text.DecimalFormat


class Monitoring {
    private val channelCodes = mapOf(
        "0400" to 0,
        "0800" to 1,
        "1000" to 2,
        "2000" to 3,
        "4000" to 4,
        "8000" to 5,
        "0001" to 6,
        "0100" to 7,
        "0200" to 8
    )

    val MAX_CH_CNT = 9
    val TCH_CH_CNT = 8
    val DM_CH_IDX = 8
    val MAX_MFM_CNT = 13

    val BIT_RESOLUTION = 0.1
    val SENSE_LOOP = 13 // 임시, 강제값 사용

    var channels : ArrayList<ChannelData> = ArrayList()
    var hasNewData = false

    constructor() {
        //for (i in 0 until TCH_CH_CNT) {
        for (i in 0 until MAX_CH_CNT) {
            channels.add(ChannelData())

            if (i == DM_CH_IDX){
                if (com.adsemicon.anmg08d.Global.percentLog != null) {
                    com.adsemicon.anmg08d.Global.percentLog.headerText += " CH_DM(%)"
                }
            }else {
                //if (Global.touchLog != null && i < TCH_CH_CNT) {
                if (com.adsemicon.anmg08d.Global.touchLog != null) {
                    com.adsemicon.anmg08d.Global.touchLog.headerText += " CH${i + 1}"
                }
                if (com.adsemicon.anmg08d.Global.percentLog != null) {
                    com.adsemicon.anmg08d.Global.percentLog.headerText += " CH${i + 1}(%)"
                }
            }
        }
    }

    fun updateMonData(
        kind: com.adsemicon.anmg08d.PacketKind?,
        dataContents: ByteArray,
    ) {
        when (kind) {
            com.adsemicon.anmg08d.PacketKind.MonTouch -> setTouch(dataContents[0])
            com.adsemicon.anmg08d.PacketKind.MonPercent -> setPercent(dataContents)
            else -> { }  // Do nothing
        }
        com.adsemicon.anmg08d.Global.monitoring.hasNewData = true
    }

    private fun setTouch(touch: Byte) {
        var boolArray = Function.byteToBooleanArray(touch, TCH_CH_CNT)

        synchronized(channels) {
            for (i in boolArray.indices) channels[i].touch = boolArray[i]
        }

        logMonData()
    }

    private fun logMonData() {
        var logStr :String

        // Touch Log
        if (com.adsemicon.anmg08d.Global.touchLog.isEnabled) {
            logStr = ""
            for (i in 0 until TCH_CH_CNT) {
                synchronized(channels) {
                    logStr += if (channels[i].touch) " 1" else " 0"
                }
            }
            com.adsemicon.anmg08d.Global.touchLog.printMonData(logStr)
        }

        // Percent Log(Touch Log 와 데이터 저장 시점 일치 목적)
        if (com.adsemicon.anmg08d.Global.percentLog.isEnabled) {
            logStr = ""
            for (i in 0 until MAX_CH_CNT) {
                synchronized(channels) {
                    var percent = channels[i].percent
                    var df = DecimalFormat("0.000")
                    logStr += " ${df.format(percent)}"
                }
            }
            com.adsemicon.anmg08d.Global.percentLog.printMonData(logStr)
        }
    }

    private fun setPercent(contents: ByteArray) {
        var chCodeStr = String.format("%02X%02X", contents[0], contents[1])
        var percentStr = String.format("%02X%02X%02X", contents[2], contents[3], contents[4])
        var dPercent: Double

        var ch = getChannel(chCodeStr)

        if (ch != null) {
            var iPercent = percentStr.toInt(16)            // 16진수 String -> Int 변환
            if (iPercent > 8_388_608) iPercent -= 16_777_216    // 24-bit 2의 보수 처리

            dPercent = iPercent.toDouble() / SENSE_LOOP.toDouble() * BIT_RESOLUTION

            synchronized(channels) {
                channels[ch].percent = dPercent
            }
        }
        //Log.d("[ADS] ", "Percent set : CH$ch / ${dPercent.toString()} %")
    }

    private fun getChannel(chStr: String): Int? {

        //        //중간의 Disable된 채널에 대한 처리 (구현 예정)
//        while (!m_baChEnable.get(chidx)) {
//            chidx--
//            if (chidx == -1) chidx = Define.DM_CH_CNT
//            if (chidx == idx) break //한바퀴 돌아 원위치 빠져 나옴
//        }

        return channelCodes[chStr]
    }


}