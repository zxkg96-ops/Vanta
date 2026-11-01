package com.vanta.app.ui.home

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.vanta.app.R
import com.vanta.app.ui.components.GlassTabBar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanta.app.ui.components.AlbumCard
import com.vanta.app.ui.components.MiniPlayer
import com.vanta.app.ui.components.TrackRow
import com.vanta.core.ScanState
import com.vanta.core.Track

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenPlayer: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scanState by viewModel.scanState.collectAsStateWithLifecycle()
    val allTracks by viewModel.allTracks.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val playlists by viewModel.playlists.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    var permissionGranted by remember { mutableStateOf(false) }
    var tab by remember { mutableIntStateOf(0) }
    var searching by remember { mutableStateOf(false) }
    var showEq by remember { mutableStateOf(false) }
    var actionsTrack by remember { mutableStateOf<Track?>(null) }
    var pickPlaylistFor by remember { mutableStateOf<Track?>(null) }
    var newPlaylistFor by remember { mutableStateOf<Track?>(null) }
    var showNewPlaylist by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    // Build lookup tables once per library update, not once per playlist row composition.
    val trackById = remember(allTracks) { allTracks.associateBy { it.id } }
    val playlistTrackCounts = remember(playlists, trackById) {
        playlists.associate { playlist ->
            playlist.id to playlist.trackIds.count { it in trackById }
        }
    }
    val tabTitles = remember { listOf("Треки", "Альбомы", "Артисты", "Плейлисты") }

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) viewModel.rescan()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    LaunchedEffect(Unit) {
        launcher.launch(permission)
    }

    LaunchedEffect(searching) {
        if (searching) focusRequester.requestFocus()
    }

    BackHandler(enabled = searching) {
        searching = false
        viewModel.setQuery("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (searching) {
                        TextField(
                            value = query,
                            onValueChange = viewModel::setQuery,
                            singleLine = true,
                            placeholder = { Text("Поиск по трекам") },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_vanta_logo),
                                contentDescription = null,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
                            )
                            Spacer(Modifier.width(10.dp))
                            Text("VANTA", style = MaterialTheme.typography.titleLarge)
                        }
                    }
                },
                actions = {
                    if (searching) {
                        IconButton(onClick = {
                            searching = false
                            viewModel.setQuery("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Закрыть поиск")
                        }
                    } else {
                        IconButton(onClick = { viewModel.rescan() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Rescan")
                        }
                        IconButton(onClick = {
                            tab = 0
                            searching = true
                        }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showEq = true }) {
                            Icon(Icons.Default.GraphicEq, contentDescription = "Equalizer")
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = state.player.queue.getOrNull(state.player.currentIndex) != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                MiniPlayer(
                    state = state.player,
                    onOpen = onOpenPlayer,
                    onToggle = viewModel::togglePlayback,
                    onNext = viewModel::next,
                    onDismiss = viewModel::closePlayer,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            GlassTabBar(
                titles = tabTitles,
                selected = tab,
                onSelect = { tab = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    0 -> TracksTab(
                        tracks = state.tracks,
                        favorites = favorites,
                        searching = query.isNotBlank(),
                        permissionGranted = permissionGranted,
                        scanState = scanState,
                        onPlay = viewModel::play,
                        onLongClick = { actionsTrack = it },
                        onRequestPermission = { launcher.launch(permission) },
                        onRescan = viewModel::rescan
                    )
                    1 -> if (albums.isEmpty()) {
                        EmptyText("Альбомов пока нет")
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(144.dp),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(20.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = albums,
                                key = { it.id },
                                contentType = { "album" }
                            ) { album ->
                                AlbumCard(album = album, onClick = { viewModel.playAlbum(album.id) })
                            }
                        }
                    }
                    2 -> if (artists.isEmpty()) {
                        EmptyText("Артистов пока нет")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            items(
                                items = artists,
                                key = { it.id },
                                contentType = { "artist" }
                            ) { artist ->
                                SimpleRow(
                                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                                    title = artist.name,
                                    subtitle = "${artist.trackCount} треков",
                                    onClick = { viewModel.playArtist(artist.id) }
                                )
                            }
                        }
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        item {
                            val favTracks = allTracks.filter { it.id in favorites }
                            SimpleRow(
                                icon = {
                                    Icon(
                                        Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                },
                                title = "Избранное",
                                subtitle = "${favTracks.size} треков",
                                onClick = { viewModel.playAll(favTracks) }
                            )
                        }
                        items(
                            items = playlists,
                            key = { it.id },
                            contentType = { "playlist" }
                        ) { pl ->
                            SimpleRow(
                                title = pl.name,
                                subtitle = "${playlistTrackCounts[pl.id] ?: 0} треков",
                                onClick = {
                                    viewModel.playAll(pl.trackIds.mapNotNull(trackById::get))
                                },
                                trailing = {
                                    IconButton(onClick = { viewModel.deletePlaylist(pl.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить")
                                    }
                                }
                            )
                        }
                        item {
                            Button(
                                onClick = {
                                    newPlaylistFor = null
                                    showNewPlaylist = true
                                },
                                modifier = Modifier.padding(top = 16.dp)
                            ) { Text("Новый плейлист") }
                        }
                    }
                }
            }
        }
    }

    if (showEq) {
        EqualizerSheet(
            eq = state.player.equalizer,
            onDismiss = { showEq = false },
            onEnabled = viewModel::setEqEnabled,
            onBand = viewModel::setEqBand,
            onBass = viewModel::setBassBoost,
            onPreset = viewModel::applyPreset,
            onAuto = viewModel::setAutoHeadphones
        )
    }

    actionsTrack?.let { t ->
        TrackActionsDialog(
            track = t,
            isFavorite = t.id in favorites,
            onDismiss = { actionsTrack = null },
            onPlayNext = { viewModel.playNext(t); actionsTrack = null },
            onAddToQueue = { viewModel.addToQueue(t); actionsTrack = null },
            onToggleFavorite = { viewModel.toggleFavorite(t.id); actionsTrack = null },
            onAddToPlaylist = { pickPlaylistFor = t; actionsTrack = null }
        )
    }

    pickPlaylistFor?.let { t ->
        PickPlaylistDialog(
            playlists = playlists,
            onPick = { pl ->
                viewModel.addToPlaylist(pl.id, t.id)
                pickPlaylistFor = null
            },
            onNew = {
                newPlaylistFor = t
                showNewPlaylist = true
                pickPlaylistFor = null
            },
            onDismiss = { pickPlaylistFor = null }
        )
    }

    if (showNewPlaylist) {
        NewPlaylistDialog(
            onCreate = { name ->
                viewModel.createPlaylist(name, newPlaylistFor?.id)
                showNewPlaylist = false
                newPlaylistFor = null
            },
            onDismiss = {
                showNewPlaylist = false
                newPlaylistFor = null
            }
        )
    }
}

@Composable
private fun EmptyText(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SimpleRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    icon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            icon()
            Spacer(Modifier.width(16.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, maxLines = 1)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun TracksTab(
    tracks: List<Track>,
    favorites: Set<Long>,
    searching: Boolean,
    permissionGranted: Boolean,
    scanState: ScanState,
    onPlay: (Track) -> Unit,
    onLongClick: (Track) -> Unit,
    onRequestPermission: () -> Unit,
    onRescan: () -> Unit
) {
    val heart: @Composable () -> Unit = {
        Icon(
            Icons.Default.Favorite,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
    }
    when {
        !permissionGranted && tracks.isEmpty() && !searching -> CenterColumn {
            Text("Нужен доступ к музыке на устройстве", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onRequestPermission, modifier = Modifier.padding(top = 16.dp)) { Text("Разрешить") }
        }
        scanState is ScanState.Scanning && tracks.isEmpty() -> CenterColumn {
            CircularProgressIndicator()
            Text(
                "Сканирование… найдено ${scanState.found}",
                modifier = Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        tracks.isEmpty() && searching -> EmptyText("Ничего не найдено")
        tracks.isEmpty() -> CenterColumn {
            Text("Музыка не найдена", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Добавьте аудиофайлы на устройство и нажмите обновить",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, start = 24.dp, end = 24.dp)
            )
            Button(onClick = onRescan, modifier = Modifier.padding(top = 16.dp)) { Text("Сканировать снова") }
        }
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            item {
                Text(
                    "Библиотека · ${tracks.size}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            items(
                items = tracks,
                key = { it.id },
                contentType = { "track" }
            ) { track ->
                TrackRow(
                    track = track,
                    onClick = { onPlay(track) },
                    trailing = if (track.id in favorites) heart else null,
                    onLongClick = { onLongClick(track) }
                )
            }
        }
    }
}

@Composable
private fun CenterColumn(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, content = content)
    }
}
