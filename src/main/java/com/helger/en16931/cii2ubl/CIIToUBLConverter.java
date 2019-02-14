package com.helger.en16931.cii2ubl;

import java.io.File;
import java.io.Serializable;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.datatype.XMLGregorianCalendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.cii.d16b.CIID16BReader;
import com.helger.commons.datetime.PDTFromString;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.math.MathHelper;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.util.PDTXMLConverter;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressLineType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AddressType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.AttachmentType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.BillingReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CountryType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.CustomerPartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.DocumentReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ExternalReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.OrderReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyIdentificationType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyNameType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PartyType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.PeriodType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.ProjectReferenceType;
import oasis.names.specification.ubl.schema.xsd.commonaggregatecomponents_21.SupplierPartyType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.DocumentDescriptionType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.EmbeddedDocumentBinaryObjectType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.EndpointIDType;
import oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NameType;
import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.qualifieddatatype._100.FormattedDateTimeType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ExchangedDocumentContextType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ExchangedDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeAgreementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeDeliveryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.NoteType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ProcuringProjectType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ReferencedDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.SpecifiedPeriodType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.SupplyChainTradeTransactionType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeAccountingAccountType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeAddressType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradePartyType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradePaymentTermsType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeSettlementHeaderMonetarySummationType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeTaxType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.BinaryObjectType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.DateTimeType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.IDType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.TextType;

public class CIIToUBLConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverter.class);

  public CIIToUBLConverter ()
  {}

  /**
   * Copy all ID parts from a CII ID to a CCTS/UBL ID.
   *
   * @param aCIIID
   *        CII ID
   * @param aUBLID
   *        UBL ID
   * @return Created UBL ID
   */
  @Nonnull
  private static <T extends com.helger.xsds.ccts.cct.schemamodule.IdentifierType> T _copyID (@Nonnull final IDType aCIIID,
                                                                                             @Nonnull final T aUBLID)
  {
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

  @Nonnull
  private static oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType _getAsUBLID (@Nonnull final IDType aCIIID)
  {
    return _copyID (aCIIID, new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.IDType ());
  }

  @Nonnull
  private static NameType _copyName (@Nonnull final TextType aName)
  {
    final NameType ret = new NameType ();
    ret.setValue (aName.getValue ());
    ret.setLanguageID (aName.getLanguageID ());
    ret.setLanguageLocaleID (aName.getLanguageLocaleID ());
    return ret;
  }

  @Nullable
  private static XMLGregorianCalendar _parseDateDDMMYYYY (@Nullable final String s)
  {
    final LocalDate aDate = PDTFromString.getLocalDateFromString (s, "uuMMdd");
    return PDTXMLConverter.getXMLCalendarDate (aDate);
  }

  @Nullable
  private static DocumentReferenceType _convertDocumentReference (@Nullable final ReferencedDocumentType aRD)
  {
    DocumentReferenceType ret = null;
    if (aRD != null)
    {
      final String sID = aRD.getIssuerAssignedIDValue ();
      if (StringHelper.hasText (sID))
      {
        ret = new DocumentReferenceType ();
        // ID value is a mandatory field
        ret.setID (sID).setSchemeID (aRD.getReferenceTypeCodeValue ());

        // IssueDate is optional
        final FormattedDateTimeType aFDT = aRD.getFormattedIssueDateTime ();
        if (aFDT != null)
          ret.setIssueDate (_parseDateDDMMYYYY (aFDT.getDateTimeStringValue ()));

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
      }
    }
    return ret;
  }

  @Nonnull
  private static PartyType _convertParty (@Nonnull final TradePartyType aParty)
  {
    final PartyType ret = new PartyType ();

    if (aParty.getGlobalIDCount () > 0)
    {
      final IDType aGlobalID = aParty.getGlobalIDAtIndex (0);
      final EndpointIDType aUBLEndpointID = _copyID (aGlobalID, new EndpointIDType ());
      ret.setEndpointID (aUBLEndpointID);
    }

    if (aParty.getIDCount () > 0)
    {
      final IDType aID = aParty.getIDAtIndex (0);

      final PartyIdentificationType aUBLPartyIdentification = new PartyIdentificationType ();
      aUBLPartyIdentification.setID (_getAsUBLID (aID));
      ret.addPartyIdentification (aUBLPartyIdentification);
    }

    final TextType aName = aParty.getName ();
    if (aName != null)
    {
      final PartyNameType aUBLPartyName = new PartyNameType ();
      aUBLPartyName.setName (_copyName (aName));
      ret.addPartyName (aUBLPartyName);
    }

    final TradeAddressType aPostalAddress = aParty.getPostalTradeAddress ();
    if (aPostalAddress != null)
    {
      ret.setPostalAddress (_convertPostalAddress (aPostalAddress));
    }

    // TODO
    return ret;
  }

  private static AddressType _convertPostalAddress (final TradeAddressType aPostalAddress)
  {
    final AddressType ret = new AddressType ();
    ret.setStreetName (aPostalAddress.getLineOneValue ());
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

  @Nonnull
  private InvoiceType _convertToInvoice (@Nonnull final CrossIndustryInvoiceType aCIIInvoice,
                                         @Nonnull final ErrorList aErrorList)
  {
    final ExchangedDocumentContextType aEDC = aCIIInvoice.getExchangedDocumentContext ();
    final ExchangedDocumentType aED = aCIIInvoice.getExchangedDocument ();
    final SupplyChainTradeTransactionType aSCTT = aCIIInvoice.getSupplyChainTradeTransaction ();
    final HeaderTradeAgreementType aAgreement = aSCTT.getApplicableHeaderTradeAgreement ();
    final HeaderTradeDeliveryType aDelivery = aSCTT.getApplicableHeaderTradeDelivery ();
    final HeaderTradeSettlementType aSettlement = aSCTT.getApplicableHeaderTradeSettlement ();

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
        aIssueDate = _parseDateDDMMYYYY (aED.getIssueDateTime ().getDateTimeStringValue ());
      if (aIssueDate == null)
        for (final TradePaymentTermsType aPaymentTerms : aSettlement.getSpecifiedTradePaymentTerms ())
          if (aPaymentTerms.getDueDateDateTime () != null)
          {
            aIssueDate = _parseDateDDMMYYYY (aPaymentTerms.getDueDateDateTime ().getDateTimeStringValue ());
            if (aIssueDate != null)
              break;
          }
      aUBLInvoice.setIssueDate (aIssueDate);
    }

    // InvoiceTypeCode
    aUBLInvoice.setInvoiceTypeCode (aED.getTypeCodeValue ());

    // Note
    {
      for (final NoteType aEDNote : aED.getIncludedNote ())
      {
        final oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType aUBLNote = new oasis.names.specification.ubl.schema.xsd.commonbasiccomponents_21.NoteType ();
        final StringBuilder aSB = new StringBuilder ();
        for (final TextType aText : aEDNote.getContent ())
        {
          if (aSB.length () > 0)
            aSB.append ('\n');
          aSB.append (aText.getValue ());
        }
        aUBLNote.setValue (aSB.toString ());
        aUBLInvoice.addNote (aUBLNote);
      }
    }

    // TaxPointDate
    for (final TradeTaxType aTradeTax : aSettlement.getApplicableTradeTax ())
    {
      if (aTradeTax.getTaxPointDate () != null)
      {
        final XMLGregorianCalendar aTaxPointDate = _parseDateDDMMYYYY (aTradeTax.getTaxPointDate ()
                                                                                .getDateStringValue ());
        if (aTaxPointDate != null)
        {
          // Use the first tax point date only
          aUBLInvoice.setTaxPointDate (aTaxPointDate);
          break;
        }
      }
    }

    // DocumentCurrencyCode
    aUBLInvoice.setDocumentCurrencyCode (aSettlement.getInvoiceCurrencyCodeValue ());

    // TaxCurrencyCode
    aUBLInvoice.setTaxCurrencyCode (aSettlement.getTaxCurrencyCodeValue ());

    // AccountingCost
    for (final TradeAccountingAccountType aAccount : aSettlement.getReceivableSpecifiedTradeAccountingAccount ())
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
    aUBLInvoice.setBuyerReference (aAgreement.getBuyerReferenceValue ());

    // InvoicePeriod
    {
      final SpecifiedPeriodType aSPT = aSettlement.getBillingSpecifiedPeriod ();
      if (aSPT != null)
      {
        final DateTimeType aStartDT = aSPT.getStartDateTime ();
        final DateTimeType aEndDT = aSPT.getEndDateTime ();

        if (aStartDT != null && aEndDT != null)
        {
          final PeriodType aUBLPeriod = new PeriodType ();
          aUBLPeriod.setStartDate (_parseDateDDMMYYYY (aStartDT.getDateTimeStringValue ()));
          aUBLPeriod.setEndDate (_parseDateDDMMYYYY (aEndDT.getDateTimeStringValue ()));
          aUBLInvoice.addInvoicePeriod (aUBLPeriod);
        }
      }
    }

    // OrderReference
    {
      final OrderReferenceType aUBLOrderRef = new OrderReferenceType ();
      final ReferencedDocumentType aBuyerOrderRef = aAgreement.getBuyerOrderReferencedDocument ();
      if (aBuyerOrderRef != null)
        aUBLOrderRef.setID (aBuyerOrderRef.getIssuerAssignedIDValue ());
      final ReferencedDocumentType aSellerOrderRef = aAgreement.getSellerOrderReferencedDocument ();
      if (aSellerOrderRef != null)
        aUBLOrderRef.setSalesOrderID (aSellerOrderRef.getIssuerAssignedIDValue ());

      // Set if any field is set
      if (aUBLOrderRef.getIDValue () != null || aUBLOrderRef.getSalesOrderIDValue () != null)
        aUBLInvoice.setOrderReference (aUBLOrderRef);
    }

    // BillingReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aSettlement.getInvoiceReferencedDocument ());
      if (aUBLDocRef != null)
      {
        final BillingReferenceType aUBLBillingRef = new BillingReferenceType ();
        aUBLBillingRef.setInvoiceDocumentReference (aUBLDocRef);
        aUBLInvoice.addBillingReference (aUBLBillingRef);
      }
    }

    // DespatchDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aDelivery.getDespatchAdviceReferencedDocument ());
      if (aUBLDocRef != null)
        aUBLInvoice.addDespatchDocumentReference (aUBLDocRef);
    }

    // ReceiptDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aDelivery.getReceivingAdviceReferencedDocument ());
      if (aUBLDocRef != null)
        aUBLInvoice.addReceiptDocumentReference (aUBLDocRef);
    }

    // OriginatorDocumentReference
    {
      for (final ReferencedDocumentType aRD : aAgreement.getAdditionalReferencedDocument ())
      {
        // Use for "Tender or lot reference" with TypeCode "50"
        if ("50".equals (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD);
          if (aUBLDocRef != null)
            aUBLInvoice.addOriginatorDocumentReference (aUBLDocRef);
        }
      }
    }

    // ContractDocumentReference
    {
      final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aAgreement.getContractReferencedDocument ());
      if (aUBLDocRef != null)
        aUBLInvoice.addContractDocumentReference (aUBLDocRef);
    }

    // AdditionalDocumentReference
    {
      for (final ReferencedDocumentType aRD : aAgreement.getAdditionalReferencedDocument ())
      {
        // Except OriginatorDocumentReference
        if (!"50".equals (aRD.getTypeCodeValue ()))
        {
          final DocumentReferenceType aUBLDocRef = _convertDocumentReference (aRD);
          if (aUBLDocRef != null)
            aUBLInvoice.addAdditionalDocumentReference (aUBLDocRef);
        }
      }
    }

    // ProjectReference
    {
      final ProcuringProjectType aSpecifiedProcuring = aAgreement.getSpecifiedProcuringProject ();
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
      final TradePartyType aSellerParty = aAgreement.getSellerTradeParty ();
      if (aSellerParty != null)
      {
        aUBLSupplier.setParty (_convertParty (aSellerParty));
      }
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
