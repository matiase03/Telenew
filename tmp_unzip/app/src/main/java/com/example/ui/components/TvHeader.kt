package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.GoogleAccount
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvPrimaryBlue
import com.example.ui.theme.TvPrimaryCyan
import com.example.ui.theme.TvSurface
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary
import com.example.ui.theme.TvTextSecondary
import com.example.ui.theme.TvTextTertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvHeader(
  currentAccount: GoogleAccount?,
  searchQuery: String,
  onSearchChange: (String) -> Unit,
  selectedFilter: String, // "ALL", "VIDEOS", "FOLDERS"
  onFilterChange: (String) -> Unit,
  onOpenAccountDialog: () -> Unit,
  onRefresh: () -> Unit,
  modifier: Modifier = Modifier
) {
  var isSearchExpanded by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(
        Brush.verticalGradient(
          colors = listOf(TvSurfaceVariant.copy(alpha = 0.9f), TvBackground.copy(alpha = 0.95f))
        )
      )
      .padding(horizontal = 24.dp, vertical = 14.dp)
  ) {
    // Top Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // App Branding
      Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = TvPrimaryCyan.copy(alpha = 0.15f),
          border = BorderStroke(1.5.dp, TvPrimaryCyan),
          modifier = Modifier.size(42.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Cloud,
              contentDescription = null,
              tint = TvPrimaryCyan,
              modifier = Modifier.size(24.dp)
            )
          }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "DRIVE TV",
              fontSize = 22.sp,
              fontWeight = FontWeight.Black,
              color = TvTextPrimary,
              letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(4.dp),
              color = TvPrimaryBlue
            ) {
              Text(
                text = "SHARED WITH ME",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
          Text(
            text = "Reproductor Rápido de Videos de Google Drive",
            fontSize = 12.sp,
            color = TvTextSecondary
          )
        }
      }

      // Actions: Refresh + Account Switcher Pill
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Refresh Button
        IconButton(
          onClick = onRefresh,
          modifier = Modifier
            .background(TvSurfaceVariant, CircleShape)
            .testTag("btn_refresh_drive")
        ) {
          Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Recargar",
            tint = TvPrimaryCyan
          )
        }

        // Account Selector TV Card / Pill
        var isAccountPillFocused by remember { mutableStateOf(false) }
        Surface(
          shape = RoundedCornerShape(20.dp),
          color = if (isAccountPillFocused) TvSurfaceElevated else TvSurfaceVariant,
          border = BorderStroke(
            if (isAccountPillFocused) 2.dp else 1.dp,
            if (isAccountPillFocused) TvFocusBorder else TvPrimaryCyan.copy(alpha = 0.3f)
          ),
          modifier = Modifier
            .onFocusChanged { isAccountPillFocused = it.isFocused }
            .focusable()
            .clickable { onOpenAccountDialog() }
            .testTag("btn_account_selector")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (!currentAccount?.avatarUrl.isNullOrBlank()) {
              AsyncImage(
                model = currentAccount?.avatarUrl,
                contentDescription = currentAccount?.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
              )
            } else {
              Surface(
                shape = CircleShape,
                color = TvPrimaryCyan,
                modifier = Modifier.size(28.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = (currentAccount?.displayName?.take(1) ?: "U").uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = TvBackground
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
              Text(
                text = currentAccount?.displayName ?: "Seleccionar Cuenta",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TvTextPrimary
              )
              Text(
                text = currentAccount?.email ?: "Cambiar cuenta",
                fontSize = 10.sp,
                color = TvTextSecondary
              )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
              imageVector = Icons.Default.SwitchAccount,
              contentDescription = "Cambiar cuenta",
              tint = TvPrimaryCyan,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Search and Filters Bar
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      // Search Bar
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Buscar en Compartido conmigo (ej. película, mp4, serie...)", fontSize = 13.sp) },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = TvPrimaryCyan,
            modifier = Modifier.size(20.dp)
          )
        },
        trailingIcon = {
          if (searchQuery.isNotBlank()) {
            IconButton(onClick = { onSearchChange("") }) {
              Icon(
                imageVector = Icons.Default.Clear,
                contentDescription = "Borrar",
                tint = TvTextSecondary
              )
            }
          }
        },
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
          focusedTextColor = TvTextPrimary,
          unfocusedTextColor = TvTextPrimary,
          focusedContainerColor = TvSurfaceElevated,
          unfocusedContainerColor = TvSurfaceVariant.copy(alpha = 0.6f),
          focusedBorderColor = TvPrimaryCyan,
          unfocusedBorderColor = Color.Transparent,
          focusedPlaceholderColor = TvTextSecondary,
          unfocusedPlaceholderColor = TvTextTertiary
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .weight(1.5f)
          .height(48.dp)
          .testTag("input_search_shared")
      )

      Spacer(modifier = Modifier.width(16.dp))

      // Filter Chips
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        FilterChipItem(
          label = "Todos",
          selected = selectedFilter == "ALL",
          onClick = { onFilterChange("ALL") },
          icon = Icons.Default.FilterList,
          testTag = "filter_all"
        )

        FilterChipItem(
          label = "Videos",
          selected = selectedFilter == "VIDEOS",
          onClick = { onFilterChange("VIDEOS") },
          icon = Icons.Default.VideoLibrary,
          testTag = "filter_videos"
        )

        FilterChipItem(
          label = "Carpetas",
          selected = selectedFilter == "FOLDERS",
          onClick = { onFilterChange("FOLDERS") },
          icon = Icons.Default.Folder,
          testTag = "filter_folders"
        )
      }
    }
  }
}

@Composable
fun FilterChipItem(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  testTag: String
) {
  var isFocused by remember { mutableStateOf(false) }

  Surface(
    shape = RoundedCornerShape(10.dp),
    color = if (isFocused) TvSurfaceElevated else if (selected) TvPrimaryBlue else TvSurfaceVariant.copy(alpha = 0.6f),
    border = BorderStroke(
      width = if (isFocused) 2.dp else if (selected) 1.dp else 1.dp,
      color = if (isFocused) TvFocusBorder else if (selected) TvPrimaryCyan else Color.White.copy(alpha = 0.1f)
    ),
    modifier = Modifier
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .clickable { onClick() }
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected || isFocused) TvPrimaryCyan else TvTextSecondary,
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = label,
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        color = if (selected || isFocused) Color.White else TvTextSecondary
      )
    }
  }
}
