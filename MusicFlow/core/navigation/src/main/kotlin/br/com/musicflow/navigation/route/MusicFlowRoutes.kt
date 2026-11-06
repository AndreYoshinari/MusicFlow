package br.com.musicflow.navigation.route

import kotlinx.serialization.Serializable

@Serializable
data object HomeRoute

@Serializable
data object SearchRoute

@Serializable
data object LibraryRoute

@Serializable
data class TrackDetailRoute(val trackId: Long)

@Serializable
data class ArtistDetailRoute(val artistId: Long)

@Serializable
data class AlbumDetailRoute(val albumId: Long)

@Serializable
data class PlaylistDetailRoute(val playlistId: Long)

@Serializable
data object PlayerRoute
