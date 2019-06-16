package com.sneakers.sneakerschecker.model

import com.google.gson.GsonBuilder
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Retrofit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit


class RetrofitClientInstance {
    private var retrofit: Retrofit? = null
    private val BASE_URL = "http://128.199.134.167"

    fun getRetrofitInstance(): Retrofit? {
        if (retrofit == null) {
            val interceptor = HttpLoggingInterceptor()
            interceptor.level = HttpLoggingInterceptor.Level.BODY
            val client = OkHttpClient
                                        .Builder()
                                        .readTimeout(60, TimeUnit.SECONDS)
                                        .connectTimeout(60, TimeUnit.SECONDS)
                                        .addInterceptor(interceptor).build()

            retrofit = retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().setLenient().create()))
                .build()
        }
        return retrofit
    }
}