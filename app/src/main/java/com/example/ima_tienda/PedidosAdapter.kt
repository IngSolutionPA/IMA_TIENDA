package com.example.ima_tienda

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class PedidosAdapter(
    private var pedidos: List<PedidoAgrupado>,
    private val onCancelarClick: (PedidoAgrupado) -> Unit,
    private val onPagarClick: (PedidoAgrupado) -> Unit
) : RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PedidoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pedido, parent, false)
        return PedidoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PedidoViewHolder, position: Int) {
        val pedido = pedidos[position]
        holder.numPedido.text = "Pedido No: ${pedido.pedido_id}"
        holder.fechaPedido.text = "Fecha: ${pedido.fecha_pedido}"
        holder.totalPedido.text = "Total: \$${"%.2f".format(pedido.total)}"
        holder.estadoPedido.text = "Estado: ${pedido.estado}"

        // Limpiar el contenedor de productos antes de agregar nuevos datos
        holder.productosPedidoContainer.removeAllViews()

        // Crear y añadir una fila para cada producto
        pedido.productos.forEach { producto ->
            val productoRow = LinearLayout(holder.itemView.context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(4, 4, 4, 4)
            }

            // Cantidad
            val cantidadText = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "${producto.cantidad}"
            }

            // Producto
            val productoText = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
                text = producto.producto_nombre.take(18) // Limitar nombre a 18 caracteres
            }

            // Precio Unitario
            val precioText = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "$${"%.2f".format(producto.precio)}"
            }

            // Total por producto (cantidad x precio)
            val totalText = TextView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                text = "$${"%.2f".format(producto.cantidad * producto.precio)}"
                gravity = Gravity.END // Esto alinea el texto a la derecha
            }


            // Añadir vistas a la fila del producto
            productoRow.addView(cantidadText)
            productoRow.addView(productoText)
            productoRow.addView(precioText)
            productoRow.addView(totalText)

            // Añadir la fila al contenedor de productos
            holder.productosPedidoContainer.addView(productoRow)
        }
        val qrUrl = pedido.codigo_qr // Ajusta esta URL según tu lógica
        Glide.with(holder.itemView.context)
            .load(qrUrl)
            .into(holder.codigoQR)

        // Configuración del botón de cancelar
        holder.cancelarButton.setOnClickListener {
            onCancelarClick(pedido)
        }

        // Configuración del botón de pago
        holder.pagarButton.setOnClickListener {
            onPagarClick(pedido)
        }
    }

    override fun getItemCount(): Int = pedidos.size

    fun updateData(nuevosPedidos: List<PedidoAgrupado>) {
        this.pedidos = nuevosPedidos
        notifyDataSetChanged()
    }


    class PedidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numPedido: TextView = itemView.findViewById(R.id.num_pedido)
        val fechaPedido: TextView = itemView.findViewById(R.id.fecha_pedido)
        val totalPedido: TextView = itemView.findViewById(R.id.total_pedido)
        val productosPedidoContainer: LinearLayout = itemView.findViewById(R.id.productos_pedido_container)
        val codigoQR: ImageView = itemView.findViewById(R.id.codigo_qr)
        val estadoPedido: TextView = itemView.findViewById(R.id.estado_pedido)
        val cancelarButton: Button = itemView.findViewById(R.id.cancelar_pedido_button)
        val pagarButton: Button = itemView.findViewById(R.id.pagar_pedido_button)
    }
}
