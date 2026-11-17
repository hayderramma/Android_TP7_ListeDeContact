package com.example.contactsv3

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CoordinatesTest {

    @Test
    fun `coordonnees valides`() {
        assertEquals(48.8566 to 2.3522, Coordinates.parse(" 48.8566 ", "2.3522"))
    }

    @Test
    fun `valeurs limites acceptees`() {
        assertEquals(-90.0 to 180.0, Coordinates.parse("-90", "180"))
    }

    @Test
    fun `valeur non numerique refusee`() {
        assertNull(Coordinates.parse("abc", "2.3"))
        assertNull(Coordinates.parse("48.8", "x"))
    }

    @Test
    fun `hors limites refuse`() {
        assertNull(Coordinates.parse("91", "2.3"))
        assertNull(Coordinates.parse("48.8", "181"))
    }

    @Test
    fun `champ vide refuse`() {
        assertNull(Coordinates.parse("", "2.3"))
        assertNull(Coordinates.parse("48.8", ""))
    }
}
