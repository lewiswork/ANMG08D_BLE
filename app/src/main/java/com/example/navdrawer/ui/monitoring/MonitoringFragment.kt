package com.example.navdrawer.ui.monitoring

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navdrawer.GlobalVariables
import com.example.navdrawer.RxThread
import com.example.navdrawer.databinding.FragmentMonitoringBinding

class MonitoringFragment : Fragment() {
    private lateinit var monitoringViewModel: MonitoringViewModel
    private var _binding: FragmentMonitoringBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        monitoringViewModel =
            ViewModelProvider(this).get(MonitoringViewModel::class.java)

        _binding = FragmentMonitoringBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.tvMonitoring
//        monitoringViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        if (GlobalVariables.isBtConnected)
            _binding?.tvMonitoring?.text = "BT Connected"
        else
            _binding?.tvMonitoring?.text = "BT Not Connected"


//        val rTh = RxThread()
//        rTh.start()


        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}