package com.example.navdrawer.thread

import android.util.Log
import com.example.navdrawer.Global
import com.example.navdrawer.PacketCategory
import com.example.navdrawer.PacketKind
import com.example.navdrawer.data.Packet
import java.io.File
import java.io.FileNotFoundException
import java.lang.StringBuilder
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
    //var mmDataList : ArrayList<Byte> = ArrayList()
    //var mmDataList : ByteArray
    var mmExtractMode  = ExtractMode.Header
    var mmLogStr = StringBuilder()

    override fun run() {
        super.run()

        var qEmpty = true

        var dataLength : Int = 0
        var checksum : Byte = 0x00
        var category : PacketCategory? = null
        var kind : PacketKind? = null
        var rawByteArray: ByteArray

        var mmDataList : ByteArray

        Log.d("ME", "Get packet thread started. ID : ${this.id}")

        while (Global.rxPacketThreadOn) {
            try {
                //------------------------------------------------------------------------------//
                // rawByteQueue 데이터 -> byteList 로 이동
                //------------------------------------------------------------------------------//
                synchronized(this) { qEmpty = Global.rawRxBytesQueue.isEmpty() }
                if (!qEmpty) {
                    try {
                        synchronized(this) { rawByteArray = Global.rawRxBytesQueue.remove() }

                        var len = rawByteArray.count()
                        for (i in 0 until len) {
                            mmRawByteList.add(rawByteArray[i])
                        }
                        //logRawByteArray(rawByteArray)
                    } catch (ex: NoSuchElementException) {
                        Log.d("MEX", Global.rawRxBytesQueue.count().toString())
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
                        mmCurIdx = SZ_UNTIL_LEN  // Error 시 mmCurIdx 까지 버림
                        if (mmRawByteList[0] != STX) {
                            Log.d("ME", "STX Error ! : ${mmRawByteList[0]}")
                            if (clearRawByteList()) continue    // Error 처리
                        }

                        // Check Header : if in range of A to Z
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
                        mmCurIdx += dataLength + 2  // Error 시 mmCurIdx 까지 버림
                        mmDataList = ByteArray(dataLength)
                        for (i in 0 until dataLength) {
                            //mmDataList.add(mmRawByteList[IDX_DATA_START + i])
                            mmDataList[i] = mmRawByteList[IDX_DATA_START + i]
                        }

                        if (dataLength > 0) {
                            checksum = mmRawByteList[IDX_DATA_START + dataLength]

                            // Checksum Error 확인
                            if (!Global.verifyChecksum(mmDataList, checksum)) {
                                Log.d("ME", "Checksum Error !")
                                if (clearRawByteList()) continue    // Error 처리
                            }
                        }

                        // ETX
                        if (mmRawByteList[IDX_DATA_START + dataLength + 1] != ETX) {
                            Log.d("ME", "ETX Error !")
                            if (clearRawByteList()) continue    // Error 처리
                        }

                        val pk = Packet(category, kind, dataLength, mmDataList)

                        // Packet 별 Queue 에 Packet 저장(Raw Byte List Clear)
                        when (category) {
                            PacketCategory.Rom -> Global.romQueue.add(pk)

                            // Monitoring 데이터는 Queue 에 저장하지 않음
                            // Monitoring Class Data 갱신 처리
                            // 이후 Data Display Thread 에서 Monitoring Class Data Display
                            //PacketCategory.Monitoring -> Global.monQueue.add(pk)

                            PacketCategory.Hardware -> Global.hwQueue.add(pk)
                            PacketCategory.Register -> Global.regQueue.add(pk)
                            PacketCategory.Test -> Global.testQueue.add(pk)
                        }

                        prepareLog()
                        logToFile(mmLogStr.toString())

                        clearRawByteList()  // Packet 저장 완료된 Raw Data 제거
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

    private fun logRawByteArray(rawByteArray: ByteArray) {
        //-----------------------------------------------------------------------//
        //  Logcat (DEBUG)
        //-----------------------------------------------------------------------//
        var str = StringBuilder()
        for (element in rawByteArray) {
            str.append(String.format("%02X", element))
            str.append(" ")
        }
        Log.d("ME", "[RX RAW ARRAY] $str")
        //-----------------------------------------------------------------------//
    }

    private fun prepareLog() {
        //------------------------------------------------------------------------------//
        // 발췌 Packet Logcat 저장(HEX)
        //------------------------------------------------------------------------------//
        for (i in 0 until mmCurIdx) {
            mmLogStr.append(String.format("%02X", mmRawByteList[i]))
            if (i < mmCurIdx - 1) mmLogStr.append(" ")
        }
        Log.d("ME", "[RX PK HEX] $mmLogStr")
        //------------------------------------------------------------------------------//

        //------------------------------------------------------------------------------//
        // 발췌 Packet Logcat 저장(Elements, 필요 시 Character 로 표시)
        //------------------------------------------------------------------------------//
        mmLogStr.clear()
        for (i in 0 until mmCurIdx) {
            if (i == 0) {
                // STX
                mmLogStr.append("STX ")
            } else if (i > 0 && i < IDX_DATA_START) {
                // Header, Length
                if (i == 3) mmLogStr.append(" ")
                mmLogStr.append(mmRawByteList[i].toChar())
            } else if (i >= IDX_DATA_START && i < mmCurIdx - 1) {
                mmLogStr.append(" ")
                // Data, Checksum
                mmLogStr.append(String.format("%02X", mmRawByteList[i]))
            } else if (i == mmCurIdx - 1) {
                // ETX
                mmLogStr.append(" ETX")
            }
        }
        Log.d("ME", "[RX PK ELE] $mmLogStr")
        //------------------------------------------------------------------------------//
    }

    private fun logToFile(str:String) {
        //------------------------------------------------------------------------------//
        // 발췌 Packet Log 파일 저장
        // (테스트 완료, 저장된 파일 확인함 > Android Studio 의 Device File Explorer 사용)
        //------------------------------------------------------------------------------//
        // 시스템의 임시 디렉토리명을 획득, 운영체제마다 다름
        var pathname = System.getProperty("java.io.tmpdir")
        var someFile = File(pathname + "/some-file.txt")

        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
        // 이미 파일이 존재할 경우 덮어쓰기로 저장
        // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
        try {
            someFile.appendText("가나다라마바사")
            someFile.appendText(str.toString())
            //Log.d("ME", "File saved at : $someFile")
        } catch (e: FileNotFoundException) {
            Log.d("ME", "FileNotFound: $someFile")
        }
        //------------------------------------------------------------------------------//
    }

    private fun clearRawByteList() : Boolean {
        for (i in 0 until mmCurIdx) {
            //mmRawByteList.removeAt(i)
            mmRawByteList.removeAt(0)
        }
        mmCurIdx = 0
        //mmDataList.clear()
        mmExtractMode = ExtractMode.Header

        mmLogStr.clear()

        return true
    }
}