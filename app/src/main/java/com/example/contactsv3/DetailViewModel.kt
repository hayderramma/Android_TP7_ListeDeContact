package com.example.contactsv3

import androidx.lifecycle.ViewModel

/**
 * Brouillon de modification, conserve dans un ViewModel propre au fragment Detail.
 * Les coordonnees y sont stockees sous forme de texte brut : ainsi une rotation
 * d'ecran ne perd pas une saisie en cours, meme invalide ou partielle.
 */
data class ContactDraft(
    var nom: String = "",
    var prenom: String = "",
    var telephone: String = "",
    var email: String = "",
    var photoUri: String? = null,
    var latitude: String = "",
    var longitude: String = ""
)

class DetailViewModel : ViewModel() {
    var draft: ContactDraft? = null
        private set

    fun initialize(contact: Contact) {
        if (draft == null) {
            draft = ContactDraft(
                nom = contact.nom,
                prenom = contact.prenom,
                telephone = contact.telephone,
                email = contact.email,
                photoUri = contact.photoUri,
                latitude = contact.latitude?.toString() ?: "",
                longitude = contact.longitude?.toString() ?: ""
            )
        }
    }
}
