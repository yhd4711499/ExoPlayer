package com.google.android.exoplayer2.upstream.cache;

import android.util.Log;

import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Holding cache for Content-Length
 */
/*package*/ class ContentLengthCacheEntry {
    private static final String TAG = "ContentLengthCacheEntry";

    private static final Pattern CACHE_FILE_PATTERN =
            Pattern.compile("^(.+)\\.(\\d+)\\.(\\d+)\\.sclp$");

    static final String SUFFIX = ".sclp";

    /**
     * The cache key that uniquely identifies the original stream.
     */
    public final String key;
    /**
     * The length of the {@link CacheSpan}.
     */
    public final long length;
    /**
     * The file corresponding to this {@link CacheSpan}.
     */
    public final File file;
    /**
     * The last access timestamp.
     */
    public final long lastAccessTimestamp;

    static ContentLengthCacheEntry createCacheEntry(File file) throws NumberFormatException {
        Matcher matcher = CACHE_FILE_PATTERN.matcher(file.getName());
        if (!matcher.matches()) {
            Log.e(TAG, "createCacheEntry: file does not match CACHE_FILE_PATTERN!");
            return null;
        }

        String fileName = matcher.group(1);
        String key = Util.unescapeFileName(fileName);
        if (key == null) {
            Log.e(TAG, "createCacheEntry: can't find the key in : " + fileName);
            return null;
        }
        long length = Long.parseLong(matcher.group(2));
        long lastAccess = Long.parseLong(matcher.group(3));
        return new ContentLengthCacheEntry(key, length, lastAccess, file);
    }

    static ContentLengthCacheEntry createCacheEntry(File dir, String uri, long length) {
        long lastAccessTimestamp = System.currentTimeMillis();
        return new ContentLengthCacheEntry(
                uri,
                length,
                lastAccessTimestamp,
                ContentLengthCacheEntry.getCacheFileName(dir, uri, length, lastAccessTimestamp)
        );
    }

    public static File getCacheFileName(File dir, String key, long length, long lastAccess) {
        return new File(dir, Util.escapeFileName(key) + "." + length + "." + lastAccess + SUFFIX);
    }

    private ContentLengthCacheEntry(String key, long length, long lastAccessTimestamp, File file) {
        this.key = key;
        this.length = length;
        this.file = file;
        this.lastAccessTimestamp = lastAccessTimestamp;
    }

    /**
     * Renames the file underlying this cache span to update its last access time.
     *
     * @return A {@link ContentLengthCacheEntry} representing the updated cache file.
     */
    ContentLengthCacheEntry touch() {
        long now = System.currentTimeMillis();
        File newCacheFile = getCacheFileName(file.getParentFile(), key, length, now);
        file.renameTo(newCacheFile);
        return new ContentLengthCacheEntry(key, length, now, newCacheFile);
    }

    public boolean isExpired(long expiration) {
        return System.currentTimeMillis() - lastAccessTimestamp > expiration;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContentLengthCacheEntry)) {
            return false;
        }
        ContentLengthCacheEntry target = ((ContentLengthCacheEntry) o);
        if (key.equals(target.key)
                && length == target.length
                && lastAccessTimestamp == target.lastAccessTimestamp
                && file.equals(target.file)
        ) {
            return true;
        }
        return false;
    }
}
