package com.example.audilu

import PreferencesViewModel
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.example.audilu.databinding.FragmentPreferenciasBinding
import com.google.android.material.snackbar.Snackbar

class preferenciasFragment : BaseTabFragment() {
    private var _binding: FragmentPreferenciasBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PreferencesViewModel by activityViewModels()
    // Variables para guardar los estados originales
    private var originalSwitchVibState: Int = 1
    private var originalSwitchFlashState: Int =1
    private var originalCbVibSonState: Int = 1
    private var originalCbVibMovState: Int = 0
    private var originalCbVibGasState: Int = 0
    private var originalCbLuzSonState: Int = 1
    private var originalCbLuzMovState: Int = 0
    private var originalCbLuzGasState: Int = 0
    //--------------------------------------------------------------------------------------------
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("PreferenciasFragment", "Inflando layout para este fragmento")
        _binding = FragmentPreferenciasBinding.inflate(inflater, container, false)

        setupObservers()
        setupTabLayout(binding.tablayout)
        setupSwitches()
        setupCheckboxes()
        setupButtons()
        saveOriginalStates()
        return binding.root
    }

    //FUNCIONES---------------------------------------------------------------------------------
    private fun setupObservers() {
        // Observa los cambios en el ViewModel y actualiza la UI
        viewModel.switchVibState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en switchVibState: $state")
            binding.switchVib.isChecked = state == 1
        })

        viewModel.switchFlashState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en switchFlashState: $state")
            binding.switchLuz.isChecked = state == 1
        })

        viewModel.cbVibSonState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en cbVibSonState: $state")
            binding.cbSonido.isChecked = state == 1
        })

        viewModel.cbVibMovState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en cbVibMovState: $state")
            binding.cbMov.isChecked = state == 1
        })

        viewModel.cbVibGasState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en cbVibGasState: $state")
            binding.cbGas.isChecked = state == 1
        })

        viewModel.cbLuzSonState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en cbLuzSonState: $state")
            binding.cbxSonidoL.isChecked = state == 1
        })

        viewModel.cbLuzMovState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en cbLuzMovState: $state")
            binding.cbxMovL.isChecked = state == 1
        })

        viewModel.cbLuzGasState.observe(viewLifecycleOwner, { state: Int ->
            Log.d("PreferenciasFragment", "Observando cambios en cbLuzGasState: $state")
            binding.cbxGasL.isChecked = state == 1
        })
    }
    private fun setupSwitches() {
        binding.switchVib.setOnCheckedChangeListener { _, isChecked ->
            handleVibrationSwitch(isChecked)
            Log.d("PreferenciasFragment", "Switch de Vibracion cambiado a $isChecked")
            viewModel.setSwitchVibState(if (isChecked) 1 else 0)
            checkForChanges()
        }

        binding.switchLuz.setOnCheckedChangeListener { _, isChecked ->
            handleFlashSwitch(isChecked)
            Log.d("PreferenciasFragment", "Switch de Flash cambiado a $isChecked")
            viewModel.setSwitchFlashState(if (isChecked) 1 else 0)
            checkForChanges()
        }
    }

    private fun handleVibrationSwitch(isChecked: Boolean) {
        if (isChecked) {
            Snackbar.make(requireView(), "Alertas vibratorias activadas", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(requireView(), "Alertas vibratorias desactivadas", Snackbar.LENGTH_SHORT).show()
        }
        binding.cbSonido.isEnabled = isChecked
        binding.cbMov.isEnabled = isChecked
        binding.cbGas.isEnabled = isChecked
    }

    private fun handleFlashSwitch(isChecked: Boolean) {
        if (isChecked) {
            Snackbar.make(binding.root, "Alertas de Flash activadas", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, "Alertas de Flash desactivadas", Snackbar.LENGTH_SHORT).show()
        }
        binding.cbxSonidoL.isEnabled = isChecked
        binding.cbxMovL.isEnabled = isChecked
        binding.cbxGasL.isEnabled = isChecked
    }

    private fun setupCheckboxes() {
        binding.cbSonido.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PreferenciasFragment", "Checkbox de Sonido cambiado")
            viewModel.setCbVibSonState(if (isChecked) 1 else 0)
            handleVibrationCheckboxes()
            checkForChanges()
        }
        binding.cbMov.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PreferenciasFragment", "Checkbox de Movimiento cambiado")
            viewModel.setCbVibMovState(if (isChecked) 1 else 0)
            handleVibrationCheckboxes()
            checkForChanges()
        }
        binding.cbGas.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PreferenciasFragment", "Checkbox de Gas cambiado")
            viewModel.setCbVibGasState(if (isChecked) 1 else 0)
            handleVibrationCheckboxes()
            checkForChanges()
        }

        binding.cbxSonidoL.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PreferenciasFragment", "Checkbox de SonidoL cambiado")
            viewModel.setCbLuzSonState(if (isChecked) 1 else 0)
            handleFlashCheckboxes()
            checkForChanges()
        }
        binding.cbxMovL.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PreferenciasFragment", "Checkbox de MovimientoL cambiado")
            viewModel.setCbLuzMovState(if (isChecked) 1 else 0)
            handleFlashCheckboxes()
            checkForChanges()
        }
        binding.cbxGasL.setOnCheckedChangeListener { _, isChecked ->
            Log.d("PreferenciasFragment", "Checkbox de GasL cambiado")
            viewModel.setCbLuzGasState(if (isChecked) 1 else 0)
            handleFlashCheckboxes()
            checkForChanges()
        }
    }

    private fun handleVibrationCheckboxes() {
        val root = binding.root
        val cbSonV = binding.cbSonido
        val cbMovV = binding.cbMov
        val cbGasV = binding.cbGas

        when {
            cbSonV.isChecked && cbMovV.isChecked && cbGasV.isChecked -> {
                Log.d("PreferenciasFragment", "Todas opciones activadas")
                Snackbar.make(root, "Todas las opciones activadas para VIBRACION", Snackbar.LENGTH_SHORT).show()
            }
            cbSonV.isChecked -> {
                Log.d("PreferenciasFragment", "Opcion SONIDO activada")
                Snackbar.make(root, "Opcion de Sonido activada para VIBRACION", Snackbar.LENGTH_SHORT).show()
            }
            cbMovV.isChecked -> {
                Log.d("PreferenciasFragment", "Opcion MOV activada")
                Snackbar.make(root, "Opcion de Movimiento activada para VIBRACION", Snackbar.LENGTH_SHORT).show()
            }
            cbGasV.isChecked -> {
                Log.d("PreferenciasFragment", "Opcion GAS activadas")
                Snackbar.make(root, "Opcion de Gas activada para VIBRACION", Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                Snackbar.make(root, "Ninguna opción activada para VIBRACION", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleFlashCheckboxes() {
        val root = binding.root
        val cbSonL = binding.cbxSonidoL
        val cbMovL = binding.cbxMovL
        val cbGasL = binding.cbxGasL

        when {
            cbSonL.isChecked && cbMovL.isChecked && cbGasL.isChecked -> {
                Snackbar.make(root, "Todas las opciones fueron activadas para FLASH", Snackbar.LENGTH_SHORT).show()
            }
            cbSonL.isChecked -> {
                Snackbar.make(root, "Opcion de Sonido activada para FLASH", Snackbar.LENGTH_SHORT).show()
            }
            cbMovL.isChecked -> {
                Snackbar.make(root, "Opcion de Movimiento activada para FLASH", Snackbar.LENGTH_SHORT).show()
            }
            cbGasL.isChecked -> {
                Snackbar.make(root, "Opcion de Gas activada para FLASH", Snackbar.LENGTH_SHORT).show()
            }
            else -> {
                Snackbar.make(root, "Ninguna opción activada para FLASH", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveOriginalStates() {
        originalSwitchVibState = viewModel.switchVibState.value ?: 0
        originalSwitchFlashState = viewModel.switchFlashState.value ?: 0
        originalCbVibSonState = viewModel.cbVibSonState.value ?: 0
        originalCbVibMovState = viewModel.cbVibMovState.value ?: 0
        originalCbVibGasState = viewModel.cbVibGasState.value ?: 0
        originalCbLuzSonState = viewModel.cbLuzSonState.value ?: 0
        originalCbLuzMovState = viewModel.cbLuzMovState.value ?: 0
        originalCbLuzGasState = viewModel.cbLuzGasState.value ?: 0
    }
    private fun checkForChanges() {
        // Compara el estado actual con el del ViewModel para mostrar u ocultar los botones
        if (binding.switchVib.isChecked != (originalSwitchVibState == 1) ||
            binding.switchLuz.isChecked != (originalSwitchFlashState == 1) ||
            binding.cbSonido.isChecked != (originalCbVibSonState == 1) ||
            binding.cbMov.isChecked != (originalCbVibMovState == 1) ||
            binding.cbGas.isChecked != (originalCbVibGasState == 1) ||
            binding.cbxSonidoL.isChecked != (originalCbLuzSonState == 1) ||
            binding.cbxMovL.isChecked != (originalCbLuzMovState == 1) ||
            binding.cbxGasL.isChecked != (originalCbLuzGasState == 1)
        ) {
            Log.d("PreferenciasFragment", "Showing buttons")
            binding.btAceptar.visibility = View.VISIBLE
            binding.btCancelar.visibility = View.VISIBLE
        } else {
            Log.d("PreferenciasFragment", "Hiding buttons")
            binding.btAceptar.visibility = View.GONE
            binding.btCancelar.visibility = View.GONE
        }
    }
    private fun setupButtons() {
        binding.btAceptar.setOnClickListener {
            // Lógica para aceptar cambios
            Snackbar.make(requireView(), "Cambios aceptados", Snackbar.LENGTH_SHORT).show()
            resetActualStates()
        }

        binding.btCancelar.setOnClickListener {
            // Lógica para cancelar cambios
            Snackbar.make(requireView(), "Cambios cancelados", Snackbar.LENGTH_SHORT).show()
            restoreInitialStates()
        }
    }

    private fun resetActualStates() { // guardar cambios
        // Actualiza el estado inicial de las variables al aceptar cambios
        Log.d("preferenciasFragment", "Resetting switch states")
        viewModel.setSwitchVibState(if (binding.switchVib.isChecked) 1 else 0)
        viewModel.setSwitchFlashState(if (binding.switchLuz.isChecked) 1 else 0)
        viewModel.setCbVibSonState(if (binding.cbSonido.isChecked) 1 else 0)
        viewModel.setCbVibMovState(if (binding.cbMov.isChecked) 1 else 0)
        viewModel.setCbVibGasState(if (binding.cbGas.isChecked) 1 else 0)
        viewModel.setCbLuzSonState(if (binding.cbxSonidoL.isChecked) 1 else 0)
        viewModel.setCbLuzMovState(if (binding.cbxMovL.isChecked) 1 else 0)
        viewModel.setCbLuzGasState(if (binding.cbxGasL.isChecked) 1 else 0)

        saveOriginalStates() // Actualiza los estados originales
        Log.d("preferenciasFragment", "Calling function checkForChanges()")
        checkForChanges() // Oculta los botones
    }

    private fun restoreInitialStates() { //volver a la conguracion inicial
        // Restaura el estado inicial de los switches al cancelar cambios
        Log.d("preferenciasFragment", "Restoring previous switch states")
        // Restablecer los estados desde el ViewModel
        binding.switchVib.isChecked = originalSwitchVibState == 1
        binding.switchLuz.isChecked = originalSwitchFlashState == 1
        binding.cbSonido.isChecked = originalCbVibSonState == 1
        binding.cbMov.isChecked = originalCbVibMovState == 1
        binding.cbGas.isChecked = originalCbVibGasState == 1
        binding.cbxSonidoL.isChecked = originalCbLuzSonState == 1
        binding.cbxMovL.isChecked = originalCbLuzMovState == 1
        binding.cbxGasL.isChecked = originalCbLuzGasState == 1

        Log.d("preferenciasFragment", "Calling function checkForChanges()")
        checkForChanges() // Oculta los botones
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
