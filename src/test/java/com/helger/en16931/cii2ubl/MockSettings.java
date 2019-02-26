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

import com.helger.bdve.en16931.EN16931Validation;
import com.helger.bdve.executorset.ValidationExecutorSetRegistry;

final class MockSettings
{
  static final String [] TEST_FILES = new String [] { "CII_business_example_01.xml",
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
  static final ValidationExecutorSetRegistry VES_REGISTRY = new ValidationExecutorSetRegistry ();
  static
  {
    EN16931Validation.initEN16931 (VES_REGISTRY);
  }

}
