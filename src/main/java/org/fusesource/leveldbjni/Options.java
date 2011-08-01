/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni;

import org.fusesource.hawtjni.runtime.*;
import sun.nio.cs.HistoricallyNamedCharset;

import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.MethodFlag.CONSTANT_INITIALIZER;

/**
 * Provides a java interface to the C++ leveldb::Options class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::Options", flags={ClassFlag.STRUCT, ClassFlag.CPP})
public class Options {

//    : comparator(BytewiseComparator()),
//      env(Env::Default()),
//      info_log(NULL),
//      block_cache(NULL),
//      compression(kSnappyCompression) {
    // Example of how to load constants.
    static {
        DB.LIBRARY.load();
        init();
    }

    @JniMethod(flags={CONSTANT_INITIALIZER})
    private static final native void init();

    @JniField(flags={CONSTANT}, cast="const Comparator*", accessor="leveldb::BytewiseComparator()")
    private static long BYTEWISE_COMPARATOR;

    @JniField(flags={CONSTANT}, cast="Env*", accessor="leveldb::Env::Default()")
    private static long DEFAULT_ENV;

    private boolean create_if_missing = false;
    private boolean error_if_exists = false;
    private boolean paranoid_checks = false;
    @JniField(cast="size_t")
    private long write_buffer_size = 4 << 20;
    @JniField(cast="size_t")
    private long block_size = 4086;
    private int max_open_files = 1000;
    private int block_restart_interval = 16;
    @JniField(cast="const leveldb::Comparator*")
    private long comparator = BYTEWISE_COMPARATOR;
    @JniField(cast="leveldb::Env*")
    private long env = DEFAULT_ENV;
    @JniField(cast="leveldb::Logger*")
    private long info_log = 0;
    @JniField(cast="leveldb::Cache*")
    private long block_cache = 0;
    @JniField(ignore = true)
    private Cache cache;

    @JniField(cast="leveldb::CompressionType")
    private int compression = CompressionType.kSnappyCompression.value;

    public void setCreateIfMissing(boolean value) {
        this.create_if_missing = value;
    }
    public boolean getCreateIfMissing() {
        return create_if_missing;
    }

    public void setErrorIfExists(boolean value) {
        this.error_if_exists = value;
    }
    public boolean getErrorIfExists() {
        return error_if_exists;
    }

    public void setParanoidChecks(boolean value) {
        this.paranoid_checks = value;
    }
    public boolean getParanoidChecks() {
        return paranoid_checks;
    }

    public void setWriteBufferSize(long value) {
        this.write_buffer_size = value;
    }
    public long getWriteBufferSize() {
        return write_buffer_size;
    }

    public void setMaxOpenFiles(int value) {
        this.max_open_files = value;
    }
    public int getMaxOpenFiles() {
        return max_open_files;
    }

    public void setBlockRestartInterval(int value) {
        this.block_restart_interval = value;
    }
    public int getBlockRestartInterval() {
        return block_restart_interval;
    }

    public void setBlockSize(long value) {
        this.block_size = value;
    }
    public long getBlockSize() {
        return block_size;
    }


//    @JniField(cast="const Comparator*")
//    private long comparator = BYTEWISE_COMPARATOR;
//    @JniField(cast="Env*")
//    private long env = DEFAULT_ENV;
//    @JniField(cast="Logger*")
//    private long info_log = 0;
//    @JniField(cast="Cache*")
//    private long block_cache = 0;
//    @JniField(cast="CompressionType")
//    private int compression = kSnappyCompression;


    public CompressionType getCompression() {
        if(compression == CompressionType.kNoCompression.value) {
            return CompressionType.kNoCompression;
        } else if(compression == CompressionType.kSnappyCompression.value) {
            return CompressionType.kSnappyCompression;
        } else {
            return CompressionType.kSnappyCompression;
        }
    }

    public void setCompression(CompressionType compression) {
        this.compression = compression.value;
    }

    public Cache getBlockCache() {
        return cache;
    }

    public void setBlockCache(Cache cache) {
        this.cache = cache;
        if( cache!=null ) {
            this.block_cache = cache.pointer();
        } else {
            this.block_cache = 0;
        }
    }
}
