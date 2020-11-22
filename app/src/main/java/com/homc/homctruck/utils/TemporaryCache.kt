package com.homc.homctruck.utils

import android.util.LruCache

/**
 * Acts as an ephemeral data structure.
 * Wrapper implementation of [LruCache] with initial size of 20
 * It is a reusable component that can be used to pass the data in/out classes just by using the memory.
 * Can be used in where the objects need to be made parcelable and then passed across into the intent.
 *
 */
object TemporaryCache {
    private val cache = LruCache<String, Any>(20)

    /**
     * Store temporary data into the cached for the parameter key.
     * @param key for against which the data is stored.
     * @param stuff The data to be stored. Marshalling and unmarshelling the data into the correct
     * type is the responsibility of the caller.
     * @return
     */
    fun put(key: String, stuff: Any): Boolean {
        try {
            cache.put(key, stuff)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
        return true
    }

    /**
     * Get the cached value.
     * @param key
     * @return
     */
    operator fun get(key: String): Any {
        return cache[key]
    }

    /**
     * Remove the cached value.
     * @param key
     * @return
     */
    fun remove(key: String?): Any? {
        return if (key == null) {
            null
        } else cache.remove(key)
    }
}