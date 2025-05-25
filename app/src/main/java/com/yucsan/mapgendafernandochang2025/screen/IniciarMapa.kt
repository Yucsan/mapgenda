package com.yucsan.mapgendafernandochang2025.screen


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

import com.yucsan.mapgendafernandochang2025.screen.onLine.PantallaMapaCompose
import com.yucsan.mapgendafernandochang2025.ThemeViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.GPSViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.LugarViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.MapViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.NavegacionViewModel

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource

import androidx.media3.ui.PlayerView
import androidx.media3.common.util.UnstableApi
import androidx.compose.runtime.*

import androidx.media3.datasource.DefaultDataSource
import androidx.media3.ui.AspectRatioFrameLayout


import androidx.compose.material3.CircularProgressIndicator

import com.yucsan.mapgendafernandochang2025.screen.perfil.PantallaLoginGoogle
import com.yucsan.mapgendafernandochang2025.util.Auth.AuthState
import com.yucsan.mapgendafernandochang2025.util.state.NetworkMonitor
import com.yucsan.mapgendafernandochang2025.viewmodel.AuthViewModel
import com.yucsan.mapgendafernandochang2025.viewmodel.UsuarioViewModel


@SuppressLint("ContextCastToActivity")
@OptIn(UnstableApi::class)
@Composable
fun iniciarMapa(
   lugarViewModel: LugarViewModel,
   navegacionViewModel: NavegacionViewModel,
   mapViewModel: MapViewModel,
   navController: NavController,
   gpsViewModel: GPSViewModel,
   themeViewModel: ThemeViewModel,
   usuarioViewModel: UsuarioViewModel,
   authViewModel: AuthViewModel,
    networkMonitor: NetworkMonitor
) {
   val context = LocalContext.current
   val authState by authViewModel.authState.collectAsState()

   // 🎬 Video de fondo
   val exoPlayer = remember {
      ExoPlayer.Builder(context).build().apply {
         val dataSourceFactory = DefaultDataSource.Factory(context)
         val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/raw/introvideo")
         val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
         setMediaSource(mediaSource)
         repeatMode = ExoPlayer.REPEAT_MODE_ALL
         prepare()
         playWhenReady = true
      }
   }

   DisposableEffect(Unit) {
      onDispose { exoPlayer.release() }
   }

   Box(modifier = Modifier.fillMaxSize()) {
      // Fondo de video
      AndroidView(
         factory = {
            PlayerView(it).apply {
               player = exoPlayer
               useController = false
               resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
               layoutParams = FrameLayout.LayoutParams(
                  ViewGroup.LayoutParams.MATCH_PARENT,
                  ViewGroup.LayoutParams.MATCH_PARENT
               )
            }
         },
         modifier = Modifier.fillMaxSize()
      )

      // Redirección por estado de sesión
      when (authState) {
         is AuthState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
               CircularProgressIndicator()
            }
         }

         is AuthState.NoAutenticado -> {
            Box(modifier = Modifier.fillMaxSize()) {
               AndroidView(
                  factory = {
                     PlayerView(it).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = FrameLayout.LayoutParams(
                           ViewGroup.LayoutParams.MATCH_PARENT,
                           ViewGroup.LayoutParams.MATCH_PARENT
                        )
                     }
                  },
                  modifier = Modifier.fillMaxSize()
               )

               PantallaLoginGoogle(
                  gpsViewModel = gpsViewModel,
                  authViewModel = authViewModel,
                  usuarioViewModel = usuarioViewModel,
                  networkMonitor = networkMonitor,
                  context = context
               )
            }
         }

         is AuthState.Autenticado -> {
            DisposableEffect(Unit) {
               exoPlayer.release() // 🔥 Libera el video inmediatamente al autenticarse
               onDispose { }
            }

            PantallaMapaCompose(
               viewModelLugar = lugarViewModel,
               navegacionViewModel = navegacionViewModel,
               mapViewModel = mapViewModel,
               navController = navController,
               themeViewModel = themeViewModel,
               networkMonitor = networkMonitor
            )
         }
      }

   }
}




