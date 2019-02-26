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
import com.helger.bdve.executorset.ValidationExecutorSetRegistry;
import com.helger.bdve.result.ValidationResult;
import com.helger.bdve.result.ValidationResultList;
import com.helger.bdve.source.ValidationSource;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.resource.FileSystemResource;
import com.helger.ubl21.UBL21Writer;
import com.helger.ubl21.UBL21WriterBuilder;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeAgreementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeDeliveryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.SupplyChainTradeTransactionType;

/**
 * Test class for class {@link CIIToUBLConverter}.
 *
 * @author Philip Helger
 */
public final class CIIToUBLConverterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverterTest.class);
  private static final String [] TEST_FILES = new String [] { "CII_business_example_01.xml",
                                                              "CII_business_example_02.xml",
                                                              "CII_example1.xml",
                                                              "CII_example2.xml",
                                                              "CII_example3.xml",
                                                              "CII_example4.xml",
                                                              "CII_example5.xml",
                                                              "CII_example6.xml",
                                                              "CII_example7.xml",
                                                              "CII_example8.xml",
                                                              "CII_example9.xml" };
  private static final ValidationExecutorSetRegistry VES_REGISTRY = new ValidationExecutorSetRegistry ();
  static
  {
    EN16931Validation.initEN16931 (VES_REGISTRY);
  }

  @Test
  public void testConvertAndValidateAll ()
  {
    final UBL21WriterBuilder <InvoiceType> aWriter = UBL21Writer.invoice ().setFormattedOutput (true);
    for (final String sFilename : TEST_FILES)
    {
      LOGGER.info ("Converting " + sFilename + " to UBL");

      // Main conversion
      final ErrorList aErrorList = new ErrorList ();
      final Serializable aInvoice = new CIIToUBLConverter ().convertCIItoUBL (new File ("src/test/resources/cii",
                                                                                        sFilename),
                                                                              aErrorList);
      assertTrue ("Errors: " + aErrorList.toString (), aErrorList.isEmpty ());
      assertNotNull (aInvoice);
      assertTrue (aInvoice instanceof InvoiceType);

      // Check UBL XSD scheme
      final InvoiceType aUBLInvoice = (InvoiceType) aInvoice;
      final File aDestFile = new File ("toubl", FilenameHelper.getBaseName (sFilename) + "-ubl.xml");
      aWriter.write (aUBLInvoice, aDestFile);

      // Validate against EN16931 validation rules
      final ValidationResultList aResultList = VES_REGISTRY.getOfID (EN16931Validation.VID_UBL_INVOICE_110)
                                                           .createExecutionManager ()
                                                           .executeValidation (ValidationSource.createXMLSource (new FileSystemResource (aDestFile)));
      assertNotNull (aResultList);
      for (final ValidationResult aResult : aResultList)
      {
        assertTrue ("Errors: " + aResult.getErrorList ().toString (), aResult.getErrorList ().isEmpty ());
      }
    }
  }

  @Nullable
  private static InvoiceType _convert (final CrossIndustryInvoiceType aInvoice)
  {
    return (InvoiceType) new CIIToUBLConverter ().convertCIItoUBL (aInvoice, new ErrorList ());
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
