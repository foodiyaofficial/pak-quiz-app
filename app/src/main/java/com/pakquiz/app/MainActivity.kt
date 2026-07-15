package com.pakquiz.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.pakquiz.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.categoryRecyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.categoryRecyclerView.adapter = CategoryAdapter(QuestionBank.categories) { category ->
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("category_id", category.id)
            intent.putExtra("category_json", category.jsonFile)
            intent.putExtra("category_title", category.title)
            startActivity(intent)
        }

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
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("category_id", category.id)
            intent.putExtra("category_json", category.jsonFile)
            intent.putExtra("category_title", category.title)
            startActivity(intent)
        }

        // TODO (AdMob): once you have a real Ad Unit ID, replace bannerAdContainer's placeholder
        // TextView with a com.google.android.gms.ads.AdView and call adView.loadAd(AdRequest.Builder().build())
        // See README section "Adding real AdMob ads" for the exact steps.
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
