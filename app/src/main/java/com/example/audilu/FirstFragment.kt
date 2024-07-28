package com.example.audilu

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
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.audilu.databinding.FragmentFirstBinding
import com.google.android.material.snackbar.Snackbar
import com.ingenieriajhr.blujhr.BluJhr
import java.io.IOException
import java.util.UUID
import android.os.VibrationEffect
import android.os.Vibrator

class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private val bluetoothPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            Log.d("FirstFragment", "Bluetooth permissions granted")
            searchAndDisplayBluetoothDevices()
        } else {
            Log.d("FirstFragment", "Bluetooth permissions denied")
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
    private lateinit var devicesBluetooth: MutableList<BluetoothDevice>
    private lateinit var bluetoothDevice: BluetoothDevice
    private var temperature  = 0f
    private var humidity = 0f
    private var movement = 0
    private var sound = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        //bluetooth = BluJhr(requireContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {//CONTROLA LA VERSION DE ANDROIRD
            Log.d("FirstFragment", "Requesting permissions for android 12+")
            requestBluetoothPermissions()// Solicitar permisos de Bluetooth para Android 12+
        } else {
            Log.d("FirstFragment", "Requesting permissions for android 11-")
            requestLegacyBluetoothPermissions()// Solicitar permisos de Bluetooth para Android 11 y anteriores
        }

            binding.listDeviceBluetooth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            binding.btnDisconnect.setOnClickListener {
                disconnectBluetooth()
            }

        return binding.root
    }

    private fun requestBluetoothPermissions() {
        val requiredPermissions = arrayOf(
            android.Manifest.permission.BLUETOOTH_CONNECT,
            android.Manifest.permission.BLUETOOTH_SCAN
        )
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            Log.d("FirstFragment", "Launching Bluetooth permissions launcher")
            bluetoothPermissionsLauncher.launch(missingPermissions.toTypedArray())
        } else {
            Log.d("FirstFragment", "All Bluetooth permissions already granted")
            searchAndDisplayBluetoothDevices()
        }
    }

    private fun requestLegacyBluetoothPermissions() {
        val requiredPermissions = arrayOf(
            android.Manifest.permission.BLUETOOTH,
            android.Manifest.permission.BLUETOOTH_ADMIN
        )
        Log.d("FirstFragment", "Checking permissions")
        val missingPermissions = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }
        if (missingPermissions.isNotEmpty()) {
            bluetoothPermissionsLauncher.launch(missingPermissions.toTypedArray())
            Log.d("FirstFragment", "Missing Permission. Launching")
        } else {
            Log.d("FirstFragment", "All Bluetooth permissions already granted")
            searchAndDisplayBluetoothDevices()
        }
    }

    private fun searchAndDisplayBluetoothDevices() {
        // Verificar si el Bluetooth está habilitado
        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            // Solicitar al usuario que habilite el Bluetooth
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            // Obtener la lista de dispositivos Bluetooth emparejados
            val pairedDevices: Set<BluetoothDevice> = bluetoothAdapter!!.bondedDevices
            if (pairedDevices.isNotEmpty()) {
                // Guardar la lista de dispositivos Bluetooth
                devicesBluetooth = pairedDevices.toMutableList()

                // Convertir la lista de dispositivos en una lista de nombres para mostrar
                val deviceNames = devicesBluetooth.map { it.name }.toMutableList()

                // Crear y establecer el adaptador para la lista de nombres de dispositivos
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_expandable_list_item_1, deviceNames)
                binding.listDeviceBluetooth.adapter = adapter
            } else {
                // Mostrar mensaje si no hay dispositivos emparejados
                Snackbar.make(requireView(), "No paired Bluetooth devices", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
    //
    private fun connectToDevice(device: BluetoothDevice) {
        Thread {
            try {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                Log.d("FirstFragment", "Creating a socket")
                val socket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket = socket

                Log.d("FirstFragment", "Trying to connect")
                socket.connect()

                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Connected to device", Snackbar.LENGTH_SHORT).show()
                    //initBluetooth()
                    startDataReceiving()
                }
            } catch (e: IOException) {
                Log.e("FirstFragment", "Error connecting to device: ${e.message}")
                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Error connecting to device", Snackbar.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("FirstFragment", "General error: ${e.message}")
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
            Log.e("FirstFragment", "Bluetooth not enabled or adapter is null")
            requireActivity().runOnUiThread {
                Snackbar.make(requireView(), "Bluetooth no está habilitado o no hay adaptador", Snackbar.LENGTH_SHORT).show()
            }
            return
        }
        bluetoothDevice = bluetoothAdapter!!.getRemoteDevice("98:D3:36:81:02:77")//inicializad dispositivo BT con el adaptador

        try {
            Log.d("FirstFragment", "Create a socket")
            if (bluetoothSocket == null || !isConnected) {
                val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
                bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
                Log.d("FirstFragment", "Try to connect")
                bluetoothSocket?.connect()
                isConnected = true
            }
        } catch (e: IOException) {
            Log.e("FirstFragment", "Error connecting Bluetooth: $e")
            requireActivity().runOnUiThread {
                Snackbar.make(requireView(), "Error al conectarse al dispositivo", Snackbar.LENGTH_SHORT).show()
            }
            isConnected = false
        } catch (e: Exception) {
            Log.e("FirstFragment", "General error: $e")
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
                        Log.d("FirstFragment", "Trying to receive data")
                        bytes = inputStream.read(buffer)
                        val data = String(buffer, 0, bytes)
                        stringBuilder.append(data)
                        Log.d("FirstFragment", "Received data: $data")

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
                        Log.e("FirstFragment", "Error receiving data, breaking thread: $e")
                        break
                    }
                }
            }.start()
        } else {
            Log.e("FirstFragment", "BluetoothSocket is null")
            Snackbar.make(requireView(), "Error: BluetoothSocket is null", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun parseData(data: String) {
        Log.d("FirstFragment", "Parsing data")
        val parts = data.split("|")
        if (parts.size == 4) {
            try {
                // Parse the temperature and humidity as float values
                temperature = parts[0].toFloat()
                humidity = parts[1].toFloat()
                movement = parts[2].toInt()
                sound = parts[3].toInt()
            } catch (e: NumberFormatException) {
                Log.e("FirstFragment", "Error parsing data: ${e.message}")
            }
        } else {
            Log.e("FirstFragment", "Data format error: expected 4 parts, got ${parts.size}")
        }
    }
    private fun updateUI() {
        Log.d("FirstFragment", "Updating the User Interface with values - Temperature: $temperature, Humidity: $humidity, Movement: $movement, Sound: $sound")
        binding.valTemp.text = String.format("%.1f °C", temperature)
        binding.valHum.text = String.format("%.1f %%", humidity)
        binding.valMov.text = "$movement"
        binding.valSonido.text = "$sound"

        if(sound==1){
            vibrate(1500L)
        }
        if(sound==2){
            vibrate(1000L)
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
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun updateUIWithDefaultValues() {
        // Suponiendo que tienes TextViews o variables para mostrar los valores
        binding.valTemp.text="0.0"
        binding.valHum.text ="0.0"
        binding.valSonido.text= "0"
        binding.valMov.text= "0"
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        try {
            bluetoothSocket?.let {
                Log.d("FirstFragment", "Closing socket")
                it.close()
            }
        } catch (e: IOException) {
            Log.e("FirstFragment", "Error cerrando el bluetoothSocket", e)
        }
    }
}