package com.example.audilu
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
    /** CREACION DE VARIABLES----------------------------------------------------------**/
    private lateinit var bluetooth: BluJhr//variable para bluetooth
    private var devicesBluetooth = ArrayList<String>()//lista de dispositivos
    private var _binding: FragmentFirstBinding? = null//inicializo el binding
    private val binding get() = _binding!!
    private val bluetoothPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->//CREAR VARIABLE PERMISO
        if (isGranted) {    //pedir permiso para activar bluetooth
            searchAndDisplayBluetoothDevices()//llamar funcion para activa bluetooth
        } else {
            Snackbar.make(requireView(), "Bluetooth permission denied", Snackbar.LENGTH_SHORT).show()//mensaje de error
        }
    }
    private val enableBluetoothLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->//ACTIVAR BLUETOOTH
        if (result.resultCode == Activity.RESULT_OK) {
            searchAndDisplayBluetoothDevices()//llamar funcion para activar bt
        } else {
            Snackbar.make(requireView(), "Bluetooth not enabled", Snackbar.LENGTH_SHORT).show()
        }
    }
    //variables para el bluetooth
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var bluetoothManager: BluetoothManager
    private var temperature = 0
    private var humidity = 0
    private var movement = 0
    private var sound = 0

    /** VISTA--------------------------------------------------------------------------**/
    override fun onCreateView(//crea vista
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater, container, false)//inicializo el binding
        bluetooth = BluJhr(requireContext())//inicializo la variable bluetooth
        requestBluetoothPermission()//llamo funcion para pedir permiso bt

        binding.listDeviceBluetooth.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {//seleccionar dispositivo
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {//vista, posicion, id
                if (devicesBluetooth.isNotEmpty()) {//si hay dispositivos
                    bluetooth.connect(devicesBluetooth[position])//conectar dispositivo en la posicion seleccionada
                    bluetooth.setDataLoadFinishedListener(object : BluJhr.ConnectedBluetooth {//conectar bluetooth
                        override fun onConnectState(state: BluJhr.Connected) {//estado de conexion
                            when (state) {
                                BluJhr.Connected.True -> {//si esta conectado
                                    Snackbar.make(requireView(), "True", Snackbar.LENGTH_SHORT).show()
                                    //inicializa y configura el bluetooth
                                    initBluetooth()
                                    //inicia la recepcion de datos
                                    startDataReceiving()
                                }
                                BluJhr.Connected.Pending -> {//si esta pendiente
                                    Snackbar.make(requireView(), "Pending", Snackbar.LENGTH_SHORT).show()
                                }
                                BluJhr.Connected.False -> {//si no esta conectado
                                    Snackbar.make(requireView(), "False", Snackbar.LENGTH_SHORT).show()
                                }
                                BluJhr.Connected.Disconnect -> {//si se desconecta
                                    Snackbar.make(requireView(), "Disconnect", Snackbar.LENGTH_SHORT).show()
                                }
                            }
                        }
                    })
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {//no hay dispositivos
                // Implementa la lógica cuando no se selecciona nada
            }
        }

        return binding.root//retorno vista
    }

    /** FUNCIONES----------------------------------------------------------------------**/
    //PERMISOS
    private fun requestBluetoothPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {//si no tiene permiso
            bluetoothPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH)//pedir permiso
        } else {//si tiene permiso
            searchAndDisplayBluetoothDevices()//llamar funcion para activar bluetooth
        }
    }
    private fun searchAndDisplayBluetoothDevices() {//busca y muestra dispositivos bt
        if (!bluetooth.stateBluetoooth()) {//si no esta activado
            enableBluetoothLauncher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))//activar bluetooth
        } else {//si esta activado
            devicesBluetooth = bluetooth.deviceBluetooth()//llamar funcion para activar bt
            if (devicesBluetooth.isNotEmpty()) {//si hay dispositivos
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_expandable_list_item_1, devicesBluetooth)//crear adaptador(contexto,vista,objeto)
                binding.listDeviceBluetooth.adapter = adapter//mostrar lista de dispositivos
            } else {
                Snackbar.make(requireView(), "No paired Bluetooth devices", Snackbar.LENGTH_SHORT).show()//mensaje de error
            }
        }
    }
    //RECEPCION DE DATOS
    private fun initBluetooth() {//inicializar bluetooth
        bluetoothManager =
            requireContext().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothDevice = bluetoothAdapter.getRemoteDevice("98:D3:36:81:02:77")

        try {
            // Crear y conectar el socket Bluetooth
            val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket.connect()
        } catch (e: IOException) {
            // Manejar la excepción de IOException
            Log.e("FirstFragment", "Error connecting Bluetooth: $e")
            // Aquí puedes mostrar un mensaje de error al usuario o tomar otras acciones
            Snackbar.make(requireView(), "Error al conectarse al dispositivo", Snackbar.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Manejar otras excepciones genéricas
            Log.e("FirstFragment", "General error: $e")
            // Aquí puedes manejar otras excepciones que puedan ocurrir
            Snackbar.make(requireView(), "Error desconocido.", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun startDataReceiving() {//recibir datos
        val inputStream = bluetoothSocket.inputStream
        val buffer = ByteArray(1024)//buffer
        var bytes: Int

        Thread {
            while (true) {
                try {
                    bytes = inputStream.read(buffer)
                    val data = String(buffer, 0, bytes)
                    parseData(data)
                    updateUI()
                } catch (e: IOException) {
                    break
                }
            }
        }.start()
    }

    private fun parseData(data: String) {
        val parts = data.split("|")
        if (parts.size == 4) {
            try {
                temperature = parts[0].toInt()
                humidity = parts[1].toInt()
                movement = parts[2].toInt()
                sound = parts[3].toInt()
            } catch (e: NumberFormatException) {
                // Manejo de error si alguno de los valores no se puede convertir correctamente
                Log.e("FirstFragment", "Error parsing data: $e")
            }
        }
    }


    private fun updateUI() {
        binding.valTemp.text = "$temperature °C"
        binding.valHum.text = "$humidity %"
        binding.valMov.text = if (movement == 1) "Movimiento" else "Quieto"
        binding.valSonido.text = if (sound == 1) "Llanto" else "Silencio"
    }

    override fun onDestroyView() {//destruir vista
        super.onDestroyView()
        _binding = null//destruir binding
    }
}
