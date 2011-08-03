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

import org.fusesource.hawtjni.runtime.ClassFlag;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.JniMethod;

import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.MethodFlag.CONSTANT_INITIALIZER;

/**
 * Provides a java interface to the C++ leveldb::Options class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::Options", flags={ClassFlag.STRUCT, ClassFlag.CPP})
public class Options {

    static {
        DB.LIBRARY.load();
        init();
    }

    @JniMethod(flags={CONSTANT_INITIALIZER})
    private static final native void init();

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

    @JniField(ignore = true)
    private Comparator comparatorObject = Comparator.BYTEWISE_COMPARATOR;
    @JniField(cast="const leveldb::Comparator*")
    private long comparator = comparatorObject.pointer();

    @JniField(ignore = true)
    private Logger infoLogObject = null;
    @JniField(cast="leveldb::Logger*")
    private long info_log = 0;

    @JniField(cast="leveldb::Env*")
    private long env = DEFAULT_ENV;
    @JniField(cast="leveldb::Cache*")
    private long block_cache = 0;
    @JniField(ignore = true)
    private Cache cache;

    @JniField(cast="leveldb::CompressionType")
    private int compression = CompressionType.kSnappyCompression.value;

    public Options createIfMissing(boolean value) {
        this.create_if_missing = value;
        return this;
    }
    public boolean createIfMissing() {
        return create_if_missing;
    }

    public Options errorIfExists(boolean value) {
        this.error_if_exists = value;
        return this;
    }
    public boolean errorIfExists() {
        return error_if_exists;
    }

    public Options paranoidChecks(boolean value) {
        this.paranoid_checks = value;
        return this;
    }
    public boolean paranoidChecks() {
        return paranoid_checks;
    }

    public Options writeBufferSize(long value) {
        this.write_buffer_size = value;
        return this;
    }
    public long writeBufferSize() {
        return write_buffer_size;
    }

    public Options maxOpenFiles(int value) {
        this.max_open_files = value;
        return this;
    }
    public int maxOpenFiles() {
        return max_open_files;
    }

    public Options blockRestartInterval(int value) {
        this.block_restart_interval = value;
        return this;
    }
    public int blockRestartInterval() {
        return block_restart_interval;
    }

    public Options blockSize(long value) {
        this.block_size = value;
        return this;
    }
    public long blockSize() {
        return block_size;
    }

//    @JniField(cast="Env*")
//    private long env = DEFAULT_ENV;

    public Comparator comparator() {
        return comparatorObject;
    }

    public Options comparator(Comparator comparator) {
        if( comparator==null ) {
            throw new IllegalArgumentException("comparator cannot be null");
        }
        this.comparatorObject = comparator;
        this.comparator = comparator.pointer();
        return this;
    }

    public Logger infoLog() {
        return infoLogObject;
    }

    public Options infoLog(Logger logger) {
        this.infoLogObject = logger;
        if( logger ==null ) {
            this.info_log = 0;
        } else {
            this.info_log = logger.pointer();
        }
        return this;
    }

    public CompressionType compression() {
        if(compression == CompressionType.kNoCompression.value) {
            return CompressionType.kNoCompression;
        } else if(compression == CompressionType.kSnappyCompression.value) {
            return CompressionType.kSnappyCompression;
        } else {
            return CompressionType.kSnappyCompression;
        }
    }

    public Options compression(CompressionType compression) {
        this.compression = compression.value;
        return this;
    }

    public Cache cache() {
        return cache;
    }

    public Options cache(Cache cache) {
        this.cache = cache;
        if( cache!=null ) {
            this.block_cache = cache.pointer();
        } else {
            this.block_cache = 0;
        }
        return this;
    }
}
