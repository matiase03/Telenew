package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.DriveTvViewModel
import com.example.ui.components.AccountSwitchDialog
import com.example.ui.components.MovieDetailsDialog
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PlayerScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: DriveTvViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        DriveTvApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun DriveTvApp(viewModel: DriveTvViewModel) {
  val uiState by viewModel.uiState.collectAsState()

  // Handle Android TV remote Back button
  BackHandler(enabled = uiState.currentPlayingVideo != null || uiState.currentFolderPath.isNotEmpty() || uiState.activeVideoForDetails != null || uiState.isAccountDialogOpen) {
    when {
      uiState.isAccountDialogOpen -> viewModel.closeAccountDialog()
      uiState.activeVideoForDetails != null -> viewModel.closeMovieDetails()
      uiState.currentPlayingVideo != null -> viewModel.closePlayback(0L, 0L)
      uiState.currentFolderPath.isNotEmpty() -> viewModel.navigateBackFolder()
    }
  }

  Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
    if (uiState.currentPlayingVideo != null) {
      val video = uiState.currentPlayingVideo!!
      val streamUrl = viewModel.getStreamUrl(video.id)
      val playlist = uiState.items.filter { it.isVideo }
      val token = uiState.currentAccount?.accessToken ?: ""

      PlayerScreen(
        item = video,
        playlist = playlist,
        accessToken = token,
        streamUrl = streamUrl,
        initialPositionMs = uiState.currentPlayingInitialPos,
        onClose = { lastPos, duration ->
          viewModel.closePlayback(lastPos, duration)
        },
        onPlayNext = { nextItem ->
          viewModel.playNextVideo(nextItem)
        },
        onPlayPrevious = { prevItem ->
          viewModel.playPreviousVideo(prevItem)
        }
      )
    } else {
      HomeScreen(
        currentAccount = uiState.currentAccount,
        items = uiState.items,
        currentFolderPath = uiState.currentFolderPath,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        progressMap = uiState.progressMap,
        searchQuery = uiState.searchQuery,
        onSearchChange = { viewModel.onSearchChange(it) },
        selectedFilter = uiState.selectedFilter,
        onFilterChange = { viewModel.onFilterChange(it) },
        onOpenAccountDialog = { viewModel.openAccountDialog() },
        onRefresh = { viewModel.loadCurrentFolderOrShared() },
        onItemClick = { viewModel.onItemClick(it) },
        onNavigateBackFolder = { viewModel.navigateBackFolder() },
        modifier = Modifier.padding(innerPadding)
      )

      // Movie Details Dialog
      uiState.activeVideoForDetails?.let { video ->
        MovieDetailsDialog(
          item = video,
          progress = uiState.progressMap[video.id],
          onDismiss = { viewModel.closeMovieDetails() },
          onPlay = { resume ->
            viewModel.startPlayback(video, resume)
          }
        )
      }

      // Account Switch & Add Account Dialog
      if (uiState.isAccountDialogOpen) {
        AccountSwitchDialog(
          accounts = uiState.accounts,
          currentAccount = uiState.currentAccount,
          onDismiss = { viewModel.closeAccountDialog() },
          onSelectAccount = { accountId ->
            viewModel.selectAccount(accountId)
          },
          onAddAccount = { token, email, name ->
            viewModel.addAccount(token, email, name)
          },
          onRemoveAccount = { accountId ->
            viewModel.removeAccount(accountId)
          }
        )
      }
    }
  }
}
