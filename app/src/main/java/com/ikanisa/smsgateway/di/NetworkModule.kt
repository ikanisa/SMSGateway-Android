package com.ikanisa.smsgateway.di

import com.ikanisa.smsgateway.AppDefaults
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

/**
 * Hilt module for network-related dependencies including Supabase client.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideSupabaseClient(): SupabaseClient {
        return createSupabaseClient(
            supabaseUrl = AppDefaults.SUPABASE_URL,
            supabaseKey = AppDefaults.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
        }
    }
}
