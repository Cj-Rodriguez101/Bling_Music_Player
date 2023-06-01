package com.cjrodriguez.blingmusicplayer.dataSource.cache

import androidx.media3.common.MediaItem
import androidx.room.TypeConverter
import com.cjrodriguez.blingmusicplayer.util.MediaItemDeserializer
import com.cjrodriguez.blingmusicplayer.util.MediaItemSerializer
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class ConverterHelper {

    private val gsonParser = GsonBuilder().apply {
        this.registerTypeAdapter(MediaItem::class.java, MediaItemSerializer())
        this.registerTypeAdapter(MediaItem::class.java, MediaItemDeserializer())
        this.setPrettyPrinting()
    }.create()

    private val typeToken = object: TypeToken<MediaItem>(){}.type

    @TypeConverter
    fun convertMediaItemToString(mediaItem: MediaItem): String{
        return gsonParser.toJson(mediaItem, MediaItem::class.java)
    }

    @TypeConverter
    fun convertDataToMediaItem(data: String): MediaItem{
        val mediaItem = gsonParser.fromJson<MediaItem>(data, typeToken)
        return mediaItem?: MediaItem.EMPTY
    }


}