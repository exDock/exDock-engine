//package com.ex_dock.ex_dock.frontend.template_engine
//
//import com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use.SingleUseTemplateData
//import com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use.SingleUseTemplateDataCodec
//import com.ex_dock.ex_dock.helper.deployWorkerVerticleHelper
//import io.vertx.core.eventbus.EventBus
//import io.vertx.junit5.VertxExtension
//import io.vertx.junit5.VertxTestContext
//import io.vertx.core.Vertx
//import io.vertx.core.eventbus.DeliveryOptions
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.BeforeEach
//
//@ExtendWith(VertxExtension::class)
//class TemplateEngineVerticleSingleUseTest {
//  lateinit var eventBus: EventBus
//
//  @BeforeEach
//  fun deployEventBus(vertx: Vertx, testContext: VertxTestContext) {
//    eventBus = vertx.eventBus()
//
//    testContext.completeNow()
//  }
//
//  @BeforeEach
//  fun deployTemplateEngineVerticle(vertx: Vertx, testContext: VertxTestContext) {
//    deployWorkerVerticleHelper(
//      vertx,
//      TemplateEngineVerticle::class.qualifiedName.toString(),
//      1,
//      1
//    ).onSuccess {
//      testContext.completeNow()
//    }.onFailure { throwable ->
//      testContext.failNow(throwable)
//    }
//  }
//
//  @Test
//  fun testSingleUseTemplate(vertx: Vertx, testContext: VertxTestContext) {
//    val checkpoint = testContext.checkpoint()
//
//    val singleUseTemplateData: SingleUseTemplateData = SingleUseTemplateData(
//      "<test>{{ name }}</test>",
//      mapOf("name" to "testName")
//    )
//
//    val expectedResult: String = "<test>testName</test>"
//
//    eventBus.request<String>(
//      "template.generate.singleUse", singleUseTemplateData, DeliveryOptions().setCodecName(
//        SingleUseTemplateDataCodec().name()
//      )
//    ).onFailure {
//      testContext.failNow(it)
//    }.onComplete { message ->
//      assert(message.succeeded())
//      assertEquals(
//        message.result().body()::class.simpleName,
//        "String",
//        "Received class isn't String"
//      )
//
//      val result: String = message.result().body()
//      testContext.verify {
//        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
//      }
//
//      // Mark as successful
//      checkpoint.flag()
//    }
//  }
//
//  @Test
//  fun testAbundantData(vertx: Vertx, testContext: VertxTestContext) {
//    val singleUseTemplateData: SingleUseTemplateData = SingleUseTemplateData(
//      "<test>{{ name }}</test>",
//      mapOf("name" to "testName", "abundantName" to "testAbundantName")
//    )
//
//    val expectedResult: String = "<test>testName</test>"
//
//    eventBus.request<String>(
//      "template.generate.singleUse", singleUseTemplateData, DeliveryOptions().setCodecName(
//        SingleUseTemplateDataCodec().name()
//      )
//    ).onFailure {
//      testContext.failNow(it)
//    }.onComplete { message ->
//      assert(message.succeeded())
//      assertEquals(
//        message.result().body()::class.simpleName,
//        "String",
//        "Received class isn't String"
//      )
//
//      val result: String = message.result().body()
//      testContext.verify {
//        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
//      }
//
//      // Mark as successful
//      testContext.completeNow()
//    }
//  }
//
//  @Test
//  fun testMissingData(vertx: Vertx, testContext: VertxTestContext) {
//    val singleUseTemplateData: SingleUseTemplateData = SingleUseTemplateData(
//      "<test>{{ name }}</test>",
//      mapOf()
//    )
//
//    val expectedResult: String = "<test></test>"
//
//    eventBus.request<String>(
//      "template.generate.singleUse", singleUseTemplateData, DeliveryOptions().setCodecName(
//        SingleUseTemplateDataCodec().name()
//      )
//    ).onFailure {
//      testContext.failNow(it)
//    }.onComplete { message ->
//      assert(message.succeeded())
//      assertEquals(
//        message.result().body()::class.simpleName,
//        "String",
//        "Received class isn't String"
//      )
//
//      val result: String = message.result().body()
//      testContext.verify {
//        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
//      }
//
//      // Mark as successful
//      testContext.completeNow()
//    }
//  }
//
//  @Test
//  fun testSubData(vertx: Vertx, testContext: VertxTestContext) {
//    val singleUseTemplateData: SingleUseTemplateData = SingleUseTemplateData(
//      "<test>{{ name.subData }}</test>",
//      mapOf("name" to mapOf("subData" to "testSubData"))
//    )
//
//    val expectedResult: String = "<test>testSubData</test>"
//
//    eventBus.request<String>(
//      "template.generate.singleUse", singleUseTemplateData, DeliveryOptions().setCodecName(
//        SingleUseTemplateDataCodec().name()
//      )
//    ).onFailure {
//      testContext.failNow(it)
//    }.onComplete { message ->
//      assert(message.succeeded())
//      assertEquals(
//        message.result().body()::class.simpleName,
//        "String",
//        "Received class isn't String"
//      )
//
//      val result: String = message.result().body()
//      testContext.verify {
//        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
//      }
//
//      // Mark as successful
//      testContext.completeNow()
//    }
//  }
//
//  @Test
//  fun testRequestMapValue(vertx: Vertx, testContext: VertxTestContext) {
//    val singleUseTemplateData: SingleUseTemplateData = SingleUseTemplateData(
//      "<test>{{ name }}</test>",
//      mapOf("name" to mapOf("subData" to "testSubData"))
//    )
//
//    val expectedResult: String = "<test>{subData=testSubData}</test>"
//
//    eventBus.request<String>(
//      "template.generate.singleUse", singleUseTemplateData, DeliveryOptions().setCodecName(
//        SingleUseTemplateDataCodec().name()
//      )
//    ).onFailure {
//      testContext.failNow(it)
//    }.onComplete { message ->
//      assert(message.succeeded())
//      assertEquals(
//        message.result().body()::class.simpleName,
//        "String",
//        "Received class isn't String"
//      )
//
//      val result: String = message.result().body()
//      testContext.verify {
//        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
//      }
//
//      // Mark as successful
//      testContext.completeNow()
//    }
//  }
//
//  @Test
//  fun testPickNewestData(vertx: Vertx, testContext: VertxTestContext) {
//    val singleUseTemplateData: SingleUseTemplateData = SingleUseTemplateData(
//      "<test>{{ name }}</test>",
//      mapOf("name" to "testData1", "name" to "testData2")
//    )
//
//    val expectedResult: String = "<test>testData2</test>"
//
//    eventBus.request<String>(
//      "template.generate.singleUse", singleUseTemplateData, DeliveryOptions().setCodecName(
//        SingleUseTemplateDataCodec().name()
//      )
//    ).onFailure {
//      testContext.failNow(it)
//    }.onComplete { message ->
//      assert(message.succeeded())
//      assertEquals(
//        message.result().body()::class.simpleName,
//        "String",
//        "Received class isn't String"
//      )
//
//      val result: String = message.result().body()
//      testContext.verify {
//        assertEquals(expectedResult, result, "Output isn't equal to the expected output")
//      }
//
//      // Mark as successful
//      testContext.completeNow()
//    }
//  }
//}
