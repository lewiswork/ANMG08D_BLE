package com.example.navdrawer.ui.monitoring

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.R
import com.example.navdrawer.packet.Packet
import com.example.navdrawer.packet.RPacket
import java.lang.Exception
import java.util.NoSuchElementException
import kotlin.experimental.and

class RegisterActivity : AppCompatActivity() {

    private lateinit var etSingleAddr:EditText
    private lateinit var etSingleVal:EditText

    private lateinit var btnReadSingle:Button
    private lateinit var btnWriteSingle:Button
    private lateinit var btnClose:Button

    lateinit var tvStatus:TextView

    private var regThreadOn: Boolean = false
    private lateinit var regThread: RegisterThread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        getControls()
        setListeners()

        //checkConnections()        // BT 연결상태 별 초기화 처리
        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onCreate")
    }

    private fun getControls() {
        etSingleAddr = findViewById(R.id.etSingleAddr)
        etSingleVal = findViewById(R.id.etSingleVal)

        btnReadSingle = findViewById(R.id.btnReadSingle)
        btnWriteSingle = findViewById(R.id.btnWriteSingle)
        btnClose = findViewById(R.id.btnClose)

        tvStatus = findViewById(R.id.tvStatusReg)
    }

    private fun setListeners() {
        btnReadSingle.setOnClickListener(listenerReadSingle)
        btnWriteSingle.setOnClickListener(listenerWriteSingle)
        btnClose.setOnClickListener(listenerClose)
    }

    override fun onResume() {
        super.onResume()

        checkConnections()        // BT 연결상태 별 초기화 처리
        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onResume")
    }

    override fun onPause() {
        super.onPause()

        regThreadOn = false
        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onDestroy")
    }

    private val listenerReadSingle = View.OnClickListener {
        val imm: InputMethodManager
        var addrStr: String = ""
        var addr: Byte = 0

        try {
            addrStr = etSingleAddr.text.toString()
            Log.d("[ADS] ", "String is : $addrStr")

            addr = addrStr.toInt(16).toByte()
            Packet.send(Global.outStream, PacketKind.RegSingleRead, addr) // Send packet
        } catch (ex: Exception) {
            Log.d("[ADS] ", "Address format Error!!")
            addr = 0
        } finally {
            etSingleAddr.setText(String.format("%02X", addr))

            imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)
            imm.hideSoftInputFromWindow(etSingleVal.windowToken, 0)
            etSingleAddr.clearFocus()
            etSingleVal.clearFocus()

            btnReadSingle.requestFocus()
        }
    }

    private val listenerWriteSingle = View.OnClickListener {

    }

    private val listenerClose = View.OnClickListener {
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun checkConnections() {
        if (Global.isBtConnected) {
            if ((Global.hwStat and 0x06) != 0x06.toByte()) {
                tvStatus.text = "Relays are off."
                setControlEnabled(false)
            } else {
                tvStatus.text = "BT connected and relays are on."

                regThreadOn = true
                regThread = RegisterThread()
                regThread.start()

                setControlEnabled(true)
            }
        } else {
            tvStatus.text = "BT disconnected."
            setControlEnabled(false)
        }
    }

    private fun setControlEnabled(flag:Boolean) {
        etSingleAddr.isEnabled = flag
        etSingleVal.isEnabled = flag
        btnReadSingle.isEnabled = flag
        btnWriteSingle.isEnabled = flag
    }

    private fun displaySingleRegVal(value:Byte) {
        etSingleVal.setText(String.format("%02X", value))
    }

    //---------------------------------------------------------------------------------------//
    // Register Packet 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class RegisterThread : Thread() {
        override fun run() {
            var qEmpty :Boolean
            var packet : RPacket

            //inner class DisplayThread : ADThread() { // AD Lib 사용 테스트 -> Compile Error, 추후 재 테스트 예정
            //override fun doWork() {
            Log.d("[ADS] ", "Register thread started. ID : ${this.id}")
            while (regThreadOn) {
                //------------------------------------------------------------------------------//
                // Packet 처리
                //------------------------------------------------------------------------------//
                synchronized(Global.regQueue) { qEmpty = Global.regQueue.isEmpty() }

                if (!qEmpty) {
                    try {
                        synchronized(Global.regQueue) { packet = Global.regQueue.remove() }

                        when (packet.kind) {
                            PacketKind.RegSingleRead -> {
                                runOnUiThread {
                                    displaySingleRegVal(packet.dataList[0])
                                }
                            }
                        }
                    } catch (ex: NoSuchElementException) {
                        Log.d("[ADS/ERR] ", ex.toString())
                        continue
                    }
                }
                //------------------------------------------------------------------------------//

                Thread.sleep(10)
            }
            Log.d("[ADS] ", "Register thread finished. ID : ${this.id}")
        }
    }

}