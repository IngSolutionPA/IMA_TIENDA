package com.example.ima_tienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class ProductoAdapter(
    private val productos: List<Producto>,
    private val onDelete: (Producto) -> Unit,
    private val onEdit: (Producto) -> Unit // Nuevo callback para editar
) : RecyclerView.Adapter<ProductoAdapter.ProductoViewHolder>() {

    class ProductoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreTextView: TextView = view.findViewById(R.id.nombreTextView)
        val precioTextView: TextView = view.findViewById(R.id.precioTextView)
        val eliminarButton: View = view.findViewById(R.id.eliminarButton) // Asegúrate de que este ID existe en tu layout
        val editarButton: View = view.findViewById(R.id.editarButton) // Asegúrate de que este ID existe en tu layout
        val imagenProducto: ImageView = view.findViewById(R.id.imagenProducto) // Agregamos la referencia a la ImageView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto, parent, false)
        return ProductoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        val producto = productos[position]
        holder.nombreTextView.text = producto.nombre
        holder.precioTextView.text = "B/. ${String.format("%.2f", producto.precio)}" // Formatear el precio

        // Cargar la imagen usando Glide
        Glide.with(holder.itemView.context)
            .load(producto.imagen) // Usar la ruta de la imagen almacenada
            .into(holder.imagenProducto)

        holder.eliminarButton.setOnClickListener {
            onDelete(producto)
        }

        holder.editarButton.setOnClickListener {
            onEdit(producto)
        }
    }

    override fun getItemCount(): Int = productos.size
}
