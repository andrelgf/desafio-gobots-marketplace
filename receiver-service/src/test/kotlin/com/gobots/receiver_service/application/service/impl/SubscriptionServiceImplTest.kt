package com.gobots.receiver_service.application.service.impl

import com.gobots.receiver_service.application.exception.InvalidSubscriptionRequestException
import com.gobots.receiver_service.domain.model.Subscription
import com.gobots.receiver_service.domain.repository.SubscriptionRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SubscriptionServiceImplTest {
    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val service = SubscriptionServiceImpl(subscriptionRepository)

    @Test
    fun subscribe_normalizesAndSavesOnlyNewStoreCodes() {
        val captured = slot<Collection<String>>()
        every { subscriptionRepository.findAllByStoreCodeIn(capture(captured)) } returns
            listOf(Subscription("STORE_001"))
        every { subscriptionRepository.saveAll(any<List<Subscription>>()) } answers { firstArg() }

        val result = service.subscribe(listOf(" store_001 ", "store_002", "STORE_002", "store_001"))

        assertEquals(1, result.size)
        assertEquals("STORE_002", result.first().storeCode)
        assertTrue(captured.captured.containsAll(listOf("STORE_001", "STORE_002")))
        verify(exactly = 1) {
            subscriptionRepository.saveAll(match<List<Subscription>> {
                it.size == 1 && it.first().storeCode == "STORE_002"
            })
        }
    }

    @Test
    fun subscribe_whenAllExisting_returnsEmptyList() {
        every { subscriptionRepository.findAllByStoreCodeIn(any()) } returns
            listOf(Subscription("STORE_001"))

        val result = service.subscribe(listOf("store_001"))

        assertTrue(result.isEmpty())
        verify(exactly = 0) { subscriptionRepository.saveAll(any<List<Subscription>>()) }
    }

    @Test
    fun subscribe_whenBlankOnly_throwsInvalidSubscriptionRequestException() {
        assertThrows<InvalidSubscriptionRequestException> {
            service.subscribe(listOf(" ", ""))
        }

        verify(exactly = 0) { subscriptionRepository.findAllByStoreCodeIn(any()) }
    }
}
