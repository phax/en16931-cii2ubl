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

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.*;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.*;
import oasis.names.specification.ubl.schema.xsd.creditnote_23.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_23.InvoiceType;
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
 * CII to UBL 2.3 converter.
 *
 * @author Philip Helger
 * @since 1.3.0
 */
public class CIIToUBL23Converter extends AbstractCIIToUBLConverter <CIIToUBL23Converter>
{
  private static final String UBL_VERSION = "2.3";

  public CIIToUBL23Converter ()
  {}

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.@Nullable IDType _copyID (@Nullable final IDType aCIIID)
  {
    return copyID (aCIIID, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType ());
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.@Nullable NoteType _copyNote (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.@Nullable NoteType aNote)
  {
    if (aNote == null)
      return null;

    final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.NoteType aUBLNote = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.NoteType ();
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

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.@Nullable NoteType _copyNote (@Nullable final TextType aText)
  {
    return copyName (aText, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.NoteType ());
  }

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
    // ID value is a mandatory field
    ret.setID (sID).setSchemeID (aRD.getReferenceTypeCodeValue ());

    // Add DocumentTypeCode where possible
    if (isValidDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
      ret.setDocumentTypeCode (aRD.getTypeCodeValue ());

    // IssueDate is optional
    final FormattedDateTimeType aFDT = aRD.getFormattedIssueDateTime ();
    if (aFDT != null)
      ret.setIssueDate (parseDate (aFDT.getDateTimeString (), aErrorList));

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
      aEmbeddedDoc.setValue (aBinObj.getValue ());
      aUBLAttachment.setEmbeddedDocumentBinaryObject (aEmbeddedDoc);

      ret.setAttachment (aUBLAttachment);
    }

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

  @NonNull
  private static AddressType _convertPostalAddress (@NonNull final TradeAddressType aPostalAddress)
  {
    final AddressType ret = new AddressType ();
    if (StringHelper.isNotEmpty (aPostalAddress.getLineOneValue ()))
      ret.setStreetName (aPostalAddress.getLineOneValue ());
    if (StringHelper.isNotEmpty (aPostalAddress.getLineTwoValue ()))
      ret.setAdditionalStreetName (aPostalAddress.getLineTwoValue ());
    if (StringHelper.isNotEmpty (aPostalAddress.getLineThreeValue ()))
    {
      final AddressLineType aUBLAddressLine = new AddressLineType ();
      aUBLAddressLine.setLine (aPostalAddress.getLineThreeValue ());
      ret.addAddressLine (aUBLAddressLine);
    }
    if (StringHelper.isNotEmpty (aPostalAddress.getCityNameValue ()))
      ret.setCityName (aPostalAddress.getCityNameValue ());
    if (StringHelper.isNotEmpty (aPostalAddress.getPostcodeCodeValue ()))
      ret.setPostalZone (aPostalAddress.getPostcodeCodeValue ());
    if (aPostalAddress.hasCountrySubDivisionNameEntries ())
      ret.setCountrySubentity (aPostalAddress.getCountrySubDivisionNameAtIndex (0).getValue ());
    if (StringHelper.isNotEmpty (aPostalAddress.getCountryIDValue ()))
    {
      final CountryType aUBLCountry = new CountryType ();
      aUBLCountry.setIdentificationCode (aPostalAddress.getCountryIDValue ());
      ret.setCountry (aUBLCountry);
    }
    return ret;
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.@Nullable IDType _extractFirstPartyID (@NonNull final TradePartyType aParty)
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
                                           @NonNull final Consumer <? super oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType> aIDConsumer)
  {
    if (canUseGlobalID (aParty))
      getAllUsableGlobalIDs (aParty).forEach (x -> aIDConsumer.accept (_copyID (x)));
    else
      for (final IDType aID : aParty.getID ())
        aIDConsumer.accept (_copyID (aID));
  }

  private static void _addPartyID (final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.@Nullable IDType aUBLID,
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

    if (aParty.hasURIUniversalCommunicationEntries ())
    {
      final UniversalCommunicationType UC = aParty.getURIUniversalCommunicationAtIndex (0);
      ret.setEndpointID (copyID (UC.getURIID (), new EndpointIDType ()));
    }

    if (bMultiID)
      _extractAllPartyIDs (aParty, x -> _addPartyID (x, ret));
    else
      _addPartyID (_extractFirstPartyID (aParty), ret);

    // BT-27, BT-44, BT-59, BT-62, BT-70
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

  private static void _convertPartyLegalEntity (@NonNull final TradePartyType aTradeParty,
                                                @NonNull final PartyType aUBLParty)
  {
    final PartyLegalEntityType aUBLPartyLegalEntity;
    if (aUBLParty.hasPartyLegalEntityEntries ())
    {
      aUBLPartyLegalEntity = aUBLParty.getPartyLegalEntityAtIndex (0);
    }
    else
    {
      aUBLPartyLegalEntity = new PartyLegalEntityType ();
      aUBLParty.addPartyLegalEntity (aUBLPartyLegalEntity);
    }

    final LegalOrganizationType aSLO = aTradeParty.getSpecifiedLegalOrganization ();
    if (aSLO != null)
    {
      if (StringHelper.isNotEmpty (aSLO.getTradingBusinessNameValue ()))
      {
        final PartyNameType aUBLPartyName = new PartyNameType ();
        aUBLPartyName.setName (aSLO.getTradingBusinessNameValue ());
        if (aUBLPartyName.getName () != null)
          aUBLParty.addPartyName (aUBLPartyName);
      }

      aUBLPartyLegalEntity.setCompanyID (copyID (aSLO.getID (), new CompanyIDType ()));
    }

    // UBL 2.3+ supports multiple of them
    for (final TextType aDesc : aTradeParty.getDescription ())
      if (StringHelper.isNotEmpty (aDesc.getValue ()))
        aUBLPartyLegalEntity.addCompanyLegalForm (new CompanyLegalFormType (aDesc.getValue ()));

    if (aUBLPartyLegalEntity.getRegistrationName () == null)
    {
      // Mandatory field according to Schematron
      aUBLPartyLegalEntity.setRegistrationName (aTradeParty.getNameValue ());
    }
  }

  @Nullable
  private static ContactType _convertContact (@NonNull final TradePartyType aTradeParty)
  {
    if (!aTradeParty.hasDefinedTradeContactEntries ())
      return null;

    final TradeContactType aDTC = aTradeParty.getDefinedTradeContactAtIndex (0);
    final ContactType aUBLContact = new ContactType ();

    aUBLContact.setName (copyName (aDTC.getPersonName (), new NameType ()));
    if (aUBLContact.getNameValue () == null)
    {
      // BT-41
      aUBLContact.setName (copyName (aDTC.getDepartmentName (), new NameType ()));
    }

    final UniversalCommunicationType aTel = aDTC.getTelephoneUniversalCommunication ();
    if (aTel != null)
      ifNotEmpty (aTel.getCompleteNumberValue (), aUBLContact::setTelephone);

    final UniversalCommunicationType aEmail = aDTC.getEmailURIUniversalCommunication ();
    if (aEmail != null)
      ifNotEmpty (aEmail.getURIIDValue (), aUBLContact::setElectronicMail);

    if (aUBLContact.getName () == null &&
        aUBLContact.getTelephone () == null &&
        aUBLContact.getElectronicMail () == null)
      return null;
    return aUBLContact;
  }

  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.@Nullable AmountType _copyAmount (@Nullable final AmountType aAmount,
                                                                                                                     @Nullable final String sDefaultCurrencyCode)
  {
    return copyAmount (aAmount,
                       new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.AmountType (),
                       sDefaultCurrencyCode);
  }

  private void _copyAllowanceCharge (@NonNull final TradeAllowanceChargeType aAllowanceCharge,
                                     @NonNull final AllowanceChargeType aUBLAllowanceCharge,
                                     @Nullable final String sDefaultCurrencyCode)
  {
    if (StringHelper.isNotEmpty (aAllowanceCharge.getReasonCodeValue ()))
      aUBLAllowanceCharge.setAllowanceChargeReasonCode (aAllowanceCharge.getReasonCodeValue ());

    if (aAllowanceCharge.getReason () != null)
    {
      final AllowanceChargeReasonType aUBLReason = new AllowanceChargeReasonType ();
      aUBLReason.setValue (aAllowanceCharge.getReasonValue ());
      aUBLAllowanceCharge.addAllowanceChargeReason (aUBLReason);
    }
    if (aAllowanceCharge.getCalculationPercent () != null)
    {
      // UBL requires values between 0 and 100
      aUBLAllowanceCharge.setMultiplierFactorNumeric (aAllowanceCharge.getCalculationPercentValue ());
    }
    if (aAllowanceCharge.hasActualAmountEntries ())
    {
      aUBLAllowanceCharge.setAmount (_copyAmount (aAllowanceCharge.getActualAmountAtIndex (0), sDefaultCurrencyCode));
    }

    aUBLAllowanceCharge.setBaseAmount (copyAmount (aAllowanceCharge.getBasisAmount (),
                                                   new BaseAmountType (),
                                                   sDefaultCurrencyCode));

    // TaxCategory
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
                                     @NonNull final Consumer <oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType> aSellerIDHandler,
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
          {
            // UBL 2.3+ supports multiple
            aUBLPaymentMeans.addCardAccount (aUBLCardAccount);
          }
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

      // BT-90
      // how to determine if it is the Seller or the Payee?
      // For direct debit it's always assumed to be the seller
      final IDType aCreditorRefID = aHeaderSettlement.getCreditorReferenceID ();
      if (aCreditorRefID != null)
      {
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType aSellerID = _copyID (aCreditorRefID);
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

    if (aED != null)
      aUBLInvoice.setID (aED.getIDValue ());

    // Mandatory supplier
    final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
    aUBLInvoice.setAccountingSupplierParty (aUBLSupplier);

    // Mandatory customer
    final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
    aUBLInvoice.setAccountingCustomerParty (aUBLCustomer);

    // IssueDate
    {
      LocalDate aIssueDate = null;
      if (aED != null && aED.getIssueDateTime () != null)
        aIssueDate = parseDate (aED.getIssueDateTime ().getDateTimeString (), aErrorList);

      if (aIssueDate != null)
        aUBLInvoice.setIssueDate (aIssueDate);
    }

    // BT-9 DueDate
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

    // InvoiceTypeCode
    if (aED != null)
      aUBLInvoice.setInvoiceTypeCode (aED.getTypeCodeValue ());

    // Note
    if (aED != null)
      for (final un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType aEDNote : aED.getIncludedNote ())
        ifNotNull (_copyNote (aEDNote), aUBLInvoice::addNote);

    // BT-7 TaxPointDate
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
      if (StringHelper.isNotEmpty (sID))
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

    // OrderReference
    {
      final OrderReferenceType aUBLOrderRef = _createUBLOrderRef (aHeaderAgreement.getBuyerOrderReferencedDocument (),
                                                                  aHeaderAgreement.getSellerOrderReferencedDocument ());
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
        if (!isOriginatorDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
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
        if (StringHelper.isNotEmpty (sID))
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

    // Customer Party
    {
      final TradePartyType aBuyerParty = aHeaderAgreement.getBuyerTradeParty ();
      if (aBuyerParty != null)
      {
        // BT-44
        final PartyType aUBLParty = _convertParty (aBuyerParty, false, true);

        for (final TaxRegistrationType aTaxRegistration : aBuyerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        _convertPartyLegalEntity (aBuyerParty, aUBLParty);

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
        // BT-59
        final PartyType aUBLParty = _convertParty (aPayeeParty, false, false);

        for (final TaxRegistrationType aTaxRegistration : aPayeeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
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
        // BT-62
        final PartyType aUBLParty = _convertParty (aTaxRepresentativeParty, false, false);

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

    // Delivery
    {
      final DeliveryType aUBLDelivery = new DeliveryType ();
      boolean bUseDelivery = false;

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
        final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType aUBLDeliveryLocation = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType ();
        boolean bUseLocation = false;

        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType aUBLID = _extractFirstPartyID (aShipToParty);
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
        {
          aUBLDelivery.setDeliveryLocation (aUBLDeliveryLocation);
          bUseDelivery = true;
        }

        final TextType aName = aShipToParty.getName ();
        if (aName != null)
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

    // Payment means
    {
      for (final TradeSettlementPaymentMeansType aPaymentMeans : aHeaderSettlement.getSpecifiedTradeSettlementPaymentMeans ())
      {
        _convertPaymentMeans (aHeaderSettlement,
                              aPaymentMeans,
                              x -> _addPartyID (x, aUBLInvoice.getAccountingSupplierParty ().getParty ()),
                              aUBLInvoice::addPaymentMeans,
                              aErrorList);

        // Allowed again in 1.2.1: exactly 2
        if (false)
          // Since v1.2.0 only one is allowed
          if (true)
            break;
      }
    }

    // Payment Terms
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

    // Allowance Charge
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

    // TaxTotal
    {
      TaxTotalType aUBLTaxTotal = null;
      if (aSTSHMS != null && aSTSHMS.hasTaxTotalAmountEntries ())
      {
        // For all currencies
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

      for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();

        if (aTradeTax.hasBasisAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxableAmount (copyAmount (aTradeTax.getBasisAmountAtIndex (0),
                                                        new TaxableAmountType (),
                                                        sDefaultCurrencyCode));
        }

        if (aTradeTax.hasCalculatedAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxAmount (copyAmount (aTradeTax.getCalculatedAmountAtIndex (0),
                                                    new TaxAmountType (),
                                                    sDefaultCurrencyCode));
        }

        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
        if (StringHelper.isNotEmpty (aTradeTax.getExemptionReasonCodeValue ()))
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
        aUBLTaxScheme.setID (getVATScheme ());
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
          aUBLMonetaryTotal.setLineExtensionAmount (copyAmount (aSTSHMS.getLineTotalAmountAtIndex (0),
                                                                new LineExtensionAmountType (),
                                                                sDefaultCurrencyCode));
        if (aSTSHMS.hasTaxBasisTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxExclusiveAmount (copyAmount (aSTSHMS.getTaxBasisTotalAmountAtIndex (0),
                                                               new TaxExclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        if (aSTSHMS.hasGrandTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxInclusiveAmount (copyAmount (aSTSHMS.getGrandTotalAmountAtIndex (0),
                                                               new TaxInclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        if (aSTSHMS.hasAllowanceTotalAmountEntries ())
          aUBLMonetaryTotal.setAllowanceTotalAmount (copyAmount (aSTSHMS.getAllowanceTotalAmountAtIndex (0),
                                                                 new AllowanceTotalAmountType (),
                                                                 sDefaultCurrencyCode));
        if (aSTSHMS.hasChargeTotalAmountEntries ())
          aUBLMonetaryTotal.setChargeTotalAmount (copyAmount (aSTSHMS.getChargeTotalAmountAtIndex (0),
                                                              new ChargeTotalAmountType (),
                                                              sDefaultCurrencyCode));
        if (aSTSHMS.hasTotalPrepaidAmountEntries ())
          aUBLMonetaryTotal.setPrepaidAmount (copyAmount (aSTSHMS.getTotalPrepaidAmountAtIndex (0),
                                                          new PrepaidAmountType (),
                                                          sDefaultCurrencyCode));
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
        if (aSTSHMS.hasDuePayableAmountEntries ())
          aUBLMonetaryTotal.setPayableAmount (copyAmount (aSTSHMS.getDuePayableAmountAtIndex (0),
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
        ifNotNull (_copyNote (aLineNote), aUBLInvoiceLine::addNote);

      // Line extension amount
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

      // Invoiced quantity
      final LineTradeDeliveryType aLineDelivery = aLineItem.getSpecifiedLineTradeDelivery ();
      if (aLineDelivery != null)
      {
        final QuantityType aBilledQuantity = aLineDelivery.getBilledQuantity ();
        if (aBilledQuantity != null)
        {
          aUBLInvoiceLine.setInvoicedQuantity (copyQuantity (aBilledQuantity, new InvoicedQuantityType ()));
        }
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
        final DateTimeType aStartDT = aLineBillingPeriod.getStartDateTime ();
        if (aStartDT != null)
          aUBLLinePeriod.setStartDate (parseDate (aStartDT.getDateTimeString (), aErrorList));

        final DateTimeType aEndDT = aLineBillingPeriod.getEndDateTime ();
        if (aEndDT != null)
          aUBLLinePeriod.setEndDate (parseDate (aEndDT.getDateTimeString (), aErrorList));

        if (aUBLLinePeriod.getStartDate () != null || aUBLLinePeriod.getEndDate () != null)
          aUBLInvoiceLine.addInvoicePeriod (aUBLLinePeriod);
      }

      // Order line reference
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

      // Item
      final ItemType aUBLItem = new ItemType ();
      final TradeProductType aLineProduct = aLineItem.getSpecifiedTradeProduct ();
      if (aLineProduct != null)
      {
        final TextType aDescription = aLineProduct.getDescription ();
        if (aDescription != null)
          ifNotNull (copyName (aDescription, new DescriptionType ()), aUBLItem::addDescription);

        if (aLineProduct.hasNameEntries ())
          aUBLItem.setName (copyName (aLineProduct.getNameAtIndex (0), new NameType ()));

        final IDType aBuyerAssignedID = aLineProduct.getBuyerAssignedID ();
        if (aBuyerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aBuyerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setBuyersItemIdentification (aUBLID);
        }

        final IDType aSellerAssignedID = aLineProduct.getSellerAssignedID ();
        if (aSellerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aSellerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setSellersItemIdentification (aUBLID);
        }

        final IDType aGlobalID = aLineProduct.getGlobalID ();
        if (aGlobalID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aGlobalID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setStandardItemIdentification (aUBLID);
        }

        final TradeCountryType aOriginCountry = aLineProduct.getOriginTradeCountry ();
        if (aOriginCountry != null)
        {
          final CountryType aUBLCountry = new CountryType ();
          aUBLCountry.setIdentificationCode (aOriginCountry.getIDValue ());
          if (aOriginCountry.hasNameEntries ())
            aUBLCountry.setName (copyName (aOriginCountry.getNameAtIndex (0), new NameType ()));
          aUBLItem.setOriginCountry (aUBLCountry);
        }

        // Commodity Classification
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
                                                                new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.AmountType (),
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
            // BT-150 is from gross only
            if (aUBLPrice.getBaseQuantity () != null)
              aUBLPrice.getBaseQuantity ().setUnitCode (sBT150);
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

    if (aED != null)
      aUBLCreditNote.setID (aED.getIDValue ());

    // Mandatory supplier
    final SupplierPartyType aUBLSupplier = new SupplierPartyType ();
    aUBLCreditNote.setAccountingSupplierParty (aUBLSupplier);

    // Mandatory customer
    final CustomerPartyType aUBLCustomer = new CustomerPartyType ();
    aUBLCreditNote.setAccountingCustomerParty (aUBLCustomer);

    // IssueDate
    {
      LocalDate aIssueDate = null;
      if (aED != null && aED.getIssueDateTime () != null)
        aIssueDate = parseDate (aED.getIssueDateTime ().getDateTimeString (), aErrorList);

      if (aIssueDate != null)
        aUBLCreditNote.setIssueDate (aIssueDate);
    }

    // BT-9 DueDate
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

    // CreditNoteTypeCode
    if (aED != null)
      aUBLCreditNote.setCreditNoteTypeCode (aED.getTypeCodeValue ());

    // Note
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

    // DocumentCurrencyCode
    final String sDefaultCurrencyCode = aHeaderSettlement.getInvoiceCurrencyCodeValue ();
    aUBLCreditNote.setDocumentCurrencyCode (sDefaultCurrencyCode);

    // TaxCurrencyCode
    if (aHeaderSettlement.getTaxCurrencyCodeValue () != null)
    {
      aUBLCreditNote.setTaxCurrencyCode (aHeaderSettlement.getTaxCurrencyCodeValue ());
    }

    // AccountingCost
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

    // BuyerReferences
    if (aHeaderAgreement.getBuyerReferenceValue () != null)
    {
      aUBLCreditNote.setBuyerReference (aHeaderAgreement.getBuyerReferenceValue ());
    }

    // CreditNotePeriod
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

    // OrderReference
    {
      final OrderReferenceType aUBLOrderRef = _createUBLOrderRef (aHeaderAgreement.getBuyerOrderReferencedDocument (),
                                                                  aHeaderAgreement.getSellerOrderReferencedDocument ());
      aUBLCreditNote.setOrderReference (aUBLOrderRef);
    }

    // BillingReference
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

    // DespatchDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getDespatchAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLCreditNote.addDespatchDocumentReference (aUBLDocRef);
    }

    // ReceiptDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderDelivery.getReceivingAdviceReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLCreditNote.addReceiptDocumentReference (aUBLDocRef);
    }

    // OriginatorDocumentReference
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

    // ContractDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aHeaderAgreement.getContractReferencedDocument (),
                                                                          aErrorList);
      if (aUBLDocRef != null)
        aUBLCreditNote.addContractDocumentReference (aUBLDocRef);
    }

    // AdditionalDocumentReference
    {
      for (final ReferencedDocumentType aRD : aHeaderAgreement.getAdditionalReferencedDocument ())
      {
        // Except OriginatorDocumentReference
        if (!isOriginatorDocumentReferenceTypeCode (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD, aErrorList);
          if (aUBLDocRef != null)
            aUBLCreditNote.addAdditionalDocumentReference (aUBLDocRef);
        }
      }
    }

    // ProjectReference
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

    // Supplier Party
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

    // Customer Party
    {
      final TradePartyType aBuyerParty = aHeaderAgreement.getBuyerTradeParty ();
      if (aBuyerParty != null)
      {
        // BT-44
        final PartyType aUBLParty = _convertParty (aBuyerParty, false, true);

        for (final TaxRegistrationType aTaxRegistration : aBuyerParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        _convertPartyLegalEntity (aBuyerParty, aUBLParty);

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
        // BT-59
        final PartyType aUBLParty = _convertParty (aPayeeParty, false, false);

        for (final TaxRegistrationType aTaxRegistration : aPayeeParty.getSpecifiedTaxRegistration ())
        {
          final PartyTaxSchemeType aUBLPartyTaxScheme = _convertPartyTaxScheme (aTaxRegistration);
          if (aUBLPartyTaxScheme != null)
            aUBLParty.addPartyTaxScheme (aUBLPartyTaxScheme);
        }

        final ContactType aUBLContact = _convertContact (aPayeeParty);
        if (aUBLContact != null)
          aUBLParty.setContact (aUBLContact);

        aUBLCreditNote.setPayeeParty (aUBLParty);
      }
    }

    // Tax Representative Party
    {
      final TradePartyType aTaxRepresentativeParty = aHeaderAgreement.getSellerTaxRepresentativeTradeParty ();
      if (aTaxRepresentativeParty != null)
      {
        // BT-62
        final PartyType aUBLParty = _convertParty (aTaxRepresentativeParty, false, false);

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
            aUBLDelivery.setActualDeliveryDate (parseDate (aODT.getDateTimeString (), aErrorList));
        }

        final oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType aUBLDeliveryLocation = new oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_23.LocationType ();
        boolean bUseLocation = false;

        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_23.IDType aUBLID = _extractFirstPartyID (aShipToParty);
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
          aUBLPartyName.setName (copyName (aName, new NameType ()));
          aUBLDeliveryParty.addPartyName (aUBLPartyName);
          aUBLDelivery.setDeliveryParty (aUBLDeliveryParty);
        }

        aUBLCreditNote.addDelivery (aUBLDelivery);
      }
    }

    // Payment means
    {
      for (final TradeSettlementPaymentMeansType aPaymentMeans : aHeaderSettlement.getSpecifiedTradeSettlementPaymentMeans ())
      {
        _convertPaymentMeans (aHeaderSettlement,
                              aPaymentMeans,
                              x -> _addPartyID (x, aUBLCreditNote.getAccountingSupplierParty ().getParty ()),
                              aPM -> {
                                if (aPaymentDueDate != null)
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

    // Payment Terms
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

    // Allowance Charge
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

    // TaxTotal
    {
      TaxTotalType aUBLTaxTotal = null;
      if (aSTSHMS != null && aSTSHMS.hasTaxTotalAmountEntries ())
      {
        // For all currencies
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

      for (final TradeTaxType aTradeTax : aHeaderSettlement.getApplicableTradeTax ())
      {
        final TaxSubtotalType aUBLTaxSubtotal = new TaxSubtotalType ();

        if (aTradeTax.hasBasisAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxableAmount (copyAmount (aTradeTax.getBasisAmountAtIndex (0),
                                                        new TaxableAmountType (),
                                                        sDefaultCurrencyCode));
        }

        if (aTradeTax.hasCalculatedAmountEntries ())
        {
          aUBLTaxSubtotal.setTaxAmount (copyAmount (aTradeTax.getCalculatedAmountAtIndex (0),
                                                    new TaxAmountType (),
                                                    sDefaultCurrencyCode));
        }

        final TaxCategoryType aUBLTaxCategory = new TaxCategoryType ();
        aUBLTaxCategory.setID (aTradeTax.getCategoryCodeValue ());
        if (aTradeTax.getRateApplicablePercentValue () != null)
          aUBLTaxCategory.setPercent (BigHelper.getWithoutTrailingZeroes (aTradeTax.getRateApplicablePercentValue ()));
        if (StringHelper.isNotEmpty (aTradeTax.getExemptionReasonCodeValue ()))
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
        aUBLTaxScheme.setID (getVATScheme ());
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
          aUBLMonetaryTotal.setLineExtensionAmount (copyAmount (aSTSHMS.getLineTotalAmountAtIndex (0),
                                                                new LineExtensionAmountType (),
                                                                sDefaultCurrencyCode));
        if (aSTSHMS.hasTaxBasisTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxExclusiveAmount (copyAmount (aSTSHMS.getTaxBasisTotalAmountAtIndex (0),
                                                               new TaxExclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        if (aSTSHMS.hasGrandTotalAmountEntries ())
          aUBLMonetaryTotal.setTaxInclusiveAmount (copyAmount (aSTSHMS.getGrandTotalAmountAtIndex (0),
                                                               new TaxInclusiveAmountType (),
                                                               sDefaultCurrencyCode));
        if (aSTSHMS.hasAllowanceTotalAmountEntries ())
          aUBLMonetaryTotal.setAllowanceTotalAmount (copyAmount (aSTSHMS.getAllowanceTotalAmountAtIndex (0),
                                                                 new AllowanceTotalAmountType (),
                                                                 sDefaultCurrencyCode));
        if (aSTSHMS.hasChargeTotalAmountEntries ())
          aUBLMonetaryTotal.setChargeTotalAmount (copyAmount (aSTSHMS.getChargeTotalAmountAtIndex (0),
                                                              new ChargeTotalAmountType (),
                                                              sDefaultCurrencyCode));
        if (aSTSHMS.hasTotalPrepaidAmountEntries ())
          aUBLMonetaryTotal.setPrepaidAmount (copyAmount (aSTSHMS.getTotalPrepaidAmountAtIndex (0),
                                                          new PrepaidAmountType (),
                                                          sDefaultCurrencyCode));
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
        if (aSTSHMS.hasDuePayableAmountEntries ())
          aUBLMonetaryTotal.setPayableAmount (copyAmount (aSTSHMS.getDuePayableAmountAtIndex (0),
                                                          new PayableAmountType (),
                                                          sDefaultCurrencyCode));
      }
      aUBLCreditNote.setLegalMonetaryTotal (aUBLMonetaryTotal);
    }

    // All invoice lines
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

      // Order line reference
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

      // Document reference
      for (final ReferencedDocumentType aLineReferencedDocument : aLineSettlement.getAdditionalReferencedDocument ())
      {
        final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aLineReferencedDocument, aErrorList);
        if (aUBLDocRef != null)
          aUBLCreditNoteLine.addDocumentReference (aUBLDocRef);
      }

      // Allowance charge
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

      // Item
      final ItemType aUBLItem = new ItemType ();
      final TradeProductType aLineProduct = aLineItem.getSpecifiedTradeProduct ();
      if (aLineProduct != null)
      {
        final TextType aDescription = aLineProduct.getDescription ();
        if (aDescription != null)
          ifNotNull (copyName (aDescription, new DescriptionType ()), aUBLItem::addDescription);

        if (aLineProduct.hasNameEntries ())
          aUBLItem.setName (copyName (aLineProduct.getNameAtIndex (0), new NameType ()));

        final IDType aBuyerAssignedID = aLineProduct.getBuyerAssignedID ();
        if (aBuyerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aBuyerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setBuyersItemIdentification (aUBLID);
        }

        final IDType aSellerAssignedID = aLineProduct.getSellerAssignedID ();
        if (aSellerAssignedID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aSellerAssignedID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setSellersItemIdentification (aUBLID);
        }

        final IDType aGlobalID = aLineProduct.getGlobalID ();
        if (aGlobalID != null)
        {
          final ItemIdentificationType aUBLID = new ItemIdentificationType ();
          aUBLID.setID (_copyID (aGlobalID));
          if (StringHelper.isNotEmpty (aUBLID.getIDValue ()))
            aUBLItem.setStandardItemIdentification (aUBLID);
        }

        final TradeCountryType aOriginCountry = aLineProduct.getOriginTradeCountry ();
        if (aOriginCountry != null)
        {
          final CountryType aUBLCountry = new CountryType ();
          aUBLCountry.setIdentificationCode (aOriginCountry.getIDValue ());
          if (aOriginCountry.hasNameEntries ())
            aUBLCountry.setName (copyName (aOriginCountry.getNameAtIndex (0), new NameType ()));
          aUBLItem.setOriginCountry (aUBLCountry);
        }

        // Commodity Classification
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
      if (aLineAgreement != null)
      {
        final TradePriceType aNPPTP = aLineAgreement.getNetPriceProductTradePrice ();
        if (aNPPTP != null)
        {
          if (aNPPTP.hasChargeAmountEntries ())
          {
            aUBLPrice.setPriceAmount (copyAmount (aNPPTP.getChargeAmountAtIndex (0),
                                                  new PriceAmountType (),
                                                  sDefaultCurrencyCode));
            bUsePrice = true;
          }
          if (aNPPTP.getBasisQuantity () != null)
          {
            aUBLPrice.setBaseQuantity (copyQuantity (aNPPTP.getBasisQuantity (), new BaseQuantityType ()));
            bUsePrice = true;
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

      // Allowance charge
      final TradePriceType aGrossTradePrice = aLineAgreement.getGrossPriceProductTradePrice ();
      if (aGrossTradePrice != null)
      {
        for (final TradeAllowanceChargeType aPriceAllowanceCharge : aGrossTradePrice.getAppliedTradeAllowanceCharge ())
        {
          ETriState eIsCharge = ETriState.UNDEFINED;
          if (aPriceAllowanceCharge.getChargeIndicator () != null)
            eIsCharge = parseIndicator (aPriceAllowanceCharge.getChargeIndicator (), aErrorList);
          else
            aErrorList.add (buildError (new String [] { "CrossIndustryCreditNote",
                                                        "SupplyChainTradeTransaction",
                                                        "IncludedSupplyChainTradeLineItem",
                                                        "SpecifiedLineTradeAgreement",
                                                        "GrossPriceProductTradePrice",
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
      }

      if (bUsePrice)
        aUBLCreditNoteLine.setPrice (aUBLPrice);

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
