package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

class HealthRepository(private val db: AppDatabase) {

    private val medicineDao = db.medicineDao()
    private val prescriptionDao = db.prescriptionDao()
    private val queueDao = db.queueDao()
    private val hospitalStatusDao = db.hospitalStatusDao()
    private val crowdReportDao = db.crowdReportDao()
    private val notificationPreferenceDao = db.notificationPreferenceDao()

    // Flow getters
    fun getAllMedicines(): Flow<List<MedicineEntity>> = medicineDao.getAllMedicines()
    fun getMedicinesByHospital(hospital: String): Flow<List<MedicineEntity>> = medicineDao.getMedicinesByHospital(hospital)
    fun getMedicineByCode(medCode: String): Flow<MedicineEntity?> = medicineDao.getMedicineByCode(medCode)
    fun getPrescription(esicNumber: String): Flow<PrescriptionEntity?> = prescriptionDao.getPrescriptionByEsicFlow(esicNumber)
    fun getQueueByEsic(esicNumber: String): Flow<QueueEntity?> = queueDao.getQueueByEsic(esicNumber)
    fun getStatusByHospital(hospital: String): Flow<HospitalStatusEntity?> = hospitalStatusDao.getStatusByHospital(hospital)
    fun getAllCrowdReports(): Flow<List<CrowdReportEntity>> = crowdReportDao.getAllReports()
    fun getCrowdReportsForMedicine(medCode: String): Flow<List<CrowdReportEntity>> = crowdReportDao.getReportsForMedicine(medCode)
    fun getNotificationPreferences(esicNumber: String): Flow<List<NotificationPreferenceEntity>> = notificationPreferenceDao.getPreferencesForPatient(esicNumber)

    // Suspend updates
    suspend fun insertMedicine(med: MedicineEntity) = medicineDao.insertMedicine(med)
    suspend fun updateMedicine(med: MedicineEntity) = medicineDao.updateMedicine(med)
    suspend fun updateStock(medCode: String, stock: Int, isAvailable: Boolean, restockDays: Int) {
        medicineDao.updateMedicineStock(medCode, stock, isAvailable, restockDays)
    }

    suspend fun addCrowdReport(report: CrowdReportEntity) = crowdReportDao.insertReport(report)
    suspend fun clearCrowdReports(medCode: String) = crowdReportDao.deleteReportsForMedicine(medCode)
    suspend fun clearAllCrowdReports() = crowdReportDao.clearAllReports()

    suspend fun updateNowServing(hospital: String, token: Int) = hospitalStatusDao.updateNowServing(hospital, token)

    suspend fun enableNotification(pref: NotificationPreferenceEntity) = notificationPreferenceDao.insertPreference(pref)
    suspend fun disableNotification(medCode: String, esicNumber: String) = notificationPreferenceDao.removePreference(medCode, esicNumber)

    suspend fun getPrescriptionByEsic(esicNumber: String): PrescriptionEntity? = prescriptionDao.getPrescriptionByEsic(esicNumber)
    suspend fun getMedicineByCodeSuspend(medCode: String): MedicineEntity? = medicineDao.getMedicineByCodeSuspend(medCode)

    // Prepopulate DB with high quality simulated data
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val meds = medicineDao.getAllMedicines().firstOrNull() ?: emptyList()
        if (meds.isEmpty()) {
            val hospitals = listOf(
                "ESIC Hospital, Sanath Nagar",
                "ESIC Hospital, Peenya",
                "ESIC Hospital, Basaidarapur"
            )

            val initialMeds = mutableListOf<MedicineEntity>()
            val medsData = listOf(
                Triple("MP-101", "Paracetamol 500mg", "Analgesic"),
                Triple("MP-102", "Metformin 500mg", "Anti-diabetic"),
                Triple("MP-103", "Atorvastatin 10mg", "Cardiovascular"),
                Triple("MP-104", "Cetirizine 10mg", "Anti-allergic"),
                Triple("MP-105", "Amoxicillin 500mg", "Antibiotic"),
                Triple("MP-106", "Amlodipine 5mg", "Cardiovascular"),
                Triple("MP-107", "Pantoprazole 40mg", "Gastrointestinal"),
                Triple("MP-108", "Ibuprofen 400mg", "Analgesic"),
                Triple("MP-109", "Losartan 50mg", "Cardiovascular"),
                Triple("MP-110", "Vitamin D3 60k UI", "Vitamins"),
                Triple("MP-111", "Dolo 650", "Analgesic"),
                Triple("MP-112", "Azithromycin 500mg", "Antibiotic"),
                Triple("MP-113", "Omeprazole 20mg", "Gastrointestinal"),
                Triple("MP-114", "Telmisartan 40mg", "Cardiovascular"),
                Triple("MP-115", "Aspirin 75mg", "Cardiovascular"),
                Triple("MP-116", "Crocin Advance", "Analgesic"),
                Triple("MP-117", "ORS Sachet", "Rehydration"),
                Triple("MP-118", "Zinc Tablets 20mg", "Nutritional"),
                Triple("MP-119", "Calcium Tablets", "Nutritional"),
                Triple("MP-120", "Iron Folic Acid", "Nutritional"),
                Triple("MP-121", "Levocetirizine 5mg", "Anti-allergic"),
                Triple("MP-122", "Ranitidine 150mg", "Gastrointestinal"),
                Triple("MP-123", "Ondansetron 4mg", "Anti-emetic"),
                Triple("MP-124", "Domperidone 10mg", "Gastrointestinal"),
                Triple("MP-125", "Diclofenac 50mg", "Analgesic"),
                Triple("MP-126", "Aceclofenac 100mg", "Analgesic"),
                Triple("MP-127", "Insulin Regular", "Anti-diabetic"),
                Triple("MP-128", "Novorapid Flexpen", "Anti-diabetic"),
                Triple("MP-129", "Human Mixtard", "Anti-diabetic"),
                Triple("MP-130", "Salbutamol Inhaler", "Respiratory"),
                Triple("MP-131", "Budecort Inhaler", "Respiratory"),
                Triple("MP-132", "Montelukast 10mg", "Respiratory"),
                Triple("MP-133", "Clopidogrel 75mg", "Cardiovascular"),
                Triple("MP-134", "Ecosprin AV", "Cardiovascular"),
                Triple("MP-135", "Thyronorm 50mcg", "Hormonal"),
                Triple("MP-136", "Glimepiride 2mg", "Anti-diabetic"),
                Triple("MP-137", "Vildagliptin Metformin", "Anti-diabetic"),
                Triple("MP-138", "Cefixime 200mg", "Antibiotic"),
                Triple("MP-139", "Ceftriaxone Injection", "Antibiotic"),
                Triple("MP-140", "Rabeprazole 20mg", "Gastrointestinal"),
                Triple("MP-141", "Lactulose Syrup", "Gastrointestinal"),
                Triple("MP-142", "PCM Syrup", "Analgesic"),
                Triple("MP-143", "Benadryl Syrup", "Respiratory"),
                Triple("MP-144", "Amoxiclav 625", "Antibiotic"),
                Triple("MP-145", "Albendazole 400mg", "Antiparasitic"),
                Triple("MP-146", "Mefenamic Acid", "Analgesic"),
                Triple("MP-147", "Norfloxacin 400mg", "Antibiotic"),
                Triple("MP-148", "Ciprofloxacin 500mg", "Antibiotic"),
                Triple("MP-149", "Dexamethasone 4mg", "Steroid"),
                Triple("MP-150", "Multivitamin Tablets", "Vitamins")
            )

            // Generate standard meds for each hospital to simulate stock differences
            for (hosp in hospitals) {
                medsData.forEachIndexed { index, data ->
                    val code = "${data.first}-${hosp.take(15)}"
                    val name = data.second
                    val category = data.third
                    
                    val currentStock = when {
                        index % 11 == 0 -> 0 // Out of stock
                        index % 7 == 0 -> (index % 5) + 1 // Very low stock
                        else -> (index * 13) % 400 + 10 // Normal stock
                    }
                    val isAvailable = currentStock > 0
                    val expectedRestockDays = if (!isAvailable) (index % 9) + 2 else 0
                    val avgDosage = (index % 3) + 1

                    initialMeds.add(
                        MedicineEntity(
                            medCode = code,
                            medName = name,
                            currentStock = currentStock,
                            avgDosage = avgDosage,
                            expectedRestockDays = expectedRestockDays,
                            hospital = hosp,
                            isAvailable = isAvailable,
                            category = category
                        )
                    )
                }
            }

            // Prepopulate specific user high-priority medicines
            val userMeds = listOf(
                MedicineEntity("PCM500", "Paracetamol 500mg", 120, 10, 0, "ESIC Hospital, Sanath Nagar", true, "Analgesic"),
                MedicineEntity("DOLO650", "Dolo 650", 85, 5, 0, "ESIC Hospital, Sanath Nagar", true, "Analgesic"),
                MedicineEntity("AZ500", "Azithromycin 500mg", 140, 3, 0, "ESIC Hospital, Sanath Nagar", true, "Antibiotic"),
                MedicineEntity("PAN40", "Pantoprazole 40mg", 100, 1, 0, "ESIC Hospital, Sanath Nagar", true, "Gastrointestinal")
            )
            initialMeds.addAll(userMeds)

            medicineDao.insertMedicines(initialMeds)

            // Setup prescriptions
            val initialPrescriptions = listOf(
                PrescriptionEntity(
                    "1234567890", "Ramesh Kumar", "ESIC Hospital, Sanath Nagar", "2026-05-20", 45, "Waiting",
                    "PCM500,DOLO650,AZ500,PAN40"
                ),
                PrescriptionEntity(
                    "9876543210", "Sunita Devi", "ESIC Hospital, Basaidarapur", "2026-05-21", 12, "Waiting",
                    "MP-104-ESIC Hospital, Ba,MP-105-ESIC Hospital, Ba,MP-108-ESIC Hospital, Ba"
                ),
                PrescriptionEntity(
                    "1122334455", "Lakshmi Prasanna", "ESIC Hospital, Peenya", "2026-05-22", 89, "Waiting",
                    "MP-102-ESIC Hospital, Pe,MP-106-ESIC Hospital, Pe,MP-110-ESIC Hospital, Pe"
                )
            )
            prescriptionDao.insertPrescriptions(initialPrescriptions)

            // Setup queues
            val initialQueues = listOf(
                QueueEntity("1234567890", 45, "WAITING", "Ramesh Kumar", "ESIC Hospital, Sanath Nagar"),
                QueueEntity("9876543210", 12, "WAITING", "Sunita Devi", "ESIC Hospital, Basaidarapur"),
                QueueEntity("1122334455", 89, "WAITING", "Lakshmi Prasanna", "ESIC Hospital, Peenya")
            )
            queueDao.insertQueues(initialQueues)

            // Setup hospital serving status
            val initialStatuses = listOf(
                HospitalStatusEntity("ESIC Hospital, Sanath Nagar", 40, 2),
                HospitalStatusEntity("ESIC Hospital, Peenya", 85, 3),
                HospitalStatusEntity("ESIC Hospital, Basaidarapur", 7, 3)
            )
            hospitalStatusDao.insertHospitalStatuses(initialStatuses)
        }
    }
}
