package com.learner.invoicegenerator.ApiCalling

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object RetrofitInstance {
    val api: ApiService by lazy{
        Retrofit.Builder()
            .baseUrl("https://api.upcitemdb.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}