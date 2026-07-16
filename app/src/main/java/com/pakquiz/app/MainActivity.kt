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

        buildCategoryGrid()

        binding.aboutButton.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        binding.fullTestInclude.root.setOnClickListener {
            startActivity(Intent(this, FullTestActivity::class.java))
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

        // TODO (AdMob): once you have a real Ad Unit ID, replace bannerAdContainer's placeholder
        // TextView with a com.google.android.gms.ads.AdView and call adView.loadAd(AdRequest.Builder().build())
        // See README section "Adding real AdMob ads" for the exact steps.
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
                    tile.setOnClickListener { openCategory(category) }

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

    private fun openCategory(category: Category) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("category_id", category.id)
        intent.putExtra("category_json", category.jsonFile)
        intent.putExtra("category_title", category.title)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
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
