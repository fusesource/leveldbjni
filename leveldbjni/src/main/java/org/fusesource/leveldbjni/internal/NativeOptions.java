/*
 * Copyright (C) 2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */
package org.fusesource.leveldbjni.internal;

import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.JniMethod;

import static org.fusesource.hawtjni.runtime.ClassFlag.CPP;
import static org.fusesource.hawtjni.runtime.ClassFlag.STRUCT;
import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.FieldFlag.FIELD_SKIP;
import static org.fusesource.hawtjni.runtime.MethodFlag.CONSTANT_INITIALIZER;

/**
 * Provides a java interface to the C++ leveldb::Options class.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(name="leveldb::Options", flags={STRUCT, CPP})
public class NativeOptions {

    static {
        NativeDB.LIBRARY.load();
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

    @JniField(flags={FIELD_SKIP})
    private NativeComparator comparatorObject = NativeComparator.BYTEWISE_COMPARATOR;
    @JniField(cast="const leveldb::Comparator*")
    private long comparator = comparatorObject.pointer();

    @JniField(flags={FIELD_SKIP})
    private NativeLogger infoLogObject = null;
    @JniField(cast="leveldb::Logger*")
    private long info_log = 0;

    @JniField(cast="leveldb::Env*")
    private long env = DEFAULT_ENV;
    @JniField(cast="leveldb::Cache*")
    private long block_cache = 0;
    @JniField(flags={FIELD_SKIP})
    private NativeCache cache;

    @JniField(cast="leveldb::CompressionType")
    private int compression = NativeCompressionType.kSnappyCompression.value;

    public NativeOptions createIfMissing(boolean value) {
        this.create_if_missing = value;
        return this;
    }
    public boolean createIfMissing() {
        return create_if_missing;
    }

    public NativeOptions errorIfExists(boolean value) {
        this.error_if_exists = value;
        return this;
    }
    public boolean errorIfExists() {
        return error_if_exists;
    }

    public NativeOptions paranoidChecks(boolean value) {
        this.paranoid_checks = value;
        return this;
    }
    public boolean paranoidChecks() {
        return paranoid_checks;
    }

    public NativeOptions writeBufferSize(long value) {
        this.write_buffer_size = value;
        return this;
    }
    public long writeBufferSize() {
        return write_buffer_size;
    }

    public NativeOptions maxOpenFiles(int value) {
        this.max_open_files = value;
        return this;
    }
    public int maxOpenFiles() {
        return max_open_files;
    }

    public NativeOptions blockRestartInterval(int value) {
        this.block_restart_interval = value;
        return this;
    }
    public int blockRestartInterval() {
        return block_restart_interval;
    }

    public NativeOptions blockSize(long value) {
        this.block_size = value;
        return this;
    }
    public long blockSize() {
        return block_size;
    }

//    @JniField(cast="Env*")
//    private long env = DEFAULT_ENV;

    public NativeComparator comparator() {
        return comparatorObject;
    }

    public NativeOptions comparator(NativeComparator comparator) {
        if( comparator==null ) {
            throw new IllegalArgumentException("comparator cannot be null");
        }
        this.comparatorObject = comparator;
        this.comparator = comparator.pointer();
        return this;
    }

    public NativeLogger infoLog() {
        return infoLogObject;
    }

    public NativeOptions infoLog(NativeLogger logger) {
        this.infoLogObject = logger;
        if( logger ==null ) {
            this.info_log = 0;
        } else {
            this.info_log = logger.pointer();
        }
        return this;
    }

    public NativeCompressionType compression() {
        if(compression == NativeCompressionType.kNoCompression.value) {
            return NativeCompressionType.kNoCompression;
        } else if(compression == NativeCompressionType.kSnappyCompression.value) {
            return NativeCompressionType.kSnappyCompression;
        } else {
            return NativeCompressionType.kSnappyCompression;
        }
    }

    public NativeOptions compression(NativeCompressionType compression) {
        this.compression = compression.value;
        return this;
    }

    public NativeCache cache() {
        return cache;
    }

    public NativeOptions cache(NativeCache cache) {
        this.cache = cache;
        if( cache!=null ) {
            this.block_cache = cache.pointer();
        } else {
            this.block_cache = 0;
        }
        return this;
    }
}
