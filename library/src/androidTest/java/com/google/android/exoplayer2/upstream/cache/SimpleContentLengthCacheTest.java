package com.google.android.exoplayer2.upstream.cache;

import android.test.InstrumentationTestCase;

import java.io.File;

/**
 * @author Ornithopter on 2016/7/30.
 */
public class SimpleContentLengthCacheTest extends InstrumentationTestCase {
        private final static long EXPIRATION = 10 * 60 * 1000;
    private SimpleContentLengthCache cache;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        cache = new SimpleContentLengthCache(getCacheDir(), EXPIRATION);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        cache.clear();
    }

    private File getCacheDir() {
        return new File(getInstrumentation().getContext().getCacheDir(), "content_length_cache");
    }

    public void testGet() throws Exception {
        assertTrue(cache.get("test://key.testGet", -1) == -1);
    }

    public void testCommit() throws Exception {
        String key = "test://key.testCommit";
        cache.commit(key, 1);
        assertTrue(cache.get(key, -1) == 1);

        SimpleContentLengthCache anotherCache = new SimpleContentLengthCache(getCacheDir(), EXPIRATION);
        assertTrue(anotherCache.get(key, -1) == 1);
    }

    public void testExpiration() throws Exception {
        String key = "test://key.testExpiration";
        cache = new SimpleContentLengthCache(getCacheDir(), 1);
        cache.commit(key, 1);
        Thread.sleep(2);
        assertTrue(cache.get(key, -1) == -1);
    }

    public void testRemove() throws Exception {
        cache.commit("test://key.testRemove", 1);
        cache.remove("test://key.testRemove");
        assertTrue(cache.get("test://key.testRemove", -1) == -1);

        SimpleContentLengthCache anotherCache = new SimpleContentLengthCache(getCacheDir(), EXPIRATION);
        assertTrue(anotherCache.get("test://key.testRemove", -1) == -1);
    }

    public void testClear() throws Exception {
        cache.commit("test://key.testClear", 1);
        cache.clear();
        assertTrue(cache.get("test://key.testClear", -1) == -1);
        assertTrue(!getCacheDir().exists());
    }
}