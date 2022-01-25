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
import kotlin.experimental.and
import kotlin.experimental.or


class JigFragment : Fragment() {

    private lateinit var jigViewModel: JigViewModel
    private var _binding: FragmentJigBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var mmJigThread: JigFragment.JigThread
    private var mmJigThreadOn:Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        jigViewModel =
            ViewModelProvider(this).get(JigViewModel::class.java)

        _binding = FragmentJigBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Log.d("ME", "Jig Fragment > onCreateView")

        if (Global.isBtConnected) {
            //_binding?.tvJig?.text = "BT connected"

            mmJigThreadOn = true
            mmJigThread = JigThread()
            mmJigThread.start()

            _binding?.swVdd?.isEnabled = true
            _binding?.swI2c?.isEnabled = true

        } else {
            //_binding?.tvJig?.text = "BT disconnected"

            _binding?.swVdd?.isEnabled = false
            _binding?.swI2c?.isEnabled = false
            binding.swI2c.isEnabled = true
        }

        if ((Global.hwStat and 0x01.toByte()) == 0x01.toByte())_binding?.swVdd?.isChecked = true
        if ((Global.hwStat and 0x02.toByte()) == 0x02.toByte())_binding?.swI2c?.isChecked = true

        _binding?.swVdd?.setOnCheckedChangeListener(listenerCheckedChanged)
        _binding?.swI2c?.setOnCheckedChangeListener(listenerCheckedChanged)

        return root
    }

    private val listenerCheckedChanged = CompoundButton.OnCheckedChangeListener { buttonView: CompoundButton?, isChecked: Boolean ->

        var mmTxBuffer: ArrayList<Byte> = ArrayList()
        var mask: Byte = 0x00

        try {
            if (binding.swVdd.isChecked) mask = mask or 0x02
            if (binding.swI2c.isChecked) mask = mask or 0x04

            Global.hwStat = mask

            // Make packet to send
            Packet.make(PacketKind.HwWrite, mmTxBuffer, Global.hwStat)

//            // Start(STX)
//            Packet.setStart(mmTxBuffer)

//            // Header
//            Packet.setHeader(mmTxBuffer, PacketKind.HwWrite)
//
//            // Size
//            Packet.setSize(mmTxBuffer, 1)

//            // Data
//            mmTxBuffer.add(Global.hwStat)

//            // Checksum
//            Packet.setChecksum(mmTxBuffer, Packet.makeChecksum(Global.hwStat))

//            // End
//            Packet.setEnd(mmTxBuffer)

            // Send Packet
            Packet.sendPacket(Global.outStream, mmTxBuffer)

            binding.textView3.text = mask.toString()    // for debugging
        }catch (ex:Exception){
            Log.d("[ADS]", "Making packet error ! / ${ex.message}}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        mmJigThreadOn = false
        Log.d("ME", "Jig Fragment > onDestroyView")
    }

    //---------------------------------------------------------------------------------------//
    // Jig 처리용 Inner Class
    //---------------------------------------------------------------------------------------//
    inner class JigThread : Thread() {
        override fun run() {

            Log.d("ME", "Jig thread started. ID : ${this.id}")
            while (mmJigThreadOn) {
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
            Log.d("ME", "Jig thread finished. ID : ${this.id}")
        }
    }
}