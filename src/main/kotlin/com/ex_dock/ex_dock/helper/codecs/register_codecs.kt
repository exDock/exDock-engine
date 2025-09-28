package com.ex_dock.ex_dock.helper.codecs

import com.ex_dock.ex_dock.database.codec.GenericCodec
import com.ex_dock.ex_dock.database.codec.GenericListCodec
import io.vertx.core.eventbus.EventBus
import kotlin.reflect.KClass

fun EventBus.registerGenericCodec(codecClass: KClass<*>): EventBus {
  this.registerCodec(GenericCodec(codecClass))

  return this
}

fun EventBus.registerGenericListCodec(codecClass: KClass<*>): EventBus {
  this.registerCodec(GenericListCodec(codecClass))

  return this
}
