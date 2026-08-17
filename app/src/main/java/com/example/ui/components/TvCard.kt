package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
import com.example.data.model.PlaybackProgress
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
fun DriveItemTvCard(
  item: DriveItem,
  progress: PlaybackProgress? = null,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isFocused by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(targetValue = if (isFocused) 1.06f else 1.0f, label = "tvCardScale")

  val borderStroke = if (isFocused) {
    BorderStroke(3.dp, TvFocusBorder)
  } else {
    BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
  }

  Card(
    modifier = modifier
      .scale(scale)
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .clickable { onClick() }
      .testTag("drive_item_${item.id}"),
    shape = RoundedCornerShape(16.dp),
    border = borderStroke,
    colors = CardDefaults.cardColors(
      containerColor = if (isFocused) TvSurfaceElevated else TvSurface
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (isFocused) 16.dp else 4.dp
    )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Thumbnail & Badges Container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
          .background(
            Brush.verticalGradient(
              listOf(TvSurfaceVariant, TvBackground)
            )
          )
      ) {
        if (!item.isFolder && !item.thumbnailLink.isNullOrBlank()) {
          AsyncImage(
            model = item.thumbnailLink,
            contentDescription = item.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else if (item.isFolder) {
          // Folder icon representation
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Folder,
              contentDescription = "Carpeta",
              tint = if (isFocused) TvPrimaryCyan else TvPrimaryBlue,
              modifier = Modifier.size(54.dp)
            )
          }
        } else {
          // Fallback Video Icon
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.VideoFile,
              contentDescription = "Video",
              tint = if (isFocused) TvPrimaryCyan else TvTextSecondary.copy(alpha = 0.6f),
              modifier = Modifier.size(48.dp)
            )
          }
        }

        // Overlay gradient for contrast
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.85f)
                ),
                startY = 60f
              )
            )
        )

        // Top Badges (Resolution / File Type)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (item.isFolder) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = TvPrimaryBlue.copy(alpha = 0.85f)
            ) {
              Text(
                text = "CARPETA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          } else {
            item.resolutionBadge?.let { res ->
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (res.contains("4K")) Color(0xFFE65100) else Color(0xFF0D47A1)
              ) {
                Text(
                  text = res,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            } ?: Spacer(modifier = Modifier.width(1.dp))
          }

          if (!item.isFolder && item.formattedSize.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = Color.Black.copy(alpha = 0.7f)
            ) {
              Text(
                text = item.formattedSize,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = TvTextPrimary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        // Bottom Right Duration
        if (!item.isFolder && item.durationFormatted.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(4.dp),
            color = Color.Black.copy(alpha = 0.8f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(8.dp)
          ) {
            Text(
              text = item.durationFormatted,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color.White,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
          }
        }

        // Play icon hover badge on focus
        if (isFocused && !item.isFolder) {
          Surface(
            shape = CircleShape,
            color = TvPrimaryCyan,
            modifier = Modifier
              .align(Alignment.Center)
              .size(44.dp)
              .shadow(12.dp, CircleShape)
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Reproducir",
              tint = TvBackground,
              modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
            )
          }
        }

        // Progress bar indicator if partially watched
        if (progress != null && progress.percentWatched > 0.02f) {
          LinearProgressIndicator(
            progress = { progress.percentWatched },
            modifier = Modifier
              .fillMaxWidth()
              .height(4.dp)
              .align(Alignment.BottomCenter),
            color = TvPrimaryCyan,
            trackColor = Color.White.copy(alpha = 0.2f)
          )
        }
      }

      // Title & Metadata
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        Text(
          text = item.name,
          color = if (isFocused) TvFocusBorder else TvTextPrimary,
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Owner / Shared Info
        val owner = item.owners?.firstOrNull()
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(
            imageVector = Icons.Default.Person,
            contentDescription = null,
            tint = TvTextSecondary,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = owner?.displayName ?: "Compartido conmigo",
            color = TvTextSecondary,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Normal
          )
        }
      }
    }
  }
}
