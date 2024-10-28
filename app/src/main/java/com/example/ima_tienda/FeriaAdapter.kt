package com.example.ima_tienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeriaAdapter(
    private val ferias: List<Feria>,
    private val onDelete: (Feria) -> Unit,
    private val onEdit: (Feria) -> Unit
) : RecyclerView.Adapter<FeriaAdapter.FeriaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeriaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feria, parent, false)
        return FeriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeriaViewHolder, position: Int) {
        val feria = ferias[position]
        holder.bind(feria, onDelete, onEdit)
    }

    override fun getItemCount(): Int = ferias.size

    class FeriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nombreTextView: TextView = itemView.findViewById(R.id.nombreTextView)
        private val ubicacionTextView: TextView = itemView.findViewById(R.id.ubicacionTextView)
        private val fechaTextView: TextView = itemView.findViewById(R.id.fechaTextView)

        fun bind(feria: Feria, onDelete: (Feria) -> Unit, onEdit: (Feria) -> Unit) {
            nombreTextView.text = feria.nombre
            ubicacionTextView.text = feria.ubicacion
            fechaTextView.text = feria.fecha

            itemView.findViewById<View>(R.id.eliminarButton).setOnClickListener { onDelete(feria) }
            itemView.findViewById<View>(R.id.editarButton).setOnClickListener { onEdit(feria) }
        }
    }
}
