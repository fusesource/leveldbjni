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

import org.fusesource.leveldbjni.internal.*;
import org.iq80.leveldb.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class JniDBFactory implements DBFactory {

    public static final JniDBFactory factory = new JniDBFactory();

    public static byte[] bytes(String value) {
        if( value == null) {
            return null;
        }
        try {
            return value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String asString(byte value[]) {
        if( value == null) {
            return null;
        }
        try {
            return new String(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public DB open(File path, Options options) throws IOException {
        NativeDB db=null;
        NativeCache cache = null;
        NativeComparator comparator=null;
        NativeLogger logger=null;
        try {
            NativeOptions o = new NativeOptions();
            o.blockRestartInterval(options.getBlockRestartInterval());
            o.blockSize(options.getBlockSize());
            o.createIfMissing(options.isCreateIfMissing());
            o.errorIfExists(options.isErrorIfExists());
            o.maxOpenFiles(options.getMaxOpenFiles());
            o.paranoidChecks(options.isParanoidChecks());
            o.writeBufferSize(options.getWriteBufferSize());

            switch(options.getCompressionType()) {
                case NONE:
                    o.compression(NativeCompressionType.kNoCompression);
                    break;
                case SNAPPY:
                    o.compression(NativeCompressionType.kSnappyCompression);
                    break;
            }


            if(options.getCacheSize()>0 ) {
                cache = new NativeCache(options.getCacheSize());
                o.cache(cache);
            }

            final DBComparator userComparator = options.getComparator();
            if(userComparator!=null) {
                comparator = new NativeComparator() {
                    @Override
                    public int compare(byte[] key1, byte[] key2) {
                        return userComparator.compare(key1, key2);
                    }

                    @Override
                    public String name() {
                        return userComparator.name();
                    }
                };
                o.comparator(comparator);
            }

            final Logger userLogger = options.getLogger();
            if(userLogger!=null) {
                logger = new NativeLogger() {
                    @Override
                    public void log(String message) {
                        userLogger.log(message);
                    }
                };
                o.infoLog(logger);
            }

            db = NativeDB.open(o, path);

        } finally {
            // if we could not open up the DB, then clean up the
            // other allocated native resouces..
            if(db==null) {
                if(cache!=null) {
                    cache.delete();
                }
                if(comparator!=null){
                    comparator.delete();
                }
                if(logger!=null) {
                    logger.delete();
                }
            }
        }
        return new JniDB(db, cache, comparator, logger);
    }

    public void destroy(File path, Options options) throws IOException {
    }

    public void repair(File path, Options options) throws IOException {
    }

}
