package com.deenjourney.app.feature.tools

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ZakatTest {
    @Test fun eligibilityRequiresHawlAndNisab() {
        assertFalse(estimateZakat(100_000, 0, 50_000, false).eligible)
        assertFalse(estimateZakat(49_999, 0, 50_000, true).eligible)
        assertTrue(estimateZakat(50_000, 0, 50_000, true).eligible)
        assertFalse(estimateZakat(100_000, 0, 0, true).eligible)
    }
    @Test fun eligibleWealthIsChargedInFullAfterDebts() {
        val result = estimateZakat(1_000_000, 200_000, 500_000, true)
        assertEquals(800_000, result.net)
        assertEquals(20_000, result.due)
    }
    @Test fun debtCannotProduceNegativeWealthOrTax() {
        assertEquals(ZakatEstimate(0, false, 0), estimateZakat(100, 200, 50, true))
        assertEquals(0, estimateZakat(-100, 0, 50, true).due)
    }
    @Test fun payableAmountRoundsToMinorCurrencyUnits() {
        assertEquals(3, estimateZakat(100, 0, 1, true).due)
    }
}
