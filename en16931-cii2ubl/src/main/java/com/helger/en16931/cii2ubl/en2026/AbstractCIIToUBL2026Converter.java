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

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.numeric.BigHelper;
import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.cii.d25a.CIID25ACrossIndustryInvoiceTypeMarshaller;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.diagnostics.error.list.IErrorList;
import com.helger.en16931.cii2ubl.AbstractCIIToUBLConverterBase;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import oasis.names.specification.ubl.schema.xsd.creditnote_25.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_25.InvoiceType;
import un.unece.uncefact.data.standard.cii.d25a.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.ExchangedDocumentType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.SupplyChainTradeTransactionType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradePartyType;
import un.unece.uncefact.data.standard.cii.d25a.rabie.TradeSettlementHeaderMonetarySummationType;
import un.unece.uncefact.data.standard.cii.d25a.udt.AmountType;
import un.unece.uncefact.data.standard.cii.d25a.udt.CodeType;
import un.unece.uncefact.data.standard.cii.d25a.udt.IDType;
import un.unece.uncefact.data.standard.cii.d25a.udt.IndicatorType;
import un.unece.uncefact.data.standard.cii.d25a.udt.QuantityType;
import un.unece.uncefact.data.standard.cii.d25a.udt.TextType;

/**
 * Base class for the conversion from CII D25A to UBL according to the EN 16931:2026 syntax binding.
 *
 * @author Philip Helger
 * @param <IMPLTYPE>
 *        The implementation type
 * @since 4.0.0
 */
public abstract class AbstractCIIToUBL2026Converter <IMPLTYPE extends AbstractCIIToUBL2026Converter <IMPLTYPE>>
                                                    extends
                                                    AbstractCIIToUBLConverterBase <IMPLTYPE>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractCIIToUBL2026Converter.class);

  protected AbstractCIIToUBL2026Converter ()
  {}

  @Nullable
  protected static LocalDate parseDate (final un.unece.uncefact.data.standard.cii.d25a.udt.DateTimeType.@Nullable DateTimeString aDateObj,
                                        @NonNull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate parseDate (final un.unece.uncefact.data.standard.cii.d25a.qdt.FormattedDateTimeType.@Nullable DateTimeString aDateObj,
                                        @NonNull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate parseDate (final un.unece.uncefact.data.standard.cii.d25a.udt.DateType.@Nullable DateString aDateObj,
                                        @NonNull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @NonNull
  protected static ETriState parseIndicator (@Nullable final IndicatorType aIndicator,
                                             @NonNull final IErrorList aErrorList)
  {
    if (aIndicator == null)
      return ETriState.UNDEFINED;

    // Choice
    if (aIndicator.isIndicator () != null)
      return ETriState.valueOf (aIndicator.isIndicator ().booleanValue ());

    if (aIndicator.getIndicatorString () != null)
    {
      final String sIndicator = aIndicator.getIndicatorStringValue ();
      // Parse string
      if (sIndicator == null)
        return ETriState.UNDEFINED;
      if ("true".equals (sIndicator))
        return ETriState.TRUE;
      if ("false".equals (sIndicator))
        return ETriState.FALSE;

      aErrorList.add (buildError (null,
                                  "Failed to parse the indicator value '" + aIndicator + "' to a boolean value."));
      return ETriState.UNDEFINED;
    }

    throw new IllegalStateException ("Indicator has neither string nor boolean");
  }

  /**
   * Copy all ID parts from a CII ID to a CCTS/UBL ID.
   *
   * @param aCIIID
   *        CII ID
   * @param aUBLID
   *        UBL ID
   * @return Created UBL ID
   */
  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.IdentifierType> T copyID (@Nullable final IDType aCIIID,
                                                                                              @NonNull final T aUBLID)
  {
    if (aCIIID == null)
      return null;

    // Avoid empty element
    if (StringHelper.isEmpty (aCIIID.getValue ()))
      return null;

    aUBLID.setValue (aCIIID.getValue ());
    aUBLID.setSchemeID (aCIIID.getSchemeID ());
    aUBLID.setSchemeName (aCIIID.getSchemeName ());
    aUBLID.setSchemeAgencyID (aCIIID.getSchemeAgencyID ());
    aUBLID.setSchemeAgencyName (aCIIID.getSchemeAgencyName ());
    aUBLID.setSchemeVersionID (aCIIID.getSchemeVersionID ());
    aUBLID.setSchemeDataURI (aCIIID.getSchemeDataURI ());
    aUBLID.setSchemeURI (aCIIID.getSchemeURI ());
    return aUBLID;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.TextType> T copyName (@Nullable final TextType aName,
                                                                                          @NonNull final T ret)
  {
    if (aName == null)
      return null;

    // Avoid empty element
    if (StringHelper.isEmpty (aName.getValue ()))
      return null;

    ret.setValue (aName.getValue ());
    ret.setLanguageID (aName.getLanguageID ());
    ret.setLanguageLocaleID (aName.getLanguageLocaleID ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.CodeType> T copyCode (@Nullable final CodeType aCode,
                                                                                          @NonNull final T ret)
  {
    if (aCode == null)
      return null;

    // Avoid empty element
    if (StringHelper.isEmpty (aCode.getValue ()))
      return null;

    ret.setValue (aCode.getValue ());
    ret.setListID (aCode.getListID ());
    ret.setListAgencyID (aCode.getListAgencyID ());
    ret.setListAgencyName (aCode.getListAgencyName ());
    ret.setListName (aCode.getListName ());
    ret.setListVersionID (aCode.getListVersionID ());
    ret.setName (aCode.getName ());
    ret.setLanguageID (aCode.getLanguageID ());
    ret.setListURI (aCode.getListURI ());
    ret.setListSchemeURI (aCode.getListSchemeURI ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.QuantityType> T copyQuantity (@Nullable final QuantityType aQuantity,
                                                                                                  @NonNull final T ret)
  {
    if (aQuantity == null)
      return null;

    // Avoid empty element
    if (aQuantity.getValue () == null)
      return null;

    ret.setValue (BigHelper.getWithoutTrailingZeroes (aQuantity.getValue ()));
    ret.setUnitCode (aQuantity.getUnitCode ());
    ret.setUnitCodeListID (aQuantity.getUnitCodeListID ());
    ret.setUnitCodeListAgencyID (aQuantity.getUnitCodeListAgencyID ());
    ret.setUnitCodeListAgencyName (aQuantity.getUnitCodeListAgencyName ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.AmountType> T copyAmount (@Nullable final AmountType aAmount,
                                                                                              @NonNull final T ret,
                                                                                              @Nullable final String sDefaultCurrencyCode)
  {
    if (aAmount == null)
      return null;

    // Avoid empty element
    if (aAmount.getValue () == null)
      return null;

    ret.setValue (BigHelper.getWithoutTrailingZeroes (aAmount.getValue ()));
    ret.setCurrencyID (aAmount.getCurrencyID ());
    if (StringHelper.isEmpty (ret.getCurrencyID ()))
      ret.setCurrencyID (sDefaultCurrencyCode);
    ret.setCurrencyCodeListVersionID (aAmount.getCurrencyCodeListVersionID ());
    return ret;
  }

  protected static boolean canUseGlobalID (@NonNull final TradePartyType aParty)
  {
    // GloablID, if global identifier exists and can be stated in @schemeID, ID
    // else
    if (aParty.hasGlobalIDEntries ())
      for (final IDType aID : aParty.getGlobalID ())
        if (StringHelper.isNotEmpty (aID.getValue ()) && StringHelper.isNotEmpty (aID.getSchemeID ()))
          return true;
    return false;
  }

  @NonNull
  protected static ICommonsList <IDType> getAllUsableGlobalIDs (@NonNull final TradePartyType aParty)
  {
    return CommonsArrayList.createFiltered (aParty.getGlobalID (),
                                            x -> StringHelper.isNotEmpty (x.getValue ()) &&
                                                 StringHelper.isNotEmpty (x.getSchemeID ()));
  }

  @NonNull
  protected static ETriState isInvoiceType (@NonNull final CrossIndustryInvoiceType aCIIInvoice,
                                            @NonNull final IErrorList aErrorList)
  {
    ETriState eIsInvoice = ETriState.UNDEFINED;

    // First check TypeCode
    final String sTypeCode;
    final ExchangedDocumentType aExchangedDoc = aCIIInvoice.getExchangedDocument ();
    if (aExchangedDoc != null)
    {
      sTypeCode = StringHelper.trim (aExchangedDoc.getTypeCodeValue ());
      if (INVOICE_TYPE_CODES.contains (sTypeCode))
        eIsInvoice = ETriState.TRUE;
      else
        if (CREDIT_NOTE_TYPE_CODES.contains (sTypeCode))
          eIsInvoice = ETriState.FALSE;
    }
    else
      sTypeCode = null;

    // Check total
    final SupplyChainTradeTransactionType aTransaction = aCIIInvoice.getSupplyChainTradeTransaction ();
    final HeaderTradeSettlementType aSettlement = aTransaction == null ? null : aTransaction
                                                                                            .getApplicableHeaderTradeSettlement ();
    final TradeSettlementHeaderMonetarySummationType aTotal = aSettlement == null ? null : aSettlement
                                                                                                      .getSpecifiedTradeSettlementHeaderMonetarySummation ();
    final AmountType aDuePayable = aTotal == null || aTotal.hasNoDuePayableAmountEntries () ? null : aTotal
                                                                                                           .getDuePayableAmount ()
                                                                                                           .get (0);

    if (eIsInvoice.isUndefined () && aDuePayable != null)
    {
      eIsInvoice = ETriState.valueOf (BigHelper.isGE0 (aDuePayable.getValue ()));
    }

    if (eIsInvoice.isUndefined ())
    {
      aErrorList.add (buildWarn (null,
                                 "Could not determine, if the provided CII document is an Invoice or a CreditNote. TypeCode is '" +
                                       sTypeCode +
                                       "'; DuePayable is " +
                                       aDuePayable));
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Determined the provided CII document to be " +
                      (eIsInvoice.isTrue () ? "an Invoice" : "a CreditNote"));
    }
    return eIsInvoice;
  }

  /**
   * Convert CII to UBL
   *
   * @param aFile
   *        Source file with CII to be parsed. May not be <code>null</code>.
   * @param aErrorList
   *        Error list to be filled. May not be <code>null</code>.
   * @return The parsed Invoice or CreditNote as UBL 2.x. May be <code>null</code> in case of error.
   */
  @Override
  @Nullable
  public Serializable convertCIItoUBL (@NonNull final File aFile, @NonNull final ErrorList aErrorList)
  {
    // Parse XML and convert to domain model
    final CrossIndustryInvoiceType aCIIInvoice = new CIID25ACrossIndustryInvoiceTypeMarshaller ().setValidationEventHandler (new WrappedCollectingValidationEventHandler (aErrorList))
                                                                                                 .read (aFile);
    if (aCIIInvoice == null)
      return null;

    return convertCIItoUBL (aCIIInvoice, aErrorList);
  }

  /**
   * Convert CII to UBL
   *
   * @param aCIIInvoice
   *        The CII invoice to be converted. May not be <code>null</code>. Ideally this is a valid
   *        CII invoice only and not some handcrafted domain object.
   * @param aErrorList
   *        Error list to be filled. May not be <code>null</code>.
   * @return The parsed {@link InvoiceType} or {@link CreditNoteType}. May be <code>null</code> in
   *         case of error.
   */
  @Nullable
  public abstract Serializable convertCIItoUBL (@NonNull CrossIndustryInvoiceType aCIIInvoice,
                                                @NonNull ErrorList aErrorList);
}
