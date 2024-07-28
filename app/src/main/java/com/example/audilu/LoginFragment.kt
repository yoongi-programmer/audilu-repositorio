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
import android.hardware.camera2.CameraManager
import androidx.annotation.RequiresApi
import com.google.android.material.snackbar.Snackbar
import android.content.Context

class LoginFragment : Fragment() {
    // Generar clase de vinculación de vistas (binding)
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var cameraM: CameraManager
    var isFlash = false

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout para este fragmento con el método binding
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val root = binding.root

        // Inicializa el CameraManager
        cameraM = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Aquí puedes acceder a los elementos de la vista usando binding.nombreDelView
        binding.btnNav.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_firstFragment) // Ir al primer fragmento
        }
        binding.btnVibrate.setOnClickListener {
            vibrate(500L)
        }
        binding.btnFlash.setOnClickListener {
            flashLightOnRoOff(it)
        }
        return root
    }

    // Función para encender/apagar el flash
    @RequiresApi(Build.VERSION_CODES.M)
    private fun flashLightOnRoOff(v: View?) {
        /** Set flash code */
        try {
            val cameraListId = cameraM.cameraIdList[0]
            if (!isFlash) {
                cameraM.setTorchMode(cameraListId, true)
                isFlash = true
                Snackbar.make(requireView(), "Flash Light is On", Snackbar.LENGTH_SHORT).show()
            } else {
                cameraM.setTorchMode(cameraListId, false)
                isFlash = false
                Snackbar.make(requireView(), "Flash Light is Off", Snackbar.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Snackbar.make(requireView(), "Error accessing the flashlight", Snackbar.LENGTH_SHORT).show()
        }
    }

    // Función para vibrar
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
