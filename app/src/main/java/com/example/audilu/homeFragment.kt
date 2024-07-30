package com.example.audilu

import android.R
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
    @RequiresApi(Build.VERSION_CODES.M)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Infla el layout para este fragmento con el método binding
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        //funcion para la navegacion con tablayout
        setupTabLayout(binding.tablayout)

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
        }
        //SELECCION DE DISPOSITIVO BT AL QUE CONECTARSE------------------------------------
        binding.spinnerBT.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (devicesBluetooth.isNotEmpty()) {
                    Log.d("FirstFragment", "Selected device: ${devicesBluetooth[position].name}")
                    val device = devicesBluetooth[position]
                    connectToDevice(device)
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.btDiconnect.setOnClickListener{
            binding.spinnerBT.visibility= View.GONE
            disconnectBluetooth()
        }

        return binding.root //devuelve la vista
    }

    //FUNCIONES-------------------------------------------------------------------

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

            if (pairedDevices.isNotEmpty()) {
                devicesBluetooth =
                    pairedDevices.toMutableList()// Guardar la lista de dispositivos Bluetooth

                // Convertir la lista de dispositivos en una lista de nombres para mostrar
                val deviceNames = devicesBluetooth.map { it.name }.toMutableList()

                // Crear y establecer el adaptador para la lista de nombres de dispositivos
                val adapter = ArrayAdapter(requireContext(), R.layout.simple_expandable_list_item_1, deviceNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerBT.adapter = adapter
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
                    Snackbar.make(requireView(), "Connected to device", Snackbar.LENGTH_SHORT).show()
                    //initBluetooth()
                    startDataReceiving()
                }
            } catch (e: IOException) {
                Log.e("homeFragment", "Error connecting to device: ${e.message}")
                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Error connecting to device", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("homeFragment", "General error: ${e.message}")
                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Unknown error occurred", Snackbar.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun initBluetooth() {
        bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter//inicializar adaptador
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {//comprobar que el adaptador no sea nulo
            Log.e("homeFragment", "Bluetooth not enabled or adapter is null")
            requireActivity().runOnUiThread {
                Snackbar.make(requireView(), "Bluetooth no está habilitado o no hay adaptador", Snackbar.LENGTH_SHORT).show()
            }
            return
        }
        bluetoothDevice = bluetoothAdapter!!.getRemoteDevice("98:D3:36:81:02:77")//inicializad dispositivo BT con el adaptador

        try {
            Log.d("homeFragment", "Create a socket")
            if (bluetoothSocket == null || !isConnected) {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
                Log.d("FirstFragment", "Try to connect")
                bluetoothSocket?.connect()
                isConnected = true
            }
        } catch (e: IOException) {
            Log.e("homeFragment", "Error connecting Bluetooth: $e")
            requireActivity().runOnUiThread {
                Snackbar.make(requireView(), "Error al conectarse al dispositivo", Snackbar.LENGTH_SHORT).show()
            }
            isConnected = false
        } catch (e: Exception) {
            Log.e("homeFragment", "General error: $e")
            requireActivity().runOnUiThread {
                Snackbar.make(requireView(), "Error desconocido.", Snackbar.LENGTH_SHORT).show()
            }
        }
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
                                requireActivity().runOnUiThread { updateUI() }
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
    private fun updateUI() {
        Log.d("homeFragment", "Updating the User Interface with values - Temperature: $temperature, Humidity: $humidity, Movement: $movement, Sound: $sound Gas: $gas")
        binding.tempVal.text = String.format("%.1f°C", temperature)
        binding.humVal.text = String.format("%.1f", humidity)

        if(sound==1){
            vibrate(1500L)
            Log.d("homeFragment","Alerta: el bebe llora")
            binding.msjAlerta.text="Bebé llorando!"
            binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.crybaby)
        }
        if(sound==2){
            Log.d("homeFragment","Alerta: el bebe llora mucho")
            vibrate(1000L)
            binding.imgBbSon.setImageResource(com.example.audilu.R.drawable.crybaby)
        }
        if(movement==1){
            Log.d("homeFragment","Alerta: el bebe se mueve")
            vibrate(1500L)
            binding.msjAlerta.text="Bebé moviendose!"
            binding.imgBbMov.setImageResource(com.example.audilu.R.drawable.babymov)
        }
        if(gas==1){
            Log.d("homeFragment","Alerta: HAY GAS EN EL AIRE")
            binding.gasVal.text="GAS DETECTADO"
        }
    }
    //CODE function FOR VIBRATION
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
    }
    private fun disconnectBluetooth() {
        try {
            bluetoothSocket?.close()
            bluetoothSocket = null
            // Aquí restableces los valores a 0
            updateUIWithDefaultValues()
        } catch (e: IOException) { e.printStackTrace()
            Log.e("homeFragment", "Error desconectando", e)
        }
    }
    private fun updateUIWithDefaultValues() {
        // Suponiendo que tienes TextViews o variables para mostrar los valores
        binding.tempVal.text="0.0"
        binding.humVal.text ="0.0"
        binding.msjAlerta.visibility= View.INVISIBLE
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