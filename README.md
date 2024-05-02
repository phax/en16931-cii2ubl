# en16931-cii2ubl

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.helger/en16931-cii2ubl/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.helger/en16931-cii2ubl) 
[![javadoc](https://javadoc.io/badge2/com.helger/en16931-cii2ubl/javadoc.svg)](https://javadoc.io/doc/com.helger/en16931-cii2ubl)

Converter for unidirectional EN16931 invoices from CII D16B to UBL 2.1, 2.2, 2.3 or 2.4.

This is a Java 11+ library that converts a Cross Industry Invoice (CII) into a Universal Business Language (UBL) document following the rules of the European Norm (EN) 16931 that defines a common semantic data model for electronic invoices in Europe.

Special care was given to XRechnung invoices - all the CII examples of them translate to UBL that is valid according to the EN 16931 validation rules.

See https://peppol.helger.com/public/locale-en_US/menuitem-tools-rest-api#cii2ubl for a service implementation using this library.

This library is licensed under the Apache License Version 2.0.

The binary releases are available on Maven Central at https://repo1.maven.org/maven2/com/helger/en16931-cii2ubl/ and below.

# Usage

This is a pure Java library and not a self-contained conversion tool.
You can convert CII D16B invoices following the EN 16931 rules to different UBL versions.
The entrance classes are:
* Create UBL 2.1: `com.helger.en16931.cii2ubl.CIIToUBL21Converter`
* Create UBL 2.2: `com.helger.en16931.cii2ubl.CIIToUBL22Converter`
* Create UBL 2.3: `com.helger.en16931.cii2ubl.CIIToUBL23Converter` (since v1.3.0)
* Create UBL 2.4: `com.helger.en16931.cii2ubl.CIIToUBL24Converter` (since v2.1.0)

The main conversion method is called `convertCIItoUBL` and takes either a `File` as input or a pre-parsed `un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType` object (that reading is done with class `com.helger.cii.d16b.CIID16BCrossIndustryInvoiceTypeMarshaller` from [ph-cii](https://github.com/phax/ph-cii)).
Additionally an `ErrorList` object must be provided as a container for all the errors that occur.

The conversion is deemed successful, if a non-`null` object is returned **and** if the error list contains no error (`errorList.containsNoError ()`).

## Maven usage

Replace `x.y.z` with the effective version you want to use:

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>en16931-cii2ubl</artifactId>
  <version>x.y.z</version>
</dependency>
```

## Commandline usage

Call it via `java -jar en16931-cii2ubl-cli-full.jar` followed by the options and parameters.

```
[INFO] CII to UBL Converter v2.2.2 (build 2024-04-12T15:26:48Z)
Missing required parameter: 'source files'
Usage: CIItoUBLConverter [-hV] [--disable-wildcard-expansion] [--verbose]
                         [--mode mode] [--output-suffix filename part] [-t
                         directory] [--ubl version] [--ubl-cardaccountnetworkid
                         ID] [--ubl-customizationid ID]
                         [--ubl-defaultorderrefid ID] [--ubl-profileid ID]
                         [--ubl-vatscheme vat scheme] source files...
CII to UBL Converter for EN 16931 invoices
      source files...      One or more CII file(s)
      --disable-wildcard-expansion
                           Disable wildcard expansion of filenames
  -h, --help               Show this help message and exit.
      --mode mode          Allowed values: AUTOMATIC, INVOICE, CREDIT_NOTE
                             (default: 'AUTOMATIC')
      --output-suffix filename part
                           The suffix added to the output filename (default:
                             '-ubl')
  -t, --target directory   The target directory for result output (default: '.')
      --ubl version        Version of the target UBL Format: '2.1', '2.2',
                             '2.3' or '2.4' (default: '2.1')
      --ubl-cardaccountnetworkid ID
                           The UBL CardAccount network ID to be used (default:
                             'mapped-from-cii')
      --ubl-customizationid ID
                           The UBL customization ID to be used (default: 'urn:
                             cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:
                             2017:poacc:billing:3.0')
      --ubl-defaultorderrefid ID
                           The UBL default order reference ID to be used
                             (default: '')
      --ubl-profileid ID   The UBL profile ID to be used (default: 'urn:fdc:
                             peppol.eu:2017:poacc:billing:01:1.0')
      --ubl-vatscheme vat scheme
                           The UBL VAT scheme to be used (default: 'VAT')
  -V, --version            Print version information and exit.
      --verbose            Enable debug logging (default: 'false')
```

# Open issues

* The migration of CII `NetPriceProductTradePrice/BasisQuantity` to UBL `Price/BaseQuantity` is not consistent for me
    * See example files 2, 8 and 9
    * The UBL example files use a BaseQuanity of 1 in all cases

# News and noteworthy

* v2.2.3 - work in progress
    * Added additional mapping of BT-41. See [issue #28](https://github.com/phax/en16931-cii2ubl/issues/28) - thx @bdewein
* v2.2.2 - 2024-04-12
    * Added support for providing the default UBL order reference ID, in case the empty String is not good enough. See [issue #23](https://github.com/phax/en16931-cii2ubl/issues/23) - thx @lkumai
* v2.2.1 - 2024-03-29
    * Updated to ph-commons 11.1.5
    * Ensured Java 21 compatibility
* v2.2.0 - 2024-03-08
    * Updated to ph-ubl 9.0.0
    * Tested output against EN 16931 rules v1.3.10 and v1.3.11 - no changes necessary
    * Updated to create UBL 2.4-CS01
    * Added support for BT-8 mapping. See [issue #22](https://github.com/phax/en16931-cii2ubl/issues/22)
* v2.1.0 - 2023-04-28
    * Updated to ph-ubl 8.0.1
    * Added support for converting to UBL 2.4
* v2.0.3 - 2023-04-20
    * Improved mapping of references to external documents in additional document references. See [issue #20](https://github.com/phax/en16931-cii2ubl/issues/20) - thanks @msccip
* v2.0.2 - 2023-03-30
    * Changed the default mode of the CLI version from `INVOICE` to `AUTOMATIC`. See [issue #19](https://github.com/phax/en16931-cii2ubl/issues/19) - thanks @msccip
* v2.0.1 - 2023-03-15
    * Added manual wildcard expansion of filenames
    * Added new CLI option `--disable-wildcard-expansion` to disable the wildcard expansion and stick to the old resolution logic
* v2.0.0 - 2023-02-20
    * Using Java 11 as the baseline
    * Updated to ph-commons 11
    * Updated to JAXB 4.0
    * Added the new CLI parameter `--verbose` for a few more details
    * Improved logging
    * Successfully tested the CLI parameter with wildcard parameters (because the Java Windows Runtime performs automatic wildcard expansion)
* v1.4.10 - 2022-12-16
    * Fixed the conversion of the `TypeCode` element in `AdditionalReferencedDocument`. See [issue #18](https://github.com/phax/en16931-cii2ubl/issues/18) - thanks @L3Mars
* v1.4.9 - 2022-11-15
    * Fixed an unnecessary division by 100 for creating `MultiplierFactorNumeric`. See [issue #17](https://github.com/phax/en16931-cii2ubl/issues/17) - thanks @L3Mars
    * Tested output against EN 16931 rules v1.3.9 - no changes necessary
* v1.4.8 - 2022-09-28
    * Added new option `--output-suffix` to customize the output file suffix, that is currently hard coded to `-ubl`
    * Fixed a problem with the mapping of BT-147, BT-148, BT-149 and BT-150. See [issue #15](https://github.com/phax/en16931-cii2ubl/issues/15). Thanks to @cambid for pointing that out
* v1.4.7 - 2022-02-15
    * Further improved `null`/empty handling to avoid creation of empty elements
* v1.4.6 - 2022-02-12
    * Allowing the additional Payment Means Type Codes `1`, `42` and `68`. See [issue #13](https://github.com/phax/en16931-cii2ubl/issues/13)
    * For decimal values, trailing zeroes are no longer emitted. See [issue #13](https://github.com/phax/en16931-cii2ubl/issues/13)
    * Improved not creating empty UBL elements. See [issue #13](https://github.com/phax/en16931-cii2ubl/issues/13)
    * The `SubjectCode` of `IncludedNote` elements is copied over. See [issue #13](https://github.com/phax/en16931-cii2ubl/issues/13)
    * Improved the Party ID handling and allowing for multiple seller IDs. See [issue #13](https://github.com/phax/en16931-cii2ubl/issues/13)
* v1.4.5 - 2021-12-20
    * Tested output against EN 16931 rules v1.3.7 - no changes necessary
    * Fixed creating invalid UBL if `SellerAssignedID` is empty. See [issue #12](https://github.com/phax/en16931-cii2ubl/issues/12) - thanks @DerHamm
* v1.4.4 - 2021-10-14
    * Improved sign swapping of Quantity and Price to avoid negative prices (BT-146)
* v1.4.3 - 2021-10-07
    * Tested output against EN 16931 rules v1.3.6 - no changes necessary
    * Changed determination if Invoice or CreditNote primarily to `rsm:ExchangedDocument/ram:TypeCode` instead of the payable amount
* v1.4.2 - 2021-06-10
    * Changed the default customization ID to `urn:cen.eu:en16931:2017#compliant#urn:fdc:peppol.eu:2017:poacc:billing:3.0`
    * If the UBL `LineExtensionAmount` is negative, but the line `Quantity` is positive, the `Quantity` will be changed to negative. Customizable via `setSwapQuantitySignIfNeeded(boolean)`
* v1.4.1 - 2021-05-02
    * Updated to ph-commons 10.1
    * Tested output against EN 16931 rules v1.3.5 - no changes necessary
* v1.4.0 - 2021-03-22
    * Updated to ph-commons 10
* v1.3.0 - 2021-01-05
    * Added support for different CII time formats (2, 3, 4, 101, 102, 103 and 105) where `102` is the default
    * The error handling for the commandline client was improved (checking errors before writing UBL) (see [issue #9](https://github.com/phax/en16931-cii2ubl/issues/9))
    * Added the possibility to create UBL 2.3
    * Made the API more open to directly convert to Invoice or CreditNote
    * Improved the handling of payment means to be more EN compliant. See [issue #7](https://github.com/phax/en16931-cii2ubl/issues/7).
    * Added a possibility to retrieve the internal converter version number
* v1.2.5 - 2020-11-30
    * Added Jakarta Activation dependency to the standalone CLI version. See [issue #6](https://github.com/phax/en16931-cii2ubl/issues/6).
* v1.2.4 - 2020-10-20
    * Tested with EN 16031 validation rules 1.3.3
    * Not emitting the `LegalMonetaryTotal/PayableRoundingAmount` if the value is `0` as a work around for https://github.com/ConnectingEurope/eInvoicing-EN16931/issues/242
* v1.2.3 - 2020-09-17
    * Updated to Jakarta JAXB 2.3.3
* v1.2.2 - 2020-08-30
    * Updated to ph-commons 9.4.7
    * Updated to ph-cii 2.3.0
    * Updated to ph-ubl 6.4.0
* v1.2.1 - 2020-05-26
    * Updated to new Maven groupIds
* v1.2.0 - 2020-03-09
    * Verified against EN 16931 validation artefacts 1.3.0 - no changes in the output
    * Added commandline interface (CLI). See [PR #3](https://github.com/phax/en16931-cii2ubl/pull/3). Thanks to @rkottmann
    * Fixed creating invalid UBL if `SellerOrderReferencedDocument` is present but `BuyerOrderReferencedDocument` is not set (see [issue #5](https://github.com/phax/en16931-cii2ubl/issues/5))
    * Made default VAT scheme, UBL `CustomizationID`, UBL `ProfileID` and the `PaymentMeans/CardAccount/NetworkID` customizable. See [issue #1](https://github.com/phax/en16931-cii2ubl/issues/1) and [issue #2](https://github.com/phax/en16931-cii2ubl/issues/2).
    * Fixed embedded attachment mapping. See [issue #4](https://github.com/phax/en16931-cii2ubl/issues/4).
* v1.1.5 - 2019-09-13
    * Added possibility to enforce invoice creation
    * Verified against EN 16931 validation artefacts 1.3.0
* v1.1.4 - 2019-07-15
    * Updated to EN 16931 validation artefacts 1.2.3
* v1.1.3 - 2019-05-15
    * Updated to EN 16931 validation artefacts 1.2.1
* v1.1.2 - 2019-04-26
    * Updated to EN 16931 validation artefacts 1.2.0
* v1.1.1 - 2019-02-27
    * Improved delivery date handling
    * Improved price base quantity handling
* v1.1.0 - 2019-02-26
    * Added support to create UBL 2.1 Invoice and CreditNote
* v1.0.0 - 2019-02-26
    * Initial release creating UBL 2.2 Invoice and CreditNote

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
It is appreciated if you star the GitHub project if you like it.
