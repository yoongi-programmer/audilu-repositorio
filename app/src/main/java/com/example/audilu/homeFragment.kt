package com.example.audilu

import PreferencesViewModel
import android.R
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.audilu.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import java.io.IOException
import java.util.UUID

class homeFragment : BaseTabFragment() {
    //inicializacion de vinculacion de vistas (binding)
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PreferencesViewModel by activityViewModels()
    //variables de bluetooth
    private val bluetoothPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            Log.d("homeFragment", "Bluetooth permissions granted")
            searchAndDisplayBluetoothDevices()
        } else {
            Log.d("homeFragment", "Bluetooth permissions denied")
            Snackbar.make(requireView(), "Bluetooth permissions denied", Snackbar.LENGTH_SHORT).show()
        }
    }
    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            searchAndDisplayBluetoothDevices()
        } else {
            Snackbar.make(requireView(), "Bluetooth not enabled", Snackbar.LENGTH_SHORT).show()
        }
    }
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null
    private lateinit var bluetoothManager: BluetoothManager
    private var isConnected: Boolean = false
    private lateinit var devicesBluetooth: MutableList<BluetoothDevice>//lista mutable de objetos tipo bt_device
    private lateinit var bluetoothDevice: BluetoothDevice
    //variables de datos a mostrar
    private var temperature  = 0f
    private var humidity = 0f
    private var movement = 0
    private var sound = 0
    private var gas = 0
    //variables de flash
    private lateinit var cameraM: CameraManager
    var isFlash = false
    @RequiresApi(Build.VERSION_CODES.M)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout para este fragmento con el método binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //funcion para la navegacion con tablayout
        setupTabLayout(binding.tablayout)
        getPreferences()
        cameraM = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        //SOLICITAR PERMISOS PARA UTILIZAR BLUETOOTH--------------------------------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {//Revisa version de android y solicita permisos de forma acorde
            Log.d("homeFragment", "Requesting permissions for android 12+")
            requestBluetoothPermissions()// Solicitar permisos de Bluetooth para Android 12+
        } else {
            Log.d("homeFragment", "Requesting permissions for android 11-")
            requestLegacyBluetoothPermissions()// Solicitar permisos de Bluetooth para Android 11 y anteriores
        }
        binding.btConnect.setOnClickListener{
            binding.spinnerBT.visibility= View.VISIBLE
            Log.d("homeFragment", "Searching for Bluetooth devices: ${binding.spinnerBT.visibility}")
        }
        //SELECCION DE DISPOSITIVO BT AL QUE CONECTARSE------------------------------------
        binding.spinnerBT.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (devicesBluetooth.isNotEmpty()) {
                    Log.d("homeFragment", "Selected device: ${devicesBluetooth[position].name}")
                    val device = devicesBluetooth[position]
                    connectToDevice(device)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                Snackbar.make(requireView(), "Seleccione un dispositivo", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.btDiconnect.setOnClickListener{
            binding.spinnerBT.visibility= View.GONE
            disconnectBluetooth()
        }

        return binding.root //devuelve la vista
    }

    //FUNCIONES-------------------------------------------------------------------
    private fun getPreferences() {
        Log.d("homeFragment", "Getting preferences. switchVibState: ${viewModel.switchVibState.value}")
        val switchVibState = viewModel.switchVibState.value ?: 0
        Log.d("homeFragment", "Getting preferences. switchFlashState: ${viewModel.switchFlashState.value}")
        val switchFlashState = viewModel.switchFlashState.value ?: 0
        Log.d("homeFragment", "Getting preferences. cbVibSonState: ${viewModel.cbVibSonState.value}")
        val cbVibSonState = viewModel.cbVibSonState.value ?: 0
        Log.d("homeFragment", "Getting preferences. cbVibMovState: ${viewModel.cbVibMovState.value}")
        val cbVibMovState = viewModel.cbVibMovState.value ?: 0
        Log.d("homeFragment", "Getting preferences. cbVibGasState: ${viewModel.cbVibGasState.value}")
        val cbVibGasState = viewModel.cbVibGasState.value ?: 0
        Log.d("homeFragment", "Getting preferences. cbLuzSonState: ${viewModel.cbLuzSonState.value}")
        val cbLuzSonState = viewModel.cbLuzSonState.value ?: 0
        Log.d("homeFragment", "Getting preferences. cbLuzMovState: ${viewModel.cbLuzMovState.value}")
        val cbLuzMovState = viewModel.cbLuzMovState.value ?: 0
        Log.d("homeFragment", "Getting preferences. cbLuzGasState: ${viewModel.cbLuzGasState.value}")
        val cbLuzGasState = viewModel.cbLuzGasState.value ?: 0

        // Usa estos valores para realizar las acciones necesarias
        updateUI(switchVibState, switchFlashState, cbVibSonState, cbVibMovState, cbVibGasState, cbLuzSonState, cbLuzMovState, cbLuzGasState)
    }

    private fun requestLegacyBluetoothPermissions() {//Funcion para solicita permisos bt a android 11 e inferiores
        val requiredPermissions = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
        Log.d("homeFragment", "Checking permissions")
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            bluetoothPermissionsLauncher.launch(missingPermissions.toTypedArray())
            Log.d("homeFragment", "Missing Permission. Launching")
        } else {
            Log.d("homeFragment", "All Bluetooth permissions already granted")
            searchAndDisplayBluetoothDevices()
        }
    }
    private fun requestBluetoothPermissions() {//Funcion para solicita permisos bt a android 12 y superiores
        val requiredPermissions = arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            Log.d("homeFragment", "Launching Bluetooth permissions launcher")
            bluetoothPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            Log.d("homeFragment", "All Bluetooth permissions already granted")
            searchAndDisplayBluetoothDevices()
        }
    }
    private fun searchAndDisplayBluetoothDevices() {//Funcion para buscar y mostrar dispositivos BT
        // Verificar si el Bluetooth está habilitado
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {// Verificar si el Bluetooth está habilitado
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))// Solicitar al usuario que habilite el Bluetooth
        } else {
            val pairedDevices: Set<BluetoothDevice> =
                bluetoothAdapter!!.bondedDevices// Obtener la lista de dispositivos Bluetooth emparejados

            if (!pairedDevices.isNullOrEmpty()) {
                devicesBluetooth = pairedDevices.toMutableList()// Guardar la lista de dispositivos Bluetooth

                // Convertir la lista de dispositivos en una lista de nombres para mostrar
                val deviceNames = devicesBluetooth.mapNotNull { it.name }.toMutableList()

                if (deviceNames.isNotEmpty()) {
                    // Crear y establecer el adaptador para la lista de nombres de dispositivos
                    val adapter = ArrayAdapter(requireContext(), R.layout.simple_expandable_list_item_1, deviceNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerBT.adapter = adapter
                }
            } else {
                // Mostrar mensaje si no hay dispositivos emparejados
                Snackbar.make(requireView(), "No hay dispositivos bluetooth emparejados", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    private fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                Log.d("homeFragment", "Creating a socket")
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket = socket

                Log.d("homeFragment", "Trying to connect")
                socket.connect()

                requireActivity().runOnUiThread {
                    Log.d("homeFragment", "Connected to device")
                    Snackbar.make(requireView(), "Se conectó al dispositivo correctamente", Snackbar.LENGTH_SHORT).show()
                    //initBluetooth()
                    startDataReceiving()
                }
            } catch (e: IOException) {
                Log.e("homeFragment", "Error connecting to device: ${e.message}")
                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Error al conectar al dispositivo", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("homeFragment", "General error: ${e.message}")
                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Error desconocido", Snackbar.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun startDataReceiving() {
        val socket = bluetoothSocket // Copiamos la referencia para evitar múltiples verificaciones de null
        if (socket != null) {
            val inputStream = socket.inputStream
            val buffer = ByteArray(1024)
            val stringBuilder = StringBuilder()
            var bytes: Int

            Thread {
                while (true) {
                    try {
                        Log.d("homeFragment", "Trying to receive data")
                        bytes = inputStream.read(buffer)
                        val data = String(buffer, 0, bytes)
                        stringBuilder.append(data)
                        Log.d("homeFragment", "Received data: $data")

                        // Procesar líneas completas
                        val lines = stringBuilder.split("\n")
                        for (i in 0 until lines.size - 1) {
                            val line = lines[i].trim()
                            if (line.isNotEmpty()) {
                                parseData(line)
                                // Obtener las preferencias y parámetros actuales
                                val switchVib = viewModel.switchVibState.value ?: 0
                                val switchFlash = viewModel.switchFlashState.value ?: 0
                                val cbVibSon = viewModel.cbVibSonState.value ?: 0
                                val cbVibMov = viewModel.cbVibMovState.value ?: 0
                                val cbVibGas = viewModel.cbVibGasState.value ?: 0
                                val cbLuzSon = viewModel.cbLuzSonState.value ?: 0
                                val cbLuzMov = viewModel.cbLuzMovState.value ?: 0
                                val cbLuzGas = viewModel.cbLuzGasState.value ?: 0

                                // Actualizar la interfaz de usuario en el hilo principal
                                requireActivity().runOnUiThread {
                                    updateUI(switchVib, switchFlash, cbVibSon, cbVibMov, cbVibGas, cbLuzSon, cbLuzMov, cbLuzGas)
                                }
                            }
                        }
                        // Mantener la última parte de datos no procesada
                        stringBuilder.clear()
                        stringBuilder.append(lines.last())
                    } catch (e: IOException) {
                        Log.e("homeFragment", "Error receiving data, breaking thread: $e")
                        break
                    }
                }
            }.start()
        } else {
            Log.e("homeFragment", "BluetoothSocket is null")
            Snackbar.make(requireView(), "Error: BluetoothSocket is null", Snackbar.LENGTH_SHORT).show()
        }
    }
    private fun parseData(data: String) {
        Log.d("homeFragment", "Parsing data")
        val parts = data.split("|")
        if (parts.size == 5) {
            try {
               // Parsear todos los datos a el formato correspondiente
                temperature = parts[0].toFloat()
                humidity = parts[1].toFloat()
                movement = parts[2].toInt()
                sound = parts[3].toInt()
                gas = parts[4].toInt()
            } catch (e: NumberFormatException) {
                Log.e("homeFragment", "Error parsing data: ${e.message}")
            }
        } else {
            Log.e("homeFragment", "Data format error: expected 5 parts, got ${parts.size}")
        }
    }
    private fun updateUI(switchVib: Int, switchFlash: Int, cbVibSon: Int, cbVibMov: Int, cbVibGas: Int, cbLuzSon: Int, cbLuzMov: Int, cbLuzGas: Int) {
        Log.d("homeFragment", "Updating the User Interface with values - Temperature: $temperature, Humidity: $humidity, Movement: $movement, Sound: $sound Gas: $gas")
        binding.tempVal.text = String.format("%.1f°C", temperature)
        binding.humVal.text = String.format("%.1f", humidity)

        // Comprobamos si la vibración está activada
        if (switchVib == 1) {
            // Controlar la vibración basada en el sonido
            if (cbVibSon == 1 && sound == 1) {
                vibrate(1000L)
                Log.d("homeFragment","Alerta: el bebe llora")
                binding.msjSon.text="Bebé llorando!"
                binding.msjSon.visibility= View.VISIBLE
                binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.crybaby)
            } else if (cbVibSon == 1 && sound == 2) {
                vibrate(1500L)
                Log.d("homeFragment","Alerta: el bebe llora mucho")
                binding.msjSon.text="Bebé llorando!"
                binding.msjSon.visibility= View.VISIBLE
                binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.crybaby)
            } else {
                Log.d("homeFragment","Quitar Alerta: el bebe no llora")
                binding.msjSon.text="Mensaje de alerta"
                binding.msjSon.visibility= View.INVISIBLE
                binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.happybaby)
            }

            // Controlar la vibración basada en el movimiento
            if (cbVibMov == 1 && movement == 1) {
                vibrate(1000L)
                Log.d("homeFragment","Alerta: el bebe se mueve")
                binding.msjMov.text="Bebé moviendose!"
                binding.msjMov.visibility= View.VISIBLE
                binding.imgBbMov.setImageResource(com.example.audilu.R.drawable.babymov)
            } else {
                Log.d("homeFragment","Quitar Alerta: el bebe no se mueve")
                binding.msjMov.text="Mensaje de alerta"
                binding.msjMov.visibility= View.INVISIBLE
                binding.imgBbMov.setImageResource(com.example.audilu.R.drawable.sleepbaby)
            }

            // Controlar la vibración basada en el gas
            if (cbVibGas == 1 && gas == 1) {
                vibrate(1000L)
                Log.d("homeFragment","Alerta: HAY GAS EN EL AIRE")
                binding.gasVal.text="DETECTADO"
            } else {
                Log.d("homeFragment","Quitar Alerta: NO HAY GAS EN EL AIRE")
                binding.gasVal.text="NO DETECTADO"
            }
        }
        // Comprobamos si el flash está activado
        if (switchFlash == 1) {
            // Controlar el flash basado en el sonido
            if (cbLuzSon == 1 && sound == 1) {
                flashLightOnOrOff(view, 1000L, 3)
                Log.d("homeFragment","Alerta: el bebe llora")
                binding.msjSon.text="Bebé llorando!"
                binding.msjSon.visibility= View.VISIBLE
                binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.crybaby)
            } else if (cbLuzSon == 1 && sound == 2) {
                flashLightOnOrOff(view, 1000L, 5)
                Log.d("homeFragment","Alerta: el bebe llora mucho")
                binding.msjSon.text="Bebé llorando!"
                binding.msjSon.visibility= View.VISIBLE
                binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.crybaby)
            } else {
                Log.d("homeFragment","Quitar Alerta: el bebe no llora")
                binding.msjSon.visibility= View.INVISIBLE
                binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.happybaby)
            }

            // Controlar el flash basado en el movimiento
            if (cbLuzMov == 1 && movement == 1) {
                flashLightOnOrOff(view, 1000L, 3)
                Log.d("homeFragment","Alerta: el bebe se mueve")
                binding.msjMov.text="Bebé moviendose!"
                binding.msjMov.visibility= View.VISIBLE
                binding.imgBbMov.setImageResource(com.example.audilu.R.drawable.babymov)
            } else {
                Log.d("homeFragment","Quitar Alerta: el bebe no se mueve")
                binding.msjMov.text="Mensaje de alerta"
                binding.msjMov.visibility= View.INVISIBLE
                binding.imgBbMov.setImageResource(com.example.audilu.R.drawable.sleepbaby)
            }

            // Controlar el flash basado en el gas
            if (cbLuzGas == 1 && gas == 1) {
                flashLightOnOrOff(view, 1000L, 3)
                Log.d("homeFragment","Alerta: HAY GAS EN EL AIRE")
                binding.gasVal.text="DETECTADO"
            } else {
                Log.d("homeFragment","Quitar Alerta: NO HAY GAS EN EL AIRE")
                binding.gasVal.text="NO DETECTADO"
            }
        }
    }
    fun vibrate(duration: Long = 1000L) {
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
    }//CODE function FOR VIBRATION
    @RequiresApi(Build.VERSION_CODES.M)
    private fun flashLightOnOrOff(v: View?, duration: Long, times: Int) {
        val cameraListId = cameraM.cameraIdList[0]
        val handler = Handler(Looper.getMainLooper())
        var count = 0

        val flashRunnable = object : Runnable {
            override fun run() {
                try {
                    if (count < times * 2) { // Times * 2 because we have ON and OFF states
                        isFlash = !isFlash
                        cameraM.setTorchMode(cameraListId, isFlash)
                        count++
                        handler.postDelayed(this, duration)
                    } else {
                        handler.removeCallbacks(this) // Stop after desired repetitions
                    }
                } catch (e: Exception) {
                    Snackbar.make(requireView(), "Error accessing the flashlight", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        handler.post(flashRunnable)
    }// Función para encender/apagar el flash
    private fun disconnectBluetooth() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            // Reestablecer valores a cero
            updateUIWithDefaultValues()
        } catch (e: IOException) { e.printStackTrace()
            Log.e("homeFragment", "Error desconectando", e)
        }
    }
    private fun updateUIWithDefaultValues() {
        // Suponiendo que tienes TextViews o variables para mostrar los valores
        binding.tempVal.text="0.0"
        binding.humVal.text ="0.0"
        binding.msjMov.visibility= View.INVISIBLE
        binding.msjSon.visibility= View.INVISIBLE
        binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.happybaby)
        binding.imgBbMov.setImageResource(com.example.audilu.R.drawable.sleepbaby)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        try {
            bluetoothSocket?.let {
                Log.d("homeFragment", "Closing socket")
                it.close()
            }
        } catch (e: IOException) {
            Log.e("homeFragment", "Error cerrando el bluetoothSocket", e)
        }
    }
    //FUNCIONES-------------------------------------------------------------------
}