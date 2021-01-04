/**
 * Copyright (C) 2019-2021 Philip Helger
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
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.cii.d16b.CIID16BReader;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.datetime.PDTFromString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.commons.traits.IGenericImplTrait;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
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
  public static final String DEFAULT_CUSTOMIZATION_ID = "urn:cen.eu:en16931:2017:extended:urn:fdc:peppol.eu:2017:poacc:billing:3.0";
  public static final String DEFAULT_PROFILE_ID = "urn:fdc:peppol.eu:2017:poacc:billing:01:1.0";
  public static final String DEFAULT_CARD_ACCOUNT_NETWORK_ID = "mapped-from-cii";

  private EUBLCreationMode m_eCreationMode = DEFAULT_UBL_CREATION_MODE;
  private String m_sVATScheme = DEFAULT_VAT_SCHEME;
  private String m_sCustomizationID = DEFAULT_CUSTOMIZATION_ID;
  private String m_sProfileID = DEFAULT_PROFILE_ID;
  private String m_sCardAccountNetworkID = DEFAULT_CARD_ACCOUNT_NETWORK_ID;

  protected AbstractCIIToUBLConverter ()
  {}

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
  protected static IError _buildError (@Nullable final String [] aPath, final String sErrorMsg)
  {
    return SingleError.builderError ()
                      .setErrorText (sErrorMsg)
                      .setErrorFieldName (aPath == null ? null : StringHelper.getImploded ('/', aPath))
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
  public static String _getDatePattern (@Nonnull @Nonempty final String sFormat, @Nonnull final IErrorList aErrorList)
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
      case "102":
        return "uuuuMMdd";
      // YYWWD
      case "103":
        return "YYwwee";
      // YYDDD
      case "105":
        return "uuDDD";
      default:
        aErrorList.add (_buildError (null, "Unsupported date format '" + sFormat + "'"));
        return "'error'";
    }
  }

  @Nullable
  protected static LocalDate _parseDate (@Nullable final String sDate, @Nullable final String sFormat, @Nonnull final IErrorList aErrorList)
  {
    if (StringHelper.hasNoText (sDate))
      return null;

    // Default to 102
    final String sRealFormat = StringHelper.getNotEmpty (sFormat, "102");
    final String sPattern = _getDatePattern (sRealFormat, aErrorList);
    if (sPattern == null)
      return null;

    // Try to parse it
    final LocalDate aDate = PDTFromString.getLocalDateFromString (sDate, sPattern);
    if (aDate == null)
      aErrorList.add (_buildError (null, "Failed to parse the date '" + sDate + "' using format '" + sRealFormat + "'"));

    return aDate;
  }

  @Nullable
  protected static LocalDate _parseDate (@Nullable final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType.DateTimeString aDateObj,
                                         @Nonnull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return _parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate _parseDate (@Nullable final un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType.DateTimeString aDateObj,
                                         @Nonnull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return _parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nullable
  protected static LocalDate _parseDate (@Nullable final un.unece.uncefact.data.standard.unqualifieddatatype._100.DateType.DateString aDateObj,
                                         @Nonnull final IErrorList aErrorList)
  {
    if (aDateObj == null)
      return null;

    return _parseDate (aDateObj.getValue (), aDateObj.getFormat (), aErrorList);
  }

  @Nonnull
  protected static ETriState _parseIndicator (@Nullable final IndicatorType aIndicator, @Nonnull final IErrorList aErrorList)
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

      aErrorList.add (_buildError (null, "Failed to parse the indicator value '" + aIndicator + "' to a boolean value."));
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
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.IdentifierType> T _copyID (@Nullable final IDType aCIIID,
                                                                                               @Nonnull final T aUBLID)
  {
    if (aCIIID == null)
      return null;
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
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.TextType> T _copyName (@Nullable final TextType aName,
                                                                                           @Nonnull final T ret)
  {
    if (aName == null)
      return null;

    ret.setValue (aName.getValue ());
    ret.setLanguageID (aName.getLanguageID ());
    ret.setLanguageLocaleID (aName.getLanguageLocaleID ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.CodeType> T _copyCode (@Nullable final CodeType aCode,
                                                                                           @Nonnull final T ret)
  {
    if (aCode == null)
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
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.QuantityType> T _copyQuantity (@Nullable final QuantityType aQuantity,
                                                                                                   @Nonnull final T ret)
  {
    if (aQuantity == null)
      return null;

    ret.setValue (aQuantity.getValue ());
    ret.setUnitCode (aQuantity.getUnitCode ());
    ret.setUnitCodeListID (aQuantity.getUnitCodeListID ());
    ret.setUnitCodeListAgencyID (aQuantity.getUnitCodeListAgencyID ());
    ret.setUnitCodeListAgencyName (aQuantity.getUnitCodeListAgencyName ());
    return ret;
  }

  @Nullable
  protected static <T extends com.helger.xsds.ccts.cct.schemamodule.AmountType> T _copyAmount (@Nullable final AmountType aAmount,
                                                                                               @Nonnull final T ret,
                                                                                               @Nullable final String sDefaultCurrencyCode)
  {
    if (aAmount == null)
      return null;

    ret.setValue (aAmount.getValue ());
    ret.setCurrencyID (aAmount.getCurrencyID ());
    if (StringHelper.hasNoText (ret.getCurrencyID ()))
      ret.setCurrencyID (sDefaultCurrencyCode);
    ret.setCurrencyCodeListVersionID (aAmount.getCurrencyCodeListVersionID ());
    return ret;
  }

  protected static boolean paymentMeansCodeRequiresPayeeFinancialAccountID (@Nullable final String s)
  {
    // in ebinterface-ubl-mapping this is 30, 31, 42 and 58
    return "30".equals (s) || "58".equals (s);
  }

  protected static boolean isOriginatorDocumentReferenceTypeCode (@Nullable final String s)
  {
    return "50".equals (s);
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
    final CrossIndustryInvoiceType aCIIInvoice = CIID16BReader.crossIndustryInvoice ()
                                                              .setValidationEventHandler (new WrappedCollectingValidationEventHandler (aErrorList))
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
  public abstract Serializable convertCIItoUBL (@Nonnull CrossIndustryInvoiceType aCIIInvoice, @Nonnull ErrorList aErrorList);
}
