import io.mockk.*
import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Future
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.Assertions.assertEquals

@ExtendWith(VertxExtension::class)
class MainTest {

    private lateinit var vertx: Vertx

    @BeforeEach
    fun setup() {
        vertx = spyk(Vertx.vertx())
    }

    @AfterEach
    fun tearDown(testContext: VertxTestContext) {
        vertx.close(testContext.succeeding { testContext.completeNow() })
    }

    @Test
    fun testConfigurationAndVerticleDeployment(testContext: VertxTestContext) = runBlocking {
        val mockConfig = JsonObject().put("key", "value")

        // Mock the ConfigRetriever behavior
        mockkStatic(ConfigRetriever::class)
        val mockRetriever = mockk<ConfigRetriever>()
        every { ConfigRetriever.create(any(), any<ConfigRetrieverOptions>()) } returns mockRetriever
        every { mockRetriever.getConfig(any()) } answers {
            val handler = it.invocation.args[0] as Handler<AsyncResult<JsonObject>>
            handler.handle(Future.succeededFuture(mockConfig))
        }

        // Spy on the deployment of verticles
        val optionsSlot = slot<DeploymentOptions>()
        every { vertx.deployVerticle(any<String>(), capture(optionsSlot), any()) } answers {
            val handler = it.invocation.args[2] as Handler<AsyncResult<String>>
            handler.handle(Future.succeededFuture("deploymentID"))
            Future.succeededFuture("deploymentID")
        }

        // Call the deployment method directly
        deployVerticles(vertx)

        // Verify deployments
        verify(exactly = 1) {
            vertx.deployVerticle(CentralVerticle::class.java.name, any(), any())
        }
        verify(exactly = 1) {
            vertx.deployVerticle(CreateVerticle::class.java.name, any(), any())
        }
        verify(exactly = 1) {
            vertx.deployVerticle(ReadVerticle::class.java.name, any(), any())
        }
        verify(exactly = 1) {
            vertx.deployVerticle(UpdateVerticle::class.java.name, any(), any())
        }
        verify(exactly = 1) {
            vertx.deployVerticle(DeleteVerticle::class.java.name, any(), any())
        }

        // Verify the configuration content
        assertEquals(mockConfig, optionsSlot.captured.config)

        testContext.completeNow()
    }
}