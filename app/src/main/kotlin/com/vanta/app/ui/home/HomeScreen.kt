package com.vanta.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanta.app.ui.components.MiniPlayer
import com.vanta.app.ui.components.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("VANTA", style = MaterialTheme.typography.titleLarge)
                        Text("YOUR MUSIC, REFINED", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = {}) { Icon(Icons.Default.Search, contentDescription = "Search") }
                    IconButton(onClick = {}) { Icon(Icons.Default.GraphicEq, contentDescription = "Equalizer") }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            MiniPlayer(
                state = state.player,
                onOpen = {},
                onToggle = viewModel::togglePlayback,
                onNext = viewModel::next,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Good evening.", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(5.dp))
                Text("A quieter way to listen.", color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(22.dp))
                Text("RECENTLY ADDED", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
            }
            items(state.recentlyAdded, key = { it.id }) { track ->
                TrackRow(track = track, onClick = { viewModel.play(track) })
            }
            item { Spacer(Modifier.height(18.dp)) }
        }
    }
}
