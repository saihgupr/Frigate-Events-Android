package com.pizzaman.frigateevents

import android.os.Bundle
import androidx.activity.viewModels
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
 
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.ripple.rememberRipple

import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusTarget
import androidx.compose.material3.Divider
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.key.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LifecycleOwner
import com.pizzaman.frigateevents.data.FrigateApiClient
import com.pizzaman.frigateevents.data.FrigateEvent
import com.pizzaman.frigateevents.data.SettingsStore
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import coil.compose.AsyncImage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.viewinterop.AndroidView

import androidx.activity.compose.BackHandler
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.text.style.TextAlign
import java.io.File
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import android.content.pm.PackageManager


@Composable
fun FrigateTheme(content: @Composable () -> Unit) {
    val colors = darkColorScheme(
        primary = ComposeColor.Gray,
        onPrimary = ComposeColor.White,
        background = ComposeColor.Black,
        onBackground = ComposeColor.White,
        surface = Color(0xFF1A1A1A),
        onSurface = ComposeColor.White,
        secondary = Color(0xFF404040),
        onSecondary = ComposeColor.White
    )
    MaterialTheme(colorScheme = colors, typography = Typography(), content = content)
}

fun String.toFriendlyName(): String {
    return this.replace("_", " ")
        .split(" ")
        .joinToString(" ") { word ->
            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}

fun formatTime(milliseconds: Long): String {
    val totalSeconds = milliseconds / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

// Persistent storage utilities for IP address
object PersistentStorage {
    private const val CONFIG_FILE = "frigate_config.json"
    private const val BASE_URL_KEY = "base_url"

    fun saveBaseUrl(context: Context, baseUrl: String) {
        try {
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir != null) {
                val configFile = File(externalDir, CONFIG_FILE)
                val json = """{"$BASE_URL_KEY":"$baseUrl"}"""
                configFile.writeText(json)
                println("💾 Saved IP address to persistent storage: $baseUrl")
            }
        } catch (e: Exception) {
            println("❌ Failed to save IP address to persistent storage: ${e.message}")
        }
    }

    fun loadBaseUrl(context: Context): String? {
        return try {
            val externalDir = context.getExternalFilesDir(null)
            if (externalDir != null) {
                val configFile = File(externalDir, CONFIG_FILE)
                if (configFile.exists()) {
                    val json = configFile.readText()
                    // Simple JSON parsing for base_url
                    val regex = "\"$BASE_URL_KEY\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                    val match = regex.find(json)
                    val url = match?.groupValues?.get(1)
                    if (url != null) {
                        println("📂 Loaded IP address from persistent storage: $url")
                    }
                    url
                } else {
                    println("📂 No persistent config file found")
                    null
                }
            } else {
                println("❌ External storage not available")
                null
            }
        } catch (e: Exception) {
            println("❌ Failed to load IP address from persistent storage: ${e.message}")
            null
        }
    }
}

fun FrigateEvent.toFriendlyLabelName(): String = label?.toFriendlyName() ?: "Unknown"

fun FrigateEvent.toFriendlyCameraName(): String = camera.toFriendlyName()

fun FrigateEvent.toFriendlyZoneNames(): String = zones.joinToString(", ") { it.toFriendlyName() }

// Function to detect if the device is an Android TV
fun isAndroidTV(context: Context): Boolean {
    val packageManager = context.packageManager
    return packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}

fun FrigateEvent.formatStartTime(): String {
    val date = Date((start_time * 1000).toLong())
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}

fun FrigateEvent.formatDuration(): String {
    val duration = durationSeconds ?: return "In Progress"
    val hours = (duration / 3600).toInt()
    val minutes = ((duration % 3600) / 60).toInt()
    val seconds = (duration % 60).toInt()
    return when {
        hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
        minutes > 0 -> "${minutes}m ${seconds}s"
        else -> "${seconds}s"
    }
}

class MainActivity : ComponentActivity() {
    private val settingsStore by lazy { SettingsStore(applicationContext) }
    private val viewModel by lazy {
        ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return EventsViewModel(settingsStore) as T
                }
            }
        )[EventsViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge and immersive mode for fullscreen experience
        enableEdgeToEdge()
        
        // Add lifecycle observer to detect when app comes to foreground
        // ProcessLifecycleOwner works better on TV, Activity lifecycle works better on mobile
        ProcessLifecycleOwner.get().lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    // App came to foreground - trigger silent refresh
                    println("🔄 ProcessLifecycle - App came to foreground - triggering silent refresh")
                    android.util.Log.d("FrigateEvents", "🔄 ProcessLifecycle - App came to foreground - triggering silent refresh")
                    viewModel.refreshSilently()
                }
                Lifecycle.Event.ON_STOP -> {
                    println("📱 ProcessLifecycle - App went to background")
                    android.util.Log.d("FrigateEvents", "📱 ProcessLifecycle - App went to background")
                }
                else -> { 
                    println("📱 ProcessLifecycle event: $event")
                    android.util.Log.d("FrigateEvents", "📱 ProcessLifecycle event: $event")
                }
            }
        })
        
        // Also add Activity lifecycle observer for mobile devices
        lifecycle.addObserver(LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    // Activity resumed - trigger silent refresh
                    println("🔄 ActivityLifecycle - Activity resumed - triggering silent refresh")
                    android.util.Log.d("FrigateEvents", "🔄 ActivityLifecycle - Activity resumed - triggering silent refresh")
                    viewModel.refreshSilently()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    println("📱 ActivityLifecycle - Activity paused")
                    android.util.Log.d("FrigateEvents", "📱 ActivityLifecycle - Activity paused")
                }
                else -> { 
                    println("📱 ActivityLifecycle event: $event")
                    android.util.Log.d("FrigateEvents", "📱 ActivityLifecycle event: $event")
                }
            }
        })
        
        setContent {
            FrigateTheme { 
                EventsScreen(viewModel)
            }
        }
    }
}

class EventsViewModel(
    private val settingsStore: SettingsStore
) : ViewModel() {
    var baseUrl by mutableStateOf("")
        private set
    var events by mutableStateOf<List<FrigateEvent>>(emptyList())
        private set
    var filteredEvents by mutableStateOf<List<FrigateEvent>>(emptyList())
        private set
    var inProgressEvents by mutableStateOf<List<FrigateEvent>>(emptyList())
        private set
    var filteredInProgressEvents by mutableStateOf<List<FrigateEvent>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var selectedEvent by mutableStateOf<FrigateEvent?>(null)
        private set
    var showSettings by mutableStateOf(false)
        private set


    // Track when we last refreshed to avoid too frequent refreshes
    private var lastRefreshTime = 0L
    private var lastInProgressRefreshTime = 0L
    private val REFRESH_INTERVAL_MS = 30_000L // 30 seconds minimum between refreshes
    private val IN_PROGRESS_REFRESH_INTERVAL_MS = 2_000L // 2 seconds for in-progress events

    private var client = FrigateApiClient(baseUrl)

    init {
        // Sync base URL with settings store
        baseUrl = settingsStore.frigateBaseURL
        client.baseUrl = baseUrl

        // Apply initial filters
        applyFilters()

        // Auto-refresh on app start to populate filters
        if (baseUrl.isNotBlank()) {
            refresh()
        }
    }

    fun updateBaseUrl(url: String) {
        baseUrl = url
        client.baseUrl = url
        settingsStore.updateFrigateBaseURL(url)
    }

    // Apply filters to events
    private fun applyFilters() {
        val selectedLabels = settingsStore.selectedLabels.value
        val selectedZones = settingsStore.selectedZones.value
        val selectedCameras = settingsStore.selectedCameras.value

        // Filter completed events
        filteredEvents = if (selectedLabels.isEmpty() && selectedZones.isEmpty() && selectedCameras.isEmpty()) {
            events
        } else {
            events.filter { event ->
                val labelMatch = selectedLabels.isEmpty() || (event.label != null && selectedLabels.contains(event.label))
                val zoneMatch = selectedZones.isEmpty() || event.zones.any { selectedZones.contains(it) }
                val cameraMatch = selectedCameras.isEmpty() || selectedCameras.contains(event.camera)
                labelMatch && zoneMatch && cameraMatch
            }
        }

        // Filter in-progress events
        filteredInProgressEvents = if (selectedLabels.isEmpty() && selectedZones.isEmpty() && selectedCameras.isEmpty()) {
            inProgressEvents
        } else {
            inProgressEvents.filter { event ->
                val labelMatch = selectedLabels.isEmpty() || (event.label != null && selectedLabels.contains(event.label))
                val zoneMatch = selectedZones.isEmpty() || event.zones.any { selectedZones.contains(it) }
                val cameraMatch = selectedCameras.isEmpty() || selectedCameras.contains(event.camera)
                labelMatch && zoneMatch && cameraMatch
            }
        }
    }

    // Refresh available filters from server (accumulate, don't replace)
    fun refreshFilters() {
        if (baseUrl.isBlank()) return

        viewModelScope.launch {
            try {
                println("🔍 Starting to refresh filters from: $baseUrl")

                // Fetch events and extract labels/zones
                val events = client.fetchEvents(limit = 100)
                val labels = events.mapNotNull { it.label }.distinct()
                val zones = events.flatMap { it.zones }.distinct()

                println("📊 Found ${events.size} events")
                println("🏷️ Found ${labels.size} labels: $labels")
                println("🏠 Found ${zones.size} zones: $zones")

                // Update accumulated filters
                settingsStore.updateAvailableLabels(labels)
                settingsStore.updateAvailableZones(zones)

                // Also fetch cameras from events (more reliable than config endpoint)
                val cameras = client.fetchAvailableCameras(100)
                println("📹 Found ${cameras.size} cameras: $cameras")
                settingsStore.updateAvailableCameras(cameras)

                println("✅ Filters successfully updated and accumulated")

            } catch (e: Exception) {
                println("❌ Failed to refresh filters: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun refresh() {
        if (baseUrl.isBlank()) {
            error = "Enter base URL"
            return
        }
        isLoading = true
        error = null

        viewModelScope.launch {
            runCatching { client.fetchEvents() }
                .onSuccess {
                    events = it
                    applyFilters()
                    refreshFilters() // Also refresh available filters
                }
                .onFailure { error = it.message }
            isLoading = false
        }
        lastRefreshTime = System.currentTimeMillis()
    }

    // Silent refresh for background updates (no loading spinner)
    fun refreshSilently() {
        if (baseUrl.isBlank()) {
            return
        }

        println("🔄 Attempting silent refresh to: $baseUrl")
        viewModelScope.launch {
            runCatching { client.fetchEvents() }
                .onSuccess {
                    events = it
                    applyFilters()
                    println("✅ Silent refresh successful - got ${it.size} events")
                }
                .onFailure {
                    // Only log errors for silent refreshes, don't show to user
                    println("❌ Silent refresh failed: ${it.message}")
                    android.util.Log.e("FrigateEvents", "Silent refresh failed: ${it.message}")
                }
        }
        lastRefreshTime = System.currentTimeMillis()
    }

    fun refreshIfNeeded(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastRefreshTime

        if (baseUrl.isBlank()) {
            return false // Can't refresh without URL
        }

        if (timeSinceLastRefresh > REFRESH_INTERVAL_MS) {
            refreshSilently()
            return true
        }

        return false
    }

    fun refreshInProgressIfNeeded(): Boolean {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastRefresh = currentTime - lastInProgressRefreshTime

        if (baseUrl.isBlank()) {
            return false // Can't refresh without URL
        }

        if (timeSinceLastRefresh > IN_PROGRESS_REFRESH_INTERVAL_MS && !isLoading) {
            refreshInProgressEvents()
            return true
        }

        return false
    }

    private fun refreshInProgressEvents() {
        if (baseUrl.isBlank()) return

        viewModelScope.launch {
            try {
                // Get previous in-progress event IDs to detect finished events
                val previousInProgressIds = inProgressEvents.map { it.id }.toSet()

                // Fetch current in-progress events
                val currentInProgressEvents = client.fetchEvents(inProgress = true)
                inProgressEvents = currentInProgressEvents
                applyFilters() // Apply filters to in-progress events

                // Check if any in-progress events have finished
                val currentInProgressIds = currentInProgressEvents.map { it.id }.toSet()
                val finishedEventIds = previousInProgressIds - currentInProgressIds

                // If events finished, silently refresh main events list to get the completed events
                if (finishedEventIds.isNotEmpty()) {
                    refreshSilently()
                }

                lastInProgressRefreshTime = System.currentTimeMillis()
            } catch (e: Exception) {
                // Don't show error for background in-progress polling
                println("Background in-progress refresh failed: ${e.message}")
            }
        }
    }

    // Public method to reapply filters when settings change
    fun reapplyFilters() {
        applyFilters()
    }

    // Get settings store for settings screen
    fun getSettingsStore(): SettingsStore = settingsStore

    fun openDetail(event: FrigateEvent) {
        selectedEvent = event
    }

    fun closeDetail() {
        selectedEvent = null
    }

    fun openSettings() { showSettings = true }
    fun closeSettings() { showSettings = false }
}

@Composable
fun EventsScreen(vm: EventsViewModel) {
    var urlField by remember {
        mutableStateOf(
            TextFieldValue(
                if (vm.baseUrl.isNotBlank()) vm.baseUrl
                else "http://192.168.1.168:5000"
            )
        )
    }
    val context = LocalContext.current
    val prefs = remember(context) { context.getSharedPreferences("frigate_prefs", Context.MODE_PRIVATE) }

    // Auto-refresh on app start - load from persistent storage first
    LaunchedEffect(Unit) {
        // First try to load from persistent storage (survives app reinstalls)
        val persistentUrl = PersistentStorage.loadBaseUrl(context)
        val stored = persistentUrl ?: prefs.getString("base_url", null)

        if (!stored.isNullOrBlank()) {
            vm.updateBaseUrl(stored)
            urlField = TextFieldValue(stored)
            vm.refresh()
            println("🔄 Loaded Frigate URL: $stored (from ${if (persistentUrl != null) "persistent storage" else "SharedPreferences"})")
        } else {
            println("📝 No saved Frigate URL found - user needs to configure")
        }
    }

    // Auto-refresh when app comes to foreground (simple approach)
    LaunchedEffect(vm.baseUrl) {
        // When URL changes, reset the refresh timer
        if (!vm.baseUrl.isBlank()) {
            vm.refreshIfNeeded()
        }
    }

    // Periodic refresh checks
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2_000L) // Check every 2 seconds
            vm.refreshInProgressIfNeeded()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000L) // Check every 30 seconds
            vm.refreshIfNeeded()
        }
    }



    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets(0)), 
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            vm.selectedEvent != null -> {
                BackHandler(enabled = true) { vm.closeDetail() }
                VideoPlayerScreen(vm)
                return@Surface
            }
            vm.showSettings -> {
                BackHandler(enabled = true) { vm.closeSettings() }
                SettingsScreen(vm, urlField, onUrlChange = {
                    urlField = it
                    // Apply URL change immediately
                    val newUrl = it.text
                    vm.updateBaseUrl(newUrl)
                    vm.getSettingsStore().updateFrigateBaseURL(newUrl)
                    prefs.edit().putString("base_url", newUrl).apply()
                    PersistentStorage.saveBaseUrl(context, newUrl)
                }, onSave = {
                    vm.closeSettings()
                })
                return@Surface
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0d0d0d))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Spacer(Modifier.weight(1f))
                Text(
                    "Frigate Events",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = { vm.openSettings() },
                    modifier = Modifier.onKeyEvent { event ->
                        if (event.key == Key.DirectionUp && event.type == KeyEventType.KeyDown) {
                            vm.refresh()
                            true
                        } else {
                            false
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = "Settings (Press UP to refresh)",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // First-time setup notification (moved below header)
            if (vm.baseUrl.isBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .border(
                            width = 1.dp,
                            color = Color(0xFF404040),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1a1a1a)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Welcome to Frigate Events!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Text(
                            text = "To get started, please configure your Frigate server URL in Settings.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(
                            onClick = { vm.openSettings() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Go to Settings")
                        }
                    }
                }
            }

            if (vm.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Loading events...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            } else if (vm.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Unable to connect to Frigate server. Please check your URL and try again.", color = Color.Gray, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(onClick = { vm.refresh() }) {
                            Text("Try Again")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    // Show in-progress events first with red borders
                    items(vm.filteredInProgressEvents) { e ->
                        var isFocused by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(120.dp) // Height for smaller images
                                .clickable { vm.openDetail(e) }
                                .onFocusChanged { isFocused = it.isFocused }
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) Color(0xFF0066CC) else Color(0xFF1a1a1a),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFocused) Color(0xFF0066CC) else Color(0xFF0d0d0d)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = e.thumbnailUrl(vm.baseUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(105.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    // "In Progress" indicator
                                    Text(
                                        "In Progress",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ComposeColor.Red,
                                        fontWeight = FontWeight.Bold
                                    )

                                                                        // Object label on its own line
                                    Text(
                                        e.toFriendlyLabelName(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp
                                    )

                                    // Camera name on its own line
                                    Text(
                                        e.toFriendlyCameraName(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 14.sp
                                    )

                                    // Time on its own line
                                    Text(
                                        e.formatStartTime(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 12.sp
                                    )

                                    // Duration on its own line
                                    Text(
                                        "Duration: ${e.formatDuration()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 12.sp
                                    )

                                    // Zones on their own line (if any)
                                    if (e.zones.isNotEmpty()) {
                                        Text(
                                            e.toFriendlyZoneNames(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 12.sp
                                        )
                                }
                            }
                        }
                    }
                }

                    // Show completed events
                    items(vm.filteredEvents) { e ->
                        var isFocused by remember { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .height(120.dp) // Height for smaller images
                                .clickable { vm.openDetail(e) }
                                .onFocusChanged { isFocused = it.isFocused }
                                .border(
                                    width = if (isFocused) 2.dp else 1.dp,
                                    color = if (isFocused) Color(0xFF0066CC) else Color(0xFF1a1a1a),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isFocused) Color(0xFF0066CC) else Color(0xFF0d0d0d)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(
                                    model = e.thumbnailUrl(vm.baseUrl),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(105.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                )
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    // Object label on its own line
                                    Text(
                                        e.toFriendlyLabelName(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Bold,
                                        lineHeight = 16.sp
                                    )

                                    // Camera name on its own line
                                    Text(
                                        e.toFriendlyCameraName(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 14.sp
                                    )

                                    // Time on its own line
                                    Text(
                                        e.formatStartTime(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 12.sp
                                    )

                                    // Duration on its own line
                                    Text(
                                        "Duration: ${e.formatDuration()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 12.sp
                                    )

                                    // Zones on their own line (if any)
                                    if (e.zones.isNotEmpty()) {
                                        Text(
                                            e.toFriendlyZoneNames(),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
@OptIn(UnstableApi::class)
fun VideoPlayerScreen(vm: EventsViewModel) {
    val e = vm.selectedEvent ?: return
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var currentUrlIndex by remember { mutableStateOf(0) }
    var hasTriedAllFormats by remember { mutableStateOf(false) }


    val videoUrls = remember(vm.baseUrl) {
        e.getAllVideoUrls(vm.baseUrl)
    }

    val currentVideoUrl = videoUrls.getOrNull(currentUrlIndex)

    fun tryNextUrl() {
        if (currentUrlIndex < videoUrls.size - 1) {
            currentUrlIndex++
            if (currentUrlIndex == videoUrls.size - 1) {
                hasTriedAllFormats = true
            }
            isLoading = true
            errorMessage = null
        } else {
            hasTriedAllFormats = true
        }
    }

    LaunchedEffect(currentVideoUrl) {
        if (currentVideoUrl != null) {
            try {
                android.net.Uri.parse(currentVideoUrl)
                    isLoading = false
                    errorMessage = null
            } catch (e: Exception) {
                errorMessage = "Invalid video URL"
                    isLoading = false
            }
        } else {
            errorMessage = "No valid video URLs found"
            isLoading = false
        }
    }

    // Clean, minimal video player focused on content
    Surface(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets(0)), color = ComposeColor.Black) {
                when {
                    isLoading && currentVideoUrl != null -> {
                // Minimal loading indicator
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = ComposeColor.White,
                        modifier = Modifier.size(48.dp)
                            )
                        }
                    }

                    errorMessage != null -> {
                // Clean error state with minimal UI
                Box(
                            modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor.Black.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                            imageVector = Icons.Default.Error,
                                contentDescription = "Error",
                            tint = ComposeColor.Red,
                            modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                            "Video unavailable",
                                style = MaterialTheme.typography.headlineSmall,
                            color = ComposeColor.White
                        )
                                if (!hasTriedAllFormats) {
                            Spacer(modifier = Modifier.height(24.dp))
                            OutlinedButton(
                                onClick = { tryNextUrl() },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = ComposeColor.White,
                                    containerColor = ComposeColor.Transparent
                                ),
                                border = BorderStroke(1.dp, ComposeColor.White)
                            ) {
                                Text("Try again")
                            }
                        }
                            }
                        }
                    }

                    currentVideoUrl != null -> {
                // Clean, minimal video player
            Box(modifier = Modifier.fillMaxSize()) {
                    // Main video player
                        AndroidView(
                            modifier = Modifier.fillMaxSize(),
                            factory = { context ->
                            val playerView = PlayerView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                useController = false  // No UI controls
                                controllerAutoShow = false
                            }

                            val player = ExoPlayer.Builder(context).build()
                                playerView.player = player
                                playerView
                            },
                        update = { playerView ->
                            // Update player when video URL changes
                            playerView.player?.let { player ->
                                player.stop()
                                player.clearMediaItems()
                                player.setMediaItem(MediaItem.fromUri(currentVideoUrl))
                                player.prepare()
                                player.playWhenReady = true
                            }
                        },
                        onRelease = { view ->
                            (view as? PlayerView)?.player?.release()
                        }
                    )

                    // Exit button - visible on mobile/tablet, transparent on TV
                    IconButton(
                        onClick = { vm.closeDetail() },
                        colors = if (isAndroidTV(context)) {
                            IconButtonDefaults.iconButtonColors(
                                contentColor = Color.Transparent,
                                containerColor = Color.Transparent
                            )
                        } else {
                            IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White,
                                containerColor = Color(0x400d0d0d)
                            )
                        },
                    modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(
                                top = if (isAndroidTV(context)) 16.dp else 8.dp,
                                end = 16.dp
                            )
                            .size(44.dp)
                            .then(
                                if (isAndroidTV(context)) {
                                    Modifier
                                } else {
                                    Modifier.background(
                                        Color(0x400d0d0d),
                                        CircleShape
                                    )
                                }
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Video",
                            tint = if (isAndroidTV(context)) Color.Transparent else Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                    }

                    else -> {
                // No video available
                Box(
                            modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeColor.Black.copy(alpha = 0.9f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.VideocamOff,
                            contentDescription = "No video",
                            tint = ComposeColor.White,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Video not available",
                                style = MaterialTheme.typography.headlineSmall,
                            color = ComposeColor.White
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        OutlinedButton(
                            onClick = { vm.closeDetail() },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = ComposeColor.White,
                                containerColor = ComposeColor.Transparent
                            ),
                            border = BorderStroke(1.dp, ComposeColor.White)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    vm: EventsViewModel,
    urlField: TextFieldValue,
    onUrlChange: (TextFieldValue) -> Unit,
    onSave: () -> Unit,
) {
    val settingsStore = vm.getSettingsStore()
    val context = LocalContext.current
    val isTV = isAndroidTV(context)

    // Collect state flows
    val frigateVersion by remember { derivedStateOf { settingsStore.frigateVersion } }
    val availableLabels by settingsStore.availableLabels.collectAsState()
    val selectedLabels by settingsStore.selectedLabels.collectAsState()
    val availableZones by settingsStore.availableZones.collectAsState()
    val selectedZones by settingsStore.selectedZones.collectAsState()
    val availableCameras by settingsStore.availableCameras.collectAsState()
    val selectedCameras by settingsStore.selectedCameras.collectAsState()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets(0)),
        color = Color.Black
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header - matching main menu style (full bleed)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0d0d0d))
                    .padding(horizontal = 24.dp, vertical = 8.dp)
            ) {
                Spacer(Modifier.weight(1f))
                            Text(
                    "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.weight(1f))
                // Always show exit button for consistent layout, but make it transparent on TV
                IconButton(
                    onClick = { vm.closeSettings() },
                    modifier = Modifier.background(
                        if (isTV) Color.Transparent else Color(0x400d0d0d), // Transparent on TV, visible on mobile/tablet
                        CircleShape
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Settings",
                        tint = if (isTV) Color.Transparent else Color.White.copy(alpha = 0.9f), // Transparent on TV, visible on mobile/tablet
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            // Single container for all settings
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    Spacer(Modifier.height(12.dp))
                }

                // URL Input Section at the top
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .background(Color(0xFF0d0d0d), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "FRIGATE SERVER",
                            style = MaterialTheme.typography.titleLarge, // Same size as filter titles
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)
                        )

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 9.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0a0a0a),
                            border = BorderStroke(1.dp, Color(0xFF404040))
                        ) {
                            BasicTextField(
                                value = urlField,
                                onValueChange = onUrlChange,
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                decorationBox = { innerTextField ->
                                    if (urlField.text.isEmpty()) {
                                        Text(
                                            text = "http://192.168.1.100:5000",
                                            color = Color(0xFF666666), // Darker placeholder
                                            fontSize = 16.sp
                                        )
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                }

                item {
                    // FILTERS Section - Separate Box
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .background(Color(0xFF0d0d0d), RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            "LABEL FILTER",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)
                        )

                // Dark background container for label items
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Full width (back to original)
                        .padding(horizontal = 9.dp, vertical = 8.dp)
                        .background(
                            Color(0xFF0a0a0a),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1a1a1a),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp)
                ) {
                    if (availableLabels.isEmpty()) {
                        Text(
                            "No labels found in recent events.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 9.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                        )
                    } else {
                        availableLabels.forEachIndexed { index, label ->
                            val isSelected = selectedLabels.contains(label)
                            val isLastItem = index == availableLabels.size - 1
                            FilterRow(
                                label = label.toFriendlyName(),
                                isSelected = isSelected,
                                onToggle = { settingsStore.toggleLabel(label) },
                                showDivider = !isLastItem
                            )
                        }
                    }
                }

                Text(
                    "ZONE FILTER",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)
                )

                // Dark background container for zone items
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Full width (back to original)
                        .padding(horizontal = 9.dp, vertical = 8.dp)
                        .background(
                            Color(0xFF0a0a0a),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1a1a1a),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp)
                ) {
                    if (availableZones.isEmpty()) {
                        Text(
                            "No zones found in recent events.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 9.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                        )
                    } else {
                        availableZones.forEachIndexed { index, zone ->
                            val isSelected = selectedZones.contains(zone)
                            val isLastItem = index == availableZones.size - 1
                            FilterRow(
                                label = zone.toFriendlyName(),
                                isSelected = isSelected,
                                onToggle = { settingsStore.toggleZone(zone) },
                                showDivider = !isLastItem
                            )
                        }
                    }
                }

                // CAMERA FILTER Section
                Text(
                    "CAMERA FILTER",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 8.dp)
                )

                // Dark background container for camera items
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Full width (back to original)
                        .padding(horizontal = 9.dp, vertical = 8.dp)
                        .background(
                            Color(0xFF0a0a0a),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = Color(0xFF1a1a1a),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp)
                ) {
                    if (availableCameras.isEmpty()) {
                        Text(
                            "No cameras found. Pull to refresh on the main screen to populate this list.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 9.dp, end = 0.dp, top = 8.dp, bottom = 8.dp)
                        )
                    } else {
                        availableCameras.forEachIndexed { index, camera ->
                            val isSelected = selectedCameras.contains(camera)
                            val isLastItem = index == availableCameras.size - 1
                            FilterRow(
                                label = camera.toFriendlyName(),
                                isSelected = isSelected,
                                onToggle = { settingsStore.toggleCamera(camera) },
                                showDivider = !isLastItem
                            )
                        }
                    }
                }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterRow(
    label: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    showDivider: Boolean = true
) {
    var isFocused by remember { mutableStateOf(false) }

    Column {
        // Right justify the darker grey background box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 9.dp) // Keep horizontal padding
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Color(0xFF0a0a0a), // Darker grey background
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { onToggle() }
                    .focusable()
                    .onFocusChanged { isFocused = it.isFocused }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isFocused) Color(0xFF404040) else Color.Transparent,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End // Right-align the content
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isFocused) Color(0xFFCCCCCC) else Color.White,
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (isFocused) Color.White else Color.White, // Same color as text
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showDivider) {
            // Right justify the divider to match the background box
            Divider(
                color = Color(0xFF404040),
                thickness = 1.dp,
                modifier = Modifier.padding(end = 9.dp)
            )
        }
    }
}
