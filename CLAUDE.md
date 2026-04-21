# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Unidirectional converter from CII (Cross Industry Invoice) D16B format to UBL (Universal Business Language) 2.1/2.2/2.3/2.4, following the EN 16931 European e-invoicing standard. Pure Java library + optional CLI wrapper.

## Build & Test Commands

```bash
mvn clean compile                # Compile
mvn clean test                   # Run all tests
mvn clean package                # Build JARs

# Single test class
mvn test -pl en16931-cii2ubl -Dtest=CIIToUBL21ConverterTest

# Single test method
mvn test -pl en16931-cii2ubl -Dtest=CIIToUBL21ConverterTest#testConvertAndValidateAll

# Build CLI fat JAR
mvn clean package -pl en16931-cii2ubl-cli
# Output: en16931-cii2ubl-cli/target/en16931-cii2ubl-cli-full.jar
```

Requires Java 17+.

## Module Structure

- **en16931-cii2ubl/** — Core conversion library
- **en16931-cii2ubl-cli/** — CLI wrapper using picocli (produces shaded fat JAR)

## Architecture

### Converter Hierarchy

```
AbstractCIIToUBLConverter<IMPLTYPE>  (base class, ~787 lines)
  ├── CIIToUBL21Converter            (~2417 lines each, nearly identical)
  ├── CIIToUBL22Converter            (differences are UBL version-specific
  ├── CIIToUBL23Converter             JAXB types and marshaller classes)
  └── CIIToUBL24Converter

EUBLCreationMode                     (enum: AUTOMATIC, INVOICE, CREDIT_NOTE)
CIIToUBLVersion                      (version constants loaded from properties)
```

The converters are **not auto-generated** — they are manually maintained with slight variations per UBL version (different JAXB type packages).

CLI entry point: `com.helger.en16931.cii2ubl.cli.CIIToUBLConverter` (picocli command, shaded into fat JAR).

### Conversion Flow

1. Parse CII XML → `CrossIndustryInvoiceType` JAXB object (via ph-cii `CIID16BCrossIndustryInvoiceTypeMarshaller`)
2. Determine document type (Invoice vs CreditNote) based on `TypeCode` — see `CREDIT_NOTE_TYPE_CODES` / `INVOICE_TYPE_CODES` constants
3. Map CII fields to UBL equivalents field-by-field (BT-1 through BT-170 from EN 16931)
4. Apply business rules (quantity/price sign swapping for credit notes)
5. Serialize UBL via `UBLxxMarshaller` (from ph-ubl)

### Configuration (fluent API on converters)

- `setUBLCreationMode()` — AUTOMATIC (default), INVOICE, or CREDIT_NOTE
- `setVATScheme()`, `setCustomizationID()`, `setProfileID()`
- `setCardAccountNetworkID()`, `setDefaultOrderRefID()`
- `setSwapQuantitySignIfNeeded()`, `setSwapPriceSignIfNeeded()`

### Error Handling

Converters accept an `ErrorList` parameter. Conversion is successful only if a non-null result is returned **and** the error list contains no errors.

## Testing

- JUnit 4, test classes mirror converter classes: `CIIToUBL21ConverterTest` etc.
- `AbstractCIIToUBLConverterTest` — base test class shared by all four converter tests
- Tests convert all CII example files and validate output against EN 16931 rules (via phive-rules)
- Test CII files in `en16931-cii2ubl/src/test/resources/external/cii/` (~65+ files: EN 16931 examples, XRechnung 1.2.2/2.0.0/3.0.2, issue-specific test cases)
- `MockSettings` centralizes test file discovery and validation rule registration

## Key Dependencies

- **ph-commons** — Helger utilities, error handling, collection types (`ICommonsList`, etc.)
- **ph-cii** — CII D16B JAXB model and marshalling
- **ph-ubl** — UBL 2.1–2.4 JAXB models and marshalling
- **phive-rules-en16931** — EN 16931 validation rules (test scope only)

## Field Mapping Reference

`docs/mapping-cii-ubl.xlsx` contains the complete CII-to-UBL field mapping with EN 16931 BT identifiers.
