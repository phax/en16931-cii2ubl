package com.helger.en16931.cii2ubl.cli;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.en16931.cii2ubl.CIIToUBL21Converter;
import com.helger.en16931.cii2ubl.EUBLCreationMode;
import com.helger.ubl21.UBL21Writer;
import com.helger.ubl21.UBL21WriterBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import oasis.names.specification.ubl.schema.xsd.invoice_21.InvoiceType;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/**
 * Main
 */
@Command(description = "CII to UBL Converter.", name = "CIItoUBLConverter", mixinStandardHelpOptions = true, separator = " ")
public class CIIToUBLConverter implements Callable<Integer> {

    private static final Logger log = LoggerFactory.getLogger(CIIToUBLConverter.class);

    @Option(names = "--ubl", defaultValue = "2.1", description = "Version of the target UBL Format (default: ${DEFAULT-VALUE})")
    private String UBLVersion;

    @Option(names = "--mode", defaultValue = "INVOICE", description = "Allowedvalues:${COMPLETION-CANDIDATES}")
    private EUBLCreationMode mode;

    @Option(names = { "-t",
            "--target" }, paramLabel = "out", defaultValue = ".", description = "The target directory for result output (default: ${DEFAULT-VALUE})")
    private String outputDir;

    @Parameters(arity = "1..*", description = "One or more Files")
    private List<File> files;

    // doing the business
    public Integer call() throws Exception {
        try {
            init();
        } catch (IOException e) {
            log.error("Could not initialize converter", e.getLocalizedMessage());
        }
        // TODO switch between versions
        final CIIToUBL21Converter converter = new CIIToUBL21Converter();
        converter.setUBLCreationMode(mode);
        final ErrorList errorList = new ErrorList();

        Serializable invoice = null;
        InvoiceType ublInvoiceType = null;
        File destFile = null;
        UBL21WriterBuilder<InvoiceType> aWriter = null;

        for (File f : this.files) {
            log.debug("Converting file={}", f.toString());
            invoice = converter.convertCIItoUBL(f, errorList);
            ublInvoiceType = (InvoiceType) invoice;
            destFile = new File(this.outputDir, FilenameHelper.getBaseName(f) + "-ubl.xml");
            aWriter = UBL21Writer.invoice().setFormattedOutput(true);
            aWriter.write(ublInvoiceType, destFile);
        }

        return 0;
    }

    private void init() throws IOException {
        this.outputDir = normalizeOutputDirectory(this.outputDir);
        this.files = normalizeInputFiles(this.files);
    }

    private String normalizeOutputDirectory(String dir) {

        log.debug("CLI option output directory={}", dir);
        String result = "";
        result = Paths.get(dir).toAbsolutePath().normalize().toString();
        log.debug("Normalized output directory={}", result);
        return result;
    }

    private List<File> normalizeInputFiles(List<File> files) throws IOException {
        List<File> normalizedFiles = new ArrayList<File>();

        List<File> dirFiles = new ArrayList<File>();
        for (File file : files) {
            if (file.isDirectory()) {
                log.debug("Is a diractory={}", file.toString());
                // collecting readable and normalized absolute path files
                dirFiles = Files.walk(file.toPath()).filter(p -> Files.isReadable(p) && !Files.isDirectory(p))
                        .map(p -> normalizeFile(p)).peek(f -> log.debug("Add file={}", f.toString()))
                        .collect(Collectors.toList());
                normalizedFiles.addAll(dirFiles);
            } else if (file.canRead()) {
                log.debug("Is a file={}", file.toString());
                normalizedFiles.add(normalizeFile(file.toPath()));
            }
        }
        return normalizedFiles;

    }

    private static File normalizeFile(Path path) {

        return new File(path.toAbsolutePath().normalize().toString());

    }

    public static void main(final String[] args) {

        log.info("Starting CII to UBL Converter");
        final CommandLine cmd = new CommandLine(new CIIToUBLConverter());
        cmd.setCaseInsensitiveEnumValuesAllowed(true);
        final int exitCode = cmd.execute(args);
        System.exit(exitCode);
    }
}
