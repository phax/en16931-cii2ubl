/*
 * Copyright (C) 2019-2025 Philip Helger
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
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.base.state.ESuccess;
import com.helger.collection.commons.CommonsArrayList;
import com.helger.collection.commons.ICommonsList;
import com.helger.diagnostics.error.IError;
import com.helger.diagnostics.error.list.ErrorList;
import com.helger.en16931.cii2ubl.AbstractCIIToUBLConverter;
import com.helger.en16931.cii2ubl.CIIToUBL21Converter;
import com.helger.en16931.cii2ubl.CIIToUBL22Converter;
import com.helger.en16931.cii2ubl.CIIToUBL23Converter;
import com.helger.en16931.cii2ubl.CIIToUBL24Converter;
import com.helger.en16931.cii2ubl.CIIToUBLVersion;
import com.helger.en16931.cii2ubl.EUBLCreationMode;
import com.helger.io.file.FileSystemIterator;
import com.helger.io.file.FileSystemRecursiveIterator;
import com.helger.io.file.FilenameHelper;
import com.helger.ubl21.UBL21Marshaller;
import com.helger.ubl22.UBL22Marshaller;
import com.helger.ubl23.UBL23Marshaller;
import com.helger.ubl24.UBL24Marshaller;

import jakarta.annotation.Nonnull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main command line client
 *
 * @author Philip Helger
 */
@Command (description = "CII to UBL Converter for EN 16931 invoices",
          name = "CIItoUBLConverter",
          mixinStandardHelpOptions = true,
          separator = " ")
public class CIIToUBLConverter implements Callable <Integer>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverter.class);

  @Option (names = "--ubl",
           paramLabel = "version",
           defaultValue = "2.1",
           description = "Version of the target UBL Format: '2.1', '2.2', '2.3' or '2.4' (default: '${DEFAULT-VALUE}')")
  private String m_sUBLVersion;

  @Option (names = "--mode",
           paramLabel = "mode",
           defaultValue = "AUTOMATIC",
           description = "Allowed values: ${COMPLETION-CANDIDATES} (default: '${DEFAULT-VALUE}')")
  private EUBLCreationMode m_eMode;

  @Option (names = { "-t", "--target" },
           paramLabel = "directory",
           defaultValue = ".",
           description = "The target directory for result output (default: '${DEFAULT-VALUE}')")
  private String m_sOutputDir;

  @Option (names = "--output-suffix",
           paramLabel = "filename part",
           defaultValue = "-ubl",
           description = "The suffix added to the output filename (default: '${DEFAULT-VALUE}')")
  private String m_sOutputFileSuffix;

  @Option (names = "--ubl-vatscheme",
           paramLabel = "vat scheme",
           defaultValue = AbstractCIIToUBLConverter.DEFAULT_VAT_SCHEME,
           description = "The UBL VAT scheme to be used (default: '${DEFAULT-VALUE}')")
  private String m_sVATScheme;

  @Option (names = "--ubl-customizationid", paramLabel = "ID", description = "The UBL customization ID to be used")
  private String m_sCustomizationID;

  @Option (names = "--ubl-profileid", paramLabel = "ID", description = "The UBL profile ID to be used")
  private String m_sProfileID;

  @Option (names = "--ubl-cardaccountnetworkid",
           paramLabel = "ID",
           defaultValue = AbstractCIIToUBLConverter.DEFAULT_CARD_ACCOUNT_NETWORK_ID,
           description = "The UBL CardAccount network ID to be used (default: '${DEFAULT-VALUE}')")
  private String m_sCardAccountNetworkID;

  @Option (names = "--ubl-defaultorderrefid",
           paramLabel = "ID",
           defaultValue = AbstractCIIToUBLConverter.DEFAULT_ORDER_REF_ID,
           description = "The UBL default order reference ID to be used (default: '${DEFAULT-VALUE}')")
  private String m_sDefaultOrderRefID;

  @Option (names = "--verbose",
           paramLabel = "boolean",
           defaultValue = "false",
           description = "Enable debug logging (default: '${DEFAULT-VALUE}')")
  private boolean m_bVerbose;

  @Option (names = "--disable-wildcard-expansion",
           paramLabel = "boolean",
           defaultValue = "false",
           description = "Disable wildcard expansion of filenames")
  private boolean m_bDisableWildcardExpansion;

  @Parameters (arity = "1..*", paramLabel = "source files", description = "One or more CII file(s)")
  private List <String> m_aSourceFilenames;

  private void _verboseLog (@Nonnull final Supplier <String> aSupplier)
  {
    if (m_bVerbose)
      LOGGER.info (aSupplier.get ());
  }

  @Nonnull
  private String _normalizeOutputDirectory (@Nonnull final String sDirectory)
  {
    _verboseLog ( () -> "CLI option UBL output directory '" + sDirectory + "'");
    final String ret = Paths.get (sDirectory).toAbsolutePath ().normalize ().toString ();
    if (!sDirectory.equals (ret))
      _verboseLog ( () -> "Normalized UBL output directory '" + ret + "'");
    return ret;
  }

  @Nonnull
  private static File _normalizeFile (@Nonnull final Path aPath)
  {
    return aPath.toAbsolutePath ().normalize ().toFile ();
  }

  @Nonnull
  private ICommonsList <File> _resolveWildcards (@Nonnull final List <String> aFilenames) throws IOException
  {
    final ICommonsList <File> ret = new CommonsArrayList <> (aFilenames.size ());

    final File aRootDir = new File (".").getCanonicalFile ();
    for (final String sFilename : aFilenames)
    {
      if (sFilename.indexOf ('*') >= 0 ||
          sFilename.indexOf ('?') >= 0 ||
          (sFilename.indexOf ('[') >= 0 && sFilename.indexOf (']') >= 0))
      {
        // Make search pattern absolute
        final String sRealName = new File (sFilename).getAbsolutePath ();
        _verboseLog ( () -> "Trying to resolve wildcards for '" + sRealName + "'");
        final PathMatcher matcher = FileSystems.getDefault ().getPathMatcher ("glob:" + sRealName);
        for (final File f : new FileSystemRecursiveIterator (aRootDir))
        {
          if (matcher.matches (f.toPath ()))
          {
            _verboseLog ( () -> "  Found wildcard match '" + f + "'");
            ret.add (f);
          }
        }
      }
      else
        ret.add (new File (sFilename));
    }
    return ret;
  }

  @Nonnull
  private ICommonsList <File> _normalizeInputFiles (@Nonnull final List <String> aFilenames) throws IOException
  {
    final ICommonsList <File> aFiles;
    if (m_bDisableWildcardExpansion)
    {
      aFiles = new CommonsArrayList <> (aFilenames, File::new);
      _verboseLog ( () -> "Using the input files '" + aFiles + "'");
    }
    else
    {
      _verboseLog ( () -> "Normalizing the input files '" + aFilenames + "'");
      aFiles = _resolveWildcards (aFilenames);
      _verboseLog ( () -> "Resolved wildcards of input files to '" + aFiles + "'");
    }

    final ICommonsList <File> ret = new CommonsArrayList <> ();

    for (final File aFile : aFiles)
    {
      if (aFile.isDirectory ())
      {
        _verboseLog ( () -> "Input '" + aFile.toString () + "' is a Directory");
        // collecting readable and normalized absolute path files
        for (final File aChildFile : new FileSystemIterator (aFile))
        {
          final Path p = aChildFile.toPath ();
          if (Files.isReadable (p) && !Files.isDirectory (p))
          {
            ret.add (_normalizeFile (p));
            _verboseLog ( () -> "Added file '" + ret.getLastOrNull ().toString () + "'");
          }
        }
      }
      else
        // Does not need to be file - only needs to be readable
        if (aFile.canRead ())
        {
          _verboseLog ( () -> "Input '" + aFile.toString () + "' is a readable File");
          ret.add (_normalizeFile (aFile.toPath ()));
        }
        else
          LOGGER.warn ("Ignoring non-existing file " + aFile.getAbsolutePath ());
    }

    _verboseLog ( () -> "Converting the following CII files: " + ret.getAllMapped (File::getAbsolutePath));
    return ret;
  }

  private static void _log (@Nonnull final IError aError)
  {
    final String sMsg = "  " + aError.getAsString (Locale.US);
    if (aError.isError ())
      LOGGER.error (sMsg);
    else
      if (aError.isFailure ())
        LOGGER.warn (sMsg);
      else
        LOGGER.info (sMsg);
  }

  // doing the business
  public Integer call () throws Exception
  {
    if (m_bVerbose)
      System.setProperty ("org.slf4j.simpleLogger.defaultLogLevel", "debug");

    m_sOutputDir = _normalizeOutputDirectory (m_sOutputDir);
    final List <File> m_aSourceFiles = _normalizeInputFiles (m_aSourceFilenames);

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
          if ("2.4".equals (m_sUBLVersion))
            aConverter = new CIIToUBL24Converter ();
          else
            throw new IllegalStateException ("Unsupported UBL version '" + m_sUBLVersion + "' provided.");

    aConverter.setUBLCreationMode (m_eMode)
              .setVATScheme (m_sVATScheme)
              .setCustomizationID (m_sCustomizationID)
              .setProfileID (m_sProfileID)
              .setCardAccountNetworkID (m_sCardAccountNetworkID)
              .setDefaultOrderRefID (m_sDefaultOrderRefID);

    for (final File f : m_aSourceFiles)
    {
      final File aDestFile = new File (m_sOutputDir, FilenameHelper.getBaseName (f) + m_sOutputFileSuffix + ".xml");

      LOGGER.info ("Converting CII file '" + f.getAbsolutePath () + "' to UBL");

      // Perform the main conversion
      final ErrorList aErrorList = new ErrorList ();
      final Serializable aUBL = aConverter.convertCIItoUBL (f, aErrorList);
      if (aErrorList.containsAtLeastOneError () || aUBL == null)
      {
        LOGGER.error ("Failed to convert CII file '" + f.getAbsolutePath () + "' to UBL:");
        for (final IError aError : aErrorList)
          _log (aError);
      }
      else
      {
        for (final IError aError : aErrorList)
          _log (aError);

        final boolean bFormattedOutput = true;
        final ESuccess eSuccess;
        if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType)
        {
          eSuccess = UBL21Marshaller.invoice ()
                                    .setFormattedOutput (bFormattedOutput)
                                    .write ((oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType) aUBL,
                                            aDestFile);
        }
        else
          if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType)
          {
            eSuccess = UBL21Marshaller.creditNote ()
                                      .setFormattedOutput (bFormattedOutput)
                                      .write ((oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType) aUBL,
                                              aDestFile);
          }
          else
            if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType)
            {
              eSuccess = UBL22Marshaller.invoice ()
                                        .setFormattedOutput (bFormattedOutput)
                                        .write ((oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType) aUBL,
                                                aDestFile);
            }
            else
              if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType)
              {
                eSuccess = UBL22Marshaller.creditNote ()
                                          .setFormattedOutput (bFormattedOutput)
                                          .write ((oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType) aUBL,
                                                  aDestFile);
              }
              else
                if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_23.InvoiceType)
                {
                  eSuccess = UBL23Marshaller.invoice ()
                                            .setFormattedOutput (bFormattedOutput)
                                            .write ((oasis.names.specification.ubl.schema.xsd.invoice_23.InvoiceType) aUBL,
                                                    aDestFile);
                }
                else
                  if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_23.CreditNoteType)
                  {
                    eSuccess = UBL23Marshaller.creditNote ()
                                              .setFormattedOutput (bFormattedOutput)
                                              .write ((oasis.names.specification.ubl.schema.xsd.creditnote_23.CreditNoteType) aUBL,
                                                      aDestFile);
                  }
                  else
                    if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_24.InvoiceType)
                    {
                      eSuccess = UBL24Marshaller.invoice ()
                                                .setFormattedOutput (bFormattedOutput)
                                                .write ((oasis.names.specification.ubl.schema.xsd.invoice_24.InvoiceType) aUBL,
                                                        aDestFile);
                    }
                    else
                      if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_24.CreditNoteType)
                      {
                        eSuccess = UBL24Marshaller.creditNote ()
                                                  .setFormattedOutput (bFormattedOutput)
                                                  .write ((oasis.names.specification.ubl.schema.xsd.creditnote_24.CreditNoteType) aUBL,
                                                          aDestFile);
                      }
                      else
                        throw new IllegalStateException ("Unsupported UBL version '" + m_sUBLVersion + "'");

        if (eSuccess.isSuccess ())
          LOGGER.info ("Successfully wrote UBL file '" + aDestFile.getAbsolutePath () + "'");
        else
          LOGGER.error ("Failed to write UBL file '" + aDestFile.getAbsolutePath () + "'");
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
