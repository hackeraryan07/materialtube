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
            println("Video streams: ${info.videoStreams.size}")
            println("Video only streams: ${info.videoOnlyStreams.size}")
            println("Audio streams: ${info.audioStreams.size}")
            println("Video stream resolutions: ${info.videoStreams.map { it.getResolution() }}")
            println("Video only stream resolutions: ${info.videoOnlyStreams.map { it.getResolution() }}")
        } catch (e: Exception) {
            println("EXCEPTION CAUGHT: ${e.message}")
            e.printStackTrace()
        }
    }
}
