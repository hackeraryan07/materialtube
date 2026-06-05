package com.example

import android.app.Application
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Headers
import java.util.concurrent.TimeUnit

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        NewPipe.init(OkHttpDownloader.instance, Localization.DEFAULT, ContentCountry.DEFAULT)
    }
}

class OkHttpDownloader private constructor() : Downloader() {
    
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()

    override fun execute(request: Request): Response {
        val httpMethod = request.httpMethod()
        val url = request.url()
        val headers = request.headers()
        val dataToSend = request.dataToSend()

        val builder = okhttp3.Request.Builder()
            .url(url)

        val okHeadersBuilder = Headers.Builder()
        if (headers != null) {
            for ((key, values) in headers) {
                if (key.equals("Accept-Encoding", ignoreCase = true)) {
                    continue
                }
                for (value in values) {
                    okHeadersBuilder.add(key, value)
                }
            }
        }
        val okHeaders = okHeadersBuilder.build()
        builder.headers(okHeaders)

        when (httpMethod) {
            "GET" -> builder.get()
            "POST" -> builder.post(dataToSend?.toRequestBody() ?: ByteArray(0).toRequestBody())
            "HEAD" -> builder.head()
            else -> throw UnsupportedOperationException("Method $httpMethod not supported")
        }

        val response = client.newCall(builder.build()).execute()
        
        val body = response.body
        val responseBody = body?.string() ?: ""
        
        // Convert headers Map<String, List<String>>
        val responseHeaders = mutableMapOf<String, List<String>>()
        response.headers.names().forEach { name ->
            responseHeaders[name] = response.headers.values(name)
        }

        return Response(
            response.code,
            response.message,
            responseHeaders,
            responseBody,
            request.url()
        )
    }

    companion object {
        val instance by lazy { OkHttpDownloader() }
    }
}
