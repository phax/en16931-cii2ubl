package com.helger.en16931.cii2ubl;

import java.io.File;

import javax.annotation.Nonnull;

import com.helger.cii.d16b.CIID16BReader;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.state.ESuccess;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType;

public class CIIToUBLConverter
{
  private boolean m_bAlwaysCreateInvoice = true;

  public CIIToUBLConverter ()
  {}

  public boolean isAlwaysCreateInvoice ()
  {
    return m_bAlwaysCreateInvoice;
  }

  @Nonnull
  public CIIToUBLConverter setAlwaysCreateInvoice (final boolean bAlwaysCreateInvoice)
  {
    m_bAlwaysCreateInvoice = bAlwaysCreateInvoice;
    return this;
  }

  @Nonnull
  public ESuccess convertCIItoUBL (@Nonnull final File aFile, @Nonnull final ErrorList aErrorList)
  {
    final CrossIndustryInvoiceType aInvoice = CIID16BReader.crossIndustryInvoice ()
                                                           .setValidationEventHandler (new WrappedCollectingValidationEventHandler (aErrorList))
                                                           .read (aFile);
    if (aInvoice == null)
      return ESuccess.FAILURE;

    // TODO

    return ESuccess.SUCCESS;
  }
}
