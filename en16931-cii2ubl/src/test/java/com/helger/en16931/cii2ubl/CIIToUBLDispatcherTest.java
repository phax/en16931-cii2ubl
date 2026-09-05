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
package com.helger.en16931.cii2ubl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;

import org.junit.Test;

import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.basics.EEN16931Edition;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test class for class {@link CIIToUBLDispatcher}. The BT-24 based detection of
 * {@link EEN16931Edition} itself is tested in en16931-basics; what is tested here is that it
 * classifies the test corpus of this project correctly.
 *
 * @author Philip Helger
 */
public final class CIIToUBLDispatcherTest
{
  private static final String D25A_TEST_DIR = "src/test/resources/external/cii-d25a/";

  @Test
  public void testDetectAll2017TestFiles ()
  {
    // Two of the existing test files carry a BT-24 that is not an EN 16931 identifier at all:
    // the legacy ZUGFeRD files use "urn:ferd:CrossIndustryDocument:invoice:1p0:comfort". They are
    // undeterminable by design and are exactly the case the explicit edition override exists for.
    final ICommonsList <String> aUndeterminable = new CommonsArrayList <> ();
    for (final File aFile : MockSettings.getAllTestFiles ())
    {
      final EEN16931Edition eEdition = EEN16931Edition.detect (aFile);
      if (eEdition == null)
        aUndeterminable.add (aFile.getName ());
      else
        assertEquals ("Wrong edition detected for " + aFile.getAbsolutePath (), EEN16931Edition.EN2017, eEdition);
    }
    assertEquals (aUndeterminable.toString (), 2, aUndeterminable.size ());
    assertTrue (aUndeterminable.contains ("CII_business_example_01.xml"));
    assertTrue (aUndeterminable.contains ("CII_business_example_02.xml"));

    // Forcing the edition converts them anyway
    final ErrorList aErrorList = new ErrorList ();
    final Serializable aUBL = new CIIToUBLDispatcher ().setEdition (EEN16931Edition.EN2017)
                                                      .convertCIItoUBL (new File (MockSettings.BASE_TEST_DIR,
                                                                                  "CII_business_example_01.xml"),
                                                                        aErrorList);
    assertTrue (aErrorList.toString (), aErrorList.containsNoError ());
    assertNotNull (aUBL);
  }

  @Test
  public void testDetect2026TestFiles ()
  {
    for (final String sFilename : new String [] { "d25a-minimal-invoice.xml",
                                                  "d25a-minimal-creditnote.xml",
                                                  "d25a-full-invoice.xml",
                                                  "d25a-new-header-invoice.xml" })
    {
      final File aFile = new File (D25A_TEST_DIR, sFilename);
      assertTrue ("Not existing: " + aFile.getAbsolutePath (), aFile.exists ());
      assertEquals (sFilename, EEN16931Edition.EN2026, EEN16931Edition.detect (aFile));
    }
  }

  @Test
  public void testDispatchAutomatic ()
  {
    // 2017 source yields a UBL 2.1 Invoice
    final ErrorList aErrorList = new ErrorList ();
    final Serializable aUBL = new CIIToUBLDispatcher ().convertCIItoUBL (new File (MockSettings.BASE_TEST_DIR,
                                                                                  "CII_example1.xml"),
                                                                        aErrorList);
    assertTrue (aErrorList.toString (), aErrorList.containsNoError ());
    assertNotNull (aUBL);
    assertTrue ("Expected a UBL 2.1 Invoice but got " + aUBL.getClass ().getName (), aUBL instanceof InvoiceType);

    // 2026 source yields a UBL 2.5 Invoice
    final ErrorList aErrorList2 = new ErrorList ();
    final Serializable aUBL2 = new CIIToUBLDispatcher ().convertCIItoUBL (new File (D25A_TEST_DIR,
                                                                                   "d25a-minimal-invoice.xml"),
                                                                         aErrorList2);
    assertTrue (aErrorList2.toString (), aErrorList2.containsNoError ());
    assertNotNull (aUBL2);
    assertTrue ("Expected a UBL 2.5 Invoice but got " + aUBL2.getClass ().getName (),
                aUBL2 instanceof oasis.names.specification.ubl.schema.xsd.invoice_25.InvoiceType);
  }

  @Test
  public void testDispatchExplicitEditionOverridesDetection ()
  {
    // Force 2017 on a file that would otherwise be detected as 2026. The D25A source is not
    // readable with the D16B model, so the conversion fails - but it proves detection was skipped.
    final ErrorList aErrorList = new ErrorList ();
    final CIIToUBLDispatcher aDispatcher = new CIIToUBLDispatcher ().setEdition (EEN16931Edition.EN2017);
    assertEquals (EEN16931Edition.EN2017, aDispatcher.getEdition ());
    aDispatcher.convertCIItoUBL (new File (D25A_TEST_DIR, "d25a-new-header-invoice.xml"), aErrorList);

    // Forcing 2026 on a 2026 file works
    final ErrorList aErrorList2 = new ErrorList ();
    final Serializable aUBL2 = new CIIToUBLDispatcher ().setEdition (EEN16931Edition.EN2026)
                                                       .convertCIItoUBL (new File (D25A_TEST_DIR,
                                                                                   "d25a-minimal-invoice.xml"),
                                                                         aErrorList2);
    assertTrue (aErrorList2.toString (), aErrorList2.containsNoError ());
    assertNotNull (aUBL2);
  }

  @Test
  public void testDispatchUndeterminableEdition ()
  {
    // pom.xml has no BT-24 at all
    final ErrorList aErrorList = new ErrorList ();
    final Serializable aUBL = new CIIToUBLDispatcher ().convertCIItoUBL (new File ("pom.xml"), aErrorList);
    assertNull (aUBL);
    assertFalse (aErrorList.containsNoError ());
    assertTrue (aErrorList.toString (), aErrorList.toString ().contains ("BT-24"));
  }

  @Test
  public void testDispatcherPassesSettingsOn ()
  {
    final CIIToUBLDispatcher aDispatcher = new CIIToUBLDispatcher ().setUBLCreationMode (EUBLCreationMode.CREDIT_NOTE)
                                                                   .setVATScheme ("MYVAT")
                                                                   .setCustomizationID ("my-cust")
                                                                   .setProfileID ("my-prof")
                                                                   .setCardAccountNetworkID ("my-card")
                                                                   .setDefaultOrderRefID ("my-order")
                                                                   .setSwapQuantitySignIfNeeded (false)
                                                                   .setSwapPriceSignIfNeeded (false);
    for (final EEN16931Edition e : EEN16931Edition.values ())
    {
      final AbstractCIIToUBLConverterBase <?> aConverter = aDispatcher.createConverter (e);
      assertEquals (EUBLCreationMode.CREDIT_NOTE, aConverter.getUBLCreationMode ());
      assertEquals ("MYVAT", aConverter.getVATScheme ());
      assertEquals ("my-cust", aConverter.getCustomizationID ());
      assertEquals ("my-prof", aConverter.getProfileID ());
      assertEquals ("my-card", aConverter.getCardAccountNetworkID ());
      assertEquals ("my-order", aConverter.getDefaultOrderRefID ());
      assertFalse (aConverter.isSwapQuantitySignIfNeeded ());
      assertFalse (aConverter.isSwapPriceSignIfNeeded ());
    }
  }
}
