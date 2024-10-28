package com.example.ima_tienda

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class MenuAdapter(
    private val menuItems: List<MenuItem>,
    private val itemClickListener: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    inner class MenuViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val button: MaterialButton = view.findViewById(R.id.menuButton)

        fun bind(menuItem: MenuItem) {
            button.text = menuItem.title
            button.icon = ContextCompat.getDrawable(button.context, menuItem.iconResId)
            button.setOnClickListener { itemClickListener(menuItem) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.menu_item, parent, false)
        return MenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        holder.bind(menuItems[position])
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }
}
