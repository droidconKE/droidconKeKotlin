package com.android254.data.network.util

import com.android254.data.network.Constants.LIVE_BASE_URL
import com.android254.data.network.Constants.TEST_BASE_URL
import com.android254.droidconKE2023.data.BuildConfig

fun provideBaseUrl(): String {
    return if (BuildConfig.DEBUG) TEST_BASE_URL else LIVE_BASE_URL
}