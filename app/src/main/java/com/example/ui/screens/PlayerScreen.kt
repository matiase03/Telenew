package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Build
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.data.model.DriveItem
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvPrimaryBlue
import com.example.ui.theme.TvPrimaryCyan
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
  item: DriveItem,
  playlist: List<DriveItem>,
  accessToken: String,
  streamUrl: String,
  initialPositionMs: Long = 0L,
  onClose: (lastPositionMs: Long, durationMs: Long) -> Unit,
  onPlayNext: (DriveItem) -> Unit,
  onPlayPrevious: (DriveItem) -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  var isPlaying by remember { mutableStateOf(true) }
  var isBuffering by remember { mutableStateOf(true) }
  var currentPositionMs by remember { mutableLongStateOf(initialPositionMs) }
  var durationMs by remember { mutableLongStateOf(0L) }
  var showControls by remember { mutableStateOf(true) }
  var lastUserActionTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

  // Aspect ratio mode: 0: FIT, 1: ZOOM, 2: STRETCH
  var resizeMode by remember { mutableStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
  var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val focusRequester = remember { FocusRequester() }

  // Build high-performance ExoPlayer with optimized buffer parameters
  val exoPlayer = remember(streamUrl) {
    val loadControl = DefaultLoadControl.Builder()
      .setBufferDurationsMs(
        2500,  // minBufferMs: Start super fast
        30000, // maxBufferMs
        500,   // bufferForPlaybackMs: 0.5s playback threshold
        1500   // bufferForPlaybackAfterRebufferMs
      )
      .setPrioritizeTimeOverSizeThresholds(true)
      .build()

    val httpDataSourceFactory = DefaultHttpDataSource.Factory()
      .setUserAgent("DriveTV-AndroidTV/1.0")
      .setConnectTimeoutMs(15000)
      .setReadTimeoutMs(20000)
      .setAllowCrossProtocolRedirects(true)

    if (accessToken.isNotBlank()) {
      httpDataSourceFactory.setDefaultRequestProperties(
        mapOf("Authorization" to "Bearer $accessToken")
      )
    }

    val mediaSourceFactory = ProgressiveMediaSource.Factory(httpDataSourceFactory)

    ExoPlayer.Builder(context)
      .setLoadControl(loadControl)
      .setMediaSourceFactory(mediaSourceFactory)
      .build().apply {
        val mediaItem = MediaItem.fromUri(Uri.parse(streamUrl))
        setMediaItem(mediaItem)
        if (initialPositionMs > 0) {
          seekTo(initialPositionMs)
        }
        prepare()
        playWhenReady = true
      }
  }

  // Monitor player state & progress
  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onPlaybackStateChanged(state: Int) {
        when (state) {
          Player.STATE_BUFFERING -> isBuffering = true
          Player.STATE_READY -> {
            isBuffering = false
            durationMs = exoPlayer.duration.coerceAtLeast(0L)
          }
          Player.STATE_ENDED -> {
            isBuffering = false
            // Auto play next video if present
            val currentIndex = playlist.indexOfFirst { it.id == item.id }
            if (currentIndex in 0 until playlist.size - 1) {
              onPlayNext(playlist[currentIndex + 1])
            }
          }
          Player.STATE_IDLE -> {
            isBuffering = false
          }
        }
      }

      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }

      override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
        isBuffering = false
        errorMessage = "Error al reproducir: ${error.message}"
      }
    }

    exoPlayer.addListener(listener)

    onDispose {
      val pos = exoPlayer.currentPosition
      val dur = exoPlayer.duration
      exoPlayer.removeListener(listener)
      exoPlayer.release()
    }
  }

  // Periodic progress updater
  LaunchedEffect(exoPlayer) {
    while (isActive) {
      if (exoPlayer.isPlaying) {
        currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
        durationMs = exoPlayer.duration.coerceAtLeast(0L)
      }
      delay(500)
    }
  }

  // Auto-hide controls after 4 seconds of inactivity
  LaunchedEffect(lastUserActionTime, showControls, isPlaying) {
    if (showControls && isPlaying) {
      delay(4000)
      showControls = false
    }
  }

  // Request focus for D-Pad navigation
  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  val currentIndex = playlist.indexOfFirst { it.id == item.id }
  val hasPrevious = currentIndex > 0
  val hasNext = currentIndex in 0 until playlist.size - 1

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black)
      .focusRequester(focusRequester)
      .focusable()
      .onKeyEvent { keyEvent ->
        if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_DOWN) {
          lastUserActionTime = System.currentTimeMillis()
          when (keyEvent.nativeKeyEvent.keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
              if (isPlaying) exoPlayer.pause() else exoPlayer.play()
              showControls = true
              true
            }
            KeyEvent.KEYCODE_DPAD_LEFT, KeyEvent.KEYCODE_MEDIA_REWIND -> {
              val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
              exoPlayer.seekTo(newPos)
              currentPositionMs = newPos
              showControls = true
              true
            }
            KeyEvent.KEYCODE_DPAD_RIGHT, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD -> {
              val maxDur = exoPlayer.duration
              val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(if (maxDur > 0) maxDur else Long.MAX_VALUE)
              exoPlayer.seekTo(newPos)
              currentPositionMs = newPos
              showControls = true
              true
            }
            KeyEvent.KEYCODE_DPAD_UP, KeyEvent.KEYCODE_DPAD_DOWN -> {
              showControls = !showControls
              true
            }
            KeyEvent.KEYCODE_BACK -> {
              if (showControls) {
                showControls = false
                true
              } else {
                onClose(exoPlayer.currentPosition, exoPlayer.duration)
                true
              }
            }
            else -> false
          }
        } else {
          false
        }
      }
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) {
        lastUserActionTime = System.currentTimeMillis()
        showControls = !showControls
      }
      .testTag("player_screen_container")
  ) {
    // Media3 PlayerView
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
          this.resizeMode = resizeMode
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
        }
      },
      update = { playerView ->
        playerView.player = exoPlayer
        playerView.resizeMode = resizeMode
      },
      modifier = Modifier.fillMaxSize()
    )

    // Buffering indicator
    if (isBuffering) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          CircularProgressIndicator(
            color = TvPrimaryCyan,
            strokeWidth = 4.dp,
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "Cargando video de Google Drive...",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    }

    // Error message overlay
    errorMessage?.let { err ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
      ) {
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = TvSurface,
          modifier = Modifier.padding(32.dp)
        ) {
          Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(text = "Error de Reproducción", color = Color(0xFFEF476F), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = err, color = TvTextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = TvPrimaryCyan,
                modifier = Modifier
                  .clickable {
                    errorMessage = null
                    exoPlayer.prepare()
                    exoPlayer.play()
                  }
                  .padding(horizontal = 16.dp, vertical = 10.dp)
              ) {
                Text(text = "Reintentar", color = TvBackground, fontWeight = FontWeight.Bold)
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = TvSurfaceVariant,
                modifier = Modifier
                  .clickable { onClose(exoPlayer.currentPosition, exoPlayer.duration) }
                  .padding(horizontal = 16.dp, vertical = 10.dp)
              ) {
                Text(text = "Salir", color = TvTextPrimary, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }

    // On-Screen Display (OSD) Overlay
    AnimatedVisibility(
      visible = showControls,
      enter = fadeIn(),
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              listOf(
                Color.Black.copy(alpha = 0.8f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.9f)
              )
            )
          )
      ) {
        // Top Header Info
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 24.dp)
            .align(Alignment.TopStart),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { onClose(exoPlayer.currentPosition, exoPlayer.duration) },
              modifier = Modifier
                .background(TvSurfaceVariant.copy(alpha = 0.7f), CircleShape)
                .testTag("btn_close_player")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar reproductor",
                tint = Color.White
              )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
              Text(
                text = item.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
              )
              item.owners?.firstOrNull()?.let { owner ->
                Text(
                  text = "Compartido por: ${owner.displayName ?: "Google Drive"}",
                  fontSize = 12.sp,
                  color = TvTextSecondary
                )
              }
            }
          }

          // Top Right Badges & Controls (Aspect Ratio, Speed)
          Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Speed Toggle Button
            PlayerModePill(
              text = "${playbackSpeed}x",
              icon = Icons.Default.Speed,
              onClick = {
                val nextSpeed = when (playbackSpeed) {
                  0.75f -> 1.0f
                  1.0f -> 1.25f
                  1.25f -> 1.5f
                  1.5f -> 2.0f
                  else -> 0.75f
                }
                playbackSpeed = nextSpeed
                exoPlayer.playbackParameters = PlaybackParameters(nextSpeed)
                lastUserActionTime = System.currentTimeMillis()
              }
            )

            // Aspect Ratio Toggle Button
            PlayerModePill(
              text = when (resizeMode) {
                AspectRatioFrameLayout.RESIZE_MODE_FIT -> "16:9 Original"
                AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> "Zoom / Llenar"
                else -> "Estirar"
              },
              icon = Icons.Default.AspectRatio,
              onClick = {
                resizeMode = when (resizeMode) {
                  AspectRatioFrameLayout.RESIZE_MODE_FIT -> AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                  AspectRatioFrameLayout.RESIZE_MODE_ZOOM -> AspectRatioFrameLayout.RESIZE_MODE_FILL
                  else -> AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
                lastUserActionTime = System.currentTimeMillis()
              }
            )
          }
        }

        // Center Quick Play / Rewind / FastForward Buttons
        Row(
          modifier = Modifier.align(Alignment.Center),
          horizontalArrangement = Arrangement.spacedBy(28.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Previous Video
          if (hasPrevious) {
            TvPlayerRoundButton(
              icon = Icons.Default.SkipPrevious,
              contentDescription = "Video anterior",
              onClick = { onPlayPrevious(playlist[currentIndex - 1]) },
              size = 48.dp
            )
          }

          // Rewind 10s
          TvPlayerRoundButton(
            icon = Icons.Default.FastRewind,
            contentDescription = "Retroceder 10 segundos",
            onClick = {
              val newPos = (exoPlayer.currentPosition - 10000L).coerceAtLeast(0L)
              exoPlayer.seekTo(newPos)
              currentPositionMs = newPos
              lastUserActionTime = System.currentTimeMillis()
            },
            size = 54.dp
          )

          // Play / Pause
          TvPlayerRoundButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
            onClick = {
              if (isPlaying) exoPlayer.pause() else exoPlayer.play()
              lastUserActionTime = System.currentTimeMillis()
            },
            isPrimary = true,
            size = 68.dp
          )

          // Fast Forward 10s
          TvPlayerRoundButton(
            icon = Icons.Default.FastForward,
            contentDescription = "Avanzar 10 segundos",
            onClick = {
              val maxDur = exoPlayer.duration
              val newPos = (exoPlayer.currentPosition + 10000L).coerceAtMost(if (maxDur > 0) maxDur else Long.MAX_VALUE)
              exoPlayer.seekTo(newPos)
              currentPositionMs = newPos
              lastUserActionTime = System.currentTimeMillis()
            },
            size = 54.dp
          )

          // Next Video
          if (hasNext) {
            TvPlayerRoundButton(
              icon = Icons.Default.SkipNext,
              contentDescription = "Siguiente video",
              onClick = { onPlayNext(playlist[currentIndex + 1]) },
              size = 48.dp
            )
          }
        }

        // Bottom Controls & Timeline Bar
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 36.dp, vertical = 28.dp)
            .align(Alignment.BottomCenter)
        ) {
          // Time Labels
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = formatTime(currentPositionMs),
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = TvPrimaryCyan
            )
            Text(
              text = formatTime(durationMs),
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              color = TvTextSecondary
            )
          }

          Spacer(modifier = Modifier.height(4.dp))

          // Slider / Seekbar
          val progressRatio = if (durationMs > 0) {
            (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
          } else {
            0f
          }

          Slider(
            value = progressRatio,
            onValueChange = { ratio ->
              lastUserActionTime = System.currentTimeMillis()
              val targetPos = (ratio * durationMs).toLong()
              exoPlayer.seekTo(targetPos)
              currentPositionMs = targetPos
            },
            colors = SliderDefaults.colors(
              thumbColor = TvPrimaryCyan,
              activeTrackColor = TvPrimaryCyan,
              inactiveTrackColor = Color.White.copy(alpha = 0.25f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("player_seek_slider")
          )

          // Quick D-Pad Hint
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            Text(
              text = "Control remoto TV: ◀ ▶ Salto 10s • OK Pausa/Play • ▲ ▼ Menú",
              fontSize = 11.sp,
              color = TvTextSecondary.copy(alpha = 0.8f)
            )
          }
        }
      }
    }
  }
}

@Composable
fun PlayerModePill(
  text: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  var isFocused by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = if (isFocused) TvPrimaryCyan else TvSurfaceVariant.copy(alpha = 0.8f),
    modifier = Modifier
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .clickable { onClick() }
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isFocused) TvBackground else Color.White,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isFocused) TvBackground else Color.White
      )
    }
  }
}

@Composable
fun TvPlayerRoundButton(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  onClick: () -> Unit,
  isPrimary: Boolean = false,
  size: androidx.compose.ui.unit.Dp
) {
  var isFocused by remember { mutableStateOf(false) }

  Surface(
    shape = CircleShape,
    color = if (isFocused) TvFocusBorder else if (isPrimary) TvPrimaryCyan else TvSurfaceVariant.copy(alpha = 0.75f),
    modifier = Modifier
      .size(size)
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .clickable { onClick() }
  ) {
    Box(contentAlignment = Alignment.Center) {
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = if (isFocused || isPrimary) TvBackground else Color.White,
        modifier = Modifier.size(size * 0.5f)
      )
    }
  }
}

fun formatTime(millis: Long): String {
  val totalSeconds = millis / 1000
  val hours = totalSeconds / 3600
  val minutes = (totalSeconds % 3600) / 60
  val seconds = totalSeconds % 60

  return if (hours > 0) {
    String.format("%d:%02d:%02d", hours, minutes, seconds)
  } else {
    String.format("%02d:%02d", minutes, seconds)
  }
}
