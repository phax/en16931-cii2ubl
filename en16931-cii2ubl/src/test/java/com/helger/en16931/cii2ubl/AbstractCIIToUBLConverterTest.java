/**
 * Copyright (C) 2019-2021 Philip Helger
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.Month;

import org.junit.Test;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.error.list.ErrorList;

/**
 * Test class for class {@link AbstractCIIToUBLConverter}.
 *
 * @author Philip Helger
 */
public final class AbstractCIIToUBLConverterTest
{
  @Test
  public void testParseDate ()
  {
    final ErrorList aList = new ErrorList ();
    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6), AbstractCIIToUBLConverter._parseDate ("060705", "2", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6), AbstractCIIToUBLConverter._parseDate ("070605", "3", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6), AbstractCIIToUBLConverter._parseDate ("06072005", "4", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6), AbstractCIIToUBLConverter._parseDate ("050706", "101", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6), AbstractCIIToUBLConverter._parseDate ("20050706", "102", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JANUARY, 3), AbstractCIIToUBLConverter._parseDate ("050101", "103", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2019, Month.JANUARY, 5), AbstractCIIToUBLConverter._parseDate ("19005", "105", aList));
    assertTrue (aList.isEmpty ());

    // Unsupported format
    assertNull (AbstractCIIToUBLConverter._parseDate ("050101", "999", aList));
    assertFalse (aList.isEmpty ());
  }
}
