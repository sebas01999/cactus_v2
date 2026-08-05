package com.phytotec.recepcion.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// --- Request/response DTOs, mirroring phytotec_v2's src/Controller/Api/*.php ---

data class LoginRequest(val email: String, val password: String)

data class LoginUserDto(val id: Int, val email: String, val firstName: String, val lastName: String, val roles: List<String>)

data class LoginResponse(val token: String, val user: LoginUserDto)

/**
 * Lo que devuelve tanto GET /recepciones/{id} (para la vista previa antes de
 * confirmar) como POST /recepciones/{id}/confirmar (para el resultado).
 * Ya viene con nombres legibles — el celular no necesita cachear catálogos
 * de variedades/fincas/etc. como antes, solo pide esto por id.
 */
data class RecepcionDetalleDto(
    val id: Int,
    val productoNombre: String,
    val variedadNombre: String,
    val colorHex: String?,
    val fincaNombre: String,
    val bloqueNombre: String,
    val empleadoNombre: String?,
    val tallos: Int,
    val source: String,
    val createdAt: String?,
    val confirmedAt: String?,
    val confirmedByNombre: String?,
    val confirmedByNewScan: Boolean? = null,
)

interface ApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("recepciones/{id}")
    suspend fun getRecepcion(@Path("id") id: Int): RecepcionDetalleDto

    @POST("recepciones/{id}/confirmar")
    suspend fun confirmarRecepcion(@Path("id") id: Int): RecepcionDetalleDto

    @GET("navigation")
    suspend fun getNavigation(): MobileNavigationResponse
}
