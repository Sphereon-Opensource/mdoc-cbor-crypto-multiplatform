package com.sphereon.crypto.generic

import com.sphereon.crypto.IKey
import com.sphereon.crypto.IKeyInfo
import com.sphereon.crypto.KeyInfo
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * Interface representing the result of a verification process.
 */
expect interface IVerifyResult {
    /**
     * Stores the name of an entity.
     */
    val name: String
    /**
     * Indicates whether an error has occurred.
     *
     * This boolean variable is used to represent the presence of an error condition.
     * When set to `true`, it means an error has been detected; when set to `false`,
     * it indicates that no error is currently present.
     */
    val error: Boolean
    /**
     * An optional message that provides additional information about the verification result.
     */
    val message: String?
    /**
     * Indicates whether the condition is critical.
     *
     * This boolean variable is used to flag a critical state or condition within the application. When set to true,
     * it denotes that the condition has reached a critical level and may require immediate attention or handling.
     */
    val critical: Boolean
}


/**
 * Interface representing the results of a verification process.
 *
 * Provides information about the overall success or failure of the verification,
 * the individual verification results, and associated key information.
 *
 * @param KeyType The specific type of key implementing the IKey interface.
 */
expect interface IVerifyResults<out KeyType : IKey> {
    /**
     * Indicates whether an error has occurred.
     *
     * This variable can be used to determine if an operation resulted in an error state.
     * A value of `true` means an error is present, while `false` means no error has occurred.
     */
    val error: Boolean
    /**
     * Holds an array of verification results.
     *
     * Each element of the array represents a specific verification result,
     * detailing aspects such as the name of the verification, whether it resulted
     * in an error, an optional message describing the result, and whether the
     * result is critical.
     */
    val verifications: Array<out IVerifyResult>
    /**
     * Represents information about a specific key.
     *
     * This variable holds an instance of the [IKeyInfo] interface which is parameterized with [KeyType].
     * It encapsulates various details related to a key, such as its type and potentially other metadata.
     *
     * The variable can be null, indicating that no key information is available.
     *
     * @property keyInfo an instance of [IKeyInfo] for the specified [KeyType], or null if no key information is present.
     */
    val keyInfo: IKeyInfo<KeyType>?
}

/**
 * Represents the results of verification processes involving cryptographic keys.
 *
 * @param KeyType The type of key being verified, which must implement the `IKey` interface.
 * @property error Indicates if there was an error during the verification process.
 * @property verifications An array of `VerifyResult`, each representing an individual verification result.
 * @property keyInfo Information about the key being verified.
 */
@Suppress("NON_EXPORTABLE_TYPE") // We are really exporting them because of the expect/actual
@Serializable
@JsExport
data class VerifyResults<KeyType : IKey>(

    override val error: Boolean,
    override val verifications: Array<VerifyResult>,
    override val keyInfo: KeyInfo<KeyType>?
) : IVerifyResults<KeyType> {
    /**
     * Compares this `VerifyResults` instance with another object for equality.
     *
     * @param other the object to compare with this instance.
     * @return `true` if the specified object is equal to this instance, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyResults<*>) return false

        if (error != other.error) return false
        if (!verifications.contentEquals(other.verifications)) return false
        if (keyInfo != other.keyInfo) return false

        return true
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash tables
     * such as those provided by `HashMap`.
     *
     * @return a hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = error.hashCode()
        result = 31 * result + verifications.contentHashCode()
        result = 31 * result + (keyInfo?.hashCode() ?: 0)
        return result
    }

    /**
     * Returns a string representation of the VerifyResults instance.
     *
     * @return A string describing the VerifyResults object, including the error status, array of verifications, and key information.
     */
    override fun toString(): String {
        return "VerifyResults(error=$error, verifications=${verifications.contentToString()}, keyInfo=$keyInfo)"
    }


}


/**
 * Represents the result of a verification process.
 *
 * @property name The name of the verification.
 * @property error A flag indicating whether an error occurred.
 * @property message An optional message providing additional details about the verification.
 * @property critical A flag indicating whether the verification result is critical.
 */
@Serializable
@JsExport
open class VerifyResult(
    override val name: String,
    override val error: Boolean,
    override val message: String? = null,
    override val critical: Boolean = true
) : IVerifyResult {

    /**
     * Returns a string representation of the VerifyResult object.
     * @return a string containing the name, error status, message, and critical flag of the VerifyResult.
     */
    override fun toString(): String {
        return "VerifyResult(name='$name', error=$error, message=$message, critical=$critical)"
    }

    /**
     * Compares this object with the specified object for equality.
     *
     * @param other the object to be compared for equality with this object.
     * @return `true` if the specified object is equal to this object, `false` otherwise.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifyResult) return false

        if (name != other.name) return false
        if (error != other.error) return false
        if (message != other.message) return false
        if (critical != other.critical) return false

        return true
    }

    /**
     * Returns the hash code value for this object.
     * The hash code is computed based on the `name`, `error`, `message`,
     * and `critical` properties.
     *
     * @return an integer representing the hash code value of the object
     */
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + error.hashCode()
        result = 31 * result + (message?.hashCode() ?: 0)
        result = 31 * result + critical.hashCode()
        return result
    }

    /**
     * The `Static` object provides utility functions for working with `IVerifyResult` instances.
     */
    object Static {
        /**
         * Converts an instance of IVerifyResult to a VerifyResult.
         *
         * @param dto The IVerifyResult instance to be converted.
         * @return A new VerifyResult instance containing the data from the given IVerifyResult.
         */
        fun fromDTO(dto: IVerifyResult) = with(dto) { VerifyResult(name = name, error = error, message = message, critical = critical) }
    }
}


/**
 * Interface representing the result of a signature verification process.
 *
 * Provides information about the outcome of the verification, including
 * details about the cryptographic key used in the process.
 *
 * @param KeyType The specific type of key implementing the IKey interface.
 */
expect interface IVerifySignatureResult<out KeyType : IKey> : IVerifyResult {
    /**
     * Represents information related to a specific key.
     *
     * This variable holds an instance of `IKeyInfo<KeyType>`, which provides metadata and functionality
     * associated with a particular key type. The key type is defined by the generic `KeyType` parameter.
     * The variable may also be null, indicating the absence of key information.
     */
    val keyInfo: IKeyInfo<KeyType>?
}

/**
 * Represents the result of a signature verification process.
 *
 * @param KT The type of the key implementing the IKey interface.
 * @property error Boolean flag indicating if there was an error during verification.
 * @property name The name associated with the verification result.
 * @property critical Boolean flag indicating if the verification result is critical.
 * @property message Optional message providing additional information about the verification result.
 * @property keyInfo Optional key information relevant to the verification process.
 */
@JsExport
class VerifySignatureResult<out KT : IKey>(
    error: Boolean,
    name: String,
    critical: Boolean,
    message: String?,
    override val keyInfo: IKeyInfo<KT>?
) : IVerifySignatureResult<KT>, VerifyResult(error = error, name = name, critical = critical, message = message) {
    /**
     * Determines whether the specified object is equal to this instance.
     *
     * @param other The object to compare with the current object.
     * @return true if the specified object is equal to the current object; otherwise, false.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VerifySignatureResult<*>) return false
        if (!super.equals(other)) return false

        if (keyInfo != other.keyInfo) return false

        return true
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hash tables.
     *
     * @return a hash code value for this object.
     */
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (keyInfo?.hashCode() ?: 0)
        return result
    }

    /**
     * Returns a string representation of the VerifySignatureResult object.
     *
     * @return A string that includes the key information of this VerifySignatureResult.
     */
    override fun toString(): String {
        return "VerifySignatureResult(keyInfo=$keyInfo)"
    }


}
