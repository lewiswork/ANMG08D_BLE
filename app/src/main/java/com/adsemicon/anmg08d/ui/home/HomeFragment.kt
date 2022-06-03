package com.adsemicon.anmg08d.ui.home

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SimpleAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.adsemicon.anmg08d.R
import com.adsemicon.anmg08d.databinding.FragmentHomeBinding
import androidx.appcompat.app.AppCompatActivity


class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val menuNames = arrayOf(
        "1. Connect", "2. Jig", "3. Monitoring", "4. Settings", "5. About"
    )

    private val menuDescriptions = arrayOf(
        "Bluetooth 연결",
        "Jig 설정(Relay On/Off 등)",
        "Touch Data Monitoring 및 Register 설정",
        "App. 설정(Log On/Off 등)",
        "App. 정보"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //val root: View = binding.root

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvHomeTitle.text = "\n좌측 상단 Navigation 메뉴를 사용하세요.\n"

        val dataList = ArrayList<HashMap<String, Any>>()
        for (i in menuNames.indices) {
            val map = HashMap<String, Any>()
            map["data1"] = menuNames[i]
            map["data2"] = menuDescriptions[i]
            dataList.add(map)
        }
        val keys = arrayOf("data1", "data2")
        val ids = intArrayOf(R.id.tvMenuName, R.id.tvMenuDescription)
        val adapter1 = SimpleAdapter(requireContext(), dataList, R.layout.row_home, keys, ids)
        binding.lvMenuInfo.adapter = adapter1
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}