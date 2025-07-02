package com.netbillvpn

import okhttp3.Interceptor
import okhttp3.Response

class HttpInjector(private val payload: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val newRequestBuilder = originalRequest.newBuilder()

        // Example: Add custom headers or modify request based on payload
        // This is a placeholder, actual implementation depends on payload format
        newRequestBuilder.header("X-Custom-Payload", payload)

        val newRequest = newRequestBuilder.build()
        return chain.proceed(newRequest)
    }
}
