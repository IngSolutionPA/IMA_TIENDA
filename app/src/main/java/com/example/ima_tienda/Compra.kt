package com.example.ima_tienda

data class Compra(
    val id: Int,
    val producto: String,
    val cantidad: Int,
    val precio: Double,
    val fecha_compra: String,
    val feria: String
)
