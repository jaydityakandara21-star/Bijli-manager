package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.BijliDatabase
import com.example.data.local.SampleConsumerGenerator
import com.example.data.repository.ConsumerRepository
import com.example.data.repository.SaveConsumerResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var database: BijliDatabase
    private lateinit var repository: ConsumerRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, BijliDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = ConsumerRepository(database.consumerDao())
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Bijli Manager", appName)
    }

    @Test
    fun `seed 1000 consumers and enforce unique account and k numbers`() = runTest {
        repository.ensureSeeded()
        val all = repository.allConsumers.first()
        assertEquals(SampleConsumerGenerator.SAMPLE_COUNT, all.size)
        assertEquals(1, all.first().id)
        assertEquals(1000, all.last().id)

        val duplicateResult = repository.saveConsumer(
            all.first().copy(id = 0, kNumber = "K-9999999")
        )
        assertTrue(duplicateResult is SaveConsumerResult.ValidationError)
    }
}
