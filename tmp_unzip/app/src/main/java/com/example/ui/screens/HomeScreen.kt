package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.DriveItem
import com.example.data.model.GoogleAccount
import com.example.data.model.PlaybackProgress
import com.example.ui.components.DriveItemTvCard
import com.example.ui.components.TvHeader
import com.example.ui.components.TvPrimaryButton
import com.example.ui.components.TvSecondaryButton
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvPrimaryBlue
import com.example.ui.theme.TvPrimaryCyan
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun HomeScreen(
  currentAccount: GoogleAccount?,
  items: List<DriveItem>,
  currentFolderPath: List<Pair<String, String>>, // list of (folderId, folderName)
  isLoading: Boolean,
  errorMessage: String?,
  progressMap: Map<String, PlaybackProgress>,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedFilter: String,
  onFilterChange: (String) -> Unit,
  onOpenAccountDialog: () -> Unit,
  onRefresh: () -> Unit,
  onItemClick: (DriveItem) -> Unit,
  onNavigateBackFolder: () -> Unit,
  modifier: Modifier = Modifier
) {
  val folders = items.filter { it.isFolder }
  val videos = items.filter { it.isVideo }
  val inProgressVideos = videos.filter {
    val prog = progressMap[it.id]
    prog != null && prog.percentWatched > 0.03f && prog.percentWatched < 0.95f
  }

  val heroVideo = inProgressVideos.firstOrNull() ?: videos.firstOrNull()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(TvBackground)
  ) {
    // TV Top Navigation & Header
    TvHeader(
      currentAccount = currentAccount,
      searchQuery = searchQuery,
      onSearchChange = onSearchChange,
      selectedFilter = selectedFilter,
      onFilterChange = onFilterChange,
      onOpenAccountDialog = onOpenAccountDialog,
      onRefresh = onRefresh
    )

    // Folder Breadcrumbs (if inside subfolder)
    if (currentFolderPath.isNotEmpty()) {
      FolderBreadcrumbBar(
        folderPath = currentFolderPath,
        onNavigateBack = onNavigateBackFolder
      )
    }

    // Main Content Area
    Box(modifier = Modifier.fillMaxSize()) {
      if (isLoading && items.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
              color = TvPrimaryCyan,
              modifier = Modifier.size(52.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = "Obteniendo archivos de Compartido conmigo...",
              color = TvTextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Medium
            )
          }
        }
      } else if (items.isEmpty()) {
        // Empty State
        EmptySharedView(
          searchQuery = searchQuery,
          currentAccount = currentAccount,
          onOpenAccountDialog = onOpenAccountDialog,
          onRefresh = onRefresh
        )
      } else {
        // Grid & Carousel content
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 40.dp)
        ) {
          // Hero Featured Banner (only when at root and not searching)
          if (currentFolderPath.isEmpty() && searchQuery.isBlank() && heroVideo != null) {
            item {
              HeroFeaturedCard(
                video = heroVideo,
                progress = progressMap[heroVideo.id],
                onPlay = { onItemClick(heroVideo) }
              )
              Spacer(modifier = Modifier.height(24.dp))
            }
          }

          // Continue Watching Row (if any)
          if (inProgressVideos.isNotEmpty() && searchQuery.isBlank()) {
            item {
              SectionHeader(
                title = "Continuar viendo",
                icon = Icons.Default.History,
                count = inProgressVideos.size
              )
              Spacer(modifier = Modifier.height(10.dp))
              LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                items(inProgressVideos, key = { "continue_${it.id}" }) { item ->
                  DriveItemTvCard(
                    item = item,
                    progress = progressMap[item.id],
                    onClick = { onItemClick(item) },
                    modifier = Modifier.width(280.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(28.dp))
            }
          }

          // Folders Section (if any folders present)
          if (folders.isNotEmpty() && (selectedFilter == "ALL" || selectedFilter == "FOLDERS")) {
            item {
              SectionHeader(
                title = "Carpetas compartidas",
                icon = Icons.Default.Folder,
                count = folders.size
              )
              Spacer(modifier = Modifier.height(10.dp))
              LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                items(folders, key = { "folder_${it.id}" }) { item ->
                  DriveItemTvCard(
                    item = item,
                    onClick = { onItemClick(item) },
                    modifier = Modifier.width(240.dp)
                  )
                }
              }
              Spacer(modifier = Modifier.height(28.dp))
            }
          }

          // All Shared Videos Grid/List
          if (videos.isNotEmpty() && (selectedFilter == "ALL" || selectedFilter == "VIDEOS")) {
            item {
              SectionHeader(
                title = if (currentFolderPath.isEmpty()) "Videos en Compartido conmigo" else "Videos en esta carpeta",
                icon = Icons.Default.VideoLibrary,
                count = videos.size
              )
              Spacer(modifier = Modifier.height(12.dp))
            }

            // Render video rows in pairs or grids
            val chunkedVideos = videos.chunked(3)
            items(chunkedVideos) { rowItems ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
              ) {
                for (item in rowItems) {
                  DriveItemTvCard(
                    item = item,
                    progress = progressMap[item.id],
                    onClick = { onItemClick(item) },
                    modifier = Modifier.weight(1f)
                  )
                }
                // Fill empty slots in last row if needed
                if (rowItems.size < 3) {
                  repeat(3 - rowItems.size) {
                    Spacer(modifier = Modifier.weight(1f))
                  }
                }
              }
            }
          }
        }
      }

      // Error toast overlay
      errorMessage?.let { err ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFEF476F).copy(alpha = 0.95f),
          modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(24.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = err, color = Color.White, fontSize = 13.sp)
          }
        }
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  count: Int
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = TvPrimaryCyan,
      modifier = Modifier.size(20.dp)
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = title,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold,
      color = TvTextPrimary
    )
    Spacer(modifier = Modifier.width(8.dp))
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = TvSurfaceVariant
    ) {
      Text(
        text = count.toString(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = TvPrimaryCyan,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
      )
    }
  }
}

@Composable
fun FolderBreadcrumbBar(
  folderPath: List<Pair<String, String>>,
  onNavigateBack: () -> Unit
) {
  Surface(
    color = TvSurfaceVariant.copy(alpha = 0.8f),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      var isFocused by remember { mutableStateOf(false) }
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isFocused) TvPrimaryCyan else TvSurfaceElevated,
        modifier = Modifier
          .onFocusChanged { isFocused = it.isFocused }
          .focusable()
          .clickable { onNavigateBack() }
          .testTag("btn_back_folder")
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = "Volver",
            tint = if (isFocused) TvBackground else TvTextPrimary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Volver",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFocused) TvBackground else TvTextPrimary
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Text(
        text = "Compartido conmigo",
        fontSize = 13.sp,
        color = TvTextSecondary
      )

      folderPath.forEach { (_, folderName) ->
        Text(text = "  /  ", color = TvTextSecondary, fontSize = 13.sp)
        Text(
          text = folderName,
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TvPrimaryCyan
        )
      }
    }
  }
}

@Composable
fun HeroFeaturedCard(
  video: DriveItem,
  progress: PlaybackProgress?,
  onPlay: () -> Unit
) {
  var isFocused by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 24.dp)
      .height(230.dp)
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .clickable { onPlay() }
      .testTag("hero_featured_card"),
    shape = RoundedCornerShape(20.dp),
    border = BorderStroke(
      width = if (isFocused) 3.dp else 1.dp,
      color = if (isFocused) TvFocusBorder else TvPrimaryCyan.copy(alpha = 0.2f)
    ),
    colors = CardDefaults.cardColors(containerColor = TvSurfaceElevated)
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      // Backdrop Thumbnail Image
      if (!video.thumbnailLink.isNullOrBlank()) {
        AsyncImage(
          model = video.thumbnailLink,
          contentDescription = video.name,
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
      }

      // Dark gradient overlay
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.horizontalGradient(
              listOf(
                TvBackground.copy(alpha = 0.95f),
                TvBackground.copy(alpha = 0.8f),
                Color.Transparent
              )
            )
          )
      )

      // Hero Content
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = TvPrimaryBlue
        ) {
          Text(
            text = if (progress != null) "REANUDAR REPRODUCCIÓN" else "DESTACADO COMPARTIDO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = video.name,
          fontSize = 22.sp,
          fontWeight = FontWeight.Bold,
          color = if (isFocused) TvFocusBorder else TvTextPrimary,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.fillMaxWidth(0.7f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          video.resolutionBadge?.let { res ->
            Text(text = res, color = TvPrimaryCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
          if (video.durationFormatted.isNotBlank()) {
            Text(text = "•  ${video.durationFormatted}", color = TvTextSecondary, fontSize = 12.sp)
          }
          if (video.formattedSize.isNotBlank()) {
            Text(text = "•  ${video.formattedSize}", color = TvTextSecondary, fontSize = 12.sp)
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        TvPrimaryButton(
          text = if (progress != null) "Continuar Viendo" else "Reproducir Ahora",
          icon = Icons.Default.PlayArrow,
          onClick = onPlay,
          testTag = "btn_hero_play"
        )
      }
    }
  }
}

@Composable
fun EmptySharedView(
  searchQuery: String,
  currentAccount: GoogleAccount?,
  onOpenAccountDialog: () -> Unit,
  onRefresh: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Surface(
        shape = CircleShape,
        color = TvSurfaceVariant,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = TvPrimaryCyan,
            modifier = Modifier.size(36.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(18.dp))

      Text(
        text = if (searchQuery.isNotBlank()) "No se encontraron videos con '$searchQuery'" else "No hay videos en 'Compartido conmigo'",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = TvTextPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Verifica que tengas archivos compartidos en la cuenta de Google o cambia a otra cuenta configurada en la TV.",
        fontSize = 14.sp,
        color = TvTextSecondary,
        modifier = Modifier.padding(horizontal = 48.dp),
        lineHeight = 20.sp
      )

      Spacer(modifier = Modifier.height(24.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        TvPrimaryButton(
          text = "Cambiar Cuenta Google",
          onClick = onOpenAccountDialog,
          testTag = "empty_btn_accounts"
        )

        TvSecondaryButton(
          text = "Actualizar",
          icon = Icons.Default.Refresh,
          onClick = onRefresh,
          testTag = "empty_btn_refresh"
        )
      }
    }
  }
}
