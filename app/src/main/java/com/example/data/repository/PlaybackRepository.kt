package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.PlaybackProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

class PlaybackRepository(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("drive_tv_playback", Context.MODE_PRIVATE)

  private val _progressMap = MutableStateFlow<Map<String, PlaybackProgress>>(emptyMap())
  val progressMap: StateFlow<Map<String, PlaybackProgress>> = _progressMap.asStateFlow()

  init {
    loadProgress()
  }

  private fun loadProgress() {
    val all = prefs.all
    val map = mutableMapOf<String, PlaybackProgress>()
    for ((key, value) in all) {
      if (value is String) {
        try {
          val obj = JSONObject(value)
          val progress = PlaybackProgress(
            fileId = obj.getString("fileId"),
            fileName = obj.optString("fileName", ""),
            positionMs = obj.getLong("positionMs"),
            durationMs = obj.getLong("durationMs"),
            lastWatchedAt = obj.optLong("lastWatchedAt", System.currentTimeMillis())
          )
          map[key] = progress
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
    _progressMap.value = map
  }

  fun saveProgress(fileId: String, fileName: String, positionMs: Long, durationMs: Long) {
    if (positionMs <= 0L || durationMs <= 0L) return

    val progress = PlaybackProgress(
      fileId = fileId,
      fileName = fileName,
      positionMs = positionMs,
      durationMs = durationMs,
      lastWatchedAt = System.currentTimeMillis()
    )

    val currentMap = _progressMap.value.toMutableMap()
    currentMap[fileId] = progress
    _progressMap.value = currentMap

    val obj = JSONObject().apply {
      put("fileId", fileId)
      put("fileName", fileName)
      put("positionMs", positionMs)
      put("durationMs", durationMs)
      put("lastWatchedAt", progress.lastWatchedAt)
    }

    prefs.edit().putString(fileId, obj.toString()).apply()
  }

  fun getProgress(fileId: String): PlaybackProgress? {
    return _progressMap.value[fileId]
  }

  fun clearProgress(fileId: String) {
    val currentMap = _progressMap.value.toMutableMap()
    currentMap.remove(fileId)
    _progressMap.value = currentMap
    prefs.edit().remove(fileId).apply()
  }
}
