/*
 * Copyright (C) 2019-2024 Philip Helger
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

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.cii.d16b.CIID16BCrossIndustryInvoiceTypeMarshaller;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.collection.impl.ICommonsSet;
import com.helger.commons.datetime.PDTFromString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.commons.traits.IGenericImplTrait;
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
 * Base class for conversion from CII to UBL.
 *
 * @author Philip Helger
 * @param <IMPLTYPE>
 *        The implementation type
 */
public abstract class AbstractCIIToUBLConverter <IMPLTYPE extends AbstractCIIToUBLConverter <IMPLTYPE>> implements
                                                IGenericImplTrait <IMPLTYPE>
{
  public static final EUBLCreationMode DEFAULT_UBL_CREATION_MODE = EUBLCreationMode.AUTOMATIC;
  public static final String DEFAULT_VAT_SCHEME = "VAT";
  public static final String DEFAULT_CUSTOMIZATION_ID = "urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0";
  public static final String DEFAULT_PROFILE_ID = "urn:fdc:peppol.eu:2017:poacc:billing:01:1.0";
  public static final String DEFAULT_CARD_ACCOUNT_NETWORK_ID = "mapped-from-cii";
  public static final String DEFAULT_DATE_TIME_FORMAT = "102";
  public static final String DEFAULT_ORDER_REF_ID = "";
  public static final boolean DEFAULT_SWAP_QUANTITY_SIGN_IF_NEEDED = true;
  public static final boolean DEFAULT_SWAP_PRICE_SIGN_IF_NEEDED = true;

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractCIIToUBLConverter.class);

  // Source: EN 16931 validation artefacts
  private static final ICommonsSet <String> CREDIT_NOTE_TYPE_CODES = StringHelper.getExplodedToSet (" ",
                                                                                                    "81 83 261 262 296 308 381 396 420 458 532");
  private static final ICommonsSet <String> INVOICE_TYPE_CODES = StringHelper.getExplodedToSet (" ",
                                                                                                "80 82 84 130 202 203 204 211 295 325 326 380 383 384 385 386 387 388 389 390 393 394 395 456 457 527 575 623 633 751 780 935");
  static
  {
    // XRechnung 2.1 extensions
    INVOICE_TYPE_CODES.add ("875");
    INVOICE_TYPE_CODES.add ("876");
    INVOICE_TYPE_CODES.add ("877");
  }

  private EUBLCreationMode m_eCreationMode = DEFAULT_UBL_CREATION_MODE;
  private String m_sVATScheme = DEFAULT_VAT_SCHEME;
  private String m_sCustomizationID = DEFAULT_CUSTOMIZATION_ID;
  private String m_sProfileID = DEFAULT_PROFILE_ID;
  private String m_sCardAccountNetworkID = DEFAULT_CARD_ACCOUNT_NETWORK_ID;
  private String m_sDefaultOrderRefID = DEFAULT_ORDER_REF_ID;
  private boolean m_bSwapQuantitySignIfNeeded = DEFAULT_SWAP_QUANTITY_SIGN_IF_NEEDED;
  private boolean m_bSwapPriceSignIfNeeded = DEFAULT_SWAP_PRICE_SIGN_IF_NEEDED;

  protected AbstractCIIToUBLConverter ()
  {}

  protected static <T> boolean ifNotNull (@Nonnull final Consumer <? super T> aConsumer, @Nullable final T aObj)
  {
    if (aObj == null)
      return false;
    aConsumer.accept (aObj);
    return true;
  }

  protected static boolean ifNotEmpty (@Nonnull final Consumer <? super String> aConsumer, @Nullable final String aObj)
  {
    if (aObj == null)
      return false;
    aConsumer.accept (aObj);
    return true;
  }

  @Nonnull
  public final EUBLCreationMode getUBLCreationMode ()
  {
    return m_eCreationMode;
  }

  @Nonnull
  public final IMPLTYPE setUBLCreationMode (@Nonnull final EUBLCreationMode eCreationMode)
  {
    ValueEnforcer.notNull (eCreationMode, "CreationMode");
    m_eCreationMode = eCreationMode;
    return thisAsT ();
  }

  @Nonnull
  public final String getVATScheme ()
  {
    return m_sVATScheme;
  }

  @Nonnull
  public final IMPLTYPE setVATScheme (@Nonnull final String sVATScheme)
  {
    ValueEnforcer.notNull (sVATScheme, "VATScheme");
    m_sVATScheme = sVATScheme;
    return thisAsT ();
  }

  @Nonnull
  public final String getCustomizationID ()
  {
    return m_sCustomizationID;
  }

  @Nonnull
  public final IMPLTYPE setCustomizationID (@Nonnull final String sCustomizationID)
  {
    ValueEnforcer.notNull (sCustomizationID, "CustomizationID");
    m_sCustomizationID = sCustomizationID;
    return thisAsT ();
  }

  @Nonnull
  public final String getProfileID ()
  {
    return m_sProfileID;
  }

  @Nonnull
  public final IMPLTYPE setProfileID (@Nonnull final String sProfileID)
  {
    ValueEnforcer.notNull (sProfileID, "ProfileID");
    m_sProfileID = sProfileID;
    return thisAsT ();
  }

  @Nonnull
  public final String getCardAccountNetworkID ()
  {
    return m_sCardAccountNetworkID;
  }

  @Nonnull
  public final IMPLTYPE setCardAccountNetworkID (@Nonnull final String sCardAccountNetworkID)
  {
    ValueEnforcer.notNull (sCardAccountNetworkID, "CardAccountNetworkID");
    m_sCardAccountNetworkID = sCardAccountNetworkID;
    return thisAsT ();
  }

  @Nonnull
  public final String getDefaultOrderRefID ()
  {
    return m_sDefaultOrderRefID;
  }

  @Nonnull
  public final IMPLTYPE setDefaultOrderRefID (@Nonnull final String sDefaultOrderRefID)
  {
    ValueEnforcer.notNull (sDefaultOrderRefID, "DefaultOrderRefID");
    m_sDefaultOrderRefID = sDefaultOrderRefID;
    return thisAsT ();
  }

  public final boolean isSwapQuantitySignIfNeeded ()
  {
    return m_bSwapQuantitySignIfNeeded;
  }

  @Nonnull
  public final IMPLTYPE setSwapQuantitySignIfNeeded (final boolean bSwapQuantitySignIfNeeded)
  {
    m_bSwapQuantitySignIfNeeded = bSwapQuantitySignIfNeeded;
    return thisAsT ();
  }

  public final boolean isSwapPriceSignIfNeeded ()
  {
    return m_bSwapPriceSignIfNeeded;
  }

  @Nonnull
  public final IMPLTYPE setSwapPriceSignIfNeeded (final boolean bSwapPriceSignIfNeeded)
  {
    m_bSwapPriceSignIfNeeded = bSwapPriceSignIfNeeded;
    return thisAsT ();
  }

  @Nonnull
  protected static IError buildInfo (@Nullable final String [] aPath, @Nonnull final String sErrorMsg)
  {
    return SingleError.builderInfo ()
                      .errorFieldName (aPath == null ? null : StringHelper.getImploded ('/', aPath))
                      .errorText (sErrorMsg)
                      .build ();
  }

  @Nonnull
  protected static IError buildWarn (@Nullable final String [] aPath, @Nonnull final String sErrorMsg)
  {
    return SingleError.builderWarn ()
                      .errorFieldName (aPath == null ? null : StringHelper.getImploded ('/', aPath))
                      .errorText (sErrorMsg)
                      .build ();
  }

  @Nonnull
  protected static IError buildError (@Nullable final String [] aPath, @Nonnull final String sErrorMsg)
  {
    return SingleError.builderError ()
                      .errorFieldName (aPath == null ? null : StringHelper.getImploded ('/', aPath))
                      .errorText (sErrorMsg)
                      .build ();
  }

  /**
   * Get the pattern based on
   * https://service.unece.org/trade/untdid/d16b/tred/tred2379.htm
   *
   * @param sFormat
   *        Format to use. May be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled if an unsupported format is provided.
   * @return <code>null</code> if the format is unknown.
   */
  @Nullable
  protected static String getDatePattern (@Nonnull @Nonempty final String sFormat, @Nonnull final IErrorList aErrorList)
  {
    ValueEnforcer.notEmpty (sFormat, "Format");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    switch (sFormat)
    {
      // DDMMYY
      case "2":
        return "ddMMuu";
      // MMDDYY
      case "3":
        return "MMdduu";
      // DDMMCCYY
      case "4":
        return "ddMMuuuu";
      // YYMMDD
      case "101":
        return "uuMMdd";
      // CCYYMMDD
      case DEFAULT_DATE_TIME_FORMAT:
        return "uuuuMMdd";
      // YYWWD
      case "103":
        return "YYwwee";
      // YYDDD
      case "105":
        return "uuDDD";
      default:
        aErrorList.add (buildError (null, "Unsupported date format '" + sFormat + "'"));
        return null;
    }
  }

  @Nullable
  protected static LocalDate parseDate (@Nullable final String sDate,
                                        @Nullable final String sFormat,
                                        @Nonnull final IErrorList aErrorList)
  {
    if (StringHelper.hasNoText (sDate))
      return null;

    // Default to 102
    final String sRealFormat = StringHelper.getNotEmpty (sFormat, DEFAULT_DATE_TIME_FORMAT);
    final String sPattern = getDatePattern (sRealFormat, aErrorList);
    if (sPattern == null)
      return null;

    // Try to parse it
    final LocalDate aDate = PDTFromString.getLocalDateFromString (sDate, sPattern);
    if (aDate == null)
      aErrorList.add (buildError (null, "Failed to parse the date '" + sDate + "' using format '" + sRealFormat + "'"));

    return aDate;
  }

  @Nullable
  protected static LocalDate parseDate (@Nullable final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aDateObj,
                                        @Nonnull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate parseDate (@Nullable final un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType.DateTimeString aDateObj,
                                        @Nonnull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate parseDate (@Nullable final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateType.DateString aDateObj,
                                        @Nonnull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nonnull
  protected static ETriState parseIndicator (@Nullable final IndicatorType aIndicator,
                                             @Nonnull final IErrorList aErrorList)
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

    throw new IllegalStateException ("Indicator has neither string nor boolen");
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
                                                                                              @Nonnull final T aUBLID)
  {
    if (aCIIID == null)
      return null;

    // Avoid empty element
    if (StringHelper.hasNoText (aCIIID.getValue ()))
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
                                                                                          @Nonnull final T ret)
  {
    if (aName == null)
      return null;

    // Avoid empty element
    if (StringHelper.hasNoText (aName.getValue ()))
      return null;

    ret.setValue (aName.getValue ());
    ret.setLanguageID (aName.getLanguageID ());
    ret.setLanguageLocaleID (aName.getLanguageLocaleID ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.CodeType> T copyCode (@Nullable final CodeType aCode,
                                                                                          @Nonnull final T ret)
  {
    if (aCode == null)
      return null;

    // Avoid empty element
    if (StringHelper.hasNoText (aCode.getValue ()))
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
                                                                                                  @Nonnull final T ret)
  {
    if (aQuantity == null)
      return null;

    // Avoid empty element
    if (aQuantity.getValue () == null)
      return null;

    ret.setValue (MathHelper.getWithoutTrailingZeroes (aQuantity.getValue ()));
    ret.setUnitCode (aQuantity.getUnitCode ());
    ret.setUnitCodeListID (aQuantity.getUnitCodeListID ());
    ret.setUnitCodeListAgencyID (aQuantity.getUnitCodeListAgencyID ());
    ret.setUnitCodeListAgencyName (aQuantity.getUnitCodeListAgencyName ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.AmountType> T copyAmount (@Nullable final AmountType aAmount,
                                                                                              @Nonnull final T ret,
                                                                                              @Nullable final String sDefaultCurrencyCode)
  {
    if (aAmount == null)
      return null;

    // Avoid empty element
    if (aAmount.getValue () == null)
      return null;

    ret.setValue (MathHelper.getWithoutTrailingZeroes (aAmount.getValue ()));
    ret.setCurrencyID (aAmount.getCurrencyID ());
    if (StringHelper.hasNoText (ret.getCurrencyID ()))
      ret.setCurrencyID (sDefaultCurrencyCode);
    ret.setCurrencyCodeListVersionID (aAmount.getCurrencyCodeListVersionID ());
    return ret;
  }

  protected static boolean isPaymentMeansCodeCreditTransfer (@Nullable final String s)
  {
    // the EN 16931 XSLT only checks for 30 and 58
    // in ebinterface-ubl-mapping this is 30, 31, 42 and 58
    // 30 = Credit transfer
    // 31 = Debit transfer
    // 42 = Payment to bank account
    // 58 = SEPA credit transfer
    return "30".equals (s) || "42".equals (s) || "58".equals (s);
  }

  protected static boolean isPaymentMeansCodePaymentCard (@Nullable final String s)
  {
    // 48 = Bank card
    return "48".equals (s);
  }

  protected static boolean isPaymentMeansCodeDirectDebit (@Nullable final String s)
  {
    // 49 = Direct debit (non-SEPA)
    // 59 = SEPA direct debit
    return "49".equals (s) || "59".equals (s);
  }

  protected static boolean isPaymentMeansCodeOtherKnown (@Nullable final String s)
  {
    // Allow all other codes of UNTDID 4461 (for BT-81)
    // Based on issue #34
    if (true)
      return true;

    // 1 = Instrument not defined
    // 57 = Standing agreement
    // 68 = Online payment service
    return "1".equals (s) || "57".equals (s) || "68".equals (s);
  }

  protected static boolean isOriginatorDocumentReferenceTypeCode (@Nullable final String s)
  {
    // BT-17
    return "50".equals (s);
  }

  protected static boolean isValidDocumentReferenceTypeCode (@Nullable final String s)
  {
    // BT-17 or BT-18
    // Value 916 from BT-122 should not lead to a DocumentTypeCode
    return isOriginatorDocumentReferenceTypeCode (s) || "130".equals (s);
  }

  @Nullable
  protected static String mapDueDateTypeCode (@Nullable final String s)
  {
    // BT-8 mapping; see #29
    if ("5".equals (s))
      return "3";
    if ("29".equals (s))
      return "35";
    if ("72".equals (s))
      return "432";
    return s;
  }

  protected static boolean isLT0Strict (@Nullable final BigDecimal aBD)
  {
    return aBD != null && MathHelper.isLT0 (aBD);
  }

  protected static boolean canUseGlobalID (@Nonnull final TradePartyType aParty)
  {
    // GloablID, if global identifier exists and can be stated in @schemeID, ID
    // else
    if (aParty.hasGlobalIDEntries ())
      for (final IDType aID : aParty.getGlobalID ())
        if (StringHelper.hasText (aID.getValue ()) && StringHelper.hasText (aID.getSchemeID ()))
          return true;
    return false;
  }

  @Nonnull
  protected static ICommonsList <IDType> getAllUsableGlobalIDs (@Nonnull final TradePartyType aParty)
  {
    return CommonsArrayList.createFiltered (aParty.getGlobalID (),
                                            x -> StringHelper.hasText (x.getValue ()) &&
                                                 StringHelper.hasText (x.getSchemeID ()));
  }

  /**
   * The goal is to have a positive price, because of EN validation rule BT-146.
   * This method fiddles with Quantity and Price to align this as best as
   * possible.
   *
   * @param bLineExtensionAmountIsNegative
   *        is the line sum negative?
   * @param aQuantity
   *        Existing line quantity.
   * @param aQuantitySetter
   *        Setter to change line quantity
   * @param aPriceAmount
   *        Optional line price amount
   * @param aPriceAmountSetter
   *        Optional setter to change line price amount
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   */
  protected void swapQuantityAndPriceIfNeeded (final boolean bLineExtensionAmountIsNegative,
                                               @Nonnull final BigDecimal aQuantity,
                                               @Nonnull final Consumer <BigDecimal> aQuantitySetter,
                                               @Nullable final BigDecimal aPriceAmount,
                                               @Nullable final Consumer <BigDecimal> aPriceAmountSetter,
                                               @Nonnull final IErrorList aErrorList)
  {
    final boolean bHasPrice = aPriceAmount != null && aPriceAmountSetter != null;

    if (bLineExtensionAmountIsNegative)
    {
      // We have a negative line amount
      final boolean bPosQuantity = MathHelper.isGE0 (aQuantity);
      final boolean bNegQuantity = !bPosQuantity;

      if (bHasPrice)
      {
        final boolean bNegPrice = MathHelper.isLT0 (aPriceAmount);

        if (bNegQuantity == bNegPrice)
        {
          // If both are positive, or if both are negative
          // This looks like an inconsistency
          aErrorList.add (buildWarn (null,
                                     "A negative line extension amount with quantity " +
                                           aQuantity +
                                           " and price " +
                                           aPriceAmount +
                                           " looks interesting."));
        }
        else
          if (bNegPrice)
          {
            // Non-negative quantity and negative price
            // We need to swap quantity and price
            if (isSwapQuantitySignIfNeeded ())
              aQuantitySetter.accept (aQuantity.negate ());
            else
              aErrorList.add (buildInfo (null, "Swapping of the quantity sign is disabled, so not doing it"));

            if (isSwapPriceSignIfNeeded ())
              aPriceAmountSetter.accept (aPriceAmount.negate ());
            else
              aErrorList.add (buildInfo (null, "Swapping of the price sign is disabled, so not doing it"));
          }
          else
            if (bNegQuantity)
            {
              // Negative quantity and non-negative price
              // No action needed
            }
      }
      else
      {
        // We only have the quantity
        if (bPosQuantity)
        {
          // This looks like an inconsistency
          aErrorList.add (buildWarn (null,
                                     "A negative line extension amount with quantity " +
                                           aQuantity +
                                           " looks interesting."));
        }
      }
    }
    else
    {
      // We have a positive line amount
      final boolean bNegQuantity = MathHelper.isLT0 (aQuantity);

      if (bHasPrice)
      {
        final boolean bNegPrice = MathHelper.isLT0 (aPriceAmount);

        if (bNegQuantity && bNegPrice)
        {
          // If both are negative, swap both signs to make them positive
          if (isSwapQuantitySignIfNeeded ())
            aQuantitySetter.accept (aQuantity.negate ());
          else
            aErrorList.add (buildInfo (null, "Swapping of the quantity sign is disabled, so not doing it"));

          if (isSwapPriceSignIfNeeded ())
            aPriceAmountSetter.accept (aPriceAmount.negate ());
          else
            aErrorList.add (buildInfo (null, "Swapping of the price sign is disabled, so not doing it"));
        }
        else
          if (bNegQuantity || bNegPrice)
          {
            // Only one value is negative
            // This looks like an inconsistency
            aErrorList.add (buildWarn (null,
                                       "A positive line extension amount with quantity " +
                                             aQuantity +
                                             " and price " +
                                             aPriceAmount +
                                             " looks interesting."));
          }
        // If both values are positive, no action needed
      }
      else
      {
        // We only have the quantity
        if (bNegQuantity)
        {
          // This looks like an inconsistency
          aErrorList.add (buildWarn (null,
                                     "A positive line extension amount with quantity " +
                                           aQuantity +
                                           " looks interesting."));
        }
      }
    }
  }

  @Nonnull
  protected static ETriState isInvoiceType (@Nonnull final CrossIndustryInvoiceType aCIIInvoice,
                                            @Nonnull final IErrorList aErrorList)
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
      eIsInvoice = ETriState.valueOf (MathHelper.isGE0 (aDuePayable.getValue ()));
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
   * @return The parsed Invoice or CreditNote as UBL 2.x. May be
   *         <code>null</code> in case of error.
   */
  @Nullable
  public Serializable convertCIItoUBL (@Nonnull final File aFile, @Nonnull final ErrorList aErrorList)
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
   *        The CII invoice to be converted. May not be <code>null</code>.
   *        Ideally this is a valid CII invoice only and not some handcrafted
   *        domain object.
   * @param aErrorList
   *        Error list to be filled. May not be <code>null</code>.
   * @return The parsed {@link InvoiceType} or {@link CreditNoteType}. May be
   *         <code>null</code> in case of error.
   */
  @Nullable
  public abstract Serializable convertCIItoUBL (@Nonnull CrossIndustryInvoiceType aCIIInvoice,
                                                @Nonnull ErrorList aErrorList);
}
