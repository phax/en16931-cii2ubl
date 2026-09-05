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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import org.junit.Test;

import com.helger.datetime.helper.PDTFactory;
import com.helger.base.state.ETriState;
import com.helger.diagnostics.error.list.ErrorList;

import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.InvoicedQuantityType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.NameType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.PayableAmountType;

/**
 * Test class for class {@link AbstractCIIToUBLConverterBase}.
 *
 * @author Philip Helger
 */
public final class AbstractCIIToUBLConverterBaseTest
{
  @Test
  public void testParseDate ()
  {
    final ErrorList aList = new ErrorList ();
    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6),
                  AbstractCIIToUBLConverterBase.parseDate ("060705", "2", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6),
                  AbstractCIIToUBLConverterBase.parseDate ("070605", "3", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6),
                  AbstractCIIToUBLConverterBase.parseDate ("06072005", "4", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6),
                  AbstractCIIToUBLConverterBase.parseDate ("050706", "101", aList));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2005, Month.JULY, 6),
                  AbstractCIIToUBLConverterBase.parseDate ("20050706", "102", aList));
    assertTrue (aList.isEmpty ());

    final LocalDate aLD = AbstractCIIToUBLConverterBase.parseDate ("050101", "103", aList);
    // Windows: 2005, Linux: 2004
    assertTrue (aLD.equals (PDTFactory.createLocalDate (2005, Month.JANUARY, 3)) ||
                aLD.equals (PDTFactory.createLocalDate (2004, Month.DECEMBER, 26)));
    assertTrue (aList.isEmpty ());

    assertEquals (PDTFactory.createLocalDate (2019, Month.JANUARY, 5),
                  AbstractCIIToUBLConverterBase.parseDate ("19005", "105", aList));
    assertTrue (aList.isEmpty ());

    // Unsupported format
    assertNull (AbstractCIIToUBLConverterBase.parseDate ("050101", "999", aList));
    assertFalse (aList.isEmpty ());
  }

  @Test
  public void testCopyName ()
  {
    final NameType aUBL = new NameType ();
    assertNull (AbstractCIIToUBLConverterBase.copyName (null, "lang", "locale", aUBL));
    assertNull (AbstractCIIToUBLConverterBase.copyName ("", "lang", "locale", aUBL));

    // Every attribute gets a distinct value, so that a transposition fails
    assertSame (aUBL, AbstractCIIToUBLConverterBase.copyName ("val", "lang", "locale", aUBL));
    assertEquals ("val", aUBL.getValue ());
    assertEquals ("lang", aUBL.getLanguageID ());
    assertEquals ("locale", aUBL.getLanguageLocaleID ());
  }

  @Test
  public void testCopyQuantity ()
  {
    final InvoicedQuantityType aUBL = new InvoicedQuantityType ();
    assertNull (AbstractCIIToUBLConverterBase.copyQuantity (null, "uc", "ucli", "uclai", "uclan", aUBL));

    // Every attribute gets a distinct value, so that a transposition fails. The test corpus never
    // exercises unitCodeListID, unitCodeListAgencyID and unitCodeListAgencyName.
    assertSame (aUBL,
                AbstractCIIToUBLConverterBase.copyQuantity (new BigDecimal ("2.500"),
                                                            "uc",
                                                            "ucli",
                                                            "uclai",
                                                            "uclan",
                                                            aUBL));
    // Trailing zeroes are removed
    assertEquals (new BigDecimal ("2.5"), aUBL.getValue ());
    assertEquals ("uc", aUBL.getUnitCode ());
    assertEquals ("ucli", aUBL.getUnitCodeListID ());
    assertEquals ("uclai", aUBL.getUnitCodeListAgencyID ());
    assertEquals ("uclan", aUBL.getUnitCodeListAgencyName ());
  }

  @Test
  public void testCopyAmount ()
  {
    final PayableAmountType aUBL = new PayableAmountType ();
    assertNull (AbstractCIIToUBLConverterBase.copyAmount (null, "EUR", "clv", aUBL, "USD"));

    assertSame (aUBL, AbstractCIIToUBLConverterBase.copyAmount (new BigDecimal ("12.300"), "EUR", "clv", aUBL, "USD"));
    assertEquals (new BigDecimal ("12.3"), aUBL.getValue ());
    assertEquals ("EUR", aUBL.getCurrencyID ());
    assertEquals ("clv", aUBL.getCurrencyCodeListVersionID ());

    // The default currency code is only used if none is present
    final PayableAmountType aUBL2 = new PayableAmountType ();
    AbstractCIIToUBLConverterBase.copyAmount (BigDecimal.ONE, null, null, aUBL2, "USD");
    assertEquals ("USD", aUBL2.getCurrencyID ());
  }

  @Test
  public void testParseIndicator ()
  {
    final ErrorList aList = new ErrorList ();
    // The boolean branch of the choice wins
    assertEquals (ETriState.TRUE, AbstractCIIToUBLConverterBase.parseIndicator (Boolean.TRUE, false, null, null, aList));
    assertEquals (ETriState.FALSE,
                  AbstractCIIToUBLConverterBase.parseIndicator (Boolean.FALSE, true, "true", null, aList));
    assertTrue (aList.isEmpty ());

    // The string branch
    assertEquals (ETriState.TRUE, AbstractCIIToUBLConverterBase.parseIndicator (null, true, "true", null, aList));
    assertEquals (ETriState.FALSE, AbstractCIIToUBLConverterBase.parseIndicator (null, true, "false", null, aList));
    assertEquals (ETriState.UNDEFINED, AbstractCIIToUBLConverterBase.parseIndicator (null, true, null, null, aList));
    assertTrue (aList.isEmpty ());

    // An unparsable string is an error
    assertEquals (ETriState.UNDEFINED, AbstractCIIToUBLConverterBase.parseIndicator (null, true, "yes", null, aList));
    assertFalse (aList.isEmpty ());

    // Neither branch present
    try
    {
      AbstractCIIToUBLConverterBase.parseIndicator (null, false, null, null, new ErrorList ());
      fail ("Expected an IllegalStateException");
    }
    catch (final IllegalStateException ex)
    {
      // expected
    }
  }

  @Test
  public void testIsInvoiceType ()
  {
    final ErrorList aList = new ErrorList ();
    // BT-3 decides, using the shared UNTDID 1001 subset
    assertEquals (ETriState.TRUE, AbstractCIIToUBLConverterBase.isInvoiceType ("380", null, null, aList));
    assertEquals (ETriState.FALSE, AbstractCIIToUBLConverterBase.isInvoiceType ("381", null, null, aList));
    // Surrounding whitespace is tolerated
    assertEquals (ETriState.TRUE, AbstractCIIToUBLConverterBase.isInvoiceType (" 380 ", null, null, aList));
    // "81" is a Credit Note in every version of the code list, even though the EN 16931 validation
    // artefacts additionally accept it on an Invoice
    assertEquals (ETriState.FALSE, AbstractCIIToUBLConverterBase.isInvoiceType ("81", null, null, aList));
    // Added in v15 of the code list: 471 as an Invoice, 502 and 503 as Credit Notes
    assertEquals (ETriState.TRUE, AbstractCIIToUBLConverterBase.isInvoiceType ("471", null, null, aList));
    assertEquals (ETriState.FALSE, AbstractCIIToUBLConverterBase.isInvoiceType ("502", null, null, aList));
    assertEquals (ETriState.FALSE, AbstractCIIToUBLConverterBase.isInvoiceType ("503", null, null, aList));
    assertTrue (aList.isEmpty ());

    // Unknown BT-3 falls back to the sign of BT-115
    assertEquals (ETriState.TRUE, AbstractCIIToUBLConverterBase.isInvoiceType ("999", BigDecimal.ONE, "src", aList));
    assertEquals (ETriState.FALSE,
                  AbstractCIIToUBLConverterBase.isInvoiceType ("999", BigDecimal.valueOf (-1), "src", aList));
    assertTrue (aList.isEmpty ());

    // Neither is conclusive - a warning, not an error
    assertEquals (ETriState.UNDEFINED, AbstractCIIToUBLConverterBase.isInvoiceType ("999", null, null, aList));
    assertTrue (aList.containsNoError ());
    assertFalse (aList.isEmpty ());
  }

  @Test
  public void testIsUsableGlobalID ()
  {
    assertTrue (AbstractCIIToUBLConverterBase.isUsableGlobalID ("v", "s"));
    assertFalse (AbstractCIIToUBLConverterBase.isUsableGlobalID ("v", null));
    assertFalse (AbstractCIIToUBLConverterBase.isUsableGlobalID (null, "s"));
    assertFalse (AbstractCIIToUBLConverterBase.isUsableGlobalID ("", "s"));
  }
}
