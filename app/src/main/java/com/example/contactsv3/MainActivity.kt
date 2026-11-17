package com.example.contactsv3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/**
 * Activite unique : elle ne fait que porter le FragmentContainerView
 * et piloter la navigation entre les trois fragments dynamiques.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // FragmentContainerView restaure automatiquement les fragments apres rotation.
        // On n'ajoute la liste que lors de la premiere creation pour ne jamais empiler
        // deux instances du meme ecran.
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener { updateTitle() }
        updateTitle()
    }

    private fun updateTitle() {
        val current = supportFragmentManager.findFragmentById(R.id.fragment_container)
        title = when (current) {
            is CarteContactFragment -> getString(R.string.titre_carte)
            is DetailContactFragment -> getString(R.string.titre_detail)
            else -> getString(R.string.titre_liste)
        }
    }
}
