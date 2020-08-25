package com.homc.homctruck.restapi

import com.homc.homctruck.utils.AppConfig
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Admin on 1/3/2017.
 */
object AppApiInstance {
    private val logLevel = HttpLoggingInterceptor.Level.BODY

    val api: AppApiService =  retrofit.create(AppApiService::class.java)
    private val retrofit: Retrofit
        get() = getRetrofitForUrl(AppConfig.serverUrl)

    private fun getRetrofitForUrl(baseUrl: String): Retrofit {
        val client = httpClient
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) /*.addCallAdapterFactory(CoroutineCallAdapterFactory.create())*/
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val httpClient: OkHttpClient
        get() {
            val interceptorLogging = HttpLoggingInterceptor()
            interceptorLogging.level = logLevel
            val interceptorRequest = getInterceptor(AppConfig.token)
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
            val headersMap = getHeadersMap(original.headers())
            authHeader?.let {
                headersMap["Authorization"] = "Basic $it"
            }
            headersMap["Accept"] = "application/json"
            val request = original.newBuilder()
                .headers(toHeaders(headersMap))
                .method(original.method(), original.body())
                .build()
            chain.proceed(request)
        }
    }

    private fun getHeadersMap(headers: Headers?): MutableMap<String, String?> {
        val headersMap = HashMap<String, String?>()
        if (headers != null) {
            val names = headers.names()
            for (name in names) {
                headersMap[name] = headers[name]
            }
        }
        return headersMap
    }

    private fun toHeaders(headersMap: Map<String, String?>): Headers {
        return Headers.of(headersMap)
    }
}