package com.example.audilu
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.audilu.databinding.FragmentPreferenciasBinding
import com.google.android.material.snackbar.Snackbar

class preferenciasFragment : Fragment() {
    //inicializacion de vinculacion de vistas (binding)
    private var _binding: FragmentPreferenciasBinding? = null
    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout para este fragmento con el método binding
        Log.d("preferenciasFragment","Inflando layout para este fragmento")
        _binding = FragmentPreferenciasBinding.inflate(inflater, container, false)
        val root = binding.root
        //Declaracion de variables
        val switchVib = binding.switchVib
        val switchLuz = binding.switchLuz
        val cbSonL = binding.cbxSonidoL
        val cbMovL = binding.cbxMovL
        val cbGasL = binding.cbxGasL
        val cbSonV = binding.cbSonido
        val cbMovV = binding.cbMov
        val cbGasV = binding.cbGas
        //SWITCH PARA VIBRACION-------------------------------------
        switchVib.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Snackbar.make(requireView(), "Alertas vibratorias activadas", Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(requireView(),"Alertas vibratorias desactivadas", Snackbar.LENGTH_SHORT).show()
            }

            // Habilitar o deshabilitar los CheckBox según el estado del Switch
            binding.cbSonido.isEnabled = isChecked
            binding.cbMov.isEnabled = isChecked
            binding.cbGas.isEnabled = isChecked

        }
        if(cbSonV.isChecked && cbMovV.isChecked && cbGasV.isChecked){
            Log.d("preferenciasFragment","Todas opciones activadas")
            Snackbar.make(root,"Todas las opciones activadas para VIBRACION",Snackbar.LENGTH_SHORT).show()
        }else if (cbSonV.isChecked) {
            Log.d("preferenciasFragment","Opcion SONIDO activada")
            Snackbar.make(root,"Opcion de Sonido activada para VIBRACION",Snackbar.LENGTH_SHORT).show()
        }else if (cbMovV.isChecked){
            Log.d("preferenciasFragment","Opcion MOV activada")
            Snackbar.make(root,"Opcion de Movimiento activada para VIBRACION",Snackbar.LENGTH_SHORT).show()
        }else if (cbGasV.isChecked){
            Log.d("preferenciasFragment","Opcion GAS activadas")
            Snackbar.make(root,"Opcion de Gas activada para VIBRACION",Snackbar.LENGTH_SHORT).show()
        }
        //SWITCH PARA FLASH/LUCES-------------------------------------
        switchLuz.setOnCheckedChangeListener{ _, isChecked ->
            if(isChecked){
                Snackbar.make(root,"Alertas de Flash activadas", Snackbar.LENGTH_SHORT).show()
            }else{
                Snackbar.make(root,"Alertas de Flash desactivadas", Snackbar.LENGTH_SHORT).show()
            }
            // Habilitar o deshabilitar los CheckBox según el estado del Switch
            binding.cbxSonidoL.isEnabled = isChecked
            binding.cbxMovL.isEnabled = isChecked
            binding.cbxGasL.isEnabled = isChecked
        }
        if(cbSonL.isChecked && cbMovL.isChecked && cbGasL.isChecked){
            Snackbar.make(root,"Todas las opciones fueron activadas para FLASH",Snackbar.LENGTH_SHORT).show()
        }else if (cbSonL.isChecked) {
            Snackbar.make(root,"Opcion de Sonido activada para FLASH",Snackbar.LENGTH_SHORT).show()
        }else if (cbMovL.isChecked){
            Snackbar.make(root,"Opcion de Movimiento activada para FLASH",Snackbar.LENGTH_SHORT).show()
        }else if (cbGasL.isChecked){
            Snackbar.make(root,"Opcion de Gas activada para FLASH",Snackbar.LENGTH_SHORT).show()
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}

