package com.example.navdrawer.data

import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind

//class Packet {
//    val category: PacketCategory? = null
//    val kind: PacketKind? = null
//    val dataLength: Int = 0
//    val dataList: ArrayList<Byte> = ArrayList()
//}

data class Packet (
    val category: PacketCategory? = null,
    val kind: PacketKind? = null,
    val dataLength: Int,
    val dataList: ArrayList<Byte> = ArrayList()
)