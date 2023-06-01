package com.cjrodriguez.blingmusicplayer.util

import androidx.media3.common.MediaItem
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

internal class MediaItemDeserializer : JsonDeserializer<MediaItem> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): MediaItem {
        val stringList = (json.asString ?: "").split(",")
        return if (stringList.size == 7) {
            getMediaItemWithMetaData(
                stringList[0],
                stringList[1],
                stringList[2],
                stringList[3],
                stringList[4],
                stringList[5],
                stringList[6]
            )
        } else {
            MediaItem.EMPTY
        }
    }
}