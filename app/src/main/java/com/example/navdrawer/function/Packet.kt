package com.example.navdrawer.function

import android.util.Log
import com.example.navdrawer.Global
import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind
import java.io.OutputStream

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

        fun setStart(list: ArrayList<Byte>) {
            list.add(STX);    // 0x02
        }

        fun setEnd(list: ArrayList<Byte>) {
            list.add(ETX);    // 0x03
        }

        fun sendPacket(out: OutputStream?, list: ArrayList<Byte>) {
            val ba: ByteArray = list.toByteArray()
            Global.outStream!!.write(ba)
        }

        fun setChecksum(list: ArrayList<Byte>, checksum: Byte) {
            list.add(checksum);
        }

        fun setSize(list: ArrayList<Byte>, size: Int) {
            if (size < 256) {
                val str = String.format("%03d", size)
                val ca = str.toCharArray()

                for (c in ca) {
                    list.add(c.toByte())
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

