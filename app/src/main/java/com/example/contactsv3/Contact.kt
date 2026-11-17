package com.example.contactsv3

import java.util.UUID

/**
 * Modele unique de l'application : toute l'interface (liste, detail, carte)
 * est alimentee a partir de ce modele, jamais de valeurs figees dans les vues.
 */
data class Contact(
    val id: String = UUID.randomUUID().toString(),
    var nom: String = "",
    var prenom: String = "",
    var telephone: String = "",
    var email: String = "",
    var photoUri: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
) {
    val nomComplet: String
        get() = listOf(prenom, nom).filter { it.isNotBlank() }.joinToString(" ")

    val aPosition: Boolean
        get() = latitude != null && longitude != null
}
