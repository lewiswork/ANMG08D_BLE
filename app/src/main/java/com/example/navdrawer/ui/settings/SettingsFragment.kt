package com.example.navdrawer.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.navdrawer.Global
import com.example.navdrawer.PacketKind
import com.example.navdrawer.R
import com.example.navdrawer.databinding.FragmentSettingsBinding
import com.example.navdrawer.packet.Packet
import java.lang.Exception
import kotlin.experimental.or

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("[ADS] ", "SettingsFragment > onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        setListeners()          // Listener 등록
        setControlStatus()

        Log.d("[ADS] ", "SettingsFragment > onCreateView")

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        Log.d("[ADS] ", "SettingsFragment > onDestroyView")
    }

    private fun setListeners() {
        binding.swSysLog.setOnClickListener(listenerSwitches)
        binding.swMonLog.setOnClickListener(listenerSwitches)
    }

    private fun setControlStatus() {
        binding.swSysLog.isChecked = Global.packetLog.isEnabled
    }

    private val listenerSwitches = View.OnClickListener {
        try {
            val str:String
            when (it) {
                binding.swSysLog -> {
                    Global.packetLog.isEnabled = binding.swSysLog.isChecked
                     str = if (binding.swSysLog.isChecked) "enabled" else "disabled"
                    Log.d("[ADS] ", "System log $str")
                    binding.tvStatusSettingsFrag.text ="System log $str." 
                }
                binding.swMonLog -> {
                    Global.touchLog.isEnabled = binding.swMonLog.isChecked
                    Global.percentLog.isEnabled = binding.swMonLog.isChecked
                     str = if (binding.swMonLog.isChecked) "enabled" else "disabled"
                    Log.d("[ADS] ", "Monitoring log $str")
                    binding.tvStatusSettingsFrag.text ="Monitoring log $str."
                }
                else->{}
            }
        } catch (ex: Exception) {
            Log.d("[ADS]", "${ex.message}")
        }
    }

}