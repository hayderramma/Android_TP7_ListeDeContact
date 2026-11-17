package com.example.contactsv3

import org.junit.Assert.assertEquals
import org.junit.Test

class ContactFilterTest {

    private val contacts = listOf(
        Contact(id = "1", nom = "Dubois", prenom = "Marie", telephone = "0612345678"),
        Contact(id = "2", nom = "Martin", prenom = "Lucas", telephone = "0623456789"),
        Contact(id = "3", nom = "Bernard", prenom = "Sophie", telephone = "0634567890")
    )

    @Test
    fun `requete vide renvoie tous les contacts`() {
        assertEquals(contacts, ContactFilter.apply(contacts, ""))
        assertEquals(contacts, ContactFilter.apply(contacts, "   "))
    }

    @Test
    fun `filtrage par nom insensible a la casse`() {
        assertEquals(listOf("1"), ContactFilter.apply(contacts, "duBOIS").map { it.id })
    }

    @Test
    fun `filtrage par prenom`() {
        assertEquals(listOf("2"), ContactFilter.apply(contacts, "lucas").map { it.id })
    }

    @Test
    fun `filtrage par telephone`() {
        assertEquals(listOf("3"), ContactFilter.apply(contacts, "6345").map { it.id })
    }

    @Test
    fun `recherche sans resultat renvoie une liste vide`() {
        assertEquals(emptyList<Contact>(), ContactFilter.apply(contacts, "inconnu"))
    }
}
