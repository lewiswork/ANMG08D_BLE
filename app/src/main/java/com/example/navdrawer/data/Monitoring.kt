package com.example.navdrawer.data

import android.util.Log

class Monitoring {
    val MAX_CH_CNT = 9
    val TCH_CH_CNT = 8
    val DM_CH_IDX = 8
    val MAX_MFM_CNT = 13

    var mmChData : ArrayList<ChannelData> = ArrayList()

    constructor() {
        for (i in 0 until MAX_CH_CNT) {
            mmChData.add(ChannelData())
        }
        //Log.d("ME", mmChData.count().toString())
    }
}