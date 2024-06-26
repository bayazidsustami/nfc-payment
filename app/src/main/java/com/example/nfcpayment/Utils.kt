package com.example.nfcpayment

import android.content.Context
import android.widget.Toast


object Utils {
    private val hexArray = "0123456789ABCDEF".toCharArray()

    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    fun isEqual(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) {
            return false
        }
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].toInt() xor b[i].toInt())
        }
        return result == 0
    }
}

fun Context.showToast(msg:String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}