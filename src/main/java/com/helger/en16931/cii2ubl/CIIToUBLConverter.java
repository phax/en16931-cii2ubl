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

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.cii.d16b.CIID16BReader;
import com.helger.commons.CGlobal;
import com.helger.commons.datetime.PDTFromString;
import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.error.list.IErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.state.ETriState;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.util.PDTXMLConverter;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.*;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.BinaryObjectType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IndicatorType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

public class CIIToUBLConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverter.class);

  public CIIToUBLConverter ()
  {}

  @Nonnull
  private static IError _buildError (@Nullable final String [] aPath, final String sErrorMsg)
  {
    return SingleError.builderError ()
                      .setErrorText (sErrorMsg)
                      .setErrorFieldName (aPath == null ? null : StringHelper.getImploded ('/', aPath))
                      .build ();
  }

  @Nullable
  private static XMLGregorianCalendar _parseDateDDMMYYYY (@Nullable final String sDate,
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
  private static ETriState _parseIndicator (@Nullable final IndicatorType aIndicator,
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
  private static <T extends com.helger.xsds.ccts.cct.schemamodule.IdentifierType> T _copyID (@Nullable final IDType aCIIID,
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
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType _copyID (@Nullable final IDType aCIIID)
  {
    return _copyID (aCIIID, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType ());
  }

  @Nullable
  private static <T extends com.helger.xsds.ccts.cct.schemamodule.TextType> T _copyName (@Nullable final TextType aName,
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
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType _copyNote (@Nullable final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aNote)
  {
    if (aNote == null)
      return null;

    final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aUBLNote = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType ();
    final StringBuilder aSB = new StringBuilder ();
    for (final TextType aText : aNote.getContent ())
    {
      if (aSB.length () > 0)
        aSB.append ('\n');
      aSB.append (aText.getValue ());
    }
    aUBLNote.setValue (aSB.toString ());
    return aUBLNote;
  }

  @Nullable
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType _copyNote (@Nullable final TextType aText)
  {
    if (aText == null)
      return null;

    final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aUBLNote = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType ();
    aUBLNote.setValue (aText.getValue ());
    aUBLNote.setLanguageID (aText.getLanguageID ());
    aUBLNote.setLanguageLocaleID (aText.getLanguageLocaleID ());
    return aUBLNote;
  }

  @Nullable
  private static DocumentReferenceType _convertDocumentReference (@Nullable final ReferencedDocumentType aRD,
                                                                  @Nonnull final IErrorList aErrorList)
  {
    if (aRD == null)
      return null;

    final String sID = aRD.getIssuerAssignedIDValue ();
    if (StringHelper.hasNoText (sID))
      return null;

    final DocumentReferenceType ret = new DocumentReferenceType ();
    // ID value is a mandatory field
    ret.setID (sID).setSchemeID (aRD.getReferenceTypeCodeValue ());

    // IssueDate is optional
    final FormattedDateTimeType aFDT = aRD.getFormattedIssueDateTime ();
    if (aFDT != null)
      ret.setIssueDate (_parseDateDDMMYYYY (aFDT.getDateTimeStringValue (), aErrorList));

    // Name is optional
    for (final TextType aItem : aRD.getName ())
    {
      final DocumentDescriptionType aUBLDocDesc = new DocumentDescriptionType ();
      aUBLDocDesc.setValue (aItem.getValue ());
      aUBLDocDesc.setLanguageID (aItem.getLanguageID ());
      aUBLDocDesc.setLanguageLocaleID (aItem.getLanguageLocaleID ());
      ret.addDocumentDescription (aUBLDocDesc);
    }

    // Attachment (0..1 for CII)
    if (aRD.getAttachmentBinaryObjectCount () > 0)
    {
      final BinaryObjectType aBinObj = aRD.getAttachmentBinaryObjectAtIndex (0);

      final AttachmentType aUBLAttachment = new AttachmentType ();
      final EmbeddedDocumentBinaryObjectType aEmbeddedDoc = new EmbeddedDocumentBinaryObjectType ();
      aEmbeddedDoc.setMimeCode (aBinObj.getMimeCode ());
      aEmbeddedDoc.setFilename (aBinObj.getFilename ());
      aUBLAttachment.setEmbeddedDocumentBinaryObject (aEmbeddedDoc);

      final String sURI = aRD.getURIIDValue ();
      if (StringHelper.hasText (sURI))
      {
        final ExternalReferenceType aUBLExtRef = new ExternalReferenceType ();
        aUBLExtRef.setURI (sURI);
        aUBLAttachment.setExternalReference (aUBLExtRef);
      }
      ret.setAttachment (aUBLAttachment);
    }
    return ret;
  }

  @Nonnull
  private static AddressType _convertPostalAddress (@Nonnull final TradeAddressType aPostalAddress)
  {
    final AddressType ret = new AddressType ();
    ret.setStreetName (aPostalAddress.getLineOneValue ());
    if (StringHelper.hasText (aPostalAddress.getLineTwoValue ()))
      ret.setAdditionalStreetName (aPostalAddress.getLineTwoValue ());
    if (StringHelper.hasText (aPostalAddress.getLineThreeValue ()))
    {
      final AddressLineType aUBLAddressLine = new AddressLineType ();
      aUBLAddressLine.setLine (aPostalAddress.getLineThreeValue ());
      ret.addAddressLine (aUBLAddressLine);
    }
    ret.setCityName (aPostalAddress.getCityNameValue ());
    ret.setPostalZone (aPostalAddress.getPostcodeCodeValue ());
    if (aPostalAddress.hasCountrySubDivisionNameEntries ())
      ret.setCountrySubentity (aPostalAddress.getCountrySubDivisionNameAtIndex (0).getValue ());
    if (StringHelper.hasText (aPostalAddress.getCountryIDValue ()))
    {
      final CountryType aUBLCountry = new CountryType ();
      aUBLCountry.setIdentificationCode (aPostalAddress.getCountryIDValue ());
      ret.setCountry (aUBLCountry);
    }
    return ret;
  }

  @Nullable
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType _extractPartyID (@Nonnull final TradePartyType aParty)
  {
    IDType aID;
    if (aParty.hasGlobalIDEntries ())
      aID = aParty.getGlobalIDAtIndex (0);
    else
      if (aParty.hasIDEntries ())
        aID = aParty.getIDAtIndex (0);
      else
        aID = null;

    return aID == null ? null : _copyID (aID);
  }

  @Nonnull
  private static PartyType _convertParty (@Nonnull final TradePartyType aParty)
  {
    final PartyType ret = new PartyType ();

    if (aParty.hasURIUniversalCommunicationEntries ())
    {
      final UniversalCommunicationType UC = aParty.getURIUniversalCommunicationAtIndex (0);
      ret.setEndpointID (_copyID (UC.getURIID (), new EndpointIDType ()));
    }

    final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType aUBLID = _extractPartyID (aParty);
    if (aUBLID != null)
    {
      final PartyIdentificationType aUBLPartyIdentification = new PartyIdentificationType ();
      aUBLPartyIdentification.setID (aUBLID);
      ret.addPartyIdentification (aUBLPartyIdentification);
    }

    final TextType aName = aParty.getName ();
    if (aName != null)
    {
      final PartyNameType aUBLPartyName = new PartyNameType ();
      aUBLPartyName.setName (_copyName (aName, new NameType ()));
      ret.addPartyName (aUBLPartyName);
    }

    final TradeAddressType aPostalAddress = aParty.getPostalTradeAddress ();
    if (aPostalAddress != null)
    {
      ret.setPostalAddress (_convertPostalAddress (aPostalAddress));
    }

    return ret;
  }

  @Nonnull
  private static PartyTaxSchemeType _convertPartyTaxScheme (@Nonnull final TaxRegistrationType aTaxRegistration)
  {
    if (aTaxRegistration.getID () == null)
      return null;

    final PartyTaxSchemeType aUBLPartyTaxScheme = new PartyTaxSchemeType ();
    aUBLPartyTaxScheme.setCompanyID (aTaxRegistration.getIDValue ());

    String sSchemeID = aTaxRegistration.getID ().getSchemeID ();
    if (StringHelper.hasNoText (sSchemeID))
      sSchemeID = "VAT";

    final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
    aUBLTaxScheme.setID (sSchemeID);
    aUBLPartyTaxScheme.setTaxScheme (aUBLTaxScheme);
    return aUBLPartyTaxScheme;
  }

  @Nullable
  private static PartyLegalEntityType _convertPartyLegalEntity (@Nonnull final TradePartyType aTradeParty)
  {
    final PartyLegalEntityType aUBLPartyLegalEntity = new PartyLegalEntityType ();

    final LegalOrganizationType aSLO = aTradeParty.getSpecifiedLegalOrganization ();
    if (aSLO != null)
    {
      if (StringHelper.hasText (aSLO.getTradingBusinessNameValue ()))
        aUBLPartyLegalEntity.setRegistrationName (aSLO.getTradingBusinessNameValue ());

      aUBLPartyLegalEntity.setCompanyID (_copyID (aSLO.getID (), new CompanyIDType ()));
    }

    for (final TextType aDesc : aTradeParty.getDescription ())
      if (StringHelper.hasText (aDesc.getValue ()))
      {
        // Use the first only
        aUBLPartyLegalEntity.setCompanyLegalForm (aDesc.getValue ());
        break;
      }

    if (aUBLPartyLegalEntity.getRegistrationName () == null)
    {
      // Mandatory field according to Schematron
      aUBLPartyLegalEntity.setRegistrationName (aTradeParty.getNameValue ());
    }

    return aUBLPartyLegalEntity;
  }

  @Nullable
  private static ContactType _convertContact (@Nonnull final TradePartyType aTradeParty)
  {
    if (!aTradeParty.hasDefinedTradeContactEntries ())
      return null;

    final TradeContactType aDTC = aTradeParty.getDefinedTradeContactAtIndex (0);
    final ContactType aUBLContact = new ContactType ();
    boolean bUseContact = false;
    if (aDTC.getPersonName () != null)
    {
      aUBLContact.setName (_copyName (aDTC.getPersonName (), new NameType ()));
      bUseContact = true;
    }

    final UniversalCommunicationType aTel = aDTC.getTelephoneUniversalCommunication ();
    if (aTel != null)
    {
      aUBLContact.setTelephone (aTel.getCompleteNumberValue ());
      bUseContact = true;
    }

    final UniversalCommunicationType aEmail = aDTC.getEmailURIUniversalCommunication ();
    if (aEmail != null)
    {
      aUBLContact.setElectronicMail (aEmail.getURIIDValue ());
      bUseContact = true;
    }

    return bUseContact ? aUBLContact : null;
  }

  @Nullable
  private static <T extends oasis.names.specification.ubl.schema.xsd.unqualifieddatatypes_21.AmountType> T _copyAmount (@Nullable final AmountType aAmount,
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

  @Nullable
  private static <T extends oasis.names.specification.ubl.schema.xsd.unqualifieddatatypes_21.QuantityType> T _copyQuantity (@Nullable final QuantityType aQuantity,
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
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AmountType _copyAmount (@Nullable final AmountType aAmount,
                                                                                                           @Nullable final String sDefaultCurrencyCode)
  {
    return _copyAmount (aAmount,
                        new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.AmountType (),
                        sDefaultCurrencyCode);
  }

  @Nullable
  private static <T extends com.helger.xsds.ccts.cct.schemamodule.CodeType> T _copyCode (@Nullable final CodeType aCode,
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

  private static void _copyAllowanceCharge (@Nonnull final TradeAllowanceChargeType aAllowanceCharge,
                                            @Nonnull final AllowanceChargeType aUBLAllowanceCharge,
                                            @Nullable final String sDefaultCurrencyCode)
  {
    if (StringHelper.hasText (aAllowanceCharge.getReasonCodeValue ()))
      aUBLAllowanceCharge.setAllowanceChargeReasonCode (aAllowanceCharge.getReasonCodeValue ());

    if (aAllowanceCharge.getReason () != null)
    {
      final AllowanceChargeReasonType aUBLReason = new AllowanceChargeReasonType ();
      aUBLReason.setValue (aAllowanceCharge.getReasonValue ());
      aUBLAllowanceCharge.addAllowanceChargeReason (aUBLReason);
    }
    if (aAllowanceCharge.getCalculationPercent () != null)
    {
      // TODO calc is correct?
      aUBLAllowanceCharge.setMultiplierFactorNumeric (aAllowanceCharge.getCalculationPercentValue ()
                                                                      .divide (CGlobal.BIGDEC_100));
    }
    if (aAllowanceCharge.hasActualAmountEntries ())
    {
      aUBLAllowanceCharge.setAmount (_copyAmount (aAllowanceCharge.getActualAmountAtIndex (0), sDefaultCurrencyCode));
    }

    aUBLAllowanceCharge.setBaseAmount (_copyAmount (aAllowanceCharge.getBasisAmount (),
                                                    new BaseAmountType (),
                                                    sDefaultCurrencyCode));

    // TaxCategory
    for (final TradeTaxType aTradeTax : aAllowanceCharge.getCategoryTradeTax ())
    {
      final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
      aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
      if (aTradeTax.getRateApplicablePercentValue () != null)
        aUBLTaxCategory.setPercent (aTradeTax.getRateApplicablePercentValue ());
      final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
      aUBLTaxScheme.setID ("VAT");
      aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
      aUBLAllowanceCharge.addTaxCategory (aUBLTaxCategory);
    }
  }

  @Nonnull
  private InvoiceType _convertToInvoice (@Nonnull final CrossIndustryInvoiceType aCIIInvoice,
                                         @Nonnull final ErrorList aErrorList)
  {
    final ExchangedDocumentType aED = aCIIInvoice.getExchangedDocument ();
    final SupplyChainTradeTransactionType aSCTT = aCIIInvoice.getSupplyChainTradeTransaction ();
    final HeaderTradeAgreementType aHeaderAgreement = aSCTT.getApplicableHeaderTradeAgreement ();
    final HeaderTradeDeliveryType aHeaderDelivery = aSCTT.getApplicableHeaderTradeDelivery ();
    final HeaderTradeSettlementType aHeaderSettlement = aSCTT.getApplicableHeaderTradeSettlement ();

    final InvoiceType aUBLInvoice = new InvoiceType ();
    aUBLInvoice.setUBLVersionID ("2.1");
    aUBLInvoice.setCustomizationID ("urn:cen.eu:en16931:2017:extended:urn:fdc:peppol.eu:2017:poacc:billing:3.0");
    aUBLInvoice.setProfileID ("urn:fdc:peppol.eu:2017:poacc:billing:01:1.0");
    aUBLInvoice.setID (aED.getIDValue ());

    // Mandatory supplier
    final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
    aUBLInvoice.setAccountingSupplierParty (aUBLSupplier);

    // Mandatory customer
    final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
    aUBLInvoice.setAccountingCustomerParty (aUBLCustomer);

    // IssueDate
    {
      XMLGregorianCalendar aIssueDate = null;
      if (aED.getIssueDateTime () != null)
        aIssueDate = _parseDateDDMMYYYY (aED.getIssueDateTime ().getDateTimeStringValue (), aErrorList);

      if (aIssueDate != null)
        aUBLInvoice.setIssueDate (aIssueDate);
    }

    // DueDate
    {
      XMLGregorianCalendar aDueDate = null;
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
        if (aPaymentTerms.getDueDateDateTime () != null)
        {
          aDueDate = _parseDateDDMMYYYY (aPaymentTerms.getDueDateDateTime ().getDateTimeStringValue (), aErrorList);
          if (aDueDate != null)
            break;
        }
      if (aDueDate != null)
        aUBLInvoice.setDueDate (aDueDate);
    }

    // InvoiceTypeCode
    aUBLInvoice.setInvoiceTypeCode (aED.getTypeCodeValue ());

    // Note
    {
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aEDNote : aED.getIncludedNote ())
        aUBLInvoice.addNote (_copyNote (aEDNote));
    }

    // TaxPointDate
    for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
    {
      if (aTradeTax.getTaxPointDate () != null)
      {
        final XMLGregorianCalendar aTaxPointDate = _parseDateDDMMYYYY (aTradeTax.getTaxPointDate ()
                                                                                .getDateStringValue (),
                                                                       aErrorList);
        if (aTaxPointDate != null)
        {
          // Use the first tax point date only
          aUBLInvoice.setTaxPointDate (aTaxPointDate);
          break;
        }
      }
    }

    // DocumentCurrencyCode
    final String sDefaultCurrencyCode = aHeaderSettlement.getInvoiceCurrencyCodeValue ();
    aUBLInvoice.setDocumentCurrencyCode (sDefaultCurrencyCode);

    // TaxCurrencyCode
    if (aHeaderSettlement.getTaxCurrencyCodeValue () != null)
    {
      aUBLInvoice.setTaxCurrencyCode (aHeaderSettlement.getTaxCurrencyCodeValue ());
    }

    // AccountingCost
    for (final TradeAccountingAccountType aAccount : aHeaderSettlement.getReceivableSpecifiedTradeAccountingAccount ())
    {
      final String sID = aAccount.getIDValue ();
      if (StringHelper.hasText (sID))
      {
        // Use the first ID
        aUBLInvoice.setAccountingCost (sID);
        break;
      }
    }

    // BuyerReferences
    if (aHeaderAgreement.getBuyerReferenceValue () != null)
    {
      aUBLInvoice.setBuyerReference (aHeaderAgreement.getBuyerReferenceValue ());
    }

    // InvoicePeriod
    {
      final SpecifiedPeriodType aSPT = aHeaderSettlement.getBillingSpecifiedPeriod ();
      if (aSPT != null)
      {
        final DateTimeType aStartDT = aSPT.getStartDateTime ();
        final DateTimeType aEndDT = aSPT.getEndDateTime ();

        if (aStartDT != null && aEndDT != null)
        {
          final PeriodType aUBLPeriod = new PeriodType ();
          aUBLPeriod.setStartDate (_parseDateDDMMYYYY (aStartDT.getDateTimeStringValue (), aErrorList));
          aUBLPeriod.setEndDate (_parseDateDDMMYYYY (aEndDT.getDateTimeStringValue (), aErrorList));
          aUBLInvoice.addInvoicePeriod (aUBLPeriod);
        }
      }
    }

    // OrderReference
    {
      final OrderReferenceType aUBLOrderRef = new OrderReferenceType ();
      final ReferencedDocumentType aBuyerOrderRef = aHeaderAgreement.getBuyerOrderReferencedDocument ();
      if (aBuyerOrderRef != null)
        aUBLOrderRef.setID (aBuyerOrderRef.getIssuerAssignedIDValue ());
      final ReferencedDocumentType aSellerOrderRef = aHeaderAgreement.getSellerOrderReferencedDocument ();
      if (aSellerOrderRef != null)
        aUBLOrderRef.setSalesOrderID (aSellerOrderRef.getIssuerAssignedIDValue ());

      // Set if any field is set
      if (aUBLOrderRef.getIDValue () != null || aUBLOrderRef.getSalesOrderIDValue () != null)
        aUBLInvoice.setOrderReference (aUBLOrderRef);
    }

    // BillingReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderSettlement.getInvoiceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
      {
        final BillingReferenceType aUBLBillingRef = new BillingReferenceType ();
        aUBLBillingRef.setInvoiceDocumentReference (aUBLDocRef);
        aUBLInvoice.addBillingReference (aUBLBillingRef);
      }
    }

    // DespatchDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getDespatchAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLInvoice.addDespatchDocumentReference (aUBLDocRef);
    }

    // ReceiptDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getReceivingAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLInvoice.addReceiptDocumentReference (aUBLDocRef);
    }

    // OriginatorDocumentReference
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Use for "Tender or lot reference" with TypeCode "50"
        if ("50".equals (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
            aUBLInvoice.addOriginatorDocumentReference (aUBLDocRef);
        }
      }
    }

    // ContractDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderAgreement.getContractReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLInvoice.addContractDocumentReference (aUBLDocRef);
    }

    // AdditionalDocumentReference
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Except OriginatorDocumentReference
        if (!"50".equals (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
            aUBLInvoice.addAdditionalDocumentReference (aUBLDocRef);
        }
      }
    }

    // ProjectReference
    {
      final ProcuringProjectType aSpecifiedProcuring = aHeaderAgreement.getSpecifiedProcuringProject ();
      if (aSpecifiedProcuring != null)
      {
        final String sID = aSpecifiedProcuring.getIDValue ();
        if (StringHelper.hasText (sID))
        {
          final ProjectReferenceType aUBLProjectRef = new ProjectReferenceType ();
          aUBLProjectRef.setID (sID);
          aUBLInvoice.addProjectReference (aUBLProjectRef);
        }
      }
    }

    // Supplier Party
    {
      final TradePartyType aSellerParty = aHeaderAgreement.getSellerTradeParty ();
      if (aSellerParty != null)
      {
        final PartyType aUBLParty = _convertParty (aSellerParty);

        for (final TaxRegistrationType aTaxRegistration : aSellerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        final PartyLegalEntityType aUBLPartyLegalEntity = _convertPartyLegalEntity (aSellerParty);
        if (aUBLPartyLegalEntity != null)
          aUBLParty.addPartyLegalEntity (aUBLPartyLegalEntity);

        final ContactType aUBLContact = _convertContact (aSellerParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLSupplier.setParty (aUBLParty);
      }
    }

    // Customer Party
    {
      final TradePartyType aBuyerParty = aHeaderAgreement.getBuyerTradeParty ();
      if (aBuyerParty != null)
      {
        final PartyType aUBLParty = _convertParty (aBuyerParty);

        for (final TaxRegistrationType aTaxRegistration : aBuyerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        final PartyLegalEntityType aUBLPartyLegalEntity = _convertPartyLegalEntity (aBuyerParty);
        if (aUBLPartyLegalEntity != null)
          aUBLParty.addPartyLegalEntity (aUBLPartyLegalEntity);

        final ContactType aUBLContact = _convertContact (aBuyerParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLCustomer.setParty (aUBLParty);
      }
    }

    // Payee Party
    {
      final TradePartyType aPayeeParty = aHeaderSettlement.getPayeeTradeParty ();
      if (aPayeeParty != null)
      {
        final PartyType aUBLParty = _convertParty (aPayeeParty);

        for (final TaxRegistrationType aTaxRegistration : aPayeeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // validation rules warning
        if (false)
        {
          final PartyLegalEntityType aUBLPartyLegalEntity = _convertPartyLegalEntity (aPayeeParty);
          if (aUBLPartyLegalEntity != null)
            aUBLParty.addPartyLegalEntity (aUBLPartyLegalEntity);
        }

        final ContactType aUBLContact = _convertContact (aPayeeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLInvoice.setPayeeParty (aUBLParty);
      }
    }

    // Tax Representative Party
    {
      final TradePartyType aTaxRepresentativeParty = aHeaderAgreement.getSellerTaxRepresentativeTradeParty ();
      if (aTaxRepresentativeParty != null)
      {
        final PartyType aUBLParty = _convertParty (aTaxRepresentativeParty);

        for (final TaxRegistrationType aTaxRegistration : aTaxRepresentativeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // validation rules warning
        if (false)
        {
          final PartyLegalEntityType aUBLPartyLegalEntity = _convertPartyLegalEntity (aTaxRepresentativeParty);
          if (aUBLPartyLegalEntity != null)
            aUBLParty.addPartyLegalEntity (aUBLPartyLegalEntity);
        }

        final ContactType aUBLContact = _convertContact (aTaxRepresentativeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLInvoice.setTaxRepresentativeParty (aUBLParty);
      }
    }

    // Delivery
    {
      final TradePartyType aShipToParty = aHeaderDelivery.getShipToTradeParty ();
      if (aShipToParty != null)
      {
        final DeliveryType aUBLDelivery = new DeliveryType ();

        final SupplyChainEventType aSCE = aHeaderDelivery.getActualDeliverySupplyChainEvent ();
        if (aSCE != null)
        {
          final DateTimeType aODT = aSCE.getOccurrenceDateTime ();
          if (aODT != null)
            aUBLDelivery.setActualDeliveryDate (_parseDateDDMMYYYY (aODT.getDateTimeStringValue (), aErrorList));
        }

        final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType aUBLDeliveryLocation = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.LocationType ();
        boolean bUseLocation = false;

        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType aUBLID = _extractPartyID (aShipToParty);
        if (aUBLID != null)
        {
          aUBLDeliveryLocation.setID (aUBLID);
          bUseLocation = true;
        }

        final TradeAddressType aPostalAddress = aShipToParty.getPostalTradeAddress ();
        if (aPostalAddress != null)
        {
          aUBLDeliveryLocation.setAddress (_convertPostalAddress (aPostalAddress));
          bUseLocation = true;
        }

        if (bUseLocation)
          aUBLDelivery.setDeliveryLocation (aUBLDeliveryLocation);

        final TextType aName = aShipToParty.getName ();
        if (aName != null)
        {
          final PartyType aUBLDeliveryParty = new PartyType ();
          final PartyNameType aUBLPartyName = new PartyNameType ();
          aUBLPartyName.setName (_copyName (aName, new NameType ()));
          aUBLDeliveryParty.addPartyName (aUBLPartyName);
          aUBLDelivery.setDeliveryParty (aUBLDeliveryParty);
        }

        aUBLInvoice.addDelivery (aUBLDelivery);
      }
    }

    // Payment means
    {
      for (final TradeSettlementPaymentMeansType aPaymentMeans : aHeaderSettlement.getSpecifiedTradeSettlementPaymentMeans ())
      {
        final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();

        final PaymentMeansCodeType aUBLPaymentMeansCode = new PaymentMeansCodeType ();
        aUBLPaymentMeansCode.setValue (aPaymentMeans.getTypeCodeValue ());
        if (aPaymentMeans.hasInformationEntries ())
          aUBLPaymentMeansCode.setName (aPaymentMeans.getInformationAtIndex (0).getValue ());
        aUBLPaymentMeans.setPaymentMeansCode (aUBLPaymentMeansCode);

        final boolean bRequiresPayeeFinancialAccountID = "30".equals (aUBLPaymentMeansCode.getValue ()) ||
                                                         "58".equals (aUBLPaymentMeansCode.getValue ());

        for (final TextType aPaymentRef : aHeaderSettlement.getPaymentReference ())
        {
          final PaymentIDType aUBLPaymentID = new PaymentIDType ();
          aUBLPaymentID.setValue (aPaymentRef.getValue ());
          aUBLPaymentMeans.addPaymentID (aUBLPaymentID);
        }

        final TradeSettlementFinancialCardType aCard = aPaymentMeans.getApplicableTradeSettlementFinancialCard ();
        if (aCard != null)
        {
          final CardAccountType aUBLCardAccount = new CardAccountType ();
          aUBLCardAccount.setPrimaryAccountNumberID (_copyID (aCard.getID (), new PrimaryAccountNumberIDType ()));
          // No CII field present
          aUBLCardAccount.setNetworkID ("mapped-from-cii");
          aUBLCardAccount.setHolderName (aCard.getCardholderNameValue ());
          aUBLPaymentMeans.setCardAccount (aUBLCardAccount);
        }

        {
          final FinancialAccountType aUBLFinancialAccount = new FinancialAccountType ();

          final CreditorFinancialAccountType aAccount = aPaymentMeans.getPayeePartyCreditorFinancialAccount ();
          if (aAccount != null)
          {
            aUBLFinancialAccount.setID (_copyID (aAccount.getIBANID ()));
            aUBLFinancialAccount.setName (_copyName (aAccount.getAccountName (), new NameType ()));
          }
          else
          {
            // For PaymentMeansCode 58
            final DebtorFinancialAccountType aAccount2 = aPaymentMeans.getPayerPartyDebtorFinancialAccount ();
            if (aAccount2 != null)
            {
              aUBLFinancialAccount.setID (_copyID (aAccount2.getIBANID ()));
              aUBLFinancialAccount.setName (_copyName (aAccount2.getAccountName (), new NameType ()));
            }
          }

          if (bRequiresPayeeFinancialAccountID && aUBLFinancialAccount.getID () == null)
          {
            // Ignore PaymentMeans because required IBAN is missing
            continue;
          }

          final CreditorFinancialInstitutionType aInstitution = aPaymentMeans.getPayeeSpecifiedCreditorFinancialInstitution ();
          if (aInstitution != null)
          {
            final BranchType aUBLBranch = new BranchType ();
            aUBLBranch.setID (_copyID (aInstitution.getBICID ()));
            aUBLFinancialAccount.setFinancialInstitutionBranch (aUBLBranch);
          }

          if (aUBLFinancialAccount.getID () != null ||
              aUBLFinancialAccount.getName () != null ||
              aUBLFinancialAccount.getFinancialInstitutionBranch () != null)
            aUBLPaymentMeans.setPayeeFinancialAccount (aUBLFinancialAccount);
        }

        {
          boolean bUseMandate = false;
          final PaymentMandateType aUBLPaymentMandate = new PaymentMandateType ();

          for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
          {
            if (aPaymentTerms.hasDirectDebitMandateIDEntries ())
            {
              aUBLPaymentMandate.setID (_copyID (aPaymentTerms.getDirectDebitMandateIDAtIndex (0)));
              bUseMandate = true;
              break;
            }
          }

          final IDType aCreditorRefID = aHeaderSettlement.getCreditorReferenceID ();
          if (aCreditorRefID != null)
          {
            final FinancialAccountType aUBLFinancialAccount = new FinancialAccountType ();
            aUBLFinancialAccount.setID (_copyID (aCreditorRefID));
            aUBLPaymentMandate.setPayerFinancialAccount (aUBLFinancialAccount);
            bUseMandate = true;
          }

          if (bUseMandate)
            aUBLPaymentMeans.setPaymentMandate (aUBLPaymentMandate);
        }

        aUBLInvoice.addPaymentMeans (aUBLPaymentMeans);
      }
    }

    // Payment Terms
    {
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
      {
        boolean bUsePaymenTerms = false;
        final PaymentTermsType aUBLPaymenTerms = new PaymentTermsType ();

        for (final TextType aDesc : aPaymentTerms.getDescription ())
        {
          aUBLPaymenTerms.addNote (_copyNote (aDesc));
          bUsePaymenTerms = true;
        }

        if (bUsePaymenTerms)
          aUBLInvoice.addPaymentTerms (aUBLPaymenTerms);
      }
    }

    // Allowance Charge
    {
      for (final TradeAllowanceChargeType aAllowanceCharge : aHeaderSettlement.getSpecifiedTradeAllowanceCharge ())
      {
        ETriState eIsCharge = ETriState.UNDEFINED;
        if (aAllowanceCharge.getChargeIndicator () != null)
          eIsCharge = _parseIndicator (aAllowanceCharge.getChargeIndicator (), aErrorList);
        else
          aErrorList.add (_buildError (new String [] { "CrossIndustryInvoice",
                                                       "SupplyChainTradeTransaction",
                                                       "ApplicableHeaderTradeSettlement",
                                                       "SpecifiedTradeAllowanceCharge" },
                                       "Failed to determine if SpecifiedTradeAllowanceCharge is an Allowance or a Charge"));
        if (eIsCharge.isDefined ())
        {
          final AllowanceChargeType aUBLAllowanceCharge = new AllowanceChargeType ();
          aUBLAllowanceCharge.setChargeIndicator (eIsCharge.getAsBooleanValue ());
          _copyAllowanceCharge (aAllowanceCharge, aUBLAllowanceCharge, sDefaultCurrencyCode);
          aUBLInvoice.addAllowanceCharge (aUBLAllowanceCharge);
        }
      }
    }

    final TradeSettlementHeaderMonetarySummationType aSTSHMS = aHeaderSettlement.getSpecifiedTradeSettlementHeaderMonetarySummation ();

    // TaxTotal
    {
      TaxTotalType aUBLTaxTotal = null;
      if (aSTSHMS != null && aSTSHMS.hasTaxTotalAmountEntries ())
      {
        // For all currencies
        for (final AmountType aTaxTotalAmount : aSTSHMS.getTaxTotalAmount ())
        {
          final TaxTotalType aUBLCurTaxTotal = new TaxTotalType ();
          aUBLCurTaxTotal.setTaxAmount (_copyAmount (aTaxTotalAmount, new TaxAmountType (), sDefaultCurrencyCode));
          aUBLInvoice.addTaxTotal (aUBLCurTaxTotal);

          if (aUBLTaxTotal == null)
          {
            // Use the first one
            aUBLTaxTotal = aUBLCurTaxTotal;
          }
        }
      }
      else
      {
        // Mandatory in UBL
        final TaxAmountType aUBLTaxAmount = new TaxAmountType ();
        aUBLTaxAmount.setValue (BigDecimal.ZERO);
        aUBLTaxAmount.setCurrencyID (sDefaultCurrencyCode);

        aUBLTaxTotal = new TaxTotalType ();
        aUBLTaxTotal.setTaxAmount (aUBLTaxAmount);
        aUBLInvoice.addTaxTotal (aUBLTaxTotal);
      }

      for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();

        if (aTradeTax.hasBasisAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxableAmount (_copyAmount (aTradeTax.getBasisAmountAtIndex (0),
                                                         new TaxableAmountType (),
                                                         sDefaultCurrencyCode));
        }

        if (aTradeTax.hasCalculatedAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxAmount (_copyAmount (aTradeTax.getCalculatedAmountAtIndex (0),
                                                     new TaxAmountType (),
                                                     sDefaultCurrencyCode));
        }

        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (aTradeTax.getRateApplicablePercentValue ());
        if (StringHelper.hasText (aTradeTax.getExemptionReasonCodeValue ()))
          aUBLTaxCategory.setTaxExemptionReasonCode (aTradeTax.getExemptionReasonCodeValue ());
        if (aTradeTax.getExemptionReason () != null)
        {
          final TaxExemptionReasonType aUBLTaxExemptionReason = new TaxExemptionReasonType ();
          aUBLTaxExemptionReason.setValue (aTradeTax.getExemptionReason ().getValue ());
          aUBLTaxExemptionReason.setLanguageID (aTradeTax.getExemptionReason ().getLanguageID ());
          aUBLTaxExemptionReason.setLanguageLocaleID (aTradeTax.getExemptionReason ().getLanguageLocaleID ());
          aUBLTaxCategory.addTaxExemptionReason (aUBLTaxExemptionReason);
        }
        final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
        aUBLTaxScheme.setID ("VAT");
        aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);

        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);
      }
    }

    // LegalMonetaryTotal
    {
      final MonetaryTotalType aUBLMonetaryTotal = new MonetaryTotalType ();
      if (aSTSHMS != null)
      {
        if (aSTSHMS.hasLineTotalAmountEntries ())
          aUBLMonetaryTotal.setLineExtensionAmount (_copyAmount (aSTSHMS.getLineTotalAmountAtIndex (0),
                                                                 new LineExtensionAmountType (),
                                                                 sDefaultCurrencyCode));
        if (aSTSHMS.hasTaxBasisTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxExclusiveAmount (_copyAmount (aSTSHMS.getTaxBasisTotalAmountAtIndex (0),
                                                                new TaxExclusiveAmountType (),
                                                                sDefaultCurrencyCode));
        if (aSTSHMS.hasGrandTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxInclusiveAmount (_copyAmount (aSTSHMS.getGrandTotalAmountAtIndex (0),
                                                                new TaxInclusiveAmountType (),
                                                                sDefaultCurrencyCode));
        if (aSTSHMS.hasAllowanceTotalAmountEntries ())
          aUBLMonetaryTotal.setAllowanceTotalAmount (_copyAmount (aSTSHMS.getAllowanceTotalAmountAtIndex (0),
                                                                  new AllowanceTotalAmountType (),
                                                                  sDefaultCurrencyCode));
        if (aSTSHMS.hasChargeTotalAmountEntries ())
          aUBLMonetaryTotal.setChargeTotalAmount (_copyAmount (aSTSHMS.getChargeTotalAmountAtIndex (0),
                                                               new ChargeTotalAmountType (),
                                                               sDefaultCurrencyCode));
        if (aSTSHMS.hasTotalPrepaidAmountEntries ())
          aUBLMonetaryTotal.setPrepaidAmount (_copyAmount (aSTSHMS.getTotalPrepaidAmountAtIndex (0),
                                                           new PrepaidAmountType (),
                                                           sDefaultCurrencyCode));
        if (aSTSHMS.hasRoundingAmountEntries ())
          aUBLMonetaryTotal.setPayableRoundingAmount (_copyAmount (aSTSHMS.getRoundingAmountAtIndex (0),
                                                                   new PayableRoundingAmountType (),
                                                                   sDefaultCurrencyCode));
        if (aSTSHMS.hasDuePayableAmountEntries ())
          aUBLMonetaryTotal.setPayableAmount (_copyAmount (aSTSHMS.getDuePayableAmountAtIndex (0),
                                                           new PayableAmountType (),
                                                           sDefaultCurrencyCode));
      }
      aUBLInvoice.setLegalMonetaryTotal (aUBLMonetaryTotal);
    }

    // All invoice lines
    for (final SupplyChainTradeLineItemType aLineItem : aSCTT.getIncludedSupplyChainTradeLineItem ())
    {
      final InvoiceLineType aUBLInvoiceLine = new InvoiceLineType ();

      final DocumentLineDocumentType aDLD = aLineItem.getAssociatedDocumentLineDocument ();
      aUBLInvoiceLine.setID (_copyID (aDLD.getLineID ()));

      // Note
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aLineNote : aDLD.getIncludedNote ())
        aUBLInvoiceLine.addNote (_copyNote (aLineNote));

      // Invoiced quantity
      final LineTradeDeliveryType aLineDelivery = aLineItem.getSpecifiedLineTradeDelivery ();
      if (aLineDelivery != null)
      {
        final QuantityType aBilledQuantity = aLineDelivery.getBilledQuantity ();
        if (aBilledQuantity != null)
        {
          aUBLInvoiceLine.setInvoicedQuantity (_copyQuantity (aBilledQuantity, new InvoicedQuantityType ()));
        }
      }

      // Line extension amount
      final LineTradeSettlementType aLineSettlement = aLineItem.getSpecifiedLineTradeSettlement ();
      final TradeSettlementLineMonetarySummationType aSTSLMS = aLineSettlement.getSpecifiedTradeSettlementLineMonetarySummation ();
      if (aSTSLMS != null)
      {
        if (aSTSLMS.hasLineTotalAmountEntries ())
          aUBLInvoiceLine.setLineExtensionAmount (_copyAmount (aSTSLMS.getLineTotalAmountAtIndex (0),
                                                               new LineExtensionAmountType (),
                                                               sDefaultCurrencyCode));
      }

      // Accounting cost
      if (aLineSettlement.hasReceivableSpecifiedTradeAccountingAccountEntries ())
      {
        final TradeAccountingAccountType aLineAA = aLineSettlement.getReceivableSpecifiedTradeAccountingAccountAtIndex (0);
        aUBLInvoiceLine.setAccountingCost (aLineAA.getIDValue ());
      }

      // Invoice period
      final SpecifiedPeriodType aLineBillingPeriod = aLineSettlement.getBillingSpecifiedPeriod ();
      if (aLineBillingPeriod != null)
      {
        final PeriodType aUBLLinePeriod = new PeriodType ();
        if (aLineBillingPeriod.getStartDateTime () != null)
          aUBLLinePeriod.setStartDate (_parseDateDDMMYYYY (aLineBillingPeriod.getStartDateTime ()
                                                                             .getDateTimeStringValue (),
                                                           aErrorList));
        if (aLineBillingPeriod.getEndDateTime () != null)
          aUBLLinePeriod.setEndDate (_parseDateDDMMYYYY (aLineBillingPeriod.getEndDateTime ().getDateTimeStringValue (),
                                                         aErrorList));
        aUBLInvoiceLine.addInvoicePeriod (aUBLLinePeriod);
      }

      // Order line reference
      final LineTradeAgreementType aLineAgreement = aLineItem.getSpecifiedLineTradeAgreement ();
      if (aLineAgreement != null)
      {
        final ReferencedDocumentType aOrderReference = aLineAgreement.getBuyerOrderReferencedDocument ();
        if (aOrderReference != null)
        {
          final OrderLineReferenceType aUBLOrderLineReference = new OrderLineReferenceType ();
          aUBLOrderLineReference.setLineID (_copyID (aOrderReference.getLineID (), new LineIDType ()));
          aUBLInvoiceLine.addOrderLineReference (aUBLOrderLineReference);
        }
      }

      // Document reference
      for (final ReferencedDocumentType aLineReferencedDocument : aLineSettlement.getAdditionalReferencedDocument ())
      {
        final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aLineReferencedDocument, aErrorList);
        if (aUBLDocRef != null)
          aUBLInvoiceLine.addDocumentReference (aUBLDocRef);
      }

      // Allowance charge
      for (final TradeAllowanceChargeType aLineAllowanceCharge : aLineSettlement.getSpecifiedTradeAllowanceCharge ())
      {
        ETriState eIsCharge = ETriState.UNDEFINED;
        if (aLineAllowanceCharge.getChargeIndicator () != null)
          eIsCharge = _parseIndicator (aLineAllowanceCharge.getChargeIndicator (), aErrorList);
        else
          aErrorList.add (_buildError (new String [] { "CrossIndustryInvoice",
                                                       "SupplyChainTradeTransaction",
                                                       "IncludedSupplyChainTradeLineItem",
                                                       "SpecifiedLineTradeSettlement",
                                                       "SpecifiedTradeAllowanceCharge" },
                                       "Failed to determine if SpecifiedTradeAllowanceCharge is an Allowance or a Charge"));
        if (eIsCharge.isDefined ())
        {
          final AllowanceChargeType aUBLLineAllowanceCharge = new AllowanceChargeType ();
          aUBLLineAllowanceCharge.setChargeIndicator (eIsCharge.getAsBooleanValue ());
          _copyAllowanceCharge (aLineAllowanceCharge, aUBLLineAllowanceCharge, sDefaultCurrencyCode);
          aUBLInvoiceLine.addAllowanceCharge (aUBLLineAllowanceCharge);
        }
      }

      // Item
      final ItemType aUBLItem = new ItemType ();
      final TradeProductType aLineProduct = aLineItem.getSpecifiedTradeProduct ();
      if (aLineProduct != null)
      {
        final TextType aDescription = aLineProduct.getDescription ();
        if (aDescription != null)
          aUBLItem.addDescription (_copyName (aDescription, new DescriptionType ()));

        if (aLineProduct.hasNameEntries ())
          aUBLItem.setName (_copyName (aLineProduct.getNameAtIndex (0), new NameType ()));

        final IDType aBuyerAssignedID = aLineProduct.getBuyerAssignedID ();
        if (aBuyerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aBuyerAssignedID));
          aUBLItem.setBuyersItemIdentification (aUBLID);
        }

        final IDType aSellerAssignedID = aLineProduct.getSellerAssignedID ();
        if (aSellerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aSellerAssignedID));
          aUBLItem.setSellersItemIdentification (aUBLID);
        }

        final IDType aGlobalID = aLineProduct.getGlobalID ();
        if (aGlobalID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aGlobalID));
          aUBLItem.setStandardItemIdentification (aUBLID);
        }

        final TradeCountryType aOriginCountry = aLineProduct.getOriginTradeCountry ();
        if (aOriginCountry != null)
        {
          final CountryType aUBLCountry = new CountryType ();
          aUBLCountry.setIdentificationCode (aOriginCountry.getIDValue ());
          if (aOriginCountry.hasNameEntries ())
            aUBLCountry.setName (_copyName (aOriginCountry.getNameAtIndex (0), new NameType ()));
          aUBLItem.setOriginCountry (aUBLCountry);
        }

        // Commodity Classification
        for (final ProductClassificationType aLineProductClassification : aLineProduct.getDesignatedProductClassification ())
        {
          final CodeType aClassCode = aLineProductClassification.getClassCode ();
          if (aClassCode != null)
          {
            final CommodityClassificationType aUBLCommodityClassification = new CommodityClassificationType ();
            aUBLCommodityClassification.setItemClassificationCode (_copyCode (aClassCode,
                                                                              new ItemClassificationCodeType ()));
            aUBLItem.addCommodityClassification (aUBLCommodityClassification);
          }
        }
      }

      for (final TradeTaxType aTradeTax : aLineSettlement.getApplicableTradeTax ())
      {
        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (aTradeTax.getRateApplicablePercentValue ());
        final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
        aUBLTaxScheme.setID ("VAT");
        aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
        aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);
      }

      if (aLineProduct != null)
      {
        for (final ProductCharacteristicType aAPC : aLineProduct.getApplicableProductCharacteristic ())
          if (aAPC.hasDescriptionEntries ())
          {
            final ItemPropertyType aUBLAdditionalItem = new ItemPropertyType ();
            aUBLAdditionalItem.setName (_copyName (aAPC.getDescriptionAtIndex (0), new NameType ()));
            if (aAPC.hasValueEntries ())
              aUBLAdditionalItem.setValue (aAPC.getValueAtIndex (0).getValue ());
            aUBLItem.addAdditionalItemProperty (aUBLAdditionalItem);
          }
      }

      final PriceType aUBLPrice = new PriceType ();
      boolean bUsePrice = false;
      if (aLineAgreement != null)
      {

        final TradePriceType aNPPTP = aLineAgreement.getNetPriceProductTradePrice ();
        if (aNPPTP != null)
        {
          if (aNPPTP.hasChargeAmountEntries ())
          {
            aUBLPrice.setPriceAmount (_copyAmount (aNPPTP.getChargeAmountAtIndex (0),
                                                   new PriceAmountType (),
                                                   sDefaultCurrencyCode));
            bUsePrice = true;
          }
        }

        final TradePriceType aGPPTP = aLineAgreement.getGrossPriceProductTradePrice ();
        if (aGPPTP != null)
        {
          if (aGPPTP.getBasisQuantity () != null)
          {
            aUBLPrice.setBaseQuantity (_copyQuantity (aGPPTP.getBasisQuantity (), new BaseQuantityType ()));
            bUsePrice = true;
          }
        }
      }

      // Allowance charge
      final TradePriceType aTradePrice = aLineAgreement.getNetPriceProductTradePrice ();
      if (aTradePrice != null)
        for (final TradeAllowanceChargeType aPriceAllowanceCharge : aTradePrice.getAppliedTradeAllowanceCharge ())
        {
          ETriState eIsCharge = ETriState.UNDEFINED;
          if (aPriceAllowanceCharge.getChargeIndicator () != null)
            eIsCharge = _parseIndicator (aPriceAllowanceCharge.getChargeIndicator (), aErrorList);
          else
            aErrorList.add (_buildError (new String [] { "CrossIndustryInvoice",
                                                         "SupplyChainTradeTransaction",
                                                         "IncludedSupplyChainTradeLineItem",
                                                         "SpecifiedLineTradeAgreement",
                                                         "NetPriceProductTradePrice",
                                                         "AppliedTradeAllowanceCharge" },
                                         "Failed to determine if AppliedTradeAllowanceCharge is an Allowance or a Charge"));
          if (eIsCharge.isDefined ())
          {
            final AllowanceChargeType aUBLLineAllowanceCharge = new AllowanceChargeType ();
            aUBLLineAllowanceCharge.setChargeIndicator (eIsCharge.getAsBooleanValue ());
            _copyAllowanceCharge (aPriceAllowanceCharge, aUBLLineAllowanceCharge, sDefaultCurrencyCode);
            aUBLPrice.addAllowanceCharge (aUBLLineAllowanceCharge);
          }
        }

      if (bUsePrice)
        aUBLInvoiceLine.setPrice (aUBLPrice);

      aUBLInvoiceLine.setItem (aUBLItem);

      aUBLInvoice.addInvoiceLine (aUBLInvoiceLine);
    }

    // TODO
    return aUBLInvoice;
  }

  /**
   * Convert CII to UBL
   *
   * @param aFile
   *        Source file with CII to be parsed. May not be <code>null</code>.
   * @param aErrorList
   *        Error list to be filled. May not be <code>null</code>.
   * @return The parsed {@link InvoiceType} or {@link CreditNoteType}. May be
   *         <code>null</code> in case of error.
   */
  @Nullable
  public Serializable convertCIItoUBL (@Nonnull final File aFile, @Nonnull final ErrorList aErrorList)
  {
    final CrossIndustryInvoiceType aCIIInvoice = CIID16BReader.crossIndustryInvoice ()
                                                              .setValidationEventHandler (new WrappedCollectingValidationEventHandler (aErrorList))
                                                              .read (aFile);
    if (aCIIInvoice == null)
      return null;

    final TradeSettlementHeaderMonetarySummationType aTotal = aCIIInvoice.getSupplyChainTradeTransaction ()
                                                                         .getApplicableHeaderTradeSettlement ()
                                                                         .getSpecifiedTradeSettlementHeaderMonetarySummation ();
    final AmountType aDuePayable = aTotal == null ? null : aTotal.getDuePayableAmount ().get (0);

    if (aDuePayable == null || MathHelper.isGE0 (aDuePayable.getValue ()))
    {
      final InvoiceType aUBLInvoice = _convertToInvoice (aCIIInvoice, aErrorList);
      // TODO
      return aUBLInvoice;
    }

    LOGGER.info ("CreditNote is not yet supported");
    // Credit note
    // TODO
    return null;
  }
}
