package com.pakquiz.app

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Local, on-device performance tracking - no server, no account, no network.
 * Tracks totals, streaks, best scores per subject, and "last played" info for the
 * Continue Learning card and daily goal progress.
 */
object PerformanceStore {

    private const val PREFS_NAME = "pakquiz_performance"
    private const val DAILY_GOAL = 50

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /**
     * Call this once after any quiz (subject quiz or Full Test) is completed.
     */
    fun recordQuizCompletion(context: Context, categoryId: String, categoryTitle: String, score: Int, total: Int) {
        val p = prefs(context)
        val editor = p.edit()

        // Totals
        editor.putInt("total_quizzes", p.getInt("total_quizzes", 0) + 1)
        editor.putInt("total_questions", p.getInt("total_questions", 0) + total)
        editor.putInt("total_correct", p.getInt("total_correct", 0) + score)

        // Streak tracking
        val today = todayKey()
        val lastPlayedDate = p.getString("last_played_date", null)
        if (lastPlayedDate != today) {
            val currentStreak = p.getInt("current_streak", 0)
            val newStreak = if (isYesterday(lastPlayedDate)) currentStreak + 1 else 1
            editor.putInt("current_streak", newStreak)
            editor.putString("last_played_date", today)
        }

        // Today's questions-answered progress (resets automatically on a new day)
        val progressDate = p.getString("daily_progress_date", null)
        val todaysCount = if (progressDate == today) p.getInt("daily_progress_count", 0) else 0
        editor.putString("daily_progress_date", today)
        editor.putInt("daily_progress_count", todaysCount + total)

        // Continue Learning info
        editor.putString("last_subject_id", categoryId)
        editor.putString("last_subject_title", categoryTitle)

        editor.apply()
    }

    private fun isYesterday(dateString: String?): Boolean {
        if (dateString == null) return false
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val lastDate = format.parse(dateString) ?: return false
            val diffMillis = Date().time - lastDate.time
            val diffDays = diffMillis / (1000 * 60 * 60 * 24)
            diffDays.toInt() == 1
        } catch (e: Exception) {
            false
        }
    }

    fun getTotalQuizzes(context: Context): Int = prefs(context).getInt("total_quizzes", 0)

    fun getTotalQuestionsSolved(context: Context): Int = prefs(context).getInt("total_questions", 0)

    fun getOverallAccuracy(context: Context): Int {
        val total = prefs(context).getInt("total_questions", 0)
        val correct = prefs(context).getInt("total_correct", 0)
        return if (total == 0) 0 else ((correct.toFloat() / total.toFloat()) * 100).toInt()
    }

    fun getCurrentStreak(context: Context): Int {
        val p = prefs(context)
        val lastPlayedDate = p.getString("last_played_date", null)
        val today = todayKey()
        // If they haven't played today or yesterday, streak is effectively broken (shown as 0)
        if (lastPlayedDate != today && !isYesterday(lastPlayedDate)) return 0
        return p.getInt("current_streak", 0)
    }

    fun getDailyGoal(): Int = DAILY_GOAL

    fun getTodaysProgress(context: Context): Int {
        val p = prefs(context)
        val progressDate = p.getString("daily_progress_date", null)
        return if (progressDate == todayKey()) p.getInt("daily_progress_count", 0) else 0
    }

    fun getLastSubjectId(context: Context): String? = prefs(context).getString("last_subject_id", null)

    fun getLastSubjectTitle(context: Context): String? = prefs(context).getString("last_subject_title", null)

    fun getBestScore(context: Context, categoryId: String): Int =
        context.getSharedPreferences("pakquiz_scores", Context.MODE_PRIVATE).getInt("best_$categoryId", 0)
}
