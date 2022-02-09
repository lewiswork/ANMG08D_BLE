package com.example.navdrawer.ui.monitoring

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.databinding.FragmentMonitoringBinding
import com.example.navdrawer.function.Packet
import java.lang.Exception
import kotlin.experimental.and
import kotlin.experimental.or

class MonitoringFragment : Fragment() {
    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    private lateinit var mmDisplayThread: DisplayThread
    private var mmDisplayThreadOn:Boolean = false

    //private var waitForStopMon = false

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

        //Log.d("[ADS] ", "Monitoring Fragment > onPause> Monitoring stopped.")
        Log.d("[ADS] ", "Monitoring Fragment > onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        //mmDisplayThreadOn = false
        Log.d("[ADS] ", "Monitoring Fragment > onDestroyView")
    }

    private fun setControlEnabled() {
        if (Global.isBtConnected) {
            if ((Global.hwStat and 0x06) != 0x06.toByte()) {
                binding.swTouch.isEnabled = false
                binding.swPercent.isEnabled = false
                binding.btnClearMon.isEnabled = false

                binding.tvStatusMonFrag.text = "Relays are off."
            } else {
                binding.swTouch.isEnabled = true
                binding.swPercent.isEnabled = true
                binding.btnClearMon.isEnabled = true

                mmDisplayThreadOn = true
                mmDisplayThread = DisplayThread()
                mmDisplayThread.start()
                binding.tvStatusMonFrag.text = "BT connected and relays are on."
            }
        } else {
            binding.swTouch.isEnabled = false
            binding.swPercent.isEnabled = false
            binding.btnClearMon.isEnabled = false

            binding.tvStatusMonFrag.text = "BT disconnected."
        }
    }

    private fun setListeners() {
        binding.swTouch.setOnClickListener(listenerOnClick)
        binding.swPercent.setOnClickListener(listenerOnClick)
        binding.btnClearMon.setOnClickListener(listenerOnClick)
    }

    private val listenerOnClick = View.OnClickListener {
        var mask: Byte =0x00

        try {
            when(it) {
                binding.swTouch, binding.swPercent -> {
                    if (binding.swTouch.isChecked) mask = mask or 0x01
                    if (binding.swPercent.isChecked) mask = mask or 0x02

                    //Packet.send(Global.outStream, PacketKind.MonSet, mask) // Send packet
                    if (mask == 0x00.toByte()) {
                        stopMonitoring()
                        Global.waitForStopMon = true
                    }
                    else Packet.send(Global.outStream, PacketKind.MonSet, mask) // Send packet
                }
                binding.btnClearMon -> {
                    stopMonitoring()
                    Global.waitForStopMon = true
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
        //Log.d("[ADS] ", "Monitoring stopped.$waitEnable")

        //Global.waitForStopMon = waitEnable
    }

    //---------------------------------------------------------------------------------------//
    // Display 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class DisplayThread : Thread() {
        override fun run() {

            Log.d("[ADS] ", "Display thread started. ID : ${this.id}")
            while (mmDisplayThreadOn) {
                if (Global.monitoring.updated){
                    var str = java.lang.StringBuilder()

                    synchronized(this) {
                        for (i in Global.monitoring.mmChData.indices) {
                            if (i == Global.monitoring.DM_CH_IDX)
                                str.append("CH DM : ${Global.monitoring.mmChData[i].touch}")
                            else
                                str.append("CH ${i + 1} : ${Global.monitoring.mmChData[i].touch}\n")
                        }
                    }
                    activity?.runOnUiThread {
                        binding.tvStatusMonFrag.text = str.toString()
                        if (Global.waitForStopMon){
                            stopMonitoring()
                        }
                    }


                }
                Thread.sleep(10)
            }
            Log.d("[ADS] ", "Display thread finished. ID : ${this.id}")
        }
    }
}