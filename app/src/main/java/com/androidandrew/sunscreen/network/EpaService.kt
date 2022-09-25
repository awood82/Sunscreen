package com.androidandrew.sunscreen.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private val BASE_URL = "https://data.epa.gov/"

interface EpaService {
    // TODO: Remove hardcoded ZIP code
    @GET("efservice/getEnvirofactsUVHOURLY/ZIP/{zipCode}/JSON")
    suspend fun getUvForecast(@Path("zipCode") zipCode: String): DailyUvIndexForecast
}

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

object EpaApi {
    val service : EpaService by lazy {
        retrofit.create(EpaService::class.java)
    }
}