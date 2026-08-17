package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.model.DriveItem
import com.example.data.model.PlaybackProgress
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvPrimaryCyan
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary

@Composable
fun MovieDetailsDialog(
  item: DriveItem,
  progress: PlaybackProgress?,
  onDismiss: () -> Unit,
  onPlay: (resume: Boolean) -> Unit
) {
  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = TvSurface,
      border = BorderStroke(2.dp, TvPrimaryCyan.copy(alpha = 0.4f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("movie_details_dialog")
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Hero Header with backdrop
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
              Brush.verticalGradient(
                listOf(TvSurfaceElevated, TvSurface)
              )
            )
        ) {
          if (!item.thumbnailLink.isNullOrBlank()) {
            AsyncImage(
              model = item.thumbnailLink,
              contentDescription = item.name,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )
          }

          // Dark gradient overlay
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  colors = listOf(
                    Color.Black.copy(alpha = 0.3f),
                    TvSurface
                  )
                )
              )
          )

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(12.dp)
              .testTag("close_details_dialog")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cerrar",
              tint = Color.White
            )
          }
        }

        // Details Content
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
          Text(
            text = item.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TvTextPrimary,
            lineHeight = 24.sp
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Metadata Chips Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            item.resolutionBadge?.let { res ->
              MetadataBadge(
                icon = Icons.Default.HighQuality,
                text = res,
                color = if (res.contains("4K")) Color(0xFFE65100) else Color(0xFF0D47A1)
              )
            }

            if (item.durationFormatted.isNotBlank()) {
              MetadataBadge(
                icon = Icons.Default.AccessTime,
                text = item.durationFormatted,
                color = TvSurfaceVariant
              )
            }

            if (item.formattedSize.isNotBlank()) {
              MetadataBadge(
                icon = Icons.Default.Storage,
                text = item.formattedSize,
                color = TvSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Owner info
          val owner = item.owners?.firstOrNull()
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = null,
              tint = TvPrimaryCyan,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Compartido por: ${owner?.displayName ?: "Google Drive"}${if (owner?.emailAddress != null) " (${owner.emailAddress})" else ""}",
              fontSize = 13.sp,
              color = TvTextSecondary
            )
          }

          if (!item.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = item.description,
              fontSize = 13.sp,
              color = TvTextSecondary,
              lineHeight = 18.sp
            )
          }

          // Progress resume info
          if (progress != null && progress.percentWatched > 0.05f) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
              val resumeSeconds = progress.positionMs / 1000
              val resumeMin = resumeSeconds / 60
              val resumeSec = resumeSeconds % 60
              val resumeTimeStr = String.format("%02d:%02d", resumeMin, resumeSec)

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "Progreso guardado: $resumeTimeStr",
                  fontSize = 12.sp,
                  color = TvPrimaryCyan,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "${(progress.percentWatched * 100).toInt()}% visto",
                  fontSize = 12.sp,
                  color = TvTextSecondary
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              LinearProgressIndicator(
                progress = { progress.percentWatched },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = TvPrimaryCyan,
                trackColor = TvSurfaceVariant
              )
            }
          }

          Spacer(modifier = Modifier.height(24.dp))

          // Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            if (progress != null && progress.percentWatched > 0.05f) {
              TvPrimaryButton(
                text = "Continuar viendo",
                icon = Icons.Default.PlayArrow,
                onClick = { onPlay(true) },
                modifier = Modifier.weight(1.2f),
                testTag = "btn_resume_video"
              )

              TvSecondaryButton(
                text = "Desde el inicio",
                icon = Icons.Default.Replay,
                onClick = { onPlay(false) },
                modifier = Modifier.weight(1f),
                testTag = "btn_play_from_start"
              )
            } else {
              TvPrimaryButton(
                text = "Reproducir Video",
                icon = Icons.Default.PlayArrow,
                onClick = { onPlay(false) },
                modifier = Modifier.fillMaxWidth(),
                testTag = "btn_play_video"
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
fun MetadataBadge(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  text: String,
  color: Color
) {
  Surface(
    shape = RoundedCornerShape(6.dp),
    color = color
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = Color.White,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White
      )
    }
  }
}
