package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.HealthRepository
import com.example.data.firebase.FirebaseSyncManager
import com.example.data.firebase.PatientVerificationResult
import android.util.Log
import com.example.ui.translation.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val android.content.Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "esic_theme_settings")

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HealthRepository(db)
    
    // Core Firebase Sync / Auth helper
    val firebaseSyncManager = FirebaseSyncManager(application, db)

    // Current app configurations
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _selectedHospital = MutableStateFlow("ESIC Hospital, Sanath Nagar")
    val selectedHospital: StateFlow<String> = _selectedHospital.asStateFlow()

    // Patient state
    private val _searchEsicQuery = MutableStateFlow("")
    val searchEsicQuery: StateFlow<String> = _searchEsicQuery.asStateFlow()

    private val _currentPatientEsic = MutableStateFlow<String?>(null)
    val currentPatientEsic: StateFlow<String?> = _currentPatientEsic.asStateFlow()

    private val _loggedInPatientPrescription = MutableStateFlow<PrescriptionEntity?>(null)
    val loggedInPatientPrescription: StateFlow<PrescriptionEntity?> = _loggedInPatientPrescription.asStateFlow()

    private val _activePrescription = MutableStateFlow<PrescriptionEntity?>(null)
    val activePrescription: StateFlow<PrescriptionEntity?> = _activePrescription.asStateFlow()

    private val _activeQueue = MutableStateFlow<QueueEntity?>(null)
    val activeQueue: StateFlow<QueueEntity?> = _activeQueue.asStateFlow()

    private val _activeHospitalStatus = MutableStateFlow<HospitalStatusEntity?>(null)
    val activeHospitalStatus: StateFlow<HospitalStatusEntity?> = _activeHospitalStatus.asStateFlow()

    // Live inventory for the selected hospital
    val liveInventory: StateFlow<List<MedicineEntity>> = _selectedHospital
        .flatMapLatest { hospital ->
            repository.getMedicinesByHospital(hospital)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All medicines (for admin lookup and universal list)
    val allMedicines: StateFlow<List<MedicineEntity>> = repository.getAllMedicines()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All crowd reports
    val allCrowdReports: StateFlow<List<CrowdReportEntity>> = repository.getAllCrowdReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications active for current logged in patient
    private val _patientNotifications = MutableStateFlow<List<NotificationPreferenceEntity>>(emptyList())
    val patientNotifications: StateFlow<List<NotificationPreferenceEntity>> = _patientNotifications.asStateFlow()

    // Simulated alerts (in-app notifications)
    private val _simulatedNotifications = MutableStateFlow<List<String>>(emptyList())
    val simulatedNotifications: StateFlow<List<String>> = _simulatedNotifications.asStateFlow()

    // Admin login / panel state
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    // Patient Firestore real-time notifications
    private val _realtimeNotifications = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val realtimeNotifications: StateFlow<List<Map<String, Any>>> = _realtimeNotifications.asStateFlow()

    // Admin profile state flows
    val adminEmployeeId = MutableStateFlow<String?>(null)
    val adminFullName = MutableStateFlow<String?>(null)
    val adminRole = MutableStateFlow<String?>(null)
    val adminHospital = MutableStateFlow<String?>(null)
    val adminPhoneNumber = MutableStateFlow<String?>(null)

    // QR scanner simulations
    private val _scannerVisible = MutableStateFlow(false)
    val scannerVisible: StateFlow<Boolean> = _scannerVisible.asStateFlow()

    // Theme preference via DataStore
    private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    private val _themeMode = MutableStateFlow("system")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { preferences ->
                preferences[THEME_MODE_KEY] = mode
            }
        }
    }

    private val sharedPrefs = application.getSharedPreferences("esic_session_pref", android.content.Context.MODE_PRIVATE)

    init {
        // Collect theme mode from DataStore
        viewModelScope.launch {
            getApplication<Application>().dataStore.data
                .map { preferences ->
                    preferences[THEME_MODE_KEY] ?: "system"
                }
                .collect { mode ->
                    _themeMode.value = mode
                }
        }

        // Initialize Firebase Messaging and setup live Firestore synchronization
        firebaseSyncManager.configureFCM()
        firebaseSyncManager.startSync()

        // Persistent Session restore: read saved credentials on boot
        val savedEsic = sharedPrefs.getString("saved_esic", null)
        if (savedEsic != null) {
            _currentPatientEsic.value = savedEsic
            Log.d("HealthViewModel", "Auto-logged in with stored ESIC Number: $savedEsic")
        }

        val savedAdmin = sharedPrefs.getBoolean("is_admin_logged_in", false)
        if (savedAdmin) {
            _isAdminLoggedIn.value = true
            adminEmployeeId.value = sharedPrefs.getString("admin_employee_id", "ESICADM001")
            adminFullName.value = sharedPrefs.getString("admin_full_name", "Dr. Ramesh Kumar")
            adminRole.value = sharedPrefs.getString("admin_role", "Pharmacist")
            adminHospital.value = sharedPrefs.getString("admin_hospital", "ESIC Hospital, Sanath Nagar")
            adminPhoneNumber.value = sharedPrefs.getString("admin_phone_number", "")
            Log.d("HealthViewModel", "Auto-logged in as Pharmacist/Admin: ${adminFullName.value}")
        }

        viewModelScope.launch(Dispatchers.IO) {
            // First, load prepopulated local DB contents
            repository.prepopulateIfEmpty()
            
            // Seed to Firestore cloud if remote tables are not yet initialized
            firebaseSyncManager.syncLocalToFirestoreIfNeeded()
            
            // Start queue time tick simulations
            startQueueSimulator()
        }

        // Keep patient notifications and queue updated if a patient registers
        viewModelScope.launch {
            _currentPatientEsic.collectLatest { esic ->
                if (esic != null) {
                    try {
                        // Setup real-time notifications observer
                        val fs = firebaseSyncManager.firestore
                        if (fs != null) {
                            fs.collection("notifications")
                                .whereEqualTo("esic_number", esic)
                                .addSnapshotListener { snapshot, err ->
                                    if (err != null) {
                                        Log.w("HealthViewModel", "Notifications listen failed.", err)
                                        return@addSnapshotListener
                                    }
                                    if (snapshot != null) {
                                        val list = snapshot.documents.map { doc ->
                                            mapOf(
                                                "doc_id" to doc.id,
                                                "esic_number" to (doc.getString("esic_number") ?: ""),
                                                "medicine_code" to (doc.getString("medicine_code") ?: ""),
                                                "medicine_name" to (doc.getString("medicine_name") ?: ""),
                                                "hospital" to (doc.getString("hospital") ?: ""),
                                                "message" to (doc.getString("message") ?: ""),
                                                "timestamp" to (doc.getLong("timestamp") ?: System.currentTimeMillis()),
                                                "read_status" to (doc.getBoolean("read_status") ?: false)
                                            )
                                        }.sortedByDescending { (it["timestamp"] as? Number)?.toLong() ?: 0L }
                                        _realtimeNotifications.value = list
                                    }
                                }
                        }

                        // Gather prescription
                        val prescription = repository.getPrescriptionByEsic(esic)
                        _loggedInPatientPrescription.value = prescription
                        _activePrescription.value = prescription

                        if (prescription != null) {
                            // Gather queue matching prescription in its own coroutine to avoid blocking subsequent flow collection
                            launch {
                                repository.getQueueByEsic(esic).collect { queue ->
                                    _activeQueue.value = queue
                                }
                            }
                        }

                        // Gather patient active notifications in its own coroutine so it doesn't get blocked
                        launch {
                            repository.getNotificationPreferences(esic).collect { prefs ->
                                _patientNotifications.value = prefs
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("HealthViewModel", "Error fetching patient records inside esic flow collector: ${e.message}", e)
                    }
                } else {
                    _loggedInPatientPrescription.value = null
                    _activePrescription.value = null
                    _activeQueue.value = null
                    _patientNotifications.value = emptyList()
                }
            }
        }

        // Gather real-time hospital serving details for current patient/screen hospital
        viewModelScope.launch {
            combine(_selectedHospital, _activePrescription) { hosp, presc ->
                presc?.hospitalName ?: hosp
            }.flatMapLatest { activeHosp ->
                repository.getStatusByHospital(activeHosp)
            }.collect { status ->
                _activeHospitalStatus.value = status
            }
        }
    }

    // Language configuration
    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    fun setSelectedHospital(hosp: String) {
        _selectedHospital.value = hosp
    }

    fun setEsicQuery(query: String) {
        _searchEsicQuery.value = query
    }

    suspend fun verifyPatientPhoneAndName(esicNumber: String): Pair<String, String>? {
        return firebaseSyncManager.fetchPatientPhoneAndName(esicNumber)
    }

    /**
     * Authenticates Patient using the patients collection in Firestore (async).
     * Check if document exists and account active status, then pre-fetches profiles.
     */
    suspend fun trackPrescriptionAccess(esic: String, prescription: PrescriptionEntity) {
        val fs = firebaseSyncManager.firestore ?: return
        try {
            val medCodesList = prescription.medCodes.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            
            val data = mapOf(
                "esic_number" to esic,
                "prescription_id" to "RX-$esic",
                "medicine_codes" to medCodesList,
                "accessed_timestamp" to System.currentTimeMillis()
            )
            val docId = "${esic}_${System.currentTimeMillis()}"
            fs.collection("recent_prescription_medicine_access").document(docId).set(data)
            Log.d("HealthViewModel", "Successfully tracked prescription access in Firestore for ESIC: $esic")
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed to track prescription access: ${e.message}")
        }
    }

    suspend fun triggerNotificationsForMedicine(medCode: String, medName: String, hospital: String) {
        val fs = firebaseSyncManager.firestore ?: return
        try {
            val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
            val querySnapshot = fs.collection("recent_prescription_medicine_access")
                .whereArrayContains("medicine_codes", medCode)
                .get()
                .await()
                
            val eligibleDocs = querySnapshot.documents.filter { doc ->
                val timestamp = doc.getLong("accessed_timestamp") ?: 0L
                timestamp >= thirtyDaysAgo
            }
            
            Log.d("HealthViewModel", "Triggering stock alert notifications for $medCode to ${eligibleDocs.size} users.")
            for (doc in eligibleDocs) {
                val esic = doc.getString("esic_number") ?: continue
                val message = "$medName is now available at $hospital."
                val nData = mapOf(
                    "esic_number" to esic,
                    "medicine_code" to medCode,
                    "medicine_name" to medName,
                    "hospital" to hospital,
                    "message" to message,
                    "timestamp" to System.currentTimeMillis(),
                    "read_status" to false
                )
                
                val dateStr = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
                val notifId = "${esic}_${medCode}_$dateStr"
                fs.collection("notifications").document(notifId).set(nData)
                addSimulatedNotification("🔔 NOTIFICATION Sent: To ESIC Patient #$esic: $message")
            }
        } catch (e: Exception) {
            Log.e("HealthViewModel", "Failed during smart medicine notification trigger: ${e.message}")
        }
    }

    suspend fun verifyAndLoginPatient(esic: String): PatientVerificationResult {
        val result = firebaseSyncManager.verifyPatientEsicFromPatientsCollection(esic)
        if (result is PatientVerificationResult.Success) {
            Log.d("LOGIN_DEBUG", "Firestore Success")
            _currentPatientEsic.value = esic
            sharedPrefs.edit().putString("saved_esic", esic).apply()
            Log.d("LOGIN_DEBUG", "Session Saved")
            // Fetch/cache patient profile prescription details
            try {
                val prescription = firebaseSyncManager.verifyPatientEsic(esic)
                if (prescription != null) {
                    trackPrescriptionAccess(esic, prescription)
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to fetch patient prescription profile on success login: ${e.message}")
            }
        }
        return result
    }

    private var searchQueueJob: kotlinx.coroutines.Job? = null

    // Patient lookup - fully linked with Firestore live authentication lookups
    suspend fun performSearch(esic: String): Boolean {
        val prescription = firebaseSyncManager.verifyPatientEsic(esic)
        return if (prescription != null) {
            _activePrescription.value = prescription
            trackPrescriptionAccess(esic, prescription)

            searchQueueJob?.cancel()
            searchQueueJob = viewModelScope.launch {
                repository.getQueueByEsic(esic).collect { queue ->
                    _activeQueue.value = queue
                }
            }
            true
        } else {
            false
        }
    }

    suspend fun findPrescriptionForVerification(esic: String): PrescriptionEntity? {
        return firebaseSyncManager.verifyPatientEsic(esic)
    }

    fun setActivePrescription(prescription: PrescriptionEntity) {
        _activePrescription.value = prescription
        searchQueueJob?.cancel()
        searchQueueJob = viewModelScope.launch {
            repository.getQueueByEsic(prescription.esicNumber).collect { queue ->
                _activeQueue.value = queue
            }
        }
    }

    fun logoutPatient() {
        _currentPatientEsic.value = null
        sharedPrefs.edit().remove("saved_esic").apply()
    }

    // QR Simulation triggeres
    fun setScannerVisible(visible: Boolean) {
        _scannerVisible.value = visible
    }

    fun simulateQrSearch(scannedEsic: String) {
        setScannerVisible(false)
        viewModelScope.launch {
            performSearch(scannedEsic)
        }
    }

    // Admin commands
    fun setAdminLoggedIn(loggedIn: Boolean) {
        _isAdminLoggedIn.value = loggedIn
        sharedPrefs.edit().putBoolean("is_admin_logged_in", loggedIn).apply()
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
        adminEmployeeId.value = null
        adminFullName.value = null
        adminRole.value = null
        adminHospital.value = null
        adminPhoneNumber.value = null
        sharedPrefs.edit()
            .putBoolean("is_admin_logged_in", false)
            .remove("admin_employee_id")
            .remove("admin_full_name")
            .remove("admin_role")
            .remove("admin_hospital")
            .remove("admin_phone_number")
            .apply()
    }

    fun markNotificationAsRead(docId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val fs = firebaseSyncManager.firestore ?: return@launch
            try {
                fs.collection("notifications").document(docId).update("read_status", true)
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Failed to mark notification as read: ${e.message}")
            }
        }
    }

    /**
     * Authenticates Pharmacists against live Firebase Authentication.
     */
    suspend fun authenticatePharmacist(employeeId: String, password: String): Boolean {
        val result = firebaseSyncManager.verifyPharmacistLogin(employeeId, password)
        if (result != null) {
            _isAdminLoggedIn.value = true
            val empId = result["employee_id"] as? String ?: employeeId
            val fullName = result["full_name"] as? String ?: "Administrator"
            val role = result["role"] as? String ?: "Pharmacist"
            val hospital = result["hospital"] as? String ?: "ESIC Hospital, Sanath Nagar"
            val phone = result["phone_number"] as? String ?: ""
            
            adminEmployeeId.value = empId
            adminFullName.value = fullName
            adminRole.value = role
            adminHospital.value = hospital
            adminPhoneNumber.value = phone
            
            sharedPrefs.edit()
                .putBoolean("is_admin_logged_in", true)
                .putString("admin_employee_id", empId)
                .putString("admin_full_name", fullName)
                .putString("admin_role", role)
                .putString("admin_hospital", hospital)
                .putString("admin_phone_number", phone)
                .apply()
            return true
        }
        return false
    }

    fun updateMedicineStock(medCode: String, newStock: Int, isAvailable: Boolean, expectedDays: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update local Room database and remote Firestore in real-time
            repository.updateStock(medCode, newStock, isAvailable, expectedDays)
            firebaseSyncManager.updateStockInFirestore(medCode, newStock, isAvailable, expectedDays)
            
            // Check if any patient is waiting for this medicine (notify alerts)
            if (newStock > 0 && isAvailable) {
                val meds = repository.getMedicineByCodeSuspend(medCode)
                if (meds != null) {
                    addSimulatedNotification("🔔 Stock Update: ${meds.medName} is now available at ${meds.hospital}!")
                    triggerNotificationsForMedicine(medCode, meds.medName, meds.hospital)
                }
            }
        }
    }

    fun updateServingToken(hospitalName: String, newTokenNum: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            // Update local Room database and remote Firestore in real-time
            repository.updateNowServing(hospitalName, newTokenNum)
            firebaseSyncManager.updateServingTokenInFirestore(hospitalName, newTokenNum)
            
            // If the patient token is approaching, alert them
            val currentPatient = _activePrescription.value
            if (currentPatient != null && currentPatient.hospitalName == hospitalName) {
                val patientToken = currentPatient.tokenNumber
                val diff = patientToken - newTokenNum
                if (diff in 1..4) {
                    addSimulatedNotification("🏃 Queue Alert: Token $newTokenNum is currently being served at $hospitalName. Your token ($patientToken) is approaching! Please visit the counter index.")
                }
            }
        }
    }

    // Notifications triggered in-app for user testing
    fun toggleNotificationPreference(medCode: String, medName: String) {
        val currentEsic = _currentPatientEsic.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val existing = _patientNotifications.value.any { it.medCode == medCode }
            if (existing) {
                repository.disableNotification(medCode, currentEsic)
                firebaseSyncManager.disableNotificationInFirestore(medCode, currentEsic)
            } else {
                val pref = NotificationPreferenceEntity(
                    medCode = medCode,
                    medName = medName,
                    isEnabled = true,
                    esicNumber = currentEsic
                )
                repository.enableNotification(pref)
                firebaseSyncManager.enableNotificationInFirestore(pref)
            }
        }
    }

    // Crowd reporting empty medicine counter
    fun submitCrowdReport(medCode: String, medName: String, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val report = CrowdReportEntity(
                medCode = medCode,
                medName = medName,
                reportText = text
            )
            repository.addCrowdReport(report)
            firebaseSyncManager.addCrowdReportToFirestore(report)
            addSimulatedNotification("⚠️ Report Submitted: Thank you for reporting that $medName is out at the counter. Hospital staff have been notified.")
        }
    }

    fun clearCrowdReports(medCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearCrowdReports(medCode)
            firebaseSyncManager.clearCrowdReportsFromFirestore(medCode)
        }
    }

    fun clearAllReports() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllCrowdReports()
            firebaseSyncManager.clearAllCrowdReportsFromFirestore()
        }
    }

    fun addNewMedicine(med: MedicineEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMedicine(med)
            firebaseSyncManager.saveMedicineToFirestore(med)
        }
    }

    fun addNewPrescription(presc: PrescriptionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            db.prescriptionDao().insertPrescription(presc)
            firebaseSyncManager.savePrescriptionToFirestore(presc)
        }
    }

    // In-app Notification list additions
    fun addSimulatedNotification(alert: String) {
        viewModelScope.launch {
            val current = _simulatedNotifications.value.toMutableList()
            current.add(0, alert) // Insert at top
            _simulatedNotifications.value = current
        }
    }

    fun clearSimulatedNotifications() {
        _simulatedNotifications.value = emptyList()
    }

    // Real-time Queue and Stock Simulation engine
    private suspend fun startQueueSimulator() {
        while (true) {
            delay(15000) // Trigger state updates every 15 seconds
            
            // 1. Advance Serving token for each hospital
            val hospitals = listOf(
                "ESIC Hospital, Sanath Nagar",
                "ESIC Hospital, Peenya",
                "ESIC Hospital, Basaidarapur"
            )

            for (hosp in hospitals) {
                val status = repository.getStatusByHospital(hosp).firstOrNull()
                if (status != null) {
                    var nextToken = status.nowServing + 1
                    if (nextToken > 120) nextToken = 1 // reset cycle
                    
                    repository.updateNowServing(hosp, nextToken)
                    firebaseSyncManager.updateServingTokenInFirestore(hosp, nextToken)

                    // 2. Queue Alert check for active logged in user
                    val currentPatient = _activePrescription.value
                    if (currentPatient != null && currentPatient.hospitalName == hosp) {
                        val pt = currentPatient.tokenNumber
                        val diff = pt - nextToken
                        if (diff == 3) {
                            addSimulatedNotification("🔔 Queue approaching: Only 3 patients ahead of you at $hosp! Fast-track your arrival at Counter 3.")
                        } else if (diff == 0) {
                            addSimulatedNotification("📢 NOW SERVING: Token $pt is now being served at Counter 3 ($hosp). Please present your ESIC card.")
                        }
                    }
                }
            }

            // 3. Simple stock fluctuation: randomly reduce a stock by 1, or restock by 1 to trigger real-time stock watchers!
            val allMeds = repository.getAllMedicines().firstOrNull() ?: emptyList()
            if (allMeds.isNotEmpty()) {
                val randomMed = allMeds.random()
                if (randomMed.currentStock > 1 && randomMed.isAvailable) {
                    val newStock = randomMed.currentStock - 1
                    repository.updateStock(
                        randomMed.medCode,
                        newStock,
                        true,
                        randomMed.expectedRestockDays
                    )
                    firebaseSyncManager.updateStockInFirestore(
                        randomMed.medCode,
                        newStock,
                        true,
                        randomMed.expectedRestockDays
                    )
                } else if (randomMed.currentStock == 0) {
                    // Randomly simulate a shipment arriving
                    if (Math.random() > 0.7) {
                        repository.updateStock(
                            randomMed.medCode,
                            50,
                            true,
                            0
                        )
                        firebaseSyncManager.updateStockInFirestore(
                            randomMed.medCode,
                            50,
                            true,
                            0
                        )
                        // Trigger notifying preference check
                        addSimulatedNotification("🔔 RESTOCK ALERT: Fresh stock of ${randomMed.medName} has just arrived at ${randomMed.hospital}! Pharmacy counters are updated.")
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        firebaseSyncManager.stopSync()
    }
}
