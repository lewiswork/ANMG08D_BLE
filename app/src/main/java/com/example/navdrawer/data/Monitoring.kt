package com.example.navdrawer.data

import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.function.Function

class Monitoring {
    val MAX_CH_CNT = 9
    val TCH_CH_CNT = 8
    val DM_CH_IDX = 8
    val MAX_MFM_CNT = 13

    var mmChData : ArrayList<ChannelData> = ArrayList()

    var updated = false

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
        }
        Global.monitoring.updated = true
    }

    fun setTouch(touch: Byte) {
        var booleanArray = Function.byteToBooleanArray(touch, TCH_CH_CNT)

        synchronized(this) {
            for (i in booleanArray.indices) mmChData[i].touch = booleanArray[i]
        }
    }

    fun setPercent(contents: ByteArray) {
        var chCodeStr = String.format("%02X%02X", contents[0], contents[1])
        var percentStr = String.format("%02X%02X%02X", contents[2], contents[3], contents[4])

//        synchronized(this) {
//            for (i in booleanArray.indices) mmChData[i].touch = booleanArray[i]
//        }
    }


}