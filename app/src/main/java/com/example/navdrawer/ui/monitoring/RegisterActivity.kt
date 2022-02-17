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
import android.widget.Toast
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
            addr = addrStr.toInt(16).toByte()

            Packet.send(Global.outStream, PacketKind.RegSingleRead, addr) // Send packet
        } catch (ex: Exception) {
            val errStr = "Please enter correct address(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_SHORT).show()
            addr = 0
        } finally {
            etSingleAddr.setText(String.format("%02X", addr))

            imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)
            imm.hideSoftInputFromWindow(etSingleVal.windowToken, 0)
            etSingleAddr.clearFocus()
            etSingleVal.clearFocus()

            btnReadSingle.requestFocus()
        }
    }

    private val listenerWriteSingle = View.OnClickListener {
        val imm: InputMethodManager

        var addrStr: String = ""
        var valStr: String = ""
        var addr: Byte = 0
        var value: Byte = 0

//        try {
//            // Addr
//            addrStr = etSingleAddr.text.toString()
//            addr = addrStr.toInt(16).toByte()
//
//            // Value
//            valStr = etSingleVal.text.toString()
//            value = valStr.toInt(16).toByte()
//
//            val ba = ByteArray(2)
//            ba[0] = addr
//            ba[1] = value
//            Packet.send(Global.outStream, PacketKind.RegSingleWrite, ba) // Send packet
//        } catch (ex: Exception) {
//            val errStr = "Please enter correct address or value(HEX Format)."
//            Log.d("[ADS] ", errStr)
//            Toast.makeText(this, errStr, Toast.LENGTH_SHORT).show()
//            addr = 0
//            value = 0
//        } finally {
//            etSingleAddr.setText(String.format("%02X", addr))
//            etSingleVal.setText(String.format("%02X", value))
//
//            imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)
//            imm.hideSoftInputFromWindow(etSingleVal.windowToken, 0)
//            etSingleAddr.clearFocus()
//            etSingleVal.clearFocus()
//
//            btnWriteSingle.requestFocus()
//        }

        var dataValid=true

        val ba = ByteArray(2)

        // Addr
        try {
            addrStr = etSingleAddr.text.toString()
            addr = addrStr.toInt(16).toByte()
            ba[0] = addr
        } catch (ex: Exception) {
            val errStr = "Please enter correct address(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_SHORT).show()
            addr = 0
            dataValid = false
        }finally {
            etSingleAddr.setText(String.format("%02X", addr))
        }

        // Value
        try {
            valStr = etSingleVal.text.toString()
            value = valStr.toInt(16).toByte()
            ba[1] = value
        } catch (ex: Exception) {
            val errStr = "Please enter correct value(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_SHORT).show()
            value = 0
            dataValid=false
        }
        finally {
            etSingleVal.setText(String.format("%02X", value))
        }

        if (dataValid) {
            Packet.send(Global.outStream, PacketKind.RegSingleWrite, ba) // Send packet
            imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)
            imm.hideSoftInputFromWindow(etSingleVal.windowToken, 0)
            etSingleAddr.clearFocus()
            etSingleVal.clearFocus()

            btnWriteSingle.requestFocus()
        }
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
                            else->{}    // Do nothing
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