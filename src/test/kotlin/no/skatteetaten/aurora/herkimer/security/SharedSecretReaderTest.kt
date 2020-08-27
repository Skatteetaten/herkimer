package no.skatteetaten.aurora.herkimer.security

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.junit.jupiter.api.Test

class SharedSecretReaderTest {

    @Test
    fun `Init secret throw exception given null values `() {
        assertThat {
            SharedSecretReader(null, null).secret
        }.isNotNull().isFailure().isInstanceOf(IllegalArgumentException::class)
    }

    @Test
    fun `Get secret given secret value`() {
        val sharedSecretReader = SharedSecretReader(null, "abc123")
        assertThat(sharedSecretReader.secret).isEqualTo("abc123")
    }

    @Test
    fun `Get secret given secret location`() {
        val sharedSecretReader = SharedSecretReader("src/test/resources/secret.txt", null)
        assertThat(sharedSecretReader.secret).isEqualTo("secret_from_file")
    }

    @Test
    fun `Get secret given non-existing secret location throw exception`() {
        assertThat {
            SharedSecretReader("non-existing-path/secret.txt", null).secret
        }.isNotNull().isFailure().isInstanceOf(IllegalStateException::class)
    }
}
