package com.example.navdrawer.thread

import android.util.Log
import com.example.navdrawer.Global
import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind
import com.example.navdrawer.data.Packet
import java.lang.StringBuilder
import java.util.*
import kotlin.NoSuchElementException
import kotlin.collections.ArrayList

enum class ExtractMode{ Header, Body }    // Header : STX~LEN, Body : Data~ETX

class GetPacketThread:Thread() {

    val STX : Byte = 0x02
    val ETX : Byte = 0x03

    val SZ_UNTIL_LEN = 6
    val IDX_DATA_START = 6

    var mmCurIdx : Int = 0
    var mmRawByteList = mutableListOf<Byte>()
    //var mmRawByteList = mutableListOf<ByteArray>()
    var mmDataList : ArrayList<Byte> = ArrayList()
    var mmExtractMode  = ExtractMode.Header

    var mmLogStr = StringBuilder()

    override fun run() {
        super.run()

        var qEmpty = true



        var dataLength : Int = 0
        var checksum : Byte = 0x00
        var category : PacketCategory? = null
        var kind : PacketKind? = null
        var rawByteArray = byteArrayOf()

        Log.d("ME", "Get packet thread started. ID : ${this.id}")

        while (Global.rxPacketThreadOn) {
            try {
                //------------------------------------------------------------------------------//
                // rawByteQueue 데이터 -> byteList 로 이동
                //------------------------------------------------------------------------------//
                synchronized(this) { qEmpty = Global.rawByteQueue.isEmpty() }
                if (!qEmpty) {
                    try {
                        synchronized(this) { rawByteArray = Global.rawByteQueue.remove() }

                        var len = rawByteArray.count()
                        for (i in 0 until len) {
                            mmRawByteList.add(rawByteArray[i])
                        }

                        //  Log 저장(DEBUG)
                        var str=StringBuilder()
                        for (element in rawByteArray) {
                            str.append(String.format("%02X", element))
                            str.append(" ")
                        }
                        Log.d("ME", "[RX RAW] $str")

//                        // 발췌 Log 저장(DEBUG)
//                        str=StringBuilder()
//                        for (i in 0 until mmRawByteList.size) {
//                            str.append(String.format("%02X ", mmRawByteList[i]))
//                        }
//                        Log.d("ME", "[RX BUF] $str")
                    } catch (ex: NoSuchElementException) {
                        Log.d("MEX", Global.rawByteQueue.count().toString())
                        //ex.printStackTrace()
                        //continue
                        break
                    }
                }
                //------------------------------------------------------------------------------//

                //------------------------------------------------------------------------------//
                // byteList 데이터 -> Packet 추출
                //------------------------------------------------------------------------------//
                    if (mmRawByteList.count() > 0) {

                        if (mmExtractMode == ExtractMode.Header) {
                            if (mmRawByteList.count() < SZ_UNTIL_LEN) continue

                            // STX
                            //mmCurIdx++
                            mmCurIdx = SZ_UNTIL_LEN  // Error 시 mmCurIdx 까지 버림
                            if (mmRawByteList[0] != STX) {
                                Log.d("ME", "STX Error ! : ${mmRawByteList[0]}")
                                if (clearRawByteList()) continue    // Error 처리
                            }

                            // Check Header : if in range of A to Z
                            //mmCurIdx+=2
                            if ((mmRawByteList[1] in 0x41..0x5a) && (mmRawByteList[2] in 0x41..0x5a)) {
                                val str1 = mmRawByteList[1].toChar().toString()
                                val str2 = mmRawByteList[2].toChar().toString()

                                category = Global.packetCategory[str1]
                                kind = Global.packetKind["$str1$str2"]

                                if (category == null || kind == null){
                                    Log.d("ME", "Packet header error : $str1$str2")
                                    if (clearRawByteList()) continue    // Error 처리
                                }
                            } else {
                                Log.d("ME", "Packet Header Error!!")
                                if (clearRawByteList()) continue    // Error 처리
                            }

                            // Check Length : if in range of '0' to '9'
                            //mmCurIdx+=3
                            if ((mmRawByteList[3] in 0x30..0x39) && (mmRawByteList[4] in 0x30..0x39)
                                && (mmRawByteList[5] in 0x30..0x39)
                            ) {
                                dataLength =
                                    (mmRawByteList[3].toInt() - 0x30) * 100 + (mmRawByteList[4].toInt() - 0x30) * 10 + (mmRawByteList[5].toInt() - 0x30)
                                mmExtractMode = ExtractMode.Body
                            } else {
                                Log.d("ME", "Packet Length Error!!")
                                if (clearRawByteList()) continue    // Error 처리
                            }
                        } else if (mmExtractMode == ExtractMode.Body) {

                            if (mmRawByteList.count() < SZ_UNTIL_LEN + dataLength + 2) continue // Data, Checksum, ETX

                            // Data
                            //mmCurIdx += dataLength
                            mmCurIdx += dataLength + 2  // Error 시 mmCurIdx 까지 버림
                            for (i in 0 until dataLength) {
                                mmDataList.add(mmRawByteList[IDX_DATA_START + i])
                            }

                            if(dataLength > 0) {
                                // Checksum(Valid Check)
                                //mmCurIdx++
                                checksum = mmRawByteList[IDX_DATA_START + dataLength]

                                // Checksum Error 확인
                                if (!Global.validChecksum(mmDataList, checksum)) {
                                    Log.d("ME", "Checksum Error !")
                                    if (clearRawByteList()) continue    // Error 처리
                                }
                            }

                            // ETX
                            //mmCurIdx++
                            if (mmRawByteList[IDX_DATA_START + dataLength + 1] != ETX) {
                                Log.d("ME", "ETX Error !")
                                if (clearRawByteList()) continue    // Error 처리
                            }

                            val pk = Packet(category, kind, dataLength, mmDataList)

                            // Packet 별 Queue 에 Packet 저장(Raw Byte List Clear)
                            when(category) {
                                PacketCategory.Rom -> Global.romQueue.add(pk)
                                PacketCategory.Monitoring -> Global.monQueue.add(pk)
                                PacketCategory.Hardware -> Global.hwQueue.add(pk)
                                PacketCategory.Register -> Global.regQueue.add(pk)
                                PacketCategory.Test -> Global.testQueue.add(pk)
                            }

                            // 발췌 Packet Log 저장
                            for (i in 0 until mmCurIdx) {
                                mmLogStr.append(String.format("%02X", mmRawByteList[i]))
                                if (i in 1..5) mmLogStr.append("(${mmRawByteList[i].toChar()})")
                                if (i < mmCurIdx - 1) mmLogStr.append(" ")
                            }
                            Log.d("ME", "[RX] $mmLogStr")
                            clearRawByteList()
                        }
                        //------------------------------------------------------------------------------//
                    }

            } catch (e: java.io.IOException) {
                Log.d("MEX", e.message.toString())
                e.printStackTrace()
                break
                //continue
            }
        }
        Log.d("ME", "Get packet thread finished. ID : ${this.id}")
    }

    private fun clearRawByteList() : Boolean {
        for (i in 0 until mmCurIdx) {
            //mmRawByteList.removeAt(i)
            mmRawByteList.removeAt(0)
        }
        mmCurIdx = 0
        mmDataList.clear()
        mmExtractMode = ExtractMode.Header

        mmLogStr.clear()

        return true
    }
}