package com.example.blelab

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mScanCallback: ScanCallback? = null
    private var mBluetoothLeScanner: BluetoothLeScanner? = null

    private var mScanResults: HashMap<String, ScanResult>? = null
    private var mScanning = false
    private val deviceList: MutableList<Device> = java.util.ArrayList()

    private var listAdapter : ListAdapter? = null

    companion object {
        const val SCAN_PERIOD: Long = 2500
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        scanButton.isEnabled = false

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter!!.enable()
        }

        if (hasPermissions()) {
            Log.i("BLEinfo", "Permissions granted.")
            scanButton.isEnabled = true
        } else {
            Log.i("BLEinfo", "Permissions not granted")
        }

        listAdapter = ListAdapter(this, deviceList)
        list_view.adapter = listAdapter


        scanButton.setOnClickListener {
            Toast.makeText(this, "Scanning for devices", Toast.LENGTH_SHORT).show()
            startScan()
            scanButton.isEnabled = false
        }
    }

    private fun hasPermissions(): Boolean {
        if (mBluetoothAdapter == null || !mBluetoothAdapter!!.isEnabled) {
            Log.i("BLEinfo", "No Bluetooth LE capability")
            return false
        } else if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i("BLEinfo", "No fine location access")
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1);
            return true
        } else if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            Log.i("BLEinfo", "No bluetooth access")
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH), 1);
            return true
        } else if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            Log.i("BLEinfo", "No bluetooth admin access")
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_ADMIN), 1);
            return true
        }
        return true
    }

    private fun startScan() {
        Log.i("BLEinfo", "Scan start")
        mScanResults = HashMap()
        mScanCallback = BtleScanCallback()
        mBluetoothLeScanner = mBluetoothAdapter!!.bluetoothLeScanner

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
            .build()
        val filter: List<ScanFilter>? = null

        val mHandler = Handler()
        mHandler.postDelayed({ stopScan() }, SCAN_PERIOD)
        mScanning = true
        mBluetoothLeScanner!!.startScan(filter, settings, mScanCallback)
    }

    private fun stopScan() {
        Log.i("BLEinfo", "Scan stop")
        mBluetoothLeScanner!!.stopScan(mScanCallback)
        updateList()


        scanButton.isEnabled = true
        mScanning = false
    }

    private fun updateList() {
        deviceList.clear()
        mScanResults?.forEach { (_, result) ->
            val deviceToAdd = Device(
                result.scanRecord?.deviceName,
                result.device.address,
                result.rssi.toString(),
                result.isConnectable
            )
            deviceList.add(deviceToAdd)
        }
        listAdapter?.notifyDataSetChanged()
    }

    private inner class BtleScanCallback : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            addScanResult(result)
        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onBatchScanResults(results: List<ScanResult>) {
            for (result in results) {
                addScanResult(result)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.i("BLEinfo", "BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {
            val device = result.device
            val deviceAddress = device.address
            val deviceName = result.device.name
            mScanResults!![deviceAddress] = result

            Log.i(
                "BLEinfo",
                "Device name: $deviceName Device address: $deviceAddress (${result.isConnectable})"
            )

        }
    }
}