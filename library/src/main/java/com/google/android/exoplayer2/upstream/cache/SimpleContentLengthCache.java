package com.google.android.exoplayer2.upstream.cache;

import android.annotation.SuppressLint;
import android.os.ConditionVariable;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.exoplayer2.C;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Ornithopter on 2016/7/28.
 */
@SuppressLint("LongLogTag")
public class SimpleContentLengthCache implements ContentLengthCache {
    private static final String TAG = "SimpleContentLengthCache";

    private final long expiration;
    private final File rootDir;
    private final ConcurrentHashMap<String, ContentLengthCacheEntry> cacheRecord;

    public SimpleContentLengthCache(File dir, long expiration) {
        this.expiration = expiration;
        this.rootDir = dir;
        this.cacheRecord = new ConcurrentHashMap<>();
        // Start cache initialization.
        final ConditionVariable conditionVariable = new ConditionVariable();
        new Thread("SimpleCache.initialize()") {
            @Override
            public void run() {
                synchronized (SimpleContentLengthCache.this) {
                    initialize();
                    conditionVariable.open();
                }
            }
        }.start();
        conditionVariable.block();
    }

    @Override
    public long get(String key, int defaultValue) {
        if (!cacheRecord.containsKey(key)) {
            Log.d(TAG, "get: return default value: " + defaultValue);
            return defaultValue;
        }
        ContentLengthCacheEntry cacheSpan = cacheRecord.get(key);
        if (cacheSpan == null) {
            return C.LENGTH_UNBOUNDED;
        }
        if (cacheSpan.isExpired(expiration)) {
            Log.w(TAG, "get: cache expired!");
            return C.LENGTH_UNBOUNDED;
        }
        Log.d(TAG, "get: found cache: " + cacheSpan.key);
        return cacheSpan.length;
    }

    @Override
    public void commit(String key, long length) {
        ContentLengthCacheEntry cacheSpan = cacheRecord.get(key);
        if (cacheSpan == null
                || !cacheSpan.file.exists()
                || cacheSpan.length != length) {
            commitNew(key, length);
            return;
        }

        cacheRecord.put(key, cacheSpan.touch());
    }

    @Override
    public void remove(String key) {
        ContentLengthCacheEntry cacheSpan = cacheRecord.remove(key);
        if (cacheSpan == null) {
            return;
        }
        cacheSpan.file.delete();
    }

    @Override
    public void clear() {
        cacheRecord.clear();
        deleteRecursive(rootDir);
    }

    private static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        return fileOrDirectory.delete();
    }

    private ContentLengthCacheEntry commitNew(String uri, long length) {
        ContentLengthCacheEntry cacheSpan = ContentLengthCacheEntry.createCacheEntry(rootDir, uri, length);
        try {
            cacheSpan.file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "commitNew: failed to create contentLength cache file!", e);
        }
        cacheRecord.put(uri, cacheSpan);
        return cacheSpan;
    }


    private void initialize() {
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }
        File[] files = rootDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!addRecord(file)) {
                file.delete();
            }
        }
    }

    private boolean addRecord(File file) {
        String escapedRecord = file.getName();
        if (TextUtils.isEmpty(escapedRecord)) {
            Log.e(TAG, "addRecord: empty file name?!");
            return false;
        }

        if (!escapedRecord.endsWith(ContentLengthCacheEntry.SUFFIX)) {
            Log.e(TAG, "addRecord: file not ended with " + ContentLengthCacheEntry.SUFFIX + "!");
            return false;
        }

        ContentLengthCacheEntry cacheSpan = null;
        try {
            cacheSpan = ContentLengthCacheEntry.createCacheEntry(file);
        } catch (NumberFormatException e) {
            Log.e(TAG, "addRecord: failed to parse file: " + file, e);
        }
        if (cacheSpan == null) {
            Log.d(TAG, "addRecord: bypass file: " + file.getAbsolutePath());
            return false;
        }
        cacheRecord.put(cacheSpan.key, cacheSpan);
        return true;
    }
}
