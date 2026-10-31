package com.vanta.player

import android.content.Intent
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

/**
 * Фоновое воспроизведение: уведомление, управление с экрана блокировки и наушников.
 * Использует тот же ExoPlayer, что и ExoPlayerController.
 */
class VantaPlaybackService : MediaSessionService() {

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val controller = ExoPlayerController.instance
        if (controller == null) {
            stopSelf()
            return
        }
        session = MediaSession.Builder(this, controller.exoPlayer).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onTaskRemoved(rootIntent: Intent?) {
        val p = session?.player
        if (p == null || !p.playWhenReady || p.mediaItemCount == 0) stopSelf()
    }

    override fun onDestroy() {
        session?.release()
        session = null
        super.onDestroy()
    }
}
