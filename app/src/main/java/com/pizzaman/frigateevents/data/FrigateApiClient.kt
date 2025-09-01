package com.pizzaman.frigateevents.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

class FrigateApiClient(
    var baseUrl: String,
    private val httpClient: OkHttpClient = OkHttpClient(),
) {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private var cachedVersion: String? = null

    suspend fun fetchEvents(
        camera: String? = null,
        label: String? = null,
        zone: String? = null,
        limit: Int? = null,
        inProgress: Boolean = false,
        sortBy: String? = null,
    ): List<FrigateEvent> = withContext(Dispatchers.IO) {
        val urlBuilder = ("$baseUrl/api/events").toHttpUrlOrNull()!!.newBuilder()
            .addQueryParameter("cameras", camera ?: "all")
            .addQueryParameter("labels", label ?: "all")
            .addQueryParameter("zones", zone ?: "all")
            .addQueryParameter("sub_labels", "all")
            .addQueryParameter("time_range", "00:00,24:00")
            .addQueryParameter("timezone", "America/New_York")
            .addQueryParameter("favorites", "0")
            .addQueryParameter("is_submitted", "-1")
            .addQueryParameter("include_thumbnails", "0")
            .addQueryParameter("in_progress", if (inProgress) "1" else "0")
            .addQueryParameter("limit", (limit ?: 50).toString())
        if (sortBy != null) urlBuilder.addQueryParameter("order_by", sortBy)

        val request = Request.Builder().url(urlBuilder.build()).build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) throw IllegalStateException("Invalid response: ${'$'}{response.code}")
        val body = response.body!!.string()

        // Try decode as list
        val listType = Types.newParameterizedType(List::class.java, FrigateEvent::class.java)
        val listAdapter = moshi.adapter<List<FrigateEvent>>(listType)
        listAdapter.fromJson(body) ?: run {
            // Try wrapped keys
            val map = moshi.adapter(Map::class.java).fromJson(body) as? Map<*, *>
            val keys = listOf("events", "data", "results")
            for (k in keys) {
                val arr = map?.get(k) as? List<*>
                if (arr != null) {
                    val json = moshi.adapter(Any::class.java).toJson(arr)
                    return@withContext moshi.adapter<List<FrigateEvent>>(listType).fromJson(json) ?: emptyList()
                }
            }
            emptyList()
        }
    } ?: emptyList()

    suspend fun fetchCameras(): List<String> = withContext(Dispatchers.IO) {
        try {
            println("🔧 FrigateApiClient.fetchCameras() - Making request to: ${'$'}baseUrl/api/config")
            val request = Request.Builder().url("${'$'}baseUrl/api/config").build()
            val response = httpClient.newCall(request).execute()
            
            if (!response.isSuccessful) {
                println("🔧 FrigateApiClient.fetchCameras() - Response not successful: ${'$'}{response.code}")
                return@withContext emptyList()
            }
            
            val body = response.body!!.string()
            println("🔧 FrigateApiClient.fetchCameras() - Response body length: ${'$'}{body.length}")
            
            val cameras = Regex("\\\"cameras\\\"\\s*:\\s*\\{(.*?)\\}", RegexOption.DOT_MATCHES_ALL)
                .find(body)
                ?.groupValues?.getOrNull(1)
                ?.let { inner ->
                    println("🔧 FrigateApiClient.fetchCameras() - Found cameras section: ${'$'}{inner.take(200)}...")
                    val cameraList = Regex("\\\"([^\\\"]+)\\\"\\s*:").findAll(inner).map { it.groupValues[1] }.toList().sorted()
                    println("🔧 FrigateApiClient.fetchCameras() - Extracted cameras: ${'$'}cameraList")
                    cameraList
                } ?: emptyList()
            
            println("🔧 FrigateApiClient.fetchCameras() - Returning cameras: ${'$'}cameras")
            return@withContext cameras
            
        } catch (e: Exception) {
            println("🔧 FrigateApiClient.fetchCameras() - Exception: ${'$'}{e.message}")
            e.printStackTrace()
            return@withContext emptyList()
        }
    }

    suspend fun fetchVersion(): String = withContext(Dispatchers.IO) {
        val request = Request.Builder().url("${'$'}baseUrl/api/version").build()
        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) throw IllegalStateException("Invalid response: ${'$'}{response.code}")
        val body = response.body!!.string().trim()
        val versionRegex = Regex("^\\d+\\.\\d+(\\.\\d+.*)?$")
        if (versionRegex.matches(body)) return@withContext body
        val keyRegex = Regex("\\\"version\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"")
        keyRegex.find(body)?.groupValues?.getOrNull(1) ?: body
    }.also { cachedVersion = it }

    private suspend fun getVersionOrDefault(): String = cachedVersion ?: runCatching { fetchVersion() }.getOrElse { "0.13.0" }

    suspend fun fetchAvailableLabels(limit: Int = 100): List<String> = withContext(Dispatchers.IO) {
        try {
            // Fetch recent events to extract available labels
            val events = fetchEvents(limit = limit)
            events.mapNotNull { it.label }.distinct().sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAvailableZones(limit: Int = 100): List<String> = withContext(Dispatchers.IO) {
        try {
            // Fetch recent events to extract available zones
            val events = fetchEvents(limit = limit)
            events.flatMap { it.zones }.distinct().sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun fetchAvailableCameras(limit: Int = 100): List<String> = withContext(Dispatchers.IO) {
        try {
            // Fetch recent events to extract available cameras
            val events = fetchEvents(limit = limit)
            events.map { it.camera }.distinct().sorted()
        } catch (e: Exception) {
            emptyList()
        }
    }


}


