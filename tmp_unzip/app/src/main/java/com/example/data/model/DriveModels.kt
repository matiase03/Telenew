package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class DriveFileListResponse(
  val files: List<DriveItem> = emptyList(),
  val nextPageToken: String? = null
)

@JsonClass(generateAdapter = true)
data class DriveItem(
  val id: String,
  val name: String,
  val mimeType: String = "",
  val size: Long? = null,
  val thumbnailLink: String? = null,
  val iconLink: String? = null,
  val webContentLink: String? = null,
  val modifiedTime: String? = null,
  val sharedWithMeTime: String? = null,
  val owners: List<DriveOwner>? = null,
  val videoMediaMetadata: VideoMediaMetadata? = null,
  val description: String? = null
) {
  val isFolder: Boolean
    get() = mimeType == "application/vnd.google-apps.folder"

  val isVideo: Boolean
    get() = mimeType.startsWith("video/") ||
            name.endsWith(".mp4", ignoreCase = true) ||
            name.endsWith(".mkv", ignoreCase = true) ||
            name.endsWith(".avi", ignoreCase = true) ||
            name.endsWith(".mov", ignoreCase = true) ||
            name.endsWith(".webm", ignoreCase = true) ||
            name.endsWith(".m4v", ignoreCase = true) ||
            name.endsWith(".ts", ignoreCase = true)

  val formattedSize: String
    get() {
      val s = size ?: return ""
      val kb = s / 1024.0
      val mb = kb / 1024.0
      val gb = mb / 1024.0
      return when {
        gb >= 1.0 -> String.format("%.2f GB", gb)
        mb >= 1.0 -> String.format("%.1f MB", mb)
        kb >= 1.0 -> String.format("%.0f KB", kb)
        else -> "$s B"
      }
    }

  val durationFormatted: String
    get() {
      val durationMillis = videoMediaMetadata?.durationMillis?.toLongOrNull() ?: return ""
      val totalSeconds = durationMillis / 1000
      val hours = totalSeconds / 3600
      val minutes = (totalSeconds % 3600) / 60
      val seconds = totalSeconds % 60
      return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
      } else {
        String.format("%02d:%02d", minutes, seconds)
      }
    }

  val resolutionBadge: String?
    get() {
      val width = videoMediaMetadata?.width ?: return null
      val height = videoMediaMetadata?.height ?: return null
      return when {
        width >= 3840 || height >= 2160 -> "4K UHD"
        width >= 1920 || height >= 1080 -> "1080p FHD"
        width >= 1280 || height >= 720 -> "720p HD"
        else -> "${height}p"
      }
    }
}

@JsonClass(generateAdapter = true)
data class VideoMediaMetadata(
  val width: Int? = null,
  val height: Int? = null,
  val durationMillis: String? = null
)

@JsonClass(generateAdapter = true)
data class DriveOwner(
  val displayName: String? = null,
  val emailAddress: String? = null,
  val photoLink: String? = null
)

data class GoogleAccount(
  val id: String,
  val email: String,
  val displayName: String,
  val avatarUrl: String? = null,
  val accessToken: String,
  val refreshToken: String? = null,
  val expiresAt: Long = 0L,
  val isCurrent: Boolean = false
)

data class PlaybackProgress(
  val fileId: String,
  val fileName: String,
  val positionMs: Long,
  val durationMs: Long,
  val lastWatchedAt: Long = System.currentTimeMillis()
) {
  val percentWatched: Float
    get() = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
}
