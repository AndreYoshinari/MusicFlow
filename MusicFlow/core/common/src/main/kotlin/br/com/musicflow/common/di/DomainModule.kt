package br.com.musicflow.common.di

import br.com.musicflow.common.domain.usecase.AddTrackToHistoryUseCase
import br.com.musicflow.common.domain.usecase.AddTrackToPlaylistUseCase
import br.com.musicflow.common.domain.usecase.CreatePlaylistUseCase
import br.com.musicflow.common.domain.usecase.DeletePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistUseCase
import br.com.musicflow.common.domain.usecase.RenamePlaylistUseCase
import br.com.musicflow.common.domain.usecase.GetFavoritesUseCase
import br.com.musicflow.common.domain.usecase.GetFeaturedArtistsUseCase
import br.com.musicflow.common.domain.usecase.GetPlaylistsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularAlbumsUseCase
import br.com.musicflow.common.domain.usecase.GetPopularTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentTracksUseCase
import br.com.musicflow.common.domain.usecase.GetRecentlyPlayedUseCase
import br.com.musicflow.common.domain.usecase.ObservePopularTracksUseCase
import br.com.musicflow.common.domain.usecase.GetTrackDetailsUseCase
import br.com.musicflow.common.domain.usecase.GetTrackStreamUrlUseCase
import br.com.musicflow.common.domain.usecase.GetTracksByArtistUseCase
import br.com.musicflow.common.domain.usecase.ObserveTrackSearchUseCase
import br.com.musicflow.common.domain.usecase.RemoveTrackFromPlaylistUseCase
import br.com.musicflow.common.domain.usecase.SearchArtistsUseCase
import br.com.musicflow.common.domain.usecase.SearchTracksUseCase
import br.com.musicflow.common.domain.usecase.ToggleFavoriteUseCase
import org.koin.dsl.module

val domainModule = module {
    factory { GetPopularTracksUseCase(get()) }
    factory { ObservePopularTracksUseCase(get()) }
    factory { GetFeaturedArtistsUseCase(get()) }
    factory { GetPopularAlbumsUseCase(get()) }
    factory { GetRecentTracksUseCase(get(), get()) }
    factory { SearchTracksUseCase(get()) }
    factory { SearchArtistsUseCase(get()) }
    factory { GetTrackDetailsUseCase(get()) }
    factory { GetTracksByArtistUseCase(get()) }
    factory { GetTrackStreamUrlUseCase(get()) }
    factory { ObserveTrackSearchUseCase(get()) }
    factory { GetFavoritesUseCase(get()) }
    factory { ToggleFavoriteUseCase(get()) }
    factory { CreatePlaylistUseCase(get()) }
    factory { GetPlaylistUseCase(get()) }
    factory { RenamePlaylistUseCase(get()) }
    factory { DeletePlaylistUseCase(get()) }
    factory { AddTrackToPlaylistUseCase(get()) }
    factory { RemoveTrackFromPlaylistUseCase(get()) }
    factory { GetPlaylistsUseCase(get()) }
    factory { GetRecentlyPlayedUseCase(get()) }
    factory { AddTrackToHistoryUseCase(get()) }
}
