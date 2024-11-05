package com.example.ima_tienda

data class Producto(
    val id: Int ? = null,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val imagen: String? = null
)
data class MensajeRespuesta(
    val message: String
)