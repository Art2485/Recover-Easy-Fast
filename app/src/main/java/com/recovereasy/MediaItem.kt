package com.recovereasy

import android.net.Uri

enum class MediaType { IMAGE, VIDEO, AUDIO }

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val mime: String,
    val size: Long,
    val dateAdded: Long,
    val type: MediaType,
    val displayName: String
)
