package com.adsemicon.anmg08d.ui.settings

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adsemicon.anmg08d.GlobalVariables
import com.adsemicon.anmg08d.databinding.FragmentSettingsBinding
import java.lang.Exception

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
        binding.swSysLog.isChecked = GlobalVariables.packetLog.isEnabled
        binding.swMonLog.isChecked = GlobalVariables.touchLog.isEnabled && GlobalVariables.percentLog.isEnabled
        binding.tvStatusSettingsFrag.text=""
    }

    private val listenerSwitches = View.OnClickListener {
        try {
            val str:String
            when (it) {
                binding.swSysLog -> {
                    GlobalVariables.packetLog.isEnabled = binding.swSysLog.isChecked
                     str = if (binding.swSysLog.isChecked) "enabled" else "disabled"
                    Log.d("[ADS] ", "System log $str")
                    binding.tvStatusSettingsFrag.text ="System log $str." 
                }
                binding.swMonLog -> {
                    GlobalVariables.touchLog.isEnabled = binding.swMonLog.isChecked
                    GlobalVariables.percentLog.isEnabled = binding.swMonLog.isChecked
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