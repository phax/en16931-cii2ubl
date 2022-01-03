/*
 * Copyright (C) 2019-2022 Philip Helger
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

/**
 * UBL file type creation mode.
 *
 * @author Philip Helger
 * @since 1.1.5
 */
public enum EUBLCreationMode
{
  /** Automatic determination */
  AUTOMATIC,
  /** Force to use Invoice */
  INVOICE,
  /** Force to use CreditNote */
  CREDIT_NOTE;
}
