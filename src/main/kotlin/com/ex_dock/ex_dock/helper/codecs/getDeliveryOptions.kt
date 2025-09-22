package com.ex_dock.ex_dock.helper.codecs

import io.vertx.core.eventbus.DeliveryOptions
import kotlin.reflect.KClass

fun KClass<*>.deliveryOptions(): DeliveryOptions {
  return DeliveryOptions().setCodecName(this.codecName())
}
