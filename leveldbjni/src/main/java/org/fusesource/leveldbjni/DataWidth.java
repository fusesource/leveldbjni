/*
 * Copyright (C) 2012, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 *    * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *    * Neither the name of FuseSource Corp. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.fusesource.leveldbjni;

import java.nio.ByteBuffer;

/**
 * This interface allows us to determine the width of given keys/values in a chunked
 * representation based on the current byte stream.
 */
abstract public class DataWidth {
    // Make constructor private to prevent misuse
    DataWidth() {};
    
    /**
     * Compute the width of the field from the provided stream. After
     * this method is called the stream should be positioned at the 
     * start of the value, so implementers will likely want to use
     * mark/reset if the width is encoded in the content itself.
     */
    public abstract int getWidth(ByteBuffer stream);

    /**
     * Indicates whether to run-length encode the width of the value
     * using a 32-bit big-endian integer.
     */
    public boolean isRunLengthEncoded() {
        return false;
    }

    /**
     * Indicates the fixed width of the value. Zero indicates to use whatever the full
     * width of the value is as returned by leveldb.
     */
    public int getEncodingWidth() {
        return 0;
    }

    // Convenience (syntax) methods/properties

    /** Marker for Variable-width key/value data encoding */
    public final static DataWidth VARIABLE = new VariableWidth();

    /** Convenience method for fixed-width key/value data encoding */
    public final static DataWidth FIXED(int width) {
        return new FixedWidth(width);
    }
}