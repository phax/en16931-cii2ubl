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

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.function.Consumer;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.base.equals.EqualsHelper;
import com.helger.base.numeric.BigHelper;
import com.helger.base.state.ETriState;
import com.helger.base.string.StringHelper;
import com.helger.collection.CollectionFind;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.diagnostics.error.list.IErrorList;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_22.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.*;
import oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.*;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.BinaryObjectType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.CodeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.QuantityType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

/**
 * CII to UBL 2.2 converter.
 *
 * @author Philip Helger
 */
public class CIIToUBL22Converter extends AbstractCIIToUBLConverter <CIIToUBL22Converter>
{
  private static final String UBL_VERSION = "2.2";

  public CIIToUBL22Converter ()
  {}

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.@Nullable IDType _copyID (@Nullable final IDType aCIIID)
  {
    return copyID (aCIIID, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.IDType ());
  }

  // BG-1: BT-21 Invoice note subject code + BT-22 Invoice note
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.@Nullable NoteType _copyNote (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.@Nullable NoteType aNote)
  {
    if (aNote == null)
      return null;

    final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.NoteType aUBLNote = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.NoteType ();
    final StringBuilder aSB = new StringBuilder ();

    if (StringHelper.isNotEmpty (aNote.getSubjectCodeValue ()))
      aSB.append ('#').append (aNote.getSubjectCodeValue ()).append ('#');

    boolean bFirst = true;
    for (final TextType aText : aNote.getContent ())
    {
      if (aSB.length () > 0 && !bFirst)
        aSB.append ('\n');
      aSB.append (aText.getValue ());
      bFirst = false;
    }
    aUBLNote.setValue (aSB.toString ());
    return aUBLNote;
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.@Nullable NoteType _copyNote (@Nullable final TextType aText)
  {
    return copyName (aText, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.NoteType ());
  }

  // BG-3/BG-24: Document reference conversion
  // Used for BT-25/BT-26 (preceding invoice), BT-122/BT-123/BT-124/BT-125/BT-125-1/BT-125-2 (additional supporting docs),
  // BT-15 (receiving advice), BT-16 (despatch advice), BT-12 (contract), BT-17 (tender/lot), BT-18/BT-18-1 (invoiced object),
  // BT-128/BT-128-1 (line object)
  @Nullable
  private static DocumentReferenceType _convertDocumentReference (@Nullable final ReferencedDocumentType aRD,
                                                                  @NonNull final IErrorList aErrorList)
  {
    if (aRD == null)
      return null;

    final String sID = aRD.getIssuerAssignedIDValue ();
    if (StringHelper.isEmpty (sID))
      return null;

    final DocumentReferenceType ret = new DocumentReferenceType ();
    // BT-122/BT-25/BT-18 ID value is a mandatory field
    // BT-18-1/BT-128-1 scheme ID
    ret.setID (sID).setSchemeID (aRD.getReferenceTypeCodeValue ());

    // Add DocumentTypeCode where possible
    if (isValidDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
      ret.setDocumentTypeCode (aRD.getTypeCodeValue ());

    // BT-26 Preceding Invoice issue date is optional
    final FormattedDateTimeType aFDT = aRD.getFormattedIssueDateTime ();
    if (aFDT != null)
      ret.setIssueDate (parseDate (aFDT.getDateTimeString (), aErrorList));

    // BT-123 Supporting document description is optional
    for (final TextType aItem : aRD.getName ())
    {
      final DocumentDescriptionType aUBLDocDesc = new DocumentDescriptionType ();
      aUBLDocDesc.setValue (aItem.getValue ());
      aUBLDocDesc.setLanguageID (aItem.getLanguageID ());
      aUBLDocDesc.setLanguageLocaleID (aItem.getLanguageLocaleID ());
      ret.addDocumentDescription (aUBLDocDesc);
    }

    // BT-125 Attached document (0..1 for CII)
    if (aRD.getAttachmentBinaryObjectCount () > 0)
    {
      final BinaryObjectType aBinObj = aRD.getAttachmentBinaryObjectAtIndex (0);

      final AttachmentType aUBLAttachment = new AttachmentType ();
      final EmbeddedDocumentBinaryObjectType aEmbeddedDoc = new EmbeddedDocumentBinaryObjectType ();
      // BT-125-1 Attached document Mime code
      aEmbeddedDoc.setMimeCode (aBinObj.getMimeCode ());
      // BT-125-2 Attached document Filename
      aEmbeddedDoc.setFilename (aBinObj.getFilename ());
      aEmbeddedDoc.setValue (aBinObj.getValue ());
      aUBLAttachment.setEmbeddedDocumentBinaryObject (aEmbeddedDoc);

      ret.setAttachment (aUBLAttachment);
    }

    // BT-124 External document location
    final String sURI = aRD.getURIIDValue ();
    if (StringHelper.isNotEmpty (sURI))
    {
      AttachmentType aUBLAttachment = ret.getAttachment ();
      if (aUBLAttachment == null)
      {
        aUBLAttachment = new AttachmentType ();
        ret.setAttachment (aUBLAttachment);
      }

      final ExternalReferenceType aUBLExtRef = new ExternalReferenceType ();
      aUBLExtRef.setURI (sURI);
      aUBLAttachment.setExternalReference (aUBLExtRef);
    }

    return ret;
  }

  // Converts BG-5/BG-8/BG-12/BG-15 postal address
  @NonNull
  private static AddressType _convertPostalAddress (@NonNull final TradeAddressType aPostalAddress)
  {
    final AddressType ret = new AddressType ();
    // BT-35/BT-50/BT-64/BT-75 Address line 1
    if (StringHelper.isNotEmpty (aPostalAddress.getLineOneValue ()))
      ret.setStreetName (aPostalAddress.getLineOneValue ());
    // BT-36/BT-51/BT-65/BT-76 Address line 2
    if (StringHelper.isNotEmpty (aPostalAddress.getLineTwoValue ()))
      ret.setAdditionalStreetName (aPostalAddress.getLineTwoValue ());
    // BT-162/BT-163/BT-164/BT-165 Address line 3
    if (StringHelper.isNotEmpty (aPostalAddress.getLineThreeValue ()))
    {
      final AddressLineType aUBLAddressLine = new AddressLineType ();
      aUBLAddressLine.setLine (aPostalAddress.getLineThreeValue ());
      ret.addAddressLine (aUBLAddressLine);
    }
    // BT-37/BT-52/BT-66/BT-77 City
    if (StringHelper.isNotEmpty (aPostalAddress.getCityNameValue ()))
      ret.setCityName (aPostalAddress.getCityNameValue ());
    // BT-38/BT-53/BT-67/BT-78 Post code
    if (StringHelper.isNotEmpty (aPostalAddress.getPostcodeCodeValue ()))
      ret.setPostalZone (aPostalAddress.getPostcodeCodeValue ());
    // BT-39/BT-54/BT-68/BT-79 Country subdivision
    if (aPostalAddress.hasCountrySubDivisionNameEntries ())
      ret.setCountrySubentity (aPostalAddress.getCountrySubDivisionNameAtIndex (0).getValue ());
    // BT-40/BT-55/BT-69/BT-80 Country code
    if (StringHelper.isNotEmpty (aPostalAddress.getCountryIDValue ()))
    {
      final CountryType aUBLCountry = new CountryType ();
      aUBLCountry.setIdentificationCode (aPostalAddress.getCountryIDValue ());
      ret.setCountry (aUBLCountry);
    }
    return ret;
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.@Nullable IDType _extractFirstPartyID (@NonNull final TradePartyType aParty)
  {
    final IDType aID;
    if (canUseGlobalID (aParty))
    {
      // Use the first matching one
      aID = getAllUsableGlobalIDs (aParty).getFirstOrNull ();
    }
    else
      if (aParty.hasIDEntries ())
        aID = aParty.getIDAtIndex (0);
      else
        aID = null;

    return aID == null ? null : _copyID (aID);
  }

  private static void _extractAllPartyIDs (@NonNull final TradePartyType aParty,
                                           @NonNull final Consumer <? super oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.IDType> aIDConsumer)
  {
    if (canUseGlobalID (aParty))
      getAllUsableGlobalIDs (aParty).forEach (x -> aIDConsumer.accept (_copyID (x)));
    else
      for (final IDType aID : aParty.getID ())
        aIDConsumer.accept (_copyID (aID));
  }

  private static void _addPartyID (final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.@Nullable IDType aUBLID,
                                   @NonNull final PartyType aParty)
  {
    if (aUBLID != null)
    {
      // Avoid duplicate IDs
      if (!CollectionFind.containsAny (aParty.getPartyIdentification (), x -> EqualsHelper.equals (aUBLID, x.getID ())))
      {
        final PartyIdentificationType aUBLPartyIdentification = new PartyIdentificationType ();
        aUBLPartyIdentification.setID (aUBLID);
        aParty.addPartyIdentification (aUBLPartyIdentification);
      }
    }
  }

  @NonNull
  private static PartyType _convertParty (@NonNull final TradePartyType aParty,
                                          final boolean bMultiID,
                                          final boolean bUseLegalEntityName)
  {
    final PartyType ret = new PartyType ();

    // BT-34/BT-34-1/BT-49/BT-49-1 Electronic address
    if (aParty.hasURIUniversalCommunicationEntries ())
    {
      final UniversalCommunicationType UC = aParty.getURIUniversalCommunicationAtIndex (0);
      ret.setEndpointID (copyID (UC.getURIID (), new EndpointIDType ()));
    }

    // BT-29/BT-29-1/BT-46/BT-46-1/BT-60/BT-60-1/BT-71/BT-71-1 Party identifier
    if (bMultiID)
      _extractAllPartyIDs (aParty, x -> _addPartyID (x, ret));
    else
      _addPartyID (_extractFirstPartyID (aParty), ret);

    // BT-27/BT-44/BT-59/BT-62/BT-70 Party name
    final TextType aName = aParty.getName ();
    if (aName != null && StringHelper.isNotEmpty (aName.getValue ()))
    {
      // Some map to PartyLegalEntity some to PartyName
      if (bUseLegalEntityName)
      {
        final PartyLegalEntityType aUBLPartyLegalEntity = new PartyLegalEntityType ();
        aUBLPartyLegalEntity.setRegistrationName (aName.getValue ());
        ret.addPartyLegalEntity (aUBLPartyLegalEntity);
      }
      else
      {
        final PartyNameType aUBLPartyName = new PartyNameType ();
        aUBLPartyName.setName (copyName (aName, new NameType ()));
        if (aUBLPartyName.getName () != null)
          ret.addPartyName (aUBLPartyName);
      }
    }

    final TradeAddressType aPostalAddress = aParty.getPostalTradeAddress ();
    if (aPostalAddress != null)
    {
      ret.setPostalAddress (_convertPostalAddress (aPostalAddress));
    }

    return ret;
  }

  // BT-31/BT-32/BT-48/BT-63 Tax registration
  @NonNull
  private PartyTaxSchemeType _convertPartyTaxScheme (@NonNull final TaxRegistrationType aTaxRegistration)
  {
    if (aTaxRegistration.getID () == null)
      return null;

    final PartyTaxSchemeType aUBLPartyTaxScheme = new PartyTaxSchemeType ();
    aUBLPartyTaxScheme.setCompanyID (aTaxRegistration.getIDValue ());

    String sSchemeID = aTaxRegistration.getID ().getSchemeID ();
    if (StringHelper.isEmpty (sSchemeID))
      sSchemeID = getVATScheme ();
    else
    {
      // Special case CII validation artefacts 1.0.0 and 1.2.0
      if ("VA".equals (sSchemeID))
        sSchemeID = getVATScheme ();
    }

    final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
    aUBLTaxScheme.setID (sSchemeID);
    aUBLPartyTaxScheme.setTaxScheme (aUBLTaxScheme);
    return aUBLPartyTaxScheme;
  }

  // BT-28/BT-30/BT-30-1/BT-33/BT-45/BT-47/BT-47-1/BT-61/BT-61-1 Party legal entity
  private static void _convertPartyLegalEntity (@NonNull final TradePartyType aTradeParty,
                                                @NonNull final PartyType aUBLParty)
  {
    final PartyLegalEntityType aUBLPartyLegalEntity;
    final boolean bExistingLegalEntity;
    if (aUBLParty.hasPartyLegalEntityEntries ())
    {
      aUBLPartyLegalEntity = aUBLParty.getPartyLegalEntityAtIndex (0);
      bExistingLegalEntity = true;
    }
    else
    {
      aUBLPartyLegalEntity = new PartyLegalEntityType ();
      bExistingLegalEntity = false;
    }

    final LegalOrganizationType aSLO = aTradeParty.getSpecifiedLegalOrganization ();
    if (aSLO != null)
    {
      // BT-28/BT-45 Trading name
      if (StringHelper.isNotEmpty (aSLO.getTradingBusinessNameValue ()))
      {
        final PartyNameType aUBLPartyName = new PartyNameType ();
        aUBLPartyName.setName (aSLO.getTradingBusinessNameValue ());
        if (aUBLPartyName.getName () != null)
          aUBLParty.addPartyName (aUBLPartyName);
      }

      // BT-30/BT-30-1/BT-47/BT-47-1/BT-61/BT-61-1 Legal registration identifier
      aUBLPartyLegalEntity.setCompanyID (copyID (aSLO.getID (), new CompanyIDType ()));
    }

    // BT-33 Seller additional legal information
    for (final TextType aDesc : aTradeParty.getDescription ())
      if (StringHelper.isNotEmpty (aDesc.getValue ()))
      {
        // Use the first only
        aUBLPartyLegalEntity.setCompanyLegalForm (aDesc.getValue ());
        break;
      }

    if (aUBLPartyLegalEntity.getRegistrationName () == null && !aUBLParty.hasPartyNameEntries () && StringHelper.isNotEmpty (aTradeParty.getNameValue ()))
    {
      // Mandatory field according to Schematron (for Seller/Buyer only)
      // UBL-CR-275 forbids RegistrationName on PayeeParty
      aUBLPartyLegalEntity.setRegistrationName (aTradeParty.getNameValue ());
    }

    // Only add if it has content — avoid empty PartyLegalEntity on Payee
    if (!bExistingLegalEntity)
    {
      if (aUBLPartyLegalEntity.getRegistrationName () != null ||
          aUBLPartyLegalEntity.getCompanyID () != null ||
          aUBLPartyLegalEntity.getCompanyLegalForm () != null)
        aUBLParty.addPartyLegalEntity (aUBLPartyLegalEntity);
    }
  }

  // BG-6/BG-9 Contact
  @Nullable
  private static ContactType _convertContact (@NonNull final TradePartyType aTradeParty)
  {
    if (!aTradeParty.hasDefinedTradeContactEntries ())
      return null;

    final TradeContactType aDTC = aTradeParty.getDefinedTradeContactAtIndex (0);
    final ContactType aUBLContact = new ContactType ();

    // BT-41/BT-56 Contact point
    aUBLContact.setName (copyName (aDTC.getPersonName (), new NameType ()));
    if (aUBLContact.getNameValue () == null)
    {
      // BT-41/BT-56 fallback to department name
      aUBLContact.setName (copyName (aDTC.getDepartmentName (), new NameType ()));
    }

    // BT-42/BT-57 Contact telephone number
    final UniversalCommunicationType aTel = aDTC.getTelephoneUniversalCommunication ();
    if (aTel != null)
      ifNotEmpty (aTel.getCompleteNumberValue (), aUBLContact::setTelephone);

    // BT-43/BT-58 Contact email address
    final UniversalCommunicationType aEmail = aDTC.getEmailURIUniversalCommunication ();
    if (aEmail != null)
      ifNotEmpty (aEmail.getURIIDValue (), aUBLContact::setElectronicMail);

    if (aUBLContact.getName () == null &&
        aUBLContact.getTelephone () == null &&
        aUBLContact.getElectronicMail () == null)
      return null;
    return aUBLContact;
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.@Nullable AmountType _copyAmount (@Nullable final AmountType aAmount,
                                                                                                                     @Nullable final String sDefaultCurrencyCode)
  {
    return copyAmount (aAmount,
                       new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.AmountType (),
                       sDefaultCurrencyCode);
  }

  // BG-20/BG-21/BG-27/BG-28 Allowance/Charge details
  private void _copyAllowanceCharge (@NonNull final TradeAllowanceChargeType aAllowanceCharge,
                                     @NonNull final AllowanceChargeType aUBLAllowanceCharge,
                                     @Nullable final String sDefaultCurrencyCode)
  {
    // BT-98/BT-105/BT-140/BT-145 Reason code
    if (StringHelper.isNotEmpty (aAllowanceCharge.getReasonCodeValue ()))
      aUBLAllowanceCharge.setAllowanceChargeReasonCode (aAllowanceCharge.getReasonCodeValue ());

    // BT-97/BT-104/BT-139/BT-144 Reason
    if (aAllowanceCharge.getReason () != null)
    {
      final AllowanceChargeReasonType aUBLReason = new AllowanceChargeReasonType ();
      aUBLReason.setValue (aAllowanceCharge.getReasonValue ());
      aUBLAllowanceCharge.addAllowanceChargeReason (aUBLReason);
    }
    // BT-94/BT-101/BT-138/BT-143 Percentage
    if (aAllowanceCharge.getCalculationPercent () != null)
    {
      // UBL requires values between 0 and 100
      aUBLAllowanceCharge.setMultiplierFactorNumeric (aAllowanceCharge.getCalculationPercentValue ());
    }
    // BT-92/BT-99/BT-136/BT-141 Amount
    if (aAllowanceCharge.hasActualAmountEntries ())
    {
      aUBLAllowanceCharge.setAmount (_copyAmount (aAllowanceCharge.getActualAmountAtIndex (0), sDefaultCurrencyCode));
    }

    // BT-93/BT-100/BT-137/BT-142 Base amount
    aUBLAllowanceCharge.setBaseAmount (copyAmount (aAllowanceCharge.getBasisAmount (),
                                                   new BaseAmountType (),
                                                   sDefaultCurrencyCode));

    // BT-95/BT-102 VAT category code and BT-96/BT-103 VAT rate
    // (not applicable for line-level BG-27/BG-28)
    for (final TradeTaxType aTradeTax : aAllowanceCharge.getCategoryTradeTax ())
    {
      final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
      aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
      if (aTradeTax.getRateApplicablePercentValue () != null)
        aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
      final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
      aUBLTaxScheme.setID (getVATScheme ());
      aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
      aUBLAllowanceCharge.addTaxCategory (aUBLTaxCategory);
    }
  }

  private void _convertPaymentMeans (@NonNull final HeaderTradeSettlementType aHeaderSettlement,
                                     @NonNull final TradeSettlementPaymentMeansType aPaymentMeans,
                                     @NonNull final Consumer <oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.IDType> aSellerIDHandler,
                                     @NonNull final Consumer <PaymentMeansType> aPaymentMeansHandler,
                                     @NonNull final ErrorList aErrorList)
  {
    final String sTypeCode = aPaymentMeans.getTypeCodeValue ();

    final PaymentMeansType aUBLPaymentMeans = new PaymentMeansType ();
    final PaymentMeansCodeType aUBLPaymentMeansCode = new PaymentMeansCodeType ();

    // BG-16 PAYMENT INSTRUCTIONS
    // BT-81 TypeCode is mandatory
    aUBLPaymentMeansCode.setValue (sTypeCode);

    // BT-82
    if (aPaymentMeans.hasInformationEntries ())
      aUBLPaymentMeansCode.setName (aPaymentMeans.getInformationAtIndex (0).getValue ());

    // BT-83
    aUBLPaymentMeans.setPaymentMeansCode (aUBLPaymentMeansCode);

    for (final TextType aPaymentRef : aHeaderSettlement.getPaymentReference ())
    {
      final PaymentIDType aUBLPaymentID = new PaymentIDType ();
      aUBLPaymentID.setValue (aPaymentRef.getValue ());
      aUBLPaymentMeans.addPaymentID (aUBLPaymentID);
    }

    // BG-17 CREDIT TRANSFER
    final CreditorFinancialAccountType aPayeeCreditorAccount = aPaymentMeans.getPayeePartyCreditorFinancialAccount ();
    final boolean bIsBG17 = isPaymentMeansCodeCreditTransfer (sTypeCode) && aPayeeCreditorAccount != null;
    if (bIsBG17)
    {
      final FinancialAccountType aUBLFinancialAccount = new FinancialAccountType ();

      // BT-84 mandatory
      // ID/@scheme ID must be empty for the EN16931 Schematrons
      aUBLFinancialAccount.setID (_copyID (aPayeeCreditorAccount.getIBANID ()));
      if (aUBLFinancialAccount.getID () == null)
        aUBLFinancialAccount.setID (_copyID (aPayeeCreditorAccount.getProprietaryID ()));

      // BT-85
      aUBLFinancialAccount.setName (copyName (aPayeeCreditorAccount.getAccountName (), new NameType ()));

      // BT-86
      final CreditorFinancialInstitutionType aInstitution = aPaymentMeans.getPayeeSpecifiedCreditorFinancialInstitution ();
      if (aInstitution != null)
      {
        final BranchType aUBLBranch = new BranchType ();
        aUBLBranch.setID (_copyID (aInstitution.getBICID ()));
        if (aUBLBranch.getID () != null)
          aUBLFinancialAccount.setFinancialInstitutionBranch (aUBLBranch);
      }

      aUBLPaymentMeans.setPayeeFinancialAccount (aUBLFinancialAccount);
    }

    // BG-18 PAYMENT CARD INFORMATION
    final boolean bIsBG18 = isPaymentMeansCodePaymentCard (sTypeCode);
    if (bIsBG18)
    {
      final TradeSettlementFinancialCardType aCard = aPaymentMeans.getApplicableTradeSettlementFinancialCard ();
      if (aCard == null)
        aErrorList.add (buildError (null,
                                    "The element 'ApplicableTradeSettlementFinancialCard' is missing for Payment Card Information"));
      else
      {
        final CardAccountType aUBLCardAccount = new CardAccountType ();

        // BT-87 mandatory
        aUBLCardAccount.setPrimaryAccountNumberID (copyID (aCard.getID (), new PrimaryAccountNumberIDType ()));

        // No CII field present
        if (StringHelper.isNotEmpty (getCardAccountNetworkID ()))
          aUBLCardAccount.setNetworkID (getCardAccountNetworkID ());

        // BT-88
        if (StringHelper.isNotEmpty (aCard.getCardholderNameValue ()))
          aUBLCardAccount.setHolderName (aCard.getCardholderNameValue ());

        if (StringHelper.isEmpty (aUBLCardAccount.getPrimaryAccountNumberIDValue ()))
          aErrorList.add (buildError (null, "The Payment card primary account number is missing"));
        else
          if (StringHelper.isEmpty (aUBLCardAccount.getNetworkIDValue ()))
            aErrorList.add (buildError (null, "The Payment card network ID is missing"));
          else
            aUBLPaymentMeans.setCardAccount (aUBLCardAccount);
      }
    }

    // BG-19 DIRECT DEBIT
    final boolean bIsBG19 = isPaymentMeansCodeDirectDebit (sTypeCode);
    if (bIsBG19)
    {
      final PaymentMandateType aUBLPaymentMandate = new PaymentMandateType ();

      // BT-89
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
        if (aPaymentTerms.hasDirectDebitMandateIDEntries ())
        {
          aUBLPaymentMandate.setID (_copyID (aPaymentTerms.getDirectDebitMandateIDAtIndex (0)));
          if (aUBLPaymentMandate.getID () != null)
            break;
        }

      // BT-90 Bank assigned creditor identifier
      // Only mapped for BG-19 direct debit — EN 16931 defines BT-90 as part of BG-19.
      final IDType aCreditorRefID = aHeaderSettlement.getCreditorReferenceID ();
      if (aCreditorRefID != null)
      {
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.IDType aSellerID = _copyID (aCreditorRefID);
        if (aSellerID != null)
        {
          aSellerID.setSchemeID ("SEPA");
          aSellerIDHandler.accept (aSellerID);
        }
      }

      final FinancialAccountType aUBLFinancialAccount = new FinancialAccountType ();

      // BT-91
      final DebtorFinancialAccountType aAccount = aPaymentMeans.getPayerPartyDebtorFinancialAccount ();
      if (aAccount != null)
      {
        aUBLFinancialAccount.setID (_copyID (aAccount.getIBANID ()));
        // Name is not mapped
        if (false)
          aUBLFinancialAccount.setName (copyName (aAccount.getAccountName (), new NameType ()));
      }

      // BT-86
      final DebtorFinancialInstitutionType aInstitution = aPaymentMeans.getPayerSpecifiedDebtorFinancialInstitution ();
      if (aInstitution != null)
      {
        final BranchType aUBLBranch = new BranchType ();
        aUBLBranch.setID (_copyID (aInstitution.getBICID ()));
        if (aUBLBranch.getID () != null)
          aUBLFinancialAccount.setFinancialInstitutionBranch (aUBLBranch);
      }

      if (aUBLFinancialAccount.getID () != null || aUBLFinancialAccount.getFinancialInstitutionBranch () != null)
        aUBLPaymentMandate.setPayerFinancialAccount (aUBLFinancialAccount);

      aUBLPaymentMeans.setPaymentMandate (aUBLPaymentMandate);
    }

    if (bIsBG17 || bIsBG18 || bIsBG19 || isPaymentMeansCodeOtherKnown (sTypeCode))
      aPaymentMeansHandler.accept (aUBLPaymentMeans);
    else
      aErrorList.add (buildError (null,
                                  "Failed to determine a supported Payment Means Type from code '" + sTypeCode + "'"));
  }

  @Nullable
  private OrderReferenceType _createUBLOrderRef (@Nullable final ReferencedDocumentType aBuyerOrderRef,
                                                 @Nullable final ReferencedDocumentType aSellerOrderRef)
  {
    final OrderReferenceType aUBLOrderRef = new OrderReferenceType ();
    if (aBuyerOrderRef != null)
      aUBLOrderRef.setID (aBuyerOrderRef.getIssuerAssignedIDValue ());
    
    if (aSellerOrderRef != null)
    {
      if (aUBLOrderRef.getIDValue () == null)
      {
        // Mandatory element
        aUBLOrderRef.setID (getDefaultOrderRefID ());
      }
      ifNotEmpty (aSellerOrderRef.getIssuerAssignedIDValue (), aUBLOrderRef::setSalesOrderID);
    }

    // Ignore defacto empty elements
    if (StringHelper.isEmpty (aUBLOrderRef.getIDValue ()) &&
        StringHelper.isEmpty (aUBLOrderRef.getSalesOrderIDValue ()))
      return null;

    return aUBLOrderRef;
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
        aUBLInvoice.setProfileID (aEDC.getBusinessProcessSpecifiedDocumentContextParameterAtIndex (0).getIDValue ());
      }
      if (aEDC.hasGuidelineSpecifiedDocumentContextParameterEntries ())
      {
        // BT-24
        aUBLInvoice.setCustomizationID (aEDC.getGuidelineSpecifiedDocumentContextParameterAtIndex (0).getIDValue ());
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

    // Mandatory supplier
    final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
    aUBLInvoice.setAccountingSupplierParty (aUBLSupplier);

    // Mandatory customer
    final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
    aUBLInvoice.setAccountingCustomerParty (aUBLCustomer);

    // BT-2 Invoice issue date
    {
      LocalDate aIssueDate = null;
      if (aED != null && aED.getIssueDateTime () != null)
        aIssueDate = parseDate (aED.getIssueDateTime ().getDateTimeString (), aErrorList);

      if (aIssueDate != null)
        aUBLInvoice.setIssueDate (aIssueDate);
    }

    // BT-9 Payment due date
    {
      LocalDate aDueDate = null;
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
        if (aPaymentTerms.getDueDateDateTime () != null)
        {
          aDueDate = parseDate (aPaymentTerms.getDueDateDateTime ().getDateTimeString (), aErrorList);
          if (aDueDate != null)
            break;
        }
      if (aDueDate != null)
        aUBLInvoice.setDueDate (aDueDate);
    }

    // BT-3 Invoice type code
    if (aED != null)
      aUBLInvoice.setInvoiceTypeCode (aED.getTypeCodeValue ());

    // BG-1 INVOICE NOTE (BT-21/BT-22)
    if (aED != null)
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aEDNote : aED.getIncludedNote ())
        ifNotNull (_copyNote (aEDNote), aUBLInvoice::addNote);

    // BT-7 Value added tax point date
    for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
    {
      if (aTradeTax.getTaxPointDate () != null)
      {
        final LocalDate aTaxPointDate = parseDate (aTradeTax.getTaxPointDate ().getDateString (), aErrorList);
        if (aTaxPointDate != null)
        {
          // Use the first tax point date only
          aUBLInvoice.setTaxPointDate (aTaxPointDate);
          break;
        }
      }
    }

    // BT-5 Invoice currency code
    final String sDefaultCurrencyCode = aHeaderSettlement.getInvoiceCurrencyCodeValue ();
    aUBLInvoice.setDocumentCurrencyCode (sDefaultCurrencyCode);

    // BT-6 VAT accounting currency code
    if (aHeaderSettlement.getTaxCurrencyCodeValue () != null)
    {
      aUBLInvoice.setTaxCurrencyCode (aHeaderSettlement.getTaxCurrencyCodeValue ());
    }

    // BT-19 Buyer accounting reference
    for (final TradeAccountingAccountType aAccount : aHeaderSettlement.getReceivableSpecifiedTradeAccountingAccount ())
    {
      final String sID = aAccount.getIDValue ();
      if (StringHelper.isNotEmpty (sID))
      {
        // Use the first ID
        aUBLInvoice.setAccountingCost (sID);
        break;
      }
    }

    // BT-10 Buyer reference
    if (aHeaderAgreement.getBuyerReferenceValue () != null)
    {
      aUBLInvoice.setBuyerReference (aHeaderAgreement.getBuyerReferenceValue ());
    }

    // BG-14 INVOICING PERIOD (BT-73/BT-74)
    {
      final PeriodType aUBLPeriod = new PeriodType ();
      final SpecifiedPeriodType aSPT = aHeaderSettlement.getBillingSpecifiedPeriod ();
      if (aSPT != null)
      {
        final DateTimeType aStartDT = aSPT.getStartDateTime ();
        if (aStartDT != null)
          aUBLPeriod.setStartDate (parseDate (aStartDT.getDateTimeString (), aErrorList));

        final DateTimeType aEndDT = aSPT.getEndDateTime ();
        if (aEndDT != null)
          aUBLPeriod.setEndDate (parseDate (aEndDT.getDateTimeString (), aErrorList));
      }

      // BT-8 Value added tax point date code
      if (aHeaderSettlement.hasApplicableTradeTaxEntries ())
      {
        final TradeTaxType aTradeTax = aHeaderSettlement.getApplicableTradeTaxAtIndex (0);
        if (StringHelper.isNotEmpty (aTradeTax.getDueDateTypeCodeValue ()))
          aUBLPeriod.addDescriptionCode (new DescriptionCodeType (mapDueDateTypeCode (aTradeTax.getDueDateTypeCodeValue ())));
      }

      if (aUBLPeriod.getStartDate () != null ||
          aUBLPeriod.getEndDate () != null ||
          aUBLPeriod.hasDescriptionCodeEntries ())
        aUBLInvoice.addInvoicePeriod (aUBLPeriod);
    }

    // BT-13 Purchase order reference + BT-14 Sales order reference
    {
      final OrderReferenceType aUBLOrderRef = _createUBLOrderRef (aHeaderAgreement.getBuyerOrderReferencedDocument (),
                                                                  aHeaderAgreement.getSellerOrderReferencedDocument ());
      aUBLInvoice.setOrderReference (aUBLOrderRef);
    }

    // BG-3 PRECEDING INVOICE REFERENCE (BT-25/BT-26)
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

    // BT-16 Despatch advice reference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getDespatchAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLInvoice.addDespatchDocumentReference (aUBLDocRef);
    }

    // BT-15 Receiving advice reference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getReceivingAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLInvoice.addReceiptDocumentReference (aUBLDocRef);
    }

    // BT-17 Tender or lot reference (OriginatorDocumentReference)
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Use for "Tender or lot reference" with TypeCode "50" (BT-17)
        if (isOriginatorDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
          {
            // Explicitly disallowed here
            aUBLDocRef.setDocumentTypeCode ((DocumentTypeCodeType) null);
            aUBLInvoice.addOriginatorDocumentReference (aUBLDocRef);
          }
        }
      }
    }

    // BT-12 Contract reference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderAgreement.getContractReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLInvoice.addContractDocumentReference (aUBLDocRef);
    }

    // BG-24 ADDITIONAL SUPPORTING DOCUMENTS + BT-18/BT-18-1 Invoiced object identifier
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Except OriginatorDocumentReference (BT-17)
        if (!isOriginatorDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
            aUBLInvoice.addAdditionalDocumentReference (aUBLDocRef);
        }
      }
    }

    // BT-11 Project reference
    {
      final ProcuringProjectType aSpecifiedProcuring = aHeaderAgreement.getSpecifiedProcuringProject ();
      if (aSpecifiedProcuring != null)
      {
        final String sID = aSpecifiedProcuring.getIDValue ();
        if (StringHelper.isNotEmpty (sID))
        {
          final ProjectReferenceType aUBLProjectRef = new ProjectReferenceType ();
          aUBLProjectRef.setID (sID);
          aUBLInvoice.addProjectReference (aUBLProjectRef);
        }
      }
    }

    // BG-4 SELLER
    {
      final TradePartyType aSellerParty = aHeaderAgreement.getSellerTradeParty ();
      if (aSellerParty != null)
      {
        // BT-27
        final PartyType aUBLParty = _convertParty (aSellerParty, true, true);

        for (final TaxRegistrationType aTaxRegistration : aSellerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        _convertPartyLegalEntity (aSellerParty, aUBLParty);

        final ContactType aUBLContact = _convertContact (aSellerParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLSupplier.setParty (aUBLParty);
      }
    }

    // BG-7 BUYER
    {
      final TradePartyType aBuyerParty = aHeaderAgreement.getBuyerTradeParty ();
      if (aBuyerParty != null)
      {
        // BT-44 Buyer name
        final PartyType aUBLParty = _convertParty (aBuyerParty, false, true);

        // BT-48 Buyer VAT identifier
        for (final TaxRegistrationType aTaxRegistration : aBuyerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // BT-45/BT-47/BT-47-1 Buyer legal entity
        _convertPartyLegalEntity (aBuyerParty, aUBLParty);

        // BG-9 BUYER CONTACT (BT-56/BT-57/BT-58)
        final ContactType aUBLContact = _convertContact (aBuyerParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLCustomer.setParty (aUBLParty);
      }
    }

    // BG-10 PAYEE
    {
      final TradePartyType aPayeeParty = aHeaderSettlement.getPayeeTradeParty ();
      if (aPayeeParty != null)
      {
        // BT-59 Payee name
        final PartyType aUBLParty = _convertParty (aPayeeParty, false, false);

        for (final TaxRegistrationType aTaxRegistration : aPayeeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // BT-61/BT-61-1 Payee legal registration identifier
        _convertPartyLegalEntity (aPayeeParty, aUBLParty);

        final ContactType aUBLContact = _convertContact (aPayeeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLInvoice.setPayeeParty (aUBLParty);
      }
    }

    // BG-11 SELLER TAX REPRESENTATIVE PARTY
    {
      final TradePartyType aTaxRepresentativeParty = aHeaderAgreement.getSellerTaxRepresentativeTradeParty ();
      if (aTaxRepresentativeParty != null)
      {
        // BT-62 Seller tax representative name
        final PartyType aUBLParty = _convertParty (aTaxRepresentativeParty, false, false);

        // BT-63 Seller tax representative VAT identifier
        for (final TaxRegistrationType aTaxRegistration : aTaxRepresentativeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        final ContactType aUBLContact = _convertContact (aTaxRepresentativeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLInvoice.setTaxRepresentativeParty (aUBLParty);
      }
    }

    // BG-13 DELIVERY INFORMATION
    {
      final DeliveryType aUBLDelivery = new DeliveryType ();
      boolean bUseDelivery = false;

      // BT-72 Actual delivery date
      final SupplyChainEventType aSCE = aHeaderDelivery.getActualDeliverySupplyChainEvent ();
      if (aSCE != null)
      {
        final DateTimeType aODT = aSCE.getOccurrenceDateTime ();
        if (aODT != null)
        {
          aUBLDelivery.setActualDeliveryDate (parseDate (aODT.getDateTimeString (), aErrorList));
          bUseDelivery = true;
        }
      }

      final TradePartyType aShipToParty = aHeaderDelivery.getShipToTradeParty ();
      if (aShipToParty != null)
      {
        final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_22.LocationType aUBLDeliveryLocation = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_22.LocationType ();
        boolean bUseLocation = false;

        // BT-71/BT-71-1 Deliver to location identifier
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.IDType aUBLID = _extractFirstPartyID (aShipToParty);
        if (aUBLID != null)
        {
          aUBLDeliveryLocation.setID (aUBLID);
          bUseLocation = true;
        }

        // BG-15 DELIVER TO ADDRESS (BT-75 to BT-80, BT-165)
        final TradeAddressType aPostalAddress = aShipToParty.getPostalTradeAddress ();
        if (aPostalAddress != null)
        {
          aUBLDeliveryLocation.setAddress (_convertPostalAddress (aPostalAddress));
          bUseLocation = true;
        }

        if (bUseLocation)
        {
          aUBLDelivery.setDeliveryLocation (aUBLDeliveryLocation);
          bUseDelivery = true;
        }

        // BT-70 Deliver to party name
        final TextType aName = aShipToParty.getName ();
        if (aName != null && StringHelper.isNotEmpty (aName.getValue ()))
        {
          final PartyType aUBLDeliveryParty = new PartyType ();
          final PartyNameType aUBLPartyName = new PartyNameType ();
          aUBLPartyName.setName (copyName (aName, new NameType ()));
          aUBLDeliveryParty.addPartyName (aUBLPartyName);
          aUBLDelivery.setDeliveryParty (aUBLDeliveryParty);
          bUseDelivery = true;
        }
      }

      if (bUseDelivery)
        aUBLInvoice.addDelivery (aUBLDelivery);
    }

    // BG-16 PAYMENT INSTRUCTIONS
    {
      for (final TradeSettlementPaymentMeansType aPaymentMeans : aHeaderSettlement.getSpecifiedTradeSettlementPaymentMeans ())
      {
        _convertPaymentMeans (aHeaderSettlement,
                              aPaymentMeans,
                              // BT-90: place on Payee if present, otherwise on Seller
                              x -> {
                                if (aUBLInvoice.getPayeeParty () != null)
                                  _addPartyID (x, aUBLInvoice.getPayeeParty ());
                                else
                                  _addPartyID (x, aUBLInvoice.getAccountingSupplierParty ().getParty ());
                              },
                              aUBLInvoice::addPaymentMeans,
                              aErrorList);

        // Allowed again in 1.2.1: exactly 2
        if (false)
          // Since v1.2.0 only one is allowed
          if (true)
            break;
      }
    }

    // BT-20 Payment terms
    {
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
      {
        final PaymentTermsType aUBLPaymenTerms = new PaymentTermsType ();

        for (final TextType aDesc : aPaymentTerms.getDescription ())
          ifNotNull (_copyNote (aDesc), aUBLPaymenTerms::addNote);

        if (aUBLPaymenTerms.hasNoteEntries ())
          aUBLInvoice.addPaymentTerms (aUBLPaymenTerms);
      }
    }

    // BG-20 DOCUMENT LEVEL ALLOWANCES / BG-21 DOCUMENT LEVEL CHARGES
    {
      for (final TradeAllowanceChargeType aAllowanceCharge : aHeaderSettlement.getSpecifiedTradeAllowanceCharge ())
      {
        ETriState eIsCharge = ETriState.UNDEFINED;
        if (aAllowanceCharge.getChargeIndicator () != null)
          eIsCharge = parseIndicator (aAllowanceCharge.getChargeIndicator (), aErrorList);
        else
          aErrorList.add (buildError (new String [] { "CrossIndustryInvoice",
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

    // BG-23 VAT BREAKDOWN + BT-110/BT-111 Tax totals
    {
      TaxTotalType aUBLTaxTotal = null;
      if (aSTSHMS != null && aSTSHMS.hasTaxTotalAmountEntries ())
      {
        // BT-110 Invoice total VAT amount / BT-111 Invoice total VAT amount in accounting currency
        for (final AmountType aTaxTotalAmount : aSTSHMS.getTaxTotalAmount ())
        {
          final TaxTotalType aUBLCurTaxTotal = new TaxTotalType ();
          aUBLCurTaxTotal.setTaxAmount (copyAmount (aTaxTotalAmount, new TaxAmountType (), sDefaultCurrencyCode));
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

      // BG-23 VAT BREAKDOWN subtotals
      for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();

        // BT-116 VAT category taxable amount
        if (aTradeTax.hasBasisAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxableAmount (copyAmount (aTradeTax.getBasisAmountAtIndex (0),
                                                        new TaxableAmountType (),
                                                        sDefaultCurrencyCode));
        }

        // BT-117 VAT category tax amount
        if (aTradeTax.hasCalculatedAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxAmount (copyAmount (aTradeTax.getCalculatedAmountAtIndex (0),
                                                    new TaxAmountType (),
                                                    sDefaultCurrencyCode));
        }

        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        // BT-118 VAT category code
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        // BT-119 VAT category rate
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
        // BT-121 VAT exemption reason code
        if (StringHelper.isNotEmpty (aTradeTax.getExemptionReasonCodeValue ()))
          aUBLTaxCategory.setTaxExemptionReasonCode (aTradeTax.getExemptionReasonCodeValue ());
        // BT-120 VAT exemption reason text
        if (aTradeTax.getExemptionReason () != null)
        {
          final TaxExemptionReasonType aUBLTaxExemptionReason = new TaxExemptionReasonType ();
          aUBLTaxExemptionReason.setValue (aTradeTax.getExemptionReason ().getValue ());
          aUBLTaxExemptionReason.setLanguageID (aTradeTax.getExemptionReason ().getLanguageID ());
          aUBLTaxExemptionReason.setLanguageLocaleID (aTradeTax.getExemptionReason ().getLanguageLocaleID ());
          aUBLTaxCategory.addTaxExemptionReason (aUBLTaxExemptionReason);
        }
        final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
        aUBLTaxScheme.setID (getVATScheme ());
        aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);

        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);
      }
    }

    // BG-22 DOCUMENT TOTALS
    {
      final MonetaryTotalType aUBLMonetaryTotal = new MonetaryTotalType ();
      if (aSTSHMS != null)
      {
        // BT-106 Sum of Invoice line net amount
        if (aSTSHMS.hasLineTotalAmountEntries ())
          aUBLMonetaryTotal.setLineExtensionAmount (copyAmount (aSTSHMS.getLineTotalAmountAtIndex (0),
                                                                new LineExtensionAmountType (),
                                                                sDefaultCurrencyCode));
        // BT-109 Invoice total amount without VAT
        if (aSTSHMS.hasTaxBasisTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxExclusiveAmount (copyAmount (aSTSHMS.getTaxBasisTotalAmountAtIndex (0),
                                                               new TaxExclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        // BT-112 Invoice total amount with VAT
        if (aSTSHMS.hasGrandTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxInclusiveAmount (copyAmount (aSTSHMS.getGrandTotalAmountAtIndex (0),
                                                               new TaxInclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        // BT-107 Sum of allowances on document level
        if (aSTSHMS.hasAllowanceTotalAmountEntries ())
          aUBLMonetaryTotal.setAllowanceTotalAmount (copyAmount (aSTSHMS.getAllowanceTotalAmountAtIndex (0),
                                                                 new AllowanceTotalAmountType (),
                                                                 sDefaultCurrencyCode));
        // BT-108 Sum of charges on document level
        if (aSTSHMS.hasChargeTotalAmountEntries ())
          aUBLMonetaryTotal.setChargeTotalAmount (copyAmount (aSTSHMS.getChargeTotalAmountAtIndex (0),
                                                              new ChargeTotalAmountType (),
                                                              sDefaultCurrencyCode));
        // BT-113 Paid amount
        if (aSTSHMS.hasTotalPrepaidAmountEntries ())
          aUBLMonetaryTotal.setPrepaidAmount (copyAmount (aSTSHMS.getTotalPrepaidAmountAtIndex (0),
                                                          new PrepaidAmountType (),
                                                          sDefaultCurrencyCode));
        // BT-114 Rounding amount
        if (aSTSHMS.hasRoundingAmountEntries ())
        {
          // Work around
          // https://github.com/ConnectingEurope/eInvoicing-EN16931/issues/242
          // Fixed in release 1.3.4 of EN rules, but check left in for
          // compatibility
          if (BigHelper.isNE0 (aSTSHMS.getRoundingAmountAtIndex (0).getValue ()))
            aUBLMonetaryTotal.setPayableRoundingAmount (copyAmount (aSTSHMS.getRoundingAmountAtIndex (0),
                                                                    new PayableRoundingAmountType (),
                                                                    sDefaultCurrencyCode));
        }
        // BT-115 Amount due for payment
        if (aSTSHMS.hasDuePayableAmountEntries ())
          aUBLMonetaryTotal.setPayableAmount (copyAmount (aSTSHMS.getDuePayableAmountAtIndex (0),
                                                          new PayableAmountType (),
                                                          sDefaultCurrencyCode));
      }
      aUBLInvoice.setLegalMonetaryTotal (aUBLMonetaryTotal);
    }

    // BG-25 INVOICE LINE
    for (final SupplyChainTradeLineItemType aLineItem : aSCTT.getIncludedSupplyChainTradeLineItem ())
    {
      final InvoiceLineType aUBLInvoiceLine = new InvoiceLineType ();

      // BT-126 Invoice line identifier
      final DocumentLineDocumentType aDLD = aLineItem.getAssociatedDocumentLineDocument ();
      aUBLInvoiceLine.setID (_copyID (aDLD.getLineID ()));

      // BT-127 Invoice line note
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aLineNote : aDLD.getIncludedNote ())
        ifNotNull (_copyNote (aLineNote), aUBLInvoiceLine::addNote);

      // BT-131 Invoice line net amount
      boolean bLineExtensionAmountIsNegative = false;
      final LineTradeSettlementType aLineSettlement = aLineItem.getSpecifiedLineTradeSettlement ();
      final TradeSettlementLineMonetarySummationType aSTSLMS = aLineSettlement.getSpecifiedTradeSettlementLineMonetarySummation ();
      if (aSTSLMS != null)
      {
        if (aSTSLMS.hasLineTotalAmountEntries ())
        {
          aUBLInvoiceLine.setLineExtensionAmount (copyAmount (aSTSLMS.getLineTotalAmountAtIndex (0),
                                                              new LineExtensionAmountType (),
                                                              sDefaultCurrencyCode));
          if (isLT0Strict (aUBLInvoiceLine.getLineExtensionAmountValue ()))
            bLineExtensionAmountIsNegative = true;
        }
      }

      // BT-129/BT-130 Invoiced quantity and unit of measure
      final LineTradeDeliveryType aLineDelivery = aLineItem.getSpecifiedLineTradeDelivery ();
      if (aLineDelivery != null)
      {
        final QuantityType aBilledQuantity = aLineDelivery.getBilledQuantity ();
        if (aBilledQuantity != null)
        {
          aUBLInvoiceLine.setInvoicedQuantity (copyQuantity (aBilledQuantity, new InvoicedQuantityType ()));
        }
      }

      // BT-133 Invoice line Buyer accounting reference
      if (aLineSettlement.hasReceivableSpecifiedTradeAccountingAccountEntries ())
      {
        final TradeAccountingAccountType aLineAA = aLineSettlement.getReceivableSpecifiedTradeAccountingAccountAtIndex (0);
        aUBLInvoiceLine.setAccountingCost (aLineAA.getIDValue ());
      }

      // BG-26 INVOICE LINE PERIOD (BT-134/BT-135)
      final SpecifiedPeriodType aLineBillingPeriod = aLineSettlement.getBillingSpecifiedPeriod ();
      if (aLineBillingPeriod != null)
      {
        final PeriodType aUBLLinePeriod = new PeriodType ();

        final DateTimeType aStartDT = aLineBillingPeriod.getStartDateTime ();
        if (aStartDT != null)
          aUBLLinePeriod.setStartDate (parseDate (aStartDT.getDateTimeString (), aErrorList));

        final DateTimeType aEndDT = aLineBillingPeriod.getEndDateTime ();
        if (aEndDT != null)
          aUBLLinePeriod.setEndDate (parseDate (aEndDT.getDateTimeString (), aErrorList));

        if (aUBLLinePeriod.getStartDate () != null || aUBLLinePeriod.getEndDate () != null)
          aUBLInvoiceLine.addInvoicePeriod (aUBLLinePeriod);
      }

      // BT-132 Referenced purchase order line reference
      final LineTradeAgreementType aLineAgreement = aLineItem.getSpecifiedLineTradeAgreement ();
      if (aLineAgreement != null)
      {
        final ReferencedDocumentType aBuyerOrderReference = aLineAgreement.getBuyerOrderReferencedDocument ();
        if (aBuyerOrderReference != null && StringHelper.isNotEmpty (aBuyerOrderReference.getLineIDValue ()))
        {
          final OrderLineReferenceType aUBLOrderLineReference = new OrderLineReferenceType ();
          aUBLOrderLineReference.setLineID (copyID (aBuyerOrderReference.getLineID (), new LineIDType ()));
          aUBLInvoiceLine.addOrderLineReference (aUBLOrderLineReference);
        }
      }

      // BT-128/BT-128-1 Invoice line object identifier
      // EN 16931 only defines TypeCode="130" at line level. We don't filter here
      // because _convertDocumentReference already propagates TypeCode as
      // DocumentTypeCode, and dropping unknown type codes would silently lose data.
      for (final ReferencedDocumentType aLineReferencedDocument : aLineSettlement.getAdditionalReferencedDocument ())
      {
        final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aLineReferencedDocument, aErrorList);
        if (aUBLDocRef != null)
          aUBLInvoiceLine.addDocumentReference (aUBLDocRef);
      }

      // BG-27 INVOICE LINE ALLOWANCES / BG-28 INVOICE LINE CHARGES
      for (final TradeAllowanceChargeType aLineAllowanceCharge : aLineSettlement.getSpecifiedTradeAllowanceCharge ())
      {
        ETriState eIsCharge = ETriState.UNDEFINED;
        if (aLineAllowanceCharge.getChargeIndicator () != null)
          eIsCharge = parseIndicator (aLineAllowanceCharge.getChargeIndicator (), aErrorList);
        else
          aErrorList.add (buildError (new String [] { "CrossIndustryInvoice",
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

      // BG-31 ITEM INFORMATION
      final ItemType aUBLItem = new ItemType ();
      final TradeProductType aLineProduct = aLineItem.getSpecifiedTradeProduct ();
      if (aLineProduct != null)
      {
        // BT-154 Item description
        final TextType aDescription = aLineProduct.getDescription ();
        if (aDescription != null)
          ifNotNull (copyName (aDescription, new DescriptionType ()), aUBLItem::addDescription);

        // BT-153 Item name
        if (aLineProduct.hasNameEntries ())
          aUBLItem.setName (copyName (aLineProduct.getNameAtIndex (0), new NameType ()));

        // BT-156 Item Buyer's identifier
        final IDType aBuyerAssignedID = aLineProduct.getBuyerAssignedID ();
        if (aBuyerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aBuyerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setBuyersItemIdentification (aUBLID);
        }

        // BT-155 Item Seller's identifier
        final IDType aSellerAssignedID = aLineProduct.getSellerAssignedID ();
        if (aSellerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aSellerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setSellersItemIdentification (aUBLID);
        }

        // BT-157/BT-157-1 Item standard identifier
        final IDType aGlobalID = aLineProduct.getGlobalID ();
        if (aGlobalID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aGlobalID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setStandardItemIdentification (aUBLID);
        }

        // BT-159 Item country of origin
        final TradeCountryType aOriginCountry = aLineProduct.getOriginTradeCountry ();
        if (aOriginCountry != null)
        {
          final CountryType aUBLCountry = new CountryType ();
          aUBLCountry.setIdentificationCode (aOriginCountry.getIDValue ());
          if (aOriginCountry.hasNameEntries ())
            aUBLCountry.setName (copyName (aOriginCountry.getNameAtIndex (0), new NameType ()));
          aUBLItem.setOriginCountry (aUBLCountry);
        }

        // BT-158/BT-158-1/BT-158-2 Item classification identifier
        for (final ProductClassificationType aLineProductClassification : aLineProduct.getDesignatedProductClassification ())
        {
          final CodeType aClassCode = aLineProductClassification.getClassCode ();
          if (aClassCode != null)
          {
            final CommodityClassificationType aUBLCommodityClassification = new CommodityClassificationType ();
            aUBLCommodityClassification.setItemClassificationCode (copyCode (aClassCode,
                                                                             new ItemClassificationCodeType ()));
            if (aUBLCommodityClassification.getItemClassificationCode () != null)
              aUBLItem.addCommodityClassification (aUBLCommodityClassification);
          }
        }
      }

      // BG-30 LINE VAT INFORMATION (BT-151/BT-152)
      for (final TradeTaxType aTradeTax : aLineSettlement.getApplicableTradeTax ())
      {
        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
        final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
        aUBLTaxScheme.setID (getVATScheme ());
        aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
        aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);
      }

      if (aLineProduct != null)
      {
        // BG-32 ITEM ATTRIBUTES (BT-160/BT-161)
        for (final ProductCharacteristicType aAPC : aLineProduct.getApplicableProductCharacteristic ())
          if (aAPC.hasDescriptionEntries ())
          {
            final ItemPropertyType aUBLAdditionalItem = new ItemPropertyType ();
            aUBLAdditionalItem.setName (copyName (aAPC.getDescriptionAtIndex (0), new NameType ()));
            if (aAPC.hasValueEntries ())
              aUBLAdditionalItem.setValue (aAPC.getValueAtIndex (0).getValue ());
            if (aUBLAdditionalItem.getName () != null)
              aUBLItem.addAdditionalItemProperty (aUBLAdditionalItem);
          }
      }

      final PriceType aUBLPrice = new PriceType ();
      boolean bUsePrice = false;

      final AllowanceChargeType aUBLPriceAllowanceCharge = new AllowanceChargeType ();
      aUBLPriceAllowanceCharge.setChargeIndicator (false);
      aUBLPrice.addAllowanceCharge (aUBLPriceAllowanceCharge);
      boolean bUsePriceAC = false;

      if (aLineAgreement != null)
      {
        String sBT150 = null;
        final TradePriceType aGPPTP = aLineAgreement.getGrossPriceProductTradePrice ();
        if (aGPPTP != null)
        {
          if (aGPPTP.hasAppliedTradeAllowanceChargeEntries ())
          {
            // BT-147 Item Price Discount (optional)
            final var aTAC = aGPPTP.getAppliedTradeAllowanceChargeAtIndex (0);
            if (aTAC.hasActualAmountEntries ())
            {
              final AmountType aBT147 = aTAC.getActualAmountAtIndex (0);
              if (aBT147 != null)
              {
                aUBLPriceAllowanceCharge.setAmount (copyAmount (aBT147,
                                                                new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.AmountType (),
                                                                sDefaultCurrencyCode));
                bUsePriceAC = aUBLPriceAllowanceCharge.getAmount () != null;
              }
            }
          }
          if (aGPPTP.hasChargeAmountEntries ())
          {
            // BT-148 Item Gross Price (optional)
            final AmountType aBT148 = aGPPTP.getChargeAmountAtIndex (0);
            if (aBT148 != null)
            {
              aUBLPriceAllowanceCharge.setBaseAmount (copyAmount (aBT148, new BaseAmountType (), sDefaultCurrencyCode));
              if (!bUsePriceAC)
              {
                // Make sure the AC gets printed
                // Set "0" discount
                aUBLPriceAllowanceCharge.setAmount (BigDecimal.ZERO).setCurrencyID (sDefaultCurrencyCode);
                bUsePriceAC = true;
              }
            }
          }
          if (aGPPTP.getBasisQuantity () != null)
          {
            // BT-150 Item Price Base Quantity Unit of Measure Code
            sBT150 = aGPPTP.getBasisQuantity ().getUnitCode ();
          }
        }
        final TradePriceType aNPPTP = aLineAgreement.getNetPriceProductTradePrice ();
        if (aNPPTP != null)
        {
          if (aNPPTP.hasChargeAmountEntries ())
          {
            // BT-146 Item Net Price (mandatory)
            aUBLPrice.setPriceAmount (copyAmount (aNPPTP.getChargeAmountAtIndex (0),
                                                  new PriceAmountType (),
                                                  sDefaultCurrencyCode));
            // Only use the price if BT-146 is present
            bUsePrice = aUBLPrice.getPriceAmount () != null;
          }

          // Prefer gross over net
          // BT-149 Item Price Base Quantity (optional)
          var aBT149 = aGPPTP != null ? aGPPTP.getBasisQuantity () : null;
          if (aBT149 == null)
            aBT149 = aNPPTP.getBasisQuantity ();

          if (aBT149 != null)
          {
            // BT-149 Item Price Base Quantity (optional)
            aUBLPrice.setBaseQuantity (copyQuantity (aBT149, new BaseQuantityType ()));
            // BT-150 prefer gross unitCode, fall back to net unitCode
            if (aUBLPrice.getBaseQuantity () != null)
            {
              if (sBT150 != null)
                aUBLPrice.getBaseQuantity ().setUnitCode (sBT150);
              else
                if (aNPPTP.getBasisQuantity () != null)
                  aUBLPrice.getBaseQuantity ().setUnitCode (aNPPTP.getBasisQuantity ().getUnitCode ());
            }
          }
        }
      }

      if (aUBLInvoiceLine.getInvoicedQuantityValue () != null)
        swapQuantityAndPriceIfNeeded (bLineExtensionAmountIsNegative,
                                      aUBLInvoiceLine.getInvoicedQuantityValue (),
                                      aUBLInvoiceLine::setInvoicedQuantity,
                                      bUsePrice ? aUBLPrice.getPriceAmountValue () : null,
                                      bUsePrice ? aUBLPrice::setPriceAmount : null,
                                      aErrorList);

      if (bUsePrice)
      {
        if (!bUsePriceAC)
          aUBLPrice.setAllowanceCharge (null);
        aUBLInvoiceLine.setPrice (aUBLPrice);
      }

      aUBLInvoiceLine.setItem (aUBLItem);

      aUBLInvoice.addInvoiceLine (aUBLInvoiceLine);
    }

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
        aUBLCreditNote.setProfileID (aEDC.getBusinessProcessSpecifiedDocumentContextParameterAtIndex (0).getIDValue ());
      }
      if (aEDC.hasGuidelineSpecifiedDocumentContextParameterEntries ())
      {
        // BT-24
        aUBLCreditNote.setCustomizationID (aEDC.getGuidelineSpecifiedDocumentContextParameterAtIndex (0).getIDValue ());
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

    // Mandatory supplier
    final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
    aUBLCreditNote.setAccountingSupplierParty (aUBLSupplier);

    // Mandatory customer
    final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
    aUBLCreditNote.setAccountingCustomerParty (aUBLCustomer);

    // BT-2 Invoice issue date
    {
      LocalDate aIssueDate = null;
      if (aED != null && aED.getIssueDateTime () != null)
        aIssueDate = parseDate (aED.getIssueDateTime ().getDateTimeString (), aErrorList);

      if (aIssueDate != null)
        aUBLCreditNote.setIssueDate (aIssueDate);
    }

    // BT-9 Payment due date
    final LocalDate aPaymentDueDate;
    {
      LocalDate aDueDate = null;
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
        if (aPaymentTerms.getDueDateDateTime () != null)
        {
          aDueDate = parseDate (aPaymentTerms.getDueDateDateTime ().getDateTimeString (), aErrorList);
          if (aDueDate != null)
            break;
        }
      // Will be set in PaymentMeans/PaymentDueDate
      aPaymentDueDate = aDueDate;
    }

    // BT-3 Invoice type code
    if (aED != null)
      aUBLCreditNote.setCreditNoteTypeCode (aED.getTypeCodeValue ());

    // BG-1 INVOICE NOTE (BT-21/BT-22)
    if (aED != null)
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aEDNote : aED.getIncludedNote ())
        ifNotNull (_copyNote (aEDNote), aUBLCreditNote::addNote);

    // BT-7 TaxPointDate
    for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
    {
      if (aTradeTax.getTaxPointDate () != null)
      {
        final LocalDate aTaxPointDate = parseDate (aTradeTax.getTaxPointDate ().getDateString (), aErrorList);
        if (aTaxPointDate != null)
        {
          // Use the first tax point date only
          aUBLCreditNote.setTaxPointDate (aTaxPointDate);
          break;
        }
      }
    }

    // BT-5 Invoice currency code
    final String sDefaultCurrencyCode = aHeaderSettlement.getInvoiceCurrencyCodeValue ();
    aUBLCreditNote.setDocumentCurrencyCode (sDefaultCurrencyCode);

    // BT-6 VAT accounting currency code
    if (aHeaderSettlement.getTaxCurrencyCodeValue () != null)
    {
      aUBLCreditNote.setTaxCurrencyCode (aHeaderSettlement.getTaxCurrencyCodeValue ());
    }

    // BT-19 Buyer accounting reference
    for (final TradeAccountingAccountType aAccount : aHeaderSettlement.getReceivableSpecifiedTradeAccountingAccount ())
    {
      final String sID = aAccount.getIDValue ();
      if (StringHelper.isNotEmpty (sID))
      {
        // Use the first ID
        aUBLCreditNote.setAccountingCost (sID);
        break;
      }
    }

    // BT-10 Buyer reference
    if (aHeaderAgreement.getBuyerReferenceValue () != null)
    {
      aUBLCreditNote.setBuyerReference (aHeaderAgreement.getBuyerReferenceValue ());
    }

    // BG-14 INVOICING PERIOD (BT-73/BT-74)
    {
      final PeriodType aUBLPeriod = new PeriodType ();
      final SpecifiedPeriodType aSPT = aHeaderSettlement.getBillingSpecifiedPeriod ();
      if (aSPT != null)
      {
        final DateTimeType aStartDT = aSPT.getStartDateTime ();
        if (aStartDT != null)
          aUBLPeriod.setStartDate (parseDate (aStartDT.getDateTimeString (), aErrorList));

        final DateTimeType aEndDT = aSPT.getEndDateTime ();
        if (aEndDT != null)
          aUBLPeriod.setEndDate (parseDate (aEndDT.getDateTimeString (), aErrorList));
      }

      // BT-8 Value added tax point date code
      if (aHeaderSettlement.hasApplicableTradeTaxEntries ())
      {
        final TradeTaxType aTradeTax = aHeaderSettlement.getApplicableTradeTaxAtIndex (0);
        if (StringHelper.isNotEmpty (aTradeTax.getDueDateTypeCodeValue ()))
          aUBLPeriod.addDescriptionCode (new DescriptionCodeType (mapDueDateTypeCode (aTradeTax.getDueDateTypeCodeValue ())));
      }

      if (aUBLPeriod.getStartDate () != null ||
          aUBLPeriod.getEndDate () != null ||
          aUBLPeriod.hasDescriptionCodeEntries ())
        aUBLCreditNote.addInvoicePeriod (aUBLPeriod);
    }

    // BT-13 Purchase order reference + BT-14 Sales order reference
    {
      final OrderReferenceType aUBLOrderRef = _createUBLOrderRef (aHeaderAgreement.getBuyerOrderReferencedDocument (),
                                                                  aHeaderAgreement.getSellerOrderReferencedDocument ());
      aUBLCreditNote.setOrderReference (aUBLOrderRef);
    }

    // BG-3 PRECEDING INVOICE REFERENCE (BT-25/BT-26)
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderSettlement.getInvoiceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
      {
        final BillingReferenceType aUBLBillingRef = new BillingReferenceType ();
        // Must be the InvoiceDocumentReference - even for CreditNotes
        aUBLBillingRef.setInvoiceDocumentReference (aUBLDocRef);
        aUBLCreditNote.addBillingReference (aUBLBillingRef);
      }
    }

    // BT-16 Despatch advice reference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getDespatchAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLCreditNote.addDespatchDocumentReference (aUBLDocRef);
    }

    // BT-15 Receiving advice reference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getReceivingAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLCreditNote.addReceiptDocumentReference (aUBLDocRef);
    }

    // BT-17 Tender or lot reference (OriginatorDocumentReference)
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Use for "Tender or lot reference" with TypeCode "50" (BT-17)
        if (isOriginatorDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
          {
            // Explicitly disallowed here
            aUBLDocRef.setDocumentTypeCode ((DocumentTypeCodeType) null);
            aUBLCreditNote.addOriginatorDocumentReference (aUBLDocRef);
          }
        }
      }
    }

    // BT-12 Contract reference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderAgreement.getContractReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLCreditNote.addContractDocumentReference (aUBLDocRef);
    }

    // BG-24 ADDITIONAL SUPPORTING DOCUMENTS + BT-18/BT-18-1 Invoiced object identifier
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Except OriginatorDocumentReference (BT-17)
        if (!isOriginatorDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
            aUBLCreditNote.addAdditionalDocumentReference (aUBLDocRef);
        }
      }
    }

    // BT-11 Project reference
    {
      final ProcuringProjectType aSpecifiedProcuring = aHeaderAgreement.getSpecifiedProcuringProject ();
      if (aSpecifiedProcuring != null)
      {
        final String sID = aSpecifiedProcuring.getIDValue ();
        if (StringHelper.isNotEmpty (sID))
        {
          final ProjectReferenceType aUBLProjectRef = new ProjectReferenceType ();
          aUBLProjectRef.setID (sID);
          aUBLCreditNote.addProjectReference (aUBLProjectRef);
        }
      }
    }

    // BG-4 SELLER
    {
      final TradePartyType aSellerParty = aHeaderAgreement.getSellerTradeParty ();
      if (aSellerParty != null)
      {
        // BT-27 Seller name
        final PartyType aUBLParty = _convertParty (aSellerParty, true, true);

        // BT-31/BT-32 Seller VAT/tax registration
        for (final TaxRegistrationType aTaxRegistration : aSellerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // BT-28/BT-30/BT-30-1/BT-33 Seller legal entity
        _convertPartyLegalEntity (aSellerParty, aUBLParty);

        // BG-6 SELLER CONTACT (BT-41/BT-42/BT-43)
        final ContactType aUBLContact = _convertContact (aSellerParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLSupplier.setParty (aUBLParty);
      }
    }

    // BG-7 BUYER
    {
      final TradePartyType aBuyerParty = aHeaderAgreement.getBuyerTradeParty ();
      if (aBuyerParty != null)
      {
        // BT-44 Buyer name
        final PartyType aUBLParty = _convertParty (aBuyerParty, false, true);

        // BT-48 Buyer VAT identifier
        for (final TaxRegistrationType aTaxRegistration : aBuyerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // BT-45/BT-47/BT-47-1 Buyer legal entity
        _convertPartyLegalEntity (aBuyerParty, aUBLParty);

        // BG-9 BUYER CONTACT (BT-56/BT-57/BT-58)
        final ContactType aUBLContact = _convertContact (aBuyerParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLCustomer.setParty (aUBLParty);
      }
    }

    // BG-10 PAYEE
    {
      final TradePartyType aPayeeParty = aHeaderSettlement.getPayeeTradeParty ();
      if (aPayeeParty != null)
      {
        // BT-59 Payee name
        final PartyType aUBLParty = _convertParty (aPayeeParty, false, false);

        for (final TaxRegistrationType aTaxRegistration : aPayeeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        // BT-61/BT-61-1 Payee legal registration identifier
        _convertPartyLegalEntity (aPayeeParty, aUBLParty);

        final ContactType aUBLContact = _convertContact (aPayeeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLCreditNote.setPayeeParty (aUBLParty);
      }
    }

    // BG-11 SELLER TAX REPRESENTATIVE PARTY
    {
      final TradePartyType aTaxRepresentativeParty = aHeaderAgreement.getSellerTaxRepresentativeTradeParty ();
      if (aTaxRepresentativeParty != null)
      {
        // BT-62 Seller tax representative name
        final PartyType aUBLParty = _convertParty (aTaxRepresentativeParty, false, false);

        // BT-63 Seller tax representative VAT identifier
        for (final TaxRegistrationType aTaxRegistration : aTaxRepresentativeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        final ContactType aUBLContact = _convertContact (aTaxRepresentativeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLCreditNote.setTaxRepresentativeParty (aUBLParty);
      }
    }

    // BG-13 DELIVERY INFORMATION
    {
      final DeliveryType aUBLDelivery = new DeliveryType ();
      boolean bUseDelivery = false;

      // BT-72 Actual delivery date
      final SupplyChainEventType aSCE = aHeaderDelivery.getActualDeliverySupplyChainEvent ();
      if (aSCE != null)
      {
        final DateTimeType aODT = aSCE.getOccurrenceDateTime ();
        if (aODT != null)
        {
          aUBLDelivery.setActualDeliveryDate (parseDate (aODT.getDateTimeString (), aErrorList));
          bUseDelivery = true;
        }
      }

      final TradePartyType aShipToParty = aHeaderDelivery.getShipToTradeParty ();
      if (aShipToParty != null)
      {
        final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_22.LocationType aUBLDeliveryLocation = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_22.LocationType ();
        boolean bUseLocation = false;

        // BT-71/BT-71-1 Deliver to location identifier
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.IDType aUBLID = _extractFirstPartyID (aShipToParty);
        if (aUBLID != null)
        {
          aUBLDeliveryLocation.setID (aUBLID);
          bUseLocation = true;
        }

        // BG-15 DELIVER TO ADDRESS (BT-75 to BT-80, BT-165)
        final TradeAddressType aPostalAddress = aShipToParty.getPostalTradeAddress ();
        if (aPostalAddress != null)
        {
          aUBLDeliveryLocation.setAddress (_convertPostalAddress (aPostalAddress));
          bUseLocation = true;
        }

        if (bUseLocation)
        {
          aUBLDelivery.setDeliveryLocation (aUBLDeliveryLocation);
          bUseDelivery = true;
        }

        // BT-70 Deliver to party name
        final TextType aName = aShipToParty.getName ();
        if (aName != null && StringHelper.isNotEmpty (aName.getValue ()))
        {
          final PartyType aUBLDeliveryParty = new PartyType ();
          final PartyNameType aUBLPartyName = new PartyNameType ();
          aUBLPartyName.setName (copyName (aName, new NameType ()));
          aUBLDeliveryParty.addPartyName (aUBLPartyName);
          aUBLDelivery.setDeliveryParty (aUBLDeliveryParty);
          bUseDelivery = true;
        }
      }

      if (bUseDelivery)
        aUBLCreditNote.addDelivery (aUBLDelivery);
    }

    // BG-16 PAYMENT INSTRUCTIONS
    {
      for (final TradeSettlementPaymentMeansType aPaymentMeans : aHeaderSettlement.getSpecifiedTradeSettlementPaymentMeans ())
      {
        _convertPaymentMeans (aHeaderSettlement,
                              aPaymentMeans,
                              // BT-90: place on Payee if present, otherwise on Seller
                              x -> {
                                if (aUBLCreditNote.getPayeeParty () != null)
                                  _addPartyID (x, aUBLCreditNote.getPayeeParty ());
                                else
                                  _addPartyID (x, aUBLCreditNote.getAccountingSupplierParty ().getParty ());
                              },
                              aPM -> {
                                // Add only to the first PaymentMeans
                                if (aPaymentDueDate != null && aUBLCreditNote.getPaymentMeansCount () == 0)
                                  aPM.setPaymentDueDate (aPaymentDueDate);
                                aUBLCreditNote.addPaymentMeans (aPM);
                              },
                              aErrorList);

        // Allowed again in 1.2.1: exactly 2
        if (false)
          // Since v1.2.0 only one is allowed
          if (true)
            break;
      }
    }

    // BT-20 Payment terms
    {
      for (final TradePaymentTermsType aPaymentTerms : aHeaderSettlement.getSpecifiedTradePaymentTerms ())
      {
        final PaymentTermsType aUBLPaymenTerms = new PaymentTermsType ();

        for (final TextType aDesc : aPaymentTerms.getDescription ())
          ifNotNull (_copyNote (aDesc), aUBLPaymenTerms::addNote);

        if (aUBLPaymenTerms.hasNoteEntries ())
          aUBLCreditNote.addPaymentTerms (aUBLPaymenTerms);
      }
    }

    // BG-20 DOCUMENT LEVEL ALLOWANCES / BG-21 DOCUMENT LEVEL CHARGES
    {
      for (final TradeAllowanceChargeType aAllowanceCharge : aHeaderSettlement.getSpecifiedTradeAllowanceCharge ())
      {
        ETriState eIsCharge = ETriState.UNDEFINED;
        if (aAllowanceCharge.getChargeIndicator () != null)
          eIsCharge = parseIndicator (aAllowanceCharge.getChargeIndicator (), aErrorList);
        else
          aErrorList.add (buildError (new String [] { "CrossIndustryCreditNote",
                                                      "SupplyChainTradeTransaction",
                                                      "ApplicableHeaderTradeSettlement",
                                                      "SpecifiedTradeAllowanceCharge" },
                                      "Failed to determine if SpecifiedTradeAllowanceCharge is an Allowance or a Charge"));
        if (eIsCharge.isDefined ())
        {
          final AllowanceChargeType aUBLAllowanceCharge = new AllowanceChargeType ();
          aUBLAllowanceCharge.setChargeIndicator (eIsCharge.getAsBooleanValue ());
          _copyAllowanceCharge (aAllowanceCharge, aUBLAllowanceCharge, sDefaultCurrencyCode);
          aUBLCreditNote.addAllowanceCharge (aUBLAllowanceCharge);
        }
      }
    }

    final TradeSettlementHeaderMonetarySummationType aSTSHMS = aHeaderSettlement.getSpecifiedTradeSettlementHeaderMonetarySummation ();

    // BG-23 VAT BREAKDOWN + BT-110/BT-111 Tax totals
    {
      TaxTotalType aUBLTaxTotal = null;
      if (aSTSHMS != null && aSTSHMS.hasTaxTotalAmountEntries ())
      {
        // BT-110 Invoice total VAT amount / BT-111 Invoice total VAT amount in accounting currency
        for (final AmountType aTaxTotalAmount : aSTSHMS.getTaxTotalAmount ())
        {
          final TaxTotalType aUBLCurTaxTotal = new TaxTotalType ();
          aUBLCurTaxTotal.setTaxAmount (copyAmount (aTaxTotalAmount, new TaxAmountType (), sDefaultCurrencyCode));
          aUBLCreditNote.addTaxTotal (aUBLCurTaxTotal);

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
        aUBLCreditNote.addTaxTotal (aUBLTaxTotal);
      }

      // BG-23 VAT BREAKDOWN subtotals
      for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();

        // BT-116 VAT category taxable amount
        if (aTradeTax.hasBasisAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxableAmount (copyAmount (aTradeTax.getBasisAmountAtIndex (0),
                                                        new TaxableAmountType (),
                                                        sDefaultCurrencyCode));
        }

        // BT-117 VAT category tax amount
        if (aTradeTax.hasCalculatedAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxAmount (copyAmount (aTradeTax.getCalculatedAmountAtIndex (0),
                                                    new TaxAmountType (),
                                                    sDefaultCurrencyCode));
        }

        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        // BT-118 VAT category code
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        // BT-119 VAT category rate
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
        // BT-121 VAT exemption reason code
        if (StringHelper.isNotEmpty (aTradeTax.getExemptionReasonCodeValue ()))
          aUBLTaxCategory.setTaxExemptionReasonCode (aTradeTax.getExemptionReasonCodeValue ());
        // BT-120 VAT exemption reason text
        if (aTradeTax.getExemptionReason () != null)
        {
          final TaxExemptionReasonType aUBLTaxExemptionReason = new TaxExemptionReasonType ();
          aUBLTaxExemptionReason.setValue (aTradeTax.getExemptionReason ().getValue ());
          aUBLTaxExemptionReason.setLanguageID (aTradeTax.getExemptionReason ().getLanguageID ());
          aUBLTaxExemptionReason.setLanguageLocaleID (aTradeTax.getExemptionReason ().getLanguageLocaleID ());
          aUBLTaxCategory.addTaxExemptionReason (aUBLTaxExemptionReason);
        }
        final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
        aUBLTaxScheme.setID (getVATScheme ());
        aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
        aUBLTaxSubtotal.setTaxCategory (aUBLTaxCategory);

        aUBLTaxTotal.addTaxSubtotal (aUBLTaxSubtotal);
      }
    }

    // BG-22 DOCUMENT TOTALS
    {
      final MonetaryTotalType aUBLMonetaryTotal = new MonetaryTotalType ();
      if (aSTSHMS != null)
      {
        // BT-106 Sum of Invoice line net amount
        if (aSTSHMS.hasLineTotalAmountEntries ())
          aUBLMonetaryTotal.setLineExtensionAmount (copyAmount (aSTSHMS.getLineTotalAmountAtIndex (0),
                                                                new LineExtensionAmountType (),
                                                                sDefaultCurrencyCode));
        // BT-109 Invoice total amount without VAT
        if (aSTSHMS.hasTaxBasisTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxExclusiveAmount (copyAmount (aSTSHMS.getTaxBasisTotalAmountAtIndex (0),
                                                               new TaxExclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        // BT-112 Invoice total amount with VAT
        if (aSTSHMS.hasGrandTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxInclusiveAmount (copyAmount (aSTSHMS.getGrandTotalAmountAtIndex (0),
                                                               new TaxInclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        // BT-107 Sum of allowances on document level
        if (aSTSHMS.hasAllowanceTotalAmountEntries ())
          aUBLMonetaryTotal.setAllowanceTotalAmount (copyAmount (aSTSHMS.getAllowanceTotalAmountAtIndex (0),
                                                                 new AllowanceTotalAmountType (),
                                                                 sDefaultCurrencyCode));
        // BT-108 Sum of charges on document level
        if (aSTSHMS.hasChargeTotalAmountEntries ())
          aUBLMonetaryTotal.setChargeTotalAmount (copyAmount (aSTSHMS.getChargeTotalAmountAtIndex (0),
                                                              new ChargeTotalAmountType (),
                                                              sDefaultCurrencyCode));
        // BT-113 Paid amount
        if (aSTSHMS.hasTotalPrepaidAmountEntries ())
          aUBLMonetaryTotal.setPrepaidAmount (copyAmount (aSTSHMS.getTotalPrepaidAmountAtIndex (0),
                                                          new PrepaidAmountType (),
                                                          sDefaultCurrencyCode));
        // BT-114 Rounding amount
        if (aSTSHMS.hasRoundingAmountEntries ())
        {
          // Work around
          // https://github.com/ConnectingEurope/eInvoicing-EN16931/issues/242
          // Fixed in release 1.3.4 of EN rules, but check left in for
          // compatibility
          if (BigHelper.isNE0 (aSTSHMS.getRoundingAmountAtIndex (0).getValue ()))
            aUBLMonetaryTotal.setPayableRoundingAmount (copyAmount (aSTSHMS.getRoundingAmountAtIndex (0),
                                                                    new PayableRoundingAmountType (),
                                                                    sDefaultCurrencyCode));
        }
        // BT-115 Amount due for payment
        if (aSTSHMS.hasDuePayableAmountEntries ())
          aUBLMonetaryTotal.setPayableAmount (copyAmount (aSTSHMS.getDuePayableAmountAtIndex (0),
                                                          new PayableAmountType (),
                                                          sDefaultCurrencyCode));
      }
      aUBLCreditNote.setLegalMonetaryTotal (aUBLMonetaryTotal);
    }

    // BG-25 CREDIT NOTE LINE
    for (final SupplyChainTradeLineItemType aLineItem : aSCTT.getIncludedSupplyChainTradeLineItem ())
    {
      final CreditNoteLineType aUBLCreditNoteLine = new CreditNoteLineType ();

      final DocumentLineDocumentType aDLD = aLineItem.getAssociatedDocumentLineDocument ();
      aUBLCreditNoteLine.setID (_copyID (aDLD.getLineID ()));

      // Note
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aLineNote : aDLD.getIncludedNote ())
        ifNotNull (_copyNote (aLineNote), aUBLCreditNoteLine::addNote);

      // Line extension amount
      boolean bLineExtensionAmountIsNegative = false;
      final LineTradeSettlementType aLineSettlement = aLineItem.getSpecifiedLineTradeSettlement ();
      final TradeSettlementLineMonetarySummationType aSTSLMS = aLineSettlement.getSpecifiedTradeSettlementLineMonetarySummation ();
      if (aSTSLMS != null)
      {
        if (aSTSLMS.hasLineTotalAmountEntries ())
        {
          aUBLCreditNoteLine.setLineExtensionAmount (copyAmount (aSTSLMS.getLineTotalAmountAtIndex (0),
                                                                 new LineExtensionAmountType (),
                                                                 sDefaultCurrencyCode));
          if (isLT0Strict (aUBLCreditNoteLine.getLineExtensionAmountValue ()))
            bLineExtensionAmountIsNegative = true;
        }
      }

      // CreditNoted quantity
      final LineTradeDeliveryType aLineDelivery = aLineItem.getSpecifiedLineTradeDelivery ();
      if (aLineDelivery != null)
      {
        final QuantityType aBilledQuantity = aLineDelivery.getBilledQuantity ();
        if (aBilledQuantity != null)
        {
          aUBLCreditNoteLine.setCreditedQuantity (copyQuantity (aBilledQuantity, new CreditedQuantityType ()));
        }
      }

      // Accounting cost
      if (aLineSettlement.hasReceivableSpecifiedTradeAccountingAccountEntries ())
      {
        final TradeAccountingAccountType aLineAA = aLineSettlement.getReceivableSpecifiedTradeAccountingAccountAtIndex (0);
        aUBLCreditNoteLine.setAccountingCost (aLineAA.getIDValue ());
      }

      // CreditNote period
      final SpecifiedPeriodType aLineBillingPeriod = aLineSettlement.getBillingSpecifiedPeriod ();
      if (aLineBillingPeriod != null)
      {
        final PeriodType aUBLLinePeriod = new PeriodType ();

        final DateTimeType aStartDT = aLineBillingPeriod.getStartDateTime ();
        if (aStartDT != null)
          aUBLLinePeriod.setStartDate (parseDate (aStartDT.getDateTimeString (), aErrorList));

        final DateTimeType aEndDT = aLineBillingPeriod.getEndDateTime ();
        if (aEndDT != null)
          aUBLLinePeriod.setEndDate (parseDate (aEndDT.getDateTimeString (), aErrorList));

        if (aUBLLinePeriod.getStartDate () != null || aUBLLinePeriod.getEndDate () != null)
          aUBLCreditNoteLine.addInvoicePeriod (aUBLLinePeriod);
      }

      // BT-132 Referenced purchase order line reference
      final LineTradeAgreementType aLineAgreement = aLineItem.getSpecifiedLineTradeAgreement ();
      if (aLineAgreement != null)
      {
        final ReferencedDocumentType aBuyerOrderReference = aLineAgreement.getBuyerOrderReferencedDocument ();
        if (aBuyerOrderReference != null && StringHelper.isNotEmpty (aBuyerOrderReference.getLineIDValue ()))
        {
          final OrderLineReferenceType aUBLOrderLineReference = new OrderLineReferenceType ();
          aUBLOrderLineReference.setLineID (copyID (aBuyerOrderReference.getLineID (), new LineIDType ()));
          aUBLCreditNoteLine.addOrderLineReference (aUBLOrderLineReference);
        }
      }

      // BT-128/BT-128-1 Invoice line object identifier
      // EN 16931 only defines TypeCode="130" at line level. We don't filter here
      // because _convertDocumentReference already propagates TypeCode as
      // DocumentTypeCode, and dropping unknown type codes would silently lose data.
      for (final ReferencedDocumentType aLineReferencedDocument : aLineSettlement.getAdditionalReferencedDocument ())
      {
        final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aLineReferencedDocument, aErrorList);
        if (aUBLDocRef != null)
          aUBLCreditNoteLine.addDocumentReference (aUBLDocRef);
      }

      // BG-27 INVOICE LINE ALLOWANCES / BG-28 INVOICE LINE CHARGES
      for (final TradeAllowanceChargeType aLineAllowanceCharge : aLineSettlement.getSpecifiedTradeAllowanceCharge ())
      {
        ETriState eIsCharge = ETriState.UNDEFINED;
        if (aLineAllowanceCharge.getChargeIndicator () != null)
          eIsCharge = parseIndicator (aLineAllowanceCharge.getChargeIndicator (), aErrorList);
        else
          aErrorList.add (buildError (new String [] { "CrossIndustryCreditNote",
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
          aUBLCreditNoteLine.addAllowanceCharge (aUBLLineAllowanceCharge);
        }
      }

      // BG-31 ITEM INFORMATION
      final ItemType aUBLItem = new ItemType ();
      final TradeProductType aLineProduct = aLineItem.getSpecifiedTradeProduct ();
      if (aLineProduct != null)
      {
        // BT-154 Item description
        final TextType aDescription = aLineProduct.getDescription ();
        if (aDescription != null)
          ifNotNull (copyName (aDescription, new DescriptionType ()), aUBLItem::addDescription);

        // BT-153 Item name
        if (aLineProduct.hasNameEntries ())
          aUBLItem.setName (copyName (aLineProduct.getNameAtIndex (0), new NameType ()));

        // BT-156 Item Buyer's identifier
        final IDType aBuyerAssignedID = aLineProduct.getBuyerAssignedID ();
        if (aBuyerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aBuyerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setBuyersItemIdentification (aUBLID);
        }

        // BT-155 Item Seller's identifier
        final IDType aSellerAssignedID = aLineProduct.getSellerAssignedID ();
        if (aSellerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aSellerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setSellersItemIdentification (aUBLID);
        }

        // BT-157/BT-157-1 Item standard identifier
        final IDType aGlobalID = aLineProduct.getGlobalID ();
        if (aGlobalID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aGlobalID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setStandardItemIdentification (aUBLID);
        }

        // BT-159 Item country of origin
        final TradeCountryType aOriginCountry = aLineProduct.getOriginTradeCountry ();
        if (aOriginCountry != null)
        {
          final CountryType aUBLCountry = new CountryType ();
          aUBLCountry.setIdentificationCode (aOriginCountry.getIDValue ());
          if (aOriginCountry.hasNameEntries ())
            aUBLCountry.setName (copyName (aOriginCountry.getNameAtIndex (0), new NameType ()));
          aUBLItem.setOriginCountry (aUBLCountry);
        }

        // BT-158/BT-158-1/BT-158-2 Item classification identifier
        for (final ProductClassificationType aLineProductClassification : aLineProduct.getDesignatedProductClassification ())
        {
          final CodeType aClassCode = aLineProductClassification.getClassCode ();
          if (aClassCode != null)
          {
            final CommodityClassificationType aUBLCommodityClassification = new CommodityClassificationType ();
            aUBLCommodityClassification.setItemClassificationCode (copyCode (aClassCode,
                                                                             new ItemClassificationCodeType ()));
            if (aUBLCommodityClassification.getItemClassificationCode () != null)
              aUBLItem.addCommodityClassification (aUBLCommodityClassification);
          }
        }
      }

      // BG-30 LINE VAT INFORMATION (BT-151/BT-152)
      for (final TradeTaxType aTradeTax : aLineSettlement.getApplicableTradeTax ())
      {
        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
        final TaxSchemeType aUBLTaxScheme = new TaxSchemeType ();
        aUBLTaxScheme.setID (getVATScheme ());
        aUBLTaxCategory.setTaxScheme (aUBLTaxScheme);
        aUBLItem.addClassifiedTaxCategory (aUBLTaxCategory);
      }

      if (aLineProduct != null)
      {
        // BG-32 ITEM ATTRIBUTES (BT-160/BT-161)
        for (final ProductCharacteristicType aAPC : aLineProduct.getApplicableProductCharacteristic ())
          if (aAPC.hasDescriptionEntries ())
          {
            final ItemPropertyType aUBLAdditionalItem = new ItemPropertyType ();
            aUBLAdditionalItem.setName (copyName (aAPC.getDescriptionAtIndex (0), new NameType ()));
            if (aAPC.hasValueEntries ())
              aUBLAdditionalItem.setValue (aAPC.getValueAtIndex (0).getValue ());
            if (aUBLAdditionalItem.getName () != null)
              aUBLItem.addAdditionalItemProperty (aUBLAdditionalItem);
          }
      }

      final PriceType aUBLPrice = new PriceType ();
      boolean bUsePrice = false;

      final AllowanceChargeType aUBLPriceAllowanceCharge = new AllowanceChargeType ();
      aUBLPriceAllowanceCharge.setChargeIndicator (false);
      aUBLPrice.addAllowanceCharge (aUBLPriceAllowanceCharge);
      boolean bUsePriceAC = false;

      if (aLineAgreement != null)
      {
        String sBT150 = null;
        final TradePriceType aGPPTP = aLineAgreement.getGrossPriceProductTradePrice ();
        if (aGPPTP != null)
        {
          if (aGPPTP.hasAppliedTradeAllowanceChargeEntries ())
          {
            // BT-147 Item Price Discount (optional)
            final var aTAC = aGPPTP.getAppliedTradeAllowanceChargeAtIndex (0);
            if (aTAC.hasActualAmountEntries ())
            {
              final AmountType aBT147 = aTAC.getActualAmountAtIndex (0);
              if (aBT147 != null)
              {
                aUBLPriceAllowanceCharge.setAmount (copyAmount (aBT147,
                                                                new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_22.AmountType (),
                                                                sDefaultCurrencyCode));
                bUsePriceAC = aUBLPriceAllowanceCharge.getAmount () != null;
              }
            }
          }
          if (aGPPTP.hasChargeAmountEntries ())
          {
            // BT-148 Item Gross Price (optional)
            final AmountType aBT148 = aGPPTP.getChargeAmountAtIndex (0);
            if (aBT148 != null)
            {
              aUBLPriceAllowanceCharge.setBaseAmount (copyAmount (aBT148, new BaseAmountType (), sDefaultCurrencyCode));
              if (!bUsePriceAC)
              {
                // Make sure the AC gets printed
                // Set "0" discount
                aUBLPriceAllowanceCharge.setAmount (BigDecimal.ZERO).setCurrencyID (sDefaultCurrencyCode);
                bUsePriceAC = true;
              }
            }
          }
          if (aGPPTP.getBasisQuantity () != null)
          {
            // BT-150 Item Price Base Quantity Unit of Measure Code
            sBT150 = aGPPTP.getBasisQuantity ().getUnitCode ();
          }
        }
        final TradePriceType aNPPTP = aLineAgreement.getNetPriceProductTradePrice ();
        if (aNPPTP != null)
        {
          if (aNPPTP.hasChargeAmountEntries ())
          {
            // BT-146 Item Net Price (mandatory)
            aUBLPrice.setPriceAmount (copyAmount (aNPPTP.getChargeAmountAtIndex (0),
                                                  new PriceAmountType (),
                                                  sDefaultCurrencyCode));
            // Only use the price if BT-146 is present
            bUsePrice = aUBLPrice.getPriceAmount () != null;
          }

          // Prefer gross over net
          // BT-149 Item Price Base Quantity (optional)
          var aBT149 = aGPPTP != null ? aGPPTP.getBasisQuantity () : null;
          if (aBT149 == null)
            aBT149 = aNPPTP.getBasisQuantity ();

          if (aBT149 != null)
          {
            // BT-149 Item Price Base Quantity (optional)
            aUBLPrice.setBaseQuantity (copyQuantity (aBT149, new BaseQuantityType ()));
            // BT-150 prefer gross unitCode, fall back to net unitCode
            if (aUBLPrice.getBaseQuantity () != null)
            {
              if (sBT150 != null)
                aUBLPrice.getBaseQuantity ().setUnitCode (sBT150);
              else
                if (aNPPTP.getBasisQuantity () != null)
                  aUBLPrice.getBaseQuantity ().setUnitCode (aNPPTP.getBasisQuantity ().getUnitCode ());
            }
          }
        }
      }

      if (aUBLCreditNoteLine.getCreditedQuantityValue () != null)
        swapQuantityAndPriceIfNeeded (bLineExtensionAmountIsNegative,
                                      aUBLCreditNoteLine.getCreditedQuantityValue (),
                                      aUBLCreditNoteLine::setCreditedQuantity,
                                      bUsePrice ? aUBLPrice.getPriceAmountValue () : null,
                                      bUsePrice ? aUBLPrice::setPriceAmount : null,
                                      aErrorList);

      if (bUsePrice)
      {
        if (!bUsePriceAC)
          aUBLPrice.setAllowanceCharge (null);
        aUBLCreditNoteLine.setPrice (aUBLPrice);
      }

      aUBLCreditNoteLine.setItem (aUBLItem);

      aUBLCreditNote.addCreditNoteLine (aUBLCreditNoteLine);
    }

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
