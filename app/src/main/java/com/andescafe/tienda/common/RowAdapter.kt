package com.andescafe.tienda.common

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.andescafe.tienda.R

/** Fila genérica de lista: sirve para usuarios, productos, pedidos y carrito. */
data class Row(val title: String, val subtitle: String, val trailing: String = "", val id: String = "", val imageUrl: String = "")

/**
 * Adaptador reutilizable. [layout] puede ser item_row (lista) o item_product (cuadrícula).
 * Si la fila trae imageUrl, la imagen se descarga con Glide; si no, se muestra el ícono por defecto.
 */
class RowAdapter(
    private val layout: Int = R.layout.item_row,
    private val onClick: (Row) -> Unit = {},
    private val onLongClick: (Row) -> Unit = {}
) : RecyclerView.Adapter<RowAdapter.VH>() {

    private val items = mutableListOf<Row>()

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val subtitle: TextView = v.findViewById(R.id.tvSubtitle)
        val trailing: TextView = v.findViewById(R.id.tvTrailing)
        val image: ImageView = v.findViewById(R.id.ivImage)
        val basePadding = image.paddingLeft
    }

    /** Reemplaza el contenido de la lista (llamado desde los listeners de Firestore). */
    fun submit(list: List<Row>) {
        items.clear(); items.addAll(list); notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(layout, parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val row = items[position]
        h.title.text = row.title
        h.subtitle.text = row.subtitle
        h.trailing.text = row.trailing
        h.trailing.visibility = if (row.trailing.isEmpty()) View.GONE else View.VISIBLE
        h.image.clipToOutline = true
        if (row.imageUrl.isNotEmpty()) {
            h.image.setPadding(0, 0, 0, 0)
            h.image.imageTintList = null
            h.image.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(h.image).load(row.imageUrl).into(h.image)
        } else {
            h.image.setPadding(h.basePadding, h.basePadding, h.basePadding, h.basePadding)
            h.image.imageTintList = ColorStateList.valueOf(h.image.context.getColor(R.color.cafe_primary))
            h.image.scaleType = ImageView.ScaleType.FIT_CENTER
            h.image.setImageResource(R.drawable.ic_coffee)
        }
        h.itemView.setOnClickListener { onClick(row) }
        h.itemView.setOnLongClickListener { onLongClick(row); true }
    }
}
