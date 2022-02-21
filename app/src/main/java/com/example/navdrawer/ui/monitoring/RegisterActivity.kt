package com.example.navdrawer.ui.monitoring

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

    var singleRegAddrValid:Boolean=false
    var singleRegValueValid:Boolean=false

    var uSingleAddr:UByte=0u
    var uSingleVal:UByte=0u

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

        etSingleAddr.addTextChangedListener(listenerEtAddr)
        etSingleVal.addTextChangedListener(listenerEtVal)

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

    val listenerEtAddr = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            // Address
            var addr: Byte = 0
            try {
                var addrStr = etSingleAddr.text.toString()
                addr = addrStr.toInt(16).toByte()
                singleRegAddrValid = true
            } catch (ex: Exception) {
                val errStr = "Please enter correct address(HEX Format)."
                Log.d("[ADS] ", errStr)
                //Toast.makeText(this, errStr, Toast.LENGTH_SHORT).show()
                addr = 0
                singleRegAddrValid = false
            } finally {
                uSingleAddr = addr.toUByte()
                //etSingleAddr.setText(String.format("%02X", addr))
                Log.d("[ADS] ", "Single addr has set :${String.format("%02X", addr)}")
                Log.d("[ADS] ", "Single addr valid :${singleRegAddrValid}")
            }
        }
    }

    val listenerEtVal = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            // Value
            var value: Byte=0

            try {
                var valStr = etSingleVal.text.toString()
                value = valStr.toInt(16).toByte()
                uSingleVal = value.toUByte()
                singleRegValueValid = true

            } catch (ex: Exception) {
                //val errStr = "Please enter correct value(HEX Format)."
                //Log.d("[ADS] ", errStr)
                //Toast.makeText(this, errStr, Toast.LENGTH_SHORT).show()
                value = 0
                singleRegValueValid = false
            } finally {
                uSingleVal = value.toUByte()
                if (singleRegAddrValid){
                    if (Global.regCon.hasRegister(uSingleAddr)){
                        Global.regCon.setRegister(uSingleAddr, uSingleVal)
                        displayAllRegisters()
                    }
                }
                //etSingleVal.setText(String.format("%02X", value))
                Log.d("[ADS] ", "Single val has set :${String.format("%02X", value)}")
                Log.d("[ADS] ", "Single val valid :${singleRegValueValid}")
            }
        }
    }

    private val listenerReadSingle = View.OnClickListener {
        var errStr = ""

        // Send Packet if all valid
        if (!singleRegAddrValid) {
            errStr = "Please enter correct address(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show()
            etSingleVal.requestFocus()
            etSingleAddr.requestFocus()
        }
        else {
            Packet.send(Global.outStream,
                PacketKind.RegSingleRead,
                uSingleAddr.toByte()) // Send packet
            etSingleAddr.clearFocus()
            btnReadSingle.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)

            //setControlEnabled(false)
        }

    }

    private val listenerWriteSingle = View.OnClickListener {
        var errStr = ""
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val ba = ByteArray(2)

        // Send Packet if all valid
        if (!singleRegAddrValid) {
            errStr = "Please enter correct address(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show()

            etSingleVal.requestFocus()
            etSingleAddr.requestFocus()
        } else if (!singleRegValueValid) {
            errStr = "Please enter correct value(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show()

            etSingleAddr.requestFocus()
            etSingleVal.requestFocus()
        } else {
            ba[0] = uSingleAddr.toByte()
            ba[1] = uSingleVal.toByte()
            Packet.send(Global.outStream, PacketKind.RegSingleWrite, ba) // Send packet
            etSingleAddr.clearFocus()
            btnReadSingle.requestFocus()
            btnWriteSingle.requestFocus()
            imm.hideSoftInputFromWindow(etSingleAddr.windowToken, 0)
            imm.hideSoftInputFromWindow(etSingleVal.windowToken, 0)

            displaySingleRegVal(uSingleAddr, uSingleVal)
            //setControlEnabled(false)
        }
    }

    private val listenerReadAll = View.OnClickListener {
        try {
            rwAll = true
            rwIndex = 0;

            setControlEnabled(false)

            Packet.send(Global.outStream,
                PacketKind.RegSingleRead,
                Global.regCon.registers[rwIndex].addr.toByte()) // Send packet
            pbRegister.progress = 0
        } catch (ex: Exception) {
            Log.d("[ADS] ", ex.message!!)
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()

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
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show()
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

                    etSingleAddr.requestFocus()
                    etSingleVal.requestFocus()
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
        etSingleAddr.isEnabled = flag
        etSingleVal.isEnabled = flag
        btnReadSingle.isEnabled = flag
        btnWriteSingle.isEnabled = flag
        btnReadAllReg.isEnabled = flag
        btnWriteAllReg.isEnabled = flag
       // btnClose.isEnabled=flag
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

                                if (Global.regCon.hasRegister(uAddr)) {
                                    Global.regCon.setRegister(uAddr, uVal)
                                }

                                if (rwAll) {
                                    if (++rwIndex == regSize) {
                                        rwAll = false
                                        runOnUiThread {
                                            displayAllRegisters()
                                            pbRegister.progress =0
                                            setControlEnabled(true)
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
                                } else {
                                    runOnUiThread {
                                        displaySingleRegVal(uAddr, uVal)
                                        if (Global.regCon.hasRegister(uAddr)) {
                                            displayAllRegisters()
                                        }
                                        //setControlEnabled(true)
                                    }
                                }
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