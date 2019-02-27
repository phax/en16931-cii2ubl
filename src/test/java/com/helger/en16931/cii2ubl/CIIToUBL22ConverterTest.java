/**
 * Copyright (C) 2019 Philip Helger
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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;

import javax.annotation.Nullable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.bdve.en16931.EN16931Validation;
import com.helger.bdve.result.ValidationResult;
import com.helger.bdve.result.ValidationResultList;
import com.helger.bdve.source.ValidationSource;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.ubl22.UBL22Writer;
import com.helger.ubl22.UBL22WriterBuilder;

import oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeAgreementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeDeliveryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.SupplyChainTradeTransactionType;

/**
 * Test class for class {@link CIIToUBL22Converter}.
 *
 * @author Philip Helger
 */
public final class CIIToUBL22ConverterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBL22ConverterTest.class);

  @Test
  public void testConvertAndValidateAll ()
  {
    for (final String sFilename : MockSettings.TEST_FILES)
    {
      LOGGER.info ("Converting " + sFilename + " to UBL");

      // Main conversion
      final ErrorList aErrorList = new ErrorList ();
      final Serializable aInvoice = new CIIToUBL22Converter ().convertCIItoUBL (new File ("src/test/resources/cii",
                                                                                          sFilename),
                                                                                aErrorList);
      assertTrue ("Errors: " + aErrorList.toString (), aErrorList.isEmpty ());
      assertNotNull (aInvoice);

      final File aDestFile = new File ("toubl22", FilenameHelper.getBaseName (sFilename) + "-ubl.xml");
      final ValidationResultList aResultList;

      if (aInvoice instanceof InvoiceType)
      {
        final InvoiceType aUBLInvoice = (InvoiceType) aInvoice;

        // Check UBL XSD scheme
        final UBL22WriterBuilder <InvoiceType> aWriter = UBL22Writer.invoice ().setFormattedOutput (true);
        aWriter.write (aUBLInvoice, aDestFile);

        // Validate against EN16931 validation rules
        aResultList = MockSettings.VES_REGISTRY.getOfID (EN16931Validation.VID_UBL_INVOICE_110)
                                               .createExecutionManager ()
                                               .executeValidation (ValidationSource.createXMLSource (new FileSystemResource (aDestFile)));
      }
      else
      {
        final CreditNoteType aUBLInvoice = (CreditNoteType) aInvoice;

        // Check UBL XSD scheme
        final UBL22WriterBuilder <CreditNoteType> aWriter = UBL22Writer.creditNote ().setFormattedOutput (true);
        aWriter.write (aUBLInvoice, aDestFile);

        // Validate against EN16931 validation rules
        aResultList = MockSettings.VES_REGISTRY.getOfID (EN16931Validation.VID_UBL_CREDIT_NOTE_110)
                                               .createExecutionManager ()
                                               .executeValidation (ValidationSource.createXMLSource (new FileSystemResource (aDestFile)));
      }

      assertNotNull (aResultList);
      for (final ValidationResult aResult : aResultList)
      {
        assertTrue ("Errors: " + aResult.getErrorList ().toString (), aResult.getErrorList ().isEmpty ());
      }
    }
  }

  @Nullable
  private static Serializable _convert (final CrossIndustryInvoiceType aInvoice)
  {
    return new CIIToUBL22Converter ().convertCIItoUBL (aInvoice, new ErrorList ());
  }

  @Test
  public void testConvertCreepy ()
  {
    final CrossIndustryInvoiceType aInvoice = new CrossIndustryInvoiceType ();
    assertNull (_convert (aInvoice));

    final SupplyChainTradeTransactionType aSCTTT = new SupplyChainTradeTransactionType ();
    aInvoice.setSupplyChainTradeTransaction (aSCTTT);
    assertNull (_convert (aInvoice));

    final HeaderTradeAgreementType aHeaderAgreement = new HeaderTradeAgreementType ();
    aSCTTT.setApplicableHeaderTradeAgreement (aHeaderAgreement);
    assertNull (_convert (aInvoice));

    final HeaderTradeDeliveryType aHeaderDelivery = new HeaderTradeDeliveryType ();
    aSCTTT.setApplicableHeaderTradeDelivery (aHeaderDelivery);
    assertNull (_convert (aInvoice));

    final HeaderTradeSettlementType aHeaderSettlement = new HeaderTradeSettlementType ();
    aSCTTT.setApplicableHeaderTradeSettlement (aHeaderSettlement);
    // First version working
    assertNotNull (_convert (aInvoice));
  }
}
