package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.DriveItem
import com.example.data.model.GoogleAccount
import com.example.data.model.PlaybackProgress
import com.example.data.repository.AccountRepository
import com.example.data.repository.DriveRepository
import com.example.data.repository.PlaybackRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class DriveTvUiState(
  val accounts: List<GoogleAccount> = emptyList(),
  val currentAccount: GoogleAccount? = null,
  val items: List<DriveItem> = emptyList(),
  val currentFolderPath: List<Pair<String, String>> = emptyList(), // [(id, name)]
  val isLoading: Boolean = false,
  val errorMessage: String? = null,
  val progressMap: Map<String, PlaybackProgress> = emptyMap(),
  val selectedFilter: String = "ALL", // "ALL", "VIDEOS", "FOLDERS"
  val searchQuery: String = "",
  val activeVideoForDetails: DriveItem? = null,
  val currentPlayingVideo: DriveItem? = null,
  val currentPlayingInitialPos: Long = 0L,
  val isAccountDialogOpen: Boolean = false
)

class DriveTvViewModel @JvmOverloads constructor(
  application: Application,
  private val accountRepository: AccountRepository = AccountRepository(application),
  private val driveRepository: DriveRepository = DriveRepository(),
  private val playbackRepository: PlaybackRepository = PlaybackRepository(application)
) : AndroidViewModel(application) {

  private val _uiState = MutableStateFlow(DriveTvUiState())
  val uiState: StateFlow<DriveTvUiState> = _uiState.asStateFlow()

  init {
    viewModelScope.launch {
      accountRepository.accounts.collectLatest { accs ->
        _uiState.value = _uiState.value.copy(accounts = accs)
      }
    }

    viewModelScope.launch {
      accountRepository.currentAccount.collectLatest { acc ->
        _uiState.value = _uiState.value.copy(currentAccount = acc)
        loadCurrentFolderOrShared()
      }
    }

    viewModelScope.launch {
      playbackRepository.progressMap.collectLatest { progressMap ->
        _uiState.value = _uiState.value.copy(progressMap = progressMap)
      }
    }
  }

  fun loadCurrentFolderOrShared() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
      val currentFolder = _uiState.value.currentFolderPath.lastOrNull()
      val token = _uiState.value.currentAccount?.accessToken ?: ""

      val result = if (currentFolder == null) {
        driveRepository.getSharedWithMeItems(
          accessToken = token,
          filterVideosOnly = _uiState.value.selectedFilter == "VIDEOS",
          searchQuery = _uiState.value.searchQuery
        )
      } else {
        driveRepository.getFolderItems(
          accessToken = token,
          folderId = currentFolder.first,
          filterVideosOnly = _uiState.value.selectedFilter == "VIDEOS"
        )
      }

      result.onSuccess { items ->
        _uiState.value = _uiState.value.copy(
          items = items,
          isLoading = false,
          errorMessage = null
        )
      }.onFailure { err ->
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          errorMessage = "No se pudieron cargar los archivos: ${err.localizedMessage}"
        )
      }
    }
  }

  fun onSearchChange(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
    loadCurrentFolderOrShared()
  }

  fun onFilterChange(filter: String) {
    _uiState.value = _uiState.value.copy(selectedFilter = filter)
    loadCurrentFolderOrShared()
  }

  fun onItemClick(item: DriveItem) {
    if (item.isFolder) {
      val newPath = _uiState.value.currentFolderPath + (item.id to item.name)
      _uiState.value = _uiState.value.copy(currentFolderPath = newPath, searchQuery = "")
      loadCurrentFolderOrShared()
    } else {
      // Open Movie Details dialog first
      _uiState.value = _uiState.value.copy(activeVideoForDetails = item)
    }
  }

  fun navigateBackFolder() {
    val path = _uiState.value.currentFolderPath
    if (path.isNotEmpty()) {
      _uiState.value = _uiState.value.copy(currentFolderPath = path.dropLast(1), searchQuery = "")
      loadCurrentFolderOrShared()
    }
  }

  fun openAccountDialog() {
    _uiState.value = _uiState.value.copy(isAccountDialogOpen = true)
  }

  fun closeAccountDialog() {
    _uiState.value = _uiState.value.copy(isAccountDialogOpen = false)
  }

  fun selectAccount(accountId: String) {
    viewModelScope.launch {
      accountRepository.switchAccount(accountId)
      _uiState.value = _uiState.value.copy(currentFolderPath = emptyList(), searchQuery = "")
    }
  }

  fun addAccount(token: String, email: String?, name: String?) {
    viewModelScope.launch {
      accountRepository.addOrUpdateAccount(token, email, name)
      _uiState.value = _uiState.value.copy(currentFolderPath = emptyList(), searchQuery = "")
    }
  }

  fun removeAccount(accountId: String) {
    viewModelScope.launch {
      accountRepository.removeAccount(accountId)
    }
  }

  fun closeMovieDetails() {
    _uiState.value = _uiState.value.copy(activeVideoForDetails = null)
  }

  fun startPlayback(item: DriveItem, resume: Boolean) {
    val progress = playbackRepository.getProgress(item.id)
    val initialPos = if (resume && progress != null) progress.positionMs else 0L

    _uiState.value = _uiState.value.copy(
      activeVideoForDetails = null,
      currentPlayingVideo = item,
      currentPlayingInitialPos = initialPos
    )
  }

  fun closePlayback(lastPositionMs: Long, durationMs: Long) {
    val video = _uiState.value.currentPlayingVideo
    if (video != null && lastPositionMs > 0 && durationMs > 0) {
      playbackRepository.saveProgress(
        fileId = video.id,
        fileName = video.name,
        positionMs = lastPositionMs,
        durationMs = durationMs
      )
    }
    _uiState.value = _uiState.value.copy(
      currentPlayingVideo = null,
      currentPlayingInitialPos = 0L
    )
  }

  fun playNextVideo(nextItem: DriveItem) {
    startPlayback(nextItem, resume = false)
  }

  fun playPreviousVideo(prevItem: DriveItem) {
    startPlayback(prevItem, resume = false)
  }

  fun getStreamUrl(fileId: String): String {
    val token = _uiState.value.currentAccount?.accessToken ?: ""
    return driveRepository.getStreamUrl(fileId, token)
  }
}
