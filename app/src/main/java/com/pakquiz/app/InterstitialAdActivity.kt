package com.pakquiz.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pakquiz.app.databinding.ActivityInterstitialAdBinding

/**
 * Placeholder screen simulating where a real AdMob interstitial ad would be shown.
 * Two usage modes:
 * 1. After a normal quiz finishes (before ResultActivity) - forwards all extras onward.
 * 2. Mid-way through Full Test mode, every 25 questions - just returns a result so the
 *    caller (FullTestActivity) can resume where it left off.
 * See README "Adding real AdMob ads" for how to wire in a real ad here.
 */
class InterstitialAdActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityInterstitialAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val isResumeFullTest = intent.getBooleanExtra("resume_full_test", false)

        binding.continueButton.setOnClickListener {
            if (isResumeFullTest) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                val resultIntent = Intent(this, ResultActivity::class.java)
                resultIntent.putExtras(intent)
                startActivity(resultIntent)
                finish()
            }
        }
    }
}
