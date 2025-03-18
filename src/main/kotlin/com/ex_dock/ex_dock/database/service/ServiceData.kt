package com.ex_dock.ex_dock.database.service

import com.ex_dock.ex_dock.database.template.Template

fun getAllStandardTemplates(): List<Template> {
  val templates: MutableList<Template> = emptyList<Template>().toMutableList()

  templates.add(Template(
    "testKey",
    "<test>testData</test>",
    ""
  ))
  return templates.toList()
}

class PopulateException(message: String) : Exception(message)

class InvalidCacheKeyException(message: String) : Exception(message)
