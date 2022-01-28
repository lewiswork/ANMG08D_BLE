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
import com.example.navdrawer.data.RPacket
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

    //
    //var sendPacketEnabled = true

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

        DisplayRelayStatus()

        binding.swVdd.setOnClickListener(listenerOnClick)
        binding.swI2c.setOnClickListener(listenerOnClick)

        return root
    }

    private val listenerOnClick = View.OnClickListener {
        var mask: Byte = 0x00

        try {
            if (binding.swVdd.isChecked) mask = mask or 0x02
            if (binding.swI2c.isChecked) mask = mask or 0x04

            Global.hwStat = mask
            Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet

//            var ba = ByteArray(3)
//            ba[0] = 2
//            ba[1] = 3
//            Packet.send(Global.outStream, PacketKind.HwWrite, ba, 2) // Test

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

//        timer = kotlin.concurrent.timer(initialDelay = 1000, period = 1000 ) {
//            tick = true
//        }

        //Log.d("[ADS] ", "Jig Fragment > onResume")
        Log.d("[ADS] ", "Jig Fragment > onResume > Timer started : $timer")
    }

    override fun onPause() {
        super.onPause()
        timer!!.cancel()

        //Log.d("[ADS] ", "Jig Fragment > onPause")
        Log.d("[ADS] ", "Jig Fragment > onPause > Timer canceled : $timer")
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
                                    DisplayRelayStatus()
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

    private fun DisplayRelayStatus() {
        binding.swVdd.isChecked =
            (Global.hwStat and 0x02) == 0x02.toByte()
        binding.swI2c.isChecked =
            (Global.hwStat and 0x04) == 0x04.toByte()
    }
}