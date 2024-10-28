package com.example.ima_tienda
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface ApiService {
    @GET("productos")
    fun obtenerProductos(): Call<List<Producto>>


    @Multipart
    @POST("productos/agregar/") // Asegúrate de que la URL coincida con tu servidor
    fun agregarProductos(
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part imagen: MultipartBody.Part
    ): Call<Producto>

    @DELETE("productos/eliminar/{id}")
    fun eliminarProducto(@Path("id") id: Int): Call<Void>

    @Multipart
    @POST("productos/editar/{id}")
    fun editarProducto(
        @Path("id") id: Int,
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part imagen: MultipartBody.Part? // La imagen puede ser opcional
    ): Call<Producto>

    // Funciones para ferias
    @GET("ferias") // Cambia esto según tu endpoint
    fun obtenerFerias(): Call<List<Feria>>

    @POST("ferias/agregar/") // Asegúrate de que la URL coincida con tu servidor
    fun agregarFeria(
        @Body feria: Feria // Enviar objeto Feria directamente
    ): Call<Feria>

    @DELETE("ferias/eliminar/{id}")
    fun eliminarFeria(@Path("id") id: Int): Call<Void>

    @POST("ferias/editar/{id}")
    fun editarFeria(
        @Path("id") id: Int,
        @Body feria: Feria // Enviar objeto Feria directamente
    ): Call<Feria>

}
