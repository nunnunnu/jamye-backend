package org.jy.jamye.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CorsOriginPropertiesTest {

    @Test
    fun stripsTrailingSlashAndKeepsCordovaFileOrigin() {
        val props = CorsOriginProperties(
            "https://jamye.p-e.kr/, file:// , file://*, https://localhost/, https://d39kn92x79hg54.cloudfront.net/"
        )

        assertEquals(
            listOf(
                "https://jamye.p-e.kr",
                "file://",
                "https://localhost",
                "https://d39kn92x79hg54.cloudfront.net",
            ),
            props.exactOrigins(),
        )
        assertEquals(listOf("file://*"), props.wildcardPatterns())
    }
}
