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

        //
        // We use the default AID from the HCE Android documentation
        // https://developer.android.com/guide/topics/connectivity/nfc/hce.html
        //
        // Ala... <aid-filter android:name="F0394148148100" />
        //
        private val APDU_SELECT = byteArrayOf(
            0x00.toByte(),
            0xA4.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x07.toByte(),
            0xF0.toByte(),
            0x39.toByte(),
            0x41.toByte(),
            0x48.toByte(),
            0x14.toByte(),
            0x81.toByte(),
            0x00.toByte(),
            0x00.toByte(),
        )

        private val CAPABILITY_CONTAINER = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x00.toByte(),
            0x0c.toByte(),
            0x02.toByte(),
            0xe1.toByte(),
            0x03.toByte(),
        )

        private val READ_CAPABILITY_CONTAINER = byteArrayOf(
            0x00.toByte(),
            0xb0.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0f.toByte(),
        )

        private val READ_CAPABILITY_CONTAINER_RESPONSE = byteArrayOf(
            0x00.toByte(),
            0x0F.toByte(),
            0x20.toByte(),
            0x00.toByte(),
            0x3B.toByte(),
            0x00.toByte(),
            0x34.toByte(),
            0x04.toByte(),
            0x06.toByte(),
            0xE1.toByte(),
            0x04.toByte(),
            0x00.toByte(),
            0x32.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x90.toByte(),
            0x00.toByte()
        )

        private val NDEF_SELECT = byteArrayOf(
            0x00.toByte(),
            0xa4.toByte(),
            0x00.toByte(),
            0x0c.toByte(),
            0x02.toByte(),
            0xE1.toByte(),
            0x04.toByte()
        )

        private val NDEF_READ_BINARY_NLEN = byteArrayOf(
            0x00.toByte(),
            0xb0.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x02.toByte(),
        )

        private val NDEF_READ_BINARY_GET_NDEF = byteArrayOf(
            0x00.toByte(),
            0xb0.toByte(),
            0x00.toByte(),
            0x00.toByte(),
            0x0f.toByte(),
        )

        private val A_OKAY = byteArrayOf(
            0x90.toByte(),
            0x00.toByte(),
        )

        private val NDEF_ID = byteArrayOf(
            0xE1.toByte(),
            0x04.toByte(),
        )

    }

    private var isCapableReadContainer = false

    private var ndefUri = NdefRecord(
        NdefRecord.TNF_WELL_KNOWN,
        NdefRecord.RTD_TEXT,
        NDEF_ID,
        "Hello world!".toByteArray(),
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

    override fun processCommandApdu(p0: ByteArray, p1: Bundle): ByteArray {
        log("processCommandApdu() | incoming commandApdu: " + Utils.bytesToHex(p0))

        /**
         * First command: NDEF Tag Application select (Section 5.5.2 in NFC Forum spec)
         */
        if (Utils.isEqual(APDU_SELECT, p0)) {
            log("APDU_SELECT triggered. Our Response: " + Utils.bytesToHex(A_OKAY))
            return A_OKAY
        }

        /**
         * Second command: Capability Container select (Section 5.5.3 in NFC Forum spec)
         */
        if (Utils.isEqual(CAPABILITY_CONTAINER, p0)) {
            log("CAPABILITY_CONTAINER triggered. Our Response: " + Utils.bytesToHex(A_OKAY))
            return A_OKAY
        }

        /**
         * Third command: ReadBinary data from CC file (Section 5.5.4 in NFC Forum spec)
         */
        if (Utils.isEqual(READ_CAPABILITY_CONTAINER, p0) && !isCapableReadContainer) {
            log("READ_CAPABILITY_CONTAINER triggered. Our Response: " + Utils.bytesToHex(READ_CAPABILITY_CONTAINER_RESPONSE))
            isCapableReadContainer = true
            return READ_CAPABILITY_CONTAINER_RESPONSE
        }


        /**
         * Fourth command: NDEF Select command (Section 5.5.5 in NFC Forum spec)
         */
        if (Utils.isEqual(NDEF_SELECT, p0)) {
            log("NDEF_SELECT triggered. Our Response: " + Utils.bytesToHex(A_OKAY))
            return A_OKAY
        }

        /**
         * Fifth command:  ReadBinary, read NLEN field
         */
        if (Utils.isEqual(NDEF_READ_BINARY_NLEN, p0)) {
            val start = byteArrayOf(0x00.toByte())
            val response = ByteArray(start.size + ndefUriLen.size + A_OKAY.size)
            System.arraycopy(start, 0, response, 0, start.size)
            System.arraycopy(ndefUriLen, 0, response, start.size, ndefUriLen.size)
            System.arraycopy(A_OKAY, 0, response, start.size + ndefUriLen.size, A_OKAY.size)

            log(response.toString())
            log("NDEF_READ_BINARY_NLEN triggered. Our Response: " + Utils.bytesToHex(response))
            return response
        }

        /**
         * Sixth command: ReadBinary, get NDEF data
         */
        if (Utils.isEqual(NDEF_READ_BINARY_GET_NDEF, p0)) {
            val start = byteArrayOf(0x00.toByte())
            val response = ByteArray(start.size + ndefUriLen.size + ndefUriByte.size + A_OKAY.size)

            System.arraycopy(start, 0, response, 0, start.size);
            System.arraycopy(ndefUriLen, 0, response, start.size, ndefUriLen.size);
            System.arraycopy(ndefUriByte, 0, response, start.size + ndefUriLen.size, ndefUriByte.size);
            System.arraycopy(A_OKAY, 0, response, start.size + ndefUriLen.size + ndefUriByte.size, A_OKAY.size);

            log(ndefUri.toString())
            log("NDEF_READ_BINARY_GET_NDEF triggered. Our Response: " + Utils.bytesToHex(response))

            isCapableReadContainer = false

            return response
        }

        log("processCommandApdu() | I don't know what's going on!!!.")

        return "Can I help you?".toByteArray()

    }

    override fun onDeactivated(p0: Int) {
        log("onDeactivated() Fired! Reason: $p0")
    }

    private fun log(message: String){
        Log.d("MY_APDU_SERVICE", message)
    }
}