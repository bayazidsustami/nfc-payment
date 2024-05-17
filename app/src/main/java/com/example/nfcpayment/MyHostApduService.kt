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

        const val STATUS_NEXT = "61FF"
        const val STATUS_SUCCESS = "9000"
        const val STATUS_FAILED = "0000"
        const val AID = "A0000006022020"
        const val MIN_APDU_LENGTH = 12

        const val CMD_FIRST_REQ = "00A40400"
        const val CMD_NEXT_REQ = "00B20100"
        const val CMD_LAST_REQ = "00B20200"

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
        val payload = "okeoke"
        val payload2 = "okeokeoke"

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

        val length = hexCommandApdu.substring(8,10).toInt()
        val bodyLength = length * 2
        val bodyFirstPos = 10
        val bodyLastPos = bodyFirstPos + bodyLength

        log("length : $length")
        log("bodyLength : $bodyLength")
        log("bodyFirst : $bodyFirstPos")
        log("bodyLastPos : $bodyLastPos")
        log("aid : ${hexCommandApdu.substring(bodyFirstPos, bodyLastPos)}")
        log("firstCom : ${hexCommandApdu.substring(0, 8)}")


        if (hexCommandApdu.substring(bodyFirstPos, bodyLastPos) == AID && hexCommandApdu.substring(0, 8) == CMD_FIRST_REQ) {
            log("firstCommand : ${hexCommandApdu.substring(0, 8)}")
            return Utils.hexStringToByteArray(STATUS_SUCCESS)
        }

        if (hexCommandApdu.substring(0, 8) == CMD_NEXT_REQ) {
            log("secondCommand : ${hexCommandApdu.substring(0, 8)}")
            val hex = stringToHex(payload)
            val firstPayload = hex + STATUS_NEXT
            log(firstPayload)
            return firstPayload.hexStringToByteArray()
        }

        return if (hexCommandApdu.substring(0, 8) == CMD_LAST_REQ) {
            log("lastCommand : ${hexCommandApdu.substring(0, 8)}")
            val hex = stringToHex(payload2)
            val secondPayload = hex + STATUS_SUCCESS
            log(secondPayload)
            return secondPayload.hexStringToByteArray()
        } else {
            STATUS_FAILED.hexStringToByteArray()
        }

    }

    override fun onDeactivated(p0: Int) {
        log("onDeactivated() Fired! Reason: $p0")
    }

    private fun log(message: String){
        Log.d("MY_APDU_SERVICE", message)
    }

    private fun stringToHex(input: String): String {
        val hexString = StringBuilder()
        for (char in input) {
            val hex = Integer.toHexString(char.code)
            hexString.append(hex)
        }
        return hexString.toString()
    }

    private fun String.hexStringToByteArray() = ByteArray(this.length / 2) {
        this.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }
}