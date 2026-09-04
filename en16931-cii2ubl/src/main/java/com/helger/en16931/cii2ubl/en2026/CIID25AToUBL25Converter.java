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

import java.io.Serializable;
import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.diagnostics.error.list.ErrorList;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.CreditNoteLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.InvoiceLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.ItemType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.MonetaryTotalType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.PriceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.creditnote_25.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_25.InvoiceType;
import un.unece.uncefact.data.standard.cii.d25a.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.ExchangedDocumentContextType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.ExchangedDocumentType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.HeaderTradeAgreementType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.HeaderTradeDeliveryType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.LineTradeAgreementType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.LineTradeDeliveryType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.LineTradeSettlementType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.SupplyChainTradeLineItemType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.SupplyChainTradeTransactionType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradePartyType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradePriceType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradeProductType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradeSettlementHeaderMonetarySummationType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradeSettlementLineMonetarySummationType;
import un.unece.uncefact.data.standard.cii.d25a.udt.AmountType;
import un.unece.uncefact.data.standard.cii.d25a.udt.TextType;

/**
 * CII D25A to UBL 2.5 converter, following the EN 16931:2026 syntax binding.<br>
 * Note: this converter is under construction. It currently only covers the business terms needed
 * for a schema valid result. See <code>docs/plan-4.0.0.md</code> for the outstanding action items.
 *
 * @author Philip Helger
 * @since 4.0.0
 */
public class CIID25AToUBL25Converter extends AbstractCIIToUBL2026Converter <CIID25AToUBL25Converter>
{
  private static final String UBL_VERSION = "2.5";

  public CIID25AToUBL25Converter ()
  {}

  @Nullable
  private static String _getSellerName (@Nullable final HeaderTradeAgreementType aHeaderAgreement)
  {
    // BT-27 Seller name
    if (aHeaderAgreement == null)
      return null;

    final TradePartyType aSeller = aHeaderAgreement.getSellerTradeParty ();
    return aSeller == null ? null : aSeller.getNameValue ();
  }

  @NonNull
  private static SupplierPartyType _createUBLSupplier (@Nullable final HeaderTradeAgreementType aHeaderAgreement)
  {
    // BG-4 SELLER - mandatory in UBL
    final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
    final PartyType aUBLParty = new PartyType ();
    ifNotEmpty (_getSellerName (aHeaderAgreement), s -> {
      final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.PartyNameType aUBLPartyName = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.PartyNameType ();
      aUBLPartyName.setName (s);
      aUBLParty.addPartyName (aUBLPartyName);
    });
    aUBLSupplier.setParty (aUBLParty);
    return aUBLSupplier;
  }

  @NonNull
  private static CustomerPartyType _createUBLCustomer (@Nullable final HeaderTradeAgreementType aHeaderAgreement)
  {
    // BG-7 BUYER
    final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
    final PartyType aUBLParty = new PartyType ();
    final TradePartyType aBuyer = aHeaderAgreement == null ? null : aHeaderAgreement.getBuyerTradeParty ();
    ifNotEmpty (aBuyer == null ? null : aBuyer.getNameValue (), s -> {
      final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.PartyNameType aUBLPartyName = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_25.PartyNameType ();
      aUBLPartyName.setName (s);
      aUBLParty.addPartyName (aUBLPartyName);
    });
    aUBLCustomer.setParty (aUBLParty);
    return aUBLCustomer;
  }

  @NonNull
  private static MonetaryTotalType _createUBLMonetaryTotal (@Nullable final HeaderTradeSettlementType aHeaderSettlement,
                                                            @Nullable final String sCurrency)
  {
    // BG-22 DOCUMENT TOTALS - mandatory in UBL
    final MonetaryTotalType aUBLMonetaryTotal = new MonetaryTotalType ();

    final TradeSettlementHeaderMonetarySummationType aSummation = aHeaderSettlement == null ? null
                                                                                            : aHeaderSettlement.getSpecifiedTradeSettlementHeaderMonetarySummation ();
    if (aSummation != null)
    {
      // BT-112 Invoice total amount with VAT
      if (aSummation.hasGrandTotalAmountEntries ())
      {
        final AmountType aCIIAmount = aSummation.getGrandTotalAmount ().get (0);
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.TaxInclusiveAmountType aUBLAmount = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.TaxInclusiveAmountType ();
        ifNotNull (copyAmount (aCIIAmount, aUBLAmount, sCurrency), aUBLMonetaryTotal::setTaxInclusiveAmount);
      }

      // BT-115 Amount due for payment
      if (aSummation.hasDuePayableAmountEntries ())
      {
        final AmountType aCIIAmount = aSummation.getDuePayableAmount ().get (0);
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.PayableAmountType aUBLAmount = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.PayableAmountType ();
        ifNotNull (copyAmount (aCIIAmount, aUBLAmount, sCurrency), aUBLMonetaryTotal::setPayableAmount);
      }
    }
    return aUBLMonetaryTotal;
  }

  @Nullable
  private static ItemType _createUBLItem (@Nullable final TradeProductType aProduct)
  {
    // BG-31 ITEM INFORMATION
    if (aProduct == null)
      return null;

    final ItemType aUBLItem = new ItemType ();
    // BT-153 Item name - since UBL 2.5 this is a 0..n element
    for (final TextType aName : aProduct.getName ())
      ifNotNull (copyName (aName, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.NameType ()),
                 aUBLItem::addName);
    return aUBLItem;
  }

  @Nullable
  private static PriceType _createUBLPrice (@Nullable final LineTradeAgreementType aLineAgreement,
                                            @Nullable final String sCurrency)
  {
    // BG-29 PRICE DETAILS - BT-146 Item net price
    if (aLineAgreement == null)
      return null;

    final TradePriceType aNetPrice = aLineAgreement.getNetPriceProductTradePrice ();
    if (aNetPrice == null || aNetPrice.hasNoChargeAmountEntries ())
      return null;

    final PriceType aUBLPrice = new PriceType ();
    final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.PriceAmountType aUBLAmount = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.PriceAmountType ();
    if (copyAmount (aNetPrice.getChargeAmount ().get (0), aUBLAmount, sCurrency) == null)
      return null;

    aUBLPrice.setPriceAmount (aUBLAmount);
    return aUBLPrice;
  }

  @Nullable
  private static String _getLineID (@NonNull final SupplyChainTradeLineItemType aLineItem)
  {
    // BT-126 Invoice line identifier
    return aLineItem.getAssociatedDocumentLineDocument () == null ? null : aLineItem.getAssociatedDocumentLineDocument ()
                                                                                   .getLineIDValue ();
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.@Nullable LineExtensionAmountType _getLineExtensionAmount (@NonNull final SupplyChainTradeLineItemType aLineItem,
                                                                                                                                   @Nullable final String sCurrency)
  {
    // BT-131 Invoice line net amount
    final LineTradeSettlementType aLineSettlement = aLineItem.getSpecifiedLineTradeSettlement ();
    final TradeSettlementLineMonetarySummationType aSummation = aLineSettlement == null ? null
                                                                                        : aLineSettlement.getSpecifiedTradeSettlementLineMonetarySummation ();
    if (aSummation == null || aSummation.hasNoLineTotalAmountEntries ())
      return null;

    return copyAmount (aSummation.getLineTotalAmount ().get (0),
                       new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.LineExtensionAmountType (),
                       sCurrency);
  }

  @NonNull
  private static InvoiceLineType _createUBLInvoiceLine (@NonNull final SupplyChainTradeLineItemType aLineItem,
                                                        @Nullable final String sCurrency)
  {
    // BG-25 INVOICE LINE
    final InvoiceLineType aUBLLine = new InvoiceLineType ();
    ifNotEmpty (_getLineID (aLineItem), aUBLLine::setID);

    // BT-129 Invoiced quantity + BT-130 Invoiced quantity unit of measure code
    final LineTradeDeliveryType aLineDelivery = aLineItem.getSpecifiedLineTradeDelivery ();
    if (aLineDelivery != null)
      ifNotNull (copyQuantity (aLineDelivery.getBilledQuantity (),
                               new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.InvoicedQuantityType ()),
                 aUBLLine::setInvoicedQuantity);

    ifNotNull (_getLineExtensionAmount (aLineItem, sCurrency), aUBLLine::setLineExtensionAmount);
    ifNotNull (_createUBLItem (aLineItem.getSpecifiedTradeProduct ()), aUBLLine::setItem);
    ifNotNull (_createUBLPrice (aLineItem.getSpecifiedLineTradeAgreement (), sCurrency), aUBLLine::setPrice);
    return aUBLLine;
  }

  @NonNull
  private static CreditNoteLineType _createUBLCreditNoteLine (@NonNull final SupplyChainTradeLineItemType aLineItem,
                                                              @Nullable final String sCurrency)
  {
    // BG-25 INVOICE LINE
    final CreditNoteLineType aUBLLine = new CreditNoteLineType ();
    ifNotEmpty (_getLineID (aLineItem), aUBLLine::setID);

    // BT-129 Invoiced quantity + BT-130 Invoiced quantity unit of measure code
    final LineTradeDeliveryType aLineDelivery = aLineItem.getSpecifiedLineTradeDelivery ();
    if (aLineDelivery != null)
      ifNotNull (copyQuantity (aLineDelivery.getBilledQuantity (),
                               new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_25.CreditedQuantityType ()),
                 aUBLLine::setCreditedQuantity);

    ifNotNull (_getLineExtensionAmount (aLineItem, sCurrency), aUBLLine::setLineExtensionAmount);
    ifNotNull (_createUBLItem (aLineItem.getSpecifiedTradeProduct ()), aUBLLine::setItem);
    ifNotNull (_createUBLPrice (aLineItem.getSpecifiedLineTradeAgreement (), sCurrency), aUBLLine::setPrice);
    return aUBLLine;
  }

  @Nullable
  public InvoiceType convertToInvoice (@NonNull final CrossIndustryInvoiceType aCIIInvoice,
                                       @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aCIIInvoice, "CIIInvoice");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final ExchangedDocumentType aED = aCIIInvoice.getExchangedDocument ();
    final SupplyChainTradeTransactionType aSCTT = aCIIInvoice.getSupplyChainTradeTransaction ();
    if (aSCTT == null)
    {
      // Mandatory element
      return null;
    }

    final HeaderTradeAgreementType aHeaderAgreement = aSCTT.getApplicableHeaderTradeAgreement ();
    final HeaderTradeDeliveryType aHeaderDelivery = aSCTT.getApplicableHeaderTradeDelivery ();
    final HeaderTradeSettlementType aHeaderSettlement = aSCTT.getApplicableHeaderTradeSettlement ();
    if (aHeaderAgreement == null || aHeaderDelivery == null || aHeaderSettlement == null)
    {
      // All mandatory elements
      return null;
    }

    final InvoiceType aUBLInvoice = new InvoiceType ();
    if (false)
      aUBLInvoice.setUBLVersionID (UBL_VERSION);

    final ExchangedDocumentContextType aEDC = aCIIInvoice.getExchangedDocumentContext ();
    if (aEDC != null)
    {
      if (aEDC.hasBusinessProcessSpecifiedDocumentContextParameterEntries ())
      {
        // BT-23
        aUBLInvoice.setProfileID (aEDC.getBusinessProcessSpecifiedDocumentContextParameter ()
                                      .get (0)
                                      .getIDValue ());
      }
      if (aEDC.hasGuidelineSpecifiedDocumentContextParameterEntries ())
      {
        // BT-24
        aUBLInvoice.setCustomizationID (aEDC.getGuidelineSpecifiedDocumentContextParameter ().get (0).getIDValue ());
      }
    }

    // Overwrite with custom values, if provided
    if (StringHelper.isNotEmpty (getProfileID ()))
      aUBLInvoice.setProfileID (getProfileID ());
    if (StringHelper.isNotEmpty (getCustomizationID ()))
      aUBLInvoice.setCustomizationID (getCustomizationID ());

    // BT-1 Invoice number
    if (aED != null)
      aUBLInvoice.setID (aED.getIDValue ());

    // BT-2 Invoice issue date
    {
      LocalDate aIssueDate = null;
      if (aED != null && aED.getIssueDateTime () != null)
        aIssueDate = parseDate (aED.getIssueDateTime ().getDateTimeString (), aErrorList);

      if (aIssueDate != null)
        aUBLInvoice.setIssueDate (aIssueDate);
    }

    // BT-3 Invoice type code
    if (aED != null)
      aUBLInvoice.setInvoiceTypeCode (aED.getTypeCodeValue ());

    // BT-5 Invoice currency code
    final String sCurrency = aHeaderSettlement.getInvoiceCurrencyCodeValue ();
    aUBLInvoice.setDocumentCurrencyCode (sCurrency);

    // Mandatory supplier
    aUBLInvoice.setAccountingSupplierParty (_createUBLSupplier (aHeaderAgreement));

    // Mandatory customer
    aUBLInvoice.setAccountingCustomerParty (_createUBLCustomer (aHeaderAgreement));

    // Mandatory totals
    aUBLInvoice.setLegalMonetaryTotal (_createUBLMonetaryTotal (aHeaderSettlement, sCurrency));

    // BG-25 INVOICE LINE - at least one is mandatory in UBL
    for (final SupplyChainTradeLineItemType aLineItem : aSCTT.getIncludedSupplyChainTradeLineItem ())
      aUBLInvoice.addInvoiceLine (_createUBLInvoiceLine (aLineItem, sCurrency));

    return aUBLInvoice;
  }

  @Nullable
  public CreditNoteType convertToCreditNote (@NonNull final CrossIndustryInvoiceType aCIICreditNote,
                                             @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aCIICreditNote, "CIICreditNote");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final ExchangedDocumentType aED = aCIICreditNote.getExchangedDocument ();
    final SupplyChainTradeTransactionType aSCTT = aCIICreditNote.getSupplyChainTradeTransaction ();
    if (aSCTT == null)
    {
      // Mandatory element
      return null;
    }

    final HeaderTradeAgreementType aHeaderAgreement = aSCTT.getApplicableHeaderTradeAgreement ();
    final HeaderTradeDeliveryType aHeaderDelivery = aSCTT.getApplicableHeaderTradeDelivery ();
    final HeaderTradeSettlementType aHeaderSettlement = aSCTT.getApplicableHeaderTradeSettlement ();
    if (aHeaderAgreement == null || aHeaderDelivery == null || aHeaderSettlement == null)
    {
      // All mandatory elements
      return null;
    }

    final CreditNoteType aUBLCreditNote = new CreditNoteType ();
    if (false)
      aUBLCreditNote.setUBLVersionID (UBL_VERSION);

    final ExchangedDocumentContextType aEDC = aCIICreditNote.getExchangedDocumentContext ();
    if (aEDC != null)
    {
      if (aEDC.hasBusinessProcessSpecifiedDocumentContextParameterEntries ())
      {
        // BT-23
        aUBLCreditNote.setProfileID (aEDC.getBusinessProcessSpecifiedDocumentContextParameter ()
                                         .get (0)
                                         .getIDValue ());
      }
      if (aEDC.hasGuidelineSpecifiedDocumentContextParameterEntries ())
      {
        // BT-24
        aUBLCreditNote.setCustomizationID (aEDC.getGuidelineSpecifiedDocumentContextParameter ().get (0).getIDValue ());
      }
    }

    // Overwrite with custom values, if provided
    if (StringHelper.isNotEmpty (getProfileID ()))
      aUBLCreditNote.setProfileID (getProfileID ());
    if (StringHelper.isNotEmpty (getCustomizationID ()))
      aUBLCreditNote.setCustomizationID (getCustomizationID ());

    // BT-1 Invoice number
    if (aED != null)
      aUBLCreditNote.setID (aED.getIDValue ());

    // BT-2 Invoice issue date
    {
      LocalDate aIssueDate = null;
      if (aED != null && aED.getIssueDateTime () != null)
        aIssueDate = parseDate (aED.getIssueDateTime ().getDateTimeString (), aErrorList);

      if (aIssueDate != null)
        aUBLCreditNote.setIssueDate (aIssueDate);
    }

    // BT-3 Invoice type code
    if (aED != null)
      aUBLCreditNote.setCreditNoteTypeCode (aED.getTypeCodeValue ());

    // BT-5 Invoice currency code
    final String sCurrency = aHeaderSettlement.getInvoiceCurrencyCodeValue ();
    aUBLCreditNote.setDocumentCurrencyCode (sCurrency);

    // Mandatory supplier
    aUBLCreditNote.setAccountingSupplierParty (_createUBLSupplier (aHeaderAgreement));

    // Mandatory customer
    aUBLCreditNote.setAccountingCustomerParty (_createUBLCustomer (aHeaderAgreement));

    // Mandatory totals
    aUBLCreditNote.setLegalMonetaryTotal (_createUBLMonetaryTotal (aHeaderSettlement, sCurrency));

    // BG-25 INVOICE LINE - at least one is mandatory in UBL
    for (final SupplyChainTradeLineItemType aLineItem : aSCTT.getIncludedSupplyChainTradeLineItem ())
      aUBLCreditNote.addCreditNoteLine (_createUBLCreditNoteLine (aLineItem, sCurrency));

    return aUBLCreditNote;
  }

  @Override
  @Nullable
  public Serializable convertCIItoUBL (@NonNull final CrossIndustryInvoiceType aCIIInvoice,
                                       @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aCIIInvoice, "CIIInvoice");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    return switch (getUBLCreationMode ())
    {
      case AUTOMATIC ->
      {
        final ETriState eIsInvoice = isInvoiceType (aCIIInvoice, aErrorList);
        // Default to invoice
        yield eIsInvoice.getAsBooleanValue (true) ? convertToInvoice (aCIIInvoice, aErrorList) : convertToCreditNote (
                                                                                                                      aCIIInvoice,
                                                                                                                      aErrorList);
      }
      case INVOICE -> convertToInvoice (aCIIInvoice, aErrorList);
      case CREDIT_NOTE -> convertToCreditNote (aCIIInvoice, aErrorList);
    };
  }
}
