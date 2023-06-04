package com.android254.presentation.feed.view

import com.android254.presentation.models.FeedUI

sealed interface FeedUIState {
    object Loading : FeedUIState
    data class Error(val message: String) : FeedUIState
    data class Success(val feeds: List<FeedUI>) : FeedUIState
}