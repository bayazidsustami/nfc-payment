package com.example.nfcpayment

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import id.co.bri.brimo.nfcpayment.NFCPayment
import id.co.bri.brimo.nfcpayment.NFCPaymentService

class MainActivity : AppCompatActivity() {

    private val nfcPayment by lazy { NFCPayment.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnEmulate = findViewById<AppCompatButton>(R.id.btn_emulate)
        val etPayload = findViewById<AppCompatEditText>(R.id.et_msg)

        btnEmulate.setOnClickListener {

            if (nfcPayment.isNfcAvailable()) {
                if (!nfcPayment.isDefaultService()) {
                    nfcPayment.changeDefaultPaymentService(this)
                    return@setOnClickListener
                }
                val payload = etPayload.text?.trim().toString()
               startService(payload)
            } else {
                Toast.makeText(this, "nfc not available or not active please check it", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startService(payload: String) {
        val serviceIntent = Intent(this, NFCPaymentService::class.java).also {
            it.putExtra(NFCPaymentService.PAYLOAD_EXTRAS, payload)
        }
        startService(serviceIntent)
    }

}