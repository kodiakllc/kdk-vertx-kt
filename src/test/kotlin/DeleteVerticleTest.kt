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
class DeleteVerticleTest {

    private val mongoClient = mockk<MongoClient>()
    private val verticle = DeleteVerticle()

    @Test
    fun testHandleDeleteDocumentSuccess(testContext: VertxTestContext) {
        runBlocking {
            // Mocking the MongoClient's findOneAndDelete method to return a deleted document (successful delete)
            val mockDeletedDocument = JsonObject().put("name", "deletedDocument")
            coEvery { mongoClient.findOneAndDelete(any(), any()) } returns Future.succeededFuture(mockDeletedDocument)

            // Creating a mock Message<JsonObject>
            val deleteBody = JsonObject().put("id", ObjectId().toHexString())
            val message = mockk<Message<JsonObject>>(relaxed = true) {
                coEvery { body() } returns deleteBody
            }
            verticle.mongoClient = mongoClient
            verticle.handleDeleteDocument(message)

            // Verifying that the correct query was used and the message.reply was called
            coVerify {
                mongoClient.findOneAndDelete("mycollection", withArg {
                    it.getJsonObject("_id").getString("\$oid") == deleteBody.getString("id")
                })
                message.reply(mockDeletedDocument)
            }

            testContext.completeNow()
        }
    }

    @Test
    fun testHandleDeleteDocumentNotFound(testContext: VertxTestContext) {
        runBlocking {
            // Mocking the MongoClient's findOneAndDelete method to return null (document not found)
            coEvery { mongoClient.findOneAndDelete(any(), any()) } returns Future.succeededFuture(null)

            // Creating a mock Message<JsonObject>
            val deleteBody = JsonObject().put("id", ObjectId().toHexString())
            val message = mockk<Message<JsonObject>>(relaxed = true) {
                coEvery { body() } returns deleteBody
            }
            verticle.mongoClient = mongoClient
            verticle.handleDeleteDocument(message)

            // Verifying that message.fail was called with the proper error message
            coVerify {
                message.fail(404, "Document not found")
            }

            testContext.completeNow()
        }
    }
}
