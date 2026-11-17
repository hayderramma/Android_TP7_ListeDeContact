package com.example.contactsv3

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel partage au niveau de l'activite : il detient la collection en memoire.
 * Il survit a la rotation et constitue la source de verite commune entre
 * ListeContactsFragment, DetailContactFragment et CarteContactFragment.
 */
class ContactsViewModel : ViewModel() {

    private val _contacts = MutableLiveData(
        listOf(
            Contact(
                id = "c1",
                nom = "Dubois",
                prenom = "Marie",
                telephone = "0612345678",
                email = "marie.dubois@example.com",
                latitude = 48.8566,
                longitude = 2.3522
            ),
            Contact(
                id = "c2",
                nom = "Martin",
                prenom = "Lucas",
                telephone = "0623456789",
                email = "lucas.martin@example.com",
                latitude = 45.7640,
                longitude = 4.8357
            ),
            Contact(
                id = "c3",
                nom = "Bernard",
                prenom = "Sophie",
                telephone = "0634567890",
                email = "sophie.bernard@example.com",
                latitude = 43.2965,
                longitude = 5.3698
            ),
            Contact(
                id = "c4",
                nom = "Petit",
                prenom = "Hugo",
                telephone = "0645678901",
                email = "hugo.petit@example.com"
                // Aucune position definie : cas "coordonnees absentes" a tester.
            ),
            Contact(
                id = "c5",
                nom = "Moreau",
                prenom = "Emma",
                telephone = "0656789012",
                email = "emma.moreau@example.com",
                latitude = 50.6292,
                longitude = 3.0573
            )
        )
    )

    val contacts: LiveData<List<Contact>> = _contacts

    /** Recherche par identifiant stable, jamais par position dans la liste. */
    fun getById(id: String): Contact? = _contacts.value?.firstOrNull { it.id == id }

    /**
     * Remplace le contact par une copie : LiveData emet une nouvelle liste,
     * ce qui declenche immediatement le rafraichissement de la RecyclerView.
     */
    fun update(contact: Contact) {
        val current = _contacts.value.orEmpty()
        _contacts.value = if (current.any { it.id == contact.id }) {
            current.map { if (it.id == contact.id) contact.copy() else it }
        } else {
            current + contact.copy()
        }
    }
}
