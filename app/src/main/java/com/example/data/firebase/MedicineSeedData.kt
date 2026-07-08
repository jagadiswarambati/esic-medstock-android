package com.example.data.firebase

object MedicineSeedData {
    val seedMeds: List<Map<String, Any>> = listOf(
        // 1. ANTIBIOTICS (8 items)
        mapOf(
            "med_code" to "MP-111-ESIC Hospital, Basaidarapur",
            "med_name" to "Linezolid 600mg",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 140,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-112-ESIC Hospital, Peenya",
            "med_name" to "Levofloxacin 500mg",
            "avg_dosage" to 1,
            "category" to "antibiotics",
            "current_stock" to 200,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-113-ESIC Hospital, Sanath Nagar",
            "med_name" to "Piperacillin Tazobactam",
            "avg_dosage" to 3,
            "category" to "antibiotics",
            "current_stock" to 0,
            "expected_restock_days" to 4,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-114-ESIC Hospital, Basaidarapur",
            "med_name" to "Meropenem Injection",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 75,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-115-ESIC Hospital, Peenya",
            "med_name" to "Clindamycin 300mg",
            "avg_dosage" to 3,
            "category" to "antibiotics",
            "current_stock" to 110,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-116-ESIC Hospital, Sanath Nagar",
            "med_name" to "Nitrofurantoin 100mg",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 180,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-117-ESIC Hospital, Basaidarapur",
            "med_name" to "Cefuroxime 500mg",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 0,
            "expected_restock_days" to 3,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-118-ESIC Hospital, Peenya",
            "med_name" to "Cefpodoxime 200mg",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 95,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 2. DIABETES MEDICINES (6 items)
        mapOf(
            "med_code" to "MP-119-ESIC Hospital, Sanath Nagar",
            "med_name" to "Sitagliptin 50mg",
            "avg_dosage" to 1,
            "category" to "diabetes medicines",
            "current_stock" to 300,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-120-ESIC Hospital, Basaidarapur",
            "med_name" to "Voglibose 0.2mg",
            "avg_dosage" to 3,
            "category" to "diabetes medicines",
            "current_stock" to 220,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-121-ESIC Hospital, Peenya",
            "med_name" to "Teneligliptin 20mg",
            "avg_dosage" to 1,
            "category" to "diabetes medicines",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-122-ESIC Hospital, Sanath Nagar",
            "med_name" to "Insulin Glargine",
            "avg_dosage" to 1,
            "category" to "diabetes medicines",
            "current_stock" to 0,
            "expected_restock_days" to 5,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-123-ESIC Hospital, Basaidarapur",
            "med_name" to "Pioglitazone 15mg",
            "avg_dosage" to 1,
            "category" to "diabetes medicines",
            "current_stock" to 140,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-124-ESIC Hospital, Peenya",
            "med_name" to "Gliclazide MR",
            "avg_dosage" to 2,
            "category" to "diabetes medicines",
            "current_stock" to 175,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 3. BP / CARDIAC MEDICINES (6 items)
        mapOf(
            "med_code" to "MP-125-ESIC Hospital, Sanath Nagar",
            "med_name" to "Bisoprolol 5mg",
            "avg_dosage" to 1,
            "category" to "BP / cardiac medicines",
            "current_stock" to 250,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-126-ESIC Hospital, Basaidarapur",
            "med_name" to "Nebivolol 5mg",
            "avg_dosage" to 1,
            "category" to "BP / cardiac medicines",
            "current_stock" to 180,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-127-ESIC Hospital, Peenya",
            "med_name" to "Olmesartan 20mg",
            "avg_dosage" to 1,
            "category" to "BP / cardiac medicines",
            "current_stock" to 0,
            "expected_restock_days" to 6,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-128-ESIC Hospital, Sanath Nagar",
            "med_name" to "Ramipril 5mg",
            "avg_dosage" to 1,
            "category" to "BP / cardiac medicines",
            "current_stock" to 190,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-129-ESIC Hospital, Basaidarapur",
            "med_name" to "Torsemide 10mg",
            "avg_dosage" to 1,
            "category" to "BP / cardiac medicines",
            "current_stock" to 130,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-130-ESIC Hospital, Peenya",
            "med_name" to "Nicorandil 5mg",
            "avg_dosage" to 2,
            "category" to "BP / cardiac medicines",
            "current_stock" to 120,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 4. GASTRIC MEDICINES (5 items)
        mapOf(
            "med_code" to "MP-131-ESIC Hospital, Sanath Nagar",
            "med_name" to "Esomeprazole 40mg",
            "avg_dosage" to 1,
            "category" to "gastric medicines",
            "current_stock" to 280,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-132-ESIC Hospital, Basaidarapur",
            "med_name" to "Sucralfate Syrup",
            "avg_dosage" to 3,
            "category" to "gastric medicines",
            "current_stock" to 0,
            "expected_restock_days" to 4,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-133-ESIC Hospital, Peenya",
            "med_name" to "Antacid Gel",
            "avg_dosage" to 3,
            "category" to "gastric medicines",
            "current_stock" to 350,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-134-ESIC Hospital, Sanath Nagar",
            "med_name" to "Famotidine 20mg",
            "avg_dosage" to 2,
            "category" to "gastric medicines",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-135-ESIC Hospital, Basaidarapur",
            "med_name" to "Digestive Enzyme Syrup",
            "avg_dosage" to 2,
            "category" to "gastric medicines",
            "current_stock" to 180,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 5. RESPIRATORY / ASTHMA (5 items)
        mapOf(
            "med_code" to "MP-136-ESIC Hospital, Peenya",
            "med_name" to "Deriphyllin",
            "avg_dosage" to 2,
            "category" to "respiratory / asthma",
            "current_stock" to 200,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-137-ESIC Hospital, Sanath Nagar",
            "med_name" to "Tiotropium Inhaler",
            "avg_dosage" to 1,
            "category" to "respiratory / asthma",
            "current_stock" to 90,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-138-ESIC Hospital, Basaidarapur",
            "med_name" to "Formoterol Inhaler",
            "avg_dosage" to 2,
            "category" to "respiratory / asthma",
            "current_stock" to 0,
            "expected_restock_days" to 3,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-139-ESIC Hospital, Peenya",
            "med_name" to "Ambroxol Syrup",
            "avg_dosage" to 3,
            "category" to "respiratory / asthma",
            "current_stock" to 240,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-140-ESIC Hospital, Sanath Nagar",
            "med_name" to "Levosalbutamol Syrup",
            "avg_dosage" to 3,
            "category" to "respiratory / asthma",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 6. PAINKILLERS / FEVER (5 items)
        mapOf(
            "med_code" to "MP-141-ESIC Hospital, Basaidarapur",
            "med_name" to "Tramadol 50mg",
            "avg_dosage" to 2,
            "category" to "painkillers / fever",
            "current_stock" to 110,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-142-ESIC Hospital, Peenya",
            "med_name" to "Ketorolac Injection",
            "avg_dosage" to 1,
            "category" to "painkillers / fever",
            "current_stock" to 85,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-143-ESIC Hospital, Sanath Nagar",
            "med_name" to "Naproxen 500mg",
            "avg_dosage" to 2,
            "category" to "painkillers / fever",
            "current_stock" to 0,
            "expected_restock_days" to 5,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-144-ESIC Hospital, Basaidarapur",
            "med_name" to "Etoricoxib 90mg",
            "avg_dosage" to 1,
            "category" to "painkillers / fever",
            "current_stock" to 190,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-145-ESIC Hospital, Peenya",
            "med_name" to "Tapentadol 50mg",
            "avg_dosage" to 2,
            "category" to "painkillers / fever",
            "current_stock" to 130,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 7. VITAMINS / SUPPLEMENTS (6 items)
        mapOf(
            "med_code" to "MP-146-ESIC Hospital, Sanath Nagar",
            "med_name" to "Zincovit",
            "avg_dosage" to 1,
            "category" to "vitamins / supplements",
            "current_stock" to 400,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-147-ESIC Hospital, Basaidarapur",
            "med_name" to "Shelcal 500",
            "avg_dosage" to 1,
            "category" to "vitamins / supplements",
            "current_stock" to 380,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-148-ESIC Hospital, Peenya",
            "med_name" to "Vitamin C Tablets",
            "avg_dosage" to 1,
            "category" to "vitamins / supplements",
            "current_stock" to 0,
            "expected_restock_days" to 3,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-149-ESIC Hospital, Sanath Nagar",
            "med_name" to "Methylcobalamin",
            "avg_dosage" to 1,
            "category" to "vitamins / supplements",
            "current_stock" to 220,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-150-ESIC Hospital, Basaidarapur",
            "med_name" to "Protein Powder Sachets",
            "avg_dosage" to 1,
            "category" to "vitamins / supplements",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-151-ESIC Hospital, Peenya",
            "med_name" to "Calcium Citrate",
            "avg_dosage" to 2,
            "category" to "vitamins / supplements",
            "current_stock" to 170,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 8. SKIN / CREAMS (5 items)
        mapOf(
            "med_code" to "MP-152-ESIC Hospital, Sanath Nagar",
            "med_name" to "Betnovate Cream",
            "avg_dosage" to 1,
            "category" to "skin / creams",
            "current_stock" to 130,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-153-ESIC Hospital, Basaidarapur",
            "med_name" to "Soframycin Cream",
            "avg_dosage" to 1,
            "category" to "skin / creams",
            "current_stock" to 0,
            "expected_restock_days" to 4,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-154-ESIC Hospital, Peenya",
            "med_name" to "Silverex Cream",
            "avg_dosage" to 1,
            "category" to "skin / creams",
            "current_stock" to 160,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-155-ESIC Hospital, Sanath Nagar",
            "med_name" to "Permethrin Lotion",
            "avg_dosage" to 1,
            "category" to "skin / creams",
            "current_stock" to 95,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-156-ESIC Hospital, Basaidarapur",
            "med_name" to "Clotrimazole Dusting Powder",
            "avg_dosage" to 2,
            "category" to "skin / creams",
            "current_stock" to 140,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 9. THYROID / HORMONAL (3 items)
        mapOf(
            "med_code" to "MP-157-ESIC Hospital, Peenya",
            "med_name" to "Thyronorm 75mcg",
            "avg_dosage" to 1,
            "category" to "thyroid / hormonal",
            "current_stock" to 310,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-158-ESIC Hospital, Sanath Nagar",
            "med_name" to "Prednisolone 10mg",
            "avg_dosage" to 1,
            "category" to "thyroid / hormonal",
            "current_stock" to 0,
            "expected_restock_days" to 5,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-159-ESIC Hospital, Basaidarapur",
            "med_name" to "Hydrocortisone Injection",
            "avg_dosage" to 1,
            "category" to "thyroid / hormonal",
            "current_stock" to 85,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // 10. GENERAL MEDICINES (5 items + original list)
        mapOf(
            "med_code" to "MP-160-ESIC Hospital, Peenya",
            "med_name" to "ORS Sachets",
            "avg_dosage" to 2,
            "category" to "general medicines",
            "current_stock" to 420,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-161-ESIC Hospital, Sanath Nagar",
            "med_name" to "Electrolyte Powder",
            "avg_dosage" to 1,
            "category" to "general medicines",
            "current_stock" to 200,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-162-ESIC Hospital, Basaidarapur",
            "med_name" to "Lactulose Syrup",
            "avg_dosage" to 2,
            "category" to "general medicines",
            "current_stock" to 160,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-163-ESIC Hospital, Peenya",
            "med_name" to "Cough Syrup",
            "avg_dosage" to 3,
            "category" to "general medicines",
            "current_stock" to 0,
            "expected_restock_days" to 3,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-164-ESIC Hospital, Sanath Nagar",
            "med_name" to "Antifungal Tablets",
            "avg_dosage" to 1,
            "category" to "general medicines",
            "current_stock" to 140,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),

        // ORIGINAL 40 MEDICINES
        mapOf(
            "med_code" to "MP-201-ESIC Hospital, Basaidarapur",
            "med_name" to "Amoxicillin 500mg",
            "avg_dosage" to 3,
            "category" to "antibiotics",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-202-ESIC Hospital, Peenya",
            "med_name" to "Azithromycin 500mg",
            "avg_dosage" to 1,
            "category" to "antibiotics",
            "current_stock" to 120,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-203-ESIC Hospital, Sanath Nagar",
            "med_name" to "Cefixime 200mg",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 0,
            "expected_restock_days" to 5,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-204-ESIC Hospital, Basaidarapur",
            "med_name" to "Ciprofloxacin 500mg",
            "avg_dosage" to 2,
            "category" to "antibiotics",
            "current_stock" to 90,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-205-ESIC Hospital, Peenya",
            "med_name" to "Amlodipine 5mg",
            "avg_dosage" to 1,
            "category" to "BP medicines",
            "current_stock" to 200,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-206-ESIC Hospital, Sanath Nagar",
            "med_name" to "Telmisartan 40mg",
            "avg_dosage" to 1,
            "category" to "BP medicines",
            "current_stock" to 180,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-207-ESIC Hospital, Basaidarapur",
            "med_name" to "Losartan 50mg",
            "avg_dosage" to 1,
            "category" to "BP medicines",
            "current_stock" to 0,
            "expected_restock_days" to 8,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-208-ESIC Hospital, Peenya",
            "med_name" to "Carvedilol 6.25mg",
            "avg_dosage" to 2,
            "category" to "BP medicines",
            "current_stock" to 110,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-209-ESIC Hospital, Sanath Nagar",
            "med_name" to "Metformin 500mg",
            "avg_dosage" to 2,
            "category" to "diabetes medicines",
            "current_stock" to 300,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-210-ESIC Hospital, Basaidarapur",
            "med_name" to "Glimepiride 2mg",
            "avg_dosage" to 1,
            "category" to "diabetes medicines",
            "current_stock" to 250,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-211-ESIC Hospital, Peenya",
            "med_name" to "Vildagliptin 50mg",
            "avg_dosage" to 2,
            "category" to "diabetes medicines",
            "current_stock" to 0,
            "expected_restock_days" to 4,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-212-ESIC Hospital, Sanath Nagar",
            "med_name" to "Gliclazide 80mg",
            "avg_dosage" to 1,
            "category" to "diabetes medicines",
            "current_stock" to 140,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-213-ESIC Hospital, Basaidarapur",
            "med_name" to "Paracetamol 500mg",
            "avg_dosage" to 3,
            "category" to "fever medicines",
            "current_stock" to 400,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-214-ESIC Hospital, Peenya",
            "med_name" to "Dolo 650mg",
            "avg_dosage" to 3,
            "category" to "fever medicines",
            "current_stock" to 350,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-215-ESIC Hospital, Sanath Nagar",
            "med_name" to "Ibuprofen 400mg",
            "avg_dosage" to 2,
            "category" to "fever medicines",
            "current_stock" to 0,
            "expected_restock_days" to 3,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-216-ESIC Hospital, Basaidarapur",
            "med_name" to "Mefenamic Acid 500mg",
            "avg_dosage" to 2,
            "category" to "fever medicines",
            "current_stock" to 160,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-217-ESIC Hospital, Peenya",
            "med_name" to "Vitamin D3 60k UI",
            "avg_dosage" to 1,
            "category" to "vitamin tablets",
            "current_stock" to 500,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-218-ESIC Hospital, Sanath Nagar",
            "med_name" to "Zinc Tablets 20mg",
            "avg_dosage" to 1,
            "category" to "vitamin tablets",
            "current_stock" to 420,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-219-ESIC Hospital, Basaidarapur",
            "med_name" to "Calcium Tablets 500mg",
            "avg_dosage" to 2,
            "category" to "vitamin tablets",
            "current_stock" to 0,
            "expected_restock_days" to 6,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-220-ESIC Hospital, Peenya",
            "med_name" to "Vitamin B-Complex",
            "avg_dosage" to 1,
            "category" to "vitamin tablets",
            "current_stock" to 280,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-221-ESIC Hospital, Sanath Nagar",
            "med_name" to "Insulin Regular 100 IU",
            "avg_dosage" to 1,
            "category" to "injections",
            "current_stock" to 80,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-222-ESIC Hospital, Basaidarapur",
            "med_name" to "Ceftriaxone Injection 1g",
            "avg_dosage" to 1,
            "category" to "injections",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-223-ESIC Hospital, Peenya",
            "med_name" to "Diclofenac Injection 75mg",
            "avg_dosage" to 1,
            "category" to "injections",
            "current_stock" to 0,
            "expected_restock_days" to 2,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-224-ESIC Hospital, Sanath Nagar",
            "med_name" to "Pantoprazole Injection 40mg",
            "avg_dosage" to 1,
            "category" to "injections",
            "current_stock" to 130,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-225-ESIC Hospital, Basaidarapur",
            "med_name" to "Pantoprazole 40mg",
            "avg_dosage" to 1,
            "category" to "gastric medicines",
            "current_stock" to 310,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-226-ESIC Hospital, Peenya",
            "med_name" to "Omeprazole 20mg",
            "avg_dosage" to 1,
            "category" to "gastric medicines",
            "current_stock" to 240,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-227-ESIC Hospital, Sanath Nagar",
            "med_name" to "Ranitidine 150mg",
            "avg_dosage" to 2,
            "category" to "gastric medicines",
            "current_stock" to 0,
            "expected_restock_days" to 4,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-228-ESIC Hospital, Basaidarapur",
            "med_name" to "Domperidone 10mg",
            "avg_dosage" to 1,
            "category" to "gastric medicines",
            "current_stock" to 190,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-229-ESIC Hospital, Peenya",
            "med_name" to "Salbutamol Inhaler",
            "avg_dosage" to 2,
            "category" to "asthma medicines",
            "current_stock" to 160,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-230-ESIC Hospital, Sanath Nagar",
            "med_name" to "Budecort Inhaler 200",
            "avg_dosage" to 2,
            "category" to "asthma medicines",
            "current_stock" to 95,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-231-ESIC Hospital, Basaidarapur",
            "med_name" to "Montelukast 10mg",
            "avg_dosage" to 1,
            "category" to "asthma medicines",
            "current_stock" to 0,
            "expected_restock_days" to 5,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-232-ESIC Hospital, Peenya",
            "med_name" to "Levosalbutamol Inhaler",
            "avg_dosage" to 2,
            "category" to "asthma medicines",
            "current_stock" to 85,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-233-ESIC Hospital, Sanath Nagar",
            "med_name" to "Thyronorm 50mcg",
            "avg_dosage" to 1,
            "category" to "thyroid medicines",
            "current_stock" to 340,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-234-ESIC Hospital, Basaidarapur",
            "med_name" to "Thyronorm 100mcg",
            "avg_dosage" to 1,
            "category" to "thyroid medicines",
            "current_stock" to 220,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-235-ESIC Hospital, Peenya",
            "med_name" to "Thyrox 75mcg",
            "avg_dosage" to 1,
            "category" to "thyroid medicines",
            "current_stock" to 0,
            "expected_restock_days" to 7,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-236-ESIC Hospital, Sanath Nagar",
            "med_name" to "Eltroxin 25mcg",
            "avg_dosage" to 1,
            "category" to "thyroid medicines",
            "current_stock" to 150,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-237-ESIC Hospital, Basaidarapur",
            "med_name" to "Atorvastatin 10mg",
            "avg_dosage" to 1,
            "category" to "cardiac medicines",
            "current_stock" to 450,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-238-ESIC Hospital, Peenya",
            "med_name" to "Aspirin 75mg",
            "avg_dosage" to 1,
            "category" to "cardiac medicines",
            "current_stock" to 390,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Peenya",
            "is_available" to true,
            "status" to "AVAILABLE"
        ),
        mapOf(
            "med_code" to "MP-239-ESIC Hospital, Sanath Nagar",
            "med_name" to "Clopidogrel 75mg",
            "avg_dosage" to 1,
            "category" to "cardiac medicines",
            "current_stock" to 0,
            "expected_restock_days" to 6,
            "hospital" to "ESIC Hospital, Sanath Nagar",
            "is_available" to false,
            "status" to "OUT_OF_STOCK"
        ),
        mapOf(
            "med_code" to "MP-240-ESIC Hospital, Basaidarapur",
            "med_name" to "Isosorbide Dinitrate 10mg",
            "avg_dosage" to 1,
            "category" to "cardiac medicines",
            "current_stock" to 130,
            "expected_restock_days" to 0,
            "hospital" to "ESIC Hospital, Basaidarapur",
            "is_available" to true,
            "status" to "AVAILABLE"
        )
    )
}
