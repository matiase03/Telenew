package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TvBackground
import com.example.ui.theme.TvFocusBorder
import com.example.ui.theme.TvPrimaryBlue
import com.example.ui.theme.TvPrimaryCyan
import com.example.ui.theme.TvSurfaceElevated
import com.example.ui.theme.TvSurfaceVariant
import com.example.ui.theme.TvTextPrimary

@Composable
fun TvPrimaryButton(
  text: String,
  onClick: () -> Unit,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
  testTag: String = "tv_btn_primary",
  isDestructive: Boolean = false
) {
  var isFocused by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(targetValue = if (isFocused) 1.08f else 1.0f, label = "btnScale")

  val baseColor = if (isDestructive) Color(0xFFEF476F) else TvPrimaryCyan
  val contentColor = if (isFocused) TvBackground else TvBackground

  Button(
    onClick = onClick,
    modifier = modifier
      .scale(scale)
      .height(48.dp)
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = if (isFocused) baseColor else baseColor.copy(alpha = 0.85f),
      contentColor = contentColor
    ),
    border = if (isFocused) BorderStroke(3.dp, Color.White) else null,
    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(20.dp),
          tint = contentColor
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = contentColor
      )
    }
  }
}

@Composable
fun TvSecondaryButton(
  text: String,
  onClick: () -> Unit,
  icon: ImageVector? = null,
  modifier: Modifier = Modifier,
  testTag: String = "tv_btn_secondary"
) {
  var isFocused by remember { mutableStateOf(false) }
  val scale by animateFloatAsState(targetValue = if (isFocused) 1.08f else 1.0f, label = "btnSecScale")

  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .scale(scale)
      .height(48.dp)
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .testTag(testTag),
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = if (isFocused) TvSurfaceElevated else TvSurfaceVariant.copy(alpha = 0.5f),
      contentColor = if (isFocused) TvFocusBorder else TvTextPrimary
    ),
    border = BorderStroke(
      width = if (isFocused) 3.dp else 1.dp,
      color = if (isFocused) TvFocusBorder else Color.White.copy(alpha = 0.15f)
    ),
    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
          tint = if (isFocused) TvFocusBorder else TvTextPrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
      }
      Text(
        text = text,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isFocused) TvFocusBorder else TvTextPrimary
      )
    }
  }
}
