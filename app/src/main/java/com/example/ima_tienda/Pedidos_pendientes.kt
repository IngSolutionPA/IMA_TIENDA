package com.example.ima_tienda

data class Pedidos_pendientes(
    val pedido_id: Int,
    val total: Double,
    val fecha_pedido: String,
    val producto_id: Int,
    val producto_nombre: String,
    val cantidad: Int,
    val precio: Double,
    val codigo_qr: String,
    val estado: String,
    val cedula_cliente: String
)

fun groupPedidos(pedidos: List<Pedidos_pendientes>): List<PedidoAgrupado> {
    return pedidos.groupBy { it.pedido_id }.map { (pedidoId, productos) ->
        PedidoAgrupado(
            pedido_id = pedidoId,
            fecha_pedido = productos.first().fecha_pedido,
            total = productos.sumOf { it.precio * it.cantidad },
            productos = productos,
            codigo_qr =  productos.first().codigo_qr ?: "No disponible",
            estado =  productos.first().estado ?: "No disponible"

        )
    }
}

data class PedidoAgrupado(
    val pedido_id: Int,
    val fecha_pedido: String,
    val total: Double,
    val productos: List<Pedidos_pendientes>,
    val codigo_qr: String,
    val estado: String
)

