package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        MedicineEntity::class,
        PrescriptionEntity::class,
        QueueEntity::class,
        HospitalStatusEntity::class,
        CrowdReportEntity::class,
        NotificationPreferenceEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun medicineDao(): MedicineDao
    abstract fun prescriptionDao(): PrescriptionDao
    abstract fun queueDao(): QueueDao
    abstract fun hospitalStatusDao(): HospitalStatusDao
    abstract fun crowdReportDao(): CrowdReportDao
    abstract fun notificationPreferenceDao(): NotificationPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "esic_medstock_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
