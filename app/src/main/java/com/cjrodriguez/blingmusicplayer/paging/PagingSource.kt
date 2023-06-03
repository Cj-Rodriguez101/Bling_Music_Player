package com.cjrodriguez.blingmusicplayer.paging

//import androidx.paging.PagingSource.LoadResult
//import com.cjrodriguez.blingmusicplayer.dataSource.cache.SongDao
//import com.cjrodriguez.blingmusicplayer.model.SongWithFavourite
//import androidx.paging.PagingSource.LoadParams as LoadParams1
//
//class PagingSource(
//    private val dao: SongDao,
//    private val query: String
//) : PagingSource<Int, SongWithFavourite>() {
//    override suspend fun load(params: LoadParams1<Int>): LoadResult<Int, SongWithFavourite> {
//        val page = params.key ?: 0
//
//        return try {
//            val entities = dao.searchSongs(query)
//
//            LoadResult.Page(
//                data = entities,
//                prevKey = if (page == 0) null else page - 1,
//                nextKey = if (entities.isEmpty()) null else page + 1
//            )
//        } catch (e: Exception) {
//            LoadResult.Error(e)
//        }
//    }
//
//    override fun getRefreshKey(state: PagingState<Int, Item>): Int? {
//        return state.anchorPosition?.let { anchorPosition ->
//            val anchorPage = state.closestPageToPosition(anchorPosition)
//            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
//        }
//    }
//}
