@file:Suppress("MemberVisibilityCanBePrivate")

package com.sphereon.crypto.sign.client.pki

import com.mayakapps.kache.InMemoryKache
import com.mayakapps.kache.KacheStrategy
import com.mayakapps.kache.ObjectKache
import com.sphereon.kmp.Logger
import kotlin.time.DurationUnit
import kotlin.time.toDuration

private const val MAX_CACHE_SIZE = 1024L
private val logger = Logger("sphereon:kmp:cache")

open class CacheService<K : Any, V : Any>(
    private val cacheName: String,
    private val cacheEnabled: Boolean? = true,
    private val cacheTTLInSeconds: Long? = 600,
) {
    private var cache: ObjectKache<K, V>? = null

    init {
        initCache()
    }

    fun get(key: K): V? {
        if (!isEnabled()) {
            return null
        }
        val value = cache!!.getIfAvailable(key)
        logger.debug("Cache ${value?.let { "HIT" } ?: "MIS"} for key '$key' in cache '$cacheName'" )
        return value
    }

    suspend fun getAsync(key: K): V? {
        if (!isEnabled()) {
            return null
        }
        val value = cache!!.get(key)
        logger.debug ( "Cache ${value?.let { "HIT" } ?: "MIS"} for key '$key' in cache '$cacheName'" )
        return value
    }

    fun isEnabled(): Boolean {
        return cacheEnabled == true && cache != null
    }


    suspend fun putAsync(key: K, value: V): V {
        if (isEnabled()) {
            logger.debug( "Caching value for key $key" )
            cache!!.put(key, value)
            if (cache!!.get(key) == null) {
                throw RuntimeException("Item was not placed in the cache")
            }
        }
        return value
    }

    private fun initCache() {
        logger.info ( "Cache '$cacheName' is ${if (cacheEnabled == true) "" else "NOT"} being enabled..." )
        if (cacheEnabled == true) {
            if (cache == null) {
                cache = InMemoryKache(maxSize = MAX_CACHE_SIZE) {
                    strategy = KacheStrategy.LRU
                    expireAfterAccessDuration = cacheTTLInSeconds!!.toDuration(DurationUnit.SECONDS)
                }
                logger.info ( "Cache '$cacheName' now is enabled" )
            }
        }
    }
}
