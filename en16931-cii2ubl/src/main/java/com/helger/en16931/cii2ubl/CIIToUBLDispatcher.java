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

import java.io.File;
import java.io.Serializable;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.enforce.ValueEnforcer;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.cii2ubl.en2017.CIID16BToUBL21Converter;
import com.helger.en16931.cii2ubl.en2026.CIID25AToUBL25Converter;

/**
 * Converts a CII document to UBL, choosing the EN 16931 edition automatically. The edition is taken
 * from BT-24 (Specification identifier), see {@link EEN16931Edition}. An explicit edition can be
 * set with {@link #setEdition(EEN16931Edition)}, which skips the detection.<br>
 * The converter specific settings of this class are handed over to whichever converter is used.
 *
 * @author Philip Helger
 * @since 4.0.0
 */
public class CIIToUBLDispatcher extends AbstractCIIToUBLConverterBase <CIIToUBLDispatcher>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLDispatcher.class);

  private EEN16931Edition m_eEdition;

  public CIIToUBLDispatcher ()
  {}

  /**
   * @return The EN 16931 edition to use, or <code>null</code> if it is detected per document. The
   *         default is <code>null</code>.
   */
  @Nullable
  public final EEN16931Edition getEdition ()
  {
    return m_eEdition;
  }

  /**
   * Force a specific EN 16931 edition instead of detecting it from BT-24.
   *
   * @param eEdition
   *        The edition to use. May be <code>null</code> to detect it per document.
   * @return this for chaining
   */
  @NonNull
  public final CIIToUBLDispatcher setEdition (@Nullable final EEN16931Edition eEdition)
  {
    m_eEdition = eEdition;
    return thisAsT ();
  }

  /**
   * Create the converter for the provided edition, initialised with the settings of this
   * dispatcher.
   *
   * @param eEdition
   *        The edition to create a converter for. May not be <code>null</code>.
   * @return The matching converter. Never <code>null</code>.
   */
  @NonNull
  public AbstractCIIToUBLConverterBase <?> createConverter (@NonNull final EEN16931Edition eEdition)
  {
    ValueEnforcer.notNull (eEdition, "Edition");

    final AbstractCIIToUBLConverterBase <?> ret = switch (eEdition)
    {
      case EN2017 -> new CIID16BToUBL21Converter ();
      case EN2026 -> new CIID25AToUBL25Converter ();
    };
    ret.setUBLCreationMode (getUBLCreationMode ())
       .setVATScheme (getVATScheme ())
       .setCardAccountNetworkID (getCardAccountNetworkID ())
       .setDefaultOrderRefID (getDefaultOrderRefID ())
       .setSwapQuantitySignIfNeeded (isSwapQuantitySignIfNeeded ())
       .setSwapPriceSignIfNeeded (isSwapPriceSignIfNeeded ());
    if (getCustomizationID () != null)
      ret.setCustomizationID (getCustomizationID ());
    if (getProfileID () != null)
      ret.setProfileID (getProfileID ());
    return ret;
  }

  @Override
  @Nullable
  public Serializable convertCIItoUBL (@NonNull final File aFile, @NonNull final ErrorList aErrorList)
  {
    ValueEnforcer.notNull (aFile, "File");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    EEN16931Edition eEdition = m_eEdition;
    if (eEdition == null)
    {
      // Peek at BT-24 - the correct JAXB model is exactly what is not known yet
      final String sSpecID = EEN16931Edition.getSpecificationIdentifier (aFile);
      eEdition = EEN16931Edition.getFromSpecificationIdentifierOrNull (sSpecID);
      if (eEdition == null)
      {
        aErrorList.add (buildError (null,
                                    sSpecID == null ? "Cannot determine the EN 16931 edition, because BT-24 (Specification identifier) is missing. Set the edition explicitly."
                                                    : "Cannot determine the EN 16931 edition from the BT-24 value '" +
                                                      sSpecID +
                                                      "'. Set the edition explicitly."));
        return null;
      }
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Detected EN 16931 edition " + eEdition.getID () + " for '" + aFile.getAbsolutePath () + "'");
    }

    return createConverter (eEdition).convertCIItoUBL (aFile, aErrorList);
  }
}
