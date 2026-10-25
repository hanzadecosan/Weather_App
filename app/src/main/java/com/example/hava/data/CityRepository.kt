
package com.example.hava.data

import com.example.hava.model.City

open class CityRepository(
    private val api: CityApi = ApiClient.cityApi
) {
    open suspend fun searchCity(name: String): List<City> {
        val response = api.searchCities(name)
        return response.results.orEmpty()
    }
}
