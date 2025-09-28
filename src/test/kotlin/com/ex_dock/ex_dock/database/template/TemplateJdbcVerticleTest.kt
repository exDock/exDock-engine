package com.ex_dock.ex_dock.database.template

import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
import com.ex_dock.ex_dock.helper.codecs.registerGenericCodec
import io.vertx.core.Vertx
import io.vertx.core.eventbus.DeliveryOptions
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import io.vertx.ext.unit.TestSuite
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(VertxExtension::class)
class TemplateJdbcVerticleTest {
  private lateinit var eventBus: EventBus
  private val templateDeliveryOptions = DeliveryOptions().setCodecName("TemplateCodec")
  private val testTemplate = Template(
    templateKey = "testKey",
    blockName = "testBlockName",
    templateData = "testTemplate",
    dataString = "testDataString"
  )

  @Test
  @DisplayName("Test the template classes functions")
  fun testTemplateClassesFunctions(vertx: Vertx, context: VertxTestContext) {
    val suite = TestSuite.create("testTemplateClassesFunctions")

    suite.test("testTemplateToJson") { testContext ->
      val result = testTemplate.toDocument()
      testContext.assertEquals(testTemplate.templateKey, result.getString("template_key"))
      testContext.assertEquals(testTemplate.blockName, result.getString("block_name"))
    }.test("testTemplateFromJson") { testContext ->
      val templateJson = testTemplate.toDocument()
      val template = Template.fromJson(templateJson)
      testContext.assertEquals(testTemplate.templateKey, template.templateKey)
      testContext.assertEquals(testTemplate.blockName, template.blockName)
    }

    suite.run(vertx).handler { res ->
      if (res.succeeded()) {
        context.completeNow()
      } else {
        context.failNow(res.cause())
      }
    }
  }

  @BeforeEach
  @DisplayName("Add the template to the database")
  fun setup(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus = vertx.eventBus()
    eventBus.registerGenericCodec(Template::class)
    eventBus.registerGenericCodec(List::class)

    deployWorkerVerticleHelper(
      vertx,
      TemplateJdbcVerticle::class.qualifiedName.toString(),
      1,
      1
    ).onFailure { err ->
      vertxTestContext.failNow(err)
    }.onSuccess {
      eventBus.request<Template>("process.template.createTemplate", testTemplate, templateDeliveryOptions).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = message.body()
        vertxTestContext.verify { ->
          assert(result.templateKey == testTemplate.templateKey)
          assert(result.blockName == testTemplate.blockName)
          assert(result.templateData == testTemplate.templateData)
          assert(result.dataString == testTemplate.dataString)
          vertxTestContext.completeNow()
        }
      }
    }
  }

  @Test
  @DisplayName("Test getting a template by key from the database")
  fun testGetTemplateByKey(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<JsonObject>("process.template.getTemplateByKey", testTemplate.templateKey).onFailure {
        vertxTestContext.failNow(it)
      }.onSuccess { message ->
        val result = Template.fromJson(message.body())
        vertxTestContext.verify { ->
          assert(result.templateKey == testTemplate.templateKey)
          assert(result.blockName == testTemplate.blockName)
          assert(result.templateData == testTemplate.templateData)
          assert(result.dataString == testTemplate.dataString)
          vertxTestContext.completeNow()
        }
      }
  }

  @Test
  @DisplayName("Test updating a template in the database")
  fun testUpdateTemplate(vertx: Vertx, vertxTestContext: VertxTestContext) {
    val updatedTemplate = testTemplate.copy(blockName = "updatedBlockName")
    eventBus.request<Template>("process.template.updateTemplate", updatedTemplate, templateDeliveryOptions).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      val result = message.body()
      vertxTestContext.verify { ->
        assert(result.blockName == updatedTemplate.blockName)
        vertxTestContext.completeNow()
      }
    }
  }

  @AfterEach
  @DisplayName("Remove the template from the database")
  fun tearDown(vertx: Vertx, vertxTestContext: VertxTestContext) {
    eventBus.request<String>("process.template.deleteTemplate", testTemplate.templateKey).onFailure {
      vertxTestContext.failNow(it)
    }.onSuccess { message ->
      vertxTestContext.verify { ->
        assert(message.body() == "Successfully deleted template")
        vertxTestContext.completeNow()
      }
    }
  }
}
