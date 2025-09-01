package com.pizzaman.frigateevents.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SettingsStore(
    private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("frigate_settings", Context.MODE_PRIVATE)

    // Frigate Base URL
    var frigateBaseURL by mutableStateOf(
        prefs.getString("frigateBaseURL", "http://192.168.1.168:5000") ?: "http://192.168.1.168:5000"
    )
        private set

    // Frigate Version
    var frigateVersion by mutableStateOf("Unknown")
        private set

    // Available Labels (populated from events)
    private val _availableLabels = MutableStateFlow<List<String>>(emptyList())
    val availableLabels: StateFlow<List<String>> = _availableLabels

    // Selected Labels for filtering
    private val _selectedLabels = MutableStateFlow<Set<String>>(
        prefs.getStringSet("selectedLabels", emptySet()) ?: emptySet()
    )
    val selectedLabels: StateFlow<Set<String>> = _selectedLabels

    // Available Zones (populated from events)
    private val _availableZones = MutableStateFlow<List<String>>(emptyList())
    val availableZones: StateFlow<List<String>> = _availableZones

    // Selected Zones for filtering
    private val _selectedZones = MutableStateFlow<Set<String>>(
        prefs.getStringSet("selectedZones", emptySet()) ?: emptySet()
    )
    val selectedZones: StateFlow<Set<String>> = _selectedZones

    // Available Cameras (populated from events)
    private val _availableCameras = MutableStateFlow<List<String>>(emptyList())
    val availableCameras: StateFlow<List<String>> = _availableCameras

    // Selected Cameras for filtering
    private val _selectedCameras = MutableStateFlow<Set<String>>(
        prefs.getStringSet("selectedCameras", emptySet()) ?: emptySet()
    )
    val selectedCameras: StateFlow<Set<String>> = _selectedCameras

    // Update base URL
    fun updateFrigateBaseURL(newURL: String) {
        frigateBaseURL = newURL
        prefs.edit().putString("frigateBaseURL", newURL).apply()
    }

    // Update version
    fun updateFrigateVersion(version: String) {
        frigateVersion = version
    }

    // Update available labels (accumulate, don't replace)
    fun updateAvailableLabels(labels: List<String>) {
        val current = _availableLabels.value.toSet()
        val newLabels = (current + labels.toSet()).sorted()
        _availableLabels.value = newLabels
    }

    // Toggle label selection
    fun toggleLabel(label: String) {
        val current = _selectedLabels.value.toMutableSet()
        if (current.contains(label)) {
            current.remove(label)
        } else {
            current.add(label)
        }
        _selectedLabels.value = current
        prefs.edit().putStringSet("selectedLabels", current).apply()
    }

    // Update available zones (accumulate, don't replace)
    fun updateAvailableZones(zones: List<String>) {
        val current = _availableZones.value.toSet()
        val newZones = (current + zones.toSet()).sorted()
        _availableZones.value = newZones
    }

    // Toggle zone selection
    fun toggleZone(zone: String) {
        val current = _selectedZones.value.toMutableSet()
        if (current.contains(zone)) {
            current.remove(zone)
        } else {
            current.add(zone)
        }
        _selectedZones.value = current
        prefs.edit().putStringSet("selectedZones", current).apply()
    }

    // Update available cameras (accumulate, don't replace)
    fun updateAvailableCameras(cameras: List<String>) {
        println("🔧 SettingsStore.updateAvailableCameras called with: $cameras")
        val current = _availableCameras.value.toSet()
        println("🔧 Current cameras: $current")
        val newCameras = (current + cameras.toSet()).sorted()
        println("🔧 New cameras after merge: $newCameras")
        _availableCameras.value = newCameras
        println("🔧 Camera list updated successfully")
    }

    // Toggle camera selection
    fun toggleCamera(camera: String) {
        val current = _selectedCameras.value.toMutableSet()
        if (current.contains(camera)) {
            current.remove(camera)
        } else {
            current.add(camera)
        }
        _selectedCameras.value = current
        prefs.edit().putStringSet("selectedCameras", current).apply()
    }

    // Check if version is supported
    fun isVersionSupported(versionString: String): Boolean {
        val components = versionString.split(".")
        val major = components.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = components.getOrNull(1)?.toIntOrNull() ?: 0

        // Support Frigate v0.12.x and later
        if (major == 0 && minor >= 12) {
            return true
        }

        // Support Frigate v1.0.0 and later
        if (major >= 1) {
            return true
        }

        return false
    }

    // Get compatibility status
    fun getCompatibilityStatus(versionString: String): Pair<String, String> {
        val components = versionString.split(".")
        val major = components.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = components.getOrNull(1)?.toIntOrNull() ?: 0

        // Fully supported versions
        if ((major == 0 && minor >= 13) || major >= 1) {
            return "Fully Supported" to "green"
        }

        // Limited support for older versions
        if (major == 0 && minor >= 12) {
            return "Limited Support" to "orange"
        }

        // Unsupported versions
        return "Unsupported" to "red"
    }

    // Clear all filters
    fun clearAllFilters() {
        _selectedLabels.value = emptySet()
        _selectedZones.value = emptySet()
        _selectedCameras.value = emptySet()

        prefs.edit()
            .remove("selectedLabels")
            .remove("selectedZones")
            .remove("selectedCameras")
            .apply()
    }
}
