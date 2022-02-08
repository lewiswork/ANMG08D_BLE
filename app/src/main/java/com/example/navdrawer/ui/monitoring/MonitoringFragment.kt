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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel = ViewModelProvider(this)[MonitoringViewModel::class.java]

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //setControlEnabled()        // BT 연결상태 별 초기화 처리
        setListeners()          // Listener 등록

        Log.d("ME", "Monitoring Fragment > onCreateView")
        return root
    }

    override fun onResume() {
        super.onResume()
        setControlEnabled()        // BT 연결상태 별 초기화 처리
        Log.d("ME", "Monitoring Fragment > onResume")
    }

    override fun onPause() {
        super.onPause()
        mmDisplayThreadOn = false

        Log.d("ME", "Monitoring Fragment > onPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        //mmDisplayThreadOn = false
        Log.d("ME", "Monitoring Fragment > onDestroyView")
    }

    private fun setControlEnabled() {
        if (Global.isBtConnected) {
            //binding.tvStatusMonFrag.text = "BT connected."

            if ((Global.hwStat and 0x06) != 0x06.toByte()) {
                binding.tvStatusMonFrag.text = "Relays are turned off."
            } else {
                mmDisplayThreadOn = true
                mmDisplayThread = DisplayThread()
                mmDisplayThread.start()
                binding.tvStatusMonFrag.text = "BT connected and relays are turned off."
            }
        } else {
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
            when(it)
            {
                binding.swTouch, binding.swPercent ->{
                    if (binding.swTouch.isChecked) mask = mask or 0x01
                    if (binding.swPercent.isChecked) mask = mask or 0x02

                    Packet.send(Global.outStream, PacketKind.MonSet, mask) // Send packet
                }

                binding.btnClearMon -> Packet.send(Global.outStream, PacketKind.MonSet, 0x00) // Send packet
            }

//
//
            binding.textView8.text = mask.toString()    // for debugging

        } catch (ex: Exception) {
            Log.d("[ADS]", "Making packet error! / ${ex.message}")
        }
    }



    //---------------------------------------------------------------------------------------//
    // Display 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class DisplayThread : Thread() {
        override fun run() {

            Log.d("ME", "Display thread started. ID : ${this.id}")
            while (mmDisplayThreadOn) {

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
                }
                Thread.sleep(10)
            }
            Log.d("ME", "Display thread finished. ID : ${this.id}")
        }
    }
}