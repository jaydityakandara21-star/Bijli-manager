package com.example.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.ConsumerEntity
import java.util.Locale

object ShareUtils {

    fun buildSingleConsumerFullText(consumer: ConsumerEntity): String {
        val rateStr = String.format(Locale.US, "%.2f", consumer.tariffRate)
        return buildString {
            appendLine("⚡ *BIJLI MANAGER • CONSUMER RECORD*")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("👤 *Name:* ${consumer.name}")
            appendLine("🆔 *Record ID:* #${consumer.id}")
            appendLine("🏦 *Account No:* ${consumer.accountNumber}")
            appendLine("🔑 *K. Number:* ${consumer.kNumber}")
            appendLine("📟 *Meter No:* ${consumer.meterNumber}")
            appendLine("📱 *Mobile:* ${consumer.mobileNumber}")
            appendLine("🏷️ *Category:* ${consumer.category.englishLabel} (${consumer.category.hindiLabel})")
            appendLine("🔌 *Connection:* ${consumer.connectionStatus.englishLabel} (${consumer.connectionStatus.hindiLabel})")
            appendLine("📍 *Area / Feeder:* ${consumer.addressArea}")
            appendLine("━━━━━━━━━━━━━━━━━━━━")
            appendLine("📊 *METER & BILLING DETAILS*")
            appendLine("• Previous Reading: ${consumer.previousReading} kWh")
            appendLine("• Current Reading: ${consumer.currentReading} kWh")
            appendLine("• Units Consumed: ${consumer.unitsConsumed} kWh")
            appendLine("• Tariff Rate: ₹$rateStr / kWh")
            appendLine("• Last Bill Amount: ${Formatters.formatRupees(consumer.lastBillAmount)}")
            appendLine("• Paid Amount: ${Formatters.formatRupees(consumer.paidAmount)}")
            appendLine("• Pending Due: ${Formatters.formatRupees(consumer.dueAmount)}")
            appendLine("• Billing Status: ${consumer.billingStatus.englishLabel} (${consumer.billingStatus.hindiLabel})")
            appendLine("🕒 *Updated:* ${Formatters.formatTimestamp(consumer.lastUpdatedMillis)}")
        }.trim()
    }

    fun buildAppShareText(): String {
        return buildString {
            appendLine("⚡ *Bijli Manager (बिजली प्रबंधक)*")
            appendLine("Electricity Consumer, Meter Reading & Billing Manager App.")
            appendLine()
            appendLine("✅ Manage 1,000+ consumer accounts (K. No, Account No, Meter No)")
            appendLine("✅ Instant search, category/billing filters & quick meter bill calculator")
            appendLine("✅ Bilingual English & Hindi support with CSV export & WhatsApp sharing")
            appendLine()
            append("Try Bijli Manager today!")
        }
    }

    fun shareTextOnWhatsApp(
        context: Context,
        text: String,
        chooserTitle: String
    ) {
        val whatsappIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
            setPackage("com.whatsapp")
        }
        try {
            context.startActivity(whatsappIntent)
        } catch (_: ActivityNotFoundException) {
            val businessIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                setPackage("com.whatsapp.w4b")
            }
            try {
                context.startActivity(businessIntent)
            } catch (_: ActivityNotFoundException) {
                val fallback = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
                context.startActivity(Intent.createChooser(fallback, chooserTitle))
            }
        }
    }

    fun sendToConsumerWhatsApp(
        context: Context,
        mobileNumber: String,
        text: String
    ) {
        val digits = mobileNumber.filter { it.isDigit() }.takeLast(10)
        val intlNumber = if (digits.length == 10) "91$digits" else digits
        val uri = Uri.parse("https://wa.me/$intlNumber?text=${Uri.encode(text)}")
        val viewIntent = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(viewIntent)
        } catch (_: ActivityNotFoundException) {
            shareTextOnWhatsApp(context, text, "Send via WhatsApp")
        }
    }

    fun shareFileOnWhatsAppOrChooser(
        context: Context,
        uri: Uri,
        mimeType: String,
        subject: String,
        caption: String,
        preferWhatsApp: Boolean = true
    ) {
        if (preferWhatsApp) {
            val waIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                setPackage("com.whatsapp")
            }
            try {
                context.startActivity(waIntent)
                return
            } catch (_: ActivityNotFoundException) {
                // Fall through to system chooser
            }
        }
        val chooserIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, caption)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(chooserIntent, subject))
    }
}
