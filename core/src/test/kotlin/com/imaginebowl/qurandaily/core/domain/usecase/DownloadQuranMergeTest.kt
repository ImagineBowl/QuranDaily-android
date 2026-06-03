package com.imaginebowl.qurandaily.core.domain.usecase

import com.imaginebowl.qurandaily.core.data.repository.DefaultQuranRepository
import com.imaginebowl.qurandaily.core.fake.FakeApiClient
import com.imaginebowl.qurandaily.core.fake.FakeStorageService
import com.imaginebowl.qurandaily.core.fake.TestFixtures
import org.junit.Assert.assertEquals
import org.junit.Test

class DownloadQuranMergeTest {
    @Test
    fun mergeQuranData_mergesArabicUrduAndMeta() {
        val useCase = DownloadQuranUseCase(
            apiClient = FakeApiClient(),
            quranRepository = DefaultQuranRepository(FakeStorageService()),
        )
        val merged = useCase.mergeQuranData(
            arabic = TestFixtures.makeArabicResponse(),
            urdu = TestFixtures.makeUrduResponse(),
            meta = TestFixtures.makeMetaResponse(),
        )

        assertEquals(1, merged.surahs.size)
        assertEquals(TestFixtures.ayah1.urduText, merged.ayahsBySurah[1]?.first()?.urduText)
        assertEquals(1, merged.juzs.size)
    }
}
