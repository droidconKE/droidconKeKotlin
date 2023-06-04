package com.android254.presentation.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android254.domain.models.ResourceResult
import com.android254.domain.repos.FeedRepo
import com.android254.presentation.feed.view.FeedUIState
import com.android254.presentation.feed.view.toPresentation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel  @Inject constructor(
    private val feedRepo: FeedRepo
) : ViewModel() {
    private val _feedsState = MutableStateFlow<FeedUIState>(FeedUIState.Loading)
    val feedsState: StateFlow<FeedUIState> get() = _feedsState

    fun fetchFeed() {
        viewModelScope.launch {
            when (val value = feedRepo.fetchFeed()) {
                is ResourceResult.Error -> _feedsState.value = FeedUIState.Error(value.message)
                is ResourceResult.Loading -> _feedsState.value = FeedUIState.Loading
                is ResourceResult.Success -> {
                    value.data?.let {
                        _feedsState.value = FeedUIState.Success(
                            it.map { feed -> feed.toPresentation() }
                        )
                    }
                }
                else -> _feedsState.value = FeedUIState.Error("Unknown")
            }
        }
    }
}