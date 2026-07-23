package com.pakquiz.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.pakquiz.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var appUpdateManager: AppUpdateManager

    private val installStateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            promptCompleteUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AdConfig.initializeSdk(this)
        AdConfig.loadBannerInto(this, binding.bannerAdContainer)

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForAppUpdate()

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

        if (PerformanceStore.shouldShowRatingPrompt(this)) {
            showRatingDialog()
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
                        .background = buildTileGradient(ContextCompat.getColor(this, category.colorRes))
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

    /**
     * Builds a diagonal gradient background (lighter top-left to darker bottom-right)
     * from a single base color, entirely in code - no image assets required. This gives
     * each subject tile a richer, less flat look.
     */
    private fun buildTileGradient(baseColor: Int): android.graphics.drawable.GradientDrawable {
        val lighter = shadeColor(baseColor, 1.35f)
        val darker = shadeColor(baseColor, 0.7f)
        val drawable = android.graphics.drawable.GradientDrawable(
            android.graphics.drawable.GradientDrawable.Orientation.TL_BR,
            intArrayOf(lighter, baseColor, darker)
        )
        drawable.cornerRadius = 20 * resources.displayMetrics.density
        return drawable
    }

    /** factor > 1 lightens the color, factor < 1 darkens it (HSV value channel). */
    private fun shadeColor(color: Int, factor: Float): Int {
        val hsv = FloatArray(3)
        android.graphics.Color.colorToHSV(color, hsv)
        hsv[2] = (hsv[2] * factor).coerceIn(0f, 1f)
        return android.graphics.Color.HSVToColor(hsv)
    }

    private fun openCategory(category: Category, questionCount: Int = QuestionBank.QUESTIONS_PER_QUIZ) {
        val intent = Intent(this, QuizActivity::class.java)
        intent.putExtra("category_id", category.id)
        intent.putExtra("category_json", category.jsonFile)
        intent.putExtra("category_title", category.title)
        intent.putExtra("question_count", questionCount)
        startActivity(intent)
    }

    /**
     * Shows the Watch Ad / Skip choice dialog. Both button labels include the exact
     * number of questions each path unlocks, so the choice is fully clear without
     * needing extra description text.
     */
    private fun showRewardDialog(
        title: String,
        watchLabel: String,
        skipLabel: String,
        onWatchAd: () -> Unit,
        onSkip: () -> Unit
    ) {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_reward_choice, null)
        dialog.setContentView(view)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        view.findViewById<TextView>(R.id.dialogTitle).text = title
        val watchBtn = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.watchAdButton)
        val skipBtn = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.skipButton)
        watchBtn.text = watchLabel
        skipBtn.text = skipLabel

        watchBtn.setOnClickListener {
            dialog.dismiss()
            onWatchAd()
        }
        skipBtn.setOnClickListener {
            dialog.dismiss()
            onSkip()
        }

        dialog.show()
        sizeDialogWindow(dialog)
    }

    private fun offerExtendedPractice(category: Category) {
        showRewardDialog(
            title = category.title,
            watchLabel = "WATCH AD \u2013 Unlock 25 Questions",
            skipLabel = "SKIP \u2013 Continue with 10 Questions",
            onWatchAd = {
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
            },
            onSkip = { openCategory(category) }
        )
    }

    private fun offerFullTestBoost() {
        showRewardDialog(
            title = "Full Test Challenge",
            watchLabel = "WATCH AD \u2013 Unlock 200 Questions",
            skipLabel = "SKIP \u2013 Continue with 100 Questions",
            onWatchAd = {
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
            },
            onSkip = { startFullTest() }
        )
    }

    private fun startFullTest(questionCount: Int = QuestionBank.FULL_TEST_SIZE) {
        val intent = Intent(this, FullTestActivity::class.java)
        intent.putExtra("question_count", questionCount)
        startActivity(intent)
    }

    private fun maybeOfferStreakFreeze() {
        if (!PerformanceStore.isStreakAtRisk(this)) return
        PerformanceStore.markStreakPromptShownToday(this)

        showRewardDialog(
            title = "Save Your Streak?",
            watchLabel = "WATCH AD \u2013 Keep My Streak",
            skipLabel = "SKIP \u2013 Let It Reset",
            onWatchAd = {
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
            },
            onSkip = { }
        )
    }

    private fun showRatingDialog() {
        val dialog = android.app.Dialog(this)
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_rating, null)
        dialog.setContentView(view)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.rateNowButton).setOnClickListener {
            PerformanceStore.markRatingHandled(this)
            dialog.dismiss()
            openPlayStoreListing()
        }
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.laterButton).setOnClickListener {
            // Don't mark as handled - this lets the prompt show again after more app opens
            dialog.dismiss()
        }
        view.findViewById<TextView>(R.id.dontAskAgainText).setOnClickListener {
            PerformanceStore.markRatingHandled(this)
            dialog.dismiss()
        }
        dialog.show()
        sizeDialogWindow(dialog)
    }

    private fun sizeDialogWindow(dialog: android.app.Dialog) {
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.88).toInt(),
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun openPlayStoreListing() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: android.content.ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
        }
    }

    override fun onResume() {
        super.onResume()
        refreshDashboard()
        maybeOfferStreakFreeze()

        appUpdateManager.registerListener(installStateListener)
        // If a flexible update finished downloading while the app was in the background,
        // catch it here so the "restart to install" prompt isn't missed.
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                promptCompleteUpdate()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appUpdateManager.unregisterListener(installStateListener)
    }

    /**
     * Checks Play Store for a newer version. Only does anything meaningful when the app
     * was installed from Google Play - safely does nothing when sideloaded (e.g. your
     * debug APK from GitHub Actions), so this is safe to leave active during testing.
     * Uses the FLEXIBLE flow: downloads quietly in the background, then prompts the user
     * to restart and install once ready - never blocks or interrupts studying.
     */
    private fun checkForAppUpdate() {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    this,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build(),
                    UPDATE_REQUEST_CODE
                )
            }
        }
    }

    private fun promptCompleteUpdate() {
        val root = binding.root
        com.google.android.material.snackbar.Snackbar.make(
            root,
            "An update has been downloaded",
            com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
        ).setAction("RESTART") {
            appUpdateManager.completeUpdate()
        }.show()
    }

    companion object {
        private const val UPDATE_REQUEST_CODE = 9001
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
