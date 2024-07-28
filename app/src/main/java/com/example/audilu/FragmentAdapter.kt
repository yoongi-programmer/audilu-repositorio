package com.example.audilu

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter(fa: MainFragment) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> preferenciasFragment()
            1 -> homeFragment()
            else -> preferenciasFragment()
        }
    }
}