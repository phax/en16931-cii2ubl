package com.helger.en16931.cii2ubl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.Serializable;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.error.list.ErrorList;
import com.helger.ubl21.UBL21Writer;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;

/**
 * Test class for class {@link CIIToUBLConverter}.
 *
 * @author Philip Helger
 */
public final class CIIToUBLConverterTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverterTest.class);
  private static final String [] TEST_FILES = new String [] { "CII_business_example_01.xml",
                                                              "CII_business_example_02.xml",
                                                              "CII_example1.xml",
                                                              "CII_example2.xml",
                                                              "CII_example3.xml",
                                                              "CII_example4.xml",
                                                              "CII_example5.xml",
                                                              "CII_example6.xml",
                                                              "CII_example7.xml",
                                                              "CII_example8.xml",
                                                              "CII_example9.xml" };

  @Test
  public void testConvertAll ()
  {
    for (final String sFilename : TEST_FILES)
    {
      LOGGER.info (sFilename);

      final ErrorList aErrorList = new ErrorList ();
      final Serializable aInvoice = new CIIToUBLConverter ().convertCIItoUBL (new File ("src/test/resources/cii",
                                                                                        sFilename),
                                                                              aErrorList);
      assertTrue ("Errors: " + aErrorList.toString (), aErrorList.isEmpty ());
      assertNotNull (aInvoice);
      assertTrue (aInvoice instanceof InvoiceType);
      final InvoiceType aUBLInvoice = (InvoiceType) aInvoice;
      final String s = UBL21Writer.invoice ().getAsString (aUBLInvoice);
      assertNotNull (s);
      LOGGER.info ("  UBL:\n" + s);
    }
  }
}
