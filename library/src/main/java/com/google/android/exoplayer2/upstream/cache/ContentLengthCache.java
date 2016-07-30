package com.google.android.exoplayer2.upstream.cache;

/**
 * An interface for caching Content-Length.
 * <p/>
 * This is used to support caching for unbounded requests
 *
 * @author Ornithopter on 2016/7/30.
 */
public interface ContentLengthCache {
    /**
     * @return Cached content length or {@code defaultValue} if not found.
     */
    long get(String key, int defaultValue);

    /**
     * Commit a content length for {@code key}.
     *
     * @param key    key
     * @param length Actual content length
     */
    void commit(String key, long length);

    /**
     * Remove the cached length for {@code key}.
     */
    void remove(String key);

    /**
     * Clear all caches.
     */
    void clear();
}
