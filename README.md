# en16931-cii2ubl

Converter for unidirectional EN16931 invoices from CII D16B to UBL 2.1 and 2.2.

This is a Java 1.8+ library that converts a Cross Industry Invoice (CII) into a Universal Business Language (UBL) document following the rules of the European Norm (EN) 16931 that defines a common semantic data model for electronic invoices in Europe.  

See https://peppol.helger.com/public/locale-en_US/menuitem-tools-rest-api#cii2ubl for a service implementation using this library.

This library is licensed under the Apache License Version 2.0.

# Usage

This is a pure Java library and not a self-contained conversion tool.
You can convert CII D16B invoices following the EN 16931 rules to both UBL 2.1 and UBL 2.2.
The entrance classes are:
* `com.helger.en16931.cii2ubl.CIIToUBL21Converter`
* `com.helger.en16931.cii2ubl.CIIToUBL22Converter`

The main conversion method is called `convertCIItoUBL` and takes either a `File` as input or a pre-parsed `un.unece.uncefact.data.standard.crossindustryinvoice._100.CrossIndustryInvoiceType` object (that reading is done with class `com.helger.cii.d16b.CIID16BReader` from [ph-cii](https://github.com/phax/ph-cii)).
Additionally an `ErrorList` object must be provided as a container for all the errors that occur.


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

The CLI interface was introduced in v1.2.0.

Call it via `java -jar en16931-cii2ubl-cli-1.2.0-full.jar`


```
Missing required parameter: source files
Usage: CIItoUBLConverter [-hV] [--mode mode] [-t director] [--ubl version]
                         [--ubl-cardaccountnetworkid ID] [--ubl-customizationid
                         ID] [--ubl-profileid ID] [--ubl-vatscheme vat scheme]
                         source files...
CII to UBL Converter.
      source files...      One or more CII file(s)
  -h, --help               Show this help message and exit.
      --mode mode          Allowed values: AUTOMATIC, INVOICE, CREDIT_NOTE
  -t, --target director    The target directory for result output (default: .)
      --ubl version        Version of the target UBL Format (default: 2.1)
      --ubl-cardaccountnetworkid ID
                           The UBL CardAccount network ID to be used (default:
                             mapped-from-cii)
      --ubl-customizationid ID
                           The UBL customization ID to be used (default: urn:
                             cen.eu:en16931:2017:extended:urn:fdc:peppol.eu:
                             2017:poacc:billing:3.0)
      --ubl-profileid ID   The UBL profile ID to be used (default: urn:fdc:
                             peppol.eu:2017:poacc:billing:01:1.0)
      --ubl-vatscheme vat scheme
                           The UBL VAT scheme to be used (default: VAT)
  -V, --version            Print version information and exit.
```

# Open issues

* The migration of CII `NetPriceProductTradePrice/BasisQuantity` to UBL `Price/BaseQuantity` is not consistent for me
    * See example files 2, 8 and 9
    * The UBL example files use a BaseQuanity of 1 in all cases

# News and noteworthy

* v1.3.0 - work in progress
    * Added support for different CII time formats (2, 3, 4, 101, 102, 103 and 105) where `102` is the default
    * The error handling for the commandline client was improved (checking errors before writing UBL) (see [issue #9](https://github.com/phax/en16931-cii2ubl/issues/9))
    * Added the possibility to create UBL 2.3
    * Made the API more open to directly convert to Invoice or CreditNote
* v1.2.5 - 2020-11-30
    * Added Jakarta Activation dependency to the standalone CLI version (see [issue #6](https://github.com/phax/en16931-cii2ubl/issues/6))
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
    * Added commandline interface (CLI) - (see [PR #3](https://github.com/phax/en16931-cii2ubl/pull/3)) - thanks to [@rkottmann](https://github.com/rkottmann)
    * Fixed creating invalid UBL if `SellerOrderReferencedDocument` is present but `BuyerOrderReferencedDocument` is not set (see [issue #5](https://github.com/phax/en16931-cii2ubl/issues/5))
    * Made default VAT scheme, UBL `CustomizationID`, UBL `ProfileID` and the `PaymentMeans/CardAccount/NetworkID` customizable (see [issue #1](https://github.com/phax/en16931-cii2ubl/issues/1) and [issue #2](https://github.com/phax/en16931-cii2ubl/issues/2))
    * Fixed embedded attachment mapping (see [issue #4](https://github.com/phax/en16931-cii2ubl/issues/4))
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
On Twitter: <a href="https://twitter.com/philiphelger">@philiphelger</a> |
Kindly supported by [YourKit Java Profiler](https://www.yourkit.com)