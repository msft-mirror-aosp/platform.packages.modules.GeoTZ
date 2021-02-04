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

package com.android.timezone.location.storage.testing;

import org.junit.Assert;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestSupport {

    private TestSupport() {
    }

    @FunctionalInterface
    public interface Testable {

        void run() throws IOException;
    }

    public static void assertThrowsIllegalArgumentException(Testable runnable) {
        try {
            runnable.run();
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    public static void assertThrowsIllegalStateException(Testable runnable) {
        try {
            runnable.run();
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException expected) {
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    public static void assertThrowsIndexOutOfBoundsException(Testable runnable) {
        try {
            runnable.run();
            Assert.fail("Expected IllegalArgumentException");
        } catch (IndexOutOfBoundsException expected) {
        } catch (Exception e) {
            Assert.fail("Unexpected exception: " + e.getMessage());
        }
    }

    @SafeVarargs
    public static <E> List<E> listOf(E... values) {
        return Arrays.asList(values);
    }

    @SafeVarargs
    public static <E> Set<E> setOf(E... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
