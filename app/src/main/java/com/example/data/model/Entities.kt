package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "inventory")
data class MedicineEntity(
    @PrimaryKey val medCode: String,
    val medName: String,
    val currentStock: Int,
    val avgDosage: Int = 1,
    val expectedRestockDays: Int = 0,
    val hospital: String,
    val isAvailable: Boolean = true,
    val category: String = "General"
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey val esicNumber: String,
    val patientName: String,
    val hospitalName: String,
    val prescribedDate: String,
    val tokenNumber: Int,
    val queueStatus: String = "Waiting",
    val medCodes: String // Comma separated medicine codes
)

@Entity(tableName = "hospital_queue")
data class QueueEntity(
    @PrimaryKey val esicNumber: String,
    val tokenNumber: Int,
    val status: String, // WAITING, SERVING, COMPLETED
    val patientName: String,
    val hospitalName: String
)

@Entity(tableName = "hospital_status")
data class HospitalStatusEntity(
    @PrimaryKey val hospitalName: String,
    val nowServing: Int,
    val avgServiceTime: Int = 3 // minutes per patient
)

@Entity(tableName = "crowd_reports")
data class CrowdReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val medCode: String,
    val medName: String,
    val reportText: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications_enabled")
data class NotificationPreferenceEntity(
    @PrimaryKey val medCode: String,
    val medName: String,
    val isEnabled: Boolean = true,
    val esicNumber: String
)
