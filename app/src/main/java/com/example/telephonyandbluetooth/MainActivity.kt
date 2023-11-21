package com.example.telephonyandbluetooth

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Adapter
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.telephonyandbluetooth.ui.theme.TelephonyAndBluetoothTheme
import java.lang.reflect.Method

class MainActivity : ComponentActivity() {

    lateinit var bAdapter: BluetoothAdapter
    val text = mutableStateOf("text")
    val REQUEST_CODE_FOR_TELEPHONE:Int =1
    val PERMISSION_REQUEST_CODE = 100
    private lateinit var simOperaterName: String

    lateinit var telephonyManager:TelephonyManager
    lateinit var receiver: IncomingCallReceiver
    val registerForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            // Handle the Intent
            text.value = "Bluetooth is on"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        receiver = IncomingCallReceiver()
        setContent {
            val context = LocalContext.current

            bAdapter = BluetoothAdapter.getDefaultAdapter()
            telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if(bAdapter == null)
            {
                text.value = "Bluetooth is not available"
            }else
            {
                text.value = "Bluetooth is available"
            }

            TelephonyAndBluetoothTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column {
                        // we are using column to align our
                        // imageview to center of the screen.
                        Button(onClick = { /*TODO*/

                            val permission = ContextCompat.checkSelfPermission(context,
                                Manifest.permission.READ_PHONE_STATE)

                            if (permission != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.READ_PHONE_STATE), 1)
                            }else
                            {
                                simOperaterName = telephonyManager.simOperatorName
                                text.value = simOperaterName
                            }

                        }) {
                            Text("Get Imei Number")
                        }

                        Button(onClick = {
                                Log.i("BlueTooth", "Enable bluetooth")
                                    if (ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.BLUETOOTH_CONNECT
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                        ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH_CONNECT), 100);

                                    return@Button
                                }else
                                {
                                    if(bAdapter.isEnabled)
                                    {

                                        val pairedDevices: Set<BluetoothDevice>? = bAdapter.bondedDevices
                                        pairedDevices?.forEach { device ->
                                            val deviceName = device.name
                                            val deviceHardwareAddress = device.address // MAC address

                                            Log.i("BlueTooth", "Devices Connected  $deviceName + address = $deviceHardwareAddress")
                                        }

                                    }else
                                    {
                                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                                        registerForResult.launch(enableBtIntent)
                                    }

                                }



                        }) {
                            Text(text = "Bluetooth On")
                        }

                        Button(onClick = {

                            if (ActivityCompat.checkSelfPermission(
                                context,
                                Manifest.permission.BLUETOOTH_SCAN
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.i("BlueTooth", "Enable bluetooth 2")
                            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE)
                            registerForResult.launch(enableBtIntent)
                            return@Button
                        }else
                            {
                                ActivityCompat.requestPermissions(context as Activity, arrayOf(Manifest.permission.BLUETOOTH_SCAN), 100);
                            }

                        }) {
                            Text(text = "Discover DEvices")

                        }
                        Button(onClick = {

                            if(bAdapter.isEnabled)
                            {

                                val pairedDevices: Set<BluetoothDevice>? = bAdapter.bondedDevices
                                pairedDevices?.forEach { device ->
                                    val deviceName = device.name
                                    val deviceHardwareAddress = device.address // MAC address

                                    Log.i("BlueTooth", "Devices Connected  $deviceName + address = $deviceHardwareAddress + connected  state" + isConnected(device) )

                                    if(isConnected(device))
                                    {
                                        Toast.makeText(context,"Connected device name -->$deviceName + address = $deviceHardwareAddress",Toast.LENGTH_LONG).show()
                                    }
                                }

                            }

                        }) {
                            Text(text = "Paired Device")
                        }

                        Text(text = text.value)


                        Button(onClick = { 
                            val intent = Intent(context, MusicPlayerActivity::class.java)
                            startActivity(intent)
                            
                        }) {

                            Text(text = "Play Audio Media")
                        }

                        Button(onClick = {
                            val intent = Intent(context, VideoPlayerActivity::class.java)
                            startActivity(intent)

                        }) {

                            Text(text = "Play Video Media")
                        }

                    }
                }
            }
        }
    }

    private fun isConnected(device: BluetoothDevice): Boolean {
        return try {
            val m: Method = device.javaClass.getMethod("isConnected")
            m.invoke(device) as Boolean
        } catch (e: Exception) {
            throw IllegalStateException(e)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.i("BlueTooth", "Enable bluetooth 1")
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.BLUETOOTH_CONNECT
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.i("BlueTooth", "Enable bluetooth 2")


                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        registerForResult.launch(enableBtIntent)
                        return
                    }



                }

                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            REQUEST_CODE_FOR_TELEPHONE -> {
                // Ignore all other requests.
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission is granted. Continue the action or workflow
                    // in your app.
                    Log.i("BlueTooth", "Enable bluetooth 1")
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.READ_PHONE_STATE
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        Log.i("BlueTooth", "Enable bluetooth 2")
                        simOperaterName = telephonyManager.simOperatorName
                        text.value = simOperaterName
                        return
                    }



                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TelephonyAndBluetoothTheme {
        Greeting("Android")
    }
}