package com.example.nfcpayment

import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val etInput = findViewById<AppCompatEditText>(R.id.et_msg)
        val btnEmulate = findViewById<AppCompatButton>(R.id.btn_emulate)

        btnEmulate.setOnClickListener {
            val inputtedText = etInput.text.toString()

            if (inputtedText.isEmpty()) {
                showToast("payload can't be empty")
                return@setOnClickListener
            }

            if (isNfcAvailable()) {
                val serviceIntent = Intent(this, MyHostApduService::class.java).also {
                    it.putExtra(MyHostApduService.PAYLOAD_EXTRAS, inputtedText)
                }
                startService(serviceIntent)
            } else {
                showToast("nfc not available or not active please check it")
            }
        }
    }

    private fun isNfcAvailable() : Boolean {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        val nfcAdapter = nfcManager.defaultAdapter
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

}