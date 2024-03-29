package com.frenzin.machinemaintain.network

import android.util.Log
import com.frenzin.machinemaintain.utills.AppPref
import com.frenzin.machinemaintain.utills.MyApp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object RetrofitClient {
    /**
     * Get Retrofit Instance
     */
    var BASE_URL = "http://emp.frenzinsoftwares.com/api_fcms/public/api/"

    private fun getRetrofitInstance(): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
            .baseUrl(BASE_URL)
            .client(getHttpClient())
            .build()
    }

    fun getHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        return OkHttpClient().newBuilder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(object : Interceptor {
                @Throws(IOException::class)
                override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                    val originalRequest: Request = chain.request()
                    val builder: Request.Builder = originalRequest.newBuilder().header("Authorization", "Bearer " + MyApp.getMyApp()
                        ?.let { AppPref.getInstance(it).getToken() })
                    Log.e("Authorization", "Bearer " + MyApp.getMyApp()
                        ?.let { AppPref.getInstance(it).getToken() })
                    val newRequest: Request = builder.build()
                    return chain.proceed(newRequest)
                }
            })
            .addInterceptor(interceptor)
            .build()
    }

    /**
     * Get API Service
     *
     * @return API Service
     */
    fun getApiService(): ApiService {
        return getRetrofitInstance().create(ApiService::class.java)
    }
}
