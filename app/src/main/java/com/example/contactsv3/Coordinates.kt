package com.example.contactsv3

/**
 * Validation des coordonnees GPS, isolee du Fragment pour etre testable.
 * Une paire est valide si les deux valeurs sont numeriques et dans les limites.
 */
object Coordinates {
    fun parse(latitudeText: String, longitudeText: String): Pair<Double, Double>? {
        val latitude = latitudeText.trim().toDoubleOrNull() ?: return null
        val longitude = longitudeText.trim().toDoubleOrNull() ?: return null
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return null
        return latitude to longitude
    }
}
