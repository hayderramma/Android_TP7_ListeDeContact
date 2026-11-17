package com.example.contactsv3

import android.net.Uri
import android.widget.ImageView

/** Centralise le chargement de la photo et fournit un image de repli robuste. */
object ImageLoader {
    fun load(imageView: ImageView, uriString: String?) {
        if (uriString.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_person)
            return
        }
        try {
            imageView.setImageURI(Uri.parse(uriString))
        } catch (e: Exception) {
            imageView.setImageResource(R.drawable.ic_person)
        }
    }
}
