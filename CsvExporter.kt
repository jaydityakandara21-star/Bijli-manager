package com.example.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.ConsumerEntity
import java.io.File
import java.io.OutputStream
import java.util.Locale

object CsvExporter {
    private const val HEADER =
        "ID,Account Number,K. Number,Name,Category,Meter Number,Mobile Number,Address / Feeder,Connection Status,Previous Reading (kWh),Current Reading (kWh),Units Consumed (kWh),Tariff Rate (INR/kWh),Last Bill Amount (INR),Paid Amount (INR),Pending Due (INR),Billing Status"

    fun buildCsvContent(consumers: List<ConsumerEntity>): String {
        val sb = StringBuilder(consumers.size * 140 + 256)
        sb.appendLine(HEADER)
        consumers.forEach { c ->
            sb.append(c.id).append(',')
            sb.append(escapeCsv(c.accountNumber)).append(',')
            sb.append(escapeCsv(c.kNumber)).append(',')
            sb.append(escapeCsv(c.name)).append(',')
            sb.append(escapeCsv(c.category.englishLabel)).append(',')
            sb.append(escapeCsv(c.meterNumber)).append(',')
            sb.append(escapeCsv(c.mobileNumber)).append(',')
            sb.append(escapeCsv(c.addressArea)).append(',')
            sb.append(escapeCsv(c.connectionStatus.englishLabel)).append(',')
            sb.append(c.previousReading).append(',')
            sb.append(c.currentReading).append(',')
            sb.append(c.unitsConsumed).append(',')
            sb.append(String.format(Locale.US, "%.2f", c.tariffRate)).append(',')
            sb.append(String.format(Locale.US, "%.2f", c.lastBillAmount)).append(',')
            sb.append(String.format(Locale.US, "%.2f", c.paidAmount)).append(',')
            sb.append(String.format(Locale.US, "%.2f", c.dueAmount)).append(',')
            sb.append(escapeCsv(c.billingStatus.englishLabel))
            sb.appendLine()
        }
        return sb.toString()
    }

    fun writeToOutputStream(outputStream: OutputStream, consumers: List<ConsumerEntity>) {
        outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
            writer.write(buildCsvContent(consumers))
        }
    }

    fun createShareableCsvUri(
        context: Context,
        consumers: List<ConsumerEntity>,
        fileName: String = "bijli_consumers_export.csv"
    ): Uri {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val file = File(exportDir, safeName)
        file.writeText(buildCsvContent(consumers), Charsets.UTF_8)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun escapeCsv(value: String): String {
        val needsQuotes = value.contains(',') || value.contains('"') || value.contains('\n')
        return if (needsQuotes) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
