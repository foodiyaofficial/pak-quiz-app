package com.pakquiz.app

import android.content.Context

/**
 * Tracks which questions have already been shown to the user, per subject, entirely
 * on-device (SharedPreferences, no network/database dependency - keeps the app 100% offline).
 *
 * Rotation rule: once a question has been shown, it will not be shown again for that
 * subject until every other question in that subject's pool has also been shown at least
 * once. When the pool is exhausted, the history resets automatically and a fresh cycle begins.
 */
object SeenQuestionsStore {

    private const val PREFS_NAME = "seen_questions_prefs"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun key(subjectKey: String) = "seen_$subjectKey"

    fun getSeenIds(context: Context, subjectKey: String): MutableSet<String> {
        val stored = prefs(context).getStringSet(key(subjectKey), emptySet()) ?: emptySet()
        return stored.toMutableSet()
    }

    fun addSeenIds(context: Context, subjectKey: String, ids: Collection<String>) {
        val current = getSeenIds(context, subjectKey)
        current.addAll(ids)
        prefs(context).edit().putStringSet(key(subjectKey), current).apply()
    }

    fun resetSeen(context: Context, subjectKey: String) {
        prefs(context).edit().remove(key(subjectKey)).apply()
    }

    fun replaceSeen(context: Context, subjectKey: String, ids: Collection<String>) {
        prefs(context).edit().putStringSet(key(subjectKey), ids.toSet()).apply()
    }
}
