package com.pakquiz.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.pakquiz.app.databinding.ActivityInterstitialAdBinding

/**
 * Loads and shows a real AdMob interstitial ad.
 * Two usage modes:
 * 1. After a normal quiz finishes (before ResultActivity) - forwards all extras onward.
 * 2. Mid-way through Full Test mode, every 25 questions - just returns a result so the
 *    caller (FullTestActivity) can resume where it left off.
 *
 * Safety net: if the ad fails to load (no network, no fill, etc.) the screen's own
 * "Continue" button still works as a manual fallback, so the user is never stuck.
 */
class InterstitialAdActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInterstitialAdBinding
    private var isResumeFullTest = false
    private var proceeded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInterstitialAdBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isResumeFullTest = intent.getBooleanExtra("resume_full_test", false)

        // Manual fallback - always available in case the ad fails to load or show
        binding.continueButton.setOnClickListener { proceed() }

        AdConfig.initializeSdk(this)
        loadAndShowInterstitial()
    }

    private fun loadAndShowInterstitial() {
        InterstitialAd.load(
            this,
            AdConfig.INTERSTITIAL_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            proceed()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            proceed()
                        }
                    }
                    ad.show(this@InterstitialAdActivity)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // No ad available (offline, no fill, etc). Don't block the user -
                    // the "Continue" button on this screen still works normally.
                }
            }
        )
    }

    private fun proceed() {
        if (proceeded) return
        proceeded = true

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
