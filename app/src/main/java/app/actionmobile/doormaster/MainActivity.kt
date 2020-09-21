package app.actionmobile.doormaster

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var activateButton: Button
    private var ct: ConnectThread? = null

    private lateinit var btAdapter: BluetoothAdapter
    private lateinit var pairedDevices: Set<BluetoothDevice>
    private lateinit var adapter: ArrayAdapter<String>
    private val REQUEST_ENABLE_BT = 1
    private var listViewItems = ArrayList<String>()
    private lateinit var btDeviceInfo: String
    private lateinit var btDeviceSpinner: Spinner
    private var isSpinnerInitialized: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        Log.d("MainActivity", "starting.........")
        activateButton = findViewById(R.id.activateButton) as Button

        btDeviceSpinner = findViewById(R.id.blueToothDevList) as Spinner

        activateButton.setOnClickListener {
            openConnection()
            ct?.writeYes()
            ct?.writeNo()
            try {
                Thread.sleep(300)
            } catch (e: InterruptedException) {
                Log.d("MainActivity", "thread.sleep fail!")
            }
            ct?.cancel()
            ct = null
        }

        adapter = ArrayAdapter(this.baseContext, android.R.layout.simple_list_item_1, listViewItems)
        // Specify the layout to use when the list of choices appears
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the spinnerAdapter to the spinner
        // Apply the spinnerAdapter to the spinner
        btDeviceSpinner.adapter = adapter
        adapter.add("") // add no filter item so user can choose to filter on none

        adapter.notifyDataSetChanged()

        adapter.notifyDataSetChanged()

        btDeviceSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                Log.d("MainActivity", "item is selected")
                if (isSpinnerInitialized) {
                    btDeviceInfo = btDeviceSpinner.selectedItem.toString()
                    Log.d("MainActivity", "$btDeviceInfo selected via spinner")
                    addUserPrefValue(btDeviceInfo)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                Log.d("MainActivity", "nothing selected")
            }
        }


        try {
        Log.d("MainActivity", "Do this thing!")
        btAdapter = BluetoothAdapter.getDefaultAdapter()


        if (!btAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, this.REQUEST_ENABLE_BT)
        }

        GetPairedDevices(btAdapter)

        loadDeviceFromPrefs()
        if (btDeviceInfo !== "") {
            var counter = 0
            while (counter < adapter.count) {
                Log.d("MainActivity", "adapter.getItem : " + adapter.getItem(counter).toString())
                if (adapter.getItem(counter).toString() == btDeviceInfo) {
                    break
                }
                counter++
            }
            btDeviceSpinner.setSelection(counter)
        }

        }
        catch(ex : Exception){Log.d("MainActivity", "Cannot get BT adapter. ${ex.message}")}
    }

    private fun openConnection() {
        if (pairedDevices.size > 0) {
            for (btItem in pairedDevices) {
                if (btItem != null) {
                    val name = btItem.name
                    Log.d("MainActivity", "bitItem.name : ${btItem.name}")
                    if (name == btDeviceInfo) {
                        val uuid = btItem.uuids[0].uuid
                        Log.d("MainActivity", uuid.toString())
                        if (ct == null) {
                            ct = ConnectThread();
                            ct?.constructor(btItem, uuid, null)
                        }
                        ct?.run(btAdapter)
                        return
                    }
                }
            }
        }
    }

    private fun GetPairedDevices(btAdapter: BluetoothAdapter) {
        pairedDevices = btAdapter.bondedDevices
        // If there are paired devices
        if (pairedDevices.size > 0) {
            // Loop through paired devices
            for (device in pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                adapter.add(device.name) // + "\n" + device.getAddress());
            }
            adapter.notifyDataSetChanged()
        }
        isSpinnerInitialized = true
    }

    fun addUserPrefValue(currentValue: String?) {
        val device = getSharedPreferences("BtDevice", MODE_PRIVATE)
        val outValue = device.getString("BtDevice", "")
        Log.d("MainActivity", device.getString("BtDevice", ""))
        val edit = device.edit()
        edit.putString("BtDevice", currentValue)
        edit.commit()
        Log.d("MainActivity", "final devices value : $outValue")
    }

    fun loadDeviceFromPrefs() {
        Log.d("MainActivity", "Loading device from preferences")
        val sitePrefs = getSharedPreferences("BtDevice", MODE_PRIVATE)
        val device = sitePrefs.getString("BtDevice", "")
        if (device !== "") {
            btDeviceInfo = device!!
        }
        Log.d("MainActivity", "btdevice : $device")
        Log.d("MainActivity", "Reading items from prefs")
    }
}