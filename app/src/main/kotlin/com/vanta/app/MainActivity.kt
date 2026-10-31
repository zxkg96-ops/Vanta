package com.vanta.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vanta.app.ui.nav.VantaRoot
import com.vanta.app.ui.theme.VantaTheme
import com.vanta.core.PlayerController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VantaTheme {
                VantaRoot(player = playerController)
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
            playerController.openUri(uri.toString())
        }
    }
}
