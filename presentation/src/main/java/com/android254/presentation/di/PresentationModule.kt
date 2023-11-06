package com.android254.presentation.di

import android.content.Context
import com.android254.presentation.notifications.DroidconNotificationManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    @Provides
    @Singleton
    fun providesDroidconNotificationManager(@ApplicationContext context: Context) = DroidconNotificationManager(context)
}