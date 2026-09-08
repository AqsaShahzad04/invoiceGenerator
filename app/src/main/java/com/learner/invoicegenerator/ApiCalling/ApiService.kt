package com.learner.invoicegenerator.ApiCalling

import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("prod/trial/lookup")
    suspend fun getitemDetails(@Query("upc")upc:String):upcLookUpResponse
}