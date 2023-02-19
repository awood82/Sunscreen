package com.androidandrew.sunscreen.network

import com.androidandrew.sunscreen.network.error.ResultCallAdapterFactory
import com.androidandrew.sunscreen.network.model.DailyUvIndexForecast
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

private const val BASE_URL = "https://data.epa.gov/"

interface EpaService {
    @GET("efservice/getEnvirofactsUVHOURLY/ZIP/{zipCode}/JSON")
    suspend fun getUvForecast(@Path("zipCode") zipCode: String): Result<DailyUvIndexForecast>
}

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(ResultCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

object EpaApi {
    val service : EpaService by lazy {
        retrofit.create(EpaService::class.java)
    }
}