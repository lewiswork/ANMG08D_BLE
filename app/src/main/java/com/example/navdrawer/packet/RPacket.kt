package com.example.navdrawer.packet

import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind

data class RPacket (
    val category: PacketCategory? = null,
    val kind: PacketKind? = null,
    val dataLength: Int,
    val dataList: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RPacket

        if (category != other.category) return false
        if (kind != other.kind) return false
        if (dataLength != other.dataLength) return false
        if (!dataList.contentEquals(other.dataList)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = category?.hashCode() ?: 0
        result = 31 * result + (kind?.hashCode() ?: 0)
        result = 31 * result + dataLength
        result = 31 * result + dataList.contentHashCode()
        return result
    }
}
