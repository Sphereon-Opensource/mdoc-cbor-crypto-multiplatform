package com.sphereon.crypto.sign.client.enums

enum class TimestampedObjectType {
    /** Original document (signed data) */
    SIGNED_DATA,

    /** Signature */
    SIGNATURE,

    /** Certificate */
    CERTIFICATE,

    /** Revocation data */
//    REVOCATION,

    /** Timestamp */
    TIMESTAMP;
}
