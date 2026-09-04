# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Unidirectional converter from CII (Cross Industry Invoice) to UBL (Universal Business Language), following the EN 16931 European e-invoicing standard. Pure Java library + optional CLI wrapper.

Both editions of the standard are supported, each with exactly one syntax pair:

| Edition | Input | Output | Specification identifier (BT-24) |
|---------|-------|--------|----------------------------------|
| EN 16931:2017 | CII D16B | UBL 2.1 | `urn:cen.eu:en16931:2017` |
| EN 16931:2026 | CII D25A | UBL 2.5 | `urn:cen.eu:en16931:2026` |

## Build & Test Commands

```bash
mvn clean compile                # Compile
mvn clean test                   # Run all tests
mvn clean package                # Build JARs

# Single test class
mvn test -pl en16931-cii2ubl -Dtest=CIID16BToUBL21ConverterTest

# Single test method
mvn test -pl en16931-cii2ubl -Dtest=CIID16BToUBL21ConverterTest#testConvertAndValidateAll

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
com.helger.en16931.cii2ubl                         edition independent
  AbstractCIIToUBLConverterBase<IMPLTYPE>          config API, error helpers, date parsing,
                                                   code predicates, swapQuantityAndPriceIfNeeded
  EEN16931Edition                                  EN2017 / EN2026 + detect() from BT-24
  CIIToUBLDispatcher                               parse -> detect -> route
  EUBLCreationMode                                 enum: AUTOMATIC, INVOICE, CREDIT_NOTE
  CIIToUBLVersion                                  version constants from properties

com.helger.en16931.cii2ubl.en2017                  CII D16B -> UBL 2.1
  AbstractCIIToUBL2017Converter<IMPLTYPE>          D16B-typed helpers
  CIID16BToUBL21Converter

com.helger.en16931.cii2ubl.en2026                  CII D25A -> UBL 2.5
  AbstractCIIToUBL2026Converter<IMPLTYPE>          D25A-typed helpers
  CIID25AToUBL25Converter
```

The converters are **not auto-generated** — they are manually maintained. The two editions cannot share code on the CII input side, because the D25A JAXB model lives in a different Java package (`un.unece.uncefact.data.standard.cii.d25a.*`) than D16B (`un.unece.uncefact.data.standard.crossindustryinvoice._100`) while using identical class names.

CLI entry point: `com.helger.en16931.cii2ubl.cli.CIIToUBLConverter` (picocli command, shaded into fat JAR). The edition is taken from BT-24 per file unless `--en-version 2017|2026` is given.

### Conversion Flow

1. Parse CII XML → `CrossIndustryInvoiceType` JAXB object (via ph-cii `CIID16BCrossIndustryInvoiceTypeMarshaller` or `CIID25ACrossIndustryInvoiceTypeMarshaller`)
2. Determine document type (Invoice vs CreditNote) based on `TypeCode` — see `CREDIT_NOTE_TYPE_CODES` / `INVOICE_TYPE_CODES` constants
3. Map CII fields to UBL equivalents field-by-field (BT-1 through BT-220 from EN 16931)
4. Apply business rules (quantity/price sign swapping for credit notes)
5. Serialize UBL via `UBL21Marshaller` respectively `UBL25Marshaller` (from ph-ubl)

### Configuration (fluent API on converters)

- `setUBLCreationMode()` — AUTOMATIC (default), INVOICE, or CREDIT_NOTE
- `setVATScheme()`, `setCustomizationID()`, `setProfileID()`
- `setCardAccountNetworkID()`, `setDefaultOrderRefID()`
- `setSwapQuantitySignIfNeeded()`, `setSwapPriceSignIfNeeded()`

### Error Handling

Converters accept an `ErrorList` parameter. Conversion is successful only if a non-null result is returned **and** the error list contains no errors.

## Testing

- JUnit 4, test classes mirror converter classes.
- **EN 16931:2017** — `en2017.CIID16BToUBL21ConverterTest` converts all 102 CII example files and validates the output against the EN 16931 Schematron (via phive-rules). Test files in `en16931-cii2ubl/src/test/resources/external/cii/` (EN 16931 examples, XRechnung 1.2.2/2.0.0/3.0.2, issue-specific cases). `MockSettings` centralizes file discovery and rule registration.
- **EN 16931:2026** — `en2026.CIID25AToUBL25ConverterTest` uses artificial CII D25A files in `src/test/resources/external/cii-d25a/`, because no official test files and no Schematron exist yet. `MockD25ASettings.convertAndValidate` validates the source against the D25A XSD, the result against the UBL 2.5 XSD, and then asserts individual business terms with `assertXPath` / `assertNoXPath` / `assertXPathCount`, using expressions taken straight from the mapping table.
- `en2026.MappingCoverageTest` fails if any row of `docs/en16931-2026-syntax.md` is never named in the 2026 converter. It exists because the mapping document's own "Business Terms and Groups New in 2026" table is **not** a complete diff against 2017 — BT-122-1 was missed that way.
- Converted output is written to `en16931-cii2ubl/generated/toubl21/` and `generated/toubl25/`, both tracked in git. `git status` on those folders is the regression check.

## Key Dependencies

- **ph-commons** — Helger utilities, error handling, collection types (`ICommonsList`, etc.)
- **ph-cii** — CII D16B and D25A JAXB models and marshalling
- **ph-ubl** — UBL 2.1 and 2.5 JAXB models and marshalling
- **phive-rules-en16931** — EN 16931 validation rules (test scope only)

## Field Mapping Reference

- `docs/en16931-2026-syntax.md` — **the source of truth for EN 16931:2026** (UBL 2.5 / CII D25A), 284 rows
- `docs/en16931-2017-syntax.md` — the same for EN 16931:2017 (UBL 2.1 / CII D16B)
- `docs/plan-4.0.0.md` — the v4.0.0 implementation plan, including the findings and traps encountered along the way
- `docs/mapping-cii-ubl.xlsx` and `docs/mapping-cii-ubl v2 fr19.xls` — the original spreadsheets, **EN 16931:2017 only**
