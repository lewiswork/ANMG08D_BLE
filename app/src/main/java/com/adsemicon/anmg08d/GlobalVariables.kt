package com.adsemicon.anmg08d

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.adsemicon.anmg08d.function.log.MonitoringLog
import com.adsemicon.anmg08d.function.log.SystemLog
import com.adsemicon.anmg08d.monitor.Monitoring
import com.adsemicon.anmg08d.packet.RPacket
import com.adsemicon.anmg08d.register.RegisterController
import com.adsemicon.anmg08d.thread.GetPacketThread
import com.adsemicon.anmg08d.thread.RxThread
import java.io.InputStream
import java.io.OutputStream
import java.util.*

enum class PacketCategory{ Rom, Monitoring, Register, Hardware, Test }
enum class PacketKind{
    HwRead, HwWrite,
    MonSet, MonTouch, MonPercent,
    RegSingleRead, RegSingleWrite, RegSwReset
}

class GlobalVariables {
    // companion object : 타 언어의 Static Class 와 같이 사용하기 위한 목적
    companion object {

        // Bluetooth 관련
        lateinit var btAdapter: BluetoothAdapter       // Late Initialize : 변수 초기화를 나중으로 미룸
        lateinit var selectedDevice: BluetoothDevice
        var rxRawBytesQueue: Queue<ByteArray> = LinkedList()
        var isBtConnected: Boolean = false           // BT 연결 상태

        var inStream: InputStream? = null
        var outStream: OutputStream? = null
        var socket: BluetoothSocket? = null

        var rxThreadOn = false
        var rxPacketThreadOn = false

        var rxThread: RxThread? = null
        var getPacketThread: GetPacketThread? = null

        var romQueue: Queue<RPacket> = LinkedList()
        var monQueue: Queue<RPacket> = LinkedList()
        var hwQueue: Queue<RPacket> = LinkedList()
        var regQueue: Queue<RPacket> = LinkedList()
        var testQueue: Queue<RPacket> = LinkedList()

        var hwStat: Byte = 0x00
        var hwStatPrev: Byte = 0x00

        var waitForStopMon: Boolean = false // Jig 로부터 응답이 없을 시 Packet 재전송 목적
        var waitForSwReset: Boolean = false // Jig 로부터 응답이 없을 시 Packet 재전송 목적

        var regCon: RegisterController = RegisterController()

//        var instance: MainActivity? =MainActivity()
//        fun applicationContext() : Context {
//            return instance!!.applicationContext
//        }
//        var path = instance!!.applicationContext

        //lateinit var basePath: File

        // System Logs
//        var packetLog = SystemLog("system", "packet.txt")
//        //var packetLog = SystemLog("system", "packet.txt")
//        var errLog = SystemLog("system", "error.txt", "ERR", true)
//
//        // Monitoring Logs
//        var touchLog = MonitoringLog("monitoring", "touch.txt")
//        var percentLog = MonitoringLog("monitoring", "percent.txt")
//
//        val monitoring = Monitoring()   // Touch/Percent Log 객체 생성 이후에 생성

        //------------------------------------------------------------------//
        // External Storage 사용을 위해
        // MainActivity 의 applicationContext 필요 -> late init
        //------------------------------------------------------------------//
        lateinit var contextMain: Context

        // System Logs
        lateinit var packetLog: SystemLog
        lateinit var errLog: SystemLog

        // Monitoring Logs
        lateinit var touchLog: MonitoringLog
        lateinit var percentLog: MonitoringLog

        // Monitoring
        lateinit var monitoring: Monitoring   // Touch/Percent Log 객체 생성 이후에 생성
        //------------------------------------------------------------------//

        fun initLogAndMonitoring(context: Context) {
            // Context
            contextMain = context

            // System Logs
            packetLog = SystemLog(context, "system", "packet.txt")
            errLog = SystemLog(context, "system", "error.txt", "ERR", true)

            // Monitoring Logs
            touchLog = MonitoringLog(context, "monitoring", "touch.txt")
            percentLog = MonitoringLog(context, "monitoring", "percent.txt")

            // Monitoring
            monitoring = Monitoring()   // Touch/Percent Log 객체 생성 이후에 생성
        }
    }
}