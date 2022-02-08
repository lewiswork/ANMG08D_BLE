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
import com.example.navdrawer.PacketKind
import com.example.navdrawer.data.RPacket
import com.example.navdrawer.function.Packet
import java.lang.Exception
import java.util.*
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        jigViewModel = ViewModelProvider(this)[JigViewModel::class.java]
        _binding = FragmentJigBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //setControlEnabled()        // BT 연결상태 별 초기화 처리
        setListeners()          // Listener 등록

        Log.d("[ADS] ", "Jig Fragment > onCreateView")
        return root
    }

    override fun onPause() {
        super.onPause()
        timer!!.cancel()

        Log.d("[ADS] ", "Jig Fragment > onPause > Timer canceled : $timer")
    }

    override fun onResume() {
        super.onResume()

        setControlEnabled()        // BT 연결상태 별 초기화 처리

        //timer = kotlin.concurrent.timer(initialDelay = 1000, period = 1000 ) {
        //timer = kotlin.concurrent.timer(period = 1000 ) {
        timer = kotlin.concurrent.timer(period = 1000 ) {
            tick = true
        }

        Log.d("[ADS] ", "Jig Fragment > onResume > Timer started : $timer")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mmJigThreadOn = false
        Log.d("[ADS] ", "Jig Fragment > onDestroyView")
    }

    private fun setControlEnabled() {
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
    }

    private fun setListeners() {
        binding.swVdd.setOnClickListener(listenerForRelays)
        binding.swI2c.setOnClickListener(listenerForRelays)
        binding.btnClearRelays.setOnClickListener(listenerForRelays)
    }
//
//    private val listenerOnClick = View.OnClickListener {
//        var mask: Byte = 0x00
//
//        try {
//            if (binding.swVdd.isChecked) mask = mask or 0x02
//            if (binding.swI2c.isChecked) mask = mask or 0x04
//            Global.hwStat = mask
//            Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet
//
//            binding.textView3.text = Global.hwStat.toString()    // for debugging
//
//        } catch (ex: Exception) {
//            Log.d("[ADS]", "Sending packet error! / ${ex.message}}")
//        }
//    }
//
//    private val listenerBtnClear = View.OnClickListener {
//        Global.hwStat = 0x00
//        Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet
//
//        binding.textView3.text = Global.hwStat.toString()    // for debugging
//    }

    private val listenerForRelays = View.OnClickListener {
        var mask: Byte = 0x00

        try {
            when (it) {
                binding.swVdd, binding.swI2c -> {
                    if (binding.swVdd.isChecked) mask = mask or 0x02
                    if (binding.swI2c.isChecked) mask = mask or 0x04
                    Global.hwStat = mask
                }
                binding.btnClearRelays -> {
                    Global.hwStat = 0x00
                    binding.swVdd.isChecked = false
                    binding.swI2c.isChecked = false
                }
            }

            if (Global.hwStatPrev == 0x06.toByte() && Global.hwStat != 0x06.toByte()) {
                Packet.send(Global.outStream, PacketKind.MonSet, 0x00)  // Stop All Monitoring
                Log.d("[ADS]", "Monitoring stopped.")
                //Thread.sleep(100) // ok
                Thread.sleep(10) // ok
            }
            Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet
            Global.hwStatPrev = Global.hwStat

            binding.textView3.text = Global.hwStat.toString()    // for debugging
        } catch (ex: Exception) {
            Log.d("[ADS]", "Sending packet error! / ${ex.message}}")
        }
    }


    //---------------------------------------------------------------------------------------//
    // Jig 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class JigThread : Thread() {
        override fun run() {
            var qEmpty :Boolean
            var packet : RPacket
            
            Log.d("[ADS] ", "Jig thread started. ID : ${this.id}")

            while (mmJigThreadOn) {
                //------------------------------------------------------------------------------//
                // Timer 처리
                //------------------------------------------------------------------------------//
                if (tick){
                    Packet.send(Global.outStream, PacketKind.HwRead) // Send packet
                    tick = false
                }

                // Packet 처리
                synchronized(this) { qEmpty = Global.hwQueue.isEmpty() }

                if (!qEmpty) {
                    try {
                        synchronized(this) { packet = Global.hwQueue.remove() }

                        when (packet.kind) {
                            PacketKind.HwRead -> {
                                Global.hwStat = packet.dataList[0]
                                activity?.runOnUiThread {
                                    displayRelayStatus()
                                }
                            }
                        }
                    } catch (ex: NoSuchElementException) {
                        Log.d("[ADS/ERR] ", ex.toString())
                        continue
                    }
                }
                //------------------------------------------------------------------------------//

                Thread.sleep(10)
            }
            Log.d("[ADS] ", "Jig thread finished. ID : ${this.id}")
        }
    }

    private fun displayRelayStatus() {
        binding.swVdd.isChecked = (Global.hwStat and 0x02) == 0x02.toByte()
        binding.swI2c.isChecked = (Global.hwStat and 0x04) == 0x04.toByte()
    }
}