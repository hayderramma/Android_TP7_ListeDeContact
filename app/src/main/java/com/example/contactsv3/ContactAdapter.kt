package com.example.contactsv3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Adapter RecyclerView + ViewHolder.
 * Le clic transmet l'identifiant stable du contact : aucune erreur d'indice
 * n'est possible apres filtrage.
 */
class ContactAdapter(
    private val onContactClick: (String) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val photo: ImageView = itemView.findViewById(R.id.itemPhoto)
        private val name: TextView = itemView.findViewById(R.id.itemName)
        private val phone: TextView = itemView.findViewById(R.id.itemPhone)

        fun bind(contact: Contact) {
            name.text = contact.nomComplet
            phone.text = contact.telephone
            ImageLoader.load(photo, contact.photoUri)
            itemView.setOnClickListener { onContactClick(contact.id) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Contact>() {
            override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean =
                oldItem == newItem
        }
    }
}
