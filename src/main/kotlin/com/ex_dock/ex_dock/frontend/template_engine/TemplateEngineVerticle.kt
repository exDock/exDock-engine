package com.ex_dock.ex_dock.frontend.template_engine

import com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use.SingleUseTemplateData
import com.ex_dock.ex_dock.frontend.template_engine.template_data.single_use.SingleUseTemplateDataCodec
import io.pebbletemplates.pebble.PebbleEngine
import io.pebbletemplates.pebble.loader.StringLoader
import io.vertx.core.AbstractVerticle
import io.vertx.core.eventbus.EventBus
import java.io.StringWriter

class TemplateEngineVerticle: AbstractVerticle() {
//  private lateinit var client: Pool
  private lateinit var eventBus: EventBus
  private val engine = PebbleEngine.Builder().loader(StringLoader()).build()

  override fun start() {
//    client = Connection().getConnection(vertx)
    eventBus = vertx.eventBus()

    singleUseTemplate()
  }

  private fun singleUseTemplate() {
    eventBus.registerCodec(SingleUseTemplateDataCodec())
    eventBus.consumer<SingleUseTemplateData>("template.generate.singleUse") { message ->
      val singleUseTemplateData: SingleUseTemplateData = message.body()

      val compiledTemplate = engine.getTemplate(singleUseTemplateData.template)

      val writer = StringWriter()
      compiledTemplate.evaluate(writer, singleUseTemplateData.templateData)

      message.reply(writer.toString())
    }
  }
}