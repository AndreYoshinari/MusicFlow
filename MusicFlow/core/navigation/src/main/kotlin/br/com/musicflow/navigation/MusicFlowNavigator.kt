package br.com.musicflow.navigation

interface MusicFlowNavigator {
    fun openTrack(trackId: Long)
    fun openArtist(artistId: Long)
    fun openAlbum(albumId: Long)
    fun openPlaylist(playlistId: Long)
    fun openPlayer()
    fun back()
}
