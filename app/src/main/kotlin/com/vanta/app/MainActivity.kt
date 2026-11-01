package com.vanta.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import com.vanta.app.ui.nav.VantaRoot
import com.vanta.app.ui.splash.VantaSplash
import com.vanta.app.ui.theme.VantaTheme
import com.vanta.core.PlayerController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Lazy: the audio engine is created after the first frame, not before it.
    @Inject lateinit var playerController: dagger.Lazy<PlayerController>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VantaTheme {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                var rootReady by remember { mutableStateOf(splashDone) }

                // Draw the splash first (it is cheap); build the heavy screens one frame later.
                LaunchedEffect(Unit) {
                    if (!rootReady) {
                        withFrameNanos { }
                        rootReady = true
                    }
                }

                Box(Modifier.fillMaxSize()) {
                    if (rootReady) VantaRoot(player = playerController.get())
                    if (!splashDone) VantaSplash(onFinished = { splashDone = true })
                }
            }
        }
        handleAudioIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAudioIntent(intent)
    }

    private fun handleAudioIntent(source: Intent?) {
        if (source?.action != Intent.ACTION_VIEW) return
        val uri: Uri = source.data ?: return
        if (source.type?.startsWith("audio/") == true || source.type == null) {
            playerController.get().openUri(uri.toString())
        }
    }
}
