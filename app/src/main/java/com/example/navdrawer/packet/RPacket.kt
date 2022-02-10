package com.example.navdrawer.packet

import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind

data class RPacket (
    val category: PacketCategory? = null,
    val kind: PacketKind? = null,
    val dataLength: Int,
    val dataList: ByteArray
)
