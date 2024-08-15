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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThrows;

import com.android.storage.io.write.TypedOutputStream;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/** Tests {@link BlockData}. */
public class BlockDataTest {

    @Test
    public void getSize() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TypedOutputStream typedOutputStream = new TypedOutputStream(baos);
        byte[] tinyByteArray = "Tiny Byte Array".getBytes(StandardCharsets.UTF_8);
        typedOutputStream.writeTinyByteArray(tinyByteArray);
        typedOutputStream.writeVarByteValue(8, 0x123456789ABCDEF1L);
        typedOutputStream.writeVarByteValue(8, 0xFFFFFFFFFFFFFFFFL);
        typedOutputStream.close();

        byte[] blockDataBytes = baos.toByteArray();
        BlockData blockData = new BlockData(ByteBuffer.wrap(blockDataBytes).asReadOnlyBuffer());

        assertEquals(blockDataBytes.length, blockData.getSize());
    }

    @Test
    public void getByteBuffer() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TypedOutputStream typedOutputStream = new TypedOutputStream(baos);
        byte[] tinyByteArray = "Tiny Byte Array".getBytes(StandardCharsets.UTF_8);
        typedOutputStream.writeTinyByteArray(tinyByteArray);
        typedOutputStream.writeVarByteValue(8, 0x123456789ABCDEF1L);
        typedOutputStream.writeVarByteValue(8, 0xFFFFFFFFFFFFFFFFL);
        typedOutputStream.close();

        byte[] blockDataBytes = baos.toByteArray();
        ByteBuffer originalByteBuffer = ByteBuffer.wrap(blockDataBytes).asReadOnlyBuffer();
        BlockData blockData = new BlockData(originalByteBuffer);

        ByteBuffer byteBuffer = blockData.getByteBuffer();
        assertNotSame(originalByteBuffer, byteBuffer);
        assertEquals(0, byteBuffer.position());
        assertEquals(originalByteBuffer.limit(), byteBuffer.limit());
        assertEquals(blockDataBytes.length, byteBuffer.capacity());
    }

    @Test
    public void slice() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TypedOutputStream typedOutputStream = new TypedOutputStream(baos);
        byte[] tinyByteArray = "Tiny Byte Array".getBytes(StandardCharsets.UTF_8);
        typedOutputStream.writeTinyByteArray(tinyByteArray);
        typedOutputStream.close();

        byte[] blockDataBytes = baos.toByteArray();
        ByteBuffer originalByteBuffer = ByteBuffer.wrap(blockDataBytes).asReadOnlyBuffer();
        BlockData blockData = new BlockData(originalByteBuffer);

        assertSliceEmptyBehavior(blockData, 0, 0);
        assertSliceNonEmptyBehavior(blockData, 0, 1);
        assertSliceNonEmptyBehavior(blockData, 0, blockData.getSize());

        assertSliceEmptyBehavior(blockData, 1, 0);
        assertSliceNonEmptyBehavior(blockData, 1, 1);
        assertSliceNonEmptyBehavior(blockData, 1, blockData.getSize() - 1);

        assertSliceEmptyBehavior(blockData, blockData.getSize() - 2, 0);
        assertSliceNonEmptyBehavior(blockData, blockData.getSize() - 2, 1);
        assertSliceNonEmptyBehavior(blockData, blockData.getSize() - 2, 2);

        assertSliceEmptyBehavior(blockData, blockData.getSize() - 1, 0);
        assertSliceNonEmptyBehavior(blockData, blockData.getSize() - 1, 1);

        assertSliceEmptyBehavior(blockData, blockData.getSize(), 0);

        // Edge cases: length of slice puts the top of the new slice outside of the original buffer.
        assertSliceTypedDataBadArguments(blockData, -1, 0);
        assertSliceTypedDataBadArguments(blockData, 0, blockData.getSize() + 1);
        assertSliceTypedDataBadArguments(blockData, 0, blockData.getSize() * 2);
        assertSliceTypedDataBadArguments(blockData, 1, blockData.getSize());
        assertSliceTypedDataBadArguments(blockData, blockData.getSize() - 2, 3);
        assertSliceTypedDataBadArguments(blockData, blockData.getSize() - 1, 2);
        assertSliceTypedDataBadArguments(blockData, blockData.getSize(), 1);
    }

    private static void assertSliceEmptyBehavior(BlockData blockData, int offset, int length) {
        TypedData slice = blockData.slice(offset, length);
        assertEquals(0, slice.getSize());
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getInt(0));
    }

    private static void assertSliceNonEmptyBehavior(BlockData blockData, int offset, int length) {
        TypedData slice = blockData.slice(offset, length);
        assertEquals(length, slice.getSize());
        for (int i = 0; i < length; i++) {
            assertEquals(blockData.getByte(offset + i), slice.getByte(i));
        }
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getInt(length));
    }

    private static void assertSliceTypedDataBadArguments(
            BlockData blockData, int offset, int length) {
        assertThrows(IllegalArgumentException.class, () -> blockData.slice(offset, length));
    }

    @Test
    public void typedRandomAccess() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TypedOutputStream typedOutputStream = new TypedOutputStream(baos);
        byte[] tinyByteArray = "Tiny Byte Array".getBytes(StandardCharsets.UTF_8);
        typedOutputStream.writeTinyByteArray(tinyByteArray);
        typedOutputStream.writeVarByteValue(8, 0x123456789ABCDEF1L);
        typedOutputStream.writeVarByteValue(8, 0xFFFFFFFFFFFFFFFFL);
        typedOutputStream.writeUnsignedByte(129);
        typedOutputStream.writeInt(0xFEDCBA98);
        typedOutputStream.writeChar(0x9ABC);
        typedOutputStream.writeByte(-52);
        typedOutputStream.writeLong(0x123456789ABCDEF1L);
        char[] tinyCharArray = "Char array".toCharArray();
        typedOutputStream.writeTinyCharArray(tinyCharArray);
        byte[] bytes = "Bytes".getBytes(StandardCharsets.UTF_8);
        typedOutputStream.writeBytes(bytes);
        typedOutputStream.close();

        byte[] blockDataBytes = baos.toByteArray();
        BlockData blockData = new BlockData(ByteBuffer.wrap(blockDataBytes).asReadOnlyBuffer());
        assertArrayEquals(tinyByteArray, blockData.getTinyByteArray(0));
        int offset = 1 + tinyByteArray.length;

        assertEquals(0x123456789ABCDEF1L, blockData.getValueAsLong(8, offset, false));
        assertEquals(0x123456789ABCDEL, blockData.getValueAsLong(7, offset, false));
        assertEquals(0x123456789ABCL, blockData.getValueAsLong(6, offset, false));
        assertEquals(0x123456789AL, blockData.getValueAsLong(5, offset, false));
        assertEquals(0x12345678L, blockData.getValueAsLong(4, offset, false));
        assertEquals(0x123456L, blockData.getValueAsLong(3, offset, false));
        assertEquals(0x1234L, blockData.getValueAsLong(2, offset, false));
        assertEquals(0x12L, blockData.getValueAsLong(1, offset, false));

        offset += 8;

        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(8, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(7, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(6, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(5, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(4, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(3, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(3, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(2, offset, true));
        assertEquals(0xFFFFFFFFFFFFFFFFL, blockData.getValueAsLong(1, offset, true));

        offset += 8;

        assertEquals(129, blockData.getUnsignedByte(offset));
        offset++;

        assertEquals(0xFEDCBA98, blockData.getInt(offset));
        offset += 4;

        assertEquals(0x9ABC, blockData.getChar(offset));
        offset += 2;

        assertEquals(-52, blockData.getByte(offset));
        offset++;

        assertEquals(0x123456789ABCDEF1L, blockData.getLong(offset));
        offset += 8;

        assertArrayEquals(tinyCharArray, blockData.getTinyCharArray(offset));
        offset += 1 + (tinyCharArray.length * 2);

        assertArrayEquals(bytes, blockData.getBytes(offset, bytes.length));
    }
}
