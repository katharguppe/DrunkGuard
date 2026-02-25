# DrunkGuard Build Tasks

**Current:** C05 - ANPRHelper
**Remaining:** C05-C17

---

## C03 - BetaMockProvider
**Status:** ✅ COMPLETE

### Files Created:
- [x] `beta/BetaCategory.kt` - Enum with SOBER, SLIGHTLY, MODERATELY, HEAVILY, VEHICLES
- [x] `beta/BetaModeManager.kt` - Singleton injected via Hilt, reads isBetaMode from SettingsRepository
- [x] `utils/BetaMockProvider.kt` - Reads mock_data.json from assets, provides:
  - getMockOfficer(): Officer
  - getMockVehicle(): MockVehicle data class
  - getMockLocation(): MockLocation data class
  - listBetaImages(category: BetaCategory): List<Bitmap>

### Tests:
- [x] BetaMockProvider can parse mock_data.json
- [x] BetaModeManager reflects settings state
- [x] BetaCategory enum covers all cases

### Acceptance Criteria:
- [x] BetaModeManager.isBeta() returns value from SettingsRepository
- [x] BetaMockProvider can load mock officer, vehicle, location
- [x] BetaMockProvider can list images from beta/ folders

---

## C04 - TFLiteInferenceHelper
**Status:** ✅ COMPLETE

### Files Created:
- [x] `ml/TFLiteInferenceHelper.kt` - Load model, preprocess, run inference
- [x] `ml/InferenceResult.kt` - Data class for results
- [x] `di/MLModule.kt` - Hilt module for ML components

### Functions:
- [x] loadModel(context)
- [x] preprocess(bitmap): ByteBuffer (224x224, ImageNet normalize)
- [x] runInference(input): InferenceResult
- [x] close() - release resources

### Acceptance Criteria:
- [x] Model loads from assets/model/drunkguard.tflite
- [x] Preprocessing converts Bitmap to ByteBuffer (224x224, normalized)
- [x] Inference returns 4-class probabilities
- [x] Close() releases interpreter resources

---

## C05 - ANPRHelper
**Status:** NEXT
**Priority:** HIGH
**Blocked by:** None (C04 complete)

### Files to Create:
- [ ] `utils/ANPRHelper.kt` - MLKit text recognition for license plates

### Functions:
- [ ] recognizePlate(bitmap): String? - Extract license plate text
- [ ] Filter with regex: `[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}`

### Acceptance Criteria:
- [ ] ANPRHelper integrates MLKit text recognition
- [ ] Returns formatted license plate string or null
- [ ] Uses regex validation for Indian license plates

---

## C05 - ANPRHelper
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C04

### Files to Create:
- [ ] `utils/ANPRHelper.kt`

### Functions:
- [ ] recognizePlate(bitmap): String?
- [ ] Filter with regex: [A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}

---

## C06 - GeoHelper
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C05

### Files to Create:
- [ ] `utils/GeoHelper.kt`

### Functions:
- [ ] getCurrentLocation(): LatLng (uses FusedLocationProvider in PROD)
- [ ] getMockLocation(): LatLng (from BetaMockProvider in BETA)
- [ ] reverseGeocode(lat, lng): String

---

## C07 - PDFGenerator
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C06

### Files to Create:
- [ ] `utils/PDFGenerator.kt`
- [ ] `data/model/ChallanData.kt`

### Functions:
- [ ] generateChallanPDF(context, challanData): File
- [ ] Includes: header, all fields, subject photo, QR code (ZXing)

---

## C08 - WhatsAppSender (STUB)
**Status:** PENDING
**Priority:** MEDIUM
**Blocked by:** C07

### Files to Create:
- [ ] `utils/WhatsAppSender.kt`

### Functions:
- [ ] sendChallan(context, pdfFile, phone): Result<Unit> - shows Snackbar stub

---

## C09 - LoginScreen
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C08

### Files to Create/Modify:
- [ ] `ui/login/LoginViewModel.kt`
- [ ] `ui/login/LoginFragment.kt` (replace placeholder)
- [ ] `layout/fragment_login.xml` (full layout)

### Features:
- [ ] Badge ID, Password fields
- [ ] "Use Mock Officer" button (beta only)
- [ ] Biometric unlock
- [ ] Validation and error states

---

## C10 - DashboardScreen
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C09

### Files:
- [ ] `ui/dashboard/DashboardViewModel.kt`
- [ ] `ui/dashboard/DashboardFragment.kt` (replace)
- [ ] `layout/fragment_dashboard.xml`

### Features:
- [ ] Officer info card
- [ ] Today's stats (checks, violations)
- [ ] Recent challans RecyclerView
- [ ] New Check, Records, Settings buttons

---

## C11 - NewCheckWizard
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C10

### Files:
- [ ] `ui/newcheck/NewCheckViewModel.kt`
- [ ] `ui/newcheck/NewCheckFragment.kt` (replace)
- [ ] `ui/newcheck/steps/VehicleStepFragment.kt`
- [ ] `ui/newcheck/steps/LocationStepFragment.kt`
- [ ] `ui/newcheck/steps/DetectionStepFragment.kt`
- [ ] ViewPager2 adapter

### Features:
- [ ] 3-step wizard (Vehicle → Location → Detection)
- [ ] ANPR integration for plate scanning
- [ ] GPS/Map integration
- [ ] Photo capture (CameraX in PROD, Beta picker in BETA)
- [ ] TFLite inference with confidence bars

---

## C12 - ChallanScreen
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C11

### Files:
- [ ] `ui/challan/ChallanViewModel.kt`
- [ ] `ui/challan/ChallanFragment.kt` (replace)
- [ ] `layout/fragment_challan.xml`

### Features:
- [ ] Auto-populated challan form
- [ ] PDF generation
- [ ] WhatsApp stub button
- [ ] Save to DB

---

## C13 - RecordsScreen
**Status:** PENDING
**Priority:** MEDIUM
**Blocked by:** C12

### Files:
- [ ] `ui/records/RecordsViewModel.kt`
- [ ] `ui/records/RecordsFragment.kt` (replace)
- [ ] `ui/records/RecordsAdapter.kt`
- [ ] `layout/fragment_records.xml`
- [ ] CSV export utility

### Features:
- [ ] RecyclerView with all checks
- [ ] Filter by date, officer, plate, verdict
- [ ] Detail view
- [ ] Export CSV

---

## C14 - SettingsScreen
**Status:** PENDING
**Priority:** MEDIUM
**Blocked by:** C13

### Files:
- [ ] `ui/settings/SettingsViewModel.kt`
- [ ] `ui/settings/SettingsFragment.kt` (replace)
- [ ] `layout/fragment_settings.xml`

### Features:
- [ ] Fine amounts (slightly/moderately/heavily)
- [ ] Confidence threshold slider
- [ ] Station name
- [ ] Beta mode toggle (with warning dialog)
- [ ] Dark mode toggle
- [ ] App version

---

## C15 - MLTrainingPipeline (Python)
**Status:** PENDING
**Priority:** LOW (can run in parallel)
**Blocked by:** None

### Files:
- [ ] `drunkguard-ml/train.py`
- [ ] `drunkguard-ml/evaluate.py`
- [ ] `drunkguard-ml/export_to_tflite.py`
- [ ] `drunkguard-ml/requirements.txt`

### Features:
- [ ] MobileNetV3-Small with custom head
- [ ] 4-class classification
- [ ] Data augmentation
- [ ] Export to TFLite with INT8 quantization

---

## C16 - IntegrationPass
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C14, C15

### Tasks:
- [ ] Wire all screens together
- [ ] Verify navigation graph
- [ ] End-to-end smoke test
- [ ] Beta mode verification
- [ ] Production mode verification

---

## C17 - RegressionFinal
**Status:** PENDING
**Priority:** HIGH
**Blocked by:** C16

### Tasks:
- [ ] Run full regression test suite
- [ ] Verify all C01-C15 tests pass
- [ ] Create FINAL_BUILD_REPORT.md
- [ ] Create README.md

---

## Progress Summary

```
C01  [██████████] DONE
C02  [██████████] DONE
C03  [██████████] DONE
C04  [██████████] DONE
C05  [          ] NEXT
C06  [          ] PENDING
C07  [          ] PENDING
C08  [          ] PENDING
C09  [          ] PENDING
C10  [          ] PENDING
C11  [          ] PENDING
C12  [          ] PENDING
C13  [          ] PENDING
C14  [          ] PENDING
C15  [          ] PENDING
C16  [          ] PENDING
C17  [          ] PENDING
```
