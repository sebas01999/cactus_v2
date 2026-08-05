package com.phytotec.recepcion.data.remote

import com.phytotec.recepcion.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Attaches "Authorization: Bearer <token>" to every request except /login
 * (which must work even with a stale/invalid token still stored locally —
 * attaching a bad token there would make ApiTokenAuthenticator reject the
 * request with 401 before it ever reaches the login controller).
 *
 * Provided (not @Inject-constructed) via NetworkModule.
 */
class AuthInterceptor(
    private val sessionManager: SessionManager,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.url.encodedPath.endsWith("/login")) {
            return chain.proceed(request)
        }

        val token = runBlocking { sessionManager.currentToken() }
        val authorized = if (token.isNullOrBlank()) {
            request
        } else {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        }

        return chain.proceed(authorized)
    }
}
