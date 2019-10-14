/**
 * Copyright (C) 2019 Philip Helger
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

import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.datatype.XMLGregorianCalendar;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.datetime.PDTFromString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.util.PDTXMLConverter;

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
 */
public abstract class AbstractCIIToUBLConverter
{
  public static final EUBLCreationMode DEFAULT_UBL_CREATION_MODE = EUBLCreationMode.AUTOMATIC;
  public static final String DEFAULT_VAT_SCHEME = "VAT";
  public static final String DEFAULT_CUSTOMIZATION_ID = "urn:cen.eu:en16931:2017:extended:urn:fdc:peppol.eu:2017:poacc:billing:3.0";
  public static final String DEFAULT_PROFILE_ID = "urn:fdc:peppol.eu:2017:poacc:billing:01:1.0";
  public static final String DEFAULT_CARD_ACCOUNT_NETWORK_ID = "mapped-from-cii";

  private EUBLCreationMode m_eCreationMode = DEFAULT_UBL_CREATION_MODE;

  protected AbstractCIIToUBLConverter ()
  {}

  @Nonnull
  public final EUBLCreationMode getUBLCreationMode ()
  {
    return m_eCreationMode;
  }

  public final void setUBLCreationMode (@Nonnull final EUBLCreationMode eCreationMode)
  {
    ValueEnforcer.notNull (eCreationMode, "CreationMode");
    m_eCreationMode = eCreationMode;
  }

  @Nonnull
  protected static IError _buildError (@Nullable final String [] aPath, final String sErrorMsg)
  {
    return SingleError.builderError ()
                      .setErrorText (sErrorMsg)
                      .setErrorFieldName (aPath == null ? null : StringHelper.getImploded ('/', aPath))
                      .build ();
  }

  @Nullable
  protected static XMLGregorianCalendar _parseDateDDMMYYYY (@Nullable final String sDate,
                                                            @Nonnull final IErrorList aErrorList)
  {
    if (StringHelper.hasNoText (sDate))
      return null;

    final LocalDate aDate = PDTFromString.getLocalDateFromString (sDate, "uuuuMMdd");
    if (aDate == null)
      aErrorList.add (_buildError (null, "Failed to parse the date '" + sDate + "'"));

    return PDTXMLConverter.getXMLCalendarDate (aDate);
  }

  @Nonnull
  protected static ETriState _parseIndicator (@Nullable final IndicatorType aIndicator,
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

      aErrorList.add (_buildError (null,
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
}
