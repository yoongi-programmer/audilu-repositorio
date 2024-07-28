package com.example.audilu
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.audilu.databinding.FragmentPreferenciasBinding

class preferenciasFragment : Fragment() {

    private var _binding: FragmentPreferenciasBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout para este fragmento con el método binding
        _binding = FragmentPreferenciasBinding.inflate(inflater, container, false)
        val root = binding.root

        return root
    }
}

