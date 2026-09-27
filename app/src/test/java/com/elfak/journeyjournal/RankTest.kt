package com.elfak.journeyjournal

import com.elfak.journeyjournal.data.model.Rank
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RankTest {

    @Test
    fun `rank grows with points`() {
        assertEquals(Rank.POCETNIK, Rank.of(0))
        assertEquals(Rank.POCETNIK, Rank.of(49))
        assertEquals(Rank.PUTNIK, Rank.of(50))
        assertEquals(Rank.ISTRAZIVAC, Rank.of(150))
        assertEquals(Rank.AVANTURISTA, Rank.of(299 + 1))
        assertEquals(Rank.LEGENDA, Rank.of(10_000))
    }

    @Test
    fun `points to next rank`() {
        assertEquals(50L, Rank.pointsToNext(0))
        assertEquals(1L, Rank.pointsToNext(149))
        assertNull(Rank.pointsToNext(600))
    }
}
