package com.noop.analytics

/** One flame skin, the collectible's draw recipe. All colors are 0xAARRGGBB. */
data class FlameSkin(
    val id: String,
    val name: String,
    val rarity: GoalRewards.Tier,
    val outer: IntArray,   // [top, mid, base]
    val glow: Int,
    val core: IntArray,    // [brightest, mid]
) {
    companion object {
        val ember = FlameSkin("ember", "Ember", GoalRewards.Tier.COMMON,
            intArrayOf(0xFFD27A.toInt(), 0xFFF2B45C.toInt(), 0xFFF0742C.toInt()),
            0xFFF0742C.toInt(), intArrayOf(0xFFFFF3D6.toInt(), 0xFFFFD27A.toInt()))

        val catalog = listOf(
            FlameSkin("sunset", "Sunset", GoalRewards.Tier.COMMON,
                intArrayOf(0xFFFFC1A1.toInt(), 0xFFFF7A59.toInt(), 0xFFE0445A.toInt()),
                0xFFFF7A59.toInt(), intArrayOf(0xFFFFF0E6.toInt(), 0xFFFFC1A1.toInt())),
            FlameSkin("citrus", "Citrus", GoalRewards.Tier.COMMON,
                intArrayOf(0xFFF4FF9A.toInt(), 0xFFD4F04A.toInt(), 0xFF8DBF2A.toInt()),
                0xFFD4F04A.toInt(), intArrayOf(0xFFFFFCE6.toInt(), 0xFFF4FF9A.toInt())),
            FlameSkin("ocean", "Ocean", GoalRewards.Tier.RARE,
                intArrayOf(0xFF9FE7FF.toInt(), 0xFF3BB4F2.toInt(), 0xFF1F6FD1.toInt()),
                0xFF3BB4F2.toInt(), intArrayOf(0xFFF0FBFF.toInt(), 0xFF9FE7FF.toInt())),
            FlameSkin("jade", "Jade", GoalRewards.Tier.RARE,
                intArrayOf(0xFFA8F5CF.toInt(), 0xFF34C98A.toInt(), 0xFF138A5E.toInt()),
                0xFF34C98A.toInt(), intArrayOf(0xFFEFFFF7.toInt(), 0xFFA8F5CF.toInt())),
            FlameSkin("rose", "Rose", GoalRewards.Tier.RARE,
                intArrayOf(0xFFFFD1DC.toInt(), 0xFFF07A9A.toInt(), 0xFFC2456A.toInt()),
                0xFFF07A9A.toInt(), intArrayOf(0xFFFFF2F5.toInt(), 0xFFFFD1DC.toInt())),
            FlameSkin("ice", "Ice", GoalRewards.Tier.EPIC,
                intArrayOf(0xFFFFFFFF.toInt(), 0xFFBFE9FF.toInt(), 0xFF7CC4F0.toInt()),
                0xFFBFE9FF.toInt(), intArrayOf(0xFFFFFFFF.toInt(), 0xFFE6F7FF.toInt())),
            FlameSkin("gold", "Gold", GoalRewards.Tier.EPIC,
                intArrayOf(0xFFFFF1B8.toInt(), 0xFFE9C15A.toInt(), 0xFFB8862B.toInt()),
                0xFFE9C15A.toInt(), intArrayOf(0xFFFFFBEA.toInt(), 0xFFFFF1B8.toInt())),
            FlameSkin("aurora", "Aurora", GoalRewards.Tier.LEGENDARY,
                intArrayOf(0xFF7CF5D8.toInt(), 0xFF5AA9F5.toInt(), 0xFFB678F0.toInt()),
                0xFF5AA9F5.toInt(), intArrayOf(0xFFFFFFFF.toInt(), 0xFFC9FFF1.toInt())),
            FlameSkin("void", "Void", GoalRewards.Tier.LEGENDARY,
                intArrayOf(0xFFE8E6FF.toInt(), 0xFF6B6BFF.toInt(), 0xFF2A2A55.toInt()),
                0xFF6B6BFF.toInt(), intArrayOf(0xFFFFFFFF.toInt(), 0xFFE8E6FF.toInt())),
        )

        /** Ember is the pre-chest default and is NOT in the catalog. */
        fun named(id: String?): FlameSkin = catalog.firstOrNull { it.id == id } ?: ember
    }
}
