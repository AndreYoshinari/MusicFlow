package br.com.musicflow.di

import br.com.musicflow.common.di.domainModule
import br.com.musicflow.data.di.dataModule
import br.com.musicflow.feature.home.di.homeModule
import br.com.musicflow.feature.library.di.libraryModule
import br.com.musicflow.feature.player.di.playerUiModule
import br.com.musicflow.feature.playlist.di.playlistModule
import br.com.musicflow.feature.search.di.searchModule
import br.com.musicflow.player.di.playerModule
import org.koin.dsl.module

val presentationModule = module {
    includes(
        homeModule,
        searchModule,
        libraryModule,
        playlistModule,
        playerUiModule,
    )
}

val musicFlowModules = listOf(
    dataModule,
    domainModule,
    playerModule,
    presentationModule,
)
