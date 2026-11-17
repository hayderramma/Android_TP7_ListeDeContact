package com.example.contactsv3

import java.util.Locale

/**
 * Strategie de filtrage isolee du Fragment : recherche insensible a la casse
 * sur le nom, le prenom et le telephone. Une requete vide renvoie tout.
 */
object ContactFilter {
    fun apply(contacts: List<Contact>, rawQuery: String): List<Contact> {
        val query = rawQuery.trim().lowercase(Locale.getDefault())
        if (query.isEmpty()) return contacts

        return contacts.filter { contact ->
            contact.nom.lowercase(Locale.getDefault()).contains(query) ||
                contact.prenom.lowercase(Locale.getDefault()).contains(query) ||
                contact.telephone.lowercase(Locale.getDefault()).contains(query)
        }
    }
}
