/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.storage.block.read;

import com.android.storage.io.read.TypedInputStream;

/**
 * Provides typed, absolute position, random access to data.
 *
 * <p>See also {@link TypedInputStream} for a streamed equivalent.
 */
public interface TypedData {

    /**
     * Returns a new read-only view into the data.
     *
     * @param startPos the start of the slice
     * @param length the length of the slice to create
     */
    TypedData slice(int startPos, int length);

    /** Returns the value of the byte at the specified position. */
    byte getByte(int byteOffset);

    /** Returns the value of the byte at the specified position as an unsigned value. */
    int getUnsignedByte(int byteOffset);

    /** Returns the value of the 16-bit char at the specified position as an unsigned value. */
    char getChar(int byteOffset);

    /** Returns the value of the 32-bit int at the specified position as an signed value. */
    int getInt(int byteOffset);

    /** Returns the value of the 64-bit long at the specified position as an signed value. */
    long getLong(int byteOffset);

    /**
     * Returns a tiny (<= 255 entries) array of signed bytes starting at the specified position,
     * where the length is encoded in the data.
     */
    byte[] getTinyByteArray(int byteOffset);

    /**
     * Returns an array of signed bytes starting at the specified position, where the 4-byte length
     * is encoded in the data.
     */
    byte[] getByteArray(int byteOffset);

    /**
     * Returns an array of signed bytes starting at the specified position.
     */
    byte[] getBytes(int byteOffset, int byteCount);

    /**
     * Returns a tiny (<= 255 entries) array of chars starting at the specified position, where the
     * length is encoded in the data.
     */
    char[] getTinyCharArray(int byteOffset);

    /**
     * Returns an array of chars starting at the specified position.
     */
    char[] getChars(int byteOffset, int charCount);

    /**
     * Returns 1-8 bytes ({@code valueSizeBytes}) starting as the specified position as a
     * {@code long}. The value can be interpreted as signed or unsigned depending on
     * {@code signExtend}.
     */
    long getValueAsLong(int valueSizeBytes, int byteOffset, boolean signExtend);

    /** Returns the size of the block data. */
    int getSize();
}
