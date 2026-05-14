/*
 * Copyright (C) 2025 The Android Open Source Project
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

package com.android.dx.mockito.inline.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mockSingleton;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockedSingleton;

@RunWith(AndroidJUnit4.class)
public class MockSingleton {

    enum SingleEnum {
        VALUE_A {
            @Override
            String greeting() {
                return "hello";
            }
        },
        VALUE_B {
            @Override
            String greeting() {
                return "world";
            }
        };

        abstract String greeting();

        int compute(int x) {
            return x + 1;
        }
    }

    static class SimpleSingleton {
        static final SimpleSingleton INSTANCE = new SimpleSingleton();

        String getValue() {
            return "original";
        }

        int add(int a, int b) {
            return a + b;
        }
    }

    @Test
    public void mockEnumSingleton() {
        assertEquals("hello", SingleEnum.VALUE_A.greeting());

        try (MockedSingleton<SingleEnum> mocked = mockSingleton(SingleEnum.VALUE_A)) {
            when(SingleEnum.VALUE_A.greeting()).thenReturn("mocked");
            assertEquals("mocked", SingleEnum.VALUE_A.greeting());
        }

        assertEquals("hello", SingleEnum.VALUE_A.greeting());
    }

    @Test
    public void mockEnumDoesNotAffectOtherValues() {
        try (MockedSingleton<SingleEnum> mocked = mockSingleton(SingleEnum.VALUE_A)) {
            when(SingleEnum.VALUE_A.greeting()).thenReturn("mocked");

            assertEquals("mocked", SingleEnum.VALUE_A.greeting());
            assertEquals("world", SingleEnum.VALUE_B.greeting());
        }
    }

    @Test
    public void mockSingletonInstance() {
        assertEquals("original", SimpleSingleton.INSTANCE.getValue());

        try (MockedSingleton<SimpleSingleton> mocked = mockSingleton(SimpleSingleton.INSTANCE)) {
            when(SimpleSingleton.INSTANCE.getValue()).thenReturn("mocked");
            assertEquals("mocked", SimpleSingleton.INSTANCE.getValue());
        }

        assertEquals("original", SimpleSingleton.INSTANCE.getValue());
    }

    @Test
    public void getInstanceReturnsSameObject() {
        try (MockedSingleton<SingleEnum> mocked = mockSingleton(SingleEnum.VALUE_A)) {
            assertSame(SingleEnum.VALUE_A, mocked.getInstance());
        }
    }

    @Test
    public void resetClearsStubs() {
        try (MockedSingleton<SingleEnum> mocked = mockSingleton(SingleEnum.VALUE_A)) {
            when(SingleEnum.VALUE_A.greeting()).thenReturn("mocked");
            assertEquals("mocked", SingleEnum.VALUE_A.greeting());

            reset(SingleEnum.VALUE_A);
            // After reset, default mock behavior returns null for objects
            assertEquals(null, SingleEnum.VALUE_A.greeting());
        }
    }

    @Test
    public void verifyInteraction() {
        try (MockedSingleton<SimpleSingleton> mocked = mockSingleton(SimpleSingleton.INSTANCE)) {
            SimpleSingleton.INSTANCE.getValue();

            verify(SimpleSingleton.INSTANCE).getValue();
        }
    }

    @Test
    public void stubMethodWithArgs() {
        try (MockedSingleton<SimpleSingleton> mocked = mockSingleton(SimpleSingleton.INSTANCE)) {
            when(SimpleSingleton.INSTANCE.add(2, 3)).thenReturn(99);

            assertEquals(99, SimpleSingleton.INSTANCE.add(2, 3));
            assertEquals(0, SimpleSingleton.INSTANCE.add(1, 1));
        }

        assertEquals(2, SimpleSingleton.INSTANCE.add(1, 1));
    }
}
