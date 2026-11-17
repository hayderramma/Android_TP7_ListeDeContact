package com.example.contactsv3

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import java.io.File

/** Consultation, modification et actions du contact. */
class DetailContactFragment : Fragment(R.layout.fragment_detail) {

    private val sharedVm: ContactsViewModel by activityViewModels()
    private lateinit var detailVm: DetailViewModel
    private lateinit var contactId: String

    private lateinit var photoImageView: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText
    private lateinit var tvCoordonnees: TextView

    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private var cameraOutputUri: Uri? = null

    companion object {
        const val ARG_ID = "contact_id"

        fun newInstance(id: String): DetailContactFragment = DetailContactFragment().apply {
            arguments = bundleOf(ARG_ID to id)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri == null) {
                toast(R.string.msg_gallery_cancelled)
            } else {
                takePersistentReadPermission(uri)
                setPhoto(uri)
            }
        }

        cameraLauncher = registerForActivityResult(TakePictureContract()) { success ->
            if (success && cameraOutputUri != null) {
                setPhoto(cameraOutputUri!!)
            } else {
                toast(R.string.msg_camera_cancelled)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactId = requireArguments().getString(ARG_ID).orEmpty()
        val contact = sharedVm.getById(contactId)
        if (contact == null || contactId.isEmpty()) {
            toast(R.string.msg_contact_introuvable)
            parentFragmentManager.popBackStack()
            return
        }

        detailVm = ViewModelProvider(this)[DetailViewModel::class.java]
        detailVm.initialize(contact)

        bindViews(view)
        fillFieldsFromDraft()
        setupTextWatchers()
        setupButtons(view)
    }

    private fun bindViews(view: View) {
        photoImageView = view.findViewById(R.id.detailPhoto)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)
        tvCoordonnees = view.findViewById(R.id.tvCoordonnees)
    }

    private fun fillFieldsFromDraft() {
        val draft = detailVm.draft ?: return
        etNom.setText(draft.nom)
        etPrenom.setText(draft.prenom)
        etTelephone.setText(draft.telephone)
        etEmail.setText(draft.email)
        etLatitude.setText(draft.latitude)
        etLongitude.setText(draft.longitude)
        ImageLoader.load(photoImageView, draft.photoUri)
        updateCoordsSummary()
    }

    private fun setupTextWatchers() {
        listOf(etNom, etPrenom, etTelephone, etEmail, etLatitude, etLongitude).forEach { field ->
            field.doOnTextChanged { _, _, _, _ ->
                syncDraft()
                if (field === etLatitude || field === etLongitude) updateCoordsSummary()
            }
        }
    }

    private fun setupButtons(view: View) {
        view.findViewById<Button>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        view.findViewById<Button>(R.id.btnCamera).setOnClickListener {
            try {
                cameraOutputUri = createCameraUri()
                cameraLauncher.launch(cameraOutputUri!!)
            } catch (e: ActivityNotFoundException) {
                toast(R.string.msg_camera_indisponible)
            } catch (e: Exception) {
                toast(R.string.msg_camera_indisponible)
            }
        }

        view.findViewById<Button>(R.id.btnCall).setOnClickListener { callContact() }
        view.findViewById<Button>(R.id.btnEmail).setOnClickListener { sendEmail() }
        view.findViewById<Button>(R.id.btnMap).setOnClickListener { openMap() }

        view.findViewById<Button>(R.id.btnSave).setOnClickListener {
            if (saveContact()) parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.btnCancel).setOnClickListener {
            toast(R.string.msg_cancel)
            parentFragmentManager.popBackStack()
        }
    }

    private fun syncDraft() {
        detailVm.draft?.apply {
            nom = etNom.text.toString()
            prenom = etPrenom.text.toString()
            telephone = etTelephone.text.toString()
            email = etEmail.text.toString()
            latitude = etLatitude.text.toString()
            longitude = etLongitude.text.toString()
        }
    }

    private fun updateCoordsSummary() {
        val lat = etLatitude.text.toString().trim()
        val lon = etLongitude.text.toString().trim()
        tvCoordonnees.text = if (lat.isEmpty() && lon.isEmpty()) {
            getString(R.string.coords_absentes)
        } else {
            getString(R.string.coords_summary, lat.ifEmpty { "—" }, lon.ifEmpty { "—" })
        }
    }

    private fun setPhoto(uri: Uri) {
        detailVm.draft?.photoUri = uri.toString()
        ImageLoader.load(photoImageView, uri.toString())
    }

    private fun takePersistentReadPermission(uri: Uri) {
        try {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } catch (e: SecurityException) {
            // Certains fournisseurs refusent la persistance : l'Uri reste lisible
            // pour la session en cours, le repli est gere par ImageLoader.
        }
    }

    private fun createCameraUri(): Uri {
        val directory = File(requireContext().filesDir, "photos").apply { mkdirs() }
        val file = File(directory, "contact_${System.currentTimeMillis()}.jpg")
        return androidx.core.content.FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            file
        )
    }

    private fun callContact() {
        val number = detailVm.draft?.telephone?.trim().orEmpty()
        if (number.isEmpty()) {
            toast(R.string.msg_telephone_vide)
            return
        }
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_call_indisponible)
        }
    }

    private fun sendEmail() {
        val address = detailVm.draft?.email?.trim().orEmpty()
        if (address.isEmpty()) {
            toast(R.string.msg_email_vide)
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$address")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject))
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            toast(R.string.msg_email_indisponible)
        }
    }

    /**
     * Valide les deux coordonnees. Renvoie null si elles sont absentes,
     * partielles, non numeriques ou hors limites (avec message dans le champ).
     */
    private fun parseCoordinates(showErrors: Boolean): Pair<Double, Double>? {
        syncDraft()
        val latText = etLatitude.text.toString().trim()
        val lonText = etLongitude.text.toString().trim()

        if (latText.isEmpty() || lonText.isEmpty()) {
            if (showErrors) {
                etLatitude.error = getString(R.string.erreur_coords_paire)
                etLongitude.error = getString(R.string.erreur_coords_paire)
            }
            return null
        }

        val parsed = Coordinates.parse(latText, lonText)
        if (parsed == null) {
            if (showErrors) {
                val latitude = latText.toDoubleOrNull()
                if (latitude == null || latitude !in -90.0..90.0) {
                    etLatitude.error = getString(R.string.erreur_latitude)
                } else {
                    etLongitude.error = getString(R.string.erreur_longitude)
                }
            }
            return null
        }

        return parsed
    }

    private fun hasAnyCoordinate(): Boolean =
        etLatitude.text.toString().trim().isNotEmpty() ||
            etLongitude.text.toString().trim().isNotEmpty()

    private fun saveContact(): Boolean {
        syncDraft()
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim()

        when {
            nom.isEmpty() -> {
                etNom.error = getString(R.string.erreur_nom)
                return false
            }
            prenom.isEmpty() -> {
                etPrenom.error = getString(R.string.erreur_prenom)
                return false
            }
            telephone.isEmpty() -> {
                etTelephone.error = getString(R.string.erreur_telephone)
                return false
            }
            email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                etEmail.error = getString(R.string.erreur_email)
                return false
            }
        }

        val coords = if (hasAnyCoordinate()) parseCoordinates(true) else null
        if (hasAnyCoordinate() && coords == null) return false

        val updated = Contact(
            id = contactId,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = detailVm.draft?.photoUri,
            latitude = coords?.first,
            longitude = coords?.second
        )

        sharedVm.update(updated)
        toast(R.string.msg_saved)
        return true
    }

    private fun openMap() {
        if (!hasAnyCoordinate()) {
            toast(R.string.msg_coords_absentes_carte)
            return
        }

        val coords = parseCoordinates(true) ?: return
        val draft = detailVm.draft
        val name = listOf(draft?.prenom.orEmpty(), draft?.nom.orEmpty())
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .ifBlank { getString(R.string.sans_nom) }

        parentFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                CarteContactFragment.newInstance(name, coords.first, coords.second, contactId)
            )
            .addToBackStack("carte")
            .commit()
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun toast(resId: Int) = toast(getString(resId))
}
