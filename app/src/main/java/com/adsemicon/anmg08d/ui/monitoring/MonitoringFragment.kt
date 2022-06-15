package com.adsemicon.anmg08d.ui.monitoring

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import com.adsemicon.anmg08d.R
import com.adsemicon.anmg08d.databinding.FragmentMonitoringBinding
import com.adsemicon.anmg08d.packet.Packet
import com.adsemicon.anmg08d.packet.RPacket
import java.lang.Exception
import java.text.DecimalFormat
import kotlin.experimental.and
import kotlin.experimental.or
import com.adsemicon.anmg08d.GlobalVariables
import com.adsemicon.anmg08d.PacketKind

class MonitoringFragment : Fragment() {

    private val REGISTER_ACTIVYTY = 0

    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var displayThread: DisplayThread
    private var displayThreadOn: Boolean = false
    private var viewMonitoring: View? = null

    private val imgTouchStat = intArrayOf(
        R.drawable.img_white_dot,   // not touched
        R.drawable.img_blue_dot     // touched
    )

    private val chStr = arrayOf("CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8", "DM")
    private val dataListMon = ArrayList<HashMap<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel = ViewModelProvider(this)[MonitoringViewModel::class.java]
        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setListeners()          // Listener 등록
        Log.d("[ADS] ", "Monitoring Fragment > onCreateView")

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initGridView(view)
        viewMonitoring = view

        Log.d("[ADS] ", "Monitoring Fragment > onViewCreated")
    }

    override fun onResume() {
        super.onResume()

        checkConnections()        // BT 연결상태 별 초기화 처리
        Log.d("[ADS] ", "Monitoring Fragment > onResume")
    }

    override fun onPause() {
        super.onPause()

        displayThreadOn = false

        if (binding.swTouch.isChecked || binding.swPercent.isChecked) {
            stopMonitoring()
        }

        GlobalVariables.waitForStopMon = true

        Log.d("[ADS] ", "Monitoring Fragment > onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        Log.d("[ADS] ", "Monitoring Fragment > onDestroyView")
    }


    private fun initGridView(view: View) {
        for (i in 0 until GlobalVariables.monitoring.MAX_CH_CNT) {
            val map = HashMap<String, Any>()
            map["chNum"] = chStr[i]
            map["img"] = imgTouchStat[0]    // touch status
            map["chVal"] = "0.000 %"
            dataListMon.add(map)
        }
        val keys = arrayOf("img", "chNum", "chVal")
        val ids = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)

        val adapter = SimpleAdapter(view.context, dataListMon, R.layout.row_monitoring, keys, ids)
        binding.gridMon.adapter = adapter
    }

//    private fun updateMonData() {
//        dataListMon.clear()
//        var percent: Double
//
//        for (i in 0 until GlobalVariables.monitoring.MAX_CH_CNT) {
//            val map = HashMap<String, Any>()
//            map["chNum"] = chStr[i]
//            synchronized(GlobalVariables.monitoring.channels) {
//                map["img"] =
//                    if (GlobalVariables.monitoring.channels[i].touch) imgTouchStat[1] else imgTouchStat[0]     // touch status
//
//                percent = GlobalVariables.monitoring.channels[i].percent
//            }
//            var df = DecimalFormat("0.000")
//            var perStr = df.format(percent)
//
//            if (percent >= 0) map["chVal"] = " $perStr %"   // (-) 표시 부분만큼 앞에 공백 추가
//            else map["chVal"] = "$perStr %"
//
//            dataListMon.add(map)
//        }
//        val keys = arrayOf("img", "chNum", "chVal")
//        val ids = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)
//        val adapter =
//            SimpleAdapter(view?.context, dataListMon, R.layout.row_monitoring, keys, ids)
//
//        binding.gridMon.adapter = adapter
//    }

    private fun updateMonData() {
        // Graphical Display 시 반응 속도 저하 관련 테스트를 위해 임시 주석 처리
        dataListMon.clear()
        var percent: Double

        for (i in 0 until GlobalVariables.monitoring.MAX_CH_CNT) {
            val map = HashMap<String, Any>()
            map["chNum"] = chStr[i]
            synchronized(GlobalVariables.monitoring.channels) {
                map["img"] =
                    if (GlobalVariables.monitoring.channels[i].touch) imgTouchStat[1] else imgTouchStat[0]     // touch status

                percent = GlobalVariables.monitoring.channels[i].percent
            }
            var df = DecimalFormat("0.000")
            var perStr = df.format(percent)

            if (percent >= 0) map["chVal"] = " $perStr %"   // (-) 표시 부분만큼 앞에 공백 추가
            else map["chVal"] = "$perStr %"

            dataListMon.add(map)
        }
        val keys = arrayOf("img", "chNum", "chVal")
        val ids = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)
        val adapter =
            SimpleAdapter(view?.context, dataListMon, R.layout.row_monitoring, keys, ids)

        binding.gridMon.adapter = adapter

        //binding.tvStatusMonFrag.text = GlobalVariables.monitoring.channels[6].touch.toString()
    }

    private fun checkConnections() {
        if (com.adsemicon.anmg08d.GlobalVariables.isBtConnected) {
            if ((com.adsemicon.anmg08d.GlobalVariables.hwStat and 0x06) != 0x06.toByte()) {
                setControlEnabled(false)
                binding.tvStatusMonFrag.text = "Relays are off."
            } else {
                setControlEnabled(true)
                displayThreadOn = true
                displayThread = DisplayThread()
                displayThread.start()
                binding.tvStatusMonFrag.text = "BT connected and relays are on."
            }
        } else {
            setControlEnabled(false)
            binding.tvStatusMonFrag.text = "BT disconnected."
        }
    }

    private fun setControlEnabled(flag: Boolean) {
        binding.swTouch.isEnabled = flag
        binding.swPercent.isEnabled = flag
        binding.btnClearMon.isEnabled = flag
        binding.btnSwReset.isEnabled = flag
    }

    private fun setListeners() {
        binding.swTouch.setOnClickListener(listenerOnClick)
        binding.swPercent.setOnClickListener(listenerOnClick)
        binding.btnClearMon.setOnClickListener(listenerOnClick)
        binding.btnRegister.setOnClickListener(listenerBtnRegClick)
        binding.btnSwReset.setOnClickListener(listenerSwReset)
    }

    private val listenerOnClick = View.OnClickListener {
        var mask: Byte = 0x00

        try {
            when (it) {
                binding.swTouch, binding.swPercent -> {
                    if (binding.swTouch.isChecked) mask = mask or 0x01
                    if (binding.swPercent.isChecked) mask = mask or 0x02

                    if (mask == 0x00.toByte()) {
                        stopMonitoring()
                        GlobalVariables.waitForStopMon = true
                    } else Packet.send(PacketKind.MonSet, mask) // Send packet
                }
                binding.btnClearMon -> {
                    stopMonitoring()
                    GlobalVariables.waitForStopMon = true
                }
            }
        } catch (ex: Exception) {
            Log.d("[ADS] ", "Making packet error! / ${ex.message}")
        }
    }

    private val listenerBtnRegClick = View.OnClickListener {
            val intent = Intent(this@MonitoringFragment.context, RegisterActivity::class.java)

            try {
                startActivityForResult(intent, REGISTER_ACTIVYTY)
            } catch (ex: Exception) {

            }
    }

    private val listenerSwReset = View.OnClickListener {
        Packet.send(PacketKind.RegSwReset) // Send packet
        GlobalVariables.waitForSwReset=true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REGISTER_ACTIVYTY) {
            if (resultCode == Activity.RESULT_OK) {
            } else if (resultCode == Activity.RESULT_CANCELED) {
            }
        }
    }

    private fun stopMonitoring() {
        binding.swTouch.isChecked = false
        binding.swPercent.isChecked = false

        Packet.send(PacketKind.MonSet, 0x00)
    }

    //---------------------------------------------------------------------------------------//
    // Display 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class DisplayThread : Thread() {
        override fun run() {
    //inner class DisplayThread : ADThread() { // AD Lib 사용 테스트 -> Compile Error, 추후 재 테스트 예정
        //override fun doWork() {
            var qEmpty:Boolean
            var packet: RPacket

            Log.d("[ADS] ", "Display thread started. ID : ${this.id}")
            while (displayThreadOn) {
                monitoring()

                // SW Reset Packet 미응답 시, 재 전송
                if (GlobalVariables.waitForSwReset) Packet.send(PacketKind.RegSwReset) // Send packet

                //------------------------------------------------------------------------------//
                // Packet 처리
                //------------------------------------------------------------------------------//
                synchronized(GlobalVariables.regQueue) { qEmpty = GlobalVariables.regQueue.isEmpty() }

                if (!qEmpty) {
                    try {
                        synchronized(GlobalVariables.regQueue) { packet = GlobalVariables.regQueue.remove() }

                        when (packet.kind) {
                            com.adsemicon.anmg08d.PacketKind.RegSwReset ->Log.d("[ADS] ", "SW reset done.")
                            else -> {}    // Do nothing
                        }
                    } catch (ex: NoSuchElementException) {
                        GlobalVariables.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.toString())
                        continue
                    }catch (ex: Exception) {
                        GlobalVariables.errLog.printError(ex)
                        Log.d("[ADS/ERR] ", ex.message.toString())
                        Log.d("[ADS/ERR] ", ex.printStackTrace().toString())
                        break
                    }
                } else {
                    Thread.sleep(10)
                }
                //------------------------------------------------------------------------------//
                //Thread.sleep(10)
            }
            Log.d("[ADS] ", "Display thread finished. ID : ${this.id}")
        }

        private fun monitoring() {
            if (GlobalVariables.monitoring.hasNewData) {
                // -------------------------------------------------------------------------//
                // Display Touch and Percent(임시)
                // -------------------------------------------------------------------------//
                activity?.runOnUiThread {
                    updateMonData()
                    if (GlobalVariables.waitForStopMon) stopMonitoring()
                }
                // -------------------------------------------------------------------------//

                GlobalVariables.monitoring.hasNewData = false
            }
        }
    }
}