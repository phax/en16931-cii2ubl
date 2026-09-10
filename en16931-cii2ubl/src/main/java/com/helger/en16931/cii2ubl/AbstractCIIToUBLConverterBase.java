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
package com.helger.en16931.cii2ubl;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.numeric.BigHelper;
import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.base.trait.IGenericImplTrait;
import com.helger.datetime.xml.XMLOffsetDateTime;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.diagnostics.error.list.IErrorList;
import com.helger.en16931.basics.ConversionHelper;
import com.helger.en16931.basics.EEN16931DateFormatCode;
import com.helger.en16931.basics.codelist.EN16931CodeLists;

/**
 * Base class for the conversion from CII to UBL, independent of the EN 16931 edition. It contains
 * the configuration API, the error helpers and all mapping helpers that only work on Java standard
 * types. Everything typed to a specific CII release resides in the edition specific subclasses.
 *
 * @author Philip Helger
 * @param <IMPLTYPE>
 *        The implementation type
 * @since 4.0.0
 */
public abstract class AbstractCIIToUBLConverterBase <IMPLTYPE extends AbstractCIIToUBLConverterBase <IMPLTYPE>>
                                                    implements
                                                    IGenericImplTrait <IMPLTYPE>
{
  public static final EUBLCreationMode DEFAULT_UBL_CREATION_MODE = EUBLCreationMode.AUTOMATIC;
  public static final String DEFAULT_VAT_SCHEME = "VAT";
  public static final String DEFAULT_CARD_ACCOUNT_NETWORK_ID = "mapped-from-cii";
  /**
   * BT-13 Purchase order reference. <code>cac:OrderReference/cbc:ID</code> is mandatory in UBL as
   * soon as the element exists, which BT-14 and BT-188 can force without BT-13 being present, so a
   * placeholder is needed. Up to 4.0.0 that placeholder was the empty string, which produced a
   * mandatory element with no content.
   */
  public static final String DEFAULT_ORDER_REF_ID = EN16931CodeLists.MISSING_VALUE_PLACEHOLDER;
  public static final boolean DEFAULT_SWAP_QUANTITY_SIGN_IF_NEEDED = true;
  public static final boolean DEFAULT_SWAP_PRICE_SIGN_IF_NEEDED = true;

  private static final Logger LOGGER = LoggerFactory.getLogger (AbstractCIIToUBLConverterBase.class);

  private EUBLCreationMode m_eCreationMode = DEFAULT_UBL_CREATION_MODE;
  private String m_sVATScheme = DEFAULT_VAT_SCHEME;
  private String m_sCustomizationID;
  private String m_sProfileID;
  private String m_sCardAccountNetworkID = DEFAULT_CARD_ACCOUNT_NETWORK_ID;
  private String m_sDefaultOrderRefID = DEFAULT_ORDER_REF_ID;
  private boolean m_bSwapQuantitySignIfNeeded = DEFAULT_SWAP_QUANTITY_SIGN_IF_NEEDED;
  private boolean m_bSwapPriceSignIfNeeded = DEFAULT_SWAP_PRICE_SIGN_IF_NEEDED;

  protected AbstractCIIToUBLConverterBase ()
  {}

  // The two shorthands below only forward to ConversionHelper. They stay as protected members,
  // because the mapping code uses them several hundred times and the unqualified call is what keeps
  // it readable.

  protected static <T> boolean ifNotNull (@Nullable final T aObj, @NonNull final Consumer <? super T> aConsumer)
  {
    return ConversionHelper.ifNotNull (aObj, aConsumer);
  }

  protected static boolean ifNotEmpty (@Nullable final String s, @NonNull final Consumer <? super String> aConsumer)
  {
    return ConversionHelper.ifNotEmpty (s, aConsumer);
  }

  @NonNull
  public final EUBLCreationMode getUBLCreationMode ()
  {
    return m_eCreationMode;
  }

  @NonNull
  public final IMPLTYPE setUBLCreationMode (@NonNull final EUBLCreationMode eCreationMode)
  {
    ValueEnforcer.notNull (eCreationMode, "CreationMode");
    m_eCreationMode = eCreationMode;
    return thisAsT ();
  }

  @NonNull
  public final String getVATScheme ()
  {
    return m_sVATScheme;
  }

  @NonNull
  public final IMPLTYPE setVATScheme (@NonNull final String sVATScheme)
  {
    ValueEnforcer.notNull (sVATScheme, "VATScheme");
    m_sVATScheme = sVATScheme;
    return thisAsT ();
  }

  @NonNull
  public final String getCustomizationID ()
  {
    return m_sCustomizationID;
  }

  @NonNull
  public final IMPLTYPE setCustomizationID (@NonNull final String sCustomizationID)
  {
    ValueEnforcer.notNull (sCustomizationID, "CustomizationID");
    m_sCustomizationID = sCustomizationID;
    return thisAsT ();
  }

  @NonNull
  public final String getProfileID ()
  {
    return m_sProfileID;
  }

  @NonNull
  public final IMPLTYPE setProfileID (@NonNull final String sProfileID)
  {
    ValueEnforcer.notNull (sProfileID, "ProfileID");
    m_sProfileID = sProfileID;
    return thisAsT ();
  }

  @NonNull
  public final String getCardAccountNetworkID ()
  {
    return m_sCardAccountNetworkID;
  }

  @NonNull
  public final IMPLTYPE setCardAccountNetworkID (@NonNull final String sCardAccountNetworkID)
  {
    ValueEnforcer.notNull (sCardAccountNetworkID, "CardAccountNetworkID");
    m_sCardAccountNetworkID = sCardAccountNetworkID;
    return thisAsT ();
  }

  @NonNull
  public final String getDefaultOrderRefID ()
  {
    return m_sDefaultOrderRefID;
  }

  @NonNull
  public final IMPLTYPE setDefaultOrderRefID (@NonNull final String sDefaultOrderRefID)
  {
    ValueEnforcer.notNull (sDefaultOrderRefID, "DefaultOrderRefID");
    m_sDefaultOrderRefID = sDefaultOrderRefID;
    return thisAsT ();
  }

  public final boolean isSwapQuantitySignIfNeeded ()
  {
    return m_bSwapQuantitySignIfNeeded;
  }

  @NonNull
  public final IMPLTYPE setSwapQuantitySignIfNeeded (final boolean bSwapQuantitySignIfNeeded)
  {
    m_bSwapQuantitySignIfNeeded = bSwapQuantitySignIfNeeded;
    return thisAsT ();
  }

  public final boolean isSwapPriceSignIfNeeded ()
  {
    return m_bSwapPriceSignIfNeeded;
  }

  @NonNull
  public final IMPLTYPE setSwapPriceSignIfNeeded (final boolean bSwapPriceSignIfNeeded)
  {
    m_bSwapPriceSignIfNeeded = bSwapPriceSignIfNeeded;
    return thisAsT ();
  }

  @NonNull
  protected static IError buildInfo (@Nullable final String [] aPath, @NonNull final String sErrorMsg)
  {
    return SingleError.builderInfo ()
                      .errorFieldName (aPath == null ? null : StringImplode.getImploded ('/', aPath))
                      .errorText (sErrorMsg)
                      .build ();
  }

  @NonNull
  protected static IError buildWarn (@Nullable final String [] aPath, @NonNull final String sErrorMsg)
  {
    return SingleError.builderWarn ()
                      .errorFieldName (aPath == null ? null : StringImplode.getImploded ('/', aPath))
                      .errorText (sErrorMsg)
                      .build ();
  }

  @NonNull
  protected static IError buildError (@Nullable final String [] aPath, @NonNull final String sErrorMsg)
  {
    return SingleError.builderError ()
                      .errorFieldName (aPath == null ? null : StringImplode.getImploded ('/', aPath))
                      .errorText (sErrorMsg)
                      .build ();
  }

  /**
   * Parse a UNTDID 2379 formatted date and time. Only {@link EEN16931DateFormatCode#hasTime()}
   * formats carry a time, and EN 16931 only uses "208" (CCYYMMDDHHMMSSZHHMM), which the 2026
   * edition writes when BT-166 is present.
   *
   * @param sDateTime
   *        The value to parse. May be <code>null</code>.
   * @param sFormat
   *        Format to use. May be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return <code>null</code> if the value could not be parsed.
   * @since 4.0.0
   */
  @Nullable
  protected static XMLOffsetDateTime parseDateTime (@Nullable final String sDateTime,
                                                    @Nullable final String sFormat,
                                                    @NonNull final IErrorList aErrorList)
  {
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    if (StringHelper.isEmpty (sDateTime))
      return null;

    final EEN16931DateFormatCode eFormat = EEN16931DateFormatCode.getFromIDOrNull (sFormat);
    if (eFormat == null || !eFormat.hasTime ())
    {
      aErrorList.add (buildError (null, "Unsupported date time format '" + sFormat + "'"));
      return null;
    }

    final XMLOffsetDateTime aDateTime = eFormat.parseDateTime (sDateTime);
    if (aDateTime == null)
      aErrorList.add (buildError (null,
                                  "Failed to parse the date time '" + sDateTime + "' using format '" + sFormat + "'"));

    return aDateTime;
  }

  /**
   * Parse a UNTDID 2379 formatted date. An absent format means
   * {@link EEN16931DateFormatCode#DEFAULT}, which is the only format EN 16931 writes for a plain
   * date.
   *
   * @param sDate
   *        The value to parse. May be <code>null</code>.
   * @param sFormat
   *        Format to use. May be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return <code>null</code> if the value could not be parsed.
   */
  @Nullable
  protected static LocalDate parseDate (@Nullable final String sDate,
                                        @Nullable final String sFormat,
                                        @NonNull final IErrorList aErrorList)
  {
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    if (StringHelper.isEmpty (sDate))
      return null;

    final EEN16931DateFormatCode eFormat = EEN16931DateFormatCode.getFromIDOrDefault (sFormat);
    if (eFormat == null)
    {
      aErrorList.add (buildError (null, "Unsupported date format '" + sFormat + "'"));
      return null;
    }

    // Try to parse it
    final LocalDate aDate = eFormat.parseLocalDate (sDate);
    if (aDate == null)
      aErrorList.add (buildError (null,
                                  "Failed to parse the date '" + sDate + "' using format '" + eFormat.getID () + "'"));

    return aDate;
  }

  /**
   * Check if the provided BT-81 value is one this converter maps to no specific UBL payment means
   * group. Unlike the classifying helpers of {@link EN16931CodeLists} this is a decision of the
   * converter and not a fact of the code list, which is why it lives here.
   *
   * @param s
   *        The BT-81 Payment means type code to check. May be <code>null</code>.
   * @return <code>true</code> for every code of UNTDID 4461.
   */
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

  /**
   * Parse a CII indicator, which is a choice between a real boolean and a string.<br>
   * The CII indicator types of the different CII releases are unrelated Java classes, so the
   * callers pass the already extracted values instead of the indicator object itself.
   *
   * @param aBooleanValue
   *        The boolean branch of the choice. May be <code>null</code>.
   * @param bHasStringValue
   *        <code>true</code> if the string branch of the choice is present at all.
   * @param sStringValue
   *        The value of the string branch. May be <code>null</code>.
   * @param aSource
   *        The source object, used for the error message only. May be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return Never <code>null</code>.
   * @since 4.0.0
   */
  @NonNull
  protected static ETriState parseIndicator (@Nullable final Boolean aBooleanValue,
                                             final boolean bHasStringValue,
                                             @Nullable final String sStringValue,
                                             @Nullable final Object aSource,
                                             @NonNull final IErrorList aErrorList)
  {
    // Choice
    if (aBooleanValue != null)
      return ETriState.valueOf (aBooleanValue.booleanValue ());

    if (bHasStringValue)
    {
      // Parse string
      if (sStringValue == null)
        return ETriState.UNDEFINED;
      if ("true".equals (sStringValue))
        return ETriState.TRUE;
      if ("false".equals (sStringValue))
        return ETriState.FALSE;

      aErrorList.add (buildError (null, "Failed to parse the indicator value '" + aSource + "' to a boolean value."));
      return ETriState.UNDEFINED;
    }

    throw new IllegalStateException ("Indicator has neither string nor boolean");
  }

  /**
   * Copy a CII text to a CCTS text based UBL object. The CII text types of the different CII
   * releases are unrelated Java classes, so the callers pass the already extracted values.
   *
   * @param <T>
   *        The UBL target type
   * @param sValue
   *        The text value. May be <code>null</code>.
   * @param sLanguageID
   *        The language ID. May be <code>null</code>.
   * @param sLanguageLocaleID
   *        The language locale ID. May be <code>null</code>.
   * @param ret
   *        The UBL object to fill. May not be <code>null</code>.
   * @return <code>null</code> if the value is empty, to avoid creating an empty element.
   * @since 4.0.0
   */
  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.TextType> T copyName (@Nullable final String sValue,
                                                                                          @Nullable final String sLanguageID,
                                                                                          @Nullable final String sLanguageLocaleID,
                                                                                          @NonNull final T ret)
  {
    // Avoid empty element
    if (StringHelper.isEmpty (sValue))
      return null;

    ret.setValue (sValue);
    ret.setLanguageID (sLanguageID);
    ret.setLanguageLocaleID (sLanguageLocaleID);
    return ret;
  }

  /**
   * Copy a CII quantity to a CCTS quantity based UBL object.
   *
   * @param <T>
   *        The UBL target type
   * @param aValue
   *        The quantity value. May be <code>null</code>.
   * @param sUnitCode
   *        The unit code. May be <code>null</code>.
   * @param sUnitCodeListID
   *        The unit code list ID. May be <code>null</code>.
   * @param sUnitCodeListAgencyID
   *        The unit code list agency ID. May be <code>null</code>.
   * @param sUnitCodeListAgencyName
   *        The unit code list agency name. May be <code>null</code>.
   * @param ret
   *        The UBL object to fill. May not be <code>null</code>.
   * @return <code>null</code> if the value is <code>null</code>, to avoid creating an empty
   *         element.
   * @since 4.0.0
   */
  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.QuantityType> T copyQuantity (@Nullable final BigDecimal aValue,
                                                                                                  @Nullable final String sUnitCode,
                                                                                                  @Nullable final String sUnitCodeListID,
                                                                                                  @Nullable final String sUnitCodeListAgencyID,
                                                                                                  @Nullable final String sUnitCodeListAgencyName,
                                                                                                  @NonNull final T ret)
  {
    // Avoid empty element
    if (aValue == null)
      return null;

    ret.setValue (BigHelper.getWithoutTrailingZeroes (aValue));
    ret.setUnitCode (sUnitCode);
    ret.setUnitCodeListID (sUnitCodeListID);
    ret.setUnitCodeListAgencyID (sUnitCodeListAgencyID);
    ret.setUnitCodeListAgencyName (sUnitCodeListAgencyName);
    return ret;
  }

  /**
   * Copy a CII amount to a CCTS amount based UBL object.
   *
   * @param <T>
   *        The UBL target type
   * @param aValue
   *        The amount value. May be <code>null</code>.
   * @param sCurrencyID
   *        The currency ID. May be <code>null</code>, in which case the default currency code is
   *        used.
   * @param sCurrencyCodeListVersionID
   *        The currency code list version ID. May be <code>null</code>.
   * @param ret
   *        The UBL object to fill. May not be <code>null</code>.
   * @param sDefaultCurrencyCode
   *        The fallback currency code. May be <code>null</code>.
   * @return <code>null</code> if the value is <code>null</code>, to avoid creating an empty
   *         element.
   * @since 4.0.0
   */
  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.AmountType> T copyAmount (@Nullable final BigDecimal aValue,
                                                                                              @Nullable final String sCurrencyID,
                                                                                              @Nullable final String sCurrencyCodeListVersionID,
                                                                                              @NonNull final T ret,
                                                                                              @Nullable final String sDefaultCurrencyCode)
  {
    // Avoid empty element
    if (aValue == null)
      return null;

    ret.setValue (BigHelper.getWithoutTrailingZeroes (aValue));
    ret.setCurrencyID (sCurrencyID);
    if (StringHelper.isEmpty (ret.getCurrencyID ()))
      ret.setCurrencyID (sDefaultCurrencyCode);
    ret.setCurrencyCodeListVersionID (sCurrencyCodeListVersionID);
    return ret;
  }

  /**
   * BT-29/BT-46/BT-60/BT-71: a CII GlobalID is only usable as a UBL party identification if it has
   * both a value and a scheme identifier, and if that scheme identifier is not the one reserved for
   * BT-90.
   *
   * @param sValue
   *        The identifier value. May be <code>null</code>.
   * @param sSchemeID
   *        The scheme identifier. May be <code>null</code>.
   * @return <code>true</code> if the identifier can be used.
   * @since 4.0.0
   */
  protected static boolean isUsableGlobalID (@Nullable final String sValue, @Nullable final String sSchemeID)
  {
    if (StringHelper.isEmpty (sValue) || StringHelper.isEmpty (sSchemeID))
      return false;

    // BT-90 shares cac:PartyIdentification/cbc:ID with the party identifiers and is told apart by
    // this scheme identifier alone. It is written from ram:CreditorReferenceID instead, so a
    // GlobalID carrying it would become a second, competing BT-90 - and it is not an ISO 6523 ICD
    // code either, which BT-29-1 requires.
    return !EN16931CodeLists.CREDITOR_REFERENCE_SCHEME_ID.equals (sSchemeID);
  }

  /**
   * Determine whether a CII document is an Invoice or a Credit Note. The decision is based on BT-3
   * (Invoice type code) and falls back to the sign of BT-115 (Amount due for payment).
   *
   * @param sTypeCode
   *        BT-3 Invoice type code. May be <code>null</code>.
   * @param aDuePayableAmount
   *        BT-115 Amount due for payment. May be <code>null</code>.
   * @param aDuePayableSource
   *        The source object of BT-115, used for the warning message only. May be
   *        <code>null</code>.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return {@link ETriState#TRUE} for an Invoice, {@link ETriState#FALSE} for a Credit Note and
   *         {@link ETriState#UNDEFINED} if it cannot be determined.
   * @since 4.0.0
   */
  @NonNull
  protected static ETriState isInvoiceType (@Nullable final String sTypeCode,
                                            @Nullable final BigDecimal aDuePayableAmount,
                                            @Nullable final Object aDuePayableSource,
                                            @NonNull final IErrorList aErrorList)
  {
    ETriState eIsInvoice = ETriState.UNDEFINED;

    // First check TypeCode
    final String sRealTypeCode = StringHelper.trim (sTypeCode);
    if (EN16931CodeLists.isInvoiceTypeCode (sRealTypeCode))
      eIsInvoice = ETriState.TRUE;
    else
      if (EN16931CodeLists.isCreditNoteTypeCode (sRealTypeCode))
        eIsInvoice = ETriState.FALSE;

    // Check total
    if (eIsInvoice.isUndefined () && aDuePayableSource != null)
      eIsInvoice = ETriState.valueOf (BigHelper.isGE0 (aDuePayableAmount));

    if (eIsInvoice.isUndefined ())
    {
      aErrorList.add (buildWarn (null,
                                 "Could not determine, if the provided CII document is an Invoice or a CreditNote. TypeCode is '" +
                                       sRealTypeCode +
                                       "'; DuePayable is " +
                                       aDuePayableSource));
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Determined the provided CII document to be " +
                      (eIsInvoice.isTrue () ? "an Invoice" : "a CreditNote"));
    }
    return eIsInvoice;
  }

  protected static boolean isLT0Strict (@Nullable final BigDecimal aBD)
  {
    return aBD != null && BigHelper.isLT0 (aBD);
  }

  /**
   * The goal is to have a positive price, because of EN validation rule BT-146. This method fiddles
   * with Quantity and Price to align this as best as possible.
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
                                               @NonNull final BigDecimal aQuantity,
                                               @NonNull final Consumer <BigDecimal> aQuantitySetter,
                                               @Nullable final BigDecimal aPriceAmount,
                                               @Nullable final Consumer <BigDecimal> aPriceAmountSetter,
                                               @NonNull final IErrorList aErrorList)
  {
    final boolean bHasPrice = aPriceAmount != null && aPriceAmountSetter != null;

    if (bLineExtensionAmountIsNegative)
    {
      // We have a negative line amount
      final boolean bPosQuantity = BigHelper.isGE0 (aQuantity);
      final boolean bNegQuantity = !bPosQuantity;

      if (bHasPrice)
      {
        final boolean bNegPrice = BigHelper.isLT0 (aPriceAmount);

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
      final boolean bNegQuantity = BigHelper.isLT0 (aQuantity);

      if (bHasPrice)
      {
        final boolean bNegPrice = BigHelper.isLT0 (aPriceAmount);

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

  /**
   * Convert CII to UBL
   *
   * @param aFile
   *        Source file with CII to be parsed. May not be <code>null</code>.
   * @param aErrorList
   *        Error list to be filled. May not be <code>null</code>.
   * @return The parsed Invoice or CreditNote as UBL 2.x. May be <code>null</code> in case of error.
   */
  @Nullable
  public abstract Serializable convertCIItoUBL (@NonNull File aFile, @NonNull ErrorList aErrorList);
}
