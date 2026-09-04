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

import static com.helger.en16931.cii2ubl.en2026.MockD25ASettings.assertNoXPath;
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

    // BG-4 SELLER - BT-27 maps to PartyLegalEntity/RegistrationName, not PartyName/Name
    assertXPath (aInv, "cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName", "Seller Ltd");
    // BG-7 BUYER - BT-44
    assertXPath (aInv, "cac:AccountingCustomerParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName", "Buyer Ltd");

    // BG-23 VAT BREAKDOWN
    assertXPath (aInv, "cac:TaxTotal/cbc:TaxAmount", "20");
    assertXPath (aInv, "cac:TaxTotal/cbc:TaxAmount/@currencyID", "EUR");
    assertXPath (aInv, "cac:TaxTotal/cac:TaxSubtotal/cbc:TaxableAmount", "100");
    assertXPath (aInv, "cac:TaxTotal/cac:TaxSubtotal/cbc:TaxAmount", "20");
    assertXPath (aInv, "cac:TaxTotal/cac:TaxSubtotal/cac:TaxCategory/cbc:ID", "S");
    assertXPath (aInv, "cac:TaxTotal/cac:TaxSubtotal/cac:TaxCategory/cbc:Percent", "20");
    assertXPath (aInv, "cac:TaxTotal/cac:TaxSubtotal/cac:TaxCategory/cac:TaxScheme/cbc:ID", "VAT");

    // BG-22 DOCUMENT TOTALS
    assertXPath (aInv, "cac:LegalMonetaryTotal/cbc:LineExtensionAmount", "100");
    assertXPath (aInv, "cac:LegalMonetaryTotal/cbc:TaxExclusiveAmount", "100");
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
    // BG-30 LINE VAT INFORMATION
    assertXPath (aInv, "cac:InvoiceLine/cac:Item/cac:ClassifiedTaxCategory/cbc:ID", "S");
    assertXPath (aInv, "cac:InvoiceLine/cac:Item/cac:ClassifiedTaxCategory/cbc:Percent", "20");
    assertXPath (aInv, "cac:InvoiceLine/cac:Item/cac:ClassifiedTaxCategory/cac:TaxScheme/cbc:ID", "VAT");
    // BG-29 PRICE DETAILS
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
    assertXPath (aCN, "cac:AccountingSupplierParty/cac:Party/cac:PartyLegalEntity/cbc:RegistrationName", "Seller Ltd");

    // BG-22 DOCUMENT TOTALS
    assertXPath (aCN, "cac:LegalMonetaryTotal/cbc:PayableAmount", "120");

    // BG-25 INVOICE LINE - renamed to CreditNoteLine, InvoicedQuantity to CreditedQuantity
    assertXPathCount (aCN, "cac:CreditNoteLine", 1);
    assertXPath (aCN, "cac:CreditNoteLine/cbc:ID", "1");
    assertXPath (aCN, "cac:CreditNoteLine/cbc:CreditedQuantity", "4");
    assertXPath (aCN, "cac:CreditNoteLine/cbc:LineExtensionAmount", "100");
    assertXPath (aCN, "cac:CreditNoteLine/cac:Item/cbc:Name", "Test item");
  }

  @Test
  public void testConvertHeaderInvoice ()
  {
    final Element aInv = convertAndValidate ("d25a-header-invoice.xml", true);

    // BG-1 INVOICE NOTE - since UBL 2.5 this is cac:Annotation, no more "#code#" prefix
    assertXPathCount (aInv, "cac:Annotation", 2);
    assertXPath (aInv, "cac:Annotation[1]/cbc:SubjectCode", "AAI");
    assertXPath (aInv, "cac:Annotation[1]/cbc:AnnotationContent", "Payment within 30 days");
    assertNoXPath (aInv, "cac:Annotation[2]/cbc:SubjectCode");
    assertXPath (aInv, "cac:Annotation[2]/cbc:AnnotationContent", "Second note without a subject code");
    // The 2017 workaround must be gone
    assertNoXPath (aInv, "cbc:Note");

    // BT-10 + BT-10-1 - since UBL 2.5 this is cac:BuyerAssignedReference
    assertXPath (aInv, "cac:BuyerAssignedReference/cbc:BuyerReference", "BUYER-REF-4711");
    assertXPath (aInv, "cac:BuyerAssignedReference/cbc:BuyerReferenceCode", "ADE");
    assertNoXPath (aInv, "cbc:BuyerReference");

    // BT-31 Seller VAT identifier and BT-32 Seller tax registration identifier.
    // BT-32-2 is the fixed value "LOC" since 2026 - the 2017 binding passed "FC" through.
    assertXPath (aInv,
                 "cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='VAT']/cbc:CompanyID",
                 "ATU12345678");
    assertXPath (aInv,
                 "cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme[cac:TaxScheme/cbc:ID='LOC']/cbc:CompanyID",
                 "FC-987654");
    assertNoXPath (aInv, "cac:AccountingSupplierParty/cac:Party/cac:PartyTaxScheme/cac:TaxScheme[cbc:ID='FC']");

    // BG-5 SELLER POSTAL ADDRESS
    final String sAddr = "cac:AccountingSupplierParty/cac:Party/cac:PostalAddress/";
    assertXPath (aInv, sAddr + "cbc:StreetName", "Main Street 1");
    assertXPath (aInv, sAddr + "cbc:AdditionalStreetName", "Building A");
    assertXPath (aInv, sAddr + "cac:AddressLine/cbc:Line", "Floor 3");
    assertXPath (aInv, sAddr + "cbc:CityName", "Vienna");
    assertXPath (aInv, sAddr + "cbc:PostalZone", "1010");
    assertXPath (aInv, sAddr + "cbc:CountrySubentity", "Wien");
    assertXPath (aInv, sAddr + "cac:Country/cbc:IdentificationCode", "AT");

    // BG-4 SELLER
    final String sSeller = "cac:AccountingSupplierParty/cac:Party/";
    assertXPath (aInv, sSeller + "cbc:EndpointID", "4035811234567");
    assertXPath (aInv, sSeller + "cbc:EndpointID/@schemeID", "0088");
    assertXPath (aInv, sSeller + "cac:PartyIdentification/cbc:ID", "4035811234567");
    assertXPath (aInv, sSeller + "cac:PartyName/cbc:Name", "Seller Trading Name");
    assertXPath (aInv, sSeller + "cac:PartyLegalEntity/cbc:RegistrationName", "Seller Ltd");
    assertXPath (aInv, sSeller + "cac:PartyLegalEntity/cbc:CompanyID", "FN123456x");
    assertXPath (aInv, sSeller + "cac:PartyLegalEntity/cbc:CompanyLegalForm", "Registered in Vienna");

    // BG-6 SELLER CONTACT
    assertXPath (aInv, sSeller + "cac:Contact/cbc:Name", "Jane Doe");
    assertXPath (aInv, sSeller + "cac:Contact/cbc:Telephone", "+43 1 1234567");
    assertXPath (aInv, sSeller + "cac:Contact/cbc:ElectronicMail", "jane@seller.example");

    // BT-9 Payment due date
    assertXPath (aInv, "cbc:DueDate", "2026-02-14");
    // BT-11 Project reference
    assertXPath (aInv, "cac:ProjectReference/cbc:ID", "PROJECT-7");
    // BT-12 Contract reference
    assertXPath (aInv, "cac:ContractDocumentReference/cbc:ID", "CONTRACT-42");
    // BT-13 Purchase order reference
    assertXPath (aInv, "cac:OrderReference/cbc:ID", "PO-2026-0001");

    // BG-14 INVOICING PERIOD
    assertXPath (aInv, "cac:InvoicePeriod/cbc:StartDate", "2026-01-01");
    assertXPath (aInv, "cac:InvoicePeriod/cbc:EndDate", "2026-01-31");

    // BT-127 Invoice line note stays a plain cbc:Note
    assertXPath (aInv, "cac:InvoiceLine/cbc:Note", "Line level note");
  }

  @Test
  public void testConvertHeaderCreditNote ()
  {
    final Element aCN = convertAndValidate ("d25a-header-creditnote.xml", false);

    // BT-9 - since UBL 2.2 the CreditNote has a native cbc:DueDate
    assertXPath (aCN, "cbc:DueDate", "2026-02-14");
    assertNoXPath (aCN, "cac:PaymentMeans/cbc:PaymentDueDate");

    // BT-11 - since UBL 2.2 the CreditNote has a native cac:ProjectReference
    assertXPath (aCN, "cac:ProjectReference/cbc:ID", "PROJECT-7");
    assertNoXPath (aCN, "cac:AdditionalDocumentReference/cbc:ID");

    // BG-1 and BT-10 behave exactly like in the Invoice
    assertXPathCount (aCN, "cac:Annotation", 2);
    assertXPath (aCN, "cac:Annotation[1]/cbc:SubjectCode", "AAI");
    assertXPath (aCN, "cac:BuyerAssignedReference/cbc:BuyerReference", "BUYER-REF-4711");
  }
}
