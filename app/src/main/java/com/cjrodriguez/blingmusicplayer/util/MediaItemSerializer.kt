package com.cjrodriguez.blingmusicplayer.util

import androidx.media3.common.MediaItem
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

internal class MediaItemSerializer : JsonSerializer<MediaItem> {
    override fun serialize(
        mediaItem: MediaItem?,
        srcType: Type?,
        context: JsonSerializationContext?
    ): JsonElement {
        return if (mediaItem != null) {
            JsonPrimitive(
                mediaItem.mediaMetadata.albumTitle.toString() + "," + mediaItem.mediaMetadata.title + "," + mediaItem.mediaMetadata.artist + "," + mediaItem.mediaMetadata.artworkUri.toString() + "," + mediaItem.mediaId + "," + mediaItem.mediaMetadata.extras?.getString(
                    "sortedUnSpacedTitle"
                ) + "," + mediaItem.mediaMetadata.extras?.getString("id")
            )
        } else {
            JsonPrimitive(" " + " " + " " + " " + " " + " " + " " + " " + " " + " " + " " + " ")
        }

    }
}