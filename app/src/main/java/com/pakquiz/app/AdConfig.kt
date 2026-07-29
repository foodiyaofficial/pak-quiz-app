package com.pakquiz.app

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * All AdMob IDs live here in one place so you only ever need to edit this single file
 * once your real AdMob account and ad units are ready.
 *
 * IMPORTANT: The IDs below are Google's official TEST ad unit IDs. They always work and
 * are safe to ship while developing/testing - they will never show real ads or earn revenue,
 * but they also won't get your AdMob account flagged for invalid traffic (which CAN happen
 * if you click your own real ads while testing). Replace them with your real IDs from the
 * AdMob console only when you are ready to publish.
 */
object AdConfig {
    // Real PakQuiz banner ad unit
    const val BANNER_AD_UNIT_ID = "ca-app-pub-4944863890770793/6258068963"

    // Real PakQuiz interstitial ad unit
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-4944863890770793/8408067079"

    // Google's official test rewarded ad unit ID - you haven't created a real rewarded
    // ad unit yet. Once you do (for Streak Freeze / Extended Practice), replace this line.
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    private var sdkInitialized = false

    /** Call once, early (MainActivity.onCreate) before loading any ads. */
    fun initializeSdk(context: Context) {
        if (sdkInitialized) return
        MobileAds.initialize(context) { }
        sdkInitialized = true
    }

    /** Creates and loads a banner ad into the given container. Safe to call from any screen. */
    fun loadBannerInto(context: Context, container: FrameLayout) {
        container.removeAllViews()
        val adView = AdView(context)
        adView.adUnitId = BANNER_AD_UNIT_ID
        adView.setAdSize(AdSize.BANNER)
        container.addView(adView)
        adView.loadAd(AdRequest.Builder().build())
    }

    /**
     * Loads and shows a rewarded ad. onRewardEarned fires only if the user actually
     * watched the ad to completion. onUnavailable fires if the ad fails to load (offline,
     * no fill, etc) - callers should handle this by simply not granting the reward, without
     * blocking anything else in the app.
     */
    fun loadAndShowRewarded(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onUnavailable: () -> Unit
    ) {
        RewardedAd.load(
            activity,
            REWARDED_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                            onUnavailable()
                        }
                    }
                    ad.show(activity) { onRewardEarned() }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    onUnavailable()
                }
            }
        )
    }
}
