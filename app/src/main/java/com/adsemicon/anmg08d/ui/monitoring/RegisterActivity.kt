package com.adsemicon.anmg08d.ui.monitoring

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.adsemicon.anmg08d.GlobalVariables
import com.adsemicon.anmg08d.PacketKind
import com.adsemicon.anmg08d.R
import com.adsemicon.anmg08d.packet.Packet
import com.adsemicon.anmg08d.packet.RPacket
import kotlin.experimental.and


class RegisterActivity : AppCompatActivity() {

    private lateinit var linearRegisterActivity: LinearLayout
    private lateinit var etSingleAddr: EditText
    private lateinit var etSingleVal: EditText
    private lateinit var btnReadSingle: Button
    private lateinit var btnWriteSingle: Button
    private lateinit var pbRegister: ProgressBar
    private lateinit var btnReadAllReg: Button
    private lateinit var btnWriteAllReg: Button
    private lateinit var btnClose: Button
    private lateinit var gridAllRegisters: GridView
    lateinit var tvStatus: TextView

    private var regThreadOn: Boolean = false
    private lateinit var regThread: RegisterThread

    // Gridview 에 Register Display 시 사용
    private val dataListRegisters = ArrayList<HashMap<String, Any>>()   

    var regIndex: Int = 0
    var rwAll: Boolean = false

    var singleRegAddrValid: Boolean = false
    var singleRegValueValid: Boolean = false

    var uSingleAddr: UByte = 0u
    var uSingleVal: UByte = 0u

//    var tick=false
//    var timer : Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        getControls()
        setListeners()
        displayAllRegisters()

        //getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onCreate")
    }

    override fun onResume() {
        super.onResume()

        checkConnections()        // BT 연결상태 별 초기화 처리

        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onResume")
    }

    override fun onPause() {
        super.onPause()
        //timer!!.cancel()
        regThreadOn = false
        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onPause")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("[ADS] ", "MonitoringFragment > RegisterActivity > onDestroy")
    }

    private fun displayAllRegisters() {
        dataListRegisters.clear()

        for (r in com.adsemicon.anmg08d.GlobalVariables.regCon.registers) {
            val map = HashMap<String, Any>()
            map["addr"] = String.format("%02X", r.addr.toByte())
            map["value"] = String.format("%02X", r.value.toByte())
            dataListRegisters.add(map)
        }

        val keys = arrayOf("addr", "value")
        val ids = intArrayOf(R.id.tvRowSingleAddr, R.id.tvRowSingleVal)

        val adapter = SimpleAdapter(this, dataListRegisters, R.layout.row_register, keys, ids)
        gridAllRegisters.adapter = adapter
    }

    private fun getControls() {
        linearRegisterActivity = findViewById(R.id.linearRegisterActivity)

        etSingleAddr = findViewById(R.id.etSingleAddr)
        etSingleVal = findViewById(R.id.etSingleVal)

        btnReadSingle = findViewById(R.id.btnReadSingle)
        btnWriteSingle = findViewById(R.id.btnWriteSingle)
        pbRegister = findViewById(R.id.pbRegister)
        pbRegister.progress = 0
        btnReadAllReg = findViewById(R.id.btnReadAllReg)
        btnWriteAllReg = findViewById(R.id.btnWriteAllReg)
        btnClose = findViewById(R.id.btnClose)

        gridAllRegisters = findViewById(R.id.gridAllRegisters)

        tvStatus = findViewById(R.id.tvStatusReg)
    }

    private fun setListeners() {
        linearRegisterActivity.setOnClickListener(listenerLinearRegOnClick)

        etSingleAddr.addTextChangedListener(listenerEtAddr)
        etSingleVal.addTextChangedListener(listenerEtVal)

        btnReadSingle.setOnClickListener(listenerReadSingle)
        btnWriteSingle.setOnClickListener(listenerWriteSingle)
        btnReadAllReg.setOnClickListener(listenerReadAll)
        btnWriteAllReg.setOnClickListener(listenerWriteAll)
        btnClose.setOnClickListener(listenerClose)

        gridAllRegisters.setOnItemClickListener(listenerGridClick)
    }

    private fun hideKeyboard(view:View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private val listenerLinearRegOnClick = View.OnClickListener {
        hideKeyboard(linearRegisterActivity)
    }

    private val listenerEtAddr = object : TextWatcher {
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
                val errStr = "Please enter correct address(HEX format)."
                Log.d("[ADS] ", errStr)
                addr = 0
                singleRegAddrValid = false
            } finally {
                uSingleAddr = addr.toUByte()
            }
        }
    }

    private val listenerEtVal = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            // Value
            var value: Byte = 0

            try {
                var valStr = etSingleVal.text.toString()
                value = valStr.toInt(16).toByte()
                uSingleVal = value.toUByte()
                singleRegValueValid = true

            } catch (ex: Exception) {
                value = 0
                singleRegValueValid = false
            } finally {
                uSingleVal = value.toUByte()
                if (singleRegAddrValid) {
                    if (com.adsemicon.anmg08d.GlobalVariables.regCon.hasRegister(uSingleAddr)) {
                        com.adsemicon.anmg08d.GlobalVariables.regCon.setRegister(uSingleAddr, uSingleVal)
                        displayAllRegisters()
                    }
                }
            }
        }
    }

    private val listenerReadSingle = View.OnClickListener {
        var errStr:String

        // Send Packet if all valid
        if (!singleRegAddrValid) {
            errStr = "Please enter correct address(HEX Format)."
            Log.d("[ADS] ", errStr)
            Toast.makeText(this, errStr, Toast.LENGTH_LONG).show()
            etSingleVal.requestFocus()
            etSingleAddr.requestFocus()

        } else {
            Packet.send(PacketKind.RegSingleRead,
                uSingleAddr.toByte()) // Send packet
            etSingleAddr.clearFocus()
            btnReadSingle.requestFocus()


            hideKeyboard(etSingleAddr)
        }
    }

    private val listenerWriteSingle = View.OnClickListener {
        var errStr :String
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
            Packet.send(PacketKind.RegSingleWrite, ba) // Send packet
            etSingleAddr.clearFocus()
            btnReadSingle.requestFocus()
            btnWriteSingle.requestFocus()


            displaySingleRegVal(uSingleAddr, uSingleVal)
            hideKeyboard(etSingleAddr)
        }
    }

    private val listenerReadAll = View.OnClickListener {
        try {
            rwAll = true
            regIndex = 0

            setControlEnabled(false)

            Packet.send(PacketKind.RegSingleRead,
                GlobalVariables.regCon.registers[regIndex].addr.toByte()) // Send packet
            pbRegister.progress = 0
        } catch (ex: Exception) {
            Log.d("[ADS] ", ex.message!!)
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    private val listenerWriteAll = View.OnClickListener {
        try {
            rwAll = true
            regIndex = 0

            setControlEnabled(false)

            var ba = ByteArray(2)
            ba[0] = GlobalVariables.regCon.registers[regIndex].addr.toByte()  // Address
            ba[1] = GlobalVariables.regCon.registers[regIndex].value.toByte()  // Value

            Packet.send(PacketKind.RegSingleWrite, ba) // Send packet
            pbRegister.progress = 0
        } catch (ex: Exception) {
            Log.d("[ADS] ", ex.message!!)
            Toast.makeText(this, ex.message, Toast.LENGTH_LONG).show()
        }
    }

    private val listenerGridClick = object : AdapterView.OnItemClickListener {
        override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
            when (p0?.id) {
                R.id.gridAllRegisters -> {
                    val addr = com.adsemicon.anmg08d.GlobalVariables.regCon.registers[p2].addr
                    val value = com.adsemicon.anmg08d.GlobalVariables.regCon.registers[p2].value
                    etSingleAddr.setText(String.format("%02X", addr.toByte()))
                    etSingleVal.setText(String.format("%02X", value.toByte()))

                    etSingleAddr.requestFocus()
                    etSingleVal.requestFocus()

                    hideKeyboard(p0)
                }
            }
        }
    }

    private val listenerClose = View.OnClickListener {
        setResult(RESULT_OK, intent)
        finish()
    }

    private fun checkConnections() {
        if (GlobalVariables.isBtConnected) {
            if ((GlobalVariables.hwStat and 0x06) != 0x06.toByte()) {
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
            //setControlEnabled(true) // for debugging, 임시
        }
    }

    private fun setControlEnabled(flag: Boolean) {
        etSingleAddr.isEnabled = flag
        etSingleVal.isEnabled = flag
        btnReadSingle.isEnabled = flag
        btnWriteSingle.isEnabled = flag
        btnReadAllReg.isEnabled = flag
        btnWriteAllReg.isEnabled = flag
        //btnClose.isEnabled = flag
    }

    private fun displaySingleRegVal(address: UByte, value: UByte) {
        etSingleAddr.setText(String.format("%02X", address.toByte()))
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
                synchronized(GlobalVariables.regQueue) { qEmpty = GlobalVariables.regQueue.isEmpty() }

                if (!qEmpty) {
                    try {
                        synchronized(GlobalVariables.regQueue) { packet = GlobalVariables.regQueue.remove() }

                        when (packet.kind) {
                            PacketKind.RegSingleRead -> packetProcRegRead(packet)
                            PacketKind.RegSingleWrite -> {
                                packetProcRegWrite()
                            }
                            else -> {}    // Do nothing
                        }
                    } catch (ex: NoSuchElementException) {
                        com.adsemicon.anmg08d.GlobalVariables.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.toString())
                        continue
                    } catch (ex: java.lang.Exception) {
                        com.adsemicon.anmg08d.GlobalVariables.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.message.toString())
                        Log.d("[ADS/ERR] ", ex.printStackTrace().toString())
                        break
                    }
                } else {
                    //Thread.sleep(10)
                }
                //------------------------------------------------------------------------------//
            }
            Log.d("[ADS] ", "Register thread finished. ID : ${this.id}")
        }

        private fun packetProcRegRead(packet: RPacket) {
            val regSize = GlobalVariables.regCon.registers.size
            val uAddr = packet.dataList[0].toUByte()
            val uVal = packet.dataList[1].toUByte()

            if (GlobalVariables.regCon.hasRegister(uAddr)) {
                GlobalVariables.regCon.setRegister(uAddr, uVal)
            }

            if (rwAll) {
                if (++regIndex == regSize) {
                    rwAll = false
                    runOnUiThread {
                        displayAllRegisters()
                        pbRegister.progress = 0
                        setControlEnabled(true)
                    }
                } else {
                    Packet.send(PacketKind.RegSingleRead,
                        GlobalVariables.regCon.registers[regIndex].addr.toByte()) // Send packet
                    runOnUiThread {
                        pbRegister.progress =
                            ((regIndex.toDouble() / regSize.toDouble()) * 100.0).toInt()
                    }
                }
            } else {
                runOnUiThread {
                    if (GlobalVariables.regCon.hasRegister(uAddr)) {
                        displayAllRegisters()
                    }
                    displaySingleRegVal(uAddr, uVal)
                }
            }
        }

        private fun packetProcRegWrite() {
            val regSize = com.adsemicon.anmg08d.GlobalVariables.regCon.registers.size
            val ba = ByteArray(2)

            if (rwAll) {
                if (++regIndex == regSize) {
                    rwAll = false
                    runOnUiThread {
                        displayAllRegisters()
                        pbRegister.progress = 0
                        setControlEnabled(true)
                    }
                } else {
                    ba[0] = GlobalVariables.regCon.registers[regIndex].addr.toByte()
                    ba[1] = GlobalVariables.regCon.registers[regIndex].value.toByte()
                    Packet.send(PacketKind.RegSingleWrite, ba) // Send packet

                    runOnUiThread {
                        pbRegister.progress =
                            ((regIndex.toDouble() / regSize.toDouble()) * 100.0).toInt()
                    }

                }
            } else {
             //   runOnUiThread { }
            }
        }
    }
}