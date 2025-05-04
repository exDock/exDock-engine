package com.ex_dock.ex_dock.database.auth

import io.vertx.core.AbstractVerticle
import io.vertx.ext.auth.PubSecKeyOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.*

class AuthenticationVerticle: AbstractVerticle() {
  private lateinit var jwtAuth: JWTAuth;
  private val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
  private val keyPair: KeyPair = generator.generateKeyPair()

  private companion object {
    const val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
    const val END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----"
    const val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
    const val END_PUBLIC_KEY = "\n-----END PUBLIC KEY-----"
  }

  override fun start() {
    setupJwtAuth()

  }

  private fun setupJwtAuth() {
    val privateKey = BEGIN_PRIVATE_KEY +
      Base64.getMimeEncoder().encodeToString(keyPair.private.encoded) +
      END_PRIVATE_KEY
    val publicKey = BEGIN_PUBLIC_KEY +
      Base64.getMimeEncoder().encodeToString(keyPair.public.encoded) +
      END_PUBLIC_KEY

    val config: JWTAuthOptions = JWTAuthOptions()
      .addPubSecKey(PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer(privateKey))
      .addPubSecKey(PubSecKeyOptions()
        .setAlgorithm("HS256")
        .setBuffer(publicKey)
      )

    jwtAuth = JWTAuth.create(vertx, config)
  }
}
