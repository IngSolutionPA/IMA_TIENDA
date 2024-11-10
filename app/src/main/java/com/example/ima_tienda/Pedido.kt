package com.example.ima_tienda

data class Pedido(
    val cedulaCliente: String,        // Cédula del cliente
    val totalPedido: Double,         // Total del pedido
    val fechaPedido: Any,            // Fecha del pedido (puede ser de tipo Date o String según tu necesidad)
    val productos: List<ProductoSeleccionado> // Lista de productos seleccionados
)

