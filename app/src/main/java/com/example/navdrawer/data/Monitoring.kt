package com.example.navdrawer.data

import android.util.Log
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.function.Function


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

    var mmChData : ArrayList<ChannelData> = ArrayList()
    var hasNewData = false

    constructor() {
        for (i in 0 until MAX_CH_CNT) mmChData.add(ChannelData())
    }

    fun updateMonData(
        kind: PacketKind?,
        dataContents: ByteArray,
    ) {
        when (kind) {
            PacketKind.MonTouch -> setTouch(dataContents[0])
            PacketKind.MonPercent -> setPercent(dataContents)
            else -> {   // Do nothing
            }
        }
        Global.monitoring.hasNewData = true
    }

    fun setTouch(touch: Byte) {
        var booleanArray = Function.byteToBooleanArray(touch, TCH_CH_CNT)

        //synchronized(this) {
        synchronized(mmChData) {
            for (i in booleanArray.indices) mmChData[i].touch = booleanArray[i]
        }
    }

    private fun setPercent(contents: ByteArray) {
        var chCodeStr = String.format("%02X%02X", contents[0], contents[1])
        var percentStr = String.format("%02X%02X%02X", contents[2], contents[3], contents[4])
        var dPercent: Double

        var ch = getChannel(chCodeStr)

        if (ch != null) {
            var iPercent = percentStr.toInt(16)     // 16진수 String -> Int 변환
            if (iPercent > 8_388_608) iPercent -= 16_777_216 // 24-bit 2의 보수 처리

            dPercent = iPercent.toDouble() / SENSE_LOOP.toDouble() * BIT_RESOLUTION

            synchronized(mmChData) {
                mmChData[ch].percent = dPercent
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