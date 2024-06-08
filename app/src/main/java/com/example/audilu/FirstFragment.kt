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

class FirstFragment : Fragment() {
    private lateinit var bluetooth: BluJhr
    private var devicesBluetooth = ArrayList<String>()
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
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothManager: BluetoothManager
    private var temperature = 0
    private var humidity = 0
    private var movement = 0
    private var sound = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        bluetooth = BluJhr(requireContext())

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
                    bluetooth.connect(devicesBluetooth[position])
                    bluetooth.setDataLoadFinishedListener(object : BluJhr.ConnectedBluetooth {
                        override fun onConnectState(state: BluJhr.Connected) {
                            when (state) {
                                BluJhr.Connected.True -> {
                                    Log.d("FirstFragment", "STATE: CONNECTED")
                                    Snackbar.make(requireView(), "True", Snackbar.LENGTH_SHORT).show()
                                    Log.d("FirstFragment","Inicializando Bluetooth")
                                    initBluetooth()
                                    Log.d("FirstFragment","Function StartDataReciving")
                                    startDataReceiving()
                                }
                                BluJhr.Connected.Pending -> {
                                    Log.d("FirstFragment","STATE: PENDING")
                                    Snackbar.make(requireView(), "Pending", Snackbar.LENGTH_SHORT).show()
                                }
                                BluJhr.Connected.False -> {
                                    Log.d("FirstFragment", "STATE: FALSE")
                                    Snackbar.make(requireView(), "False", Snackbar.LENGTH_SHORT).show()
                                }
                                BluJhr.Connected.Disconnect -> {
                                    Log.d("FirstFragment", "STATE: DISCONNECT")
                                    Snackbar.make(requireView(), "Disconnect", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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
        if (!bluetooth.stateBluetoooth()) {
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
        } else {
            devicesBluetooth = bluetooth.deviceBluetooth()
            if (devicesBluetooth.isNotEmpty()) {
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_expandable_list_item_1, devicesBluetooth)
                binding.listDeviceBluetooth.adapter = adapter
            } else {
                Snackbar.make(requireView(), "No paired Bluetooth devices", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun initBluetooth() {
        bluetoothManager = requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothDevice = bluetoothAdapter.getRemoteDevice("98:D3:36:81:02:77")

        try {
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            Log.d("FirstFragment", "Creat a socket")
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            Log.d("FirstFragment", "Try to connect")
            bluetoothSocket.connect()
        } catch (e: IOException) {
            Log.e("FirstFragment", "Error connecting Bluetooth: $e")
            Snackbar.make(requireView(), "Error al conectarse al dispositivo", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("FirstFragment", "General error: $e")
            Snackbar.make(requireView(), "Error desconocido.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startDataReceiving() {
        val inputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(1024)
        var bytes: Int

        Thread {
            while (true) {
                try {
                    Log.d("FirstFragment", "Try the receipt of data")
                    bytes = inputStream.read(buffer)
                    val data = String(buffer, 0, bytes)
                    parseData(data)
                    requireActivity().runOnUiThread { updateUI() }
                } catch (e: IOException) {
                    break
                }
            }
        }.start()
    }

    private fun parseData(data: String) {
        Log.d("FirstFragment", "Parsing data")
        val parts = data.split("|")
        if (parts.size == 4) {
            try {
                temperature = parts[0].toInt()
                humidity = parts[1].toInt()
                movement = parts[2].toInt()
                sound = parts[3].toInt()
            } catch (e: NumberFormatException) {
                Log.e("FirstFragment", "Error parsing data: $e")
            }
        }
    }

    private fun updateUI() {
        Log.d("FirstFragment", "Updating the User Interface ")
        binding.valTemp.text = "$temperature °C"
        binding.valHum.text = "$humidity %"
        binding.valMov.text = if (movement == 1) "Movimiento" else "Quieto"
        binding.valSonido.text = if (sound == 1) "Llanto" else "Silencio"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        try {
            if (::bluetoothSocket.isInitialized) {
                Log.d("FirstFragment", "Closing socket")
                // Tu código para manejar la desconexión o limpieza del socket
                bluetoothSocket.close()
            }
        } catch (e: UninitializedPropertyAccessException) {
            Log.e("FirstFragment", "bluetoothSocket no ha sido inicializado", e)
        }

    }
}