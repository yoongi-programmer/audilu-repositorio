package com.example.audilu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

abstract class BaseTabFragment : Fragment() {
    protected fun setupTabLayout(tabLayout: TabLayout) {
        val currentFragmentId = findNavController().currentDestination?.id
        if (currentFragmentId == R.id.homeFragment) {
            tabLayout.getTabAt(1)?.select() // Selecciona la pestaña de Home
        } else if (currentFragmentId == R.id.preferenciasFragment) {
            tabLayout.getTabAt(0)?.select() // Selecciona la pestaña de Preferencias
        }

        // Configurar el listener de clics en las pestañas
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> navigateToFragment(R.id.preferenciasFragment)

                    1 -> navigateToFragment(R.id.homeFragment)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun navigateToFragment(fragmentId: Int) {
        findNavController().navigate(fragmentId)
    }
}