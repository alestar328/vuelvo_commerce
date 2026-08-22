package com.delta.vuelvo_commerce.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Registers/refreshes the merchant's "active" record in Firestore (`businesses/{businessCode}`)
 * every time a tag is written — replaces real subscription verification with a much simpler
 * strategy: a comercio just needs a doc here with `active == true`, toggled manually from the
 * Firebase console. `active`/`createdAt` are written once, at creation, and never touched again
 * from the client, so a later tag rewrite (new logo, new reward) can't accidentally reactivate a
 * comercio an admin already deactivated.
 *
 * `businessCode` is the sole identifier of this model — there's no `uuid` field anymore, and the
 * install uuid ([DeviceIdStore.getOrCreate]) plays no part in this flow.
 *
 * Deliberately not a singleton, same convention as [SubscriptionRepository]/[ImageUploadRepository].
 */
class BusinessRegistryRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
) {

    /**
     * Upserts this comercio's record, keyed by the already-resolved [businessCode] (see
     * [DeviceIdStore.getOrCreateBusinessCode] — callers resolve it once, up front, since it's also
     * needed for the Storage object names before this call).
     */
    suspend fun registerActiveBusiness(
        businessCode: String,
        name: String,
        reward: String,
        logoRef: String?,
        coverRef: String?,
    ) {
        ensureSignedIn()
        val ref = firestore.collection(COLLECTION).document(businessCode)
        val snapshot = getDocument(ref)
        val fields = buildMap<String, Any> {
            put(FIELD_BUSINESS_CODE, businessCode)
            put(FIELD_NAME, name)
            put(FIELD_REWARD, reward)
            logoRef?.let { put(FIELD_LOGO_REF, it) }
            coverRef?.let { put(FIELD_COVER_REF, it) }
            put(FIELD_UPDATED_AT, Timestamp.now())
        }
        if (snapshot.exists()) {
            setDocument(ref, fields, merge = true)
        } else {
            setDocument(ref, fields + mapOf(FIELD_ACTIVE to true, FIELD_CREATED_AT to Timestamp.now()), merge = false)
        }
        Log.i(TAG, "Registro de comercio actualizado en Firestore: code=$businessCode")
    }

    private suspend fun getDocument(ref: DocumentReference): DocumentSnapshot =
        suspendCancellableCoroutine { cont ->
            ref.get()
                .addOnSuccessListener { if (cont.isActive) cont.resume(it) }
                .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
        }

    private suspend fun setDocument(ref: DocumentReference, data: Map<String, Any>, merge: Boolean) {
        suspendCancellableCoroutine<Unit> { cont ->
            val task = if (merge) ref.set(data, SetOptions.merge()) else ref.set(data)
            task
                .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
        }
    }

    /** Signs in anonymously if there's no current user yet. Idempotent — the session persists across launches. */
    private suspend fun ensureSignedIn() {
        if (auth.currentUser != null) return
        suspendCancellableCoroutine<Unit> { cont ->
            auth.signInAnonymously()
                .addOnSuccessListener { if (cont.isActive) cont.resume(Unit) }
                .addOnFailureListener { if (cont.isActive) cont.resumeWithException(it) }
        }
    }

    private companion object {
        const val TAG = "BusinessRegistry"
        const val COLLECTION = "businesses"
        const val FIELD_BUSINESS_CODE = "businessCode"
        const val FIELD_NAME = "name"
        const val FIELD_REWARD = "reward"
        const val FIELD_ACTIVE = "active"
        const val FIELD_LOGO_REF = "logoRef"
        const val FIELD_COVER_REF = "coverRef"
        const val FIELD_CREATED_AT = "createdAt"
        const val FIELD_UPDATED_AT = "updatedAt"
    }
}
