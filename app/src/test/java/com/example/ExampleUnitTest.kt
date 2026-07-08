package com.example

import com.example.data.model.MedicineEntity
import com.example.ui.screens.matchRecipientPrescriptionMeds
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testMedicineMatchingLogic() {
        val allMedicines = listOf(
            MedicineEntity(
                medCode = "MP-104-ESIC Hospital, ",
                medName = "Paracetamol 500mg",
                currentStock = 120,
                hospital = "ESIC Hospital, Peenya"
            ),
            MedicineEntity(
                medCode = "MP-104-ESIC Hospital, Ba",
                medName = "Paracetamol 500mg",
                currentStock = 50,
                hospital = "ESIC Hospital, Basaidarapur"
            ),
            MedicineEntity(
                medCode = "MP-105-ESIC Hospital, ",
                medName = "Ibuprofen 400mg",
                currentStock = 90,
                hospital = "ESIC Hospital, Peenya"
            ),
            MedicineEntity(
                medCode = "DOLO650",
                medName = "Dolo 650mg",
                currentStock = 300,
                hospital = "General Hospital"
            )
        )

        // Test 1: Trimmed match with spacing and hospital prefix match
        val matched1 = matchRecipientPrescriptionMeds(
            medCodesString = "MP-104-ESIC Hospital, Pe , MP-105-ESIC Hospital, Pe",
            patientHospitalName = "ESIC Hospital, Peenya",
            allMedicines = allMedicines
        )
        assertEquals(2, matched1.size)
        assertEquals("MP-104-ESIC Hospital, ", matched1[0].medCode)
        assertEquals("ESIC Hospital, Peenya", matched1[0].hospital)
        assertEquals("MP-105-ESIC Hospital, ", matched1[1].medCode)

        // Test 2: Hospital filtering (ensure we get the correct hospital match for prefix)
        val matched2 = matchRecipientPrescriptionMeds(
            medCodesString = "MP-104-ESIC Hospital, Ba",
            patientHospitalName = "ESIC Hospital, Basaidarapur",
            allMedicines = allMedicines
        )
        assertEquals(1, matched2.size)
        assertEquals("MP-104-ESIC Hospital, Ba", matched2[0].medCode)
        assertEquals("ESIC Hospital, Basaidarapur", matched2[0].hospital)

        // Test 3: Fallback globally when hospital name does not match
        val matched3 = matchRecipientPrescriptionMeds(
            medCodesString = "DOLO650",
            patientHospitalName = "Unknown Hospital",
            allMedicines = allMedicines
        )
        assertEquals(1, matched3.size)
        assertEquals("DOLO650", matched3[0].medCode)
    }
}
