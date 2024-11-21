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
import org.bson.types.ObjectId

@ExtendWith(VertxExtension::class)
class CreateVerticleTest {

    private val mongoClient = mockk<MongoClient>()
    private val verticle = CreateVerticle()

    @Test
    fun testHandleCreateDocumentSuccess(testContext: VertxTestContext) {
        runBlocking {
            // Mocking the MongoClient's save method to return a Future (successful save)
            coEvery { mongoClient.save(any(), any()) } returns Future.succeededFuture("mockDocumentId")

            // Creating a mock Message<JsonObject>
            val document = JsonObject().put("name", "testDocument")
            val message = mockk<Message<JsonObject>>(relaxed = true) {
                // Mocking the message body to return the document
                coEvery { body() } returns document
            }

            // Assigning the mocked mongoClient to the verticle
            verticle.mongoClient = mongoClient

            // Calling the method to test
            verticle.handleCreateDocument(message)

            // Verifying that an ObjectId was added and the document was saved
            coVerify {
                mongoClient.save("mycollection", withArg {
                    it.getString("name") == "testDocument" &&
                            it.containsKey("_id")
                })
                message.reply(JsonObject().put("status", "success"))
            }

            // Completing the test
            testContext.completeNow()
        }
    }

}
