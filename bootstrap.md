# BOOTSTRAP INSTRUCTIONS FOR CLAUDE CODE
# Read this first. Then read Shiva.prompt. Then begin.

## 0. ORIENTATION
- Examine the current working directory fully before doing anything
- Run: find . -type f | head -60 to understand what already exists
- Run: cat Shiva.prompt to load the full build specification
- Confirm you have read both before proceeding

## 1. ENVIRONMENT SETUP
Create this folder structure immediately after reading:

  /dev/        ← active build area (one component at a time)
  /stage/      ← promoted, tested components live here
  /prod/       ← only fully regression-tested, all-green components
  /tests/      ← all test files (unit + integration + regression)
  /checkpoints/← JSON snapshots of build state (see Section 4)
  /logs/       ← build logs per component
  /beta/       ← mock images and mock_data.json (from Shiva.prompt)

## 2. COMPONENT BUILD ORDER
Build strictly in this sequence. Do NOT start the next component
until the current one is GREEN and promoted to /stage/.

  C01  → ProjectScaffold        (folder structure, build.gradle, manifest, Hilt setup)
  C02  → RoomDatabase           (entities, DAOs, repositories, migrations)
  C03  → BetaMockProvider       (mock_data.json reader, BetaCategory enum, BetaModeManager)
  C04  → TFLiteInferenceHelper  (model loader, preprocessor, inference runner)
  C05  → ANPRHelper             (MLKit text recognition, plate regex filter)
  C06  → GeoHelper              (FusedLocation PROD + mock inject BETA)
  C07  → PDFGenerator           (iText7 challan PDF + ZXing QR code)
  C08  → WhatsAppSender         (STUB only — SnackBar + DB log)
  C09  → LoginScreen            (UI + ViewModel + BiometricPrompt + mock autofill)
  C10  → DashboardScreen        (stats card + recent challans RecyclerView)
  C11  → NewCheckWizard         (3-step ViewPager2: Vehicle → Location → Detection)
  C12  → ChallaNScreen          (auto-populate + PDF generate + WhatsApp stub)
  C13  → RecordsScreen          (filter list + detail view + CSV export)
  C14  → SettingsScreen         (fine amounts + threshold + beta toggle + dark mode)
  C15  → MLTrainingPipeline     (Python: train.py, evaluate.py, export_to_tflite.py)
  C16  → IntegrationPass        (wire all screens, nav graph, end-to-end smoke test)
  C17  → RegressionFinal        (full regression across all components together)

## 3. PER-COMPONENT WORKFLOW
For EVERY component Cxx, follow these steps exactly:

  STEP 1 — PLAN
    Before writing code, output a brief plan (max 10 lines):
    - Files to create
    - Files to modify
    - Dependencies on previous components
    - Test strategy

  STEP 2 — BUILD in /dev/Cxx/
    - Write all files for this component into /dev/Cxx/
    - Use compact coding patterns (see Section 6)
    - Self-review: check for null safety, missing permissions, 
      hardcoded strings, missing error handling

  STEP 3 — TEST
    Write and run tests in /tests/Cxx/:
    - Unit tests for all logic functions
    - Mock-based tests for DB / network / file I/O
    - For UI components: write Espresso or Robolectric test stubs
    - All tests must PASS before promotion
    - If any test fails: fix in /dev/Cxx/, re-run, do NOT promote until green

  STEP 4 — PROMOTE to /stage/
    - Copy /dev/Cxx/ → /stage/Cxx/
    - Append to /checkpoints/stage_manifest.json:
        { "component": "Cxx", "name": "...", "promoted_at": "timestamp", 
          "tests_passed": N, "tests_failed": 0, "notes": "..." }

  STEP 5 — REGRESSION (for C03 onwards)
    After promoting Cxx, run regression tests for ALL previously 
    staged components:
    - Re-run /tests/C01/ through /tests/C(xx-1)/
    - If any prior test breaks: fix the regression in /dev/ for that 
      component, re-test it, re-promote it to /stage/ with updated manifest
    - Only proceed to C(xx+1) when ALL stage tests are green

  STEP 6 — CHECKPOINT
    Write /checkpoints/checkpoint_Cxx.json (see Section 4)
    Print a one-line summary: 
    ✅ Cxx [ComponentName] — N tests passed — staged — regression clean

## 4. CHECKPOINT FORMAT
Save /checkpoints/checkpoint_Cxx.json after every component:

{
  "checkpoint_id": "Cxx",
  "component_name": "string",
  "timestamp": "ISO8601",
  "status": "STAGED | FAILED | IN_PROGRESS",
  "files_created": ["list"],
  "files_modified": ["list"],
  "tests": {
    "unit": { "passed": N, "failed": 0 },
    "integration": { "passed": N, "failed": 0 },
    "regression": { "passed": N, "failed": 0, "components_checked": ["C01",...] }
  },
  "known_issues": [],
  "next_component": "C(xx+1)",
  "context_note": "one line summary of what this component does and its key interfaces"
}

If Claude Code session is interrupted, resume by running:
  cat checkpoints/stage_manifest.json
to see exactly where to restart from.

## 5. CONTEXT SAVING — COMPACT PATTERNS
To preserve context window across a long build, follow these rules:

  - After promoting each component, summarise its public API in 
    /checkpoints/api_surface.md (append, do not rewrite the whole file)
    Format: ## Cxx ComponentName \n - ClassName.methodName(params): ReturnType
  - When referencing a previously built component, read ONLY its 
    api_surface.md entry — do NOT re-read source files unless debugging
  - Keep inline comments minimal — prefer self-documenting names
  - Delete /dev/Cxx/ after successful promotion to /stage/ to save space
  - Never repeat full file contents in responses — use diffs / additions only
  - If context feels crowded, run: cat checkpoints/api_surface.md 
    to reload just the interface contracts, not implementations

## 6. COMPACT CODING PATTERNS TO USE THROUGHOUT
  - Sealed classes for state (UiState.Loading / Success / Error)
  - Single Activity + Navigation Component (no multiple activities)
  - ViewModels expose StateFlow<UiState>, not LiveData
  - Repositories return Result<T> — no raw exceptions bubble to UI
  - Extension functions for repetitive Bitmap/File/Date ops
  - BetaModeManager.isBeta() checked via Hilt-injected singleton — 
    NOT scattered if/else throughout codebase
  - All strings in strings.xml — zero hardcoding
  - All dimensions in dimens.xml
  - All colours in colors.xml with dark mode variants

## 7. BETA MODE CONSISTENCY RULE
Every screen, every data source, every IO call MUST route through 
BetaModeManager.isBeta() — implemented in C03 and injected everywhere.
Pattern to use:

  val source = if (betaModeManager.isBeta()) mockProvider.get() else realSource.get()

Never bypass this pattern. Regression tests must verify both paths.

## 8. RESUMING AFTER INTERRUPTION
If this session is new or resumed, start with:
  1. cat checkpoints/stage_manifest.json
  2. cat checkpoints/api_surface.md  
  3. ls stage/
  4. Identify the last GREEN component
  5. Continue from the NEXT component in the sequence
  6. Do NOT rebuild already-staged components unless regression forces it

## 9. DONE SIGNAL
When C17 (RegressionFinal) completes with all green:
  - Write /checkpoints/FINAL_BUILD_REPORT.md
  - List every component, test count, any known limitations
  - Write /README.md with setup, beta drill, and build instructions
  - Print: 🎉 DrunkGuard build complete. All components staged. Ready for prod.

---
NOW: Read Shiva.prompt fully, then begin with C01.