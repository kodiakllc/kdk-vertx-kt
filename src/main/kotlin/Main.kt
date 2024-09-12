import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.DeploymentOptions

fun main() {
    val vertx = Vertx.vertx()
    deployVerticles(vertx)
}

fun deployVerticles(vertx: Vertx) {
    val store = ConfigStoreOptions()
        .setType("file")
        .setFormat("json")
        .setConfig(JsonObject().put("path", "config.json"))

    val retriever = ConfigRetriever.create(vertx, ConfigRetrieverOptions().addStore(store))

    retriever.getConfig { ar ->
        if (ar.failed()) {
            println("Failed to load configuration: ${ar.cause()}")
        } else {
            val config = ar.result()
            println("Loaded configuration: $config")
            val options = DeploymentOptions().setConfig(config)
            deployAllVerticles(vertx, options)
        }
    }
}

fun deployAllVerticles(vertx: Vertx, options: DeploymentOptions) {
    vertx.deployVerticle(CentralVerticle::class.java.name, options) { res ->
        if (res.succeeded()) {
            println("CentralVerticle deployed successfully.")
            vertx.deployVerticle(CreateVerticle::class.java.name, options) { res ->
                if (res.succeeded()) {
                    println("CreateVerticle deployed successfully.")
                } else {
                    println("Failed to deploy CreateVerticle: ${res.cause()}")
                }
            }
            vertx.deployVerticle(ReadVerticle::class.java.name, options) { res ->
                if (res.succeeded()) {
                    println("ReadVerticle deployed successfully.")
                } else {
                    println("Failed to deploy ReadVerticle: ${res.cause()}")
                }
            }
            vertx.deployVerticle(UpdateVerticle::class.java.name, options) { res ->
                if (res.succeeded()) {
                    println("UpdateVerticle deployed successfully.")
                } else {
                    println("Failed to deploy UpdateVerticle: ${res.cause()}")
                }
            }
            vertx.deployVerticle(DeleteVerticle::class.java.name, options) { res ->
                if (res.succeeded()) {
                    println("DeleteVerticle deployed successfully.")
                } else {
                    println("Failed to deploy DeleteVerticle: ${res.cause()}")
                }
            }
        } else {
            println("Failed to deploy CentralVerticle: ${res.cause()}")
        }
    }
}