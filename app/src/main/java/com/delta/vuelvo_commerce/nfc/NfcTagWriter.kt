package com.delta.vuelvo_commerce.nfc

import android.app.Activity
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Outcome of an NFC write session. */
sealed interface WriteResult {
    data object Success : WriteResult
    data object Cancelled : WriteResult
    data class Failure(val message: String) : WriteResult
}

/**
 * Writes an NDEF URI record to a physical tag using reader mode. Reader mode delivers the tag
 * straight to our callback (no system NFC dispatch / no native popup), so the write happens and the
 * UI transitions to success without any extra user interaction.
 */
class NfcTagWriter(private val activity: Activity) {

    private val adapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    private companion object {
        const val TAG = "NfcTagWriter"
    }

    val hasNfc: Boolean get() = adapter != null
    val isEnabled: Boolean get() = adapter?.isEnabled == true

    /**
     * Starts a write session for [url]. Emits exactly once per tap with the result and keeps the
     * reader mode open until the collector is cancelled (e.g. the overlay is dismissed), at which
     * point reader mode is torn down via [awaitClose].
     */
    fun writeSession(url: String): Flow<WriteResult> = callbackFlow {
        val nfc = adapter
        if (nfc == null) {
            trySend(WriteResult.Failure("Este dispositivo no tiene NFC"))
            close()
            return@callbackFlow
        }
        if (!nfc.isEnabled) {
            trySend(WriteResult.Failure("Activa el NFC en los ajustes del sistema"))
            close()
            return@callbackFlow
        }

        val message = NdefMessage(arrayOf(NdefRecord.createUri(url)))
        Log.d(TAG, "Sesión iniciada. Acerca un tag para escribir el deeplink: $url")

        val callback = NfcAdapter.ReaderCallback { tag ->
            Log.d(TAG, "Tag detectado (${tag.id.toHex()}), escribiendo deeplink: $url")
            val result = writeTag(tag, message)
            when (result) {
                WriteResult.Success ->
                    Log.i(TAG, "✓ Deeplink escrito en el tag: $url")
                is WriteResult.Failure ->
                    Log.w(TAG, "✗ Error escribiendo el tag: ${result.message}")
                WriteResult.Cancelled ->
                    Log.d(TAG, "Escritura cancelada")
            }
            trySend(result)
        }

        val flags = NfcAdapter.FLAG_READER_NFC_A or
            NfcAdapter.FLAG_READER_NFC_B or
            NfcAdapter.FLAG_READER_NFC_F or
            NfcAdapter.FLAG_READER_NFC_V or
            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS

        nfc.enableReaderMode(activity, callback, flags, null)

        awaitClose { nfc.disableReaderMode(activity) }
    }

    private fun writeTag(tag: Tag, message: NdefMessage): WriteResult {
        val ndef = Ndef.get(tag)
        return try {
            if (ndef != null) {
                ndef.connect()
                when {
                    !ndef.isWritable ->
                        WriteResult.Failure("El tag es de solo lectura")
                    ndef.maxSize < message.toByteArray().size ->
                        WriteResult.Failure("El tag no tiene capacidad suficiente")
                    else -> {
                        ndef.writeNdefMessage(message)
                        WriteResult.Success
                    }
                }.also { runCatching { ndef.close() } }
            } else {
                val formatable = NdefFormatable.get(tag)
                    ?: return WriteResult.Failure("Tag NFC no compatible")
                formatable.connect()
                formatable.format(message)
                runCatching { formatable.close() }
                WriteResult.Success
            }
        } catch (e: Exception) {
            WriteResult.Failure(e.message ?: "Error al escribir el tag")
        }
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02X".format(it) }
}
