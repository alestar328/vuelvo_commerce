package com.delta.vuelvo_commerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.delta.vuelvo_commerce.billing.StoreManager
import com.delta.vuelvo_commerce.data.DeviceIdStore
import com.delta.vuelvo_commerce.data.ImageUploadRepository
import com.delta.vuelvo_commerce.data.SubscriptionRepository
import com.delta.vuelvo_commerce.ui.biz.BizApp
import com.delta.vuelvo_commerce.ui.theme.VuBg
import com.delta.vuelvo_commerce.ui.theme.Vuelvo_commerceTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class MainActivity : ComponentActivity() {

    private lateinit var store: StoreManager
    private lateinit var subscriptions: SubscriptionRepository
    private lateinit var imageUploads: ImageUploadRepository

    /** Device UUID, created on first launch and reused thereafter (written to the NFC tag). */
    private lateinit var deviceUuid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = StoreManager(this)
        deviceUuid = DeviceIdStore(this).getOrCreate()
        subscriptions = SubscriptionRepository(Firebase.firestore)
        imageUploads = ImageUploadRepository(Firebase.storage, Firebase.auth)
        enableEdgeToEdge()
        setContent {
            Vuelvo_commerceTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = VuBg) {
                    // The screens carry their own status-bar top padding (header padTop: 58),
                    // so we only inset the very top for edge-to-edge correctness.
                    BizApp(
                        store = store,
                        deviceUuid = deviceUuid,
                        subscriptions = subscriptions,
                        imageUploads = imageUploads,
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        // Reconnect and re-check subscription state every time the app comes to the foreground.
        store.connect()
    }

    override fun onDestroy() {
        store.disconnect()
        super.onDestroy()
    }
}
