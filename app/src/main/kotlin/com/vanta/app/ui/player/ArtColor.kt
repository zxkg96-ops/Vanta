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

/** dark: тёмный оттенок для фона, accent: яркий цвет для свечения краёв. */
data class ArtColors(val dark: Color, val accent: Color)

@Composable
fun rememberArtColors(artUri: String?, fallbackDark: Color, fallbackAccent: Color): ArtColors {
    val context = LocalContext.current
    var picked by remember(artUri) { mutableStateOf<Pair<Color, Color>?>(null) }

    LaunchedEffect(artUri) {
        picked = null
        if (artUri == null) return@LaunchedEffect
        val rgb: Pair<Int, Int>? = withContext(Dispatchers.IO) {
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
                    val dark = (p.darkMutedSwatch ?: p.mutedSwatch ?: p.dominantSwatch)?.rgb
                    val accent = (p.vibrantSwatch ?: p.lightVibrantSwatch ?: p.dominantSwatch)?.rgb
                    val d = dark ?: accent
                    val a = accent ?: dark
                    if (d == null || a == null) null else Pair(d, a)
                }
            } catch (e: Exception) {
                null
            }
        }
        if (rgb != null) picked = Pair(Color(rgb.first), Color(rgb.second))
    }

    val dark by animateColorAsState(picked?.first ?: fallbackDark, tween(600), label = "artDark")
    val accent by animateColorAsState(picked?.second ?: fallbackAccent, tween(600), label = "artAccent")
    return ArtColors(dark, accent)
}
