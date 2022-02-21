package com.example.navdrawer.ui.monitoring

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.R
import com.example.navdrawer.packet.Packet
import com.example.navdrawer.packet.RPacket
import com.example.navdrawer.register.SingleRegister
import kotlin.experimental.and

class RegisterActivity : AppCompatActivity() {

    private lateinit var etSingleAddr:EditText
    private lateinit var etSingleVal:EditText
    private lateinit var btnReadSingle:Button
    private lateinit var btnWriteSingle:Button
    private lateinit var pbRegister:ProgressBar
    private lateinit var btnReadAllReg:Button
    private lateinit var btnWriteAllReg:Button
    private lateinit var btnClose:Button
    private lateinit var gridAllRegisters:GridView
    lateinit var tvStatus:TextView

    private var regThreadOn: Boolean = false
    private lateinit var regThread: RegisterThread

    private val dataListRegisters = ArrayList<HashMap<String, Any>>()   // -> Register Class 에 사용

    var rwIndex:Int=0
    var rwAll:Boolean=false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        getControls()
        setListeners()

        displayAllRegisters()

        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onCreate")
    }

    private fun displayAllRegisters() {
        dataListRegisters.clear()

        for (r in Global.regCon.registers) {
        //for (r in Global.regCon.registers2) {
            val map = HashMap<String, Any>()
            map["addr"] = String.format("%02X",r.addr.toByte())
            map["value"] = String.format("%02X", r.value.toByte())
            dataListRegisters.add(map)
        }

        val keys = arrayOf("addr", "value")
        val ids = intArrayOf(R.id.tvRowSingleAddr, R.id.tvRowSingleVal)

        val adapter = SimpleAdapter(this, dataListRegisters, R.layout.row_register, keys, ids)
        gridAllRegisters.adapter = adapter
    }

    private fun getControls() {
        etSingleAddr = findViewById(R.id.etSingleAddr)
        etSingleVal = findViewById(R.id.etSingleVal)

        btnReadSingle = findViewById(R.id.btnReadSingle)
        btnWriteSingle = findViewById(R.id.btnWriteSingle)
        pbRegister = findViewById(R.id.pbRegister)
        pbRegister.progress=0
        btnReadAllReg = findViewById(R.id.btnReadAllReg)
        btnWriteAllReg = findViewById(R.id.btnWriteAllReg)
        btnClose = findViewById(R.id.btnClose)

        gridAllRegisters = findViewById(R.id.gridAllRegisters)

        tvStatus = findViewById(R.id.tvStatusReg)
    }

    private fun setListeners() {
        btnReadSingle.setOnClickListener(listenerReadSingle)
        btnWriteSingle.setOnClickListener(listenerWriteSingle)
        btnReadAllReg.setOnClickListener(listenerReadAll)
        //btnWriteAllReg.setOnClickListener(listenerWriteSingle)
        btnClose.setOnClickListener(listenerClose)

        gridAllRegisters.setOnItemClickListener(listenerGridClick)
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

        var dataValid = true

        val ba = ByteArray(2)

        // Address
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
        } finally {
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
            dataValid = false
        } finally {
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

    private val listenerReadAll = View.OnClickListener {
        try {
            rwAll = true
            rwIndex = 0;

            Packet.send(Global.outStream,
                PacketKind.RegSingleRead,
                Global.regCon.registers[rwIndex].addr.toByte()) // Send packet
            pbRegister.progress = 0
        } catch (ex: Exception) {
            Log.d("[ADS] ", ex.message!!)
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()

        } finally {
//            etSingleAddr.setText(String.format("%02X", addr))
//
//            imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)
//            imm.hideSoftInputFromWindow(etSingleVal.windowToken, 0)
//            etSingleAddr.clearFocus()
//            etSingleVal.clearFocus()
//
//            btnReadSingle.requestFocus()
        }
    }

    private val listenerWriteAll = View.OnClickListener {
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

    private val listenerGridClick = object :AdapterView.OnItemClickListener {
        override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            when (p0?.id) {
                R.id.gridAllRegisters -> {
                    val addr = Global.regCon.registers[p2].addr
                    val value = Global.regCon.registers[p2].value
                    etSingleAddr.setText(String.format("%02X",addr.toByte()))
                    etSingleVal.setText(String.format("%02X",value.toByte()))
                }
            }
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
//        etSingleAddr.isEnabled = flag
//        etSingleVal.isEnabled = flag
//        btnReadSingle.isEnabled = flag
//        btnWriteSingle.isEnabled = flag
    }

    private fun displaySingleRegVal(addr:UByte, value:UByte) {
        etSingleAddr.setText(String.format("%02X", addr.toByte()))
        etSingleVal.setText(String.format("%02X", value.toByte()))
    }

    //---------------------------------------------------------------------------------------//
    // Register Packet 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class RegisterThread : Thread() {
        override fun run() {
            var qEmpty: Boolean
            var packet: RPacket

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
                                val regSize = Global.regCon.registers.size
                                val uAddr = packet.dataList[0].toUByte()
                                val uVal = packet.dataList[1].toUByte()
                                Global.regCon.setRegister(uAddr, uVal)

                                if (rwAll) {
                                    //if (++rwIndex == Global.regCon.registers.size) {
                                    if (++rwIndex == regSize) {
                                        rwAll = false
                                        runOnUiThread {
                                            displayAllRegisters()
                                        }
                                    } else {
                                        Packet.send(Global.outStream,
                                            PacketKind.RegSingleRead,
                                            Global.regCon.registers[rwIndex].addr.toByte()) // Send packet
                                    }
                                    runOnUiThread {
                                        pbRegister.progress =
                                            ((rwIndex.toDouble() / regSize.toDouble()) * 100.0).toInt()
                                    }
                                }
                                //else {
                                    runOnUiThread {
                                        displaySingleRegVal(uAddr, uVal)
                                        //displayAllRegisters()
                                    }
                                //}
                            }
                            else -> {}    // Do nothing
                        }
                    } catch (ex: NoSuchElementException) {
                        Log.d("[ADS/ERR] ", ex.toString())
                        continue
                    }
                } else {
                    Thread.sleep(10)
                }
                //------------------------------------------------------------------------------//
            }
            Log.d("[ADS] ", "Register thread finished. ID : ${this.id}")
        }
    }

}