package com.helger.en16931.cii2ubl.cli;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.error.IError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.en16931.cii2ubl.AbstractCIIToUBLConverter;
import com.helger.en16931.cii2ubl.CIIToUBL21Converter;
import com.helger.en16931.cii2ubl.CIIToUBL22Converter;
import com.helger.en16931.cii2ubl.EUBLCreationMode;
import com.helger.ubl21.UBL21Writer;
import com.helger.ubl22.UBL22Writer;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main command line client
 */
@Command (description = "CII to UBL Converter.", name = "CIItoUBLConverter", mixinStandardHelpOptions = true, separator = " ")
public class CIIToUBLConverter implements Callable <Integer>
{
  private static final Logger LOGGER = LoggerFactory.getLogger (CIIToUBLConverter.class);

  @Option (names = "--ubl", defaultValue = "2.1", description = "Version of the target UBL Format (default: ${DEFAULT-VALUE})")
  private String m_sUBLVersion;

  @Option (names = "--mode", defaultValue = "INVOICE", description = "Allowedvalues:${COMPLETION-CANDIDATES}")
  private EUBLCreationMode m_eMode;

  @Option (names = { "-t",
                     "--target" }, paramLabel = "out", defaultValue = ".", description = "The target directory for result output (default: ${DEFAULT-VALUE})")
  private String m_sOutputDir;

  @Option (names = "--ubl-vatscheme", defaultValue = AbstractCIIToUBLConverter.DEFAULT_VAT_SCHEME, description = "The UBL VAT scheme to be used (default: ${DEFAULT-VALUE})")
  private String m_sVATScheme;

  @Option (names = "--ubl-customizationid", defaultValue = AbstractCIIToUBLConverter.DEFAULT_CUSTOMIZATION_ID, description = "The UBL customization ID to be used (default: ${DEFAULT-VALUE})")
  private String m_sCustomizationID;

  @Option (names = "--ubl-profileid", defaultValue = AbstractCIIToUBLConverter.DEFAULT_PROFILE_ID, description = "The UBL profile ID to be used (default: ${DEFAULT-VALUE})")
  private String m_sProfileID;

  @Option (names = "--ubl-cardaccountnetworkid", defaultValue = AbstractCIIToUBLConverter.DEFAULT_CARD_ACCOUNT_NETWORK_ID, description = "The UBL CardAccount network ID to be used (default: ${DEFAULT-VALUE})")
  private String m_sCardAccountNetworkID;

  @Parameters (arity = "1..*", description = "One or more Files")
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
  private List <File> _normalizeInputFiles (@Nonnull final List <File> files) throws IOException
  {
    final List <File> ret = new ArrayList <> ();

    List <File> dirFiles = new ArrayList <> ();
    for (final File file : files)
    {
      if (file.isDirectory ())
      {
        if (LOGGER.isDebugEnabled ())
          LOGGER.debug ("Is a directory=" + file.toString ());
        // collecting readable and normalized absolute path files
        dirFiles = Files.walk (file.toPath ())
                        .filter (p -> Files.isReadable (p) && !Files.isDirectory (p))
                        .map (p -> _normalizeFile (p))
                        .peek (f -> LOGGER.debug ("Add file={}", f.toString ()))
                        .collect (Collectors.toList ());
        ret.addAll (dirFiles);
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
      final boolean bFormattedOutput = true;
      if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType)
      {
        UBL21Writer.invoice ()
                   .setFormattedOutput (bFormattedOutput)
                   .write ((oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType) aUBL, aDestFile);
      }
      else
        if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType)
        {
          UBL21Writer.creditNote ()
                     .setFormattedOutput (bFormattedOutput)
                     .write ((oasis.names.specification.ubl.schema.xsd.creditnote_21.CreditNoteType) aUBL, aDestFile);
        }
        else
          if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType)
          {
            UBL22Writer.invoice ()
                       .setFormattedOutput (bFormattedOutput)
                       .write ((oasis.names.specification.ubl.schema.xsd.invoice_22.InvoiceType) aUBL, aDestFile);
          }
          else
            if (aUBL instanceof oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType)
            {
              UBL22Writer.creditNote ()
                         .setFormattedOutput (bFormattedOutput)
                         .write ((oasis.names.specification.ubl.schema.xsd.creditnote_22.CreditNoteType) aUBL,
                                 aDestFile);
            }
            else
            {
              if (aUBL != null)
                throw new IllegalStateException ("Unsupported UBL version '" + m_sUBLVersion + "'");

              LOGGER.error ("Failed to convert CII file '" + f.getAbsolutePath () + "' to UBL:");
              for (final IError aError : aErrorList)
                LOGGER.error (aError.getAsString (aErrorLocale));
            }
    }

    return Integer.valueOf (0);
  }

  public static void main (final String [] aArgs)
  {
    LOGGER.info ("Starting CII to UBL Converter");
    final CommandLine cmd = new CommandLine (new CIIToUBLConverter ());
    cmd.setCaseInsensitiveEnumValuesAllowed (true);
    final int nExitCode = cmd.execute (aArgs);
    System.exit (nExitCode);
  }
}
