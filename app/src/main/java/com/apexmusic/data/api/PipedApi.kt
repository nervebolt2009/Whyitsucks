package com.apexmusic.data.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

interface PipedApiService {

    /**
     * Search YouTube Music songs via Piped.
     * filter=music_songs → returns only music tracks (YT Music catalog)
     */
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("filter") filter: String = "music_songs"
    ): SearchResponse

    /**
     * Get audio stream URLs for a video.
     * videoId extracted from "/watch?v=VIDEO_ID"
     */
    @GET("streams/{videoId}")
    suspend fun getStreams(
        @Path("videoId") videoId: String
    ): StreamsResponse
}

object PipedApiClient {

    // Multiple public Piped instances as fallback
    private val INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://piped-api.garudalinux.org",
        "https://api.piped.projectsegfault.net",
        "https://pipedapi.coldforge.xyz"
    )

    private var currentInstanceIndex = 0

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        // Retry with next instance on failure
        .addInterceptor { chain ->
            var response = chain.proceed(chain.request())
            var attempts = 0
            while (!response.isSuccessful && attempts < INSTANCES.size - 1) {
                response.close()
                attempts++
                currentInstanceIndex = (currentInstanceIndex + 1) % INSTANCES.size
                response = chain.proceed(chain.request())
            }
            response
        }
        .build()

    fun createService(baseUrl: String = INSTANCES[currentInstanceIndex]): PipedApiService {
        return Retrofit.Builder()
            .baseUrl("$baseUrl/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PipedApiService::class.java)
    }

    val service: PipedApiService by lazy { createService() }
}
