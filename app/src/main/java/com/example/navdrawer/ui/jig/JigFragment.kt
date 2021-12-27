package com.example.navdrawer.ui.jig

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.navdrawer.Global
import com.example.navdrawer.databinding.FragmentJigBinding
import com.example.navdrawer.ui.jig.JigFragment

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
        savedInstanceState: Bundle?
    ): View? {
        jigViewModel =
            ViewModelProvider(this).get(JigViewModel::class.java)

        _binding = FragmentJigBinding.inflate(inflater, container, false)
        val root: View = binding.root

        Log.d("ME", "Jig Fragment > onCreateView")

        if (Global.isBtConnected) {
            _binding?.tvJig?.text = "BT Connected"

            mmJigThreadOn = true
            mmJigThread = JigThread()
            mmJigThread.start()

        } else {
            _binding?.tvJig?.text = "BT Not Connected"
        }

        return root
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
//            var pk: String = ""
//            var sb: StringBuilder = StringBuilder()
//            var sidx: Int = 0
//            var eidx: Int = 0
//            var qEmpty: Boolean = true
//            //var qCount: Int = -1

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