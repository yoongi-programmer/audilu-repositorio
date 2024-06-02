package com.example.audilu
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.audilu.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    //generar clase de vinculación de vistas (binding)
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout para este fragmento con el metodo binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root = binding.root
        // Aquí puedes acceder a los elementos de la vista usando binding.nombreDelView
        binding.btnNav.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_firstFragment)//Ir al primer fragmento
        }
        binding.btnVibrate.setOnClickListener {
            vibrate(500L)
        }
        return root
    }
//CODE function FOR VIBRATION
    private fun vibrate(duration: Long = 500L) {
        // Implementación de la función vibrate()
        val vibrator = ContextCompat.getSystemService(requireContext(), Vibrator::class.java)

        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(duration)
            }
        }
    }
}

