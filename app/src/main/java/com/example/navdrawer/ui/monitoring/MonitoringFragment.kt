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

//    val data1 = arrayOf("CH1", "CH2", "CH3", "CH4", "CH5", "CH6", "CH7", "CH8", "DM")
//    val data2 = arrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
//    val imgRes = intArrayOf(
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot,
//        R.drawable.img_white_dot
//    )

    val dataChA = arrayOf("CH1", "CH2", "CH3", "CH4", "DM")
    val dataChB = arrayOf("CH5", "CH6", "CH7", "CH8")
    val dataValA = arrayOf(0.0, 0.0, 0.0, 0.0, 0.0)
    val dataValB = arrayOf(0.0, 0.0, 0.0, 0.0)

    val imgA = intArrayOf(
        R.drawable.img_white_dot,
        R.drawable.img_white_dot,
        R.drawable.img_white_dot,
        R.drawable.img_white_dot,
        R.drawable.img_white_dot
    )

    val imgB = intArrayOf(
        R.drawable.img_blue_dot,
        R.drawable.img_blue_dot,
        R.drawable.img_blue_dot,
        R.drawable.img_blue_dot
    )

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

        setListViewContents()
    }

    private fun initListView(view: View) {
        // A
        val dataListA = ArrayList<HashMap<String, Any>>()
        for (i in imgA.indices) {
            val map = HashMap<String, Any>()
            map["img"] = imgA[i]
            map["chNum"] = dataChA[i]
            map["chVal"] = dataValA[i]
            dataListA.add(map)
        }

        val keysA = arrayOf("img", "chNum", "chVal")
        val idsA = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)

        val adapterA = SimpleAdapter(view.context, dataListA, R.layout.list_monitoring, keysA, idsA)
        binding.listMon1.adapter = adapterA

        // B
        val dataListB = ArrayList<HashMap<String, Any>>()
        for (i in imgB.indices) {
            val map = HashMap<String, Any>()
            map["img"] = imgB[i]
            map["chNum"] = dataChB[i]
            map["chVal"] = dataValB[i]
            dataListB.add(map)
        }

        val keysB = arrayOf("img", "chNum", "chVal")
        val idsB = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)

        val adapterB = SimpleAdapter(view.context, dataListB, R.layout.list_monitoring, keysB, idsB)
        binding.listMon2.adapter = adapterB

//        binding.list1.setOnItemClickListener { adapterView, view, i, l ->
//            binding.textView8.text = "${data2[i]} clicked "
//        }
    }

//    private fun setListViewContents() {
//        //
//        val dataList = ArrayList<HashMap<String, Any>>()
//        for (i in 5..5) {
//            val map = HashMap<String, Any>()
//            map["img"] = imgRes[1]
//            map["data1"] = data1[i]
//            map["data2"] = "2.5%"
//            dataList.add(map)
//        }
//
//        //
//        val keys = arrayOf("img", "data1", "data2")
//
//        //
//        val ids = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)
//
//        val adapter1 = SimpleAdapter(viewMonitoring?.context, dataList, R.layout.list_monitoring, keys, ids)
//        binding.list1.adapter = adapter1
//
//    }

    private fun setListViewContents() {

        val dataList = ArrayList<HashMap<String, Any>>()
        for (i in 2..3) {
            val map = HashMap<String, Any>()
            map["img"] = imgB[1]
            map["data1"] = dataChA[i]
            map["data2"] = "2.5%"
            dataList.add(map)
        }

        //
        val keys = arrayOf("img", "data1", "data2")

        //
        val ids = intArrayOf(R.id.ivDot, R.id.tvChNum, R.id.tvPercent)

        val adapter1 = SimpleAdapter(viewMonitoring?.context, dataList, R.layout.list_monitoring, keys, ids)
        binding.listMon1.adapter = adapter1

        //binding.listMon1.adapter.

        for (i in 0 until Global.monitoring.MAX_CH_CNT){
            when(i){
                9 -> {
                    //var item = binding.listMon1.adapter.

                } // Dummy channel
                in 0..3 ->{}    // CH1~4
                in 4..7 ->{}    // CH5~8
            }
        }

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
                    setListViewContents()
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
                    synchronized(this) {
                        for (i in Global.monitoring.mmChData.indices) {
                            var touch = Global.monitoring.mmChData[i].touch
                            var percent = Global.monitoring.mmChData[i].percent

                            if (i == Global.monitoring.DM_CH_IDX)
                                str.append("CH DM : $touch / ${String.format("%.3f", percent)}\n")
                            else
                                str.append("CH ${i + 1} : $touch / ${
                                    String.format("%.3f",
                                        percent)
                                }\n")
                        }
                    }

                    activity?.runOnUiThread {
                        binding.tvStatusMonFrag.text = str.toString()
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