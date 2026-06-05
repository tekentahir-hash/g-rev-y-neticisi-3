package com.senin.taskmanager.data

import java.time.LocalDate
import java.util.UUID

/**
 * Excel dosyasındaki ev görevleri — uygulama ilk açıldığında otomatik eklenir.
 * Her tekrarlı görev grubuna benzersiz groupId verilir.
 */
fun buildDefaultTasks(startDate: LocalDate): List<Task> {
    val tasks = mutableListOf<Task>()
    val today = startDate
    val endDate = today.plusDays(365)

    // ── GÜNLÜK GÖREVLER ─────────────────────────────────────────
    listOf(
        "Yemek yap" to TaskPriority.IMPORTANT,
        "Ranın çantasını kontrol et" to TaskPriority.IMPORTANT,
        "Lazımlık boşalt" to TaskPriority.OTHER,
        "Çamaşır makinesi doldur ve boşalt" to TaskPriority.OTHER,
        "Bulaşık makinesi doldur ve boşalt" to TaskPriority.OTHER,
    ).forEach { (title, prio) ->
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = title, frequency = TaskFrequency.DAILY,
                priority = prio, dueDate = cur.toString(), isDefault = true))
            cur = cur.plusDays(1)
        }
    }

    // ── 3 GÜNDE BİR ─────────────────────────────────────────────
    listOf(
        "Çiçekleri sula" to TaskPriority.OTHER,
        "Kuşlara yem ve su ver" to TaskPriority.OTHER,
    ).forEach { (title, prio) ->
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = title, frequency = TaskFrequency.EVERY_3_DAYS,
                priority = prio, dueDate = cur.toString(), isDefault = true))
            cur = cur.plusDays(3)
        }
    }

    // ── BELİRLİ GÜNLER: Pzt + Çarş + Cuma = Fitness ────────────
    run {
        val gid = UUID.randomUUID().toString()
        val fitnessDays = setOf(1, 3, 5) // Pzt, Çarş, Cuma
        var cur = today
        while (!cur.isAfter(endDate)) {
            if (cur.dayOfWeek.value in fitnessDays) {
                tasks.add(Task(groupId = gid, title = "Fitness", frequency = TaskFrequency.SPECIFIC_DAYS,
                    priority = TaskPriority.IMPORTANT, dueDate = cur.toString(),
                    specificDays = "1,3,5", isDefault = true))
            }
            cur = cur.plusDays(1)
        }
    }

    // ── BELİRLİ GÜNLER: Salı + Perş + Cmrt = Hamam/Havuz ───────
    run {
        val gid = UUID.randomUUID().toString()
        val days = setOf(2, 4, 6) // Salı, Perşembe, Cumartesi
        var cur = today
        while (!cur.isAfter(endDate)) {
            if (cur.dayOfWeek.value in days) {
                tasks.add(Task(groupId = gid, title = "Hamam / Havuz", frequency = TaskFrequency.SPECIFIC_DAYS,
                    priority = TaskPriority.IMPORTANT, dueDate = cur.toString(),
                    specificDays = "2,4,6", isDefault = true))
            }
            cur = cur.plusDays(1)
        }
    }

    // ── HAFTALIK: Salı = Bale hazırlığı ─────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != 2) cur = cur.plusDays(1) // ilk Salı
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Bale: patik, tütülü etekl, k.çorap koy",
                frequency = TaskFrequency.WEEKLY, priority = TaskPriority.IMPORTANT,
                dueDate = cur.toString(), specificDays = "2", isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── HAFTALIK: Çarşamba = Havuz hazırlığı ────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != 3) cur = cur.plusDays(1)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Havuz: bone, terlik, mayo, havlu, gözlük, patiği hazırla",
                frequency = TaskFrequency.WEEKLY, priority = TaskPriority.IMPORTANT,
                dueDate = cur.toString(), specificDays = "3", isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── HAFTALIK: Çarşamba = İade/değişim paketleme ─────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != 3) cur = cur.plusDays(1)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "İade ve değişim paketleme, iade kodu hazırla",
                frequency = TaskFrequency.WEEKLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), specificDays = "3", isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── HAFTALIK: Perşembe = Oyuncak koy ────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != 4) cur = cur.plusDays(1)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Oyuncak koy (Vera)",
                frequency = TaskFrequency.WEEKLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), specificDays = "4", isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── HAFTALIK: Cuma = Yemek planlama ─────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != 5) cur = cur.plusDays(1)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Yemek planla ve Pazar alışveriş listesi oluştur",
                frequency = TaskFrequency.WEEKLY, priority = TaskPriority.IMPORTANT,
                dueDate = cur.toString(), specificDays = "5", isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── HAFTALIK: Perşembe + Pazar = Çocuklar banyo ─────────────
    listOf(4, 7).forEach { dayVal ->
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (cur.dayOfWeek.value != dayVal) cur = cur.plusDays(1)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Çocuklar banyo",
                frequency = TaskFrequency.SPECIFIC_DAYS, priority = TaskPriority.IMPORTANT,
                dueDate = cur.toString(), specificDays = "4,7", isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── HAFTADA 2 KEZ: Yastık yüzleri ───────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Yastık yüzleri değiştir",
                frequency = TaskFrequency.BIWEEKLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), isDefault = true))
            cur = cur.plusDays(3) // haftada 2 ≈ her 3-4 günde bir
            if (!cur.isAfter(endDate)) {
                tasks.add(Task(groupId = gid, title = "Yastık yüzleri değiştir",
                    frequency = TaskFrequency.BIWEEKLY, priority = TaskPriority.OTHER,
                    dueDate = cur.toString(), isDefault = true))
                cur = cur.plusDays(4)
            }
        }
    }

    // ── HAFTADA 2 KEZ: Havlu değişimi ───────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Havlu değiştir",
                frequency = TaskFrequency.BIWEEKLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), isDefault = true))
            cur = cur.plusDays(3)
            if (!cur.isAfter(endDate)) {
                tasks.add(Task(groupId = gid, title = "Havlu değiştir",
                    frequency = TaskFrequency.BIWEEKLY, priority = TaskPriority.OTHER,
                    dueDate = cur.toString(), isDefault = true))
                cur = cur.plusDays(4)
            }
        }
    }

    // ── HAFTADA 2 KEZ: Lazımlık yıkama ──────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Lazımlık yıka",
                frequency = TaskFrequency.BIWEEKLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), isDefault = true))
            cur = cur.plusDays(3)
            if (!cur.isAfter(endDate)) {
                tasks.add(Task(groupId = gid, title = "Lazımlık yıka",
                    frequency = TaskFrequency.BIWEEKLY, priority = TaskPriority.OTHER,
                    dueDate = cur.toString(), isDefault = true))
                cur = cur.plusDays(4)
            }
        }
    }

    // ── HAFTADA 1: Veranın suluğunu yıka ────────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Veranın suluğunu yıka",
                frequency = TaskFrequency.WEEKLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), isDefault = true))
            cur = cur.plusDays(7)
        }
    }

    // ── AYDA 1: Betül kıyafet sirkülasyonu ──────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Betül kıyafet sirkülasyonu",
                frequency = TaskFrequency.MONTHLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), isDefault = true))
            cur = cur.plusMonths(1)
        }
    }

    // ── AYDA 1: Vera oyuncak sirkülasyonu ───────────────────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Veranın oyuncak sirkülasyonu",
                frequency = TaskFrequency.MONTHLY, priority = TaskPriority.OTHER,
                dueDate = cur.toString(), isDefault = true))
            cur = cur.plusMonths(1)
        }
    }

    // ── HER AYIN 12'si: Aşı takvimi ve boy kilo kontrol ────────
    run {
        val gid = UUID.randomUUID().toString()
        var cur = today.withDayOfMonth(12)
        if (cur.isBefore(today)) cur = cur.plusMonths(1).withDayOfMonth(12)
        while (!cur.isAfter(endDate)) {
            tasks.add(Task(groupId = gid, title = "Aşı takvimi ve boy-kilo kontrol",
                frequency = TaskFrequency.MONTHLY, priority = TaskPriority.IMPORTANT,
                dueDate = cur.toString(), monthDay = 12, isDefault = true))
            cur = cur.plusMonths(1).withDayOfMonth(12)
        }
    }

    return tasks
}
