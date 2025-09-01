package com.pizzaman.frigateevents.data

data class EventData(
    val attributes: List<String> = emptyList(),
    val box: List<Double> = emptyList(),
    val region: List<Double> = emptyList(),
    val score: Double,
    val top_score: Double,
    val type: String
)

data class FrigateEvent(
    val id: String,
    val camera: String,
    val label: String,
    val start_time: Double,
    val end_time: Double?,
    val has_clip: Boolean,
    val has_snapshot: Boolean,
    val zones: List<String> = emptyList(),
    val data: EventData? = null,
    val box: List<Double>? = null,
    val false_positive: Boolean? = null,
    val plus_id: String? = null,
    val retain_indefinitely: Boolean,
    val sub_label: String? = null,
    val top_score: Double? = null
) {
    val durationSeconds: Double?
        get() = end_time?.let { it - start_time }

    fun thumbnailUrl(baseURL: String): String? {
        return "$baseURL/api/events/$id/thumbnail.jpg"
    }

    fun clipUrl(baseURL: String): String? {
        return "$baseURL/api/events/$id/clip.mp4"
    }

    // Alternative clip URL methods for Frigate 15 compatibility
    fun clipUrlAlternative1(baseURL: String): String? {
        // Try without .mp4 extension
        return "$baseURL/api/events/$id/clip"
    }

    fun clipUrlAlternative2(baseURL: String): String? {
        // Try with different path structure
        return "$baseURL/api/events/$id/recording"
    }

    fun clipUrlAlternative3(baseURL: String): String? {
        // Try with .mov extension
        return "$baseURL/api/events/$id/clip.mov"
    }

    fun fullSizeSnapshotUrl(baseURL: String): String? {
        return "$baseURL/api/events/$id/snapshot.jpg"
    }

    fun getAllVideoUrls(baseURL: String): List<String> {
        return listOfNotNull(
            clipUrl(baseURL),
            clipUrlAlternative1(baseURL),
            clipUrlAlternative2(baseURL),
            clipUrlAlternative3(baseURL)
        )
    }
}
