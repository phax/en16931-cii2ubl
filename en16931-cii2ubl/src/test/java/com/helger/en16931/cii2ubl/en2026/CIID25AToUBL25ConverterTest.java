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

import static com.helger.en16931.cii2ubl.en2026.MockD25ASettings.assertXPath;
import static com.helger.en16931.cii2ubl.en2026.MockD25ASettings.assertXPathCount;
import static com.helger.en16931.cii2ubl.en2026.MockD25ASettings.convertAndValidate;

import org.junit.Test;
import org.w3c.dom.Element;

/**
 * Test class for class {@link CIID25AToUBL25Converter}.<br>
 * All XPath expressions are taken from <code>docs/en16931-2026-syntax.md</code> and are relative to
 * the document element.
 *
 * @author Philip Helger
 */
public final class CIID25AToUBL25ConverterTest
{
  @Test
  public void testConvertMinimalInvoice ()
  {
    final Element aInv = convertAndValidate ("d25a-minimal-invoice.xml", true);

    // Header level
    assertXPath (aInv, "cbc:CustomizationID", "urn:cen.eu:en16931:2026");
    assertXPath (aInv, "cbc:ProfileID", "urn:fdc:peppol.eu:2017:poacc:billing:01:1.0");
    assertXPath (aInv, "cbc:ID", "D25A-MIN-INV-1");
    assertXPath (aInv, "cbc:IssueDate", "2026-01-15");
    assertXPath (aInv, "cbc:InvoiceTypeCode", "380");
    assertXPath (aInv, "cbc:DocumentCurrencyCode", "EUR");

    // BG-4 SELLER
    assertXPath (aInv, "cac:AccountingSupplierParty/cac:Party/cac:PartyName/cbc:Name", "Seller Ltd");
    // BG-7 BUYER
    assertXPath (aInv, "cac:AccountingCustomerParty/cac:Party/cac:PartyName/cbc:Name", "Buyer Ltd");

    // BG-22 DOCUMENT TOTALS
    assertXPath (aInv, "cac:LegalMonetaryTotal/cbc:TaxInclusiveAmount", "120");
    assertXPath (aInv, "cac:LegalMonetaryTotal/cbc:TaxInclusiveAmount/@currencyID", "EUR");
    assertXPath (aInv, "cac:LegalMonetaryTotal/cbc:PayableAmount", "120");

    // BG-25 INVOICE LINE
    assertXPathCount (aInv, "cac:InvoiceLine", 1);
    assertXPath (aInv, "cac:InvoiceLine/cbc:ID", "1");
    assertXPath (aInv, "cac:InvoiceLine/cbc:InvoicedQuantity", "4");
    assertXPath (aInv, "cac:InvoiceLine/cbc:InvoicedQuantity/@unitCode", "C62");
    assertXPath (aInv, "cac:InvoiceLine/cbc:LineExtensionAmount", "100");
    assertXPath (aInv, "cac:InvoiceLine/cac:Item/cbc:Name", "Test item");
    assertXPath (aInv, "cac:InvoiceLine/cac:Price/cbc:PriceAmount", "25");
  }

  @Test
  public void testConvertMinimalCreditNote ()
  {
    final Element aCN = convertAndValidate ("d25a-minimal-creditnote.xml", false);

    // Header level
    assertXPath (aCN, "cbc:CustomizationID", "urn:cen.eu:en16931:2026");
    assertXPath (aCN, "cbc:ID", "D25A-MIN-CN-1");
    assertXPath (aCN, "cbc:IssueDate", "2026-01-15");
    assertXPath (aCN, "cbc:CreditNoteTypeCode", "381");
    assertXPath (aCN, "cbc:DocumentCurrencyCode", "EUR");

    // BG-4 SELLER
    assertXPath (aCN, "cac:AccountingSupplierParty/cac:Party/cac:PartyName/cbc:Name", "Seller Ltd");

    // BG-22 DOCUMENT TOTALS
    assertXPath (aCN, "cac:LegalMonetaryTotal/cbc:PayableAmount", "120");

    // BG-25 INVOICE LINE - renamed to CreditNoteLine, InvoicedQuantity to CreditedQuantity
    assertXPathCount (aCN, "cac:CreditNoteLine", 1);
    assertXPath (aCN, "cac:CreditNoteLine/cbc:ID", "1");
    assertXPath (aCN, "cac:CreditNoteLine/cbc:CreditedQuantity", "4");
    assertXPath (aCN, "cac:CreditNoteLine/cbc:LineExtensionAmount", "100");
    assertXPath (aCN, "cac:CreditNoteLine/cac:Item/cbc:Name", "Test item");
  }
}
