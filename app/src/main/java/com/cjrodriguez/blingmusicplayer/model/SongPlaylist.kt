package com.cjrodriguez.blingmusicplayer.model

import androidx.room.Entity

@Entity(tableName = "songPlaylistTable", primaryKeys= [ "songId", "playlistId" ] )
data class SongPlaylist(
    val songId: Long,
    val playlistId: Long
)
