package com.example.blelab

class Device(var name: String?, var address: String, var signal: String, var isConnectable: Boolean) {

    override fun toString(): String {
        return "$name $address $signal $isConnectable"
    }
}