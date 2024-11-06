package com.example.ima_tienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComprasAdapter(private var comprasAgrupadas: Map<Int, List<Compra>>) : RecyclerView.Adapter<ComprasAdapter.ComprasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComprasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
        return ComprasViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComprasViewHolder, position: Int) {
        val idCompra = comprasAgrupadas.keys.toList()[position]
        val productosDeLaCompra = comprasAgrupadas[idCompra]

        if (productosDeLaCompra != null && productosDeLaCompra.isNotEmpty()) {
            val compra = productosDeLaCompra[0]
            holder.feriaTextView.text = "Feria: ${compra.feria}"
            holder.fechaCompraTextView.text = "Fecha: ${compra.fecha_compra}"

            // Crear un formato más estructurado para mostrar los productos
            val productosInfo = productosDeLaCompra.joinToString(separator = "\n") {
                "- ${it.producto} x${it.cantidad} • B/. ${String.format("%.2f", it.precio)}"
            }
            holder.productoTextView.text = productosInfo
        }
    }


    override fun getItemCount() = comprasAgrupadas.size

    fun updateData(newCompras: Map<Int, List<Compra>>) {
        comprasAgrupadas = newCompras
        notifyDataSetChanged()
    }

    class ComprasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productoTextView: TextView = itemView.findViewById(R.id.text_producto)
        val feriaTextView: TextView = itemView.findViewById(R.id.text_feria)
        val fechaCompraTextView: TextView = itemView.findViewById(R.id.text_fecha_compra)
    }
}
