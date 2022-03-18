package com.adsemicon.navdrawer.thread

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.adsemicon.navdrawer.Global
import com.adsemicon.navdrawer.PacketCategory
import com.adsemicon.navdrawer.PacketKind
import com.adsemicon.navdrawer.function.log.PacketType
import com.adsemicon.navdrawer.packet.Packet
import com.adsemicon.navdrawer.packet.RPacket
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


enum class ExtractMode{ Front, Rear }    // Front : STX~LEN, Rear : Data~ETX

class GetPacketThread(context: Context):Thread() {

    val SZ_UNTIL_LEN = 6
    val IDX_DATA_START = 6

    var mmCurIdx : Int = 0
    var mmRawByteList = mutableListOf<Byte>()
    var mmExtractMode  = ExtractMode.Front
    var mmLogStr = StringBuilder()

    var mmContext = context

    override fun run() {
        super.run()

        var qEmpty :Boolean
        var rawByteArray: ByteArray
        var category : com.adsemicon.navdrawer.PacketCategory? = null
        var kind : com.adsemicon.navdrawer.PacketKind? = null
        var dataLength : Int = 0
        var dataContents : ByteArray
        var checksum : Byte

        Log.d("[ADS] ", "Get packet thread started. ID : ${this.id}")

        while (com.adsemicon.navdrawer.Global.rxPacketThreadOn) {
            try {
                //------------------------------------------------------------------------------//
                // rawByteQueue 데이터 -> byteList 로 이동
                //------------------------------------------------------------------------------//
                //synchronized(this) { qEmpty = Global.rawRxBytesQueue.isEmpty() }
                synchronized(com.adsemicon.navdrawer.Global.rawRxBytesQueue) { qEmpty = com.adsemicon.navdrawer.Global.rawRxBytesQueue.isEmpty() }

                if (!qEmpty) {
                    try {
                        synchronized(com.adsemicon.navdrawer.Global.rawRxBytesQueue) { rawByteArray = com.adsemicon.navdrawer.Global.rawRxBytesQueue.remove() }

                        var len = rawByteArray.count()
                        for (i in 0 until len) {
                            mmRawByteList.add(rawByteArray[i])
                        }
                        //logRawByteArray(rawByteArray)
                    } catch (ex: NoSuchElementException) {
                        com.adsemicon.navdrawer.Global.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", com.adsemicon.navdrawer.Global.rawRxBytesQueue.count().toString())
                        com.adsemicon.navdrawer.Global.rawRxBytesQueue.clear()
                        continue
                    } catch (ex: Exception) {
                        com.adsemicon.navdrawer.Global.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.message.toString())
                        Log.d("[ADS/ERR] ", ex.printStackTrace().toString())
                        break
                    }
                }
                //------------------------------------------------------------------------------//

                //------------------------------------------------------------------------------//
                // byteList 데이터 -> Packet 추출
                //------------------------------------------------------------------------------//
                if (mmRawByteList.count() > 0) {
                    if (mmExtractMode == ExtractMode.Front) {
                        if (mmRawByteList.count() < SZ_UNTIL_LEN) continue

                        // STX
                        mmCurIdx = SZ_UNTIL_LEN  // Error 시 mmCurIdx 까지 버림
                        if (mmRawByteList[0] != Packet.STX) {
                            Log.d("[ADS] ", "STX Error ! : ${mmRawByteList[0]}")
                            if (clearRawByteList()) continue    // Error 처리
                        }

                        // Check Header : if in range of A to Z
                        if ((mmRawByteList[1] in 0x41..0x5a) && (mmRawByteList[2] in 0x41..0x5a)) {
                            val str1 = mmRawByteList[1].toChar().toString()
                            val str2 = mmRawByteList[2].toChar().toString()

                            category = Packet.packetCategory[str1]
                            kind = Packet.packetKind["$str1$str2"]

                            if (category == null || kind == null){
                                Log.d("[ADS] ", "Packet header error : $str1$str2")
                                if (clearRawByteList()) continue    // Error 처리
                            }
                        } else {
                            Log.d("[ADS] ", "Packet Header Error!!")
                            if (clearRawByteList()) continue    // Error 처리
                        }

                        // Check Length : if in range of '0' to '9'
                        if ((mmRawByteList[3] in 0x30..0x39) && (mmRawByteList[4] in 0x30..0x39)
                            && (mmRawByteList[5] in 0x30..0x39)
                        ) {
                            dataLength =
                                (mmRawByteList[3].toInt() - 0x30) * 100 + (mmRawByteList[4].toInt() - 0x30) * 10 + (mmRawByteList[5].toInt() - 0x30)
                            mmExtractMode = ExtractMode.Rear
                        } else {
                            Log.d("[ADS] ", "Packet Length Error!!")
                            if (clearRawByteList()) continue    // Error 처리
                        }
                    } else if (mmExtractMode == ExtractMode.Rear) {

                        if (mmRawByteList.count() < SZ_UNTIL_LEN + dataLength + 2) continue // Data, Checksum, ETX

                        // Data
                        mmCurIdx += dataLength + 2  // Error 시 mmCurIdx 까지 버림
                        dataContents = ByteArray(dataLength)
                        for (i in 0 until dataLength) {
                            dataContents[i] = mmRawByteList[IDX_DATA_START + i]
                        }

                        if (dataLength > 0) {
                            checksum = mmRawByteList[IDX_DATA_START + dataLength]

                            // Checksum Error 확인
                            val calcChecksum = Packet.makeChecksum(dataContents)
                            if (calcChecksum != checksum) {
                                Log.d("[ADS] ",
                                    "Checksum Error ! / Rx Val : ${checksum},Calc Val : $calcChecksum")

                                if (clearRawByteList()) continue    // Error 처리
                            }
                        }

                        // ETX
                        if (mmRawByteList[IDX_DATA_START + dataLength + 1] != Packet.ETX) {
                            Log.d("[ADS] ", "ETX Error !")
                            if (clearRawByteList()) continue    // Error 처리
                        }

                        val pk = RPacket(category, kind, dataLength, dataContents)

                        // Packet 별 Queue 에 Packet 저장(Raw Byte List Clear)
                        when (category) {
                            com.adsemicon.navdrawer.PacketCategory.Rom -> com.adsemicon.navdrawer.Global.romQueue.add(pk)

                            //-------------------------------------------------------------------//
                            // Monitoring 데이터는 Queue 에 저장하지 않음
                            // Monitoring Class Data 갱신 처리
                            // 이후 Data Display Thread 에서 Monitoring Class Data Display
                            //-------------------------------------------------------------------//
                            com.adsemicon.navdrawer.PacketCategory.Monitoring -> {
                                if (kind == com.adsemicon.navdrawer.PacketKind.MonSet && com.adsemicon.navdrawer.Global.waitForStopMon) {
                                    com.adsemicon.navdrawer.Global.waitForStopMon = false
                                } else {
                                    com.adsemicon.navdrawer.Global.monitoring.updateMonData(kind, dataContents)
                                }
                            }
                            //-------------------------------------------------------------------//

                            com.adsemicon.navdrawer.PacketCategory.Hardware -> {
                                if (kind == com.adsemicon.navdrawer.PacketKind.HwRead) {
                                    synchronized(com.adsemicon.navdrawer.Global.hwStat) {
                                        com.adsemicon.navdrawer.Global.hwStat = dataContents[0]
                                    }
                                }
                                synchronized(com.adsemicon.navdrawer.Global.hwQueue) {
                                    com.adsemicon.navdrawer.Global.hwQueue.add(pk)
                                }
                            }
                            com.adsemicon.navdrawer.PacketCategory.Register -> {
                                synchronized(com.adsemicon.navdrawer.Global.regQueue) {
                                    com.adsemicon.navdrawer.Global.regQueue.add(pk)
                                }

                                if (kind == com.adsemicon.navdrawer.PacketKind.RegSwReset && com.adsemicon.navdrawer.Global.waitForSwReset) {
                                    com.adsemicon.navdrawer.Global.waitForSwReset = false
                                    Log.d("[ADS] ", "RI response")
                                }
                            }
                            com.adsemicon.navdrawer.PacketCategory.Test -> {
                                synchronized(com.adsemicon.navdrawer.Global.testQueue) {
                                    com.adsemicon.navdrawer.Global.testQueue.add(pk)
                                }
                            }
                        }
                        prepareLog()

                        // 임시, 향 후 Library 적용 예정
                        com.adsemicon.navdrawer.Global.packetLog.printPacket(PacketType.RX, mmLogStr.toString())

                        clearRawByteList()  // Packet 처리 완료된 Raw Data 제거
                    }
                    //------------------------------------------------------------------------------//
                }

            } catch (ex: java.io.IOException) {
                com.adsemicon.navdrawer.Global.errLog.printError(ex)
                Log.d("[ADS/ERR] ", ex.message.toString())
                ex.printStackTrace()
                break
                //continue
            }
        }
        Log.d("[ADS] ", "Get packet thread finished. ID : ${this.id}")
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
        Log.d("[ADS] ", "[RX RAW ARRAY] $str")
        //-----------------------------------------------------------------------//
    }

    private fun prepareLog() {
//        //------------------------------------------------------------------------------//
//        // 발췌 Packet Logcat 저장(HEX)
//        //------------------------------------------------------------------------------//
//        for (i in 0 until mmCurIdx) {
//            mmLogStr.append(String.format("%02X", mmRawByteList[i]))
//            if (i < mmCurIdx - 1) mmLogStr.append(" ")
//        }
//        Log.d("[ADS] ", "[RX PK HEX] $mmLogStr")
//        //------------------------------------------------------------------------------//

        //------------------------------------------------------------------------------//
        // 발췌 Packet Logcat 저장(Elements, 필요 시 Character 로 표시)
        //------------------------------------------------------------------------------//
        mmLogStr.clear()
        for (i in 0 until mmCurIdx) {
            if (i == 0) {
                // STX
                mmLogStr.append("STX ")
            } else if (i in 1 until IDX_DATA_START) {
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
        //Log.d("[ADS] ", "[RX PK ELE] $mmLogStr")
        Log.d("[ADS] ", "[PK RX] $mmLogStr")
        //------------------------------------------------------------------------------//
    }

//    private fun logToFile(str:String) {
//        //------------------------------------------------------------------------------//
//        // 발췌 Packet Log 파일 저장
//        // (테스트 완료, 저장된 파일 확인함 > Android Studio 의 Device File Explorer 사용)
//        //------------------------------------------------------------------------------//
//        // 시스템의 임시 디렉토리명을 획득, 운영체제마다 다름
////        var pathname = System.getProperty("java.io.tmpdir")
////        var someFile = File("$pathname/rx_packet.txt")
////
//        // 문자열을 앞서 지정한 경로에 파일로 저장, 저장시 캐릭터셋은 기본값인 UTF-8으로 저장
//        // 이미 파일이 존재할 경우 덮어쓰기로 저장
//        // 파일이 아닌 디렉토리이거나 기타의 이유로 저장이 불가능할 경우 FileNotFoundException 발생
//        try {
//            //someFile.appendText("가나다라마바사")
//            fileName.appendText("$str\n")
//            Log.d("[ADS] ", "File saved at : $fileName")
//        } catch (e: FileNotFoundException) {
//            Log.d("[ADS] ", "FileNotFound: $fileName")
//        }
//        //------------------------------------------------------------------------------//
//
////        //------------------------------------------------------------------------------//
////        // Lib test (향 후 적용 예정)
////        //------------------------------------------------------------------------------//
////        ADLog.print(LogKind.Log, str)
////        Log.d("[ADS] ", "File saved at : $fileName")
////        //------------------------------------------------------------------------------//
//    }

    // writeTextData() method save the data into the file in byte format
    // It also toast a message "Done/filepath_where_the_file_is_saved"
    private fun writeTextData(file: File, data: String) {
        var fileOutputStream: FileOutputStream? = null
        try {
            fileOutputStream = FileOutputStream(file)
            fileOutputStream.write(data.toByteArray())
            Toast.makeText(mmContext, "Done" + file.absolutePath, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun clearRawByteList() : Boolean {
        for (i in 0 until mmCurIdx) {
            mmRawByteList.removeAt(0)
        }
        mmCurIdx = 0
        mmExtractMode = ExtractMode.Front

        mmLogStr.clear()

        return true
    }
}