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
package com.helger.en16931.cii2ubl.en2017;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.cii.d16b.CIID16BCrossIndustryInvoiceTypeMarshaller;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.diagnostics.error.list.IErrorList;
import com.helger.en16931.cii2ubl.AbstractCIIToUBLConverterBase;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ExchangedDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.SupplyChainTradeTransactionType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradePartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeSettlementHeaderMonetarySummationType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IndicatorType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

/**
 * Base class for the conversion from CII D16B to UBL according to the EN 16931:2017 syntax binding.
 *
 * @author Philip Helger
 * @param <IMPLTYPE>
 *        The implementation type
 * @since 4.0.0
 */
public abstract class AbstractCIIToUBL2017Converter <IMPLTYPE extends AbstractCIIToUBL2017Converter <IMPLTYPE>> extends
                                                    AbstractCIIToUBLConverterBase <IMPLTYPE>
{
  // BT-3 Invoice type code, UNTDID 1001.
  // Source: EN 16931 validation artefacts 1.3.16, EN16931-UBL-validation.xslt.
  // Note that "81" is deliberately in both lists - the 2017 artefacts allow it on an Invoice as
  // well as on a Credit Note, and the Invoice list is evaluated first. EN 16931:2026 resolved this
  // and lists "81" as a Credit Note only.
  private static final Set <String> CREDIT_NOTE_TYPE_CODES = StringHelper.getExplodedToSet (" ",
                                                                                                "81 83 261 262 296 308 381 396 420 458 502 503 532");
  private static final Set <String> INVOICE_TYPE_CODES = StringHelper.getExplodedToSet (" ",
                                                                                            "71 80 81 82 84 102 130 202 203 204 211 218 219 295 325 326 331 380 382 383 384 385 386 387 388 389 390 393 394 395 456 457 471 472 473 500 501 527 553 575 623 633 751 780 817 870 875 876 877 935");

  protected AbstractCIIToUBL2017Converter ()
  {}

  @Nullable
  protected static LocalDate parseDate (final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.@Nullable DateTimeString aDateObj,
                                        @NonNull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate parseDate (final un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType.@Nullable DateTimeString aDateObj,
                                        @NonNull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate parseDate (final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateType.@Nullable DateString aDateObj,
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

    return parseIndicator (aIndicator.isIndicator (),
                           aIndicator.getIndicatorString () != null,
                           aIndicator.getIndicatorStringValue (),
                           aIndicator,
                           aErrorList);
  }

  /**
   * Copy all ID parts from a CII ID to a CCTS/UBL ID.<br>
   * Unlike copyName, copyQuantity and copyAmount this is not delegated to
   * AbstractCIIToUBLConverterBase. It copies eight attributes and contains no logic, so a shared
   * variant would need eight consecutive String parameters - and six of them (schemeName,
   * schemeAgencyID, schemeAgencyName, schemeVersionID, schemeDataURI, schemeURI) never occur in the
   * test corpus, so a transposed argument would go unnoticed. The same applies to copyCode below.
   * The duplication is cheaper than that risk.
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

    return copyName (aName.getValue (), aName.getLanguageID (), aName.getLanguageLocaleID (), ret);
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

    return copyQuantity (aQuantity.getValue (),
                         aQuantity.getUnitCode (),
                         aQuantity.getUnitCodeListID (),
                         aQuantity.getUnitCodeListAgencyID (),
                         aQuantity.getUnitCodeListAgencyName (),
                         ret);
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.AmountType> T copyAmount (@Nullable final AmountType aAmount,
                                                                                              @NonNull final T ret,
                                                                                              @Nullable final String sDefaultCurrencyCode)
  {
    if (aAmount == null)
      return null;

    return copyAmount (aAmount.getValue (),
                       aAmount.getCurrencyID (),
                       aAmount.getCurrencyCodeListVersionID (),
                       ret,
                       sDefaultCurrencyCode);
  }

  protected static boolean canUseGlobalID (@NonNull final TradePartyType aParty)
  {
    // GloablID, if global identifier exists and can be stated in @schemeID, ID
    // else
    if (aParty.hasGlobalIDEntries ())
      for (final IDType aID : aParty.getGlobalID ())
        if (isUsableGlobalID (aID.getValue (), aID.getSchemeID ()))
          return true;
    return false;
  }

  @NonNull
  protected static ICommonsList <IDType> getAllUsableGlobalIDs (@NonNull final TradePartyType aParty)
  {
    return CommonsArrayList.createFiltered (aParty.getGlobalID (),
                                            x -> isUsableGlobalID (x.getValue (), x.getSchemeID ()));
  }

  @NonNull
  protected static ETriState isInvoiceType (@NonNull final CrossIndustryInvoiceType aCIIInvoice,
                                            @NonNull final IErrorList aErrorList)
  {
    // BT-3 Invoice type code
    final ExchangedDocumentType aExchangedDoc = aCIIInvoice.getExchangedDocument ();
    final String sTypeCode = aExchangedDoc == null ? null : aExchangedDoc.getTypeCodeValue ();

    // BT-115 Amount due for payment
    final SupplyChainTradeTransactionType aTransaction = aCIIInvoice.getSupplyChainTradeTransaction ();
    final HeaderTradeSettlementType aSettlement = aTransaction == null ? null
                                                                       : aTransaction.getApplicableHeaderTradeSettlement ();
    final TradeSettlementHeaderMonetarySummationType aTotal = aSettlement == null ? null
                                                                                  : aSettlement.getSpecifiedTradeSettlementHeaderMonetarySummation ();
    final AmountType aDuePayable = aTotal == null || aTotal.hasNoDuePayableAmountEntries () ? null
                                                                                            : aTotal.getDuePayableAmount ()
                                                                                                    .get (0);

    return isInvoiceType (INVOICE_TYPE_CODES,
                          CREDIT_NOTE_TYPE_CODES,
                          sTypeCode,
                          aDuePayable == null ? null : aDuePayable.getValue (),
                          aDuePayable,
                          aErrorList);
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
    final CrossIndustryInvoiceType aCIIInvoice = new CIID16BCrossIndustryInvoiceTypeMarshaller ().setValidationEventHandler (new WrappedCollectingValidationEventHandler (aErrorList))
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
