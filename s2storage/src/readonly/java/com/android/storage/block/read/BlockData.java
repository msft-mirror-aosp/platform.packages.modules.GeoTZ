/*
 * Copyright (C) 2020 The Android Open Source Project
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
import com.android.storage.util.BitwiseUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Provides typed, absolute position, random access to a block's data.
 *
 * <p>See also {@link TypedInputStream} for a streamed equivalent.
 */
public final class BlockData implements TypedData {

    private final ByteBuffer mDataBytes;

    /** Wraps a read-only, big-endian {@link ByteBuffer}. */
    public BlockData(ByteBuffer dataBytes) {
        if (!dataBytes.isReadOnly()) {
            throw new IllegalArgumentException("dataBytes must be readonly");
        }
        if (dataBytes.order() != ByteOrder.BIG_ENDIAN) {
            throw new IllegalArgumentException("dataBytes must be big-endian");
        }
        mDataBytes = Objects.requireNonNull(dataBytes);
    }

    /** Returns a copy of the underlying {@link ByteBuffer}. */
    public ByteBuffer getByteBuffer() {
        ByteBuffer buffer = mDataBytes.duplicate();

        // mDataBytes shouldn't have a position set, but make sure the duplicate doesn't anyway.
        buffer.position(0);

        return buffer;
    }

    @Override
    public TypedData slice(int startPos, int length) {
        // None of this code is thread safe, but this is especially not thread safe because
        // it uses position / limit as part of the slicing, so synchronize.
        int newLimit = startPos + length;

        synchronized (mDataBytes) {
            // mDataBytes shouldn't have a position or mark, but preserve and reset them
            // again afterwards just in case.
            int oldPosition = mDataBytes.position();
            int oldLimit = mDataBytes.limit();

            // Avoid creating a new slice that could fail when accessed, e.g. because its limit
            // is outside of the original buffer.
            if (newLimit > oldLimit) {
                throw new IllegalArgumentException(
                        "startPos(" + startPos + ") + length(" + length + ") > size()");
            }

            mDataBytes.position(startPos);
            mDataBytes.limit(newLimit);
            ByteBuffer sliceByteBuffer = mDataBytes.slice();

            mDataBytes.limit(oldLimit);
            mDataBytes.position(oldPosition);

            return new BlockData(sliceByteBuffer);
        }
    }

    @Override
    public byte getByte(int byteOffset) {
        return mDataBytes.get(byteOffset);
    }

    @Override
    public int getUnsignedByte(int byteOffset) {
        return mDataBytes.get(byteOffset) & 0xFF;
    }

    @Override
    public char getChar(int byteOffset) {
        return mDataBytes.getChar(byteOffset);
    }

    @Override
    public int getInt(int byteOffset) {
        return mDataBytes.getInt(byteOffset);
    }

    @Override
    public long getLong(int byteOffset) {
        return mDataBytes.getLong(byteOffset);
    }

    @Override
    public byte[] getTinyByteArray(int byteOffset) {
        int size = getUnsignedByte(byteOffset);
        return getBytes(byteOffset + 1, size);
    }

    @Override
    public byte[] getByteArray(int byteOffset) {
        int size = getInt(byteOffset);
        return getBytes(byteOffset + Integer.BYTES, size);
    }

    @Override
    public byte[] getBytes(int byteOffset, int byteCount) {
        byte[] bytes = new byte[byteCount];
        for (int i = 0; i < byteCount; i++) {
            bytes[i] = mDataBytes.get(byteOffset + i);
        }
        return bytes;
    }

    @Override
    public char[] getTinyCharArray(int byteOffset) {
        int size = getUnsignedByte(byteOffset);
        return getChars(byteOffset + 1, size);
    }

    @Override
    public char[] getChars(int byteOffset, int charCount) {
        char[] array = new char[charCount];
        for (int i = 0; i < charCount; i++) {
            array[i] = getChar(byteOffset);
            byteOffset += Character.BYTES;
        }
        return array;
    }

    @Override
    public long getValueAsLong(int valueSizeBytes, int byteOffset, boolean signExtend) {
        if (valueSizeBytes < 0 || valueSizeBytes > Long.BYTES) {
            throw new IllegalArgumentException("valueSizeBytes must be <= 8 bytes");
        }
        return getValueInternal(valueSizeBytes, byteOffset, signExtend);
    }

    @Override
    public int getSize() {
        return mDataBytes.limit();
    }

    private long getValueInternal(int valueSizeBytes, int byteOffset, boolean signExtend) {
        if (byteOffset < 0) {
            throw new IllegalArgumentException(
                    "byteOffset=" + byteOffset + " must not be negative");
        }

        // High bytes read first.
        long value = 0;
        int bytesRead = 0;
        while (bytesRead++ < valueSizeBytes) {
            value <<= 8;
            value |= (mDataBytes.get(byteOffset++) & 0xFF);
        }
        if (valueSizeBytes < 8 && signExtend) {
            int entrySizeBits = valueSizeBytes * Byte.SIZE;
            value = BitwiseUtils.signExtendToLong(entrySizeBits, value);
        }
        return value;
    }
}
