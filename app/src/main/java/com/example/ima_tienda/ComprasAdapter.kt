package com.example.ima_tienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ComprasAdapter(private var compras: List<Compra>) : RecyclerView.Adapter<ComprasAdapter.ComprasViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComprasViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_compra, parent, false)
        return ComprasViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComprasViewHolder, position: Int) {
        val compra = compras[position]
        holder.productoTextView.text = compra.producto
        holder.feriaTextView.text = "Feria: ${compra.feria}"
        holder.cantidadTextView.text = "Cantidad: ${compra.cantidad}"
        holder.precioTextView.text = "B/. ${String.format("%.2f", compra.precio)}"
        holder.fechaCompraTextView.text = "Fecha: ${compra.fecha_compra}"
    }

    override fun getItemCount() = compras.size

    fun updateData(newCompras: List<Compra>) {
        compras = newCompras
        notifyDataSetChanged()
    }

    class ComprasViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productoTextView: TextView = itemView.findViewById(R.id.text_producto)
        val feriaTextView: TextView = itemView.findViewById(R.id.text_feria)
        val cantidadTextView: TextView = itemView.findViewById(R.id.text_cantidad)
        val precioTextView: TextView = itemView.findViewById(R.id.text_precio)
        val fechaCompraTextView: TextView = itemView.findViewById(R.id.text_fecha_compra)
    }
}
