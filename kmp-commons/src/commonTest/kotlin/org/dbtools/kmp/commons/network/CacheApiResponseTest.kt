package org.dbtools.kmp.commons.network

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import assertk.assertions.isTrue
import assertk.fail
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.resources.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import org.dbtools.kmp.commons.ext.executeSafelyCached
import kotlin.test.Test

class CacheApiResponseTest {

    @Test
    fun `test CacheApiResponse Success`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"success"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode.OK,
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Success::class)
        assertThat(response.getOrNull()).isEqualTo(DtoTest("success"))
        assertThat(response.getOrThrow()).isEqualTo(DtoTest("success"))
        assertThat(response.messageOrNull).isNull()
        assertThat(response.isSuccess).isTrue()
        assertThat(response.isFailure).isFalse()
        assertThat(response.isError).isFalse()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            assertThat(data).isEqualTo(DtoTest("success"))
        }.onFailure {
            fail("Should not be called")
        }.onError {
            fail("Should not be called")
        }.onException {
            fail("Should not be called")
        }
    }

    @Test
    fun `test CacheApiResponse Error Forbidden`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"forbidden"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode.Forbidden,
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Error.Forbidden::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class)
            assertThat(e.message).isEqualTo("Error executing service call: GET http://localhost/test (403 Forbidden)")
        }
        assertThat(response.messageOrNull).isEqualTo("Error executing service call: GET http://localhost/test (403 Forbidden)")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isTrue()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("Error executing service call: GET http://localhost/test (403 Forbidden)")
        }.onError {
            assertThat(message).isEqualTo("Error executing service call: GET http://localhost/test (403 Forbidden)")
        }.onException {
            fail("Should not be called")
        }

        assertThat(response.getOrElse(DtoTest("default"))).isEqualTo(DtoTest("default"))
        assertThat(response.getOrElse { DtoTest("default") }).isEqualTo(DtoTest("default"))
    }

    @Test
    fun `test CacheApiResponse Error NoToken`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"no token"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode(480, "No Token"),
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Error.NoToken::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class)
            assertThat(e.message).isEqualTo("Error executing service call: GET http://localhost/test (480 No Token)")
        }
        assertThat(response.messageOrNull).isEqualTo("Error executing service call: GET http://localhost/test (480 No Token)")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isTrue()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("Error executing service call: GET http://localhost/test (480 No Token)")
        }.onError {
            assertThat(message).isEqualTo("Error executing service call: GET http://localhost/test (480 No Token)")
        }.onException {
            fail("Should not be called")
        }
    }

    @Test
    fun `test CacheApiResponse Error Request No Details`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"error"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode.BadRequest,
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Error.Client::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class)
            assertThat(e.message).isEqualTo("Error executing service call: GET http://localhost/test (400 Bad Request)")
        }
        assertThat(response.messageOrNull).isEqualTo("Error executing service call: GET http://localhost/test (400 Bad Request)")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isTrue()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("Error executing service call: GET http://localhost/test (400 Bad Request)")
        }.onError {
            assertThat(message).isEqualTo("Error executing service call: GET http://localhost/test (400 Bad Request)")
        }.onException {
            fail("Should not be called")
        }
    }

    @Test
    fun `test CacheApiResponse Error Request With Details`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"error"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode.BadRequest,
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out DtoTest> = client.executeSafelyCached(
            apiCall = { get(TestResource) },
            mapClientError = { CacheApiResponse.Failure.Error.Client(it.body<DtoTest>()) },
            mapSuccess = { it.body() },
        )

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Error.Client::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class)
            assertThat(e.message).isEqualTo("DtoTest(status=error)")
        }
        assertThat(response.messageOrNull).isEqualTo("DtoTest(status=error)")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isTrue()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("DtoTest(status=error)")
        }.onError {
            assertThat(message).isEqualTo("DtoTest(status=error)")
        }.onException {
            fail("Should not be called")
        }

        assertThat((response as CacheApiResponse.Failure.Error.Client).details).isEqualTo(DtoTest("error"))
    }

    @Test
    fun `test CacheApiResponse Error Server`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"error"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode.InternalServerError,
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Error.Server::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class)
            assertThat(e.message).isEqualTo("Error executing service call: GET http://localhost/test (500 Internal Server Error)")
        }
        assertThat(response.messageOrNull).isEqualTo("Error executing service call: GET http://localhost/test (500 Internal Server Error)")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isTrue()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("Error executing service call: GET http://localhost/test (500 Internal Server Error)")
        }.onError {
            assertThat(message).isEqualTo("Error executing service call: GET http://localhost/test (500 Internal Server Error)")
        }.onException {
            fail("Should not be called")
        }
    }

    @Test
    fun `test CacheApiResponse Error Unknown`() = runTest {
        val mockEngine = MockEngine {
            respond(
                content = """{"status":"error"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
                status = HttpStatusCode(600, "Unknown"),
            )
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Error.Unknown::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(RuntimeException::class)
            assertThat(e.message).isEqualTo("Error executing service call: GET http://localhost/test (600 Unknown)")
        }
        assertThat(response.messageOrNull).isEqualTo("Error executing service call: GET http://localhost/test (600 Unknown)")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isTrue()
        assertThat(response.isException).isFalse()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("Error executing service call: GET http://localhost/test (600 Unknown)")
        }.onError {
            assertThat(message).isEqualTo("Error executing service call: GET http://localhost/test (600 Unknown)")
        }.onException {
            fail("Should not be called")
        }
    }

    @Test
    fun `test CacheApiResponse Exception`() = runTest {
        val mockEngine = MockEngine {
            error("Test exception")
        }
        val client = TestHttpClientProvider.getTestClient(mockEngine)

        val response: CacheApiResponse<out DtoTest, out Unit> = client.executeSafelyCached({ get(TestResource) }) { it.body() }

        assertThat(response).isInstanceOf(CacheApiResponse.Failure.Exception::class)
        assertThat(response.getOrNull()).isNull()
        try {
            assertThat(response.getOrThrow())
            fail("Should throw exception")
        } catch (e: Exception) {
            assertThat(e).isInstanceOf(IllegalStateException::class)
            assertThat(e.message).isEqualTo("Test exception")
        }
        assertThat(response.messageOrNull).isEqualTo("Test exception")
        assertThat(response.isSuccess).isFalse()
        assertThat(response.isFailure).isTrue()
        assertThat(response.isError).isFalse()
        assertThat(response.isException).isTrue()

        response.onSuccess {
            fail("Should not be called")
        }.onFailure {
            assertThat(message()).isEqualTo("Test exception")
        }.onError {
            fail("Should not be called")
        }.onException {
            assertThat(throwable).isInstanceOf(Exception::class)
            assertThat(throwable.message).isEqualTo("Test exception")
        }

    }
}