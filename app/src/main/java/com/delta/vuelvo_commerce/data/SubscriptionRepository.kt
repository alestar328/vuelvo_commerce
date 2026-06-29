package com.delta.vuelvo_commerce.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Writes the merchant's subscription to Cloud Firestore once a purchase becomes active.
 *
 * The document is keyed by the device UUID ([DeviceIdStore]) so each install owns a single record;
 * re-subscribing simply refreshes the stored end date. Stored fields:
 *  - `uuid` — the device UUID (also the document id, kept as a field for convenient querying).
 *  - `subscriptionEndDate` — when the subscription expires, as a Firestore [Timestamp].
 *
 * Deliberately *not* a singleton: construct it with a [FirebaseFirestore] instance and inject it
 * where required.
 */
class SubscriptionRepository(private val firestore: FirebaseFirestore) {

    /** Upserts the subscription record for [deviceUuid], expiring at [subscriptionEnd]. */
    suspend fun recordSubscription(deviceUuid: String, subscriptionEnd: Date) {
        val data = mapOf(
            FIELD_UUID to deviceUuid,
            FIELD_END to Timestamp(subscriptionEnd),
        )
        suspendCancellableCoroutine { cont ->
            firestore.collection(COLLECTION)
                .document(deviceUuid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
        }
        Log.i(TAG, "Suscripción registrada en Firestore: uuid=$deviceUuid, fin=$subscriptionEnd")
    }

    private companion object {
        const val TAG = "SubscriptionRepository"
        const val COLLECTION = "subscriptions"
        const val FIELD_UUID = "uuid"
        const val FIELD_END = "subscriptionEndDate"
    }
}
