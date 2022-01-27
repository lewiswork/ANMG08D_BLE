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

        if ((Global.hwStat and 0x02.toByte()) == 0x02.toByte()) binding.swVdd.isChecked = true
        if ((Global.hwStat and 0x04.toByte()) == 0x04.toByte()) binding.swI2c.isChecked = true

//        binding.swVdd.setOnCheckedChangeListener(listenerCheckedChanged)
//        binding.swI2c.setOnCheckedChangeListener(listenerCheckedChanged)

        binding.swVdd.setOnClickListener(listenerOnClick)
        binding.swI2c.setOnClickListener(listenerOnClick)

        return root
    }

//    private val listenerCheckedChanged = CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->
//
//        //if (sendPacketEnabled) {
//            var mask: Byte = 0x00
//
//            try {
//                if (binding.swVdd.isChecked) mask = mask or 0x02
//                if (binding.swI2c.isChecked) mask = mask or 0x04
//
//                Global.hwStat = mask
//
//                Packet.send(Global.outStream, PacketKind.HwWrite, Global.hwStat) // Send packet
//
//                binding.textView3.text = mask.toString()    // for debugging
//
//            } catch (ex: Exception) {
//                Log.d("[ADS]", "Making packet error! / ${ex.message}}")
//            }
//        //}
//    }

    private val listenerOnClick = View.OnClickListener {
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

                        when(packet.kind){
                            PacketKind.HwRead ->{
                                // using packet queue test
//                                val str = String.format("%02X", packet.dataList[0])
//                                Log.d("[ADS] ", "Rx HR Packet Data is $str.")

                                val relayStatus = packet.dataList[0]
//                                if (binding.swVdd.isChecked) (relayStatus and 0x02) == 0x02.toByte()
//                                if (binding.swI2c.isChecked) (relayStatus and 0x04) == 0x04.toByte()

                                //sendPacketEnabled=false
                                activity?.runOnUiThread {
                                    binding.swVdd.isChecked = (relayStatus and 0x02) == 0x02.toByte()
                                    binding.swI2c.isChecked = (relayStatus and 0x04) == 0x04.toByte()
                                }
                                //sendPacketEnabled=true

                                //binding.swVdd.setChecked
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
}