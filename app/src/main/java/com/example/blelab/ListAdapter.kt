package com.example.blelab

import android.bluetooth.BluetoothClass
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class ListAdapter(context: Context, private val devices: MutableList<Device>) : BaseAdapter() {

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return devices.size
    }

    override fun getItem(p: Int): Any {
        return devices[p]
    }

    override fun getItemId(p: Int): Long {
        return p.toLong()
    }


    override fun getView(p: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.list_item, parent, false)

        val thisDevice = devices[p]

        var tv = rowView.findViewById(R.id.deviceName) as TextView
        tv.text = thisDevice.name

        tv = rowView.findViewById(R.id.deviceAddress) as TextView
        tv.text = thisDevice.address

        tv = rowView.findViewById(R.id.deviceSignal) as TextView
        tv.text = thisDevice.signal + " dBm"

        if(!thisDevice.isConnectable){
            rowView.alpha = 0.3f
        }

        return rowView
    }

}