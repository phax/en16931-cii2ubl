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
import com.helger.datetime.util.PDTXMLConverter;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType;
import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ExchangedDocumentContextType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.ExchangedDocumentType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeAgreementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeDeliveryType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.HeaderTradeSettlementType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.SupplyChainTradeTransactionType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradePaymentTermsType;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._100.TradeSettlementHeaderMonetarySummationType;
import un.unece.uncefact.data.standard.unqualifieddatatype._100.AmountType;

public class CIIToUBLConverter
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverter.class);

  public CIIToUBLConverter ()
  {}

  @Nullable
  private static XMLGregorianCalendar _parseDateDDMMYYYY (@Nullable final String s)
  {
    final LocalDate aDate = PDTFromString.getLocalDateFromString (s, "uuMMdddd");
    return PDTXMLConverter.getXMLCalendarDate (aDate);
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
