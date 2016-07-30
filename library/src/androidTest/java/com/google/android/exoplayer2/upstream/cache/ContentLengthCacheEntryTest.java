package com.google.android.exoplayer2.upstream.cache;

import android.test.InstrumentationTestCase;

import java.io.File;

/**
 * @author Ornithopter on 2016/7/30.
 */
public class ContentLengthCacheEntryTest extends InstrumentationTestCase {
    private File getCacheDir() {
        return new File(getInstrumentation().getContext().getCacheDir(), "content_length_cache");
    }

    public void testParseCacheEntry() throws Exception {
        ContentLengthCacheEntry cacheEntry = ContentLengthCacheEntry.createCacheEntry(
                getCacheDir(), "test://key.testTouch", 1);
        cacheEntry.file.createNewFile();

        ContentLengthCacheEntry sameEntry = ContentLengthCacheEntry.createCacheEntry(cacheEntry.file);

        assertEquals(cacheEntry, sameEntry);
    }

    public void testTouch() throws Exception {
        ContentLengthCacheEntry cacheEntry = ContentLengthCacheEntry.createCacheEntry(
                getCacheDir(), "test://key.testTouch", 1);
        cacheEntry.file.createNewFile();

        Thread.sleep(2);

        ContentLengthCacheEntry touched = cacheEntry.touch();
        assertTrue(!touched.isExpired(1));

    }
}