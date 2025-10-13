package com.ex_dock.ex_dock.helper.attributes

import com.ex_dock.ex_dock.global.cachedScopes
import com.ex_dock.ex_dock.helper.scopes.ScopeLevel
import io.vertx.core.Future
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.FindOptions
import io.vertx.ext.mongo.MongoClient
import kotlin.reflect.KClass


/**
 * The [Attributes] abstract class dictates the way that attributes are handled in exDock.
 * It is the basis for all different types of attributes.
 */
abstract class Attributes(internal val client: MongoClient) {
  abstract val collection: String

  private fun getCollectionKey(scopeKey: String): String {
    if (scopeKey == "global") return collection
    return "$collection-$scopeKey"
  }

  internal fun getScopedDataSingle(
      scopeKey: String,
      query: JsonObject,
      fields: JsonObject? = null
  ): Future<JsonObject?> {
    var globalData: JsonObject? = null
    var websiteData: JsonObject? = null
    var scopeData: JsonObject? = null
    val allFutures = mutableListOf<Future<Unit>>()

    val scope = cachedScopes.getJsonObject(scopeKey) ?: return Future.failedFuture("Scope not found")

    allFutures.add(
      Future.future { promise ->
        client.findOne(getCollectionKey("global"), query, fields).onFailure { err ->
          promise.fail(err)
        }.onSuccess { res ->
          globalData = res
          promise.complete()
        }
      }
    )

    if (scopeKey != "global") {
      allFutures.add(
        Future.future { promise ->
          client.findOne(getCollectionKey(scopeKey), query, fields).onFailure { err ->
            promise.fail(err)
          }.onSuccess { res ->
            scopeData = res
          }
        }
      )

      if (scope.getString("scopeType") == "store-view") {
        allFutures.add(
          Future.future { promise ->
            client.findOne(getCollectionKey(scope.getString("websiteId")), query, fields).onFailure { err ->
              promise.fail(err)
            }.onSuccess { res ->
              websiteData = res
              promise.complete()
            }
          }
        )
      }
    }

    return Future.future { promise ->
      Future.all<Unit>(allFutures).onFailure { err ->
        promise.fail(err)
      }.onSuccess { _ ->
        if (globalData == null && websiteData == null && scopeData == null) return@onSuccess promise.complete(null)

        promise.complete(
          (globalData ?: JsonObject()).apply {
            if (websiteData != null) mergeIn(websiteData)
            if (scopeData != null) mergeIn(scopeData)
          }
        )
      }
    }
  }

  internal fun getScopedData(scopeKey: String, query: JsonObject, fields: JsonObject? = null): Future<List<JsonObject>> {
    var globalData: List<JsonObject>? = null
    var websiteData: List<JsonObject>? = null
    var scopeData: List<JsonObject>? = null
    val allFutures = mutableListOf<Future<Unit>>()

    val scope = cachedScopes.getJsonObject(scopeKey) ?: return Future.failedFuture("Scope not found")
    val findOptions = FindOptions().setFields(fields ?: JsonObject())

    allFutures.add(
      Future.future { promise ->
        client.findWithOptions(
          getCollectionKey("global"),
          query,
          findOptions,
        ).onFailure { err ->
          promise.fail(err)
        }.onSuccess { res ->
          globalData = res
          promise.complete()
        }
      }
    )

    if (scopeKey != "global") {
      allFutures.add(
        Future.future { promise ->
          client.findWithOptions(getCollectionKey(scopeKey), query, findOptions).onFailure { err ->
            promise.fail(err)
          }.onSuccess { res ->
            scopeData = res
          }
        }
      )

      if (scope.getString("scopeType") == "store-view") {
        allFutures.add(
          Future.future { promise ->
            client.findWithOptions(getCollectionKey(scope.getString("websiteId")), query, findOptions).onFailure { err ->
              promise.fail(err)
            }.onSuccess { res ->
              websiteData = res
              promise.complete()
            }
          }
        )
      }
    }

    return Future.future { promise ->
      Future.all<Unit>(allFutures).onFailure { err ->
        promise.fail(err)
      }.onSuccess { _ ->
        // Should never happen, but just to be sure
        if (globalData == null && websiteData == null && scopeData == null) return@onSuccess promise.complete(null)

        val globalDataMap = mutableMapOf<String, JsonObject>()
        val websiteDataMap = mutableMapOf<String, JsonObject>()
        val scopeDataMap = mutableMapOf<String, JsonObject>()
        val allIds = mutableListOf<String>()

        globalData?.forEach { data ->
          val id = data.getString("_id")
          allIds.add(id)
          globalDataMap[id] = data
        }
        websiteData?.forEach { data ->
          val id = data.getString("_id")
          allIds.add(id)
          websiteDataMap[id] = data
        }
        scopeData?.forEach { data ->
          val id = data.getString("_id")
          allIds.add(id)
          scopeDataMap[id] = data
        }

        val result = mutableListOf<JsonObject>()

        allIds.forEach { id ->
          var websiteDone = false
          var scopeDone = false
          var data: JsonObject? = globalDataMap[id]
          if (data == null) {
            data = websiteDataMap[id]
            websiteDone = true
            if (data == null) {
              data = scopeDataMap[id]
              scopeDone = true
            }
          }

          val websiteData = websiteDataMap[id]
          if (!websiteDone && websiteData != null) {
            data!!.mergeIn(websiteData)
          }

          val scopeData = scopeDataMap[id]
          if (!scopeDone && scopeData != null) {
            data!!.mergeIn(scopeData)
          }

          if (data != null) result.add(data)
        }

        promise.complete(result)
      }
    }
  }

  fun getAttributeValue(entityId: String, attributeKey: String, scopeKey: String): Future<Any?> {
    return Future.future { promise ->
      client.findOne(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        JsonObject().put(attributeKey, 1),
      ).onFailure { err ->
        promise.fail(err)
      }.onSuccess { res ->
        promise.complete(res)
      }
    }
  }

  fun getAttributeValue(entityIds: List<String>, attributeKey: String, scopeKey: String): Future<List<Any?>> {
    return Future.future { promise ->
      client.findWithOptions(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", JsonObject().put($$"$in", JsonArray(entityIds))),
        FindOptions().setFields(JsonObject().put(attributeKey, 1)),
      ).onFailure { err ->
        promise.fail(err)
      }.onSuccess { res ->
        promise.complete(res)
      }
    }
  }

  fun getAttributesValue(entityId: String, attributeKeys: List<String>, scopeKey: String): Future<Any?> {
    return Future.future { promise ->
      val fields = JsonObject()
      for (key in attributeKeys) fields.put(key, 1)
      client.findOne(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        fields,
      ).onFailure { err ->
        promise.fail(err)
      }.onSuccess { res ->
        promise.complete(res)
      }
    }
  }

  fun getAttributesValue(entityId: String, scopeKey: String): Future<Any?> {
    return Future.future { promise ->
      client.findOne(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        null,
      ).onFailure { err ->
        promise.fail(err)
      }.onSuccess { res ->
        promise.complete(res)
      }
    }
  }

  abstract fun getAttributeType(attributeKey: String): KClass<*>
  abstract fun checkValueType(attributeKey: String, value: Any): Boolean

  fun setAttributeValue(entityId: String, attributeKey: String, value: Any, scopeKey: String): Future<Any> {
    return Future.future { promise ->
      if (!checkValueType(attributeKey, value)) return@future promise.fail(
        "$value (type: ${value::class.simpleName}) is not the correct type for $attributeKey (type: ${
          getAttributeType(
            attributeKey
          ).simpleName
        })"
      )

      client.findOneAndUpdate(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        JsonObject().put($$"$set", JsonObject().put(attributeKey, value)),
      ).onFailure { err -> promise.fail(err) }.onSuccess { _ ->
        promise.complete(value)
      }
    }
  }

  fun setAttributesValue(entityId: String, attributes: Map<String, Any>, scopeKey: String): Future<Map<String, Any>> {
    return Future.future { promise ->
      for ((attributeKey, value) in attributes) {
        // TODO: save all wrong types and return it inside 1 fail()
        if (!checkValueType(attributeKey, value)) return@future promise.fail(
          "$value (type: ${value::class.simpleName}) is not the correct type for $attributeKey (type: ${
            getAttributeType(
              attributeKey
            ).simpleName
          })"
        )
      }
      client.findOneAndUpdate(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        JsonObject().put($$"$set", JsonObject(attributes)),
      ).onFailure { err -> promise.fail(err) }.onSuccess { _ ->
        promise.complete(attributes)
      }
    }
  }

  /**
   * Clears the attribute for the entity.
   *
   * @throws IllegalArgumentException When you try to clear a required attribute.
   */
  fun clearAttributeValue(entityId: String, attributeKey: String, scopeKey: String): Future<Unit> {
    return Future.future { promise ->
      client.findOneAndUpdate(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        JsonObject().put($$"$unset", JsonObject().put(attributeKey, null)),
      ).onFailure { err -> promise.fail(err) }.onSuccess { _ -> promise.complete() }
    }
  }

  /**
   * Clears the attributes for the entity.
   *
   * @throws IllegalArgumentException When you try to clear a required attribute.
   */
  fun clearAttributesValue(entityId: String, attributeKeys: List<String>, scopeKey: String): Future<Unit> {
    val attributes = mutableMapOf<String, Any?>()
    for (key in attributeKeys) attributes[key] = null

    return Future.future { promise ->
      client.findOneAndUpdate(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", entityId),
        JsonObject().put($$"$unset", JsonObject(attributes)),
      ).onFailure { err -> promise.fail(err) }.onSuccess { _ -> promise.complete() }
    }
  }

  /**
   * Clears all the attributes for the entity. This is meant as an assist for the removal of the entityId
   *
   * Attention: Also removes the required attributes!
   */
  abstract fun clearAllAttributesValue(entityId: String)

  abstract fun createAttribute(attributeName: String, attributeKey: String, dataType: String, scopeLevel: ScopeLevel)

  // TODO: fun editAttribute()

  abstract fun deleteAttribute(attributeKey: String)
}
