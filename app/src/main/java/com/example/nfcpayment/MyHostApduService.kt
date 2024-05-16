package com.example.nfcpayment

import android.app.Service
import android.content.Intent
import android.nfc.NdefRecord
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.math.BigInteger


class MyHostApduService: HostApduService() {

    companion object {
        const val PAYLOAD_EXTRAS = "payload-extras"

        private val NDEF_ID = byteArrayOf(
            0xE1.toByte(),
            0x04.toByte(),
        )

        const val STATUS_SUCCESS = "9000"
        const val STATUS_FAILED = "0000"
        const val CLA_NOT_SUPPORTED = "6E00"
        const val INS_NOT_SUPPORTED = "6D00"
        const val AID = "A0000006022020"
        const val SELECT_INS = "A4"
        const val DEFAULT_CLA = "00"
        const val MIN_APDU_LENGTH = 12
        const val SEND_PAYLOAD = "00B20100"
        const val SEND_PAYLOAD1 = "00B20200"

    }

    private var ndefUri = NdefRecord(
        NdefRecord.TNF_WELL_KNOWN,
        NdefRecord.RTD_TEXT,
        NDEF_ID,
        "hQVDUFYwMWFcTwegAAAGAiAgUAdRUklTQ1BNWgqTYAACISAJl0KPXyAOcml6a2EgcmFtYWRoYW5fLQRpZGVuX1AVYWRob24ucml6a3lAZ21haWwuY29tnyUCl0JjB590BGFlNjE=".toByteArray(),
    )

    private var ndefUriByte = ndefUri.toByteArray()
    private var ndefUriLen = BigInteger.valueOf(ndefUriByte.size.toLong()).toByteArray()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        if (intent?.hasExtra(PAYLOAD_EXTRAS) == true) {
            val payload = intent.getStringExtra(PAYLOAD_EXTRAS) ?: ""
            ndefUri = NdefRecord(
                NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT,
                NDEF_ID,
                payload.toByteArray(),
            )

            log("payload : $payload")
        }

        ndefUriByte = ndefUri.toByteArray()
        ndefUriLen = BigInteger.valueOf(ndefUriByte.size.toLong()).toByteArray()

        log("onStartCommand() | NDEF$ndefUri")

        return Service.START_STICKY_COMPATIBILITY
    }

    override fun processCommandApdu(commandApdu: ByteArray?, p1: Bundle?): ByteArray {
        val payload = "test test test test test tset test test test test test test test test "
        val payload2 = "test test test test test tes test test"

        val payloadByteArray = payload.toByteArray()
        val paylaodByteArray2 = payload2.toByteArray()

        if (commandApdu == null) {
            log("null")
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        val hexCommandApdu = Utils.bytesToHex(commandApdu)
        log("command apdu : $hexCommandApdu")
        if (hexCommandApdu.length < MIN_APDU_LENGTH) {
            log("min apdu length")
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        if (hexCommandApdu == "00A4040007A0000006022020")  {
            return Utils.hexStringToByteArray(STATUS_SUCCESS)
        }

        if (hexCommandApdu == "00B2010007A0000006022020") {
            return payloadByteArray + Utils.hexStringToByteArray("61FF")
        }

        return if (hexCommandApdu == "00B2020007A0000006022020") {
            paylaodByteArray2 + Utils.hexStringToByteArray(STATUS_SUCCESS)
        } else {
            Utils.hexStringToByteArray(STATUS_FAILED)
        }

    }

    override fun onDeactivated(p0: Int) {
        log("onDeactivated() Fired! Reason: $p0")
    }

    private fun log(message: String){
        Log.d("MY_APDU_SERVICE", message)
    }
}