package com.steveburns.photosearch.network

import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

object NetworkAdapter {

    val imgurService = createService()

    private fun createService() : ImgurService {
        val builder = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
        val client = builder.build()
        val retrofit = Retrofit.Builder()
                .baseUrl("https://api.imgur.com")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()) // TODO: may need to create custom converter for network errors
                .build()

        return retrofit.create(ImgurService::class.java)
    }
}

interface ImgurService {

    @GET("/3/gallery/search/time/{page}")
    @Headers("Authorization: Client-ID 126701cd8332f32")
    fun getPhotoPage(
            @Path("page") page: Int,
            @Query("q") searchTerm: String
    ) : Single<ImageDataResponse>

}

data class ImageDataResponse(
        val data: List<NetworkImageData>
)

data class NetworkImageData(
        val title: String,
        val images: List<Image>
)

data class Image(
        val type: String,
        val link: String
)