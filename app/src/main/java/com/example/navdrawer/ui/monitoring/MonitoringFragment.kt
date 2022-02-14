package com.example.navdrawer.ui.monitoring

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.R
import com.example.navdrawer.databinding.FragmentMonitoringBinding
import com.example.navdrawer.packet.Packet
import java.lang.Exception
import kotlin.experimental.and
import kotlin.experimental.or

class MonitoringFragment : Fragment() {


    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var mmDisplayThread: DisplayThread
    private var mmDisplayThreadOn: Boolean = false
    private  var viewMonitoring:View?=null

    private val imgTouchStat = intArrayOf(
        R.drawable.img_white_dot,   // not touched
        R.drawable.img_blue_dot     // touched
    )

    val chStr = arrayOf("CH1", "CH2","CH3","CH4","CH5","CH6","CH7","CH8","DM")
    private val dataListMon = ArrayList<HashMap<String, Any>>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel = ViewModelProvider(this)[MonitoringViewModel::class.java]

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //setControlEnabled()        // BT 연결상태 별 초기화 처리
        setListeners()          // Listener 등록

        Log.d("[ADS] ", "Monitoring Fragment > onCreateView")

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListView(view)
        viewMonitoring = view

        //setListViewContents()
    }

    private fun initListView(view: View) {
        //val dataList = ArrayList<HashMap<String, Any>>()

        for (i in 0 until  Global.monitoring.MAX_CH_CNT) {
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

    private fun setListViewContents() {
        activity?.runOnUiThread {
            dataListMon.clear()

            for (i in 0 until Global.monitoring.MAX_CH_CNT) {
                val map = HashMap<String, Any>()
                map["chNum"] = chStr[i]
                map["img"] =
                    if (Global.monitoring.mmChData[i].touch) imgTouchStat[1] else imgTouchStat[0]     // touch status
                map["chVal"] = String.format("%.3f", Global.monitoring.mmChData[i].percent)

                dataListMon.add(map)
            }
            val keys = arrayOf("img", "chNum", "chVal")
            val ids = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)
            val adapter =
                SimpleAdapter(view?.context, dataListMon, R.layout.row_monitoring, keys, ids)


            binding.gridMon.adapter = adapter
        }

//
//        synchronized(this) {
//                        for (i in Global.monitoring.mmChData.indices) {
//                            var touch = Global.monitoring.mmChData[i].touch
//                            var percent = Global.monitoring.mmChData[i].percent
//
//                            if (i == Global.monitoring.DM_CH_IDX)
//                                str.append("CH DM : $touch / ${String.format("%.3f", percent)}\n")
//                            else
//                                str.append("CH ${i + 1} : $touch / ${
//                                    String.format("%.3f",
//                                        percent)
//                                }\n")
//                        }
    }

    override fun onResume() {
        super.onResume()

        setControlEnabled()        // BT 연결상태 별 초기화 처리
        Log.d("[ADS] ", "Monitoring Fragment > onResume")
    }

    override fun onPause() {
        super.onPause()

        mmDisplayThreadOn = false
        stopMonitoring()
        Global.waitForStopMon = true

        Log.d("[ADS] ", "Monitoring Fragment > onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        Log.d("[ADS] ", "Monitoring Fragment > onDestroyView")
    }

    private fun setControlEnabled() {
        if (Global.isBtConnected) {
            if ((Global.hwStat and 0x06) != 0x06.toByte()) {
                setBtnSwEnabled(false)
                binding.tvStatusMonFrag.text = "Relays are off."
            } else {
                setBtnSwEnabled(true)
                mmDisplayThreadOn = true
                mmDisplayThread = DisplayThread()
                mmDisplayThread.start()
                binding.tvStatusMonFrag.text = "BT connected and relays are on."
            }
        } else {
            setBtnSwEnabled(false)
            binding.tvStatusMonFrag.text = "BT disconnected."
        }
    }

    private fun setBtnSwEnabled(flag:Boolean) {
        binding.swTouch.isEnabled = flag
        binding.swPercent.isEnabled = flag
        //binding.btnClearMon.isEnabled = flag
    }

    private fun setListeners() {
        binding.swTouch.setOnClickListener(listenerOnClick)
        binding.swPercent.setOnClickListener(listenerOnClick)
        binding.btnClearMon.setOnClickListener(listenerOnClick)
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
                        Global.waitForStopMon = true
                    } else Packet.send(Global.outStream, PacketKind.MonSet, mask) // Send packet
                }
                binding.btnClearMon -> {
                    stopMonitoring()
                    Global.waitForStopMon = true
                    //setListViewContents()
                }
            }
            binding.textView8.text = mask.toString()    // for debugging

        } catch (ex: Exception) {
            Log.d("[ADS] ", "Making packet error! / ${ex.message}")
        }
    }

    private fun stopMonitoring() {
        binding.swTouch.isChecked = false
        binding.swPercent.isChecked = false

        Packet.send(Global.outStream, PacketKind.MonSet, 0x00)
    }

    //---------------------------------------------------------------------------------------//
    // Display 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class DisplayThread : Thread() {
        override fun run() {

            Log.d("[ADS] ", "Display thread started. ID : ${this.id}")
            while (mmDisplayThreadOn) {
                if (Global.monitoring.hasNewData) {
                    var str = java.lang.StringBuilder()

                    // -------------------------------------------------------------------------//
                    // Display Touch and Percent(임시)
                    // -------------------------------------------------------------------------//
                    //synchronized(this) {
//                        for (i in Global.monitoring.mmChData.indices) {
//                            var touch = Global.monitoring.mmChData[i].touch
//                            var percent = Global.monitoring.mmChData[i].percent
//
//                            if (i == Global.monitoring.DM_CH_IDX)
//                                str.append("CH DM : $touch / ${String.format("%.3f", percent)}\n")
//                            else
//                                str.append("CH ${i + 1} : $touch / ${
//                                    String.format("%.3f",
//                                        percent)
//                                }\n")
//                        }

                        //setListViewContents()
                    //}

                    activity?.runOnUiThread {
                        binding.tvStatusMonFrag.text = str.toString()
                        setListViewContents()
                        if (Global.waitForStopMon) stopMonitoring()
                    }
                    // -------------------------------------------------------------------------//


                    Global.monitoring.hasNewData = false
                }
                Thread.sleep(10)
            }
            Log.d("[ADS] ", "Display thread finished. ID : ${this.id}")
        }
    }
}