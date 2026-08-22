package com.delta.vuelvo_commerce.data

import android.content.Context
import androidx.core.content.edit
import java.util.UUID

/**
 * Persists a per-install device UUID in [android.content.SharedPreferences].
 *
 * The id is created the first time the app runs ([getOrCreate]) and reused thereafter; it survives
 * process death but, like a regular preference, is cleared on uninstall / "clear data". It is written
 * to the NFC tag as the `uuid` deeplink parameter and used as the Firestore document key for the
 * merchant's subscription.
 *
 * It identifies an **install**, not a merchant. The backing prefs file is excluded from cloud backup
 * (see `res/xml/data_extraction_rules.xml`) so restoring the app onto a second phone can never give
 * two concurrently-used installs the same id; a device-to-device transfer does carry it over, since
 * that retires the old phone. Anything that must outlive a reinstall — entitlement above all — has to
 * key off the Play subscription (which restores per Google account) and not off this id.
 *
 * Not a singleton: instantiate once (e.g. in the Activity) and inject the instance where needed.
 */
class DeviceIdStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Returns the stored device UUID, generating and persisting a new one on first call (and as a
     * fallback whenever none is stored yet).
     */
    fun getOrCreate(): String {
        prefs.getString(KEY_UUID, null)?.let { return it }
        val generated = UUID.randomUUID().toString()
        prefs.edit { putString(KEY_UUID, generated) }
        return generated
    }

    /**
     * Returns the business code last entered on the config screen, or `null` if the merchant hasn't
     * typed one yet.
     *
     * `businessCode` is **entirely manual** — it identifies a comercio (not an install: several
     * businesses run from the same phone must not share one), so there is no auto-generation here.
     * The merchant types it in [com.delta.vuelvo_commerce.ui.biz.ConfigScreen]; this store only
     * remembers the last value typed, to prefill the field on the next launch.
     */
    fun getLastBusinessCode(): String? = prefs.getString(KEY_BUSINESS_CODE, null)

    /** Remembers the business code currently typed on the config screen, to prefill it next launch. */
    fun saveBusinessCode(value: String) {
        prefs.edit { putString(KEY_BUSINESS_CODE, value) }
    }

    private companion object {
        const val PREFS_NAME = "vuelvo_device"
        const val KEY_UUID = "device_uuid"
        const val KEY_BUSINESS_CODE = "business_code"
    }
}
