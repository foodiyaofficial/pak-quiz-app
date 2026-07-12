# Pak Quiz Prep

A 100% offline Android quiz app for Pakistani students — Islamic Studies + General Knowledge / test prep (NTS, PPSC, CSS, etc).

- No backend, no server, no APIs, no internet permission
- No sign-up
- All questions are bundled inside the app as JSON files
- Scores are stored only on the user's device (SharedPreferences)

---

## How the APK gets built (no software installation needed on your side)

Since you can't install Android Studio, this project uses **GitHub Actions** — a free
build robot that lives on GitHub's servers. Every time you push code to GitHub, it
automatically compiles a working `.apk` file for you, entirely in the browser. You
never install anything.

---

## Step 1 — Create a GitHub account (if you don't have one)

Go to https://github.com and sign up for free.

## Step 2 — Create a new repository

1. Click the **+** icon (top right) → **New repository**
2. Name it `pak-quiz-app` (or anything you like)
3. Keep it **Public**
4. Do NOT check "Add a README" (we already have one)
5. Click **Create repository**

## Step 3 — Upload these files to GitHub (no software needed)

1. On your new repo's page, click **"uploading an existing file"** (or the "Add file" → "Upload files" button)
2. Open the `pakquiz` folder I've given you, and drag **the entire folder contents** (not the outer zip) into the browser upload box
   - Make sure the folder structure stays intact: `app/`, `.github/`, `build.gradle`, `settings.gradle`, etc.
   - GitHub's drag-and-drop preserves subfolders as long as you drag folders, not just files
3. Scroll down, write a commit message like "Initial commit", and click **Commit changes**

**Tip:** If drag-and-drop of folders doesn't work well in your browser, GitHub also
has a "GitHub Desktop"-free option: use the **github.dev** web editor
(press `.` while viewing your empty repo) which lets you create files/folders
directly in the browser and paste content in — no installation required either way.

## Step 4 — Let GitHub Actions build your APK

1. Once you commit, go to the **Actions** tab on your repository
2. You'll see a workflow called **"Build APK"** running automatically (it triggers on every push to `main`)
3. Wait 2–5 minutes for it to finish (green checkmark = success)
4. Click on the completed run → scroll down to **Artifacts** → download **PakQuiz-debug-apk**
5. This gives you a `.zip` — inside is your `app-debug.apk`. That's your working Android app!

You can now transfer this APK to any Android phone and install it directly (enable
"Install from unknown sources" once) to test it.

## Step 5 — Publish to Google Play Store

1. Create a Google Play Console account at https://play.google.com/console
   (one-time $25 registration fee — set by Google, unavoidable for any publisher)
2. Click **Create app**, fill in app name, description, category (Education), etc.
3. You'll need a **signed release build**, not the debug APK. To do this without
   installing anything locally, tell me once you're at this step and I'll add a
   second GitHub Actions workflow that generates a signed release **.aab** (Android App Bundle)
   using a keystore you generate once (I can also generate that keystore for you here).
4. Upload the `.aab` file under **Production → Create release**
5. Fill in store listing details (screenshots, icon, description, privacy policy — required even for apps with no data collection; a one-line "This app collects no data" page is enough, I can generate that too)
6. Submit for review (Google usually takes 1–7 days for a new app)

---

## Adding more questions later

Just open `app/src/main/assets/questions_islamiat.json` or `questions_gk.json`
directly in GitHub's web editor (click the file → pencil/edit icon) and add more
entries in the same format:

```json
{"question": "Your question?", "options": ["A", "B", "C", "D"], "correctIndex": 0}
```

`correctIndex` is 0 for option A, 1 for B, 2 for C, 3 for D. Save (commit), and
GitHub Actions will automatically rebuild the APK with your new questions.

## Adding more categories later

Add a new entry to the `categories` list in
`app/src/main/java/com/pakquiz/app/QuestionBank.kt`, and add a matching JSON file
in `app/src/main/assets/`.
