/**
 * Copyright (C) 2019-2021 Philip Helger
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
package com.helger.en16931.cii2ubl.cli;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.IError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.state.ESuccess;
import com.helger.en16931.cii2ubl.AbstractCIIToUBLConverter;
import com.helger.en16931.cii2ubl.CIIToUBL21Converter;
import com.helger.en16931.cii2ubl.CIIToUBL22Converter;
import com.helger.en16931.cii2ubl.CIIToUBL23Converter;
import com.helger.en16931.cii2ubl.CIIToUBLVersion;
import com.helger.en16931.cii2ubl.EUBLCreationMode;
import com.helger.ubl21.UBL21Writer;
import com.helger.ubl22.UBL22Writer;
import com.helger.ubl23.UBL23Writer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main command line client
 */
@Command (description = "CII to UBL Converter for EN 16931 invoices", name = "CIItoUBLConverter", mixinStandardHelpOptions = true, separator = " ")
public class CIIToUBLConverter implements Callable <Integer>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverter.class);

  @Option (names = "--ubl", paramLabel = "version", defaultValue = "2.1", description = "Version of the target UBL Format: '2.1', '2.2' or '2.3' (default: ${DEFAULT-VALUE})")
  private String m_sUBLVersion;

  @Option (names = "--mode", paramLabel = "mode", defaultValue = "INVOICE", description = "Allowed values: ${COMPLETION-CANDIDATES}")
  private EUBLCreationMode m_eMode;

  @Option (names = { "-t",
                     "--target" }, paramLabel = "directory", defaultValue = ".", description = "The target directory for result output (default: ${DEFAULT-VALUE})")
  private String m_sOutputDir;

  @Option (names = "--ubl-vatscheme", paramLabel = "vat scheme", defaultValue = AbstractCIIToUBLConverter.DEFAULT_VAT_SCHEME, description = "The UBL VAT scheme to be used (default: ${DEFAULT-VALUE})")
  private String m_sVATScheme;

  @Option (names = "--ubl-customizationid", paramLabel = "ID", defaultValue = AbstractCIIToUBLConverter.DEFAULT_CUSTOMIZATION_ID, description = "The UBL customization ID to be used (default: ${DEFAULT-VALUE})")
  private String m_sCustomizationID;

  @Option (names = "--ubl-profileid", paramLabel = "ID", defaultValue = AbstractCIIToUBLConverter.DEFAULT_PROFILE_ID, description = "The UBL profile ID to be used (default: ${DEFAULT-VALUE})")
  private String m_sProfileID;

  @Option (names = "--ubl-cardaccountnetworkid", paramLabel = "ID", defaultValue = AbstractCIIToUBLConverter.DEFAULT_CARD_ACCOUNT_NETWORK_ID, description = "The UBL CardAccount network ID to be used (default: ${DEFAULT-VALUE})")
  private String m_sCardAccountNetworkID;

  @Parameters (arity = "1..*", paramLabel = "source files", description = "One or more CII file(s)")
  private List <File> m_aSourceFiles;

  private static String _normalizeOutputDirectory (final String dir)
  {
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("CLI option output directory=" + dir);
    final String ret = Paths.get (dir).toAbsolutePath ().normalize ().toString ();
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Normalized output directory=" + ret);
    return ret;
  }

  @Nonnull
  private static File _normalizeFile (@Nonnull final Path path)
  {
    return path.toAbsolutePath ().normalize ().toFile ();
  }

  @Nonnull
  private ICommonsList <File> _normalizeInputFiles (@Nonnull final List <File> files) throws IOException
  {
    final ICommonsList <File> ret = new CommonsArrayList <> ();
    for (final File file : files)
    {
      if (file.isDirectory ())
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Is a directory=" + file.toString ());
        // collecting readable and normalized absolute path files
        ret.addAll (Files.walk (file.toPath ())
                         .filter (p -> Files.isReadable (p) && !Files.isDirectory (p))
                         .map (p -> _normalizeFile (p))
                         .peek (f -> LOGGER.debug ("Add file={}", f.toString ()))
                         .collect (Collectors.toList ()));
      }
      else
        if (file.canRead ())
        {
          if (LOGGER.isDebugEnabled ())
            LOGGER.debug ("Is a file={}", file.toString ());
          ret.add (_normalizeFile (file.toPath ()));
        }
    }
    return ret;

  }

  // doing the business
  public Integer call () throws Exception
  {
    m_sOutputDir = _normalizeOutputDirectory (m_sOutputDir);
    m_aSourceFiles = _normalizeInputFiles (m_aSourceFiles);

    final AbstractCIIToUBLConverter <?> aConverter;
    if ("2.1".equals (m_sUBLVersion))
      aConverter = new CIIToUBL21Converter ();
    else
      if ("2.2".equals (m_sUBLVersion))
        aConverter = new CIIToUBL22Converter ();
      else
        if ("2.3".equals (m_sUBLVersion))
          aConverter = new CIIToUBL23Converter ();
        else
          throw new IllegalStateException ("Unsupported UBL version '" + m_sUBLVersion + "' provided.");

    aConverter.setUBLCreationMode (m_eMode)
              .setVATScheme (m_sVATScheme)
              .setCustomizationID (m_sCustomizationID)
              .setProfileID (m_sProfileID)
              .setCardAccountNetworkID (m_sCardAccountNetworkID);

    final Locale aErrorLocale = Locale.US;
    for (final File f : m_aSourceFiles)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Converting file=" + f.getAbsolutePath ());

      final File aDestFile = new File (m_sOutputDir, FilenameHelper.getBaseName (f) + "-ubl.xml");

      // TODO switch between versions
      final ErrorList aErrorList = new ErrorList ();
      final Serializable aUBL = aConverter.convertCIItoUBL (f, aErrorList);
      if (aErrorList.containsAtLeastOneError () || aUBL == null)
      {
        LOGGER.error ("Failed to convert CII file '" + f.getAbsolutePath () + "' to UBL:");
        for (final IError aError : aErrorList)
          LOGGER.error (aError.getAsString (aErrorLocale));
      }
      else
      {
        final boolean bFormattedOutput = true;
        final ESuccess eSuccess;
        if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType)
        {
          eSuccess = UBL21Writer.invoice ()
                                .setFormattedOutput (bFormattedOutput)
                                .write ((oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType) aUBL,
                                        aDestFile);
        }
        else
          if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType)
          {
            eSuccess = UBL21Writer.creditNote ()
                                  .setFormattedOutput (bFormattedOutput)
                                  .write ((oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType) aUBL,
                                          aDestFile);
          }
          else
            if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType)
            {
              eSuccess = UBL22Writer.invoice ()
                                    .setFormattedOutput (bFormattedOutput)
                                    .write ((oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType) aUBL,
                                            aDestFile);
            }
            else
              if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType)
              {
                eSuccess = UBL22Writer.creditNote ()
                                      .setFormattedOutput (bFormattedOutput)
                                      .write ((oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType) aUBL,
                                              aDestFile);
              }
              else
                if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_23.InvoiceType)
                {
                  eSuccess = UBL23Writer.invoice ()
                                        .setFormattedOutput (bFormattedOutput)
                                        .write ((oasis.names.specification.ubl.schema.xsd.invoice_23.InvoiceType) aUBL,
                                                aDestFile);
                }
                else
                  if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_23.CreditNoteType)
                  {
                    eSuccess = UBL23Writer.creditNote ()
                                          .setFormattedOutput (bFormattedOutput)
                                          .write ((oasis.names.specification.ubl.schema.xsd.creditnote_23.CreditNoteType) aUBL,
                                                  aDestFile);
                  }
                  else
                    throw new IllegalStateException ("Unsupported UBL version '" + m_sUBLVersion + "'");

        if (eSuccess.isSuccess ())
          LOGGER.info ("Successfully wrote UBL file " + aDestFile.getAbsolutePath ());
        else
          LOGGER.error ("Failed to write UBL file " + aDestFile.getAbsolutePath ());
      }
    }

    return Integer.valueOf (0);
  }

  public static void main (final String [] aArgs)
  {
    LOGGER.info ("CII to UBL Converter v" +
                 CIIToUBLVersion.BUILD_VERSION +
                 " (build " +
                 CIIToUBLVersion.BUILD_TIMESTAMP +
                 ")");
    final CommandLine cmd = new CommandLine (new CIIToUBLConverter ());
    cmd.setCaseInsensitiveEnumValuesAllowed (true);
    final int nExitCode = cmd.execute (aArgs);
    System.exit (nExitCode);
  }
}
