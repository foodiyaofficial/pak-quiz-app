# Zeenith Prep (formerly Pak Quiz Prep)

A polished, 100% offline Android quiz app for Pakistani students preparing for
NTS, PPSC, FPSC/CSS, ISSB, and university entry tests.

## What's new in this update

- **Renamed** from "Pak Quiz Prep" to **Zeenith Prep**
- **New app icon**: gold star on a deep green background
- **6 new subject categories** added (8 total): Islamic Studies, General
  Knowledge, General Science, Physics, Chemistry, Biology, English, Mathematics
- **Instant answer feedback**: tapping an option immediately shows green
  (correct) / red (wrong), and reveals the correct answer if you got it wrong
- **Review Answers screen**: after finishing a quiz, you can see every
  question again with your answer vs. the correct one
- **Redesigned interface**: emerald-and-gold "luxury" theme, colorful subject
  tiles in a grid (each subject has its own color), card-based question UI,
  progress bar
- **About / Privacy Policy screen**: accessible from the (i) icon on the
  home screen
- **AdMob placeholders added** (banner + interstitial) — see below for how
  to turn these into real ads

---

## Building the new APK (same process as before)

1. Take everything inside this zip and replace the contents of your existing
   `pak-quiz-app` GitHub repo with these files (delete old files first if
   names differ, then upload these — or just drag-and-drop overwrite in
   github.dev, which replaces files with matching names automatically)
2. Commit the changes
3. Go to the **Actions** tab — a new build kicks off automatically
4. Download the new APK from **Artifacts** once it's green, same as before

---

## Adding real AdMob ads (when you're ready)

Right now the app shows **grey placeholder boxes** labeled "AdMob Banner Ad
Placeholder" (on the Home and Result screens) and a **full-screen placeholder
screen** labeled "AdMob Interstitial Ad Placeholder" (shown after finishing a
quiz, before results) — exactly where real ads will go, so you can see the
layout without needing an AdMob account yet.

To turn these into real ads later:

1. Create a free account at https://admob.google.com and register your app
   to get an **App ID** and **Ad Unit IDs** (one for banner, one for
   interstitial)
2. Add the Google Mobile Ads SDK dependency to `app/build.gradle`:
   ```gradle
   implementation 'com.google.android.gms:play-services-ads:23.3.0'
   ```
3. Add your AdMob App ID to `AndroidManifest.xml` inside `<application>`:
   ```xml
   <meta-data
       android:name="com.google.android.gms.ads.APPLICATION_ID"
       android:value="ca-app-pub-xxxxxxxxxxxxxxxx~xxxxxxxxxx"/>
   ```
4. Replace the placeholder `FrameLayout` in `activity_main.xml` /
   `activity_result.xml` with a real `com.google.android.gms.ads.AdView`,
   and load it in code with `AdView.loadAd(AdRequest.Builder().build())`
5. Replace `InterstitialAdActivity`'s placeholder screen with a real
   `InterstitialAd.load(...)` call, and call `.show(activity)` when it's
   ready, then continue to `ResultActivity`

**Important:** Google requires you to use **test ad unit IDs** during
development and testing (using your real IDs before the app is published
can get your AdMob account flagged). Test IDs are published in Google's
AdMob documentation. Tell me when you're ready for this step and I can wire
the real SDK calls in for you — I just can't fetch live Ad Unit IDs since
those are tied to your personal AdMob account.

---

## Adding more questions or categories

Same as before — edit the JSON files directly in github.dev under
`app/src/main/assets/`, or add a new category to the `categories` list in
`QuestionBank.kt` plus a matching JSON file.

## Publishing to Play Store

Still pending — let me know when you're ready and I'll generate the signing
keystore, the release-build GitHub Actions workflow, and help you fill out
the Play Console listing (screenshots, description, the privacy policy page
which the app's About screen text can double as a starting point for).
