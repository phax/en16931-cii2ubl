# en16931-cii2ubl

Converter for EN16931 invoices from CII to UBL

# News and noteworthy

* v1.1.5 - work in progress
    * Added possibility to enforce invoice creation
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

# Open issues

* The migration of CII `NetPriceProductTradePrice/BasisQuantity` to UBL `Price/BaseQuantity` is not consistent for me
    * See example files 2, 8 and 9
    * The UBL example files use a BaseQuanity of 1 in all cases

# Maven usage

```xml
<dependency>
  <groupId>com.helger</groupId>
  <artifactId>en16931-cii2ubl</artifactId>
  <version>1.1.5-SNAPSHOT</version>
</dependency>
```
