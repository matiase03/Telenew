package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
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
import kotlinx.coroutines.launch

@Composable
fun AccountSwitchDialog(
  accounts: List<GoogleAccount>,
  currentAccount: GoogleAccount?,
  onDismiss: () -> Unit,
  onSelectAccount: (String) -> Unit,
  onAddAccount: (token: String, email: String?, name: String?) -> Unit,
  onRemoveAccount: (String) -> Unit
) {
  var isAddingAccount by remember { mutableStateOf(false) }
  var tokenInput by remember { mutableStateOf("") }
  var emailInput by remember { mutableStateOf("") }
  var nameInput by remember { mutableStateOf("") }
  var isVerifying by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(24.dp),
      color = TvSurface,
      border = BorderStroke(2.dp, TvPrimaryBlue.copy(alpha = 0.5f)),
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .testTag("account_switch_dialog")
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AccountCircle,
              contentDescription = null,
              tint = TvPrimaryCyan,
              modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = if (isAddingAccount) "Añadir Cuenta de Google" else "Cuentas de Google en esta TV",
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = TvTextPrimary
            )
          }

          IconButton(
            onClick = {
              if (isAddingAccount) isAddingAccount = false else onDismiss()
            },
            modifier = Modifier.testTag("close_account_dialog")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Cerrar",
              tint = TvTextSecondary
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isAddingAccount) {
          Text(
            text = "Selecciona la cuenta para ver sus carpetas y videos de 'Compartido conmigo':",
            fontSize = 13.sp,
            color = TvTextSecondary
          )

          Spacer(modifier = Modifier.height(12.dp))

          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            items(accounts, key = { it.id }) { acc ->
              AccountItemRow(
                account = acc,
                isSelected = acc.id == currentAccount?.id,
                onSelect = {
                  onSelectAccount(acc.id)
                  onDismiss()
                },
                onRemove = { onRemoveAccount(acc.id) },
                canRemove = accounts.size > 1
              )
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          TvPrimaryButton(
            text = "+ Añadir otra cuenta de Google",
            icon = Icons.Default.Add,
            onClick = { isAddingAccount = true },
            modifier = Modifier.fillMaxWidth(),
            testTag = "btn_open_add_account"
          )
        } else {
          // Form to add account
          Column(modifier = Modifier.fillMaxWidth()) {
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = TvSurfaceVariant.copy(alpha = 0.7f),
              border = BorderStroke(1.dp, TvPrimaryCyan.copy(alpha = 0.3f)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.Info,
                  contentDescription = null,
                  tint = TvPrimaryCyan,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                  text = "Puedes ingresar el Token OAuth / Access Token de Google Drive de tu cuenta para sincronizar tus archivos compartidos.",
                  fontSize = 12.sp,
                  color = TvTextPrimary,
                  lineHeight = 16.sp
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
              value = nameInput,
              onValueChange = { nameInput = it },
              label = { Text("Nombre del perfil (ej. Matías TV)") },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TvTextPrimary,
                unfocusedTextColor = TvTextPrimary,
                focusedBorderColor = TvPrimaryCyan,
                unfocusedBorderColor = TvSurfaceVariant,
                focusedLabelColor = TvPrimaryCyan,
                unfocusedLabelColor = TvTextSecondary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_account_name")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = emailInput,
              onValueChange = { emailInput = it },
              label = { Text("Correo Gmail (ej. usuario@gmail.com)") },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TvTextPrimary,
                unfocusedTextColor = TvTextPrimary,
                focusedBorderColor = TvPrimaryCyan,
                unfocusedBorderColor = TvSurfaceVariant,
                focusedLabelColor = TvPrimaryCyan,
                unfocusedLabelColor = TvTextSecondary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_account_email")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
              value = tokenInput,
              onValueChange = { tokenInput = it },
              label = { Text("Token OAuth / Access Token de Google Drive (Opcional)") },
              singleLine = true,
              colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TvTextPrimary,
                unfocusedTextColor = TvTextPrimary,
                focusedBorderColor = TvPrimaryCyan,
                unfocusedBorderColor = TvSurfaceVariant,
                focusedLabelColor = TvPrimaryCyan,
                unfocusedLabelColor = TvTextSecondary
              ),
              modifier = Modifier
                .fillMaxWidth()
                .testTag("input_account_token")
            )

            errorMessage?.let { err ->
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = err, color = Color(0xFFEF476F), fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              TvSecondaryButton(
                text = "Volver",
                onClick = { isAddingAccount = false },
                modifier = Modifier.weight(1f)
              )

              TvPrimaryButton(
                text = "Guardar y Usar",
                icon = Icons.Default.Check,
                onClick = {
                  val displayName = nameInput.ifBlank { "Cuenta Google" }
                  val email = emailInput.ifBlank { "cuenta.google@gmail.com" }
                  onAddAccount(tokenInput.trim(), email.trim(), displayName.trim())
                  onDismiss()
                },
                modifier = Modifier.weight(1.5f),
                testTag = "btn_save_account"
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AccountItemRow(
  account: GoogleAccount,
  isSelected: Boolean,
  onSelect: () -> Unit,
  onRemove: () -> Unit,
  canRemove: Boolean
) {
  var isFocused by remember { mutableStateOf(false) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .onFocusChanged { isFocused = it.isFocused }
      .focusable()
      .clickable { onSelect() }
      .testTag("account_row_${account.id}"),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(
      width = if (isFocused) 3.dp else if (isSelected) 2.dp else 1.dp,
      color = if (isFocused) TvFocusBorder else if (isSelected) TvPrimaryCyan else Color.White.copy(alpha = 0.1f)
    ),
    colors = CardDefaults.cardColors(
      containerColor = if (isFocused) TvSurfaceElevated else if (isSelected) TvSurfaceVariant else TvSurface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        if (!account.avatarUrl.isNullOrBlank()) {
          AsyncImage(
            model = account.avatarUrl,
            contentDescription = account.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
          )
        } else {
          Surface(
            shape = CircleShape,
            color = if (isSelected) TvPrimaryCyan else TvPrimaryBlue,
            modifier = Modifier.size(40.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = account.displayName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TvBackground
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = account.displayName,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = if (isSelected) TvPrimaryCyan else TvTextPrimary
            )
            if (isSelected) {
              Spacer(modifier = Modifier.width(8.dp))
              Surface(
                shape = RoundedCornerShape(4.dp),
                color = TvPrimaryCyan.copy(alpha = 0.2f)
              ) {
                Text(
                  text = "ACTIVA",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = TvPrimaryCyan,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = account.email,
            fontSize = 12.sp,
            color = TvTextSecondary
          )
        }
      }

      if (canRemove) {
        IconButton(
          onClick = onRemove,
          modifier = Modifier.testTag("btn_remove_account_${account.id}")
        ) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Eliminar cuenta",
            tint = Color(0xFFEF476F).copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}
