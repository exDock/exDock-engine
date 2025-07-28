package com.ex_dock.ex_dock.database.server

import com.ex_dock.ex_dock.database.connection.getConnection
import com.ex_dock.ex_dock.frontend.cache.setCacheFlag
import io.vertx.core.Future
import io.vertx.core.VerticleBase
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.eventbus.Message
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.Row
import io.vertx.sqlclient.RowSet
import io.vertx.sqlclient.Tuple

class ServerJDBCVerticle: VerticleBase() {
  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val failedMessage: String = "failed"
  private val serverDataDataDeliveryOptions = DeliveryOptions().setCodecName("ServerDataDataCodec")
  private val serverVersionDataDeliveryOptions = DeliveryOptions().setCodecName("ServerVersionDataCodec")
  private val listDeliveryOptions = DeliveryOptions().setCodecName("ListCodec")

  companion object {
    private const val CACHE_ADDRESS_DATA = "server_data"
    private const val CACHE_ADDRESS_VERSION = "server_version"
  }

  override fun start(): Future<*>? {
    client = vertx.getConnection()
    eventBus = vertx.eventBus()

    // Initialize all eventbus connections with the Server Data table
    getAllServerData()
    getServerDataByKey()
    createServerData()
    updateServerData()
    deleteServerData()

    // Initialize all eventbus connections with the Server Version table
    getAllServerVersions()
    getServerVersionByKey()
    createServerVersion()
    updateServerVersion()
    deleteServerVersion()

    return Future.succeededFuture<Unit>()
  }

  /**
   * Get all server data from the database
   */
  private fun getAllServerData() {
    val getAllServerDataConsumer = eventBus.consumer<String>("process.server.getAllServerData")
    getAllServerDataConsumer.handler { message ->
      val query = "SELECT * FROM server_data"
      val rowsFuture = client.preparedQuery(query).execute()

      answerServerDataMessage(rowsFuture, message)
    }
  }

  /**
   * Get all server data from the database by key
   */
  private fun getServerDataByKey() {
    val getServerDataByKeyConsumer = eventBus.localConsumer<String>("process.server.getServerByKey")
    getServerDataByKeyConsumer.handler { message ->
      val key = message.body().toString()
      val query = "SELECT * FROM server_data WHERE key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(key))

      answerServerDataMessage(rowsFuture, message)
    }
  }

  /**
   * Create a new server data entry in the database
   */
  private fun createServerData() {
    val createServerDataConsumer = eventBus.localConsumer<ServerDataData>("process.server.createServerData")
    createServerDataConsumer.handler { message ->
      val serverData = message.body()
      val query = "INSERT INTO server_data (key, value) VALUES (?,?)"
      val rowsFuture = client.preparedQuery(query).execute(serverData.toTuple(false))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS_DATA)
        message.reply(serverData, serverDataDataDeliveryOptions)
      }
    }
  }

  /**
   * Update an existing server data entry in the database
   */
  private fun updateServerData() {
    val updateServerDataConsumer = eventBus.localConsumer<ServerDataData>("process.server.updateServerData")
    updateServerDataConsumer.handler { message ->
      val serverData = message.body()
      val query = "UPDATE server_data SET value =? WHERE key =?"
      val rowsFuture = client.preparedQuery(query).execute(serverData.toTuple(true))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS_DATA)
        message.reply(serverData, serverDataDataDeliveryOptions)
      }
    }
  }

  /**
   * Delete a server data entry from the database
   */
  private fun deleteServerData() {
    val deleteServerDataConsumer = eventBus.localConsumer<String>("process.server.deleteServerData")
    deleteServerDataConsumer.handler { message ->
      val key = message.body().toString()
      val query = "DELETE FROM server_data WHERE key =?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(key))

      rowsFuture.onFailure{ res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS_DATA)
        message.reply("Server data deleted successfully!")
      }
    }
  }

  /**
   * Get all server versions from the database
   */
  private fun getAllServerVersions() {
    val getAllServerVersionsConsumer = eventBus.localConsumer<String>("process.server.getAllServerVersions")
    getAllServerVersionsConsumer.handler { message ->
      val query = "SELECT * FROM server_version"
      val rowsFuture = client.preparedQuery(query).execute()
      val allServerVersionList: MutableList<ServerVersionData> = emptyList<ServerVersionData>().toMutableList()

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          rows.forEach { row ->
            allServerVersionList.add(row.makeServerVersion())
          }
        }

        message.reply(allServerVersionList, listDeliveryOptions)
      }
    }
  }

  /**
   * Get a server version from the database by major, minor, and patch
   */
  private fun getServerVersionByKey() {
    val getServerVersionByKeyConsumer = eventBus.localConsumer<ServerVersionData>("process.server.getServerVersionByKey")
    getServerVersionByKeyConsumer.handler { message ->
      val key = message.body()
      val query = "SELECT * FROM server_version WHERE major = ? AND minor = ? AND patch = ?"
      val rowsFuture = client.preparedQuery(query).execute(Tuple.of(
        key.major, key.minor, key.patch
      ))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }.onComplete { res ->
        val rows = res.result()
        if (rows.size() > 0) {
          message.reply(rows.first().makeServerVersion(), serverDataDataDeliveryOptions)
        } else {
          message.reply("No server version found!")
        }
      }
    }
  }

  /**
   * Create a new server version entry in the database
   */
  private fun createServerVersion() {
    val createServerVersionConsumer = eventBus.localConsumer<ServerVersionData>("process.server.createServerVersion")
    createServerVersionConsumer.handler { message ->
      val serverVersion = message.body()
      val query = "INSERT INTO server_version (major, minor, patch, version_name, version_description) VALUES (?,?,?,?,?)"
      val rowsFuture = client.preparedQuery(query).execute(serverVersion.toTuple(false))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS_VERSION)
        message.reply(serverVersion, serverVersionDataDeliveryOptions)
      }
    }
  }

  /**
   * Update an existing server version entry in the database
   */
  private fun updateServerVersion() {
    val updateServerVersionConsumer = eventBus.localConsumer<ServerVersionData>("process.server.updateServerVersion")
    updateServerVersionConsumer.handler { message ->
      val serverVersion = message.body()
      val query = "UPDATE server_version SET major =?, minor =?, patch =?, version_name =?, version_description =? WHERE major = ? AND minor = ? AND patch = ?"
      val rowsFuture = client.preparedQuery(query).execute(serverVersion.toTuple(true))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS_VERSION)
        message.reply(serverVersion, serverDataDataDeliveryOptions)
      }
    }
  }

  /**
   * Delete a server version entry from the database
   */
  private fun deleteServerVersion() {
    val deleteServerVersionConsumer = eventBus.localConsumer<ServerVersionData>("process.server.deleteServerVersion")
    deleteServerVersionConsumer.handler { message ->
      val serverVersion = message.body()
      val query = "DELETE FROM server_version WHERE major = ? AND minor = ? AND patch = ?"
      val rowsFuture = client.preparedQuery(query).execute(
        Tuple.of(serverVersion.major,
          serverVersion.minor,
          serverVersion.patch))

      rowsFuture.onFailure { res ->
        println("Failed to execute query: $res")
        message.reply(failedMessage)
      }

      rowsFuture.onComplete { _ ->
        setCacheFlag(eventBus, CACHE_ADDRESS_VERSION)
        message.reply("Server version deleted successfully!")
      }
    }
  }

  /**
   * Make JSON fields from a row out of the database for the server data
   */
  private fun Row.makeServerData(): ServerDataData {
    return ServerDataData(
      key = this.getString("key"),
      value = this.getString("value")
    )
  }

  /**
   * Make JSON fields from a row out of the database for the server version
   */
  private fun Row.makeServerVersion(): ServerVersionData {
    return ServerVersionData(
      major = this.getInteger("major"),
      minor = this.getInteger("minor"),
      patch = this.getInteger("patch"),
      versionName = this.getString("version_name"),
      versionDescription = this.getString("version_description")
    )
  }

  /**
   * Make a tuple for the server data for database insertion or update
   */
  private fun ServerDataData.toTuple(putRequest: Boolean): Tuple {
    val serverDataTuple: Tuple = if (putRequest) {
      Tuple.of(
        this.value,
        this.key
      )
    } else {
      Tuple.of(
        this.key,
        this.value
      )
    }

    return serverDataTuple
  }

  /**
   * Make a tuple for the server version for database insertion or update
   */
  private fun ServerVersionData.toTuple(putRequest: Boolean): Tuple {

    val serverVersionTuple: Tuple
    if (putRequest) {
      serverVersionTuple = Tuple.of(
        this.major,
        this.minor,
        this.patch,
        this.versionName,
        this.versionDescription,
        this.major,
        this.minor,
        this.patch
      )
    } else {
      serverVersionTuple = Tuple.of(
        this.major,
        this.minor,
        this.patch,
        this.versionName,
        this.versionDescription
      )
    }

    return serverVersionTuple
  }

  /**
   * Answer the server data message with the retrieved data
   */
  private fun answerServerDataMessage(rowsFuture: Future<RowSet<Row>>, message: Message<String>) {
    val serverDataDataList: MutableList<ServerDataData> = emptyList<ServerDataData>().toMutableList()

    rowsFuture.onFailure { res ->
      println("Failed to execute query: $res")
      message.reply(failedMessage)
    }
    rowsFuture.onSuccess { res ->
      if (res.size() > 0) {
        res.value().forEach { row ->
          serverDataDataList.add(row.makeServerData())
        }
      }

      message.reply(serverDataDataList, listDeliveryOptions)
    }
  }
}
