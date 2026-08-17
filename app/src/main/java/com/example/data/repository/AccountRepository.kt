package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.GoogleAccount
import com.example.data.network.DriveApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class AccountRepository(
  private val context: Context,
  private val apiService: DriveApiService = DriveApiService.create()
) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("drive_tv_accounts", Context.MODE_PRIVATE)

  private val _accounts = MutableStateFlow<List<GoogleAccount>>(emptyList())
  val accounts: StateFlow<List<GoogleAccount>> = _accounts.asStateFlow()

  private val _currentAccount = MutableStateFlow<GoogleAccount?>(null)
  val currentAccount: StateFlow<GoogleAccount?> = _currentAccount.asStateFlow()

  init {
    loadAccounts()
  }

  private fun loadAccounts() {
    val accountsJson = prefs.getString("saved_accounts", "[]") ?: "[]"
    val activeId = prefs.getString("active_account_id", null)
    val list = mutableListOf<GoogleAccount>()

    try {
      val array = JSONArray(accountsJson)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        val id = obj.getString("id")
        val account = GoogleAccount(
          id = id,
          email = obj.getString("email"),
          displayName = obj.optString("displayName", "Usuario"),
          avatarUrl = obj.optString("avatarUrl", null).takeIf { it?.isNotBlank() == true },
          accessToken = obj.getString("accessToken"),
          refreshToken = obj.optString("refreshToken", null).takeIf { it?.isNotBlank() == true },
          expiresAt = obj.optLong("expiresAt", 0L),
          isCurrent = id == activeId
        )
        list.add(account)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }

    if (list.isEmpty()) {
      // Create a default initial profile or demo setup for testing if desired
      val defaultDemo = GoogleAccount(
        id = "demo_account_1",
        email = "usuario.demo@gmail.com",
        displayName = "Mi Cuenta Google (TV)",
        avatarUrl = null,
        accessToken = "",
        refreshToken = null,
        expiresAt = System.currentTimeMillis() + 86400000L,
        isCurrent = true
      )
      list.add(defaultDemo)
    }

    _accounts.value = list
    val current = list.firstOrNull { it.id == activeId } ?: list.firstOrNull()
    _currentAccount.value = current
  }

  private fun saveAccounts(list: List<GoogleAccount>, activeId: String?) {
    try {
      val array = JSONArray()
      for (acc in list) {
        val obj = JSONObject().apply {
          put("id", acc.id)
          put("email", acc.email)
          put("displayName", acc.displayName)
          put("avatarUrl", acc.avatarUrl ?: "")
          put("accessToken", acc.accessToken)
          put("refreshToken", acc.refreshToken ?: "")
          put("expiresAt", acc.expiresAt)
        }
        array.put(obj)
      }
      prefs.edit()
        .putString("saved_accounts", array.toString())
        .putString("active_account_id", activeId)
        .apply()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  suspend fun switchAccount(accountId: String) {
    val list = _accounts.value.map {
      it.copy(isCurrent = it.id == accountId)
    }
    _accounts.value = list
    _currentAccount.value = list.firstOrNull { it.id == accountId }
    saveAccounts(list, accountId)
  }

  suspend fun addOrUpdateAccount(
    token: String,
    customEmail: String? = null,
    customName: String? = null,
    refreshToken: String? = null
  ): Result<GoogleAccount> = withContext(Dispatchers.IO) {
    try {
      var email = customEmail ?: "cuenta_${System.currentTimeMillis()}@gmail.com"
      var name = customName ?: "Usuario Google"
      var avatar: String? = null

      if (token.isNotBlank()) {
        try {
          val userInfo = apiService.getUserInfo("Bearer $token")
          if (!userInfo.email.isNullOrBlank()) {
            email = userInfo.email
          }
          if (!userInfo.name.isNullOrBlank()) {
            name = userInfo.name
          }
          avatar = userInfo.picture
        } catch (e: Exception) {
          // Fallback to provided info if userInfo call fails
        }
      }

      val existing = _accounts.value.firstOrNull { it.email.equals(email, ignoreCase = true) }
      val id = existing?.id ?: UUID.randomUUID().toString()

      val newAccount = GoogleAccount(
        id = id,
        email = email,
        displayName = name,
        avatarUrl = avatar,
        accessToken = token,
        refreshToken = refreshToken ?: existing?.refreshToken,
        expiresAt = System.currentTimeMillis() + (3600 * 1000),
        isCurrent = true
      )

      val updatedList = _accounts.value
        .filterNot { it.id == id }
        .map { it.copy(isCurrent = false) } + newAccount

      _accounts.value = updatedList
      _currentAccount.value = newAccount
      saveAccounts(updatedList, id)

      Result.success(newAccount)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun removeAccount(accountId: String) {
    val updatedList = _accounts.value.filterNot { it.id == accountId }
    val newActive = updatedList.firstOrNull()?.id
    val finalList = updatedList.map { it.copy(isCurrent = it.id == newActive) }
    _accounts.value = finalList
    _currentAccount.value = finalList.firstOrNull { it.id == newActive }
    saveAccounts(finalList, newActive)
  }
}
