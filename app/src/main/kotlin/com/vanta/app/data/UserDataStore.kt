package com.vanta.app.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

data class UserPlaylist(val id: Long, val name: String, val trackIds: List<Long>)

/** Избранное и плейлисты пользователя (хранятся локально в SharedPreferences). */
@Singleton
class UserDataStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs = context.getSharedPreferences("vanta_user", Context.MODE_PRIVATE)

    private val _favorites = MutableStateFlow(loadFavorites())
    val favorites: StateFlow<Set<Long>> = _favorites.asStateFlow()

    private val _playlists = MutableStateFlow(loadPlaylists())
    val playlists: StateFlow<List<UserPlaylist>> = _playlists.asStateFlow()

    fun toggleFavorite(trackId: Long) {
        val cur = _favorites.value
        _favorites.value = if (trackId in cur) cur - trackId else cur + trackId
        prefs.edit().putString("favorites", _favorites.value.joinToString(",")).apply()
    }

    fun createPlaylist(name: String, firstTrackId: Long? = null) {
        val clean = name.trim()
        if (clean.isEmpty()) return
        val pl = UserPlaylist(System.currentTimeMillis(), clean, listOfNotNull(firstTrackId))
        _playlists.value = _playlists.value + pl
        savePlaylists()
    }

    fun deletePlaylist(id: Long) {
        _playlists.value = _playlists.value.filterNot { it.id == id }
        savePlaylists()
    }

    fun addToPlaylist(playlistId: Long, trackId: Long) {
        _playlists.value = _playlists.value.map {
            if (it.id == playlistId && trackId !in it.trackIds) it.copy(trackIds = it.trackIds + trackId) else it
        }
        savePlaylists()
    }

    private fun loadFavorites(): Set<Long> =
        prefs.getString("favorites", "")?.split(",")?.mapNotNull { it.toLongOrNull() }?.toSet() ?: emptySet()

    private fun loadPlaylists(): List<UserPlaylist> = try {
        val arr = JSONArray(prefs.getString("playlists", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val ids = o.getJSONArray("ids")
            UserPlaylist(o.getLong("id"), o.getString("name"), (0 until ids.length()).map { ids.getLong(it) })
        }
    } catch (e: Exception) {
        emptyList()
    }

    private fun savePlaylists() {
        val arr = JSONArray()
        _playlists.value.forEach { p ->
            arr.put(JSONObject().put("id", p.id).put("name", p.name).put("ids", JSONArray(p.trackIds)))
        }
        prefs.edit().putString("playlists", arr.toString()).apply()
    }
}
