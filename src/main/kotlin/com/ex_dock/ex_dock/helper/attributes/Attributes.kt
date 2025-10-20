package com.ex_dock.ex_dock.helper.attributes

import com.ex_dock.ex_dock.global.cachedScopes
import com.ex_dock.ex_dock.helper.futures.onComplete
import com.ex_dock.ex_dock.helper.futures.onFailure
import com.ex_dock.ex_dock.helper.futures.onSuccess
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
  val collectionConfigKey = "$collection-attributes"
  abstract val allowedTypes: Map<String, KClass<*>>

  internal fun getCollectionKey(scopeKey: String): String {
    if (scopeKey == "global") return collection
    return "$collection-$scopeKey"
  }

  internal fun isValidAttributeKey(attributeKey: String): Boolean {
    if (attributeKey.length < 4) return false

    // Regular expression explanation:
    // ^[a-zA-Z]             - Must start with a letter (upper or lower case).
    // [a-zA-Z0-9_-]* - Followed by zero or more of: letters, numbers, hyphen, or underscore.
    // [a-zA-Z0-9]$          - MUST end with a letter or a number.
    //                         This ensures the key cannot end with a hyphen or an underscore.
    val regex = Regex("^[a-zA-Z][a-zA-Z0-9_-]*[a-zA-Z0-9]$")
    return attributeKey.matches(regex)
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

  internal fun getScopedData(
      scopeKey: String,
      query: JsonObject,
      fields: JsonObject? = null
  ): Future<List<JsonObject>> {
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
            client.findWithOptions(getCollectionKey(scope.getString("websiteId")), query, findOptions)
              .onFailure { err ->
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
      getScopedDataSingle(
        scopeKey,
        JsonObject().put("_id", entityId),
        JsonObject().put(attributeKey, 1)
      ).onFailure { err ->
        promise.fail(err)
      }.onSuccess { res ->
        promise.complete(res)
      }
    }
  }

  fun getAttributeValue(entityIds: List<String>, attributeKey: String, scopeKey: String): Future<List<Any?>> {
    return Future.future { promise ->
      getScopedData(
        getCollectionKey(scopeKey),
        JsonObject().put("_id", JsonObject().put($$"$in", JsonArray(entityIds))),
        JsonObject().put(attributeKey, 1),
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
      getScopedDataSingle(
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
      getScopedDataSingle(
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

  fun getAttributeType(attributeKey: String): Future<KClass<*>> {
    return Future.future { promise ->
      client.findOne(
        collectionConfigKey,
        JsonObject().put("_id", attributeKey),
        JsonObject().put("type", 1)
      ).onFailure(promise).onSuccess { res ->
        promise.complete(
          allowedTypes[res.getString("type")]
            ?: throw IllegalStateException("For some reason, the attribute type for this attributeKey is not in the allowedTypes map... This means that the KClass can't be matched and returned. A database repair is required")
        )
      }
    }
  }

  fun checkValueType(attributeKey: String, kClass: KClass<*>): Future<Boolean> {
    return Future.future { promise ->
      getAttributeType(attributeKey).onFailure(promise).onSuccess { res ->
        promise.complete(res == kClass)
      }
    }
  }

  fun checkValueType(attributeKey: String, value: Any): Future<Boolean> {
    return checkValueType(attributeKey, value::class)
  }

  fun setAttributeValue(entityId: String, attributeKey: String, value: Any, scopeKey: String): Future<Any> {
    return Future.future { promise ->
      checkValueType(attributeKey, value).onFailure(promise).onSuccess { res ->
        if (!res) {
          getAttributeType(attributeKey).onComplete { asyncRes ->
            promise.fail(
              "$value (type: ${value::class.simpleName}) is not the correct type for $attributeKey (type: ${asyncRes.result() ?: "type name could not be retrieved"})"
            )
          }
          return@onSuccess
        }

        client.findOneAndUpdate(
          getCollectionKey(scopeKey),
          JsonObject().put("_id", entityId),
          JsonObject().put($$"$set", JsonObject().put(attributeKey, value)),
        ).onFailure(promise).onSuccess { _ ->
          promise.complete(value)
        }
      }
    }
  }

  fun setAttributesValue(entityId: String, attributes: Map<String, Any>, scopeKey: String): Future<Map<String, Any>> {
    return Future.future { promise ->
      val checkValueType: List<Future<Boolean>> = attributes.map { (attributeKey, value) ->
        checkValueType(attributeKey, value)
      }

      val attributeKeyList = attributes.keys.toList()
      val wrongTypeAttributeValues = mutableMapOf<String, Any>()

      Future.all<Boolean>(checkValueType).onFailure(promise).onSuccess { asyncRes ->
        asyncRes.list<Boolean>().forEachIndexed { index, res ->
          if (!res) {
            val attributeKey = attributeKeyList[index]
            wrongTypeAttributeValues[attributeKey] = attributes[attributeKey]!!
          }
        }

        if (wrongTypeAttributeValues.isNotEmpty()) {
          val errorMessages = mutableListOf<String>()

          val typeNameFutures: List<Future<KClass<*>?>> = wrongTypeAttributeValues.keys.map { attributeKey ->
            Future.future { promise ->
              getAttributeType(attributeKey).onComplete { asyncRes ->
                promise.complete(asyncRes.result())
              }
            }
          }

          Future.all<KClass<*>>(typeNameFutures).onFailure(promise).onSuccess { res ->
            wrongTypeAttributeValues.entries.forEachIndexed { index, (key, value) ->
              val expectedTypeName =
                (res.resultAt(index) as KClass<*>)::class.simpleName ?: "type name could not be retrieved"
              errorMessages.add(
                "$value (type: ${value::class.simpleName}) is not the correct type for $key (type: $expectedTypeName)"
              )
            }
          }

          promise.fail(
            "The following type errors occurred while trying to set multiple attributes: \n- " +
                errorMessages.joinToString("\n- ") +
                "\n\nThe complete set attributes operation was aborted."
          )

          return@onSuccess
        }

        client.findOneAndUpdate(
          getCollectionKey(scopeKey),
          JsonObject().put("_id", entityId),
          JsonObject().put($$"$set", JsonObject(attributes)),
        ).onFailure { err -> promise.fail(err) }.onSuccess { _ ->
          promise.complete(attributes)
        }

        return@onSuccess
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
   * Clears all the attributes for the entity. This is meant as an assist for the removal of the entityId.
   *
   * Attention: Also removes the required attributes!
   */
  fun clearAllAttributesValue(entityId: String): Future<Unit> {
    val query = JsonObject().put("_id", entityId)
    val allFutures = mutableListOf<Future<*>>()
    for ((key, _) in cachedScopes) allFutures.add(client.removeDocument(getCollectionKey(key), query))
    allFutures.add(client.removeDocument(collection, query))
    return Future.future { promise ->
      Future.all<Unit>(allFutures).onFailure { err ->
        promise.fail(err)
      }.onSuccess { _ ->
        promise.complete()
      }
    }
  }

  /**
   * Clears all the values for a certain attribute across all scopes. This is meant as an assist for the removal of an attribute.
   */
  fun clearAttributeAllValues(attributeKey: String): Future<Unit> {
    return Future.future { promise ->
      val allFutures = mutableListOf<Future<*>>()
      for ((key, _) in cachedScopes) allFutures.add(
        client.updateCollection(
          getCollectionKey(key),
          JsonObject(),
          JsonObject().put($$"$unset", JsonObject().put(attributeKey, null))
        )
      )
      allFutures.add(
        client.updateCollection(
          collection,
          JsonObject(),
          JsonObject().put($$"$unset", JsonObject().put(attributeKey, null))
        )
      )
      Future.all<Any?>(allFutures).onFailure(promise).onSuccess(promise)
    }
  }

  fun createAttribute(
      attributeName: String,
      attributeKey: String,
      dataType: String,
      scopeLevel: ScopeLevel
  ): Future<Unit> {
    if (!isValidAttributeKey(attributeKey)) return Future.failedFuture("Invalid attribute key")
    if (allowedTypes[dataType] == null) return Future.failedFuture("Invalid data type")
    return Future.future { promise ->
      val document = JsonObject()
        .put("_id", attributeKey)
        .put("name", attributeName)
        .put("type", dataType)
        .put("scopeLevel", scopeLevel.name)
      client.insert(collectionConfigKey, document).onFailure(promise).onSuccess(promise)
    }
  }

  // TODO: fun editAttribute()

  fun deleteAttribute(attributeKey: String): Future<Unit> {
    return Future.future { promise ->
      clearAttributeAllValues(attributeKey).onFailure(promise).onSuccess { _ ->
        client.removeDocument(collectionConfigKey, JsonObject().put("_id", attributeKey)).onComplete(promise)
      }
    }
  }
}
