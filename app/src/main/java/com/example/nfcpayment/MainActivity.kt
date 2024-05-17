package com.example.nfcpayment

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.nfc.NfcManager
import android.nfc.cardemulation.CardEmulation
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton

class MainActivity : AppCompatActivity() {

    private var hasStarted = false
    private var cardEmulation: CardEmulation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        val nfcAdapter = nfcManager.defaultAdapter

        cardEmulation = CardEmulation.getInstance(nfcAdapter)

        val btnEmulate = findViewById<AppCompatButton>(R.id.btn_emulate)

        btnEmulate.setOnClickListener {

            if (isNfcAvailable()) {
                if (!isDefaultService()) {
                    Log.d("MY", "is not default")
                    navigateToChangeDefault()
                    return@setOnClickListener
                }
                hasStarted = true
               startService()
            } else {
                showToast("nfc not available or not active please check it")
            }
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this, MyHostApduService::class.java).also {
            it.putExtra(MyHostApduService.PAYLOAD_EXTRAS, "empty")
        }
        startService(serviceIntent)
    }

    override fun onResume() {
        super.onResume()
        cardEmulation?.setPreferredService(this, ComponentName(this, MyHostApduService::class.java))
    }

    private fun isNfcAvailable() : Boolean {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        val nfcAdapter = nfcManager.defaultAdapter
        return nfcAdapter != null && nfcAdapter.isEnabled
    }

    private fun isDefaultService(): Boolean {
        return cardEmulation?.isDefaultServiceForCategory(
            ComponentName(this, MyHostApduService::class.java),
            CardEmulation.CATEGORY_PAYMENT
        ) == true
    }

    private fun navigateToChangeDefault() {
        val intent = Intent(CardEmulation.ACTION_CHANGE_DEFAULT)
        intent.putExtra(
            CardEmulation.EXTRA_SERVICE_COMPONENT,
            ComponentName("com.example.nfcpayment", "com.example.nfcpayment.MyHostApduService")
        )
        intent.putExtra(CardEmulation.EXTRA_CATEGORY, CardEmulation.CATEGORY_PAYMENT)
        startActivity(intent)
    }

}