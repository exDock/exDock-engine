package com.ex_dock.ex_dock.backend.v1.router.auth

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.util.Base64

class AuthProvider {
  private val generator: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
  private val keyPair: KeyPair = generator.generateKeyPair()

  private companion object {
    const val BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----\n"
    const val END_PRIVATE_KEY = "\n-----END PRIVATE KEY-----"
    const val BEGIN_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
    const val END_PUBLIC_KEY = "\n-----END PUBLIC KEY-----"
  }

  val privateKey = BEGIN_PRIVATE_KEY +
    Base64.getMimeEncoder().encodeToString(keyPair.private.encoded) +
    END_PRIVATE_KEY
  val publicKey = BEGIN_PUBLIC_KEY +
    Base64.getMimeEncoder().encodeToString(keyPair.public.encoded) +
    END_PUBLIC_KEY
}
