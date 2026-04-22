# EN 16931 Three-Way Field Mapping: UBL Invoice / UBL Credit Note / CII D16B

Source: FprCEN/TS 16931-3-2:2019 (UBL) and FprCEN/TS 16931-3-3:2019 (CII)

## Namespaces

| Prefix | Namespace |
|--------|-----------|
| `rsm` | `urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100` |
| `ram` | `urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100` |
| `udt` | `urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100` |
| `qdt` | `urn:un:unece:uncefact:data:standard:QualifiedDataType:100` |
| `cac` | `urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2` |
| `cbc` | `urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2` |

## Conventions

- CII paths are relative to `/rsm:CrossIndustryInvoice` (abbreviated as `/CII`)
- UBL Invoice paths are relative to `/Invoice`
- UBL Credit Note paths are relative to `/CreditNote`
- `(same)` means the Credit Note path is identical to Invoice except the root element
- Discriminators (TypeCode, ChargeIndicator, schemeID) are noted in the Notes column

## Header Level Fields

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-1 | Invoice number | 1..1 | `cbc:ID` | `cbc:ID` | `rsm:ExchangedDocument/ram:ID` | |
| BT-2 | Invoice issue date | 1..1 | `cbc:IssueDate` | `cbc:IssueDate` | `rsm:ExchangedDocument/ram:IssueDateTime/udt:DateTimeString` | CII: @format="102" |
| BT-3 | Invoice type code | 1..1 | `cbc:InvoiceTypeCode` | `cbc:CreditNoteTypeCode` | `rsm:ExchangedDocument/ram:TypeCode` | Different element name in CN |
| BT-5 | Invoice currency code | 1..1 | `cbc:DocumentCurrencyCode` | `cbc:DocumentCurrencyCode` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceCurrencyCode` | |
| BT-6 | VAT accounting currency code | 0..1 | `cbc:TaxCurrencyCode` | `cbc:TaxCurrencyCode` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:TaxCurrencyCode` | |
| BT-7 | Value added tax point date | 0..1 | `cbc:TaxPointDate` | `cbc:TaxPointDate` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:ApplicableTradeTax/ram:TaxPointDate/udt:DateString` | CII: @format="102" |
| BT-8 | Value added tax point date code | 0..1 | `cac:InvoicePeriod/cbc:DescriptionCode` | `cac:InvoicePeriod/cbc:DescriptionCode` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:ApplicableTradeTax/ram:DueDateTypeCode` | |
| BT-9 | Payment due date | 0..1 | `cbc:DueDate` | `cac:PaymentMeans/cbc:PaymentDueDate` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:DueDateDateTime/udt:DateTimeString` | Different path in CN! |
| BT-10 | Buyer reference | 0..1 | `cbc:BuyerReference` | `cbc:BuyerReference` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerReference` | |
| BT-11 | Project reference | 0..1 | `cac:ProjectReference/cbc:ID` | `cac:AdditionalDocumentReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SpecifiedProcuringProject/ram:ID` | CN: no discriminator (UBL 2.1 only; 2.2+ has ProjectReference) |
| BT-12 | Contract reference | 0..1 | `cac:ContractDocumentReference/cbc:ID` | `cac:ContractDocumentReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:ContractReferencedDocument/ram:IssuerAssignedID` | |
| BT-13 | Purchase order reference | 0..1 | `cac:OrderReference/cbc:ID` | `cac:OrderReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerOrderReferencedDocument/ram:IssuerAssignedID` | |
| BT-14 | Sales order reference | 0..1 | `cac:OrderReference/cbc:SalesOrderID` | `cac:OrderReference/cbc:SalesOrderID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerOrderReferencedDocument/ram:IssuerAssignedID` | |
| BT-15 | Receiving advice reference | 0..1 | `cac:ReceiptDocumentReference/cbc:ID` | `cac:ReceiptDocumentReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeDelivery/ram:ReceivingAdviceReferencedDocument/ram:IssuerAssignedID` | |
| BT-16 | Despatch advice reference | 0..1 | `cac:DespatchDocumentReference/cbc:ID` | `cac:DespatchDocumentReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeDelivery/ram:DespatchAdviceReferencedDocument/ram:IssuerAssignedID` | |
| BT-17 | Tender or lot reference | 0..1 | `cac:OriginatorDocumentReference/cbc:ID` | `cac:OriginatorDocumentReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:AdditionalReferencedDocument/ram:IssuerAssignedID` | CII: TypeCode="50" |
| BT-18 | Invoiced object identifier | 0..1 | `cac:AdditionalDocumentReference/cbc:ID` | `cac:AdditionalDocumentReference/cbc:ID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:AdditionalReferencedDocument/ram:IssuerAssignedID` | Invoice: DocumentTypeCode=130; CN: DocumentType="ATS"; CII: TypeCode="130" |
| BT-18-1 | Invoiced object identifier scheme identifier | 0..1 | `cac:AdditionalDocumentReference/cbc:ID/@schemeID` | `cac:AdditionalDocumentReference/cbc:ID/@schemeID` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:AdditionalReferencedDocument/ram:ReferenceTypeCode` | |
| BT-19 | Buyer accounting reference | 0..1 | `cbc:AccountingCost` | `cbc:AccountingCost` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:ReceivableSpecifiedTradeAccountingAccount/ram:ID` | |
| BT-20 | Payment terms | 0..1 | `cac:PaymentTerms/cbc:Note` | `cac:PaymentTerms/cbc:Note` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:Description` | |

## BG-1 INVOICE NOTE (0..n)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-21 | Invoice note subject code | 0..1 | `cbc:Note` (prefix `#code#`) | `cbc:Note` (prefix `#code#`) | `rsm:ExchangedDocument/ram:IncludedNote/ram:SubjectCode` | UBL: embedded in Note text |
| BT-22 | Invoice note | 1..1 | `cbc:Note` | `cbc:Note` | `rsm:ExchangedDocument/ram:IncludedNote/ram:Content` | |

## BG-2 PROCESS CONTROL (1..1)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-23 | Business process type | 0..1 | `cbc:ProfileID` | `cbc:ProfileID` | `rsm:ExchangedDocumentContext/ram:BusinessProcessSpecifiedDocumentContextParameter/ram:ID` | |
| BT-24 | Specification identifier | 1..1 | `cbc:CustomizationID` | `cbc:CustomizationID` | `rsm:ExchangedDocumentContext/ram:GuidelineSpecifiedDocumentContextParameter/ram:ID` | |

## BG-3 PRECEDING INVOICE REFERENCE (0..n)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-25 | Preceding Invoice number | 1..1 | `cac:BillingReference/cac:InvoiceDocumentReference/cbc:ID` | (same) | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceReferencedDocument/ram:IssuerAssignedID` | |
| BT-26 | Preceding Invoice issue date | 0..1 | `cac:BillingReference/cac:InvoiceDocumentReference/cbc:IssueDate` | (same) | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:InvoiceReferencedDocument/ram:FormattedIssueDateTime/qdt:DateTimeString` | CII: @format="102" |

## BG-4 SELLER (1..1)

Base path — UBL: `cac:AccountingSupplierParty/cac:Party` | CII: `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:SellerTradeParty`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-27 | Seller name | 1..1 | `cac:PartyLegalEntity/cbc:RegistrationName` | (same) | `ram:Name` | |
| BT-28 | Seller trading name | 0..1 | `cac:PartyName/cbc:Name` | (same) | `ram:SpecifiedLegalOrganization/ram:TradingBusinessName` | |
| BT-29 | Seller identifier | 0..n | `cac:PartyIdentification/cbc:ID` | (same) | `ram:ID` or `ram:GlobalID` | CII: GlobalID for global identifiers |
| BT-29-1 | Seller identifier scheme ID | 0..1 | `cac:PartyIdentification/cbc:ID/@schemeID` | (same) | `ram:GlobalID/@schemeID` | |
| BT-30 | Seller legal registration ID | 0..1 | `cac:PartyLegalEntity/cbc:CompanyID` | (same) | `ram:SpecifiedLegalOrganization/ram:ID` | |
| BT-30-1 | Seller legal registration ID scheme ID | 0..1 | `cac:PartyLegalEntity/cbc:CompanyID/@schemeID` | (same) | `ram:SpecifiedLegalOrganization/ram:ID/@schemeID` | |
| BT-31 | Seller VAT identifier | 0..1 | `cac:PartyTaxScheme/cbc:CompanyID` | (same) | `ram:SpecifiedTaxRegistration/ram:ID` | UBL: TaxScheme/ID="VAT"; CII: @schemeID="VA" |
| BT-32 | Seller tax registration ID | 0..1 | `cac:PartyTaxScheme/cbc:CompanyID` | (same) | `ram:SpecifiedTaxRegistration/ram:ID` | UBL: TaxScheme/ID!="VAT"; CII: @schemeID="FC" |
| BT-33 | Seller additional legal info | 0..1 | `cac:PartyLegalEntity/cbc:CompanyLegalForm` | (same) | `ram:Description` | |
| BT-34 | Seller electronic address | 1..1 | `cbc:EndpointID` | (same) | `ram:URIUniversalCommunication/ram:URIID` | |
| BT-34-1 | Seller electronic address scheme ID | 1..1 | `cbc:EndpointID/@schemeID` | (same) | `ram:URIUniversalCommunication/ram:URIID/@schemeID` | |

## BG-5 SELLER POSTAL ADDRESS (1..1)

Base path — UBL: `cac:AccountingSupplierParty/cac:Party/cac:PostalAddress` | CII: `.../ram:SellerTradeParty/ram:PostalTradeAddress`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-35 | Seller address line 1 | 0..1 | `cbc:StreetName` | (same) | `ram:LineOne` | |
| BT-36 | Seller address line 2 | 0..1 | `cbc:AdditionalStreetName` | (same) | `ram:LineTwo` | |
| BT-162 | Seller address line 3 | 0..1 | `cac:AddressLine/cbc:Line` | (same) | `ram:LineThree` | |
| BT-37 | Seller city | 0..1 | `cbc:CityName` | (same) | `ram:CityName` | |
| BT-38 | Seller post code | 0..1 | `cbc:PostalZone` | (same) | `ram:PostcodeCode` | |
| BT-39 | Seller country subdivision | 0..1 | `cbc:CountrySubentity` | (same) | `ram:CountrySubDivisionName` | |
| BT-40 | Seller country code | 1..1 | `cac:Country/cbc:IdentificationCode` | (same) | `ram:CountryID` | |

## BG-6 SELLER CONTACT (0..1)

Base path — UBL: `cac:AccountingSupplierParty/cac:Party/cac:Contact` | CII: `.../ram:SellerTradeParty/ram:DefinedTradeContact`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-41 | Seller contact point | 0..1 | `cbc:Name` | (same) | `ram:PersonName` | |
| BT-42 | Seller contact telephone | 0..1 | `cbc:Telephone` | (same) | `ram:TelephoneUniversalCommunication/ram:CompleteNumber` | |
| BT-43 | Seller contact email | 0..1 | `cbc:ElectronicMail` | (same) | `ram:EmailURIUniversalCommunication/ram:URIID` | |

## BG-7 BUYER (1..1)

Base path — UBL: `cac:AccountingCustomerParty/cac:Party` | CII: `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeAgreement/ram:BuyerTradeParty`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-44 | Buyer name | 1..1 | `cac:PartyLegalEntity/cbc:RegistrationName` | (same) | `ram:Name` | |
| BT-45 | Buyer trading name | 0..1 | `cac:PartyName/cbc:Name` | (same) | `ram:SpecifiedLegalOrganization/ram:TradingBusinessName` | |
| BT-46 | Buyer identifier | 0..n | `cac:PartyIdentification/cbc:ID` | (same) | `ram:ID` or `ram:GlobalID` | |
| BT-46-1 | Buyer identifier scheme ID | 0..1 | `cac:PartyIdentification/cbc:ID/@schemeID` | (same) | `ram:GlobalID/@schemeID` | |
| BT-47 | Buyer legal registration ID | 0..1 | `cac:PartyLegalEntity/cbc:CompanyID` | (same) | `ram:SpecifiedLegalOrganization/ram:ID` | |
| BT-47-1 | Buyer legal registration ID scheme ID | 0..1 | `cac:PartyLegalEntity/cbc:CompanyID/@schemeID` | (same) | `ram:SpecifiedLegalOrganization/ram:ID/@schemeID` | |
| BT-48 | Buyer VAT identifier | 0..1 | `cac:PartyTaxScheme/cbc:CompanyID` | (same) | `ram:SpecifiedTaxRegistration/ram:ID` | CII: @schemeID="VA" |
| BT-49 | Buyer electronic address | 1..1 | `cbc:EndpointID` | (same) | `ram:URIUniversalCommunication/ram:URIID` | |
| BT-49-1 | Buyer electronic address scheme ID | 1..1 | `cbc:EndpointID/@schemeID` | (same) | `ram:URIUniversalCommunication/ram:URIID/@schemeID` | |

## BG-8 BUYER POSTAL ADDRESS (1..1)

Base path — UBL: `cac:AccountingCustomerParty/cac:Party/cac:PostalAddress` | CII: `.../ram:BuyerTradeParty/ram:PostalTradeAddress`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-50 | Buyer address line 1 | 0..1 | `cbc:StreetName` | (same) | `ram:LineOne` | |
| BT-51 | Buyer address line 2 | 0..1 | `cbc:AdditionalStreetName` | (same) | `ram:LineTwo` | |
| BT-163 | Buyer address line 3 | 0..1 | `cac:AddressLine/cbc:Line` | (same) | `ram:LineThree` | |
| BT-52 | Buyer city | 0..1 | `cbc:CityName` | (same) | `ram:CityName` | |
| BT-53 | Buyer post code | 0..1 | `cbc:PostalZone` | (same) | `ram:PostcodeCode` | |
| BT-54 | Buyer country subdivision | 0..1 | `cbc:CountrySubentity` | (same) | `ram:CountrySubDivisionName` | |
| BT-55 | Buyer country code | 1..1 | `cac:Country/cbc:IdentificationCode` | (same) | `ram:CountryID` | |

## BG-9 BUYER CONTACT (0..1)

Base path — UBL: `cac:AccountingCustomerParty/cac:Party/cac:Contact` | CII: `.../ram:BuyerTradeParty/ram:DefinedTradeContact`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-56 | Buyer contact point | 0..1 | `cbc:Name` | (same) | `ram:PersonName` | |
| BT-57 | Buyer contact telephone | 0..1 | `cbc:Telephone` | (same) | `ram:TelephoneUniversalCommunication/ram:CompleteNumber` | |
| BT-58 | Buyer contact email | 0..1 | `cbc:ElectronicMail` | (same) | `ram:EmailURIUniversalCommunication/ram:URIID` | |

## BG-10 PAYEE (0..1)

Base path — UBL: `cac:PayeeParty` | CII: `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:PayeeTradeParty`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-59 | Payee name | 1..1 | `cac:PartyName/cbc:Name` | (same) | `ram:Name` | |
| BT-60 | Payee identifier | 0..1 | `cac:PartyIdentification/cbc:ID` | (same) | `ram:ID` or `ram:GlobalID` | |
| BT-60-1 | Payee identifier scheme ID | 0..1 | `cac:PartyIdentification/cbc:ID/@schemeID` | (same) | `ram:GlobalID/@schemeID` | |
| BT-61 | Payee legal registration ID | 0..1 | `cac:PartyLegalEntity/cbc:CompanyID` | (same) | `ram:SpecifiedLegalOrganization/ram:ID` | |
| BT-61-1 | Payee legal registration ID scheme ID | 0..1 | `cac:PartyLegalEntity/cbc:CompanyID/@schemeID` | (same) | `ram:SpecifiedLegalOrganization/ram:ID/@schemeID` | |

## BG-11 SELLER TAX REPRESENTATIVE PARTY (0..1)

Base path — UBL: `cac:TaxRepresentativeParty` | CII: `.../ram:ApplicableHeaderTradeAgreement/ram:SellerTaxRepresentativeTradeParty`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-62 | Seller tax rep name | 1..1 | `cac:PartyName/cbc:Name` | (same) | `ram:Name` | |
| BT-63 | Seller tax rep VAT ID | 1..1 | `cac:PartyTaxScheme/cbc:CompanyID` | (same) | `ram:SpecifiedTaxRegistration/ram:ID` | CII: @schemeID="VA" |

## BG-12 SELLER TAX REPRESENTATIVE POSTAL ADDRESS (1..1)

Base path — UBL: `cac:TaxRepresentativeParty/cac:PostalAddress` | CII: `.../ram:SellerTaxRepresentativeTradeParty/ram:PostalTradeAddress`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-64 | Tax rep address line 1 | 0..1 | `cbc:StreetName` | (same) | `ram:LineOne` | |
| BT-65 | Tax rep address line 2 | 0..1 | `cbc:AdditionalStreetName` | (same) | `ram:LineTwo` | |
| BT-164 | Tax rep address line 3 | 0..1 | `cac:AddressLine/cbc:Line` | (same) | `ram:LineThree` | |
| BT-66 | Tax rep city | 0..1 | `cbc:CityName` | (same) | `ram:CityName` | |
| BT-67 | Tax rep post code | 0..1 | `cbc:PostalZone` | (same) | `ram:PostcodeCode` | |
| BT-68 | Tax rep country subdivision | 0..1 | `cbc:CountrySubentity` | (same) | `ram:CountrySubDivisionName` | |
| BT-69 | Tax rep country code | 1..1 | `cac:Country/cbc:IdentificationCode` | (same) | `ram:CountryID` | |

## BG-13 DELIVERY INFORMATION (0..1)

Base path — UBL: `cac:Delivery` | CII: `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeDelivery`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-70 | Deliver to party name | 0..1 | `cac:DeliveryParty/cac:PartyName/cbc:Name` | (same) | `ram:ShipToTradeParty/ram:Name` | |
| BT-71 | Deliver to location ID | 0..1 | `cac:DeliveryLocation/cbc:ID` | (same) | `ram:ShipToTradeParty/ram:ID` or `ram:GlobalID` | |
| BT-71-1 | Deliver to location ID scheme ID | 0..1 | `cac:DeliveryLocation/cbc:ID/@schemeID` | (same) | `ram:ShipToTradeParty/ram:GlobalID/@schemeID` | |
| BT-72 | Actual delivery date | 0..1 | `cbc:ActualDeliveryDate` | (same) | `ram:ActualDeliverySupplyChainEvent/ram:OccurrenceDateTime/udt:DateTimeString` | CII: @format="102" |

## BG-14 DELIVERY OR INVOICE PERIOD (0..1)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-73 | Invoicing period start date | 0..1 | `cac:InvoicePeriod/cbc:StartDate` | `cac:InvoicePeriod/cbc:StartDate` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:StartDateTime/udt:DateTimeString` | CII: @format="102" |
| BT-74 | Invoicing period end date | 0..1 | `cac:InvoicePeriod/cbc:EndDate` | `cac:InvoicePeriod/cbc:EndDate` | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:BillingSpecifiedPeriod/ram:EndDateTime/udt:DateTimeString` | CII: @format="102" |

## BG-15 DELIVER TO ADDRESS (0..1)

Base path — UBL: `cac:Delivery/cac:DeliveryLocation/cac:Address` | CII: `.../ram:ApplicableHeaderTradeDelivery/ram:ShipToTradeParty/ram:PostalTradeAddress`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-75 | Deliver to address line 1 | 0..1 | `cbc:StreetName` | (same) | `ram:LineOne` | |
| BT-76 | Deliver to address line 2 | 0..1 | `cbc:AdditionalStreetName` | (same) | `ram:LineTwo` | |
| BT-165 | Deliver to address line 3 | 0..1 | `cac:AddressLine/cbc:Line` | (same) | `ram:LineThree` | |
| BT-77 | Deliver to city | 0..1 | `cbc:CityName` | (same) | `ram:CityName` | |
| BT-78 | Deliver to post code | 0..1 | `cbc:PostalZone` | (same) | `ram:PostcodeCode` | |
| BT-79 | Deliver to country subdivision | 0..1 | `cbc:CountrySubentity` | (same) | `ram:CountrySubDivisionName` | |
| BT-80 | Deliver to country code | 1..1 | `cac:Country/cbc:IdentificationCode` | (same) | `ram:CountryID` | |

## BG-16 PAYMENT INSTRUCTIONS (0..1)

Base path — UBL: `cac:PaymentMeans` | CII: `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementPaymentMeans`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-81 | Payment means type code | 1..1 | `cbc:PaymentMeansCode` | (same) | `ram:TypeCode` | |
| BT-82 | Payment means text | 0..1 | `cbc:PaymentMeansCode/@Name` | (same) | `ram:Information` | UBL: attribute; CII: element |
| BT-83 | Remittance information | 0..1 | `cbc:PaymentID` | (same) | `../ram:PaymentReference` | CII: on ApplicableHeaderTradeSettlement |

## BG-17 CREDIT TRANSFER (0..n)

Base path — UBL: `cac:PaymentMeans/cac:PayeeFinancialAccount` | CII: `.../ram:SpecifiedTradeSettlementPaymentMeans/ram:PayeePartyCreditorFinancialAccount`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-84 | Payment account identifier | 1..1 | `cbc:ID` | (same) | `ram:IBANID` or `ram:ProprietaryID` | |
| BT-85 | Payment account name | 0..1 | `cbc:Name` | (same) | `ram:AccountName` | |
| BT-86 | Payment service provider ID | 0..1 | `cac:FinancialInstitutionBranch/cbc:ID` | (same) | `../ram:PayeeSpecifiedCreditorFinancialInstitution/ram:BICID` | |

## BG-18 PAYMENT CARD INFORMATION (0..1)

Base path — UBL: `cac:PaymentMeans/cac:CardAccount` | CII: `.../ram:SpecifiedTradeSettlementPaymentMeans/ram:ApplicableTradeSettlementFinancialCard`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-87 | Payment card PAN | 1..1 | `cbc:PrimaryAccountNumberID` | (same) | `ram:ID` | |
| BT-88 | Payment card holder name | 0..1 | `cbc:HolderName` | (same) | `ram:CardholderName` | |

## BG-19 DIRECT DEBIT (0..1)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-89 | Mandate reference ID | 0..1 | `cac:PaymentMeans/cac:PaymentMandate/cbc:ID` | (same) | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradePaymentTerms/ram:DirectDebitMandateID` | |
| BT-90 | Bank assigned creditor ID | 0..1 | `cac:AccountingSupplierParty/cac:Party/cac:PartyIdentification/cbc:ID` or `cac:PayeeParty/cac:PartyIdentification/cbc:ID` | (same) | `rsm:SupplyChainTradeTransaction/ram:ApplicableHeaderTradeSettlement/ram:CreditorReferenceID` | UBL: @schemeID="SEPA" |
| BT-91 | Debited account ID | 0..1 | `cac:PaymentMeans/cac:PaymentMandate/cac:PayerFinancialAccount/cbc:ID` | (same) | `.../ram:SpecifiedTradeSettlementPaymentMeans/ram:PayerPartyDebtorFinancialAccount/ram:IBANID` | |

## BG-20 DOCUMENT LEVEL ALLOWANCES (0..n)

Base path — UBL: `cac:AllowanceCharge` (ChargeIndicator=false) | CII: `.../ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeAllowanceCharge` (ChargeIndicator=false)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-92 | Document level allowance amount | 1..1 | `cbc:Amount` | (same) | `ram:ActualAmount` | |
| BT-93 | Document level allowance base amount | 0..1 | `cbc:BaseAmount` | (same) | `ram:BasisAmount` | |
| BT-94 | Document level allowance percentage | 0..1 | `cbc:MultiplierFactorNumeric` | (same) | `ram:CalculationPercent` | |
| BT-95 | Document level allowance VAT category code | 1..1 | `cac:TaxCategory/cbc:ID` | (same) | `ram:CategoryTradeTax/ram:CategoryCode` | |
| BT-96 | Document level allowance VAT rate | 0..1 | `cac:TaxCategory/cbc:Percent` | (same) | `ram:CategoryTradeTax/ram:RateApplicablePercent` | |
| BT-97 | Document level allowance reason | 0..1 | `cbc:AllowanceChargeReason` | (same) | `ram:Reason` | |
| BT-98 | Document level allowance reason code | 0..1 | `cbc:AllowanceChargeReasonCode` | (same) | `ram:ReasonCode` | |

## BG-21 DOCUMENT LEVEL CHARGES (0..n)

Base path — UBL: `cac:AllowanceCharge` (ChargeIndicator=true) | CII: `.../ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeAllowanceCharge` (ChargeIndicator=true)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-99 | Document level charge amount | 1..1 | `cbc:Amount` | (same) | `ram:ActualAmount` | |
| BT-100 | Document level charge base amount | 0..1 | `cbc:BaseAmount` | (same) | `ram:BasisAmount` | |
| BT-101 | Document level charge percentage | 0..1 | `cbc:MultiplierFactorNumeric` | (same) | `ram:CalculationPercent` | |
| BT-102 | Document level charge VAT category code | 1..1 | `cac:TaxCategory/cbc:ID` | (same) | `ram:CategoryTradeTax/ram:CategoryCode` | |
| BT-103 | Document level charge VAT rate | 0..1 | `cac:TaxCategory/cbc:Percent` | (same) | `ram:CategoryTradeTax/ram:RateApplicablePercent` | |
| BT-104 | Document level charge reason | 0..1 | `cbc:AllowanceChargeReason` | (same) | `ram:Reason` | |
| BT-105 | Document level charge reason code | 0..1 | `cbc:AllowanceChargeReasonCode` | (same) | `ram:ReasonCode` | |

## BG-22 DOCUMENT TOTALS (1..1)

Base path — UBL: `cac:LegalMonetaryTotal` | CII: `.../ram:ApplicableHeaderTradeSettlement/ram:SpecifiedTradeSettlementHeaderMonetarySummation`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-106 | Sum of Invoice line net amount | 1..1 | `cbc:LineExtensionAmount` | (same) | `ram:LineTotalAmount` | |
| BT-107 | Sum of allowances on document level | 0..1 | `cbc:AllowanceTotalAmount` | (same) | `ram:AllowanceTotalAmount` | |
| BT-108 | Sum of charges on document level | 0..1 | `cbc:ChargeTotalAmount` | (same) | `ram:ChargeTotalAmount` | |
| BT-109 | Invoice total amount without VAT | 1..1 | `cbc:TaxExclusiveAmount` | (same) | `ram:TaxBasisTotalAmount` | |
| BT-110 | Invoice total VAT amount | 0..1 | `cac:TaxTotal/cbc:TaxAmount` | (same) | `ram:TaxTotalAmount` | CII: @currencyID=BT-5 |
| BT-111 | Invoice total VAT amount in accounting currency | 0..1 | `cac:TaxTotal/cbc:TaxAmount` (2nd TaxTotal) | (same) | `ram:TaxTotalAmount` | CII: @currencyID=BT-6 |
| BT-112 | Invoice total amount with VAT | 1..1 | `cbc:TaxInclusiveAmount` | (same) | `ram:GrandTotalAmount` | |
| BT-113 | Paid amount | 0..1 | `cbc:PrepaidAmount` | (same) | `ram:TotalPrepaidAmount` | |
| BT-114 | Rounding amount | 0..1 | `cbc:PayableRoundingAmount` | (same) | `ram:RoundingAmount` | |
| BT-115 | Amount due for payment | 1..1 | `cbc:PayableAmount` | (same) | `ram:DuePayableAmount` | |

## BG-23 VAT BREAKDOWN (1..n)

Base path — UBL: `cac:TaxTotal/cac:TaxSubtotal` | CII: `.../ram:ApplicableHeaderTradeSettlement/ram:ApplicableTradeTax`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-116 | VAT category taxable amount | 1..1 | `cbc:TaxableAmount` | (same) | `ram:BasisAmount` | |
| BT-117 | VAT category tax amount | 1..1 | `cbc:TaxAmount` | (same) | `ram:CalculatedAmount` | |
| BT-118 | VAT category code | 1..1 | `cac:TaxCategory/cbc:ID` | (same) | `ram:CategoryCode` | |
| BT-119 | VAT category rate | 0..1 | `cac:TaxCategory/cbc:Percent` | (same) | `ram:RateApplicablePercent` | |
| BT-120 | VAT exemption reason text | 0..1 | `cac:TaxCategory/cbc:TaxExemptionReason` | (same) | `ram:ExemptionReason` | |
| BT-121 | VAT exemption reason code | 0..1 | `cac:TaxCategory/cbc:TaxExemptionReasonCode` | (same) | `ram:ExemptionReasonCode` | |

## BG-24 ADDITIONAL SUPPORTING DOCUMENTS (0..n)

Base path — UBL: `cac:AdditionalDocumentReference` | CII: `.../ram:ApplicableHeaderTradeAgreement/ram:AdditionalReferencedDocument` (TypeCode="916")

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-122 | Supporting document reference | 1..1 | `cbc:ID` | (same) | `ram:IssuerAssignedID` | |
| BT-123 | Supporting document description | 0..1 | `cbc:DocumentDescription` | (same) | `ram:Name` | |
| BT-124 | External document location | 0..1 | `cac:Attachment/cac:ExternalReference/cbc:URI` | (same) | `ram:URIID` | |
| BT-125 | Attached document | 0..1 | `cac:Attachment/cbc:EmbeddedDocumentBinaryObject` | (same) | `ram:AttachmentBinaryObject` | |
| BT-125-1 | Attached document Mime code | 1..1 | `cac:Attachment/cbc:EmbeddedDocumentBinaryObject/@mimeCode` | (same) | `ram:AttachmentBinaryObject/@mimeCode` | |
| BT-125-2 | Attached document Filename | 0..1 | `cac:Attachment/cbc:EmbeddedDocumentBinaryObject/@filename` | (same) | `ram:AttachmentBinaryObject/@filename` | |

## BG-25 INVOICE LINE (1..n)

Base path — UBL Invoice: `cac:InvoiceLine` | UBL Credit Note: `cac:CreditNoteLine` | CII: `rsm:SupplyChainTradeTransaction/ram:IncludedSupplyChainTradeLineItem`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-126 | Invoice line identifier | 1..1 | `cbc:ID` | `cbc:ID` | `ram:AssociatedDocumentLineDocument/ram:LineID` | |
| BT-127 | Invoice line note | 0..1 | `cbc:Note` | `cbc:Note` | `ram:AssociatedDocumentLineDocument/ram:IncludedNote/ram:Content` | |
| BT-128 | Invoice line object identifier | 0..1 | `cac:DocumentReference/cbc:ID` | `cac:DocumentReference/cbc:ID` | `ram:SpecifiedLineTradeSettlement/ram:AdditionalReferencedDocument/ram:IssuerAssignedID` | UBL: DocumentTypeCode=130; CII: TypeCode="130" |
| BT-128-1 | Invoice line object identifier scheme ID | 0..1 | `cac:DocumentReference/cbc:ID/@schemeID` | `cac:DocumentReference/cbc:ID/@schemeID` | `ram:SpecifiedLineTradeSettlement/ram:AdditionalReferencedDocument/ram:ReferenceTypeCode` | |
| BT-129 | Invoiced quantity | 1..1 | `cbc:InvoicedQuantity` | `cbc:CreditedQuantity` | `ram:SpecifiedLineTradeDelivery/ram:BilledQuantity` | Different element in CN |
| BT-130 | Invoiced quantity unit of measure | 1..1 | `cbc:InvoicedQuantity/@unitCode` | `cbc:CreditedQuantity/@unitCode` | `ram:SpecifiedLineTradeDelivery/ram:BilledQuantity/@unitCode` | |
| BT-131 | Invoice line net amount | 1..1 | `cbc:LineExtensionAmount` | `cbc:LineExtensionAmount` | `ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeSettlementLineMonetarySummation/ram:LineTotalAmount` | |
| BT-132 | Referenced purchase order line reference | 0..1 | `cac:OrderLineReference/cbc:LineID` | `cac:OrderLineReference/cbc:LineID` | `ram:SpecifiedLineTradeAgreement/ram:BuyerOrderReferencedDocument/ram:LineID` | |
| BT-133 | Invoice line Buyer accounting reference | 0..1 | `cbc:AccountingCost` | `cbc:AccountingCost` | `ram:SpecifiedLineTradeSettlement/ram:ReceivableSpecifiedTradeAccountingAccount/ram:ID` | |

## BG-26 INVOICE LINE PERIOD (0..1)

Base path — UBL Invoice: `cac:InvoiceLine/cac:InvoicePeriod` | UBL CN: `cac:CreditNoteLine/cac:InvoicePeriod` | CII: `.../ram:SpecifiedLineTradeSettlement/ram:BillingSpecifiedPeriod`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-134 | Invoice line period start date | 0..1 | `cbc:StartDate` | `cbc:StartDate` | `ram:StartDateTime/udt:DateTimeString` | CII: @format="102" |
| BT-135 | Invoice line period end date | 0..1 | `cbc:EndDate` | `cbc:EndDate` | `ram:EndDateTime/udt:DateTimeString` | CII: @format="102" |

## BG-27 INVOICE LINE ALLOWANCES (0..n)

Base path — UBL Invoice: `cac:InvoiceLine/cac:AllowanceCharge` (ChargeIndicator=false) | UBL CN: `cac:CreditNoteLine/cac:AllowanceCharge` (ChargeIndicator=false) | CII: `.../ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge` (ChargeIndicator=false)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-136 | Invoice line allowance amount | 1..1 | `cbc:Amount` | `cbc:Amount` | `ram:ActualAmount` | |
| BT-137 | Invoice line allowance base amount | 0..1 | `cbc:BaseAmount` | `cbc:BaseAmount` | `ram:BasisAmount` | |
| BT-138 | Invoice line allowance percentage | 0..1 | `cbc:MultiplierFactorNumeric` | `cbc:MultiplierFactorNumeric` | `ram:CalculationPercent` | |
| BT-139 | Invoice line allowance reason | 0..1 | `cbc:AllowanceChargeReason` | `cbc:AllowanceChargeReason` | `ram:Reason` | |
| BT-140 | Invoice line allowance reason code | 0..1 | `cbc:AllowanceChargeReasonCode` | `cbc:AllowanceChargeReasonCode` | `ram:ReasonCode` | |

## BG-28 INVOICE LINE CHARGES (0..n)

Base path — UBL Invoice: `cac:InvoiceLine/cac:AllowanceCharge` (ChargeIndicator=true) | UBL CN: `cac:CreditNoteLine/cac:AllowanceCharge` (ChargeIndicator=true) | CII: `.../ram:SpecifiedLineTradeSettlement/ram:SpecifiedTradeAllowanceCharge` (ChargeIndicator=true)

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-141 | Invoice line charge amount | 1..1 | `cbc:Amount` | `cbc:Amount` | `ram:ActualAmount` | |
| BT-142 | Invoice line charge base amount | 0..1 | `cbc:BaseAmount` | `cbc:BaseAmount` | `ram:BasisAmount` | |
| BT-143 | Invoice line charge percentage | 0..1 | `cbc:MultiplierFactorNumeric` | `cbc:MultiplierFactorNumeric` | `ram:CalculationPercent` | |
| BT-144 | Invoice line charge reason | 0..1 | `cbc:AllowanceChargeReason` | `cbc:AllowanceChargeReason` | `ram:Reason` | |
| BT-145 | Invoice line charge reason code | 0..1 | `cbc:AllowanceChargeReasonCode` | `cbc:AllowanceChargeReasonCode` | `ram:ReasonCode` | |

## BG-29 PRICE DETAILS (1..1)

Base path — UBL Invoice: `cac:InvoiceLine/cac:Price` | UBL CN: `cac:CreditNoteLine/cac:Price` | CII: `.../ram:SpecifiedLineTradeAgreement`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-146 | Item net price | 1..1 | `cbc:PriceAmount` | `cbc:PriceAmount` | `ram:NetPriceProductTradePrice/ram:ChargeAmount` | |
| BT-147 | Item price discount | 0..1 | `cac:AllowanceCharge/cbc:Amount` (ChargeIndicator=false) | `cac:AllowanceCharge/cbc:Amount` | `ram:GrossPriceProductTradePrice/ram:AppliedTradeAllowanceCharge/ram:ActualAmount` | |
| BT-148 | Item gross price | 0..1 | `cac:AllowanceCharge/cbc:BaseAmount` (ChargeIndicator=false) | `cac:AllowanceCharge/cbc:BaseAmount` | `ram:GrossPriceProductTradePrice/ram:ChargeAmount` | |
| BT-149 | Item price base quantity | 0..1 | `cbc:BaseQuantity` | `cbc:BaseQuantity` | `ram:NetPriceProductTradePrice/ram:BasisQuantity` | |
| BT-150 | Item price base quantity unit of measure code | 0..1 | `cbc:BaseQuantity/@unitCode` | `cbc:BaseQuantity/@unitCode` | `ram:NetPriceProductTradePrice/ram:BasisQuantity/@unitCode` | |

## BG-30 LINE VAT INFORMATION (1..1)

Base path — UBL Invoice: `cac:InvoiceLine/cac:Item/cac:ClassifiedTaxCategory` | UBL CN: `cac:CreditNoteLine/cac:Item/cac:ClassifiedTaxCategory` | CII: `.../ram:SpecifiedLineTradeSettlement/ram:ApplicableTradeTax`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-151 | Invoiced item VAT category code | 1..1 | `cbc:ID` | `cbc:ID` | `ram:CategoryCode` | |
| BT-152 | Invoiced item VAT rate | 0..1 | `cbc:Percent` | `cbc:Percent` | `ram:RateApplicablePercent` | |

## BG-31 ITEM INFORMATION (1..1)

Base path — UBL Invoice: `cac:InvoiceLine/cac:Item` | UBL CN: `cac:CreditNoteLine/cac:Item` | CII: `.../ram:SpecifiedTradeProduct`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-153 | Item name | 1..1 | `cbc:Name` | `cbc:Name` | `ram:Name` | |
| BT-154 | Item description | 0..1 | `cbc:Description` | `cbc:Description` | `ram:Description` | |
| BT-155 | Item Seller's identifier | 0..1 | `cac:SellersItemIdentification/cbc:ID` | `cac:SellersItemIdentification/cbc:ID` | `ram:SellerAssignedID` | |
| BT-156 | Item Buyer's identifier | 0..1 | `cac:BuyersItemIdentification/cbc:ID` | `cac:BuyersItemIdentification/cbc:ID` | `ram:BuyerAssignedID` | |
| BT-157 | Item standard identifier | 0..1 | `cac:StandardItemIdentification/cbc:ID` | `cac:StandardItemIdentification/cbc:ID` | `ram:GlobalID` | |
| BT-157-1 | Item standard identifier scheme ID | 1..1 | `cac:StandardItemIdentification/cbc:ID/@schemeID` | `cac:StandardItemIdentification/cbc:ID/@schemeID` | `ram:GlobalID/@schemeID` | |
| BT-158 | Item classification identifier | 0..n | `cac:CommodityClassification/cbc:ItemClassificationCode` | `cac:CommodityClassification/cbc:ItemClassificationCode` | `ram:DesignatedProductClassification/ram:ClassCode` | |
| BT-158-1 | Item classification identifier scheme ID | 1..1 | `cbc:ItemClassificationCode/@listID` | `cbc:ItemClassificationCode/@listID` | `ram:ClassCode/@listID` | |
| BT-158-2 | Item classification identifier scheme version ID | 0..1 | `cbc:ItemClassificationCode/@listVersionID` | `cbc:ItemClassificationCode/@listVersionID` | `ram:ClassCode/@listVersionID` | |
| BT-159 | Item country of origin | 0..1 | `cac:OriginCountry/cbc:IdentificationCode` | `cac:OriginCountry/cbc:IdentificationCode` | `ram:OriginTradeCountry/ram:ID` | |

## BG-32 ITEM ATTRIBUTES (0..n)

Base path — UBL Invoice: `cac:InvoiceLine/cac:Item/cac:AdditionalItemProperty` | UBL CN: `cac:CreditNoteLine/cac:Item/cac:AdditionalItemProperty` | CII: `.../ram:SpecifiedTradeProduct/ram:ApplicableProductCharacteristic`

| ID | Name | Card. | UBL Invoice | UBL Credit Note | CII D16B | Notes |
|----|------|-------|-------------|-----------------|----------|-------|
| BT-160 | Item attribute name | 1..1 | `cbc:Name` | `cbc:Name` | `ram:Description` | CII uses Description, not Name |
| BT-161 | Item attribute value | 1..1 | `cbc:Value` | `cbc:Value` | `ram:Value` | |

## Key Differences Between UBL Invoice and UBL Credit Note

| ID | Difference |
|----|-----------|
| BT-3 | `cbc:InvoiceTypeCode` vs `cbc:CreditNoteTypeCode` |
| BT-9 | `cbc:DueDate` (Invoice, header level) vs `cac:PaymentMeans/cbc:PaymentDueDate` (Credit Note) |
| BT-11 | `cac:ProjectReference/cbc:ID` (Invoice) vs `cac:AdditionalDocumentReference/cbc:ID` (Credit Note UBL 2.1, no discriminator; UBL 2.2+ has `ProjectReference`) |
| BG-25 | `cac:InvoiceLine` vs `cac:CreditNoteLine` |
| BT-129 | `cbc:InvoicedQuantity` vs `cbc:CreditedQuantity` |

## Key Discriminators

| Context | CII | UBL |
|---------|-----|-----|
| BT-17 vs BT-18 vs BG-24 | `ram:AdditionalReferencedDocument/ram:TypeCode`: "50" (BT-17), "130" (BT-18), "916" (BG-24) | Invoice: BT-17=`OriginatorDocumentReference`, BT-18=`AdditionalDocumentReference` with `DocumentTypeCode=130`, BG-24=`AdditionalDocumentReference` without code. CN: same, except BT-18 uses `DocumentType="ATS"` instead of `DocumentTypeCode=130` |
| BT-11 in CN (UBL 2.1) | Not applicable (dedicated `SpecifiedProcuringProject/ID`) | UBL 2.1 CN: `AdditionalDocumentReference/ID` with no discriminator (UBL 2.2+ CN has `ProjectReference`) |
| BT-31 vs BT-32 (Seller VAT vs tax reg) | `ram:SpecifiedTaxRegistration/ram:ID/@schemeID`: "VA" (BT-31) vs "FC" (BT-32) | `cac:PartyTaxScheme/cac:TaxScheme/cbc:ID`: "VAT" (BT-31) vs other (BT-32) |
| BG-20 vs BG-21 (Allowances vs Charges) | `ram:SpecifiedTradeAllowanceCharge/ram:ChargeIndicator/udt:Indicator`: false vs true | `cac:AllowanceCharge/cbc:ChargeIndicator`: false vs true |
| BT-110 vs BT-111 (VAT amounts) | Two `ram:TaxTotalAmount` elements, distinguished by `@currencyID` (BT-5 vs BT-6) | Two `cac:TaxTotal` elements, distinguished by `cbc:TaxAmount/@currencyID` |
| CII Date format | All dates use `@format="102"` (YYYYMMDD) | UBL uses `xs:date` natively (YYYY-MM-DD) |
