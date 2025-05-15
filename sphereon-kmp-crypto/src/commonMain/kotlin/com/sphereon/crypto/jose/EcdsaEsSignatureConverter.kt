package com.sphereon.crypto.jose

object EcdsaEsSignatureConverter {
  private const val SEQUENCE_TAG: Byte = 0x30
  private const val INTEGER_TAG: Byte = 0x02

  /**
   * @param sig the signature from a external call (in DER or raw)
   * @param keySize number of bytes per coordinate (32 for P-256, 48 for P-384…)
   * @return the JOSE-style Base64Url(R‖S) string
   */
  fun toJoseRaw(sig: ByteArray, keySize: Int = 32): ByteArray {
    return if (isDerEncoded(sig, keySize)) derToRaw(sig, keySize) else sig
  }

  /** Quick check: DER SEQUENCE with length > 2*keySize means it’s DER, not raw. */
  private fun isDerEncoded(sig: ByteArray, keySize: Int): Boolean {
    return sig.size > keySize * 2 && sig.getOrNull(0) == SEQUENCE_TAG
  }

  private fun derToRaw(der: ByteArray, keySize: Int): ByteArray {
    var offset = 0

    // 1) SEQUENCE tag
    require(der[offset++] == SEQUENCE_TAG) { "Invalid DER: no SEQUENCE" }

    // 2) read length (short or long form)
    var length = der[offset++].toInt() and 0xFF
    if (length and 0x80 != 0) {
      val count = length and 0x7F
      length = 0
      repeat(count) {
        length = (length shl 8) or (der[offset++].toInt() and 0xFF)
      }
    }

    // 3) INTEGER R
    require(der[offset++] == INTEGER_TAG) { "Invalid DER: no INTEGER for R" }
    val rLen = der[offset++].toInt() and 0xFF
    val r = der.copyOfRange(offset, offset + rLen)
    offset += rLen

    // 4) INTEGER S
    require(der[offset++] == INTEGER_TAG) { "Invalid DER: no INTEGER for S" }
    val sLen = der[offset++].toInt() and 0xFF
    val s = der.copyOfRange(offset, offset + sLen)

    // 5) normalize to exactly keySize bytes
    fun normalize(coord: ByteArray): ByteArray {
      return when {
        coord.size == keySize -> coord
        coord.size >  keySize -> coord.copyOfRange(coord.size - keySize, coord.size)
        else -> ByteArray(keySize - coord.size) + coord
      }
    }

    return normalize(r) + normalize(s)
  }
}