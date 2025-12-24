package com.rhappdeveloper.dreidelgameapp.di

import com.rhappdeveloper.dreidelgameapp.domain.DreidelSideProvider
import com.rhappdeveloper.dreidelgameapp.domain.RandomDreidelSideProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DreidelModule {
    @Binds
    abstract fun bindDreidelSideProvider(
        impl: RandomDreidelSideProvider
    ): DreidelSideProvider
}