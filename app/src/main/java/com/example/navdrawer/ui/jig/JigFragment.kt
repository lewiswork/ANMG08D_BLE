package com.example.navdrawer.ui.jig

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.navdrawer.Global
import com.example.navdrawer.databinding.FragmentJigBinding
import android.widget.CompoundButton
import com.example.navdrawer.PacketKind
import com.example.navdrawer.function.Packet
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timer
import kotlin.experimental.and
import kotlin.experimental.or


class JigFragment : Fragment() {

    private lateinit var jigViewModel: JigViewModel
    private var _binding: FragmentJigBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mmJigThread: JigFragment.JigThread
    private var mmJigThreadOn:Boolean = false

    // Hardware Read Packet 용 Timer
    var tick=false
    var timer : Timer? = null
//    val timer = kotlin.concurrent.timer( period = 1000, initialDelay = 3000) {
//        tick = true
//    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        jigViewModel =
            ViewModelProvider(this).get(JigViewModel::class.java)

        _binding = FragmentJigBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Log.d("[ADS] ", "Jig Fragment > onCreateView")

        if (Global.isBtConnected) {

            mmJigThreadOn = true
            mmJigThread = JigThread()
            mmJigThread.start()

            binding.swVdd.isEnabled = true
            binding.swI2c.isEnabled = true

        } else {
            binding.swVdd.isEnabled = false
            binding.swI2c.isEnabled = false
        }

        if ((Global.hwStat and 0x02.toByte()) == 0x02.toByte()) binding.swVdd.isChecked = true
        if ((Global.hwStat and 0x04.toByte()) == 0x04.toByte()) binding.swI2c.isChecked = true

        binding.swVdd.setOnCheckedChangeListener(listenerCheckedChanged)
        binding.swI2c.setOnCheckedChangeListener(listenerCheckedChanged)

        return root
    }

    private val listenerCheckedChanged = CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
        var mask: Byte = 0x00

        try {
            if (binding.swVdd.isChecked) mask = mask or 0x02
            if (binding.swI2c.isChecked) mask = mask or 0x04

            Global.hwStat = mask

            Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet

            binding.textView3.text = mask.toString()    // for debugging

        } catch (ex: Exception) {
            Log.d("[ADS]", "Making packet error! / ${ex.message}}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mmJigThreadOn = false
        Log.d("[ADS] ", "Jig Fragment > onDestroyView")
    }

    override fun onResume() {
        super.onResume()

        timer = kotlin.concurrent.timer(initialDelay = 1000, period = 1000 ) {
            tick = true
        }

        Log.d("[ADS] ", "Jig Fragment > onResume")
        Log.d("[ADS] ", "Jig Fragment > onResume > Timer started : $timer")
    }

    override fun onPause() {
        super.onPause()
        timer!!.cancel()

        Log.d("[ADS] ", "Jig Fragment > onPause")
        Log.d("[ADS] ", "Jig Fragment > onResume > Timer canceled : $timer")
    }

    //---------------------------------------------------------------------------------------//
    // Jig 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class JigThread : Thread() {
        override fun run() {

            Log.d("[ADS] ", "Jig thread started. ID : ${this.id}")
            while (mmJigThreadOn) {

                if (tick){
//                    binding.textView3.text = i.toString()
//                    Log.d("[ADS] ", "Jig thread started. ID : ${i}")
                    //Packet.send(Global.outStream, PacketKind.HwWrite, 0x06) // Send packet
                    Packet.send(Global.outStream, PacketKind.HwRead) // Send packet
                    tick = false
                }
                //Packet.send(Global.outStream, PacketKind.HwRead) // Send packet
                //Packet.send(Global.outStream, PacketKind.HwWrite, 0x03) // Send packet
//
//                //var str = java.lang.StringBuilder()
//
//                synchronized(this) {
//                    for (i in Global.monitoring.mmChData.indices) {
//                        if (i == Global.monitoring.DM_CH_IDX)
//                            str.append("CH DM : ${Global.monitoring.mmChData[i].touch}")
//                        else
//                            str.append("CH ${i + 1} : ${Global.monitoring.mmChData[i].touch}\n")
//                    }
//                }
//                activity?.runOnUiThread {
//                    _binding?.tvMonitoring?.text = str.toString()
//                }
//
                Thread.sleep(10)
            }
            Log.d("[ADS] ", "Jig thread finished. ID : ${this.id}")
        }
    }
}