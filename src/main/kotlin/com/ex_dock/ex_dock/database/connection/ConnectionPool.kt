package com.ex_dock.ex_dock.database.connection

import com.ex_dock.ex_dock.ClassLoaderDummy
import io.vertx.core.Vertx
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.Pool
import io.vertx.sqlclient.PoolOptions
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties


fun Vertx.getConnection(): Pool {
  val connection: Pool
  val connectOptions = JDBCConnectOptions()

  try {
    val props = Properties()
    val configFileName = "secret.properties"
    val externalConfigPath = "/app/config/$configFileName"
    val localExternalConfigPath = "config/$configFileName"
    val configFile: File

    val potentialExternalFile = File(externalConfigPath)
    if (potentialExternalFile.exists()) {
      configFile = potentialExternalFile
      try {
          FileInputStream(configFile).use { props.load(it) }
      } catch (_: IOException) {}
    } else {
      configFile = File(localExternalConfigPath)
      try {
        FileInputStream(configFile).use { props.load(it) }
      } catch (_: IOException) {}
    }

    connectOptions
      .setJdbcUrl(props.getProperty("DATABASE_URL"))
      .setUser(props.getProperty("DATABASE_USERNAME"))
      .setPassword(props.getProperty("DATABASE_PASSWORD"))
  } catch (_: Exception) {
    try {
      val isDocker: Boolean = !System.getenv("GITHUB_RUN_NUMBER").isNullOrEmpty()
      if (isDocker) {
        connectOptions
          .setJdbcUrl("jdbc:postgresql://localhost:8890/ex-dock")
          .setUser("postgres")
          .setPassword("docker")
      } else {
        error("Could not load the Properties file!")
      }
    } catch (_: Exception) {
      error("Could not read the Properties file!")
    }
  }

  val poolOptions = PoolOptions()
    .setMaxSize(16)
    .setName("ex-dock")

  connection = JDBCPool.pool(this, connectOptions, poolOptions)

  return connection
}
