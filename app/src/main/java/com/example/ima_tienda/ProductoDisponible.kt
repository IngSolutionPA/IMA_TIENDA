package com.example.ima_tienda

data class ProductoDisponible(
    val producto_id: Int,
    val producto_nombre: String,
    val producto_descripcion: String,
    val id_feria: Int,
    val stock_disponible: Int,
    val limite_producto: Int,
    val regla_tiempo: String,
    val cantidad_comprada_en_periodo: Int,
    val precio: String
)

