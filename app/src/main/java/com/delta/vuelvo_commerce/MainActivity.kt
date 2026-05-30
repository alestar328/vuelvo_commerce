package com.delta.vuelvo_commerce

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.delta.vuelvo_commerce.ui.biz.BizApp
import com.delta.vuelvo_commerce.ui.theme.VuBg
import com.delta.vuelvo_commerce.ui.theme.Vuelvo_commerceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Vuelvo_commerceTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = VuBg) {
                    // The screens carry their own status-bar top padding (header padTop: 58),
                    // so we only inset the very top for edge-to-edge correctness.
                    BizApp()
                }
            }
        }
    }
}
