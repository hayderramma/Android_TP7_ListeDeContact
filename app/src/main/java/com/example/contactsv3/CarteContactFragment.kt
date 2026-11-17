package com.example.contactsv3

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * Affiche la position d'un contact deja enregistree.
 * Aucune autorisation de localisation n'est demandee : on utilise uniquement
 * les coordonnees transmises dans le Bundle.
 */
class CarteContactFragment : Fragment(R.layout.fragment_carte) {

    private lateinit var mapView: MapView
    private var mapViewInitialized = false

    companion object {
        const val ARG_ID = "contact_id"
        const val ARG_NAME = "contact_name"
        const val ARG_LAT = "latitude"
        const val ARG_LON = "longitude"

        fun newInstance(
            name: String,
            latitude: Double,
            longitude: Double,
            contactId: String
        ): CarteContactFragment = CarteContactFragment().apply {
            arguments = bundleOf(
                ARG_ID to contactId,
                ARG_NAME to name,
                ARG_LAT to latitude,
                ARG_LON to longitude
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageView = view.findViewById<TextView>(R.id.mapMessage)
        mapView = view.findViewById(R.id.mapView)

        val latitude = arguments?.getDouble(ARG_LAT, Double.NaN) ?: Double.NaN
        val longitude = arguments?.getDouble(ARG_LON, Double.NaN) ?: Double.NaN
        val name = arguments?.getString(ARG_NAME).orEmpty().ifBlank { getString(R.string.sans_nom) }

        val coordinatesValid = latitude.isFinite() &&
            longitude.isFinite() &&
            latitude in -90.0..90.0 &&
            longitude in -180.0..180.0

        if (!coordinatesValid) {
            mapView.visibility = View.GONE
            messageView.visibility = View.VISIBLE
            messageView.text = getString(R.string.coords_indisponibles_carte)
        } else {
            messageView.visibility = View.GONE
            mapView.visibility = View.VISIBLE
            mapView.onCreate(savedInstanceState)
            mapViewInitialized = true
            mapView.getMapAsync { googleMap: GoogleMap ->
                val position = LatLng(latitude, longitude)
                googleMap.addMarker(MarkerOptions().position(position).title(name))
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15f))
            }
        }

        view.findViewById<Button>(R.id.btnBackFromMap).setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onStart() {
        super.onStart()
        if (mapViewInitialized) mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (mapViewInitialized) mapView.onResume()
    }

    override fun onPause() {
        if (mapViewInitialized) mapView.onPause()
        super.onPause()
    }

    override fun onStop() {
        if (mapViewInitialized) mapView.onStop()
        super.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (mapViewInitialized) mapView.onSaveInstanceState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        if (mapViewInitialized) mapView.onLowMemory()
        super.onLowMemory()
    }

    override fun onDestroyView() {
        if (mapViewInitialized) {
            mapView.onDestroy()
            mapViewInitialized = false
        }
        super.onDestroyView()
    }
}
