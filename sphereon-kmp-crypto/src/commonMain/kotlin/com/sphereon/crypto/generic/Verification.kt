package com.sphereon.crypto.generic

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

expect interface IVerifyResult {
    val name: String
    val error: Boolean
    val message: String?
    val critical: Boolean
}


expect interface IVerifyResults<out KeyType : IKey> {
    val error: Boolean
    val verifications: Array<out IVerifyResult>
    val keyInfo: IKeyInfo<KeyType>?
}

@Suppress("NON_EXPORTABLE_TYPE") // We are really exporting them because of the expect/actual
@Serializable
@JsExport
data class VerifyResults<out KeyType : IKey>(

    override val error: Boolean,
    override val verifications: Array<VerifyResult>,
    override val keyInfo: KeyInfo<KeyType>?
) : IVerifyResults<KeyType> {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyResults<*>) return false

        if (error != other.error) return false
        if (!verifications.contentEquals(other.verifications)) return false
        if (keyInfo != other.keyInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = error.hashCode()
        result = 31 * result + verifications.contentHashCode()
        result = 31 * result + (keyInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "VerifyResults(error=$error, verifications=${verifications.contentToString()}, keyInfo=$keyInfo)"
    }


}


@Serializable
@JsExport
open class VerifyResult(
    override val name: String,
    override val error: Boolean,
    override val message: String? = null,
    override val critical: Boolean = true
) : IVerifyResult {

    override fun toString(): String {
        return "VerifyResult(name='$name', error=$error, message=$message, critical=$critical)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyResult) return false

        if (name != other.name) return false
        if (error != other.error) return false
        if (message != other.message) return false
        if (critical != other.critical) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + critical.hashCode()
        return result
    }

    object Static {
        fun fromDTO(dto: IVerifyResult) = with(dto) { VerifyResult(name = name, error = error, message = message, critical = critical) }
    }
}


expect interface IVerifySignatureResult<out KeyType : IKey> : IVerifyResult {
    val keyInfo: IKeyInfo<KeyType>?
}

@JsExport
class VerifySignatureResult<out KeyType : IKey>(
    error: Boolean,
    name: String,
    critical: Boolean,
    message: String?,
    override val keyInfo: IKeyInfo<KeyType>?
) : IVerifySignatureResult<KeyType>, VerifyResult(error = error, name = name, critical = critical, message = message) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifySignatureResult<*>) return false
        if (!super.equals(other)) return false

        if (keyInfo != other.keyInfo) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (keyInfo?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "VerifySignatureResult(keyInfo=$keyInfo)"
    }


}
