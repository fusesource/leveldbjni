package org.fusesource.leveldbjni;

/**
 * Provides a java interface to the C++ leveldb::CompressionType enum.
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public enum CompressionType {
    kNoCompression(0x0), kSnappyCompression(0x1);

    static final int t = kNoCompression.value;
    final int value;

    CompressionType(int value) {
        this.value = value;
    }
}
