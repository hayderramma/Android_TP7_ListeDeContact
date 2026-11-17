package com.example.contactsv3

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/** Ecran liste : RecyclerView + recherche instantanee. */
class ListeContactsFragment : Fragment(R.layout.fragment_liste) {

    private val sharedVm: ContactsViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private lateinit var searchEditText: EditText
    private lateinit var emptyTextView: TextView
    private var allContacts: List<Contact> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchEditText = view.findViewById(R.id.searchEditText)
        emptyTextView = view.findViewById(R.id.emptyTextView)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)

        adapter = ContactAdapter { contactId ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, DetailContactFragment.newInstance(contactId))
                .addToBackStack("detail")
                .commit()
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        sharedVm.contacts.observe(viewLifecycleOwner) { contacts ->
            allContacts = contacts
            applyFilter()
        }

        // Filtrage a la saisie, sans bouton de validation. Le texte de l'EditText
        // et la liste du ViewModel sont restaures automatiquement apres rotation.
        searchEditText.doOnTextChanged { _, _, _, _ -> applyFilter() }
    }

    private fun applyFilter() {
        val result = ContactFilter.apply(allContacts, searchEditText.text.toString())
        adapter.submitList(result)
        emptyTextView.visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
    }
}
