package com.delta.vuelvo_commerce.data

import android.util.Base64
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Uploads the tag's logo/cover images to Firebase Storage right before writing the NFC tag, as flat
 * objects `{businessID}_logo.jpg` / `{businessID}_cover.jpg` at the bucket root (see
 * `TagConfig.imageRef`). The tag carries only that object name, never a URL — Firebase's signed
 * download URLs run 150-200+ chars each once percent-encoded inside the deeplink, which alone blows
 * past an NFC tag's capacity; the reader rebuilds the download URL from the name against its own
 * hardcoded bucket.
 *
 * Storage's security rules require `request.auth != null`, so every upload first makes sure there's a
 * signed-in user — anonymous auth, since merchants never create an account in this app.
 *
 * Deliberately not a singleton, same convention as [SubscriptionRepository]: construct it with
 * [FirebaseStorage]/[FirebaseAuth] instances and inject it where required.
 */
class ImageUploadRepository(private val storage: FirebaseStorage, private val auth: FirebaseAuth) {

    /**
     * Uploads a base64url-encoded image (as produced by `TagImageCodec.encode` — JPEG, or WebP when
     * the source had transparency; the `.jpg` object name is kept regardless, readers must sniff the
     * actual format from the bytes) to [path], replacing whatever was there before.
     */
    suspend fun uploadTagImage(base64UrlEncoded: String, path: String) {
        ensureSignedIn()
        val bytes = Base64.decode(base64UrlEncoded, BASE64_FLAGS)
        val ref = storage.reference.child(path)
        suspendCancellableCoroutine<Unit> { cont ->
            ref.putBytes(bytes, cacheControlMetadata)
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

        /**
         * The object name is stable per comercio, so a merchant replacing their logo re-uploads to the
         * same URL. Without this, Storage would serve the previous bytes from cache — its own CDN and
         * the consumer app's image cache both key on the URL — and customers would keep seeing the old
         * logo for up to an hour. `no-cache` still lets clients cache, it just makes them revalidate
         * first, which for images this small costs a 304 and nothing else.
         */
        val cacheControlMetadata: StorageMetadata =
            StorageMetadata.Builder().setCacheControl("no-cache").build()
    }
}
