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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;
import java.time.Month;

import org.jspecify.annotations.NonNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.state.ESuccess;
import com.helger.datetime.helper.PDTFactory;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.io.file.FilenameHelper;
import com.helger.ubl25.UBL25Marshaller;

import oasis.names.specification.ubl.schema.xsd.creditnote_25.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_25.InvoiceType;

/**
 * Test class for class {@link CIID25AToUBL25Converter}.
 *
 * @author Philip Helger
 */
public final class CIID25AToUBL25ConverterTest
{
  private static final String BASE_TEST_DIR = "src/test/resources/external/cii-d25a/";
  private static final String BASE_DEST_DIR = "generated/toubl25/";

  private static final Logger LOGGER = LoggerFactory.getLogger (CIID25AToUBL25ConverterTest.class);

  @NonNull
  private static Serializable _convert (@NonNull final String sFilename, @NonNull final ErrorList aErrorList)
  {
    final File aFile = new File (BASE_TEST_DIR, sFilename);
    assertTrue ("Not existing: " + aFile.getAbsolutePath (), aFile.exists ());

    LOGGER.info ("Converting " + aFile.toString () + " to UBL 2.5");
    final Serializable aUBL = new CIID25AToUBL25Converter ().convertCIItoUBL (aFile, aErrorList);
    assertTrue ("Errors: " + aErrorList.toString (), aErrorList.containsNoError ());
    assertNotNull (aUBL);
    return aUBL;
  }

  @NonNull
  private static File _getDestFile (@NonNull final String sFilename)
  {
    return new File (BASE_DEST_DIR, FilenameHelper.getBaseName (sFilename) + "-ubl.xml");
  }

  @Test
  public void testConvertMinimalInvoice ()
  {
    final String sFilename = "d25a-minimal-invoice.xml";
    final ErrorList aErrorList = new ErrorList ();
    final Serializable aUBL = _convert (sFilename, aErrorList);
    assertTrue ("Expected an Invoice but got " + aUBL.getClass ().getName (), aUBL instanceof InvoiceType);

    final InvoiceType aUBLInvoice = (InvoiceType) aUBL;

    // BT-24
    assertEquals ("urn:cen.eu:en16931:2026", aUBLInvoice.getCustomizationIDValue ());
    // BT-1
    assertEquals ("D25A-MIN-INV-1", aUBLInvoice.getIDValue ());
    // BT-2
    assertEquals (PDTFactory.createLocalDate (2026, Month.JANUARY, 15),
                  aUBLInvoice.getIssueDateValue ().toLocalDate ());
    // BT-3
    assertEquals ("380", aUBLInvoice.getInvoiceTypeCodeValue ());
    // BT-5
    assertEquals ("EUR", aUBLInvoice.getDocumentCurrencyCodeValue ());
    // BG-25
    assertEquals (1, aUBLInvoice.getInvoiceLineCount ());
    // BT-126
    assertEquals ("1", aUBLInvoice.getInvoiceLineAtIndex (0).getIDValue ());

    // Ensure the result is UBL 2.5 XSD valid
    final ESuccess eSuccess = UBL25Marshaller.invoice ()
                                             .setFormattedOutput (true)
                                             .write (aUBLInvoice, _getDestFile (sFilename));
    assertTrue ("The created UBL 2.5 Invoice is not XSD valid", eSuccess.isSuccess ());
  }

  @Test
  public void testConvertMinimalCreditNote ()
  {
    final String sFilename = "d25a-minimal-creditnote.xml";
    final ErrorList aErrorList = new ErrorList ();
    final Serializable aUBL = _convert (sFilename, aErrorList);
    assertTrue ("Expected a CreditNote but got " + aUBL.getClass ().getName (), aUBL instanceof CreditNoteType);

    final CreditNoteType aUBLCreditNote = (CreditNoteType) aUBL;

    // BT-24
    assertEquals ("urn:cen.eu:en16931:2026", aUBLCreditNote.getCustomizationIDValue ());
    // BT-1
    assertEquals ("D25A-MIN-CN-1", aUBLCreditNote.getIDValue ());
    // BT-3
    assertEquals ("381", aUBLCreditNote.getCreditNoteTypeCodeValue ());
    // BT-5
    assertEquals ("EUR", aUBLCreditNote.getDocumentCurrencyCodeValue ());
    // BG-25
    assertEquals (1, aUBLCreditNote.getCreditNoteLineCount ());

    // Ensure the result is UBL 2.5 XSD valid
    final ESuccess eSuccess = UBL25Marshaller.creditNote ()
                                             .setFormattedOutput (true)
                                             .write (aUBLCreditNote, _getDestFile (sFilename));
    assertTrue ("The created UBL 2.5 CreditNote is not XSD valid", eSuccess.isSuccess ());
  }
}
