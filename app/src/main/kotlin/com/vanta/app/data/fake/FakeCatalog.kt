package com.vanta.app.data.fake

import com.vanta.core.Track

internal object FakeCatalog {
    private val titles = listOf(
        "Afterglow", "Nocturne", "Static Dreams", "Velvet Sky", "Midnight Drive",
        "Glass Echo", "Low Tide", "Parallel", "Slow Motion", "Blue Hour",
        "Weightless", "Northern Lights", "Monochrome", "Last Signal", "Quiet Storm",
        "Silverline", "Fragments", "Deep Current", "Nightfall", "Soft Focus",
        "Orbit", "Faded Polaroid", "Cold Geometry", "New Memory", "Dark Matter",
        "Hollow Light", "Somewhere Else", "Drift", "Eclipse", "Still Here",
        "Paper Planes", "Infinite Loop", "Neon Rain", "Between Worlds",
        "First Light", "Empty Streets", "Sundown", "No Reply", "Open Space",
        "The Distance"
    )
    private val artists = listOf(
        "Aether", "Mira Sol", "Northbound", "The Slow Hours", "Kairo",
        "Monoform", "Elias Grey", "Luma", "Night Archive", "Sonder"
    )
    private val albums = listOf(
        "The Quiet Between", "After Hours", "Soft Machines", "Blue Static",
        "Nocturnal Studies", "Fragments of Light", "Parallel Lines", "Low Orbit"
    )
    private val genres = listOf("Electronic", "Ambient", "Alternative", "Indie", "Downtempo")

    val tracks: List<Track> = titles.mapIndexed { index, title ->
        val artistIndex = index % artists.size
        val albumIndex = index % albums.size
        Track(
            id = index + 1L,
            uri = "content://vanta.fake/audio/${index + 1}",
            title = title,
            artist = artists[artistIndex],
            album = albums[albumIndex],
            albumId = albumIndex + 1L,
            genre = genres[index % genres.size],
            artUri = null,
            durationMs = (168 + (index * 17 % 190)) * 1000L,
            folder = "/Music/${genres[index % genres.size]}"
        )
    }
}
