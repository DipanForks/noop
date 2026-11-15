package com.noop.analytics

import android.content.Context

/**
 * The two AI companion switches, kept in the shared `noop_prefs` file so the settings rows,
 * the goal-reward face rows, and the widget publisher all read the same two booleans.
 */
object NoopAISwitches {
    private const val PREFS = "noop_prefs"
    const val COMPANION_KEY = "noop.ai.companion"
    const val BISCUIT_KEY = "noop.ai.biscuit"

    fun companionOn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(COMPANION_KEY, true)

    fun biscuit(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(BISCUIT_KEY, false)

    fun setCompanion(context: Context, on: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(COMPANION_KEY, on).apply()
    }

    fun setBiscuit(context: Context, on: Boolean) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putBoolean(BISCUIT_KEY, on).apply()
    }

    /** Pure biscuit rule: the biscuit only appears with both its own switch and the companion master. */
    fun biscuitOn(companion: Boolean, biscuit: Boolean): Boolean = companion && biscuit

    fun biscuitOn(context: Context): Boolean = biscuitOn(companionOn(context), biscuit(context))

    /** First-run settle: establish the companion key from provider availability only when nothing is stored yet. */
    fun settle(context: Context, hasProvider: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.contains(COMPANION_KEY)) {
            prefs.edit().putBoolean(COMPANION_KEY, hasProvider).apply()
        }
    }
}
