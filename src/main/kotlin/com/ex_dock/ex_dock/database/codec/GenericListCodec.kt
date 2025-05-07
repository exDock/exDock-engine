package com.ex_dock.ex_dock.database.codec

import io.vertx.core.eventbus.MessageCodec
import kotlin.reflect.KClass
import io.vertx.core.buffer.Buffer
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.ObjectInputStream
import java.io.ObjectOutput
import java.io.ObjectOutputStream


class GenericListCodec<T : Any>(private val codecClass: KClass<T>) : MessageCodec<List<T>, List<T>> {
  override fun encodeToWire(buffer: Buffer, list: List<T>) {
    val bos = ByteArrayOutputStream()
    var out: ObjectOutput? = null

    try {
      out = ObjectOutputStream(bos)
      out.writeObject(list)
      out.flush()
      val bytes: ByteArray = bos.toByteArray()
      buffer.appendInt(bytes.size)
      buffer.appendBytes(bytes)
      out.close()
    } catch (e: IOException) {
      e.printStackTrace()
    } finally {
      try {
        bos.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  override fun decodeFromWire(pos: Int, buffer: Buffer): List<T>? {
    var effectivePos: Int = pos
    val length = buffer.getInt(effectivePos)

    val bytes: ByteArray = buffer.getBytes(effectivePos + 4, effectivePos + 4 + length)
    effectivePos += 4 + length
    val bis = ByteArrayInputStream(bytes)

    return try {
      val ois = ObjectInputStream(bis)
      @Suppress("UNCHECKED_CAST")
      val list: List<T> = ois.readObject() as List<T>
      ois.close()
      list
    } catch (e: IOException) {
      e.printStackTrace()
      null
    } catch (e: ClassNotFoundException) {
      e.printStackTrace()
      null
    } finally {
      try {
        bis.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
    }
  }

  override fun name(): String {
    return "${codecClass.simpleName}ListCodec"
  }

  override fun systemCodecID(): Byte {
    return -1
  }

  override fun transform(list: List<T>): List<T> {
    return list
  }
}
