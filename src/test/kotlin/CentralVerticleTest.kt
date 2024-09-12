import io.mockk.every
import io.mockk.mockk
import io.vertx.core.DeploymentOptions
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.client.WebClient
import io.vertx.ext.web.client.WebClientOptions
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

@ExtendWith(VertxExtension::class)
class CentralVerticleTest {

    private lateinit var vertx: Vertx
    private lateinit var client: WebClient

    private val config = json {
        obj(
            "http" to obj(
                "port" to 8080
            ),
            "eventBus" to obj(
                "createDocumentAddress" to "create.document",
                "getDocumentAddress" to "get.document",
                "listDocumentsAddress" to "list.documents",
                "updateDocumentAddress" to "update.document",
                "deleteDocumentAddress" to "delete.document",
                "getDbMapAddress" to "get.db.map"
            )
        )
    }

    @BeforeEach
    fun setup(testContext: VertxTestContext) {
        vertx = Vertx.vertx()
        client = WebClient.create(vertx, WebClientOptions().setDefaultPort(8080).setDefaultHost("localhost"))
        val options = DeploymentOptions().setConfig(config)
        vertx.deployVerticle(CentralVerticle(), options, testContext.succeeding { _ -> testContext.completeNow() })
    }

    @AfterEach
    fun tearDown(testContext: VertxTestContext) {
        vertx.close(testContext.succeeding { testContext.completeNow() })
    }

    @Test
    fun `test all routes`(testContext: VertxTestContext) = runBlocking {
        val addresses = listOf(
            Triple("POST", "/set", "create.document"),
            Triple("GET", "/document", "get.document"),
            Triple("GET", "/documents", "list.documents"),
            Triple("PUT", "/update", "update.document"),
            Triple("DELETE", "/delete", "delete.document"),
            Triple("GET", "/", "get.db.map")
        )

        val jsonObject = json { obj("key" to "value") }

        val eventBusMock = mockk<io.vertx.core.eventbus.EventBus>()
        val messageMock = mockk<Message<JsonObject>>()
        every { messageMock.body() } returns JsonObject().put("key", "value")
        every { eventBusMock.request<JsonObject>(any(), any()) } returns Future.succeededFuture(messageMock)

        addresses.forEach { (method, route, address) ->
            when (method) {
                "POST" -> client.post(route).sendJsonObject(jsonObject).onComplete(testContext.succeeding { response ->
                    testContext.verify {
                        assertEquals(200, response.statusCode())
                        assertTrue(response.bodyAsJsonObject().containsKey("key"))
                    }
                })
                "GET" -> client.get(route).send().onComplete(testContext.succeeding { response ->
                    testContext.verify {
                        assertEquals(200, response.statusCode())
                        assertTrue(response.bodyAsJsonObject().containsKey("key"))
                    }
                })
                "PUT" -> client.put(route).sendJsonObject(jsonObject).onComplete(testContext.succeeding { response ->
                    testContext.verify {
                        assertEquals(200, response.statusCode())
                        assertTrue(response.bodyAsJsonObject().containsKey("key"))
                    }
                })
                "DELETE" -> client.delete(route).sendJsonObject(jsonObject).onComplete(testContext.succeeding { response ->
                    testContext.verify {
                        assertEquals(200, response.statusCode())
                        assertTrue(response.bodyAsJsonObject().containsKey("key"))
                    }
                })
            }
        }

        client.request(io.vertx.core.http.HttpMethod.OPTIONS, "/").send().onComplete(testContext.succeeding { response ->
            testContext.verify {
                assertEquals(204, response.statusCode())
                assertTrue(response.getHeader("Access-Control-Allow-Origin") == "*")
                assertTrue(response.getHeader("Access-Control-Allow-Methods")!!.contains("GET"))
                assertTrue(response.getHeader("Access-Control-Allow-Methods")!!.contains("POST"))
                assertTrue(response.getHeader("Access-Control-Allow-Methods")!!.contains("DELETE"))
                assertTrue(response.getHeader("Access-Control-Allow-Methods")!!.contains("PUT"))
            }
        })

        testContext.completeNow()
    }
}