package com.example.audilu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.audilu.databinding.FragmentMainBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainFragment : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        val root = binding.root

        var viewPager : ViewPager2 = binding.viewPager
        var tablayout : TabLayout = binding.tablayout

        viewPager.adapter = FragmentAdapter(this)
        TabLayoutMediator(tablayout,viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Preferencias"
                1 -> tab.text = "Home"
            }
        }.attach()
        return root
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}