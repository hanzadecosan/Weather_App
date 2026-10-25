package com.example.hava.ui.city

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.hava.data.CityCatalog
import com.example.hava.ui.weather.normalizedCity
import kotlinx.coroutines.flow.map

class CityViewModel(private val savedState: SavedStateHandle) : ViewModel() {
    val searchQuery = savedState.getStateFlow("search", "")
    val filteredCities = searchQuery.map { query ->
        CityCatalog.cities.filter { normalizedCity(it).contains(normalizedCity(query)) }
    }
    fun updateSearchQuery(query: String) { savedState["search"] = query }
}
