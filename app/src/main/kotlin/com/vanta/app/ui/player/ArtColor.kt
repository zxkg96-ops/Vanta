package com.vanta.app.ui.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Тёмный оттенок, взятый из обложки (плавно меняется). Без обложки возвращает fallback. */
@Composable
fun rememberArtColor(artUri: String?, fallback: Color): Color {
    val context = LocalContext.current
    var color by remember(artUri) { mutableStateOf<Color?>(null) }

    LaunchedEffect(artUri) {
        color = null
        if (artUri == null) return@LaunchedEffect
        val rgb = withContext(Dispatchers.IO) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(artUri)
                    .allowHardware(false)
                    .size(128)
                    .build()
                val result = context.imageLoader.execute(request)
                val bitmap = (result as? SuccessResult)?.drawable?.toBitmap()
                if (bitmap == null) null else {
                    val p = Palette.from(bitmap).generate()
                    (p.darkMutedSwatch ?: p.mutedSwatch ?: p.dominantSwatch)?.rgb
                }
            } catch (e: Exception) {
                null
            }
        }
        if (rgb != null) color = Color(rgb)
    }

    val animated by animateColorAsState(color ?: fallback, tween(600), label = "artColor")
    return animated
}
