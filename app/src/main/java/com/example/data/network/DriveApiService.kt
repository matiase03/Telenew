package com.example.data.network

import com.example.data.model.DriveFileListResponse
import com.squareup.moshi.JsonClass
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface DriveApiService {

  @GET("drive/v3/files")
  suspend fun listFiles(
    @Header("Authorization") authorization: String,
    @Query("q") query: String,
    @Query("fields") fields: String = "nextPageToken, files(id, name, mimeType, size, thumbnailLink, iconLink, webContentLink, modifiedTime, sharedWithMeTime, owners, videoMediaMetadata, description)",
    @Query("pageSize") pageSize: Int = 100,
    @Query("orderBy") orderBy: String? = "sharedWithMeTime desc, modifiedTime desc",
    @Query("supportsAllDrives") supportsAllDrives: Boolean = true,
    @Query("includeItemsFromAllDrives") includeItemsFromAllDrives: Boolean = true,
    @Query("pageToken") pageToken: String? = null
  ): DriveFileListResponse

  @GET("oauth2/v3/userinfo")
  suspend fun getUserInfo(
    @Header("Authorization") authorization: String
  ): GoogleUserInfoResponse

  companion object {
    private const val BASE_URL = "https://www.googleapis.com/"

    fun create(): DriveApiService {
      val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
      }

      val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

      return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()
        .create(DriveApiService::class.java)
    }
  }
}

@JsonClass(generateAdapter = true)
data class GoogleUserInfoResponse(
  val sub: String? = null,
  val name: String? = null,
  val given_name: String? = null,
  val family_name: String? = null,
  val picture: String? = null,
  val email: String? = null,
  val email_verified: Boolean? = null
)
