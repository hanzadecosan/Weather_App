
package com.example.hava.data

import com.example.hava.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApi {

    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 100,
        @Query("countryCode") countryCode: String = "TR",
        @Query("language") language: String = "tr",
        @Query("format") format: String = "json"
    ): CitySearchResponse
}
