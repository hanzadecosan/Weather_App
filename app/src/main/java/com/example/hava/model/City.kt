
package com.example.hava.model

import com.google.gson.annotations.SerializedName

data class City(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,

    @SerializedName("country_code")
    val countryCode: String? = null,

    @SerializedName("admin1")
    val region: String? = null,
    @SerializedName("feature_code")
    val featureCode: String? = null
)
