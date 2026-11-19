package com.example.ui.i18n

import com.example.data.model.LanguageMode

data class BijliLabels(
    val mode: LanguageMode
) {
    private fun pick(en: String, hi: String, separator: String = " • "): String = when (mode) {
        LanguageMode.BILINGUAL -> "$en$separator$hi"
        LanguageMode.ENGLISH -> en
        LanguageMode.HINDI -> hi
    }

    val appTitle: String get() = "Bijli Manager"
    val appSubtitle: String get() = pick("Electricity & Billing", "बिजली एवं बिलिंग")

    // Bottom Nav
    val navOverview: String get() = when (mode) {
        LanguageMode.HINDI -> "अवलोकन"
        else -> "Overview"
    }
    val navConsumers: String get() = when (mode) {
        LanguageMode.HINDI -> "उपभोक्ता"
        else -> "Consumers"
    }
    val navBilling: String get() = when (mode) {
        LanguageMode.HINDI -> "बिलिंग"
        else -> "Billing"
    }
    val navTools: String get() = when (mode) {
        LanguageMode.HINDI -> "उपकरण"
        else -> "Tools"
    }

    // Dashboard / Overview
    val dashboardTitle: String get() = pick("Dashboard", "डैशबोर्ड")
    val monthlyConsumptionTrend: String get() = pick("Monthly Consumption Trend", "मासिक खपत रुझान")
    val totalConsumers: String get() = pick("Consumers", "उपभोक्ता")
    val pendingDues: String get() = pick("Pending Dues", "बकाया राशि")
    val collectionRate: String get() = pick("Collection", "वसूली दर")
    val totalUnitsLabel: String get() = pick("Total Units Consumed", "कुल खपत यूनिट")
    val totalBilledLabel: String get() = pick("Total Billed", "कुल बिल राशि")
    val quickFilters: String get() = pick("Quick Filters", "त्वरित फ़िल्टर")
    val priorityDues: String get() = pick("Pending & Recent Accounts", "बकाया एवं हाल के खाते")
    val quickActions: String get() = pick("Quick Actions", "त्वरित कार्य")

    // Consumers List & Search
    val searchPlaceholder: String get() = pick(
        "Search Name, ACC, K. No, Meter, Mobile...",
        "नाम, खाता, K. नंबर, मीटर या मोबाइल खोजें...",
        " / "
    )
    val addConsumer: String get() = pick("Add Consumer", "नया उपभोक्ता")
    val editConsumer: String get() = pick("Edit Consumer", "उपभोक्ता संपादित करें")
    val consumerDetails: String get() = pick("Consumer Details", "उपभोक्ता विवरण")
    val meterCalculator: String get() = pick("Quick Meter Reading & Bill Calculator", "त्वरित मीटर रीडिंग और बिल कैलकुलेटर")

    // Form Fields
    val accountNumber: String get() = pick("Account Number", "खाता संख्या")
    val kNumber: String get() = pick("K. Number", "के. नंबर (K. No)")
    val fullName: String get() = pick("Consumer Full Name", "उपभोक्ता का पूरा नाम")
    val category: String get() = pick("Category", "श्रेणी")
    val meterNumber: String get() = pick("Meter Number", "मीटर संख्या")
    val mobileNumber: String get() = pick("Mobile Number", "मोबाइल नंबर")
    val addressArea: String get() = pick("Address / Area / Feeder", "पता / क्षेत्र / फीडर")
    val connectionStatus: String get() = pick("Connection Status", "कनेक्शन स्थिति")
    val previousReading: String get() = pick("Previous Reading (kWh)", "पिछली रीडिंग (kWh)")
    val currentReading: String get() = pick("Current Reading (kWh)", "वर्तमान रीडिंग (kWh)")
    val unitsConsumed: String get() = pick("Units Consumed", "खपत यूनिट")
    val tariffRate: String get() = pick("Tariff Rate (₹/kWh)", "टैरिफ दर (₹/यूनिट)")
    val billingStatus: String get() = pick("Billing Status", "बिल भुगतान स्थिति")
    val lastBillAmount: String get() = pick("Last Bill Amount (₹)", "अंतिम बिल राशि (₹)")
    val paidAmount: String get() = pick("Paid Amount (₹)", "जमा राशि (₹)")

    // Actions
    val saveChanges: String get() = pick("Save Record", "रिकॉर्ड सहेजें")
    val markPaid: String get() = pick("Mark Paid", "भुगतान दर्ज करें")
    val markUnpaid: String get() = pick("Mark Unpaid", "बकाया चिह्नित करें")
    val callConsumer: String get() = pick("Call", "कॉल")
    val copyKNumber: String get() = pick("Copy K.No", "K.No कॉपी")
    val exportCsv: String get() = pick("Export CSV", "CSV निर्यात")
    val shareCsv: String get() = pick("Share CSV", "CSV साझा करें")
    val resetSampleData: String get() = pick("Reset to 1,000 Sample Records", "1,000 नमूना रिकॉर्ड रीसेट करें")
}
