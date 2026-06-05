package com.example.data

import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.kiosk.KioskInfo
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.schabi.newpipe.extractor.ListExtractor.InfoItemsPage
import org.schabi.newpipe.extractor.stream.StreamInfoItem

class VideoRepository {
    private val youtubeService = ServiceList.YouTube

    suspend fun getTrendingVideos(): List<StreamInfoItem> = withContext(Dispatchers.IO) {
        try {
            val kioskExtractor = youtubeService.getKioskList().defaultKioskExtractor
            kioskExtractor.fetchPage()
            val page = kioskExtractor.initialPage
            page.items.filterIsInstance<StreamInfoItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun searchVideos(query: String): List<StreamInfoItem> = withContext(Dispatchers.IO) {
        try {
            val searchExtractor = youtubeService.getSearchExtractor(query)
            searchExtractor.fetchPage()
            val page = searchExtractor.initialPage
            page.items.filterIsInstance<StreamInfoItem>()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getStreamInfo(url: String): StreamInfo = withContext(Dispatchers.IO) {
        StreamInfo.getInfo(youtubeService, url)
    }
}
