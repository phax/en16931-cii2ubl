package com.helger.en16931.cii2ubl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.helger.commons.error.list.ErrorList;

/**
 * Test class for class {@link CIIToUBLConverter}.
 *
 * @author Philip Helger
 */
public final class CIIToUBLConverterTest
{
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
      final ErrorList aErrorList = new ErrorList ();
      assertNotNull (new CIIToUBLConverter ().convertCIItoUBL (new File ("src/test/resources/cii", sFilename),
                                                               aErrorList));
      assertTrue (aErrorList.isEmpty ());
    }
  }
}
