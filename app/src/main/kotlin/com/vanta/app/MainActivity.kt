package com.vanta.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint
import com.vanta.app.ui.home.HomeScreen
import com.vanta.app.ui.theme.VantaTheme
import com.vanta.core.PlayerController
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var playerController: PlayerController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VantaTheme {
                HomeScreen()
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
