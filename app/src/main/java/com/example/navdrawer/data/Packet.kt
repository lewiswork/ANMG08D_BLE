package com.example.navdrawer.data

import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind

data class Packet (
    val category: PacketCategory? = null,
    val kind: PacketKind? = null,
    val dataLength: Int,
    val dataList: ArrayList<Byte> = ArrayList()
)