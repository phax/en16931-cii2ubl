/*
 * Copyright (C) 2019-2026 Philip Helger
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
package com.helger.en16931.cii2ubl.en2026;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.string.StringHelper;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.CommonsLinkedHashSet;
import com.helger.collection.commons.CommonsTreeSet;
import com.helger.collection.commons.ICommonsOrderedSet;
import com.helger.collection.commons.ICommonsSortedSet;
import com.helger.io.file.SimpleFileIO;

/**
 * Ensures that every business term of the EN 16931:2026 mapping table is accounted for in
 * {@link CIID25AToUBL25Converter}.<br>
 * The check is deliberately crude - it only verifies that each identifier is named somewhere in the
 * converter source. That is enough to catch a whole business term being forgotten, which is exactly
 * what happened with BT-122-1: it is not listed in the "Business Terms and Groups New in 2026"
 * table of the mapping document, so it slipped through the section by section implementation.
 *
 * @author Philip Helger
 */
public final class MappingCoverageTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MappingCoverageTest.class);

  private static final File MAPPING_FILE = new File ("../docs/en16931-2026-syntax.md");
  private static final File CONVERTER_FILE = new File ("src/main/java/com/helger/en16931/cii2ubl/en2026/CIID25AToUBL25Converter.java");

  /**
   * Business terms that exist only in CII and have no UBL counterpart at all - the mapping table
   * shows "&mdash;" in both UBL columns. They are the UNTDID 2379 format codes of the CII date
   * elements plus BT-11-1 (Project name) and BT-17-1 (Tender or lot reference type code). There is
   * nothing to write to the UBL side for them.
   */
  private static final Set <String> CII_ONLY = new CommonsTreeSet <> (new CommonsArrayList <> ("BT-2-1",
                                                                                               "BT-7-1",
                                                                                               "BT-9-1",
                                                                                               "BT-11-1",
                                                                                               "BT-17-1",
                                                                                               "BT-26-1",
                                                                                               "BT-72-1",
                                                                                               "BT-73-1",
                                                                                               "BT-74-1",
                                                                                               "BT-134-1",
                                                                                               "BT-135-1",
                                                                                               "BT-166-1",
                                                                                               "BT-170-1",
                                                                                               "BT-181-1",
                                                                                               "BT-187-1",
                                                                                               "BT-218-1"));

  private static ICommonsOrderedSet <String> _getAllMappingRows ()
  {
    final String sMapping = SimpleFileIO.getFileAsString (MAPPING_FILE, StandardCharsets.UTF_8);
    assertTrue ("Failed to read " + MAPPING_FILE.getAbsolutePath (), StringHelper.isNotEmpty (sMapping));

    // Only real mapping rows have a cardinality column like "1..1" or "0..n"
    final ICommonsOrderedSet <String> ret = new CommonsLinkedHashSet <> ();
    final Matcher aMatcher = Pattern.compile ("^\\|\\s*(B[TG]-[0-9a-z\\-]+)\\s*\\|[^|]*\\|\\s*[0-9]\\.\\.[0-9n]\\s*\\|",
                                              Pattern.MULTILINE)
                                    .matcher (sMapping);
    while (aMatcher.find ())
      ret.add (aMatcher.group (1));
    return ret;
  }

  @Test
  public void testEveryMappingRowIsAccountedFor ()
  {
    final ICommonsOrderedSet <String> aAllRows = _getAllMappingRows ();
    assertTrue ("Suspiciously few mapping rows found: " + aAllRows.size (), aAllRows.size () > 250);

    final String sConverter = SimpleFileIO.getFileAsString (CONVERTER_FILE, StandardCharsets.UTF_8);
    assertTrue ("Failed to read " + CONVERTER_FILE.getAbsolutePath (), StringHelper.isNotEmpty (sConverter));

    final ICommonsSortedSet <String> aMissing = new CommonsTreeSet <> ();
    for (final String sID : aAllRows)
      if (!CII_ONLY.contains (sID))
      {
        // Match the identifier only when it is not the prefix of a longer one, so that "BT-12"
        // is not satisfied by an occurrence of "BT-122"
        if (!Pattern.compile (Pattern.quote (sID) + "(?![0-9\\-])").matcher (sConverter).find ())
          aMissing.add (sID);
      }

    LOGGER.info ("EN 16931:2026 mapping coverage: " +
                 (aAllRows.size () - aMissing.size ()) +
                 " of " +
                 aAllRows.size () +
                 " rows accounted for (" +
                 CII_ONLY.size () +
                 " of them are CII only)");

    if (aMissing.isNotEmpty ())
      fail ("The following business terms of the EN 16931:2026 mapping table are not named anywhere " +
            "in CIID25AToUBL25Converter. Either implement them, or add them to CII_ONLY if they " +
            "genuinely have no UBL counterpart: " +
            aMissing);
  }
}
