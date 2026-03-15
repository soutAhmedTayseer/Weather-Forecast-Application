package com.example.weatherforecastapplication.presentation.mapScreen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherforecastapplication.data.models.dataClasses.LocationData
import com.example.weatherforecastapplication.domain.repository.WeatherRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class MapViewModel(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<LocationData>>(emptyList())
    val searchResults: StateFlow<List<LocationData>> = _searchResults.asStateFlow()

    init {
        // Watches the text input and fetches results after the user stops typing for 500ms
        viewModelScope.launch {
            _searchQuery
                .debounce(500)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.trim().length >= 3) {
                        repository.searchLocations(query).collect { results ->
                            _searchResults.value = results
                        }
                    } else {
                        _searchResults.value = emptyList()
                    }
                }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}