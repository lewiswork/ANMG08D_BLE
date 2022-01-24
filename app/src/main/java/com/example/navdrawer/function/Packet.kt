package com.example.navdrawer.function

import android.util.Log
import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind

class Packet {
    companion object {

        val STX : Byte = 0x02
        val ETX : Byte = 0x03

        val packetCategory = mapOf(
            "E" to PacketCategory.Rom,
            "M" to PacketCategory.Monitoring,
            "R" to PacketCategory.Register,
            "H" to PacketCategory.Hardware,
            "T" to PacketCategory.Test
        )

        val packetKind = mapOf(
            "HW" to PacketKind.HwWrite,
            "HR" to PacketKind.HwRead,

            "MT" to PacketKind.MonTouch,
            "MP" to PacketKind.MonPercent
        )

        fun setSize(ar: ArrayList<Byte>, size: Int) {
            if (size < 256) {
                val str = String.format("%03d", size)
                val ca = str.toCharArray()

                for (c in ca) {
                    ar.add(c.toByte())
                }
            } else {
                Log.d("[ADS]", "Data size error while making packet.")
            }
        }

        fun makeChecksum(data :Byte):Byte {
            var calcVal: UInt = 0u

            calcVal = data.toUInt().inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }

        fun makeChecksum(buf :ByteArray):Byte {
            var calcVal: UInt = 0u

            for (data in buf) {
                calcVal += data.toUInt()
            }
            calcVal = calcVal.inv()
            calcVal = calcVal and 0x000000ff.toUInt()
            calcVal++

            return calcVal.toByte()
        }
    }
}

