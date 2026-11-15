package com.noop.analytics

/** One aura-orb skin, the collectible's draw recipe. Colors are 0xAARRGGBB. */
data class OrbSkin(
    val id: String,
    val name: String,
    val top: Int,
    val bottom: Int,
) {
    companion object {
        val catalog = listOf(
            OrbSkin("orb-dawn", "Dawn", 0xFFB199.toInt(), 0xFF6FA3.toInt()),
            OrbSkin("orb-lagoon", "Lagoon", 0xFF2DE2E6.toInt(), 0xFF6AA9FF.toInt()),
            OrbSkin("orb-orchid", "Orchid", 0xFFC77DFF.toInt(), 0xFFFF8FD8.toInt()),
            OrbSkin("orb-cinder", "Cinder", 0xFFFF7A45.toInt(), 0xFFFFC56B.toInt()),
            OrbSkin("orb-gold", "Gold", 0xFFE9C15A.toInt(), 0xFFFFF1B8.toInt()),
            OrbSkin("orb-void", "Void", 0xFF6B6BFF.toInt(), 0xFFE8E6FF.toInt()),
        )

        /** No default skin: nothing is equipped until a chest grants one. */
        fun named(id: String?): OrbSkin? = catalog.firstOrNull { it.id == id }
    }
}
