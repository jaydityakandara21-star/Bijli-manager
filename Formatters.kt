package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToLong

object Formatters {
    private val indianLocale = Locale("en", "IN")

    fun formatRupees(amount: Double): String {
        val formatter = NumberFormat.getNumberInstance(indianLocale).apply {
            maximumFractionDigits = 0
            minimumFractionDigits = 0
        }
        return "₹${formatter.format(amount.roundToLong())}"
    }

    fun formatRupeesCompact(amount: Double): String {
        return when {
            amount >= 1_00_00_000.0 -> String.format(indianLocale, "₹%.2f Cr", amount / 1_00_00_000.0)
            amount >= 1_00_000.0 -> String.format(indianLocale, "₹%.2f L", amount / 1_00_000.0)
            amount >= 1_000.0 -> String.format(indianLocale, "₹%.1fK", amount / 1_000.0)
            else -> formatRupees(amount)
        }
    }

    fun formatNumber(value: Number): String {
        return NumberFormat.getNumberInstance(indianLocale).format(value)
    }

    fun formatUnitsCompact(units: Long): String {
        return when {
            units >= 1_00_000L -> String.format(indianLocale, "%.1fL kWh", units / 1_00_000.0)
            units >= 1_000L -> String.format(indianLocale, "%.1fK kWh", units / 1_000.0)
            else -> "$units kWh"
        }
    }

    fun formatTimestamp(millis: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
