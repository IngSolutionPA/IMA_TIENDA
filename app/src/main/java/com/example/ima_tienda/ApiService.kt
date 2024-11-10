package com.example.ima_tienda
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("productos")
    fun obtenerProductos(): Call<List<Producto>>


    @Multipart
    @POST("productos/agregar/") // Asegúrate de que la URL coincida con tu servidor
    fun agregarProductos(
        @Part("nombre") nombre: RequestBody,
        @Part("descripcion") descripcion: RequestBody,
        @Part("precio") precio: RequestBody,
        @Part imagen: MultipartBody.Part? // Hace que la imagen sea opcional
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

    @POST("usuarios/agregar/")
    fun agregarUsuario(
        @Body usuario: Usuario // Enviamos el objeto Usuario directamente en el cuerpo de la solicitud
    ): Call<Usuario>

    @GET("usuarios/{cedula}")
    fun verificarUsuario(@Path("cedula") cedula: String): Call<List<Usuario>>


    @POST("usuarios/app/") // Asegúrate de que la URL sea correcta
    fun loginUsuario(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("compras/{cedula}")
    fun obtenerComprasPorCedula(@Path("cedula") cedula: String): Call<List<Compra>>

    @GET("compras/disponible/{cedula}")
    fun obtenerProductosDisponibles(@Path("cedula") cedula: String): Call<List<ProductoDisponible>>

    @POST("compras/registrar_pedido/")
    fun registrarPedido(@Body pedido: Pedido): Call<Pedido> // Se espera un objeto Pedido para registrar

    @GET("pedidos/{cedula}")
    fun obtenerHistorialPedidos(@Path("cedula") cedula: String): Call<List<Pedidos_pendientes>>


}
