/*
 * Copyright (C) 2019-2024 Philip Helger
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

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.phive.api.executorset.ValidationExecutorSetRegistry;
import com.helger.phive.en16931.EN16931Validation;
import com.helger.phive.xml.source.IValidationSourceXML;

final class MockSettings
{
  public static final String BASE_TEST_DIR = "src/test/resources/external/cii/";
  private static final String [] TEST_FILES_EN16931 = { "CII_business_example_01.xml",
                                                        "CII_business_example_02.xml",
                                                        "CII_example1.xml",
                                                        "CII_example1a.xml",
                                                        "CII_example2.xml",
                                                        "CII_example3.xml",
                                                        "CII_example4.xml",
                                                        "CII_example5.xml",
                                                        "CII_example6.xml",
                                                        "CII_example7.xml",
                                                        "CII_example8.xml",
                                                        "CII_example9.xml" };
  private static final String [] TEST_FILES_ISSUES = { "issue7.xml", "issue12.xml", "issue20.xml", "issue22.xml" };
  private static final String [] TEST_FILES_XRECHNUNG_122 = { "01.01a-INVOICE_uncefact.xml",
                                                              "01.02a-INVOICE_uncefact.xml",
                                                              "01.03a-INVOICE_uncefact.xml",
                                                              "01.04a-INVOICE_uncefact.xml",
                                                              "01.05a-INVOICE_uncefact.xml",
                                                              "01.06a-INVOICE_uncefact.xml",
                                                              "01.07a-INVOICE_uncefact.xml",
                                                              "01.08a-INVOICE_uncefact.xml",
                                                              "01.09a-INVOICE_uncefact.xml",
                                                              "01.10a-INVOICE_uncefact.xml",
                                                              "01.11a-INVOICE_uncefact.xml",
                                                              "01.12a-INVOICE_uncefact.xml",
                                                              "01.13a-INVOICE_uncefact.xml",
                                                              "01.14a-INVOICE_uncefact.xml",
                                                              "01.15a-INVOICE_uncefact.xml",
                                                              "02.01a-INVOICE_uncefact.xml",
                                                              "02.02a-INVOICE_uncefact.xml",
                                                              "02.03a-INVOICE_uncefact.xml",
                                                              "02.04a-INVOICE_uncefact.xml",
                                                              // "03.01a-INVOICE_uncefact.xml",
                                                              "03.02a-INVOICE_uncefact.xml" };
  private static final String [] TEST_FILES_XRECHNUNG_200 = { "01.01a-INVOICE_uncefact.xml",
                                                              "01.02a-INVOICE_uncefact.xml",
                                                              "01.03a-INVOICE_uncefact.xml",
                                                              "01.04a-INVOICE_uncefact.xml",
                                                              "01.05a-INVOICE_uncefact.xml",
                                                              "01.06a-INVOICE_uncefact.xml",
                                                              "01.07a-INVOICE_uncefact.xml",
                                                              "01.08a-INVOICE_uncefact.xml",
                                                              "01.09a-INVOICE_uncefact.xml",
                                                              "01.10a-INVOICE_uncefact.xml",
                                                              "01.11a-INVOICE_uncefact.xml",
                                                              "01.12a-INVOICE_uncefact.xml",
                                                              "01.13a-INVOICE_uncefact.xml",
                                                              "01.14a-INVOICE_uncefact.xml",
                                                              "01.15a-INVOICE_uncefact.xml",
                                                              "02.01a-INVOICE_uncefact.xml",
                                                              "02.02a-INVOICE_uncefact.xml",
                                                              // "03.01a-INVOICE_uncefact.xml",
                                                              "03.02a-INVOICE_uncefact.xml" };

  static final ValidationExecutorSetRegistry <IValidationSourceXML> VES_REGISTRY = new ValidationExecutorSetRegistry <> ();
  static
  {
    EN16931Validation.initEN16931 (VES_REGISTRY);
  }

  @Nonnull
  public static File getBaseDir ()
  {
    return new File (BASE_TEST_DIR);
  }

  @Nonnull
  @Nonempty
  @ReturnsMutableCopy
  public static ICommonsList <File> getAllTestFiles ()
  {
    final ICommonsList <File> ret = new CommonsArrayList <> ();
    for (final String sFile : TEST_FILES_EN16931)
      ret.add (new File (BASE_TEST_DIR, sFile));
    for (final String sFile : TEST_FILES_ISSUES)
      ret.add (new File (BASE_TEST_DIR + "issues", sFile));
    for (final String sFile : TEST_FILES_XRECHNUNG_122)
      ret.add (new File (BASE_TEST_DIR + "xrechnung/1.2.2", sFile));
    for (final String sFile : TEST_FILES_XRECHNUNG_200)
      ret.add (new File (BASE_TEST_DIR + "xrechnung/2.0.0", sFile));
    return ret;
  }
}
