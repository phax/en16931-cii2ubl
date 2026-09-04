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
import java.util.Set;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.annotation.Nonempty;
import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.numeric.BigHelper;
import com.helger.base.string.StringHelper;
import com.helger.base.string.StringImplode;
import com.helger.base.trait.IGenericImplTrait;
import com.helger.datetime.format.PDTFromString;
import com.helger.datetime.xml.XMLOffsetDateTime;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.SingleError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.diagnostics.error.list.IErrorList;

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
  public static final String DEFAULT_DATE_TIME_FORMAT = "102";
  /**
   * UNTDID 2379 format "208" - CCYYMMDDHHMMSSZHHMM. Used by EN 16931:2026 when BT-166 (Invoice
   * issue time) is present.
   *
   * @since 4.0.0
   */
  public static final String DATE_TIME_FORMAT_WITH_TIME = "208";
  public static final String DEFAULT_ORDER_REF_ID = "";
  public static final boolean DEFAULT_SWAP_QUANTITY_SIGN_IF_NEEDED = true;
  public static final boolean DEFAULT_SWAP_PRICE_SIGN_IF_NEEDED = true;

  // Source: EN 16931 validation artefacts
  // Last update: 2026-02-04 from 1.3.15
  protected static final Set <String> CREDIT_NOTE_TYPE_CODES = StringHelper.getExplodedToSet (" ",
                                                                                            "81 83 261 262 296 308 381 396 420 458 532");
  protected static final Set <String> INVOICE_TYPE_CODES = StringHelper.getExplodedToSet (" ",
                                                                                        "71 80 81 82 84 102 130 202 203 204 211 218 219 295 325 326 331 380 382 383 384 385 386 387 388 389 390 393 394 395 456 457 471 472 473 500 501 502 503 527 553 575 623 633 751 780 817 870 875 876 877 935");

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

  protected static <T> boolean ifNotNull (@Nullable final T aObj, @NonNull final Consumer <? super T> aConsumer)
  {
    if (aObj == null)
      return false;
    aConsumer.accept (aObj);
    return true;
  }

  protected static boolean ifNotEmpty (@Nullable final String s, @NonNull final Consumer <? super String> aConsumer)
  {
    if (StringHelper.isEmpty (s))
      return false;
    aConsumer.accept (s);
    return true;
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
   * Get the pattern based on https://service.unece.org/trade/untdid/d16b/tred/tred2379.htm
   *
   * @param sFormat
   *        Format to use. May be <code>null</code>.
   * @param aErrorList
   *        The error list to be filled if an unsupported format is provided.
   * @return <code>null</code> if the format is unknown.
   */
  @Nullable
  protected static String getDatePattern (@NonNull @Nonempty final String sFormat, @NonNull final IErrorList aErrorList)
  {
    ValueEnforcer.notEmpty (sFormat, "Format");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    return switch (sFormat)
    {
      case "2" -> "ddMMuu";
      case "3" -> "MMdduu";
      case "4" -> "ddMMuuuu";
      case "101" -> "uuMMdd";
      case DEFAULT_DATE_TIME_FORMAT -> "uuuuMMdd";
      case "103" -> "YYwwee";
      case "105" -> "uuDDD";
      default ->
      {
        aErrorList.add (buildError (null, "Unsupported date format '" + sFormat + "'"));
        yield null;
      }
    };
  }

  /**
   * Parse a UNTDID 2379 formatted date and time. Only format "208" (CCYYMMDDHHMMSSZHHMM) carries a
   * time, which is what EN 16931:2026 uses when BT-166 is present.
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
    if (StringHelper.isEmpty (sDateTime))
      return null;

    if (!DATE_TIME_FORMAT_WITH_TIME.equals (sFormat))
    {
      aErrorList.add (buildError (null, "Unsupported date time format '" + sFormat + "'"));
      return null;
    }

    final XMLOffsetDateTime aDateTime = PDTFromString.getXMLOffsetDateTimeFromString (sDateTime,
                                                                                     "uuuuMMddHHmmssZ");
    if (aDateTime == null)
      aErrorList.add (buildError (null,
                                  "Failed to parse the date time '" +
                                        sDateTime +
                                        "' using format '" +
                                        sFormat +
                                        "'"));

    return aDateTime;
  }

  @Nullable
  protected static LocalDate parseDate (@Nullable final String sDate,
                                        @Nullable final String sFormat,
                                        @NonNull final IErrorList aErrorList)
  {
    if (StringHelper.isEmpty (sDate))
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
