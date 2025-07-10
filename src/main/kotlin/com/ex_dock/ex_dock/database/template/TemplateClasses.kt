package com.ex_dock.ex_dock.database.template

data class Template(
  val templateKey: String,
  val templateData: String,
  val dataString: String,
)

data class Block(
  val templateKey: String,
)
