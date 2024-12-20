package com.example.ima_tienda

import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class ProductosDisponiblesAdapter(
    private var productos: List<ProductoDisponible>,
    private val onTotalChanged: (Double) -> Unit // Callback para actualizar el total
) : RecyclerView.Adapter<ProductosDisponiblesAdapter.ProductosViewHolder>() {

    private val seleccionados = mutableSetOf<Int>()
    private val cantidadesSeleccionadas = mutableMapOf<Int, Int>()
    private var total: Double = 0.0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductosViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_producto_disponible, parent, false)
        return ProductosViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductosViewHolder, position: Int) {
        val producto = productos[position]

        // Asignación de datos al ViewHolder
        val texto = "${producto.producto_nombre} \n stock: ${producto.stock_disponible}"
        val spannable = SpannableString(texto)

        // Cambiar color del texto para el stock
        val start = texto.indexOf("stock:")
        val end = texto.length
        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#cccccc")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        holder.nombreTextView.text = spannable
        holder.precioTextView.text = "Precio:\n\$${producto.precio}"

        // Verificar si el producto está agotado
        if (producto.stock_disponible == 0) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#FF8C36"))

            val texto = "${producto.producto_nombre}\n⚠\uFE0FProducto Agotado"
            val spannable = SpannableString(texto)

            // Aplicar color blanco al nombre del producto
            spannable.setSpan(ForegroundColorSpan(Color.WHITE), 0, producto.producto_nombre.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            // Aplicar color negro y negrita al texto "Producto Agotado"
            val start = texto.indexOf("Producto Agotado")
            spannable.setSpan(ForegroundColorSpan(Color.BLACK), start, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannable.setSpan(StyleSpan(Typeface.BOLD), start, texto.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            holder.nombreTextView.text = spannable

            // Deshabilitar elementos relacionados con el producto agotado
            holder.checkBox.isEnabled = false  // Deshabilitar el checkbox
            holder.checkBox.isChecked = false  // Desmarcar el checkbox
            holder.btnRestar.isEnabled = false  // Deshabilitar el botón de restar
            holder.btnSumar.isEnabled = false  // Deshabilitar el botón de sumar
            holder.btnRestar.visibility = View.GONE  // Ocultar el botón de restar
            holder.btnSumar.visibility = View.GONE  // Ocultar el botón de sumar
            holder.precioTextView.setTextColor(Color.WHITE)  // Cambiar el color del precio a blanco
            holder.inputCantidad.isEnabled = false  // Deshabilitar el input de cantidad
        } else {
            // Si el producto no está agotado, habilitar la funcionalidad normal

            // Límite de productos por persona
            val limiteProductoxpersona = producto.limite_producto - producto.cantidad_comprada_en_periodo

            // Configuración de la cantidad según la selección
            val cantidadSeleccionada = cantidadesSeleccionadas[position] ?: 0
            holder.inputCantidad.setText(cantidadSeleccionada.toString())

            // Configuración del fondo basado en la selección
            holder.cardView.setCardBackgroundColor(
                if (cantidadSeleccionada > 0) Color.parseColor("#1B914B") else Color.WHITE
            )
            // Configurar el color del texto según la condición
            holder.nombreTextView.setTextColor(
                if (cantidadSeleccionada > 0) Color.parseColor("#ecf249") else Color.BLACK
            )

            holder.precioTextView.setTextColor(
                if (cantidadSeleccionada > 0) Color.parseColor("#ffffff") else Color.BLACK
            )

            holder.inputCantidad.setTextColor(
                if (cantidadSeleccionada > 0) Color.parseColor("#ffffff") else Color.BLACK
            )

            holder.checkBox.isChecked = cantidadSeleccionada > 0

            // Configuración del estado del CheckBox
            holder.checkBox.isEnabled = true  // Habilitar el check box si no está agotado

            // Botón de restar cantidad
            holder.btnRestar.isEnabled = true  // Habilitar el botón de restar
            holder.btnSumar.isEnabled = true  // Habilitar el botón de sumar

            // Escucha para el CheckBox
            holder.checkBox.setOnClickListener {
                toggleSeleccion(position)
                updateTotal() // Actualiza el total cuando se cambia la selección
            }

            // Escucha para el CardView completo
            holder.cardView.setOnClickListener {
                toggleSeleccion(position)
                updateTotal() // Actualiza el total cuando se cambia la selección
            }

            // Botón de restar cantidad
            holder.btnRestar.setOnClickListener {
                var cantidad = holder.inputCantidad.text.toString().toInt()
                if (cantidad > 0) {
                    cantidad -= 1
                    holder.inputCantidad.setText("$cantidad")
                }
                // Guardar la nueva cantidad
                cantidadesSeleccionadas[position] = cantidad

                // Si la cantidad llega a 0, deseleccionamos el producto
                if (cantidad == 0) {
                    if (seleccionados.contains(position)) {
                        toggleSeleccion(position)
                    }
                }
                updateTotal() // Actualiza el total cuando se cambia la cantidad
            }

            // Botón de sumar cantidad
            holder.btnSumar.setOnClickListener {
                var cantidad = holder.inputCantidad.text.toString().toInt()
                if (cantidad < limiteProductoxpersona) {
                    cantidad += 1
                    holder.inputCantidad.setText("$cantidad")
                } else {
                    holder.inputCantidad.setText("$limiteProductoxpersona")
                }
                // Guardar la nueva cantidad
                cantidadesSeleccionadas[position] = cantidad

                // Si la cantidad es mayor que 0, seleccionamos el producto automáticamente
                if (cantidad > 0) {
                    if (!seleccionados.contains(position)) {
                        toggleSeleccion(position)
                    }
                }
                updateTotal() // Actualiza el total cuando se cambia la cantidad
            }
        }
    }


    override fun getItemCount() = productos.size

    fun updateData(newProductos: List<ProductoDisponible>) {
        productos = newProductos
        notifyDataSetChanged()
    }

    private fun toggleSeleccion(position: Int) {
        val producto = productos[position]

        // Si el producto está seleccionado, lo deseleccionamos
        if (seleccionados.contains(position)) {
            seleccionados.remove(position)
            cantidadesSeleccionadas[position] = 0 // Restablecer la cantidad seleccionada
        } else {
            // Si no está seleccionado, lo seleccionamos
            seleccionados.add(position)
            cantidadesSeleccionadas[position] = 1 // Aseguramos que la cantidad seleccionada sea al menos 1
        }

        // Notificar que el item ha cambiado para que se actualice la vista
        notifyItemChanged(position)

        // Actualiza el total
        updateTotal()
    }


    private fun updateTotal() {
        total = 0.0
        for (position in seleccionados) {
            val producto = productos[position]
            val cantidad = cantidadesSeleccionadas[position] ?: 0 // Usamos la cantidad seleccionada
            val precio = producto.precio.toDoubleOrNull() ?: 0.0
            total += precio * cantidad
        }
        onTotalChanged(total) // Llama al callback para actualizar el total en la actividad
    }

    fun clearSelectedProducts() {
        // Limpiar las selecciones y cantidades seleccionadas
        seleccionados.clear()
        cantidadesSeleccionadas.clear()

        // Actualizar la interfaz para reflejar que no hay productos seleccionados
        notifyDataSetChanged()

        // Restablecer el total a cero
        total = 0.0
        onTotalChanged(total) // Llamar al callback para actualizar el total en la actividad
    }
    fun obtenerProductosSeleccionados(): List<ProductoSeleccionado> {
        val productosSeleccionados = mutableListOf<ProductoSeleccionado>()
        for (position in seleccionados) {
            val producto = productos[position]
            val cantidad = cantidadesSeleccionadas[position] ?: 0
            if (cantidad > 0) {
                productosSeleccionados.add(ProductoSeleccionado(producto, cantidad))
            }
        }
        return productosSeleccionados
    }

    class ProductosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nombreTextView: TextView = itemView.findViewById(R.id.text_nombre_producto)
        val precioTextView: TextView = itemView.findViewById(R.id.text_precio_producto)
        val checkBox: CheckBox = itemView.findViewById(R.id.checkbox_seleccion_producto)
        val cardView: CardView = itemView.findViewById(R.id.cardViewProducto)
        val inputCantidad: EditText = itemView.findViewById(R.id.input_cantidad)
        val btnRestar: Button = itemView.findViewById(R.id.btn_restar)
        val btnSumar: Button = itemView.findViewById(R.id.btn_sumar)
    }
}
