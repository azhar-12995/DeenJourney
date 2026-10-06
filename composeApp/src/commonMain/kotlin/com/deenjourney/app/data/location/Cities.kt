package com.deenjourney.app.data.location

import com.deenjourney.app.core.Lang
import com.deenjourney.app.data.db.ReadOnlyDb
import com.deenjourney.app.data.db.str
import com.deenjourney.app.data.db.strOrNull
import kotlin.math.cos

data class City(val id: Long, val name: String, val lat: Double, val lng: Double, val cc: String, val country: String, val pop: Long, val tz: String)

/** Offline world cities (GeoNames cities15000, ~26k places) + CLDR country names. */
class CitiesRepo(private val db: ReadOnlyDb) {
    private val aliases = mapOf("makkah" to "Mecca", "makka" to "Mecca", "madinah" to "Medina", "madina" to "Medina", "al madinah" to "Medina",
        "bombay" to "Mumbai", "calcutta" to "Kolkata", "dacca" to "Dhaka", "peking" to "Beijing", "quds" to "Jerusalem", "al quds" to "Jerusalem")

    private fun countryCol(lang: Lang) = when (lang) { Lang.EN -> "co.en"; Lang.UR -> "coalesce(co.ur, co.en)"; Lang.AR -> "coalesce(co.ar, co.en)" }

    suspend fun search(query: String, lang: Lang, limit: Int = 30): List<City> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val name = aliases[q.lowercase()] ?: q
        return db.query(
            "SELECT c.id,c.name,c.lat,c.lng,c.cc,${countryCol(lang)},c.pop,c.tz FROM city c LEFT JOIN country co ON co.cc=c.cc " +
                "WHERE c.ascii LIKE ? OR c.name LIKE ? ORDER BY (c.ascii LIKE ? OR c.name LIKE ?) DESC, c.pop DESC LIMIT ?",
            "$name%", "$name%", name, name, limit,
        ) { City(it.getLong(0), it.str(1), it.getDouble(2), it.getDouble(3), it.str(4), it.strOrNull(5) ?: it.str(4), it.getLong(6), it.str(7)) }
    }

    /** Nearest populated place to a GPS fix (for a friendly name + the IANA time zone). */
    suspend fun nearest(lat: Double, lng: Double, lang: Lang): City? {
        val d = 1.0
        val k = cos(lat * kotlin.math.PI / 180).coerceAtLeast(0.2)
        val rows = db.query(
            "SELECT c.id,c.name,c.lat,c.lng,c.cc,${countryCol(lang)},c.pop,c.tz FROM city c LEFT JOIN country co ON co.cc=c.cc WHERE c.lat BETWEEN ? AND ? AND c.lng BETWEEN ? AND ?",
            lat - d, lat + d, lng - d / k, lng + d / k,
        ) { City(it.getLong(0), it.str(1), it.getDouble(2), it.getDouble(3), it.str(4), it.strOrNull(5) ?: it.str(4), it.getLong(6), it.str(7)) }
        return rows.minByOrNull { (it.lat - lat) * (it.lat - lat) + ((it.lng - lng) * k) * ((it.lng - lng) * k) - kotlin.math.ln((it.pop + 1).toDouble()) * 1e-5 }
            ?: if (d < 5) null else null
    }

    suspend fun countryName(cc: String, lang: Lang): String? = db.one("SELECT ${countryCol(lang).replace("co.", "")} FROM country WHERE cc=?", cc) { it.strOrNull(0) }
}
