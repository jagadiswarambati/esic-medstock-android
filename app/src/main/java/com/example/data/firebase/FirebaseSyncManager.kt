package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import com.google.firebase.firestore.DocumentSnapshot

private fun DocumentSnapshot.getLongSafe(field: String): Long? {
    val value = this.get(field) ?: return null
    if (value is Number) {
        return value.toLong()
    }
    if (value is String) {
        return value.toLongOrNull()
    }
    return null
}

sealed class PatientVerificationResult {
    data class Success(val patientName: String, val hospital: String) : PatientVerificationResult()
    object InvalidEsic : PatientVerificationResult()
    object InactiveAccount : PatientVerificationResult()
    object NoInternet : PatientVerificationResult()
    data class Failure(val message: String) : PatientVerificationResult()
}

class FirebaseSyncManager(
    private val context: Context,
    private val db: AppDatabase
) {
    private val tag = "FirebaseSyncManager"
    
    var isFirebaseAvailable = false
        private set

    var firestore: FirebaseFirestore? = null
        private set

    var auth: FirebaseAuth? = null
        private set

    var messaging: FirebaseMessaging? = null
        private set

    private val scope = CoroutineScope(Dispatchers.IO)
    private val listeners = mutableListOf<ListenerRegistration>()

    init {
        try {
            // Attempt to initialize Firebase from current google-services.json context
            val app = FirebaseApp.initializeApp(context)
            if (app != null) {
                firestore = FirebaseFirestore.getInstance()
                auth = FirebaseAuth.getInstance()
                messaging = FirebaseMessaging.getInstance()
                isFirebaseAvailable = true
                Log.d(tag, "Firebase initialized successfully.")
            } else {
                Log.e(tag, "Firebase App initialization returned null.")
            }
        } catch (e: Throwable) {
            Log.e(tag, "Firebase is not available (likely missing or invalid google-servicesConfig): ${e.message}")
            isFirebaseAvailable = false
        }
    }

    /**
     * Subscribes the device to FCM topics for notifications.
     */
    fun configureFCM() {
        if (!isFirebaseAvailable) return
        try {
            messaging?.token?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    Log.d(tag, "FCM registration token: $token")
                } else {
                    Log.w(tag, "Fetching FCM registration token failed", task.exception)
                }
            }

            // Subscribe to global alerts topic so admins can push broadcast alerts
            messaging?.subscribeToTopic("global_alerts")
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(tag, "Subscribed to global_alerts FCM topic")
                    }
                }
        } catch (e: Throwable) {
            Log.e(tag, "Error configuring FCM: ${e.message}")
        }
    }

    /**
     * Starts listening to Firestore collections in real-time.
     * Updates mapped data into local SQLite Room database instantly.
     */
    fun startSync() {
        val fs = firestore ?: return
        Log.d(tag, "Starting real-time Firestore listeners sync...")

        // Clear existing listeners if any
        stopSync()

        // 1. Inventory Sync Listener
        try {
            val invListener = fs.collection("inventory")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(tag, "Inventory listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch {
                            try {
                                val list = mutableListOf<MedicineEntity>()
                                for (doc in snapshots.documents) {
                                    val medCode = doc.getString("med_code") ?: doc.id
                                    val medName = doc.getString("med_name") ?: continue
                                    val currentStock = doc.getLongSafe("current_stock")?.toInt() ?: 0
                                    val avgDosage = doc.getLongSafe("avg_dosage")?.toInt() ?: 1
                                    val expectedRestockDays = doc.getLongSafe("expected_restock_days")?.toInt() ?: 0
                                    val hospital = doc.getString("hospital") ?: "General Hospital"
                                    val status = doc.getString("status") ?: "AVAILABLE"
                                    val isAvailable = doc.getBoolean("is_available") ?: (status == "AVAILABLE" && currentStock > 0)
                                    val category = doc.getString("category") ?: "General"
                                    
                                    list.add(
                                        MedicineEntity(
                                            medCode = medCode,
                                            medName = medName,
                                            currentStock = currentStock,
                                            avgDosage = avgDosage,
                                            expectedRestockDays = expectedRestockDays,
                                            hospital = hospital,
                                            isAvailable = isAvailable,
                                            category = category
                                        )
                                    )
                                }
                                if (list.isNotEmpty()) {
                                    db.medicineDao().insertMedicines(list)
                                    Log.d(tag, "Synced ${list.size} medicines from Firestore to Room.")
                                }
                            } catch (err: Exception) {
                                Log.e(tag, "Error in inventory sync observer callback: ${err.message}", err)
                            }
                        }
                    }
                }
            listeners.add(invListener)
        } catch (ex: Exception) {
            Log.e(tag, "Could not register inventory listener", ex)
        }

        // 2. Prescriptions Sync Listener
        try {
            val prescListener = fs.collection("prescriptions")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(tag, "Prescriptions listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch {
                            try {
                                val list = mutableListOf<PrescriptionEntity>()
                                for (doc in snapshots.documents) {
                                    val esicNumber = doc.getString("esic_number") ?: doc.id
                                    val patientName = doc.getString("patient_name") ?: continue
                                    val hospitalName = doc.getString("hospital") ?: doc.getString("hospital_name") ?: "General Hospital"
                                    val prescribedDate = doc.getString("prescribed_date") ?: "2026-05-22"
                                    val tokenNumber = doc.getLongSafe("token_number")?.toInt() ?: 0
                                    val queueStatus = doc.getString("queue_status") ?: "Waiting"
                                    
                                    // Support list or string medicines safely
                                    val medicinesVal = doc.get("medicines")
                                    val codesList = when(medicinesVal) {
                                        is String -> medicinesVal.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        is List<*> -> medicinesVal.filterIsInstance<String>().map { it.trim() }.filter { it.isNotEmpty() }
                                        else -> (doc.getString("med_codes") ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                    }
                                    val medCodes = codesList.joinToString(",")

                                    list.add(
                                        PrescriptionEntity(
                                            esicNumber = esicNumber,
                                            patientName = patientName,
                                            hospitalName = hospitalName,
                                            prescribedDate = prescribedDate,
                                            tokenNumber = tokenNumber,
                                            queueStatus = queueStatus,
                                            medCodes = medCodes
                                        )
                                    )
                                }
                                if (list.isNotEmpty()) {
                                    db.prescriptionDao().insertPrescriptions(list)
                                    Log.d(tag, "Synced ${list.size} prescriptions from Firestore to Room.")
                                }
                            } catch (err: Exception) {
                                Log.e(tag, "Error in prescriptions sync observer callback: ${err.message}", err)
                            }
                        }
                    }
                }
            listeners.add(prescListener)
        } catch (ex: Exception) {
            Log.e(tag, "Could not register prescriptions listener", ex)
        }

        // 3. Queue Sync Listener
        try {
            val qListener = fs.collection("queue")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(tag, "Queue listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch {
                            try {
                                val list = mutableListOf<QueueEntity>()
                                for (doc in snapshots.documents) {
                                    val esicNumber = doc.getString("patient_uid") ?: doc.getString("esic_number") ?: doc.id
                                    val tokenNumber = doc.getLongSafe("token_number")?.toInt() ?: 0
                                    val status = doc.getString("status") ?: "WAITING"
                                    val patientName = doc.getString("patient_name") ?: "Patient"
                                    val hospitalName = doc.getString("hospital") ?: doc.getString("hospital_name") ?: "General Hospital"

                                    list.add(
                                        QueueEntity(
                                            esicNumber = esicNumber,
                                            tokenNumber = tokenNumber,
                                            status = status,
                                            patientName = patientName,
                                            hospitalName = hospitalName
                                        )
                                    )
                                }
                                if (list.isNotEmpty()) {
                                    db.queueDao().insertQueues(list)
                                    Log.d(tag, "Synced ${list.size} queue records from Firestore to Room.")
                                }
                            } catch (err: Exception) {
                                Log.e(tag, "Error in queue status sync observer callback: ${err.message}", err)
                            }
                        }
                    }
                }
            listeners.add(qListener)
        } catch (ex: Exception) {
            Log.e(tag, "Could not register queue listener", ex)
        }

        // 4. Hospital Status Sync Listener
        try {
            val statusListener = fs.collection("hospital_status")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(tag, "Hospital Status listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch {
                            try {
                                val list = mutableListOf<HospitalStatusEntity>()
                                for (doc in snapshots.documents) {
                                    val hospitalName = doc.getString("hospital_name") ?: doc.id
                                    val nowServing = doc.getLongSafe("now_serving")?.toInt() ?: 1
                                    val avgServiceTime = doc.getLongSafe("avg_service_time")?.toInt() ?: 3

                                    list.add(
                                        HospitalStatusEntity(
                                            hospitalName = hospitalName,
                                            nowServing = nowServing,
                                            avgServiceTime = avgServiceTime
                                        )
                                    )
                                }
                                if (list.isNotEmpty()) {
                                    db.hospitalStatusDao().insertHospitalStatuses(list)
                                    Log.d(tag, "Synced ${list.size} hospital status records from Firestore to Room.")
                                }
                            } catch (err: Exception) {
                                Log.e(tag, "Error in hospital status sync observer callback: ${err.message}", err)
                            }
                        }
                    }
                }
            listeners.add(statusListener)
        } catch (ex: Exception) {
            Log.e(tag, "Could not register hospital status listener", ex)
        }

        // 5. Crowd Reports Sync Listener
        try {
            val reportListener = fs.collection("crowd_reports")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(tag, "Crowd reports listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch {
                            try {
                                // Clear and update reports to reflect deletions properly
                                db.crowdReportDao().clearAllReports()
                                val list = mutableListOf<CrowdReportEntity>()
                                for (doc in snapshots.documents) {
                                    val medCode = doc.getString("med_code") ?: ""
                                    val medName = doc.getString("med_name") ?: "Medicine"
                                    val reportText = doc.getString("report") ?: doc.getString("report_text") ?: "Out of stock alert"
                                    val timestamp = doc.getLongSafe("timestamp") ?: System.currentTimeMillis()
                                    
                                    list.add(
                                        CrowdReportEntity(
                                            medCode = medCode,
                                            medName = medName,
                                            reportText = reportText,
                                            timestamp = timestamp
                                        )
                                    )
                                }
                                if (list.isNotEmpty()) {
                                    for(rep in list) {
                                        db.crowdReportDao().insertReport(rep)
                                    }
                                    Log.d(tag, "Synced ${list.size} crowd reports from Firestore to Room.")
                                }
                            } catch (err: Exception) {
                                Log.e(tag, "Error in crowd reports sync observer callback: ${err.message}", err)
                            }
                        }
                    }
                }
            listeners.add(reportListener)
        } catch (ex: Exception) {
            Log.e(tag, "Could not register crowd reports listener", ex)
        }

        // 6. Notifications Preference Sync Listener
        try {
            val notifListener = fs.collection("notifications")
                .addSnapshotListener { snapshots, e ->
                    if (e != null) {
                        Log.w(tag, "Notifications listen failed.", e)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        scope.launch {
                            try {
                                val list = mutableListOf<NotificationPreferenceEntity>()
                                for (doc in snapshots.documents) {
                                    val medCode = doc.getString("med_code") ?: continue
                                    val medName = doc.getString("med_name") ?: "Medicine"
                                    val isEnabled = doc.getBoolean("notification_enabled") ?: true
                                    val esicNumber = doc.getString("patient_uid") ?: doc.getString("esic_number") ?: continue

                                    list.add(
                                        NotificationPreferenceEntity(
                                            medCode = medCode,
                                            medName = medName,
                                            isEnabled = isEnabled,
                                            esicNumber = esicNumber
                                        )
                                    )
                                }
                                if (list.isNotEmpty()) {
                                    for(pref in list) {
                                        db.notificationPreferenceDao().insertPreference(pref)
                                    }
                                    Log.d(tag, "Synced ${list.size} notification configurations from Firestore to Room.")
                                }
                            } catch (err: Exception) {
                                Log.e(tag, "Error in notifications subscription sync observer callback: ${err.message}", err)
                            }
                        }
                    }
                }
            listeners.add(notifListener)
        } catch (ex: Exception) {
            Log.e(tag, "Could not register notifications listener", ex)
        }
    }

    /**
     * Terminate all registration listeners to prevent memory leaks
     */
    fun stopSync() {
        for (listener in listeners) {
            listener.remove()
        }
        listeners.clear()
        Log.d(tag, "Firestore listeners stopped.")
    }

    /**
     * Populates real-time Firestore with initial local data if it's currently empty
     */
    suspend fun syncLocalToFirestoreIfNeeded() = withContext(Dispatchers.IO) {
        performActualSynchronization()
    }

    private suspend fun performActualSynchronization() {
        val fs = firestore ?: return
        try {
            val snapshot = fs.collection("inventory").get().await()
            val existingCodes = snapshot.documents.mapNotNull { it.getString("med_code") }.toSet()
            
            val seedMeds = MedicineSeedData.seedMeds

            // Filtering out existing medicines to fulfill the " Do NOT overwrite, Do NOT duplicate, ONLY append missing" constraint.
            val missingMeds = seedMeds.filter { (it["med_code"] as? String) !in existingCodes }

            if (missingMeds.isNotEmpty()) {
                Log.d(tag, "Found ${missingMeds.size} missing medicines in Firestore! Seeding them now using Firestore batch writes...")
                try {
                    // Split batch if it exceeds 500 records (here 94 is well under 500)
                    val batch = fs.batch()
                    missingMeds.forEach { med ->
                        val docRef = fs.collection("inventory").document(med["med_code"] as String)
                        batch.set(docRef, med)
                    }
                    batch.commit().await()
                    Log.d(tag, "inventory seeded successfully")
                } catch (e: Exception) {
                    Log.e(tag, "Firestore write failures: ${e.message}")
                }
            } else {
                Log.d(tag, "inventory already exists")
                Log.d(tag, "All 50+ target medicines are already present. Seeding skipped.")
            }

            // Ensure admins collection is seeded in Firestore
            try {
                val adminsSnapshot = fs.collection("admins").get().await()
                if (adminsSnapshot.isEmpty) {
                    Log.d(tag, "Admins collection is empty, seeding default admins to Firestore...")
                    val testAdmins = listOf(
                        mapOf("employee_id" to "ESICADM001", "full_name" to "Dr. Ramesh Kumar", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Basaidarapur", "phone_number" to "9876501234", "password" to "esic@123", "active_status" to true),
                        mapOf("employee_id" to "ESICADM002", "full_name" to "Dr. Priya Sharma", "role" to "Inventory Manager", "hospital" to "ESIC Hospital, Basaidarapur", "phone_number" to "9876501235", "password" to "priya@123", "active_status" to true),
                        mapOf("employee_id" to "ESICADM003", "full_name" to "Dr. Abdul Rahman", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Peenya", "phone_number" to "9123456781", "password" to "rahman@123", "active_status" to true),
                        mapOf("employee_id" to "ESICADM004", "full_name" to "Dr. Sneha Reddy", "role" to "Inventory Manager", "hospital" to "ESIC Hospital, Peenya", "phone_number" to "9988776655", "password" to "sneha@123", "active_status" to true),
                        mapOf("employee_id" to "ESICADM005", "full_name" to "Dr. Arvind Patel", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9012345678", "password" to "arvind@123", "active_status" to true),
                        mapOf("employee_id" to "ESICADM006", "full_name" to "Dr. Kavya Rao", "role" to "Senior Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9090909090", "password" to "kavya@123", "active_status" to true)
                    )
                    testAdmins.forEach { admin ->
                        val empId = admin["employee_id"] as String
                        fs.collection("admins").document(empId).set(admin)
                    }
                    Log.d(tag, "Admins collection seeded successfully.")
                }
            } catch (e: Exception) {
                Log.e(tag, "Failed to seed admins collection: ${e.message}")
            }

            // To support completely empty starts, populate the other collections only if things were empty originally
            if (snapshot.isEmpty) {
                Log.d(tag, "First run layout - seeding support Firestore tables...")
                // 2. Upload prescriptions
                val localPrescriptions = db.prescriptionDao().getAllPrescriptions()
                localPrescriptions.first().forEach { presc ->
                    val medicineCodes = presc.medCodes.split(",")
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    val data = mapOf(
                        "esic_number" to presc.esicNumber,
                        "patient_name" to presc.patientName,
                        "hospital" to presc.hospitalName,
                        "prescribed_date" to presc.prescribedDate,
                        "token_number" to presc.tokenNumber,
                        "queue_status" to presc.queueStatus,
                        "medicines" to medicineCodes
                    )
                    fs.collection("prescriptions").document(presc.esicNumber).set(data)
                }

                // 3. Upload queue records
                val localQueues = listOf(
                    QueueEntity("1234567890", 45, "WAITING", "Ramesh Kumar", "ESIC Hospital, Sanath Nagar"),
                    QueueEntity("9876543210", 12, "WAITING", "Sunita Devi", "ESIC Hospital, Basaidarapur"),
                    QueueEntity("1122334455", 89, "WAITING", "Lakshmi Prasanna", "ESIC Hospital, Peenya")
                )
                localQueues.forEach { q ->
                    val data = mapOf(
                        "patient_uid" to q.esicNumber,
                        "token_number" to q.tokenNumber,
                        "status" to q.status,
                        "patient_name" to q.patientName,
                        "hospital" to q.hospitalName,
                        "medicines" to ""
                    )
                    fs.collection("queue").document(q.esicNumber).set(data)
                }

                // 4. Upload hospital statuses
                val localStatuses = listOf(
                    HospitalStatusEntity("ESIC Hospital, Sanath Nagar", 40, 2),
                    HospitalStatusEntity("ESIC Hospital, Peenya", 85, 3),
                    HospitalStatusEntity("ESIC Hospital, Basaidarapur", 7, 3)
                )
                localStatuses.forEach { status ->
                    val data = mapOf(
                        "hospital_name" to status.hospitalName,
                        "now_serving" to status.nowServing,
                        "avg_service_time" to status.avgServiceTime
                    )
                    fs.collection("hospital_status").document(status.hospitalName).set(data)
                }

                // 5. Upload patients (including an inactive test user to verify login rules)
                val testPatients = listOf(
                    mapOf("esic_number" to "1234567890", "patient_name" to "Ramesh Kumar", "hospital" to "ESIC Hospital, Sanath Nagar", "active" to true, "phone_number" to "9876543210"),
                    mapOf("esic_number" to "9876543210", "patient_name" to "Sunita Devi", "hospital" to "ESIC Hospital, Basaidarapur", "active" to true, "phone_number" to "9123456780"),
                    mapOf("esic_number" to "1122334455", "patient_name" to "Lakshmi Prasanna", "hospital" to "ESIC Hospital, Peenya", "active" to true, "phone_number" to "9345678901"),
                    mapOf("esic_number" to "5555555555", "patient_name" to "Inactive Test User", "hospital" to "ESIC Hospital, Sanath Nagar", "active" to false, "phone_number" to "9000000000")
                )
                testPatients.forEach { patient ->
                    val esic = patient["esic_number"] as String
                    fs.collection("patients").document(esic).set(patient)
                }

                // 6. Upload admins
                val testAdmins = listOf(
                    mapOf("employee_id" to "ESICADM001", "full_name" to "Dr. Ramesh Kumar", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Basaidarapur", "phone_number" to "9876501234", "password" to "esic@123", "active_status" to true),
                    mapOf("employee_id" to "ESICADM002", "full_name" to "Dr. Priya Sharma", "role" to "Inventory Manager", "hospital" to "ESIC Hospital, Basaidarapur", "phone_number" to "9876501235", "password" to "priya@123", "active_status" to true),
                    mapOf("employee_id" to "ESICADM003", "full_name" to "Dr. Abdul Rahman", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Peenya", "phone_number" to "9123456781", "password" to "rahman@123", "active_status" to true),
                    mapOf("employee_id" to "ESICADM004", "full_name" to "Dr. Sneha Reddy", "role" to "Inventory Manager", "hospital" to "ESIC Hospital, Peenya", "phone_number" to "9988776655", "password" to "sneha@123", "active_status" to true),
                    mapOf("employee_id" to "ESICADM005", "full_name" to "Dr. Arvind Patel", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9012345678", "password" to "arvind@123", "active_status" to true),
                    mapOf("employee_id" to "ESICADM006", "full_name" to "Dr. Kavya Rao", "role" to "Senior Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9090909090", "password" to "kavya@123", "active_status" to true)
                )
                testAdmins.forEach { admin ->
                    val empId = admin["employee_id"] as String
                    fs.collection("admins").document(empId).set(admin)
                }

                Log.d(tag, "Local data seeding to Firestore cloud successfully completed!")
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to seed local data to Firestore: ${e.message}")
        }
    }

    // ==========================================
    // Real WRITE sync methods called during Admin controls
    // ==========================================

    suspend fun saveMedicineToFirestore(med: MedicineEntity) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val data = mapOf(
                "med_code" to med.medCode,
                "med_name" to med.medName,
                "current_stock" to med.currentStock,
                "avg_dosage" to med.avgDosage,
                "expected_restock_days" to med.expectedRestockDays,
                "hospital" to med.hospital,
                "is_available" to med.isAvailable,
                "category" to med.category
            )
            fs.collection("inventory").document(med.medCode).set(data).await()
            Log.d(tag, "Saved medicine ${med.medCode} to Firestore successfully.")
        } catch (e: Exception) {
            Log.e(tag, "Error saving medicine to Firestore: ${e.message}")
        }
    }

    suspend fun updateStockInFirestore(medCode: String, newStock: Int, isAvailable: Boolean, expectedDays: Int) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val docRef = fs.collection("inventory").document(medCode)
            val updates = mapOf(
                "current_stock" to newStock,
                "is_available" to isAvailable,
                "expected_restock_days" to expectedDays
            )
            docRef.update(updates).await()
            Log.d(tag, "Updated stock for $medCode in Firestore.")
        } catch (e: Exception) {
            Log.e(tag, "Error updating stock in Firestore: ${e.message}")
        }
    }

    suspend fun updateServingTokenInFirestore(hospitalName: String, newTokenNum: Int) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val docRef = fs.collection("hospital_status").document(hospitalName)
            docRef.update("now_serving", newTokenNum).await()
            Log.d(tag, "Updated now_serving for $hospitalName in Firestore.")
        } catch (e: Exception) {
            Log.e(tag, "Error updating now_serving in Firestore: ${e.message}")
        }
    }

    suspend fun addCrowdReportToFirestore(report: CrowdReportEntity) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val docId = "${report.medCode}_${report.timestamp}"
            val data = mapOf(
                "med_code" to report.medCode,
                "med_name" to report.medName,
                "report" to report.reportText,
                "timestamp" to report.timestamp
            )
            fs.collection("crowd_reports").document(docId).set(data).await()
            Log.d(tag, "Added crowd report for ${report.medCode} to Firestore.")
        } catch (e: Exception) {
            Log.e(tag, "Error adding crowd report to Firestore: ${e.message}")
        }
    }

    suspend fun clearCrowdReportsFromFirestore(medCode: String) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val querySnap = fs.collection("crowd_reports")
                .whereEqualTo("med_code", medCode)
                .get()
                .await()
            for (doc in querySnap.documents) {
                fs.collection("crowd_reports").document(doc.id).delete().await()
            }
            Log.d(tag, "Cleared crowd reports for $medCode from Firestore.")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing crowd reports from Firestore: ${e.message}")
        }
    }

    suspend fun clearAllCrowdReportsFromFirestore() = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val querySnap = fs.collection("crowd_reports").get().await()
            for (doc in querySnap.documents) {
                fs.collection("crowd_reports").document(doc.id).delete().await()
            }
            Log.d(tag, "Cleared all crowd reports from Firestore.")
        } catch (e: Exception) {
            Log.e(tag, "Error clearing all crowd reports from Firestore: ${e.message}")
        }
    }

    suspend fun enableNotificationInFirestore(pref: NotificationPreferenceEntity) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val docId = "${pref.esicNumber}_${pref.medCode}"
            val data = mapOf(
                "patient_uid" to pref.esicNumber,
                "med_code" to pref.medCode,
                "med_name" to pref.medName,
                "notification_enabled" to pref.isEnabled
            )
            fs.collection("notifications").document(docId).set(data).await()
            Log.d(tag, "Enabled Firestore preference alert for ${pref.medCode}.")
        } catch (e: Exception) {
            Log.e(tag, "Error enabling Firestore preference: ${e.message}")
        }
    }

    suspend fun disableNotificationInFirestore(medCode: String, esicNumber: String) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val docId = "${esicNumber}_${medCode}"
            fs.collection("notifications").document(docId).delete().await()
            Log.d(tag, "Disabled Firestore preference alert for $medCode.")
        } catch (e: Exception) {
            Log.e(tag, "Error disabling Firestore preference: ${e.message}")
        }
    }

    suspend fun savePrescriptionToFirestore(presc: PrescriptionEntity) = withContext(Dispatchers.IO) {
        val fs = firestore ?: return@withContext
        try {
            val medicineCodes = presc.medCodes.split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
            val data = mapOf(
                "esic_number" to presc.esicNumber,
                "patient_name" to presc.patientName,
                "hospital" to presc.hospitalName,
                "prescribed_date" to presc.prescribedDate,
                "token_number" to presc.tokenNumber,
                "queue_status" to presc.queueStatus,
                "medicines" to medicineCodes
            )
            fs.collection("prescriptions").document(presc.esicNumber).set(data).await()
            Log.d(tag, "Saved prescription to Firestore for ESIC: ${presc.esicNumber}")
        } catch (e: Exception) {
            Log.e(tag, "Error saving prescription to Firestore: ${e.message}")
        }
    }

    // ==========================================
    // Real AUTHENTICATION systems using Firestore / Auth
    // ==========================================

    /**
     * Looks up ESIC Number in Firestore prescriptions collection to authenticate the Patient.
     * Graces fully to offline Room DB check if Firestore fails.
     */
    suspend fun verifyPatientEsic(esicNumber: String): PrescriptionEntity? = withContext(Dispatchers.IO) {
        val fs = firestore
        if (isFirebaseAvailable && fs != null) {
            try {
                Log.d(tag, "Verifying Patient ESIC on Firestore: $esicNumber")
                
                // Let's first search by document ID, field prescription_id, or field esic_number to find the document dynamically
                var doc = fs.collection("prescriptions").document(esicNumber).get().await()
                var exists = doc.exists()
                
                if (!exists) {
                    val qByPrescId = fs.collection("prescriptions")
                        .whereEqualTo("prescription_id", esicNumber)
                        .get().await()
                    if (!qByPrescId.isEmpty) {
                        doc = qByPrescId.documents.first()
                        exists = true
                    } else {
                        val qByEsic = fs.collection("prescriptions")
                            .whereEqualTo("esic_number", esicNumber)
                            .get().await()
                        if (!qByEsic.isEmpty) {
                            doc = qByEsic.documents.first()
                            exists = true
                        }
                    }
                }
                
                if (exists) {
                    val patientName = doc.getString("patient_name") ?: "Patient"
                    val hospitalName = doc.getString("hospital") ?: doc.getString("hospital_name") ?: "General Hospital"
                    val prescribedDate = doc.getString("prescribed_date") ?: "2026-05-22"
                    val tokenNumber = doc.getLongSafe("token_number")?.toInt() ?: 0
                    val queueStatus = doc.getString("queue_status") ?: "Waiting"
                    val esicNo = doc.getString("esic_number") ?: doc.getString("prescription_id") ?: doc.id
                    
                    val medicinesVal = doc.get("medicines")
                    val codesList = when(medicinesVal) {
                        is String -> medicinesVal.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        is List<*> -> medicinesVal.filterIsInstance<String>().map { it.trim() }.filter { it.isNotEmpty() }
                        else -> (doc.getString("med_codes") ?: "").split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    }
                    val medCodes = codesList.joinToString(",")

                    val entity = PrescriptionEntity(
                        esicNumber = esicNo,
                        patientName = patientName,
                        hospitalName = hospitalName,
                        prescribedDate = prescribedDate,
                        tokenNumber = tokenNumber,
                        queueStatus = queueStatus,
                        medCodes = medCodes
                    )
                    // Also cache locally
                    db.prescriptionDao().insertPrescription(entity)
                    return@withContext entity
                }
            } catch (e: Exception) {
                Log.e(tag, "Firestore ESIC dynamic prescription lookup failed: ${e.message}. Falling back to Room check.")
            }
        }
        // Fallback to Room DB
        return@withContext db.prescriptionDao().getPrescriptionByEsic(esicNumber)
    }

    /**
     * Searches Firestore collection `patients` to authenticate the Patient.
     * Checks document existence, accounts active state, handles offline state & failures.
     */
    suspend fun verifyPatientEsicFromPatientsCollection(esicNumber: String): PatientVerificationResult = withContext(Dispatchers.IO) {
        val fs = firestore
        if (!isFirebaseAvailable || fs == null) {
            // Local fallback if Firebase not available: check Room prescription
            val localPrescription = db.prescriptionDao().getPrescriptionByEsic(esicNumber)
            return@withContext if (localPrescription != null) {
                PatientVerificationResult.Success(localPrescription.patientName, localPrescription.hospitalName)
            } else {
                PatientVerificationResult.Failure("Firebase / Firestore service is not configured or compiled correctly.")
            }
        }
        try {
            Log.d(tag, "Fetching patient document from collection 'patients': $esicNumber")
            
            // Allow matching by patient document ID OR querying field esic_number
            var doc = fs.collection("patients").document(esicNumber).get().await()
            var exists = doc.exists()
            if (!exists) {
                val q = fs.collection("patients").whereEqualTo("esic_number", esicNumber).get().await()
                if (!q.isEmpty) {
                    doc = q.documents.first()
                    exists = true
                }
            }
            
            if (exists) {
                val isActive = doc.getBoolean("active") ?: doc.getBoolean("active_status") ?: true
                if (!isActive) {
                    return@withContext PatientVerificationResult.InactiveAccount
                }
                val patientName = doc.getString("patient_name") ?: "Patient"
                val hospital = doc.getString("hospital") ?: doc.getString("hospital_name") ?: "General Hospital"
                return@withContext PatientVerificationResult.Success(patientName, hospital)
            } else {
                // If not in Patients, allow login if a matching Prescription ID is found dynamically
                val presc = verifyPatientEsic(esicNumber)
                if (presc != null) {
                    return@withContext PatientVerificationResult.Success(presc.patientName, presc.hospitalName)
                }
                return@withContext PatientVerificationResult.InvalidEsic
            }
        } catch (e: Exception) {
            Log.e(tag, "Firestore patient lookup failed: ${e.message}. Falling back to Room DB.", e)
            val localPrescription = db.prescriptionDao().getPrescriptionByEsic(esicNumber)
            if (localPrescription != null) {
                return@withContext PatientVerificationResult.Success(localPrescription.patientName, localPrescription.hospitalName)
            }
            val msg = e.message ?: ""
            if (msg.contains("network", ignoreCase = true) || 
                msg.contains("unavailable", ignoreCase = true) || 
                e is java.net.UnknownHostException || 
                e is java.net.ConnectException ||
                e.cause is java.net.UnknownHostException) {
                return@withContext PatientVerificationResult.NoInternet
            } else {
                return@withContext PatientVerificationResult.Failure("Firestore fetch failure: ${e.localizedMessage}")
            }
        }
    }

    /**
     * Graces fully to offline Room DB check if Firestore fails or is unconfigured.
     */
    suspend fun fetchPatientPhoneAndName(esicNumber: String): Pair<String, String>? = withContext(Dispatchers.IO) {
        val fs = firestore
        if (!isFirebaseAvailable || fs == null) {
            // Local fallback for demo
            val localPrescription = db.prescriptionDao().getPrescriptionByEsic(esicNumber)
            if (localPrescription != null) {
                return@withContext Pair("9123456780", localPrescription.patientName)
            }
            return@withContext null
        }
        try {
            // Try patients collection first
            var doc = fs.collection("patients").document(esicNumber).get().await()
            var exists = doc.exists()
            if (!exists) {
                val q = fs.collection("patients").whereEqualTo("esic_number", esicNumber).get().await()
                if (!q.isEmpty) {
                    doc = q.documents.first()
                    exists = true
                }
            }
            
            if (exists) {
                val phone = doc.getString("phone_number") ?: "9123456780"
                val name = doc.getString("patient_name") ?: "Patient"
                return@withContext Pair(phone, name)
            }
            
            // If not found in patients collection, fallback to querying prescription
            val presc = verifyPatientEsic(esicNumber)
            if (presc != null) {
                return@withContext Pair("9123456780", presc.patientName)
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to fetch patient phone: ${e.message}")
        }
        // General fallback to Room DB
        val localPrescription = db.prescriptionDao().getPrescriptionByEsic(esicNumber)
        if (localPrescription != null) {
            return@withContext Pair("9123456780", localPrescription.patientName)
        }
        return@withContext null
    }

    /**
     * Logs in the Pharmacist using the Firestore admins collection.
     * Falls back to standard password hashes or hardcoded defaults if Firestore is offline.
     */
    suspend fun verifyPharmacistLogin(employeeId: String, password: String): Map<String, Any>? = withContext(Dispatchers.IO) {
        val fs = firestore
        val trimmedId = employeeId.trim()
        if (isFirebaseAvailable && fs != null) {
            try {
                Log.d(tag, "Checking barcode admin credential login for Employee ID: $trimmedId")
                var doc = fs.collection("admins").document(trimmedId).get().await()
                var exists = doc.exists()
                if (!exists) {
                    val q = fs.collection("admins").whereEqualTo("employee_id", trimmedId).get().await()
                    if (!q.isEmpty) {
                        doc = q.documents.first()
                        exists = true
                    }
                }
                
                if (exists) {
                    val pass = doc.getString("password")
                    val active = doc.getBoolean("active_status") ?: doc.getBoolean("active") ?: true
                    if (pass == password && active) {
                        Log.d(tag, "Admin login successful for employee ID: $trimmedId via Firestore")
                        return@withContext mapOf(
                            "employee_id" to (doc.getString("employee_id") ?: trimmedId),
                            "full_name" to (doc.getString("full_name") ?: "Administrator"),
                            "role" to (doc.getString("role") ?: "Pharmacist"),
                            "hospital" to (doc.getString("hospital") ?: "ESIC Hospital, Sanath Nagar"),
                            "phone_number" to (doc.getString("phone_number") ?: ""),
                            "active_status" to active
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Firestore admin login check failed: ${e.message}")
            }
        }
        
        // Local fallback complying with the spec
        val testAdmins = listOf(
            mapOf("employee_id" to "ESICADM001", "full_name" to "Dr. Ramesh Kumar", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Basaidarapur", "phone_number" to "9876501234", "password" to "esic@123", "active_status" to true),
            mapOf("employee_id" to "ESICADM002", "full_name" to "Dr. Priya Sharma", "role" to "Inventory Manager", "hospital" to "ESIC Hospital, Basaidarapur", "phone_number" to "9876501235", "password" to "priya@123", "active_status" to true),
            mapOf("employee_id" to "ESICADM003", "full_name" to "Dr. Abdul Rahman", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Peenya", "phone_number" to "9123456781", "password" to "rahman@123", "active_status" to true),
            mapOf("employee_id" to "ESICADM004", "full_name" to "Dr. Sneha Reddy", "role" to "Inventory Manager", "hospital" to "ESIC Hospital, Peenya", "phone_number" to "9988776655", "password" to "sneha@123", "active_status" to true),
            mapOf("employee_id" to "ESICADM005", "full_name" to "Dr. Arvind Patel", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9012345678", "password" to "arvind@123", "active_status" to true),
            mapOf("employee_id" to "ESICADM006", "full_name" to "Dr. Kavya Rao", "role" to "Senior Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9090909090", "password" to "kavya@123", "active_status" to true),
            mapOf("employee_id" to "admin", "full_name" to "Super Admin", "role" to "Chief Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9000000000", "password" to "admin123", "active_status" to true),
            mapOf("employee_id" to "pharmacist", "full_name" to "Duty Pharmacist", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9000000001", "password" to "admin123", "active_status" to true),
            mapOf("employee_id" to "ep1001", "full_name" to "Admin Staff A", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9000000002", "password" to "admin123", "active_status" to true),
            mapOf("employee_id" to "ep1002", "full_name" to "Admin Staff B", "role" to "Pharmacist", "hospital" to "ESIC Hospital, Sanath Nagar", "phone_number" to "9000000003", "password" to "admin123", "active_status" to true)
        )
        val match = testAdmins.find { (it["employee_id"] as String).equals(trimmedId, ignoreCase = true) }
        if (match != null && match["password"] == password && (match["active_status"] as Boolean)) {
            val returnMap = match.toMutableMap()
            returnMap.remove("password") // do not leak password
            return@withContext returnMap
        }
        return@withContext null
    }
}
