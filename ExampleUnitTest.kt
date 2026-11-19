package com.example

import com.example.data.local.SampleConsumerGenerator
import com.example.util.CsvExporter
import com.example.util.ShareUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun `sample generator creates 1000 unique realistic consumers`() {
        val list = SampleConsumerGenerator.generate1000Consumers()
        assertEquals(1000, list.size)
        assertEquals(1000, list.map { it.id }.toSet().size)
        assertEquals(1000, list.map { it.accountNumber }.toSet().size)
        assertEquals(1000, list.map { it.kNumber }.toSet().size)
        assertTrue(list.all { it.mobileNumber.length == 10 && it.mobileNumber.all { ch -> ch.isDigit() } })
        assertTrue(list.all { it.currentReading >= it.previousReading })
    }

    @Test
    fun `csv exporter includes header and all rows`() {
        val sample = SampleConsumerGenerator.generate1000Consumers().take(5)
        val csv = CsvExporter.buildCsvContent(sample)
        val lines = csv.trim().lines()
        assertEquals(6, lines.size)
        assertTrue(lines.first().contains("Account Number,K. Number"))
        assertTrue(lines[1].contains("ACC-10001"))
    }

    @Test
    fun `single consumer full share text contains all key fields`() {
        val consumer = SampleConsumerGenerator.generate1000Consumers().first()
        val text = ShareUtils.buildSingleConsumerFullText(consumer)
        assertTrue(text.contains(consumer.name))
        assertTrue(text.contains(consumer.accountNumber))
        assertTrue(text.contains(consumer.kNumber))
        assertTrue(text.contains(consumer.meterNumber))
        assertTrue(text.contains(consumer.mobileNumber))
        assertTrue(text.contains("${consumer.unitsConsumed} kWh"))
        assertTrue(ShareUtils.buildAppShareText().contains("Bijli Manager"))
    }
}
