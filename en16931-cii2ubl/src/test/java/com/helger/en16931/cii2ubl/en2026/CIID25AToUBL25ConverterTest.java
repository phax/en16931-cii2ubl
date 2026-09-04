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

  @Test
  public void testConvertFullInvoice ()
  {
    final Element aInv = convertAndValidate ("d25a-full-invoice.xml", true);

    // Header level
    assertXPath (aInv, "cbc:ID", "D25A-FULL-INV-1");
    assertXPath (aInv, "cbc:IssueDate", "2026-01-15");
    assertXPath (aInv, "cbc:InvoiceTypeCode", "380");
    assertXPath (aInv, "cbc:DocumentCurrencyCode", "EUR");
    // BT-6 VAT accounting currency code
    assertXPath (aInv, "cbc:TaxCurrencyCode", "USD");
    // BT-7 Value added tax point date
    assertXPath (aInv, "cbc:TaxPointDate", "2026-01-10");
    // BT-9 Payment due date
    assertXPath (aInv, "cbc:DueDate", "2026-02-14");
    // BT-19 Buyer accounting reference
    assertXPath (aInv, "cbc:AccountingCost", "COST-CENTRE-1");
    // BT-13 Purchase order reference + BT-14 Sales order reference
    assertXPath (aInv, "cac:OrderReference/cbc:ID", "PO-2026-0001");
    assertXPath (aInv, "cac:OrderReference/cbc:SalesOrderID", "SO-2026-9");
    // BT-12 Contract reference
    assertXPath (aInv, "cac:ContractDocumentReference/cbc:ID", "CONTRACT-42");
    // BT-15 Receiving advice + BT-16 Despatch advice
    assertXPath (aInv, "cac:ReceiptDocumentReference/cbc:ID", "RECEIPT-4");
    assertXPath (aInv, "cac:DespatchDocumentReference/cbc:ID", "DESPATCH-3");
    // BT-17 Tender or lot reference
    assertXPath (aInv, "cac:OriginatorDocumentReference/cbc:ID", "TENDER-5");

    // BG-3 PRECEDING INVOICE REFERENCE - 0..n since CII D25A
    assertXPathCount (aInv, "cac:BillingReference", 2);
    assertXPath (aInv, "cac:BillingReference[1]/cac:InvoiceDocumentReference/cbc:ID", "PREV-INV-1");
    assertXPath (aInv, "cac:BillingReference[1]/cac:InvoiceDocumentReference/cbc:IssueDate", "2025-12-15");
    assertXPath (aInv, "cac:BillingReference[2]/cac:InvoiceDocumentReference/cbc:ID", "PREV-INV-2");

    // BT-18 Invoiced object identifier - DocumentTypeCode 130
    assertXPath (aInv, "cac:AdditionalDocumentReference[cbc:DocumentTypeCode='130']/cbc:ID", "METER-9");
    assertXPath (aInv, "cac:AdditionalDocumentReference[cbc:DocumentTypeCode='130']/cbc:ID/@schemeID", "AVE");
    // BG-24 ADDITIONAL SUPPORTING DOCUMENTS
    assertXPath (aInv, "cac:AdditionalDocumentReference[cbc:ID='DOC-916']/cbc:DocumentDescription", "Supporting document");
    assertXPath (aInv,
                 "cac:AdditionalDocumentReference[cbc:ID='DOC-916']/cac:Attachment/cac:ExternalReference/cbc:URI",
                 "https://example.org/doc");

    // BG-10 PAYEE
    assertXPath (aInv, "cac:PayeeParty/cac:PartyName/cbc:Name", "Payee Ltd");
    assertXPath (aInv, "cac:PayeeParty/cac:PartyIdentification/cbc:ID", "4035822222222");
    assertXPath (aInv, "cac:PayeeParty/cac:PartyLegalEntity/cbc:CompanyID", "FN999999z");

    // BG-11 SELLER TAX REPRESENTATIVE PARTY + BG-12 its address
    assertXPath (aInv, "cac:TaxRepresentativeParty/cac:PartyName/cbc:Name", "Tax Rep GmbH");
    assertXPath (aInv, "cac:TaxRepresentativeParty/cac:PartyTaxScheme/cbc:CompanyID", "ATU11111111");
    assertXPath (aInv, "cac:TaxRepresentativeParty/cac:PostalAddress/cbc:CityName", "Vienna");

    // BG-13 DELIVERY INFORMATION + BG-15 DELIVER TO ADDRESS
    assertXPath (aInv, "cac:Delivery/cbc:ActualDeliveryDate", "2026-01-12");
    assertXPath (aInv, "cac:Delivery/cac:DeliveryLocation/cbc:ID", "4035811111111");
    assertXPath (aInv, "cac:Delivery/cac:DeliveryLocation/cac:Address/cbc:CityName", "Linz");
    assertXPath (aInv, "cac:Delivery/cac:DeliveryParty/cac:PartyName/cbc:Name", "Delivery Site");

    // BG-16 PAYMENT INSTRUCTIONS + BG-17 CREDIT TRANSFER
    assertXPath (aInv, "cac:PaymentMeans/cbc:PaymentMeansCode", "58");
    // BT-82 uses the lower case @name attribute
    assertXPath (aInv, "cac:PaymentMeans/cbc:PaymentMeansCode/@name", "SEPA credit transfer");
    // BT-83 Remittance information
    assertXPath (aInv, "cac:PaymentMeans/cbc:PaymentID", "REMIT-1");
    assertXPath (aInv, "cac:PaymentMeans/cac:PayeeFinancialAccount/cbc:ID", "AT611904300234573201");
    assertXPath (aInv, "cac:PaymentMeans/cac:PayeeFinancialAccount/cbc:Name", "Seller Account");
    assertXPath (aInv,
                 "cac:PaymentMeans/cac:PayeeFinancialAccount/cac:FinancialInstitutionBranch/cbc:ID",
                 "GIBAATWWXXX");

    // BT-20 Payment terms
    assertXPath (aInv, "cac:PaymentTerms/cbc:Note", "Net 30 days");

    // BG-20 DOCUMENT LEVEL ALLOWANCES - ChargeIndicator false
    final String sAllow = "cac:AllowanceCharge[cbc:ChargeIndicator='false']/";
    assertXPath (aInv, sAllow + "cbc:Amount", "5");
    assertXPath (aInv, sAllow + "cbc:BaseAmount", "100");
    assertXPath (aInv, sAllow + "cbc:MultiplierFactorNumeric", "5.00");
    assertXPath (aInv, sAllow + "cbc:AllowanceChargeReason", "Volume discount");
    assertXPath (aInv, sAllow + "cbc:AllowanceChargeReasonCode", "95");
    assertXPath (aInv, sAllow + "cac:TaxCategory/cbc:ID", "S");
    // BG-21 DOCUMENT LEVEL CHARGES - ChargeIndicator true
    final String sCharge = "cac:AllowanceCharge[cbc:ChargeIndicator='true']/";
    assertXPath (aInv, sCharge + "cbc:Amount", "15");
    assertXPath (aInv, sCharge + "cbc:AllowanceChargeReason", "Freight");
    assertXPath (aInv, sCharge + "cbc:AllowanceChargeReasonCode", "FC");

    // BG-23 VAT BREAKDOWN - two categories, and BT-110/BT-111 as two TaxTotals
    assertXPathCount (aInv, "cac:TaxTotal", 2);
    // BT-110 in the invoice currency
    assertXPath (aInv, "cac:TaxTotal[cbc:TaxAmount/@currencyID='EUR']/cbc:TaxAmount", "22");
    // BT-111 in the VAT accounting currency
    assertXPath (aInv, "cac:TaxTotal[cbc:TaxAmount/@currencyID='USD']/cbc:TaxAmount", "24.2");
    assertXPathCount (aInv, "cac:TaxTotal/cac:TaxSubtotal", 2);
    final String sVat1 = "cac:TaxTotal/cac:TaxSubtotal[cac:TaxCategory/cbc:ID='S']/";
    assertXPath (aInv, sVat1 + "cbc:TaxableAmount", "100");
    assertXPath (aInv, sVat1 + "cbc:TaxAmount", "20");
    assertXPath (aInv, sVat1 + "cac:TaxCategory/cbc:Percent", "20");
    final String sVat2 = "cac:TaxTotal/cac:TaxSubtotal[cac:TaxCategory/cbc:ID='AE']/";
    assertXPath (aInv, sVat2 + "cbc:TaxableAmount", "50");
    assertXPath (aInv, sVat2 + "cac:TaxCategory/cbc:TaxExemptionReason", "Reverse charge");
    assertXPath (aInv, sVat2 + "cac:TaxCategory/cbc:TaxExemptionReasonCode", "VATEX-EU-AE");

    // BG-22 DOCUMENT TOTALS
    final String sTot = "cac:LegalMonetaryTotal/";
    assertXPath (aInv, sTot + "cbc:LineExtensionAmount", "100");
    assertXPath (aInv, sTot + "cbc:AllowanceTotalAmount", "5");
    assertXPath (aInv, sTot + "cbc:ChargeTotalAmount", "15");
    assertXPath (aInv, sTot + "cbc:TaxExclusiveAmount", "110");
    assertXPath (aInv, sTot + "cbc:TaxInclusiveAmount", "132");
    assertXPath (aInv, sTot + "cbc:PrepaidAmount", "32");
    assertXPath (aInv, sTot + "cbc:PayableAmount", "100");

    // BG-25 INVOICE LINE
    final String sLine = "cac:InvoiceLine/";
    assertXPath (aInv, sLine + "cbc:ID", "1");
    assertXPath (aInv, sLine + "cbc:Note", "Line one note");
    assertXPath (aInv, sLine + "cbc:InvoicedQuantity", "4");
    assertXPath (aInv, sLine + "cbc:InvoicedQuantity/@unitCode", "C62");
    assertXPath (aInv, sLine + "cbc:LineExtensionAmount", "100");
    assertXPath (aInv, sLine + "cbc:AccountingCost", "LINE-COST-CENTRE");
    assertXPath (aInv, sLine + "cac:DocumentReference/cbc:ID", "OBJ-1");
    // BG-26 INVOICE LINE PERIOD
    assertXPath (aInv, sLine + "cac:InvoicePeriod/cbc:StartDate", "2026-01-01");
    assertXPath (aInv, sLine + "cac:InvoicePeriod/cbc:EndDate", "2026-01-31");
    // BG-27 INVOICE LINE ALLOWANCES + BG-28 INVOICE LINE CHARGES
    assertXPath (aInv, sLine + "cac:AllowanceCharge[cbc:ChargeIndicator='false']/cbc:Amount", "10");
    assertXPath (aInv, sLine + "cac:AllowanceCharge[cbc:ChargeIndicator='false']/cbc:AllowanceChargeReason", "Line discount");
    assertXPath (aInv, sLine + "cac:AllowanceCharge[cbc:ChargeIndicator='true']/cbc:AllowanceChargeReason", "Line freight");
    // BG-31 ITEM INFORMATION
    final String sItem = sLine + "cac:Item/";
    assertXPath (aInv, sItem + "cbc:Name", "Widget");
    assertXPath (aInv, sItem + "cbc:Description", "A round widget");
    assertXPath (aInv, sItem + "cac:SellersItemIdentification/cbc:ID", "SELLER-ART-1");
    assertXPath (aInv, sItem + "cac:BuyersItemIdentification/cbc:ID", "BUYER-ART-1");
    assertXPath (aInv, sItem + "cac:StandardItemIdentification/cbc:ID", "1234567890128");
    assertXPath (aInv, sItem + "cac:StandardItemIdentification/cbc:ID/@schemeID", "0160");
    assertXPath (aInv, sItem + "cac:OriginCountry/cbc:IdentificationCode", "AT");
    // BT-158 + BT-158-1 + BT-158-2
    assertXPath (aInv, sItem + "cac:CommodityClassification/cbc:ItemClassificationCode", "CLASS-1");
    assertXPath (aInv, sItem + "cac:CommodityClassification/cbc:ItemClassificationCode/@listID", "TST");
    assertXPath (aInv, sItem + "cac:CommodityClassification/cbc:ItemClassificationCode/@listVersionID", "1.0");
    // BG-30 LINE VAT INFORMATION
    assertXPath (aInv, sItem + "cac:ClassifiedTaxCategory/cbc:ID", "S");
    assertXPath (aInv, sItem + "cac:ClassifiedTaxCategory/cbc:Percent", "20");
    // BG-32 ITEM ATTRIBUTE
    assertXPathCount (aInv, sItem + "cac:AdditionalItemProperty", 2);
    assertXPath (aInv, sItem + "cac:AdditionalItemProperty[1]/cbc:Name", "Colour");
    assertXPath (aInv, sItem + "cac:AdditionalItemProperty[1]/cbc:Value", "Blue");
    // BG-29 PRICE DETAILS - BT-146 net price, BT-147 discount, BT-148 gross price, BT-149/BT-150
    final String sPrice = sLine + "cac:Price/";
    assertXPath (aInv, sPrice + "cbc:PriceAmount", "25");
    assertXPath (aInv, sPrice + "cbc:BaseQuantity", "1");
    assertXPath (aInv, sPrice + "cbc:BaseQuantity/@unitCode", "C62");
    assertXPath (aInv, sPrice + "cac:AllowanceCharge/cbc:ChargeIndicator", "false");
    assertXPath (aInv, sPrice + "cac:AllowanceCharge/cbc:Amount", "5");
    assertXPath (aInv, sPrice + "cac:AllowanceCharge/cbc:BaseAmount", "30");
  }

  @Test
  public void testConvertFullCreditNote ()
  {
    final Element aCN = convertAndValidate ("d25a-full-creditnote.xml", false);

    // The 2026 credit note binding is a mechanical rename of the invoice binding
    assertXPath (aCN, "cbc:ID", "D25A-FULL-CN-1");
    assertXPath (aCN, "cbc:CreditNoteTypeCode", "381");
    assertNoXPath (aCN, "cbc:InvoiceTypeCode");

    // cac:InvoiceLine -> cac:CreditNoteLine, cbc:InvoicedQuantity -> cbc:CreditedQuantity
    assertXPathCount (aCN, "cac:CreditNoteLine", 1);
    assertNoXPath (aCN, "cac:InvoiceLine");
    assertXPath (aCN, "cac:CreditNoteLine/cbc:CreditedQuantity", "4");
    assertXPath (aCN, "cac:CreditNoteLine/cbc:CreditedQuantity/@unitCode", "C62");
    assertNoXPath (aCN, "cac:CreditNoteLine/cbc:InvoicedQuantity");

    // Everything else is identical to the invoice
    assertXPath (aCN, "cbc:DueDate", "2026-02-14");
    assertXPath (aCN, "cbc:TaxCurrencyCode", "USD");
    assertXPath (aCN, "cac:ProjectReference/cbc:ID", "PROJECT-7");
    assertXPath (aCN, "cac:OrderReference/cbc:SalesOrderID", "SO-2026-9");
    assertXPathCount (aCN, "cac:BillingReference", 2);
    assertXPath (aCN, "cac:PayeeParty/cac:PartyName/cbc:Name", "Payee Ltd");
    assertXPath (aCN, "cac:TaxRepresentativeParty/cac:PartyName/cbc:Name", "Tax Rep GmbH");
    assertXPath (aCN, "cac:Delivery/cbc:ActualDeliveryDate", "2026-01-12");
    assertXPath (aCN, "cac:PaymentMeans/cac:PayeeFinancialAccount/cbc:ID", "AT611904300234573201");
    assertXPathCount (aCN, "cac:TaxTotal", 2);
    assertXPathCount (aCN, "cac:TaxTotal/cac:TaxSubtotal", 2);
    assertXPath (aCN, "cac:LegalMonetaryTotal/cbc:PayableAmount", "100");
    assertXPath (aCN, "cac:CreditNoteLine/cac:Item/cbc:Name", "Widget");
    assertXPath (aCN, "cac:CreditNoteLine/cac:Price/cbc:PriceAmount", "25");
    assertXPath (aCN, "cac:CreditNoteLine/cac:Item/cac:AdditionalItemProperty[1]/cbc:Value", "Blue");
  }

  @Test
  public void testConvertNewHeaderTerms ()
  {
    final Element aInv = convertAndValidate ("d25a-new-header-invoice.xml", true);

    // BT-2 + BT-166: CII @format "208" carries date AND time in a single element
    assertXPath (aInv, "cbc:IssueDate", "2026-01-15");
    assertXPath (aInv, "cbc:IssueTime", "12:05:03+01:00");

    // BT-167 + BT-167-1 + BT-167-2 VAT accounting currency exchange rate
    assertXPath (aInv, "cac:TaxExchangeRate/cbc:CalculationRate", "1.1000");
    assertXPath (aInv, "cac:TaxExchangeRate/cbc:TargetCurrencyCode", "EUR");
    assertXPath (aInv, "cac:TaxExchangeRate/cbc:SourceCurrencyCode", "USD");

    // BT-197 Delivery note reference
    assertXPath (aInv, "cac:DeliveryNoteDocumentReference/cbc:ID", "DELNOTE-8");

    // BT-202 Preceding invoice type code
    assertXPath (aInv,
                 "cac:BillingReference[cac:InvoiceDocumentReference/cbc:ID='PREV-INV-1']/cac:InvoiceDocumentReference/cbc:DocumentTypeCode",
                 "380");
    // The second preceding invoice has no type code
    assertNoXPath (aInv,
                   "cac:BillingReference[cac:InvoiceDocumentReference/cbc:ID='PREV-INV-2']/cac:InvoiceDocumentReference/cbc:DocumentTypeCode");

    // BG-19 DIRECT DEBIT with BT-215 and BT-216
    final String sMandate = "cac:PaymentMeans/cac:PaymentMandate/";
    assertXPath (aInv, "cac:PaymentMeans/cbc:PaymentMeansCode", "59");
    // BT-89 Mandate reference identifier
    assertXPath (aInv, sMandate + "cbc:ID", "MANDATE-1");
    // BT-91 Debited account identifier
    assertXPath (aInv, sMandate + "cac:PayerFinancialAccount/cbc:ID", "AT022050302101023600");
    // BT-216 Debited account name
    assertXPath (aInv, sMandate + "cac:PayerFinancialAccount/cbc:Name", "Buyer Account");
    // BT-215 Debited account payment service provider identifier
    assertXPath (aInv, sMandate + "cac:PayerFinancialAccount/cac:FinancialInstitutionBranch/cbc:ID", "SPSBAT2SXXX");
    // BT-90 + BT-90-1 Bank assigned creditor identifier, on the Payee if present
    assertXPath (aInv, "cac:PayeeParty/cac:PartyIdentification[cbc:ID/@schemeID='SEPA']/cbc:ID", "SEPA-CRED-1");
  }

  @Test
  public void testConvertNewPaymentTermsGroups ()
  {
    final Element aInv = convertAndValidate ("d25a-new-paymentterms-invoice.xml", true);

    // BG-33, BG-35 and BG-36 all share cac:PaymentTerms, so one element is emitted per group
    assertXPathCount (aInv, "cac:PaymentTerms", 3);

    // BG-33 PAYMENT TERMS - BT-20 Payment term text
    assertXPath (aInv, "cac:PaymentTerms[cbc:Note]/cbc:Note", "Net 30 days");

    // BG-35 EARLY PAYMENT DISCOUNT
    final String sDiscount = "cac:PaymentTerms[cbc:SettlementDiscountPercent]/";
    // BT-170 Discount end date
    assertXPath (aInv, sDiscount + "cac:SettlementPeriod/cbc:EndDate", "2026-01-25");
    // BT-171 Discount percentage
    assertXPath (aInv, sDiscount + "cbc:SettlementDiscountPercent", "2.00");
    // BT-172 Discount amount
    assertXPath (aInv, sDiscount + "cbc:SettlementDiscountAmount", "2.64");

    // BG-36 LATE PAYMENT PENALTY
    final String sPenalty = "cac:PaymentTerms[cbc:PenaltyAmount]/";
    // BT-181 Penalty start date
    assertXPath (aInv, sPenalty + "cac:PenaltyPeriod/cbc:StartDate", "2026-02-15");
    // BT-182 Penalty yearly interest percentage
    assertXPath (aInv, sPenalty + "cac:PenaltyInterestRate/cbc:InterestRatePercent", "9.20");
    // BT-183 Penalty amount
    // copyAmount strips trailing zeroes, so 12.50 becomes 12.5
    assertXPath (aInv, sPenalty + "cbc:PenaltyAmount", "12.5");

    // The three groups must not be merged into one element
    assertNoXPath (aInv, "cac:PaymentTerms[cbc:Note and cbc:SettlementDiscountPercent]");
    assertNoXPath (aInv, "cac:PaymentTerms[cbc:SettlementDiscountPercent and cbc:PenaltyAmount]");
  }

  @Test
  public void testConvertNewBG34 ()
  {
    // BG-34 CHARGES ON BEHALF OF A THIRD PARTY
    final Element aInv = convertAndValidate ("d25a-new-bg34-invoice.xml", true);

    assertXPathCount (aInv, "cac:CollectionInvoiceLine", 2);
    // BT-179-1 Line identifier - synthesised, since CII has no counterpart
    assertXPath (aInv, "cac:CollectionInvoiceLine[1]/cbc:ID", "1");
    // BT-179 Charge amount collected on behalf of a third party
    assertXPath (aInv, "cac:CollectionInvoiceLine[1]/cbc:TaxInclusiveLineExtensionAmount", "3.2");
    assertXPath (aInv, "cac:CollectionInvoiceLine[1]/cbc:TaxInclusiveLineExtensionAmount/@currencyID", "EUR");
    // BT-180 Charges specification
    assertXPath (aInv, "cac:CollectionInvoiceLine[1]/cac:Item/cbc:Description", "Copyright levy");

    assertXPath (aInv, "cac:CollectionInvoiceLine[2]/cbc:ID", "2");
    assertXPath (aInv, "cac:CollectionInvoiceLine[2]/cbc:TaxInclusiveLineExtensionAmount", "1.75");
    assertXPath (aInv, "cac:CollectionInvoiceLine[2]/cac:Item/cbc:Description", "Recycling fee");

    // The credit note uses cac:CollectionCreditNoteLine
    final Element aCN = convertAndValidate ("d25a-new-bg34-creditnote.xml", false);
    assertXPathCount (aCN, "cac:CollectionCreditNoteLine", 2);
    assertNoXPath (aCN, "cac:CollectionInvoiceLine");
    assertXPath (aCN, "cac:CollectionCreditNoteLine[1]/cbc:TaxInclusiveLineExtensionAmount", "3.2");
    assertXPath (aCN, "cac:CollectionCreditNoteLine[2]/cac:Item/cbc:Description", "Recycling fee");
  }

  @Test
  public void testConvertNewAllowanceChargeAndVatTerms ()
  {
    final Element aInv = convertAndValidate ("d25a-new-allowchg-invoice.xml", true);

    // BG-20 DOCUMENT LEVEL ALLOWANCES
    final String sAllow = "cac:AllowanceCharge[cbc:ChargeIndicator='false']/cac:TaxCategory/";
    // BT-173 Document level allowance exemption reason text
    assertXPath (aInv, sAllow + "cbc:TaxExemptionReason", "Intra-community supply");
    // BT-174 VAT exemption reason and specification code
    assertXPath (aInv, sAllow + "cbc:TaxExemptionReasonCode", "VATEX-EU-IC");
    // BT-213 Document level allowance goods/services code
    assertXPath (aInv, sAllow + "cbc:SupplyTypeCode", "SUPPLY-A");

    // BG-21 DOCUMENT LEVEL CHARGES AND TAXES
    final String sCharge = "cac:AllowanceCharge[cbc:ChargeIndicator='true']/";
    // BT-177 Document level non-VAT tax code + BT-177-1 its list identifier.
    // BT-105 and BT-177 share cbc:AllowanceChargeReasonCode and are told apart by @listID.
    assertXPath (aInv, sCharge + "cbc:AllowanceChargeReasonCode", "ENV");
    assertXPath (aInv, sCharge + "cbc:AllowanceChargeReasonCode/@listID", "5153");
    assertXPath (aInv, sCharge + "cbc:AllowanceChargeReasonCode/@listAgencyID", "6");
    // BT-175 Document level charge or tax exemption reason text
    assertXPath (aInv, sCharge + "cac:TaxCategory/cbc:TaxExemptionReason", "Not subject to VAT");
    // BT-176 VAT exemption reason and specification code of the charge
    assertXPath (aInv, sCharge + "cac:TaxCategory/cbc:TaxExemptionReasonCode", "VATEX-EU-O");
    // BT-214 Document level charge goods/services code
    assertXPath (aInv, sCharge + "cac:TaxCategory/cbc:SupplyTypeCode", "SUPPLY-B");

    // BT-105 keeps no list identifier, so the allowance reason code stays plain
    assertNoXPath (aInv, "cac:AllowanceCharge[cbc:ChargeIndicator='false']/cbc:AllowanceChargeReasonCode/@listID");

    // BG-23 VAT BREAKDOWN
    final String sVat2 = "cac:TaxTotal/cac:TaxSubtotal[cac:TaxCategory/cbc:ID='AE']/";
    // BT-184 VAT breakdown currency overrides the invoice currency for this breakdown
    assertXPath (aInv, sVat2 + "cbc:TaxAmount/@currencyID", "USD");
    // BT-210 VAT breakdown goods/services code
    assertXPath (aInv, sVat2 + "cac:TaxCategory/cbc:SupplyTypeCode", "SUPPLY-C");
    // The other breakdown keeps the invoice currency
    assertXPath (aInv, "cac:TaxTotal/cac:TaxSubtotal[cac:TaxCategory/cbc:ID='S']/cbc:TaxAmount/@currencyID", "EUR");
  }
}
