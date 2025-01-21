/*
 * Copyright (C) 2019-2025 Philip Helger
 * http://www.helger.com
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.en16931.cii2ubl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Test class for class {@link CIIToUBLVersion}
 *
 * @author Philip Helger
 */
public final class CIIToUBLVersionTest
{
  @Test
  public void testBasic ()
  {
    assertNotEquals ("undefined", CIIToUBLVersion.BUILD_VERSION);
    assertNotEquals ("undefined", CIIToUBLVersion.BUILD_TIMESTAMP);

    // Check variable resolution
    assertFalse (CIIToUBLVersion.BUILD_VERSION.contains ("${"));
    assertFalse (CIIToUBLVersion.BUILD_TIMESTAMP.contains ("${"));
  }
}
