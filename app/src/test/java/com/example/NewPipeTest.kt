package com.example

import org.junit.Test
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.stream.StreamInfo

class NewPipeTest {
    @Test
    fun testYouTube() {
        NewPipe.init(OkHttpDownloader.instance, Localization.DEFAULT, ContentCountry.DEFAULT)
        try {
            val info = StreamInfo.getInfo(ServiceList.YouTube, "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
            println("Description: ${info.description.content.take(20)}")
            println("Uploader avatar: ${info.uploaderAvatars.firstOrNull()?.url}")
            val rel = info.relatedItems!![0] as org.schabi.newpipe.extractor.stream.StreamInfoItem
            println("Related Name: ${rel.name}")
            println("Related Uploader: ${rel.uploaderName}")
            println("Related Thumbnail: ${rel.thumbnails.firstOrNull()?.url}")
            println("Related views: ${rel.viewCount}")
        } catch (e: Exception) {
            println("EXCEPTION CAUGHT: ${e.message}")
            e.printStackTrace()
        }
    }
}
