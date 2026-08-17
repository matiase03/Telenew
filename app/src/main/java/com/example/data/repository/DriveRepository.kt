package com.example.data.repository

import com.example.data.model.DriveItem
import com.example.data.model.DriveOwner
import com.example.data.model.VideoMediaMetadata
import com.example.data.network.DriveApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveRepository(
  private val apiService: DriveApiService = DriveApiService.create()
) {

  suspend fun getSharedWithMeItems(
    accessToken: String,
    filterVideosOnly: Boolean = false,
    searchQuery: String? = null
  ): Result<List<DriveItem>> = withContext(Dispatchers.IO) {
    if (accessToken.isBlank()) {
      return@withContext Result.success(getDemoSharedItems(filterVideosOnly, searchQuery))
    }

    try {
      var query = "sharedWithMe = true and trashed = false"

      if (!searchQuery.isNullOrBlank()) {
        val sanitized = searchQuery.replace("'", "\\'")
        query += " and name contains '$sanitized'"
      }

      if (filterVideosOnly) {
        query += " and (mimeType contains 'video/' or name contains '.mp4' or name contains '.mkv' or name contains '.avi')"
      }

      val response = apiService.listFiles(
        authorization = "Bearer $accessToken",
        query = query,
        pageSize = 100,
        orderBy = "sharedWithMeTime desc, modifiedTime desc"
      )

      val items = response.files
      Result.success(items)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  suspend fun getFolderItems(
    accessToken: String,
    folderId: String,
    filterVideosOnly: Boolean = false
  ): Result<List<DriveItem>> = withContext(Dispatchers.IO) {
    if (accessToken.isBlank() || folderId.startsWith("demo_folder_")) {
      return@withContext Result.success(getDemoFolderItems(folderId, filterVideosOnly))
    }

    try {
      var query = "'$folderId' in parents and trashed = false"
      if (filterVideosOnly) {
        query += " and (mimeType contains 'video/' or mimeType = 'application/vnd.google-apps.folder' or name contains '.mp4' or name contains '.mkv')"
      }

      val response = apiService.listFiles(
        authorization = "Bearer $accessToken",
        query = query,
        pageSize = 100,
        orderBy = "folder, modifiedTime desc, name asc"
      )

      Result.success(response.files)
    } catch (e: Exception) {
      e.printStackTrace()
      Result.failure(e)
    }
  }

  /**
   * Sample high quality test videos and folders for testing TV playback
   * and testing fast streaming & D-Pad remote navigation.
   */
  private fun getDemoSharedItems(filterVideosOnly: Boolean, searchQuery: String?): List<DriveItem> {
    val all = listOf(
      DriveItem(
        id = "demo_vid_1",
        name = "Big Buck Bunny [4K UHD].mp4",
        mimeType = "video/mp4",
        size = 2854000000L,
        thumbnailLink = "https://images.unsplash.com/photo-1574375927938-d5a98e8ffe85?w=600&auto=format&fit=crop&q=80",
        videoMediaMetadata = VideoMediaMetadata(
          width = 3840,
          height = 2160,
          durationMillis = "596000"
        ),
        owners = listOf(DriveOwner(displayName = "Familia González", emailAddress = "gonzalez.familia@gmail.com")),
        sharedWithMeTime = "2026-08-15T14:30:00Z",
        description = "Video 4K compartido en alta resolución para reproducción en TV."
      ),
      DriveItem(
        id = "demo_folder_1",
        name = "Películas y Series Compartidas",
        mimeType = "application/vnd.google-apps.folder",
        sharedWithMeTime = "2026-08-14T10:00:00Z",
        owners = listOf(DriveOwner(displayName = "Carlos Amigo", emailAddress = "carlos.movies@gmail.com")),
        description = "Carpeta compartida con colección de cine y series."
      ),
      DriveItem(
        id = "demo_vid_2",
        name = "Sintel Movie - Animation [1080p FHD].mp4",
        mimeType = "video/mp4",
        size = 1420000000L,
        thumbnailLink = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
        videoMediaMetadata = VideoMediaMetadata(
          width = 1920,
          height = 1080,
          durationMillis = "888000"
        ),
        owners = listOf(DriveOwner(displayName = "Lucía Martínez", emailAddress = "lucia.mtz@gmail.com")),
        sharedWithMeTime = "2026-08-13T18:22:00Z",
        description = "Cortometraje de animación y fantasía de código abierto."
      ),
      DriveItem(
        id = "demo_folder_2",
        name = "Videos Familiares Vacaciones 2026",
        mimeType = "application/vnd.google-apps.folder",
        sharedWithMeTime = "2026-08-12T12:00:00Z",
        owners = listOf(DriveOwner(displayName = "Mamá", emailAddress = "mama.viajes@gmail.com")),
        description = "Grabaciones de los viajes y recuerdos familiares."
      ),
      DriveItem(
        id = "demo_vid_3",
        name = "Tears of Steel - Sci-Fi Action [1080p].mkv",
        mimeType = "video/x-matroska",
        size = 1890000000L,
        thumbnailLink = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=600&auto=format&fit=crop&q=80",
        videoMediaMetadata = VideoMediaMetadata(
          width = 1920,
          height = 800,
          durationMillis = "734000"
        ),
        owners = listOf(DriveOwner(displayName = "Carlos Amigo", emailAddress = "carlos.movies@gmail.com")),
        sharedWithMeTime = "2026-08-11T20:15:00Z",
        description = "Película corta de ciencia ficción con efectos especiales avanzados."
      ),
      DriveItem(
        id = "demo_vid_4",
        name = "Cosmos Laundromat - First Cycle [1080p].mp4",
        mimeType = "video/mp4",
        size = 940000000L,
        thumbnailLink = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
        videoMediaMetadata = VideoMediaMetadata(
          width = 1920,
          height = 1080,
          durationMillis = "720000"
        ),
        owners = listOf(DriveOwner(displayName = "Marcos Editor", emailAddress = "marcos.video@gmail.com")),
        sharedWithMeTime = "2026-08-10T16:40:00Z",
        description = "Aventura surrealista producida por Blender Institute."
      ),
      DriveItem(
        id = "demo_vid_5",
        name = "Elephants Dream [720p HD].mp4",
        mimeType = "video/mp4",
        size = 620000000L,
        thumbnailLink = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=600&auto=format&fit=crop&q=80",
        videoMediaMetadata = VideoMediaMetadata(
          width = 1280,
          height = 720,
          durationMillis = "654000"
        ),
        owners = listOf(DriveOwner(displayName = "Marcos Editor", emailAddress = "marcos.video@gmail.com")),
        sharedWithMeTime = "2026-08-09T11:00:00Z",
        description = "Clásico de animación 3D de código abierto."
      )
    )

    return all.filter { item ->
      if (filterVideosOnly && !item.isVideo) return@filter false
      if (!searchQuery.isNullOrBlank()) {
        item.name.contains(searchQuery, ignoreCase = true)
      } else {
        true
      }
    }
  }

  private fun getDemoFolderItems(folderId: String, filterVideosOnly: Boolean): List<DriveItem> {
    return when (folderId) {
      "demo_folder_1" -> listOf(
        DriveItem(
          id = "demo_folder_1_vid1",
          name = "Episodio 01 - El Comienzo [1080p].mp4",
          mimeType = "video/mp4",
          size = 1100000000L,
          thumbnailLink = "https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=600&auto=format&fit=crop&q=80",
          videoMediaMetadata = VideoMediaMetadata(width = 1920, height = 1080, durationMillis = "2700000"),
          owners = listOf(DriveOwner(displayName = "Carlos Amigo", emailAddress = "carlos.movies@gmail.com")),
          sharedWithMeTime = "2026-08-14T10:00:00Z"
        ),
        DriveItem(
          id = "demo_folder_1_vid2",
          name = "Episodio 02 - La Búsqueda [1080p].mp4",
          mimeType = "video/mp4",
          size = 1250000000L,
          thumbnailLink = "https://images.unsplash.com/photo-1478760329108-5c3ed9d495a0?w=600&auto=format&fit=crop&q=80",
          videoMediaMetadata = VideoMediaMetadata(width = 1920, height = 1080, durationMillis = "2850000"),
          owners = listOf(DriveOwner(displayName = "Carlos Amigo", emailAddress = "carlos.movies@gmail.com")),
          sharedWithMeTime = "2026-08-14T10:05:00Z"
        ),
        DriveItem(
          id = "demo_folder_1_vid3",
          name = "Episodio 03 - El Desenlace [1080p].mp4",
          mimeType = "video/mp4",
          size = 1380000000L,
          thumbnailLink = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=600&auto=format&fit=crop&q=80",
          videoMediaMetadata = VideoMediaMetadata(width = 1920, height = 1080, durationMillis = "3100000"),
          owners = listOf(DriveOwner(displayName = "Carlos Amigo", emailAddress = "carlos.movies@gmail.com")),
          sharedWithMeTime = "2026-08-14T10:10:00Z"
        )
      )
      "demo_folder_2" -> listOf(
        DriveItem(
          id = "demo_folder_2_vid1",
          name = "Paseo por la Costa - Atardecer [4K].mp4",
          mimeType = "video/mp4",
          size = 3200000000L,
          thumbnailLink = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600&auto=format&fit=crop&q=80",
          videoMediaMetadata = VideoMediaMetadata(width = 3840, height = 2160, durationMillis = "1800000"),
          owners = listOf(DriveOwner(displayName = "Mamá", emailAddress = "mama.viajes@gmail.com")),
          sharedWithMeTime = "2026-08-12T12:00:00Z"
        ),
        DriveItem(
          id = "demo_folder_2_vid2",
          name = "Cena Familiar y Festejos [1080p].mp4",
          mimeType = "video/mp4",
          size = 1500000000L,
          thumbnailLink = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=600&auto=format&fit=crop&q=80",
          videoMediaMetadata = VideoMediaMetadata(width = 1920, height = 1080, durationMillis = "1200000"),
          owners = listOf(DriveOwner(displayName = "Mamá", emailAddress = "mama.viajes@gmail.com")),
          sharedWithMeTime = "2026-08-12T12:30:00Z"
        )
      )
      else -> emptyList()
    }
  }

  /**
   * Translates drive item id to streamable URL for fast video playback
   */
  fun getStreamUrl(fileId: String, accessToken: String): String {
    return when (fileId) {
      "demo_vid_1" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
      "demo_vid_2" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4"
      "demo_vid_3" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"
      "demo_vid_4" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
      "demo_vid_5" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
      "demo_folder_1_vid1" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
      "demo_folder_1_vid2" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
      "demo_folder_1_vid3" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
      "demo_folder_2_vid1" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4"
      "demo_folder_2_vid2" -> "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
      else -> "https://www.googleapis.com/drive/v3/files/$fileId?alt=media"
    }
  }
}
