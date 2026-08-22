package com.delta.vuelvo_commerce.data

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.random.Random

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
     * Returns this install's business code, generating and persisting a new one on first call.
     *
     * `businessCode` is now the unique identifier of the "comercio activo" registry
     * ([com.delta.vuelvo_commerce.data.BusinessRegistryRepository] keys its Firestore documents on
     * it, not on the install uuid), so a freshly generated code is checked against Firestore for
     * collisions (up to 5 attempts) before being persisted.
     */
    suspend fun getOrCreateBusinessCode(firestore: FirebaseFirestore): String {
        prefs.getString(KEY_BUSINESS_CODE, null)?.let { return it }
        var candidate = "%06d".format(Random.nextInt(1_000_000))
        for (attempt in 1..5) {
            val taken = runCatching {
                firestore.collection("businesses").document(candidate).get().await().exists()
            }.getOrDefault(false)
            if (!taken || attempt == 5) break
            candidate = "%06d".format(Random.nextInt(1_000_000))
        }
        prefs.edit { putString(KEY_BUSINESS_CODE, candidate) }
        return candidate
    }

    /**
     * Overwrites this install's business code with a manually edited value (purely local — it syncs to
     * Firestore on the next tag write, via the existing [com.delta.vuelvo_commerce.data.BusinessRegistryRepository]
     * upsert, not immediately here).
     */
    fun saveBusinessCode(value: String) {
        prefs.edit { putString(KEY_BUSINESS_CODE, value) }
    }

    private companion object {
        const val PREFS_NAME = "vuelvo_device"
        const val KEY_UUID = "device_uuid"
        const val KEY_BUSINESS_CODE = "business_code"
    }
}
