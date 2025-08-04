package com.ex_dock.ex_dock.database.utils

import com.ex_dock.ex_dock.helper.load
import java.util.Properties

class JdbcSetup(
  propertiesFile: String = "secret.properties",
  defaultPropertiesFile: String = "default.properties",
  classLoader: ClassLoader = JdbcSetup::class.java.classLoader,
  ) {
  private val props: Properties = Properties().load()
  private val defaultProps: Properties = Properties().load(true)
  private var isCompleted: Boolean = false

  /**
    * Performs the initial setup for the JdbcSetup instance by using the default settings.
    *
    * This function checks if setup has already been completed. If not, it iterates through the properties
    * loaded from the [propertiesFile] and assigns the corresponding values from the [defaultPropertiesFile].
    *
    * After the setup, it sets the [isCompleted] flag to true.
    *
    * @param reset Indicates whether it should redo the setup when it is already completed
    *
    * @throws IllegalStateException If setup has already been completed and the [reset] flag has not been set to true.
    */
  fun setup(reset: Boolean = false) {
    if (isCompleted && !reset) throw IllegalStateException("Setup has already been completed")

    // TODO: test
    for (key in defaultProps.keys) {
      props[key] = defaultProps[key]
    }
    isCompleted = true

  }

  /**
   * Performs the initial setup for the JdbcSetup instance by using the default settings.
   *
   * This function checks if setup has already been completed. If not, it iterates through the properties
   * loaded from the [propertiesFile] and assigns the corresponding values from provided [inputMap].
   * if the value is not in [inputMap] a value from the [defaultPropertiesFile] is assigned.
   * All keys that are in the [inputMap], but not in [defaultPropertiesFile] are ignored.
   *
   * After the setup, it sets the [isCompleted] flag to true.
   *
   * @param inputMap the properties that are programmatically provided
   * @param reset Indicates whether it should redo the setup when it is already completed
   *
   * @throws IllegalStateException If setup has already been completed and the [reset] flag has not been set to true.
   */
  fun setup(inputMap: Map<Any, Any>, reset: Boolean = false) {
    if (isCompleted && !reset) throw IllegalStateException("Setup has already been completed")

    // TODO: test
    for (key in defaultProps.keys) {
      if (inputMap.containsKey(key)) {
        props[key] = inputMap[key]
        continue
      }
      props[key] = defaultProps[key]
    }
    isCompleted = true
  }
}
