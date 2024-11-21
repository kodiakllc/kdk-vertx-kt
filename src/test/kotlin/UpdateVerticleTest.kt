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
class UpdateVerticleTest {

    private val mongoClient = mockk<MongoClient>()
    private val verticle = UpdateVerticle()

    @Test
    fun testHandleUpdateDocumentSuccess(testContext: VertxTestContext) {
        runBlocking {
            // Mocking the MongoClient's findOneAndUpdate method to return a document (successful update)
            val mockUpdatedDocument = JsonObject().put("name", "updatedDocument")
            coEvery { mongoClient.findOneAndUpdate(any(), any(), any()) } returns Future.succeededFuture(mockUpdatedDocument)

            val updateBody = JsonObject().put("id", ObjectId().toHexString()).put("update", JsonObject().put("name", "newName"))
            val message = mockk<Message<JsonObject>>(relaxed = true) {
                coEvery { body() } returns updateBody
            }

            verticle.mongoClient = mongoClient
            verticle.handleUpdateDocument(message)
            coVerify {
                mongoClient.findOneAndUpdate("mycollection", withArg {
                    it.getJsonObject("_id").getString("\$oid") == updateBody.getString("id")
                }, JsonObject().put("\$set", updateBody.getJsonObject("update")))
                message.reply(mockUpdatedDocument)
            }

            testContext.completeNow()
        }
    }

    @Test
    fun testHandleUpdateDocumentNotFound(testContext: VertxTestContext) {
        runBlocking {
            // Mocking the MongoClient's findOneAndUpdate method to return null (document not found)
            coEvery { mongoClient.findOneAndUpdate(any(), any(), any()) } returns Future.succeededFuture(null)

            val updateBody = JsonObject().put("id", ObjectId().toHexString()).put("update", JsonObject().put("name", "newName"))
            val message = mockk<Message<JsonObject>>(relaxed = true) {
                coEvery { body() } returns updateBody
            }

            verticle.mongoClient = mongoClient
            verticle.handleUpdateDocument(message)
            coVerify {
                message.fail(404, "Document not found")
            }
            
            testContext.completeNow()
        }
    }
}
