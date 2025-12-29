package com.rhappdeveloper.dreidelgameapp.di

import com.rhappdeveloper.dreidelgameapp.domain.DreidelRules
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RulesModule {

    @Provides
    fun provideDreidelRules(): DreidelRules = DreidelRules()
}
