package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.pakquiz.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdConfig.initializeSdk(this)
        AdConfig.loadBannerInto(this, binding.bannerAdContainer)

        buildCategoryGrid()

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.fullTestInclude.root.setOnClickListener {
            offerFullTestBoost()
        }

        binding.resumeButton.setOnClickListener {
            val lastId = PerformanceStore.getLastSubjectId(this) ?: return@setOnClickListener
            if (lastId == "fulltest") {
                startActivity(Intent(this, FullTestActivity::class.java))
                return@setOnClickListener
            }
            val category = QuestionBank.categories.find { it.id == lastId } ?: return@setOnClickListener
            openCategory(category)
        }

    }

    /**
     * Builds the subject grid as plain LinearLayout rows (2 tiles per row) instead of a
     * RecyclerView. This avoids a known Android bug where a RecyclerView with wrap_content
     * height nested inside a ScrollView sometimes only measures/renders enough rows to fill
     * the initial visible screen instead of the full list - which was cutting the subject
     * list off after 6 tiles regardless of how many categories actually existed.
     */
    private fun buildCategoryGrid() {
        binding.categoryGridContainer.removeAllViews()
        val categories = QuestionBank.categories
        val inflater = LayoutInflater.from(this)

        var i = 0
        while (i < categories.size) {
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            for (col in 0 until 2) {
                val tileWrapper = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                val marginPx = (8 * resources.displayMetrics.density).toInt()
                tileWrapper.setMargins(marginPx, marginPx, marginPx, marginPx)
                if (i < categories.size) {
                    val category = categories[i]
                    val tile = inflater.inflate(R.layout.item_category, row, false)
                    tile.layoutParams = tileWrapper

                    tile.findViewById<TextView>(R.id.categoryEmoji).text = category.emoji
                    tile.findViewById<TextView>(R.id.categoryTitle).text = category.title
                    tile.findViewById<TextView>(R.id.categorySubtitle).text = category.subtitle
                    tile.findViewById<LinearLayout>(R.id.cardBackground)
                        .setBackgroundColor(ContextCompat.getColor(this, category.colorRes))
                    tile.setOnClickListener { offerExtendedPractice(category) }

                    row.addView(tile)
                    i++
                } else {
                    // Empty spacer to keep the last row's single tile from stretching full-width
                    val spacer = android.view.View(this)
                    spacer.layoutParams = tileWrapper
                    row.addView(spacer)
                }
            }

            binding.categoryGridContainer.addView(row)
        }
    }

    private fun openCategory(category: Category, questionCount: Int = QuestionBank.QUESTIONS_PER_QUIZ) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("category_id", category.id)
        intent.putExtra("category_json", category.jsonFile)
        intent.putExtra("category_title", category.title)
        intent.putExtra("question_count", questionCount)
        startActivity(intent)
    }

    private fun offerExtendedPractice(category: Category) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Boost Your Practice")
            .setMessage("Watch a short ad to unlock 25 questions for ${category.title} instead of the standard 10.")
            .setPositiveButton("Watch Ad \u2013 25 Questions") { _, _ ->
                AdConfig.loadAndShowRewarded(
                    this,
                    onRewardEarned = { openCategory(category, 25) },
                    onUnavailable = {
                        android.widget.Toast.makeText(
                            this,
                            "No ad available right now - starting with 10 questions",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        openCategory(category)
                    }
                )
            }
            .setNegativeButton("Skip \u2013 10 Questions") { _, _ -> openCategory(category) }
            .setCancelable(false)
            .show()
    }

    private fun offerFullTestBoost() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Boost Your Full Test")
            .setMessage("Watch a short ad to unlock 200 mixed questions instead of the standard 100.")
            .setPositiveButton("Watch Ad \u2013 200 Questions") { _, _ ->
                AdConfig.loadAndShowRewarded(
                    this,
                    onRewardEarned = { startFullTest(200) },
                    onUnavailable = {
                        android.widget.Toast.makeText(
                            this,
                            "No ad available right now - starting with 100 questions",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        startFullTest()
                    }
                )
            }
            .setNegativeButton("Skip \u2013 100 Questions") { _, _ -> startFullTest() }
            .setCancelable(false)
            .show()
    }

    private fun startFullTest(questionCount: Int = QuestionBank.FULL_TEST_SIZE) {
        val intent = Intent(this, FullTestActivity::class.java)
        intent.putExtra("question_count", questionCount)
        startActivity(intent)
    }

    private fun maybeOfferStreakFreeze() {
        if (!PerformanceStore.isStreakAtRisk(this)) return
        PerformanceStore.markStreakPromptShownToday(this)

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Don't Lose Your Streak! \uD83D\uDD25")
            .setMessage("You missed a day, but you can watch a short ad to keep your streak alive.")
            .setPositiveButton("Watch Ad") { _, _ ->
                AdConfig.loadAndShowRewarded(
                    this,
                    onRewardEarned = {
                        PerformanceStore.useStreakFreeze(this)
                        refreshDashboard()
                        android.widget.Toast.makeText(this, "Streak saved!", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onUnavailable = {
                        android.widget.Toast.makeText(
                            this,
                            "No ad available right now - please try again later",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
            .setNegativeButton("No Thanks", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
        maybeOfferStreakFreeze()
    }

    private fun refreshDashboard() {
        binding.streakValue.text = PerformanceStore.getCurrentStreak(this).toString()
        binding.solvedValue.text = formatCount(PerformanceStore.getTotalQuestionsSolved(this))
        binding.accuracyValue.text = "${PerformanceStore.getOverallAccuracy(this)}%"

        val todaysProgress = PerformanceStore.getTodaysProgress(this)
        val goal = PerformanceStore.getDailyGoal()
        binding.goalProgressText.text = "$todaysProgress / $goal Questions"
        binding.goalProgressBar.max = goal
        binding.goalProgressBar.progress = todaysProgress.coerceAtMost(goal)

        val lastSubjectTitle = PerformanceStore.getLastSubjectTitle(this)
        if (lastSubjectTitle != null) {
            binding.continueLearningCard.visibility = android.view.View.VISIBLE
            binding.continueSubjectTitle.text = lastSubjectTitle
        } else {
            binding.continueLearningCard.visibility = android.view.View.GONE
        }
    }

    private fun formatCount(count: Int): String {
        return if (count >= 1000) String.format("%.1fk", count / 1000.0) else count.toString()
    }
}
