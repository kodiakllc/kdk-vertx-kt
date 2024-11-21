import io.vertx.core.Future
import io.vertx.ext.mongo.MongoClient
import io.vertx.core.json.JsonObject
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.vertx.core.eventbus.Message
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class ReadVerticleTest {

    private val mongoClient = mockk<MongoClient>()
    private val verticle = ReadVerticle()

    @Test
    fun testHandleGetDocument(testContext: VertxTestContext) {
        runBlocking {
            val mockDocument = JsonObject().put("name", "testDocument")
            coEvery { mongoClient.findOne(any(), any(), any()) } returns Future.succeededFuture(mockDocument)
            val message = mockk<Message<JsonObject>>(relaxed = true)
            verticle.mongoClient = mongoClient
            verticle.handleGetDocument(message)
            coVerify { message.reply(mockDocument) }
            testContext.completeNow()
        }
    }


    @Test
    fun testHandleListDocuments(testContext: VertxTestContext) {
        runBlocking {
            val mockDocuments = listOf(JsonObject().put("name", "doc1"), JsonObject().put("name", "doc2"))
            coEvery { mongoClient.find(any(), any()) } returns Future.succeededFuture(mockDocuments)
            val message = mockk<Message<JsonObject>>(relaxed = true)
            verticle.mongoClient = mongoClient
            verticle.handleListDocuments(message)
            coVerify { message.reply(JsonObject().put("documents", mockDocuments)) }
            testContext.completeNow()
        }
    }


    @Test
    fun testHandleGetDbMap(testContext: VertxTestContext) {
        runBlocking {
            // Mocking the MongoClient's getCollections method to return a list of collection names
            val mockCollections = listOf("collection1", "collection2")
            coEvery { mongoClient.getCollections() } returns Future.succeededFuture(mockCollections)

            // Mocking the MongoClient's find method to return documents for each collection
            val mockCollection1Docs = listOf(JsonObject().put("name", "doc1"))
            val mockCollection2Docs = listOf(JsonObject().put("name", "doc2"))
            coEvery { mongoClient.find("collection1", any()) } returns Future.succeededFuture(mockCollection1Docs)
            coEvery { mongoClient.find("collection2", any()) } returns Future.succeededFuture(mockCollection2Docs)

            val message = mockk<Message<String>>(relaxed = true)
            verticle.mongoClient = mongoClient
            verticle.handleGetDbMap(message)

            // Construct the expected response
            val expectedResponse = JsonObject().put("collections", listOf(
                JsonObject().put("collection", "collection1").put("documents", mockCollection1Docs),
                JsonObject().put("collection", "collection2").put("documents", mockCollection2Docs)
            ))

            coVerify { message.reply(expectedResponse) }
            testContext.completeNow()
        }
    }
}
