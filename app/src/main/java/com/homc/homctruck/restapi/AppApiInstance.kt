package com.homc.homctruck.restapi

import com.homc.homctruck.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Headers
import okhttp3.Headers.Companion.toHeaders
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

object AppApiInstance {

    private val logLevel = HttpLoggingInterceptor.Level.BODY

    fun api(token: String?): AppApiService = retrofit(token).create(AppApiService::class.java)
    fun apiPostal(token: String?): PostalApiService = retrofitPostal(token).create(PostalApiService::class.java)

    fun retrofit(token: String?): Retrofit = getRetrofitForUrl(BuildConfig.SERVER_URL, token)
    private fun retrofitPostal(token: String?): Retrofit = getRetrofitForUrl("https://api.postalpincode.in/", token)

    private fun getRetrofitForUrl(baseUrl: String, token: String?): Retrofit {
        val client = httpClient(token)
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory.invoke())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private fun httpClient(token: String?): OkHttpClient {
            val interceptorLogging = HttpLoggingInterceptor()
            interceptorLogging.level = logLevel
            val interceptorRequest = getInterceptor(token)
            return OkHttpClient.Builder()
                .addInterceptor(interceptorLogging)
                .connectTimeout(3, TimeUnit.MINUTES)
                .readTimeout(3, TimeUnit.MINUTES)
                .writeTimeout(3, TimeUnit.MINUTES)
                .addNetworkInterceptor(interceptorRequest)
                .build()
        }

    private fun getInterceptor(authHeader: String?): Interceptor {
        return Interceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val headersMap = getHeadersMap(original.headers)
            authHeader?.let {
                headersMap["Authorization"] = "Bearer $it"
            }
            headersMap["Accept"] = "application/json"
            val request = original.newBuilder()
                .headers(headersMap.toHeaders())
                .method(original.method, original.body)
                .build()
            chain.proceed(request)
        }
    }

    private fun getHeadersMap(headers: Headers?): MutableMap<String, String> {
        val headersMap = HashMap<String, String>()
        if (headers != null) {
            val names = headers.names()
            for (name in names) {
                headers[name]?.let {
                    headersMap[name] =  it
                }
            }
        }
        return headersMap
    }
}