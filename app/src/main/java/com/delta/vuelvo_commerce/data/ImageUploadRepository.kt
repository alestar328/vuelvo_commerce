package com.delta.vuelvo_commerce.data

import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Uploads the tag's logo/cover images to Firebase Storage right before writing the NFC tag. The tag
 * itself only carries a `logo=1`/`cover=1` flag (see `TagConfig.deeplinkUrl`), never a URL — Firebase's
 * signed download URLs run 150-200+ chars each once percent-encoded inside the deeplink, which alone
 * blows past an NFC tag's capacity. The (future) reader reconstructs the exact Storage path from data
 * already on the tag: `tags/{uuid}/{businessID}/logo.jpg`, fixed bucket per app build.
 *
 * Storage's security rules require `request.auth != null`, so every upload first makes sure there's a
 * signed-in user — anonymous auth, since merchants never create an account in this app.
 *
 * Deliberately not a singleton, same convention as [SubscriptionRepository]: construct it with
 * [FirebaseStorage]/[FirebaseAuth] instances and inject it where required.
 */
class ImageUploadRepository(private val storage: FirebaseStorage, private val auth: FirebaseAuth) {

    /** Uploads a base64url-encoded JPEG (as produced by `TagImageCodec.encode`) to [path]. */
    suspend fun uploadTagImage(base64UrlEncoded: String, path: String) {
        ensureSignedIn()
        val bytes = Base64.decode(base64UrlEncoded, BASE64_FLAGS)
        val ref = storage.reference.child(path)
        suspendCancellableCoroutine<Unit> { cont ->
            ref.putBytes(bytes)
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
        const val BASE64_FLAGS = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
    }
}
