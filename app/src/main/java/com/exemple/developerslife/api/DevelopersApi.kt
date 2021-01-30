package com.exemple.developerslife.api

import com.exemple.developerslife.models.StoryItem
import retrofit2.Response
import retrofit2.http.GET

interface DevelopersApi {

    companion object {
        val BASE_URL = "https://developerslife.ru/"
    }

    @GET("random?json=true")
    suspend fun getRandom() : Response<StoryItem>

}