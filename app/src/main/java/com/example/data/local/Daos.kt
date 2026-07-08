package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicineDao {
    @Query("SELECT * FROM inventory ORDER BY medName ASC")
    fun getAllMedicines(): Flow<List<MedicineEntity>>

    @Query("SELECT * FROM inventory WHERE medCode = :medCode LIMIT 1")
    fun getMedicineByCode(medCode: String): Flow<MedicineEntity?>

    @Query("SELECT * FROM inventory WHERE medCode = :medCode LIMIT 1")
    suspend fun getMedicineByCodeSuspend(medCode: String): MedicineEntity?

    @Query("SELECT * FROM inventory WHERE medCode LIKE :prefix || '%' LIMIT 1")
    suspend fun getMedicineByCodePrefixSuspend(prefix: String): MedicineEntity?

    @Query("SELECT * FROM inventory WHERE medName = :medName LIMIT 1")
    suspend fun getMedicineByNameSuspend(medName: String): MedicineEntity?

    @Query("SELECT * FROM inventory WHERE hospital = :hospitalName ORDER BY medName ASC")
    fun getMedicinesByHospital(hospitalName: String): Flow<List<MedicineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(med: MedicineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(meds: List<MedicineEntity>)

    @Update
    suspend fun updateMedicine(med: MedicineEntity)

    @Query("UPDATE inventory SET currentStock = :stock, isAvailable = :isAvailable, expectedRestockDays = :restockDays WHERE medCode = :medCode")
    suspend fun updateMedicineStock(medCode: String, stock: Int, isAvailable: Boolean, restockDays: Int)
}

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions WHERE esicNumber = :esicNumber LIMIT 1")
    fun getPrescriptionByEsicFlow(esicNumber: String): Flow<PrescriptionEntity?>

    @Query("SELECT * FROM prescriptions WHERE esicNumber = :esicNumber LIMIT 1")
    suspend fun getPrescriptionByEsic(esicNumber: String): PrescriptionEntity?

    @Query("SELECT * FROM prescriptions ORDER BY prescribedDate DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescriptions(prescriptions: List<PrescriptionEntity>)
}

@Dao
interface QueueDao {
    @Query("SELECT * FROM hospital_queue WHERE esicNumber = :esicNumber LIMIT 1")
    fun getQueueByEsic(esicNumber: String): Flow<QueueEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(queue: QueueEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueues(queues: List<QueueEntity>)
}

@Dao
interface HospitalStatusDao {
    @Query("SELECT * FROM hospital_status WHERE hospitalName = :hospitalName LIMIT 1")
    fun getStatusByHospital(hospitalName: String): Flow<HospitalStatusEntity?>

    @Query("SELECT * FROM hospital_status WHERE hospitalName = :hospitalName LIMIT 1")
    suspend fun getStatusByHospitalSuspend(hospitalName: String): HospitalStatusEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitalStatus(status: HospitalStatusEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHospitalStatuses(statuses: List<HospitalStatusEntity>)

    @Query("UPDATE hospital_status SET nowServing = :nowServing WHERE hospitalName = :hospitalName")
    suspend fun updateNowServing(hospitalName: String, nowServing: Int)
}

@Dao
interface CrowdReportDao {
    @Query("SELECT * FROM crowd_reports WHERE medCode = :medCode")
    fun getReportsForMedicine(medCode: String): Flow<List<CrowdReportEntity>>

    @Query("SELECT * FROM crowd_reports")
    fun getAllReports(): Flow<List<CrowdReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: CrowdReportEntity)

    @Query("DELETE FROM crowd_reports WHERE medCode = :medCode")
    suspend fun deleteReportsForMedicine(medCode: String)

    @Query("DELETE FROM crowd_reports")
    suspend fun clearAllReports()
}

@Dao
interface NotificationPreferenceDao {
    @Query("SELECT * FROM notifications_enabled WHERE esicNumber = :esicNumber")
    fun getPreferencesForPatient(esicNumber: String): Flow<List<NotificationPreferenceEntity>>

    @Query("SELECT * FROM notifications_enabled WHERE medCode = :medCode")
    fun getPreferencesForMedicine(medCode: String): Flow<List<NotificationPreferenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(pref: NotificationPreferenceEntity)

    @Query("DELETE FROM notifications_enabled WHERE medCode = :medCode AND esicNumber = :esicNumber")
    suspend fun removePreference(medCode: String, esicNumber: String)
}
