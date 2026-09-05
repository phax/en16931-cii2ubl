# Plan: en16931-cii2ubl 4.0.0

Status: **A0–A17 done — all 18 action items complete.** Follow-ups after review: BT-122-1/BT-122-1-1 added to the "New in 2026" table and finding 7 (BT-218) recorded in the mapping document. One open question remains: the wording of `docs/00readme.txt` (see A17). · Created 2026-09-04 · Version: 4.0.0-SNAPSHOT · Branch: `v4`

## 1. Goal

Add the **EN 16931:2026** syntax binding alongside the existing **EN 16931:2017** binding, and
reduce the UBL target versions to exactly the two the two editions require.

| Edition | Input | Output | Spec identifier (BT-24) |
|---------|-------|--------|-------------------------|
| EN 16931:2017 | CII **D16B** | UBL **2.1** | `urn:cen.eu:en16931:2017` |
| EN 16931:2026 | CII **D25A** | UBL **2.5** | `urn:cen.eu:en16931:2026` |

UBL 2.2, 2.3 and 2.4 support is **dropped**. No cross-edition combinations
(no D16B→2.5, no D25A→2.1).

Mapping source: `docs/en16931-2026-syntax.md` — CEN/TS 16931-3-2:2026 clause 4.5 Table 10 (UBL
invoice) and clause 4.6 Table 12 (UBL credit note); CEN/TS 16931-3-3:2026 clause 4.5 Table 10 (CII).
Published version, treated as authoritative.

## 2. How to use this document

Each action item below is sized to fit **one working session**. Work them in order — later items
depend on earlier ones. At the end of a session:

1. Tick the item's checkbox and fill in its line in the [Progress log](#9-progress-log).
2. Run `mvn clean test`; it must be green before the item counts as done.
3. Commit. One commit per action item, message prefixed `[4.0.0 A<n>]`.

Do **not** re-verify section 4 — those checks were run once against the actual released
artifacts and schemas.

## 3. Locked decisions

| # | Decision | Rationale |
|---|----------|-----------|
| D1 | Two strict bindings only (D16B→2.1, D25A→2.5) | Each edition stays inside its own syntax pair, as the standard defines them |
| D2 | Sub-packages per edition (`.en2017` / `.en2026`) | Keeps the two editions from bleeding into each other; 4.0.0 is a major version so import breaks are acceptable |
| D3 | Test strategy: XSD validity **+ XPath assertions per BT** | No Schematron exists for 2026; XSD alone would not catch a BT written into the wrong element |
| D4 | CLI auto-detects the edition from BT-24; `--en-version 2017\|2026` overrides | Namespace-based detection is impossible (see 4.2); BT-24 is mandatory in every conformant instance |
| D5 | Edition detection is **public library API**, not CLI-only | Embedders face mixed inbound traffic too |
| D6 | The 2017 path stays behaviour-identical to 3.1.x | Enforced by the git-tracked `generated/toubl21/` output (A0) |
| D7 | `convertCIItoUBL (File, ErrorList)` is declared **abstract on `AbstractCIIToUBLConverterBase`** | The CLI needs one edition-independent entry point; this is the seam `CIIToUBLDispatcher` (A14) will route through. Added in A2. |

## 4. Verified preconditions

Checked 2026-09-04 against the released artifacts. Do not repeat.

### 4.1 Dependencies are available

| Artifact | Version | In BOM |
|----------|---------|--------|
| `com.helger.cii:ph-cii-d25a` | 4.1.2 | `ph-cii-parent-pom` 4.1.2 — already imported by the parent pom |
| `com.helger.ubl:ph-ubl25` | 10.2.1 | `ph-ubl-parent-pom` 10.2.1 — already imported by the parent pom |

### 4.2 Edition detection: namespace URIs do NOT work

D16B, D22B and D25A declare **byte-identical** namespace URIs — UN/CEFACT never versioned them:

| Prefix | D16B | D22B | D25A |
|--------|------|------|------|
| `rsm` | `urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100` | identical | identical |
| `ram` | `urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100` | identical | identical |
| `udt` | `urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100` | identical | identical |
| `qdt` | `urn:un:unece:uncefact:data:standard:QualifiedDataType:100` | identical | identical |

XSD sniffing does not work either: **all 103** existing D16B test files validate cleanly against
the D25A XSD (D25A is a superset for the EN 16931 subset).

The only reliable discriminator is **BT-24**:
`rsm:ExchangedDocumentContext/ram:GuidelineSpecifiedDocumentContextParameter/ram:ID`,
mandatory 1..1 in BG-2. Match on the **prefix** `urn:cen.eu:en16931:2026` — 82 of the 103 existing
files carry an XRechnung `#compliant#…` suffix after it.

### 4.3 Every element in the 2026 mapping exists in the target schemas

| Check | Result |
|-------|--------|
| 145 distinct `ram:`/`rsm:`/`udt:`/`qdt:` names in `en16931-2026-syntax.md` present in the D25A XSDs | 0 missing |
| 153 distinct `cbc:`/`cac:` names present in the UBL 2.5 XSDs | 0 missing |
| 32 context probes (element present under the *correct parent* type) | all pass |

Context probes that passed, i.e. the structurally new pieces are genuinely usable:
`/Invoice/cac:BuyerAssignedReference/{cbc:BuyerReference,cbc:BuyerReferenceCode}`,
`/Invoice/cac:CollectionInvoiceLine` + `/CreditNote/cac:CollectionCreditNoteLine`,
`cac:PaymentTerms/{cac:SettlementPeriod,cbc:SettlementDiscountPercent,cbc:SettlementDiscountAmount,cac:PenaltyPeriod,cac:PenaltyInterestRate,cbc:PenaltyAmount}`,
`cac:TaxCategory/cbc:SupplyTypeCode`, `cac:InvoiceLine/cbc:TaxInclusiveLineExtensionAmount`,
`/Invoice/cbc:IssueTime`, `/Invoice/cac:TaxExchangeRate/{cbc:CalculationRate,cbc:SourceCurrencyCode,cbc:TargetCurrencyCode}`,
`/Invoice/cac:DeliveryNoteDocumentReference`, `cac:ItemProperty/{cbc:NameCode,cbc:ValueQuantity}`,
and on CreditNote the natively available `cbc:DueDate` and `cac:ProjectReference`.

### 4.4 Scope of the change

| Metric | Value |
|--------|-------|
| Mapping rows in `en16931-2026-syntax.md` | 284 |
| — carried over from 2017 | 214 |
| — new in 2026 | 70 (63 BTs + 7 BGs: BG-33 … BG-39) |
| Existing converters, mutual difference | 52 diff lines between `CIIToUBL21Converter` and `CIIToUBL24Converter` (⇒ dropping 2.2/2.3/2.4 is pure deletion) |
| `AbstractCIIToUBLConverter` split | ≈350 lines edition-independent / ≈430 lines CII-D16B-typed |

### 4.4a Findings from the A3 port

| Finding | Consequence |
|---------|-------------|
| Every D25A accessor used by the base class matches D16B **by name** (`getValue`, `getSchemeID`, `isIndicator`, `hasGlobalIDEntries`, `getDuePayableAmount`, …) | `AbstractCIIToUBL2026Converter` is a pure import swap of the 2017 one - no logic changes |
| `cac:Item/cbc:Name` (BT-153) went **1..1 -> 0..n** between UBL 2.1 and 2.5 | Use `ItemType.addName (NameType)`; there is no `setName (String)`. Watch for the same cardinality widening on other UBL elements in A5-A7 |
| `UBL25Marshaller.invoice ().write (...)` returns `ESuccess.FAILURE` for a schema invalid document (verified with a deliberately empty `InvoiceType`) | Asserting `write (...).isSuccess ()` **is** a real XSD validity assertion; no separate xmllint step is needed |
| The UBL 2.5 XSDs cannot be compiled standalone by `xmllint` - they need the CCTS and xmldsig schemas from sibling `ph-xsds-*` jars | Validate through the marshaller (which resolves the full schema set), not through external tooling |
| UBL 2.5 mandatory Invoice children: `cbc:ID`, `cbc:IssueDate`, `cac:AccountingSupplierParty`, `cac:LegalMonetaryTotal`, `cac:InvoiceLine` (CreditNote: `cac:CreditNoteLine`) | Any 2026 test file must carry at least these, which is why the A3 skeleton reaches past its five planned BTs |

### 4.4b The 17 API deltas the A5 bulk port surfaced

Every one is a cardinality widening; there were no semantic surprises.

**CII D16B → D25A (element became 0..n):**

| Element | Handling |
|---------|----------|
| `NoteType/SubjectCode` | `getSubjectCodeValue ()` is gone — use `getSubjectCodeAtIndex (0)` |
| `TradeProductType/Description` | loop instead of a single `getDescription ()` |
| `HeaderTradeSettlementType/InvoiceReferencedDocument` | now loops, which finally matches BG-3's 0..n cardinality |
| `TradeSettlementPaymentMeansType/PayeePartyCreditorFinancialAccount` | `getPayeePartyCreditorFinancialAccountAtIndex (0)` |

**UBL 2.1 → 2.5 (element became 0..n):**

| Element | Handling |
|---------|----------|
| `AddressType`: `StreetName`, `AdditionalStreetName`, `CityName`, `PostalZone`, `CountrySubentity` | `addX (new XType (s))` |
| `AddressLineType/Line` | `addLine (new LineType (s))` |
| `PartyLegalEntityType/CompanyLegalForm` | `addCompanyLegalForm (…)`; the presence check becomes `!getCompanyLegalForm ().isEmpty ()` |
| `PaymentMeansType/CardAccount` | `addCardAccount (…)` |
| `ItemType/Name` | `addName (…)` |

`CompanyLegalForm` and `CardAccount` were already solved in the deleted `CIIToUBL24Converter`
(UBL 2.3+ shares the widening); those variants were recovered from `git show fb6bcba^` and reused.

### 4.4c Schema defaults that JAXB materialises

`qdt:AllowanceChargeReasonCodeType` in the CII D25A schema declares:

```xml
<xsd:attribute name="listID" type="xsd:token" default="4465_AllowanceChargeReasonCode"/>
<xsd:attribute name="listAgencyID" type="..." default="6"/>
```

JAXB materialises these defaults, so `getListID ()` returns a value **even when the instance
document has no such attribute**. Copying it blindly puts a bogus `@listID` on every BT-98 / BT-105
/ BT-140 / BT-145 reason code, which breaks the BT-177-1 discriminator.

Rule: propagate the list identifier **only** when it equals the fixed `5153` of BT-177-1 / BT-193-1.
Watch for the same pattern on other `qdt:` code types before copying any `@listID` / `@listAgencyID`.

### 4.4d One defect in the source mapping table — resolved

The source mapped **BT-218 "Line-level preceding invoice issue date"** to
`cac:InvoiceDocumentReference/cbc:IssueTime`, which is not implementable: `cbc:IssueTime` is an
`xs:time` in UBL, so a `CCYYMMDD` date cannot be written to it and the result would fail UBL 2.5
XSD validation.

This was raised as **finding 7** against the source documents and confirmed. The correction is now
recorded in the new "Findings in the Source Documents" section of `docs/en16931-2026-syntax.md`, and
the BT-218 row there reads `cbc:IssueDate`. Table and implementation agree — there is no longer any
knowing deviation.

### 4.4e BT-24 detection measured against the real corpus

`EEN16931Edition.detect` was run over all 103 existing CII test files:

| Result | Count | Detail |
|--------|-------|--------|
| Detected as EN 16931:2017 | 101 | includes 82 with an XRechnung `#compliant#…` suffix, which prefix matching handles |
| Undeterminable | 2 | `CII_business_example_01.xml` and `CII_business_example_02.xml` carry the legacy ZUGFeRD identifier `urn:ferd:CrossIndustryDocument:invoice:1p0:comfort` |

So ~2% of a real corpus cannot be auto-detected. This is concrete justification for D4's
`--en-version` override rather than a theoretical edge case, and the dispatcher converts these files
correctly once the edition is forced.

`DOMReader.readXMLDOM (File)` throws on a non-existent file, so `detect` guards with
`aFile.isFile ()` first.

### 4.4f Pre-existing CLI bug found in A15

Running the CLI without `--ubl-customizationid` **and** `--ubl-profileid` threw:

```
java.lang.NullPointerException: The value of 'CustomizationID' may not be null!
  at AbstractCIIToUBLConverter.setCustomizationID
  at CIIToUBLConverter.call
```

Both options have no default, so both fields are `null`, and the setters call
`ValueEnforcer.notNull`. The same unguarded setter chain is on `master` and in every released
3.x version, so the CLI's plain `java -jar … file.xml` invocation has been broken for a long time —
it was simply never exercised by a test, because the test suite only drives the library.

Fixed in A15 by setting the two values only when non-empty. Worth a note in the release notes for
users who worked around it by always passing both options.

### 4.4g Two gaps the A16 coverage guard found

`MappingCoverageTest` parses every row of `docs/en16931-2026-syntax.md` and fails if an identifier
is never named in `CIID25AToUBL25Converter`. Running it the first time reported 279 of 284, and the
shortfall was not just missing comments:

1. **BT-122-1 / BT-122-1-1 were genuinely unimplemented.** BG-24 requires
   `cbc:DocumentTypeCode = "916"` with `@listID = "1001"` since 2026. The 2017 binding had no such
   element — `isValidDocumentReferenceTypeCode` deliberately accepts only `50` and `130` — so the
   bulk port carried the suppression over. **These two rows appear in neither the 2017 document nor
   the "Business Terms and Groups New in 2026" table**, so both the A6 delta diff and the A8–A13
   pass over the "new" table missed them. 34 rows sit in that blind spot; the other 32 turned out
   to be implemented sub-identifiers.
2. **BT-91 did not fall back to `ram:ProprietaryID`.** 2026 allows it as an alternative to
   `ram:IBANID`, exactly as BT-84 already did on the creditor side.

Lesson: the mapping document's own "new in 2026" table was **not** a complete diff. BT-122-1 and
BT-122-1-1 have since been added to it, together with a note that "number above BT-165" is not a
complete test for what is new. The coverage test enforces the full row set regardless.

### 4.4h What can and cannot be shared between the two editions

The two edition base classes were **100% identical modulo package names**, and the two concrete
converters still share 175 verbatim lines. Nothing lifts for free: the CII `udt` types of D16B and
D25A are unrelated Java classes — they implement only `Serializable` and `IExplicitlyCloneable`,
with no common CCTS supertype — so a method taking a CII type cannot be shared.

What *can* be shared is the **logic**, by passing the already extracted values (the `parseIndicator`
pattern). Lifted into `AbstractCIIToUBLConverterBase`:

| Method | Why it was worth lifting |
|--------|--------------------------|
| `parseIndicator` | branching over the boolean/string choice plus error reporting |
| `isInvoiceType` | the BT-3 code sets and the BT-115 sign fallback — the actual business rule |
| `copyAmount` | the default-currency fallback and trailing-zero normalisation |
| `copyQuantity` | trailing-zero normalisation |
| `copyName` | the "avoid empty element" guard |
| `isUsableGlobalID` | the BT-29/46/60/71 usability rule |

Each edition keeps a thin typed adapter. 352 → 287 lines per edition class.

**`copyID` and `copyCode` were deliberately left duplicated.** They copy 8 and 10 attributes with
no logic at all, so sharing them means 8–10 consecutive `String` parameters. Measured against the
generated corpus, 6 of `copyID`'s attributes and 6 of `copyCode`'s **never appear in any output
file**, so a transposed argument would be invisible to every existing test. The duplication is
cheaper than that risk.

For the same reason the lifted `copyQuantity` / `copyAmount` / `copyName` are now covered by
explicit unit tests in `AbstractCIIToUBLConverterBaseTest` that give every attribute a distinct
value — verified to fail on a deliberate transposition.

The 175 identical lines in the concrete converters (`_convertParty`, `_convertContact`,
`_createUBLOrderRef`, …) are typed to edition-specific CII **and** UBL classes on both sides, so
sharing them would need generics over both models. Not attempted.

### 4.4i Code lists: what actually differs between the editions

Checked every code list the converter hard-codes against the CEN/TC 434 code list registry of
EN 16931:2026 (22 lists, 6 of them enumerated) and against the EN 16931 validation artefacts 1.3.16
for the 2017 side.

**UNTDID 1001 (BT-3 Invoice type code) — genuinely different, now per edition:**

| | EN 16931:2017 | EN 16931:2026 |
|---|---|---|
| Invoice codes | 50 | 44 |
| Credit note codes | 13 | 11 |
| `471 472 473 500 501 502 503` | applicable | **no longer applicable** |
| `81` | on **both** lists — Invoice wins, because it is evaluated first | **Credit Note only** |

The sets therefore moved from `AbstractCIIToUBLConverterBase` into the two edition base classes,
and `isInvoiceType` takes them as parameters.

**A pre-existing bug surfaced by the comparison:** the shared constants had `502` and `503` in
`INVOICE_TYPE_CODES`, but the 2017 UBL artefacts list both as **credit note** codes only. A
document with BT-3 = 502 or 503 was therefore converted to an Invoice. The 2017 sets are now taken
verbatim from `EN16931-UBL-validation.xslt`, which fixes it. No test file uses those codes, so the
golden output is unaffected.

**Checked and unchanged:**

| List | Used for | Result |
|------|----------|--------|
| UNTDID 2005 / 2475 | BT-8, `mapDueDateTypeCode` | 2026 enumerates exactly `5→3`, `29→35`, `72→432` — the existing mapping is correct |
| UNTDID 4461 | BT-81 payment means | 2026 highlights `30 48 49 57 58 59`; the list is explicitly *not* complete, so the extra `42` the converter accepts for credit transfer stays valid |
| UNTDID 5305 | BT-95/102/118/151 VAT category | 9 codes, but the converter copies the value through without a whitelist |
| UNTDID 5189 | BT-98/140 allowance reason | 19 codes, copied through |
| MIME | BT-125-1 | 6 types, copied through |
| 5153, 1153, 7161, 4451, 6313, 7143, 6523, EAS, SEPA, SUPPLY, VATEX, 4217, 3166-1, Rec20/21 | various | not enumerated in the source; the converter copies values through, so nothing to align |

### 4.5 The D25A JAXB model is a separate Java package

| Release | Package |
|---------|---------|
| D16B | `un.unece.uncefact.data.standard.crossindustryinvoice._100` (+ `…reusableaggregatebusinessinformationentity._100`, `…unqualifieddatatype._100`, `…qualifieddatatype._100`) |
| D25A | `un.unece.uncefact.data.standard.cii.d25a` (+ `.rabie`, `.udt`, `.qdt`) |

Class names are identical, packages are not. **Generics cannot bridge this.** The CII input side of
the 2026 converter is a port, not a refactor.

## 5. Target structure

```
com.helger.en16931.cii2ubl                    shared — no CII and no UBL types
  EUBLCreationMode                            (unchanged)
  CIIToUBLVersion                             (unchanged)
  EEN16931Edition                    NEW      { EN2017, EN2026 } + detect (File|Node|Document)
  CIIToUBLDispatcher                 NEW      parse -> detect -> route
  AbstractCIIToUBLConverterBase      NEW      ~350 lines lifted from today's base:
                                              settings API (IMPLTYPE fluent),
                                              buildInfo/buildWarn/buildError,
                                              getDatePattern, parseDate (String),
                                              isPaymentMeansCode*, isValidDocumentReferenceTypeCode,
                                              isOriginatorDocumentReferenceTypeCode,
                                              mapDueDateTypeCode, isLT0Strict,
                                              CREDIT_NOTE_TYPE_CODES / INVOICE_TYPE_CODES

com.helger.en16931.cii2ubl.en2017             CII D16B -> UBL 2.1
  AbstractCIIToUBL2017Converter               ~430 D16B-typed lines: parseDate overloads,
                                              parseIndicator, copyID/copyName/copyCode/
                                              copyQuantity/copyAmount, canUseGlobalID,
                                              getAllUsableGlobalIDs, swapQuantityAndPriceIfNeeded,
                                              isInvoiceType, convertCIItoUBL (File)
  CIID16BToUBL21Converter                     was CIIToUBL21Converter

com.helger.en16931.cii2ubl.en2026             CII D25A -> UBL 2.5
  AbstractCIIToUBL2026Converter               same shape, D25A-typed
  CIID25AToUBL25Converter
```

Deleted: `CIIToUBL22Converter`, `CIIToUBL23Converter`, `CIIToUBL24Converter` and their tests.

## 6. Action items

### Phase 1 — Prepare and shrink

- [x] **A0 — Establish the regression baseline** · ~15 min
  - No new code needed. `CIIToUBL21ConverterTest.testConvertAndValidateAll` already writes all 102
    converted documents to `en16931-cii2ubl/generated/toubl21/`, and that folder is **tracked in
    git**. It is the golden baseline.
  - Run `mvn clean test -pl en16931-cii2ubl -Dtest=CIIToUBL21ConverterTest` and confirm
    `git status --short en16931-cii2ubl/generated/toubl21/` reports **nothing**.
  - If it does report changes, the committed files are stale — commit them first, so the baseline
    is real before A1/A2 touch anything.
  - **Done when:** `generated/toubl21/` is clean after a full test run.
  - **Why first:** `git diff` on that folder is the proof that A1/A2 do not change 2017 behaviour (D6).
  - A1 additionally deletes `generated/toubl22/`, `toubl23/` and `toubl24/` (102 files each).

- [x] **A1 — Drop UBL 2.2 / 2.3 / 2.4** · ~2 h
  - Delete `CIIToUBL22Converter`, `CIIToUBL23Converter`, `CIIToUBL24Converter` and
    `CIIToUBL22ConverterTest`, `CIIToUBL23ConverterTest`, `CIIToUBL24ConverterTest`.
  - Remove `ph-ubl22` / `ph-ubl23` / `ph-ubl24` from `en16931-cii2ubl/pom.xml` and the CLI pom.
  - Remove the corresponding `--ubl` branches and `UBL2xMarshaller` calls from
    `cli/CIIToUBLConverter.java` (leave the option in place for now; A15 reworks it).
  - **Done when:** `mvn clean test` green, A0 golden test still byte-identical.

- [x] **A2 — Split the base class, introduce `.en2017`, bump to 4.0.0-SNAPSHOT** · ~3 h
  - Create `AbstractCIIToUBLConverterBase` with the ~350 edition-independent lines.
  - Move the remainder to `en2017.AbstractCIIToUBL2017Converter`.
  - Move + rename `CIIToUBL21Converter` → `en2017.CIID16BToUBL21Converter`.
  - Set `4.0.0-SNAPSHOT` in both poms and the parent pom.
  - **Done when:** `mvn clean test` green, A0 golden output **byte-identical**.
  - **Note:** pure move/rename. If any *behaviour* changes here, it is a bug.

### Phase 2 — 2026 skeleton and test harness

- [x] **A3 — `.en2026` scaffolding + typed base port** · ~4 h
  - Add `ph-cii-d25a` and `ph-ubl25` dependencies.
  - Port `AbstractCIIToUBL2017Converter`'s D25A-typed counterpart:
    `en2026.AbstractCIIToUBL2026Converter`.
  - Create `en2026.CIID25AToUBL25Converter` producing only BT-1, BT-2, BT-3, BT-5, BT-24 —
    enough for a schema-valid skeleton.
  - **Done when:** compiles; a hand-written minimal D25A file yields an XSD-valid UBL 2.5 Invoice.

- [x] **A4 — Test harness + the first two D25A files** · ~4 h
  - `src/test/resources/external/cii-d25a/d25a-minimal-invoice.xml` and
    `d25a-minimal-creditnote.xml` (BT-24 = `urn:cen.eu:en16931:2026`).
  - `AbstractCIID25AToUBL25ConverterTest` doing: input XSD-validate against the D25A XSD →
    convert → output XSD-validate against the UBL 2.5 XSD → run XPath assertions.
  - A small `assertXPath (aUBLDoc, sXPath, sExpected)` helper with the UBL 2.5 namespace context.
  - **Done when:** both files convert and assert green.
  - **Why here:** everything from A5 onwards is verified through this harness.

### Phase 3 — Port the 214 carried-over rows

**Approach changed during A5.** Hand-porting 214 rows section by section was neither the cheapest
nor the safest route. Instead `CIID16BToUBL21Converter` was bulk transformed (CII package swap +
UBL 2.1 → 2.5 package swap) and the **compiler enumerated every API delta** — 17 sites, all of them
cardinality widenings, listed in 4.4b. This preserves every bug fix accumulated in the 2017
converter (BT-150, BT-61, UBL-CR-275, BT-11, …) instead of risking their reintroduction.

A6 now applies the 2026 path deltas, A7 verifies the carried-over rows with a comprehensive test
file. Source of truth is the matching section of `docs/en16931-2026-syntax.md`.
These 2017 paths **changed** in 2026. Established by diffing all 180 rows present in both mapping
documents: 58 rows differed textually, but 52 of those are only base-path notation differences
between the two documents (e.g. the 2026 BG-4 base excludes `cac:Party`, and `[@format='102']` is
now spelled out). **Six are real**, and all six are implemented in A6:

| BT | 2017 | 2026 |
|----|------|------|
| BG-1 (BT-21, BT-22) | `cbc:Note`, with BT-21 embedded as a `#code#` prefix | `cac:Annotation/cbc:SubjectCode` + `cac:Annotation/cbc:AnnotationContent` — a real element since UBL 2.5, so the prefix hack is gone |
| BT-10 / BT-10-1 | `cbc:BuyerReference` (0..1), CII `ram:BuyerReference` | `cac:BuyerAssignedReference/cbc:BuyerReference` (0..n) + `cbc:BuyerReferenceCode`, CII `ram:BuyerReferenceID` |
| BT-9 (CreditNote) | `cac:PaymentMeans/cbc:PaymentDueDate` | native `/CreditNote/cbc:DueDate` |
| BT-11 (CreditNote) | `cac:AdditionalDocumentReference/cbc:ID` | native `/CreditNote/cac:ProjectReference/cbc:ID` |
| BT-32-2 | `cac:TaxScheme/cbc:ID` = anything except `VAT` | fixed value `LOC` (CII `@schemeID='FC'`) |
| BT-127 | `cbc:Note` | unchanged — but it must **not** follow BG-1 into `cac:Annotation`; it has no subject-code counterpart |

- [x] **A7 — Comprehensive carried-over test file + XPath assertions** · done · 214 rows
  - BG-25 (10): BT-126 127 128 128-1 128-2 129 130 131 132 133
  - BG-26 (4): BT-134 134-1 135 135-1 · BG-27 (5): BT-136 … 140 · BG-28 (5): BT-141 … 145
  - BG-29 (7): BT-146 147 147-1 148 148-1 149 150 · BG-30 (3): BT-151 151-1 152
  - BG-31 (10): BT-153 … 159 incl. BT-157-1 158-1 158-2 · BG-32 (2): BT-160 161
  - Includes the credit-note renames: `cac:CreditNoteLine`, `cbc:CreditedQuantity`.

### Phase 4 — The 70 new rows

- [x] **A8 — New header BTs** · done · 9 rows
  - BT-166 (`cbc:IssueTime`), BT-166-1, BT-167 / 167-1 / 167-2 (`cac:TaxExchangeRate`),
    BT-197 (`cac:DeliveryNoteDocumentReference`), BT-202 (BG-3 preceding invoice type code),
    BT-215 / BT-216 (BG-19 debited account PSP identifier and name).

- [x] **A9 — BG-33 / BG-35 / BG-36 payment terms, discount, penalty** · done · 8 rows
  - BG-35: BT-170 (`cac:SettlementPeriod/cbc:EndDate`), BT-170-1, BT-171, BT-172.
  - BG-36: BT-181 (`cac:PenaltyPeriod/cbc:StartDate`), BT-181-1,
    BT-182 (`cac:PenaltyInterestRate/cbc:InterestRatePercent`), BT-183 (`cbc:PenaltyAmount`).
  - **Structural work:** all three groups share `/Invoice/cac:PaymentTerms`, which becomes 0..n.
    CII disambiguates cleanly (`ram:ApplicableTradePaymentDiscountTerms` vs
    `ram:ApplicableTradePaymentPenaltyTerms`); UBL does not, so emit **one `cac:PaymentTerms`
    per CII container** rather than merging.

- [x] **A10 — BG-34 charges on behalf of a third party** · done · 3 rows
  - BT-179 (`cbc:TaxInclusiveLineExtensionAmount`), BT-179-1 (`cbc:ID`, UBL-only — CII has no
    counterpart, so synthesise a 1-based sequence), BT-180 (`cac:Item/cbc:Description`).
  - CII source `ram:SpecifiedFinancialAdjustment` → UBL `/Invoice/cac:CollectionInvoiceLine`
    (`/CreditNote/cac:CollectionCreditNoteLine`).

- [x] **A11 — New allowance / charge / VAT-breakdown BTs** · done · 10 rows
  - BG-20: BT-173, BT-174, BT-213 · BG-21: BT-175, BT-176, BT-177, BT-177-1, BT-214
  - BG-23: BT-184 (`cbc:TaxAmount/@currencyID`), BT-210
  - **Discriminator:** BT-105 and BT-177 share `cbc:AllowanceChargeReasonCode`. BT-177 is the one
    carrying `@listID='5153'` (BT-177-1). CII writes BT-177 as `ram:ReasonCode[.!='VAT']`.

- [x] **A12 — New line-level document references + BG-39** · done · 14 rows
  - BG-25: BT-188, BT-200, BT-201, BT-189, BT-190, BT-191, BT-192, BT-198, BT-199
  - BG-39: BT-217, BT-218, BT-218-1, BT-219, BT-220 (`cac:InvoiceLine/cac:BillingReference`)

- [x] **A13 — BG-37 / BG-38 line delivery + new item and tax BTs** · done · 19 rows
  - BG-37 (5): BT-185, BT-186, BT-186-1, BT-187, BT-187-1
  - BG-38 (7): BT-203 … BT-209
  - BG-28 (2): BT-193, BT-193-1 · BG-30 (2): BT-194, BT-195 · BG-31 (1): BT-196
  - BG-32 (2): BT-211 (`cbc:NameCode`), BT-212 (`cbc:ValueQuantity/@unitCode`)
  - **Discriminator:** BT-161a (`cbc:Value`) vs BT-161b (`cbc:ValueQuantity`) — exactly one.

### Phase 5 — Detection, CLI, comprehensive tests, docs

- [x] **A14 — `EEN16931Edition` + `CIIToUBLDispatcher`** · done
  - `EEN16931Edition.detect (File | Node | Document)` reading BT-24 via a DOM/StAX peek — **must
    not** JAXB-parse the whole document, since the correct model is not yet known. Returns `null`
    when BT-24 is absent or matches neither edition prefix.
  - `CIIToUBLDispatcher` with the shared settings API, routing to the edition converter; explicit
    edition override; a clear `ErrorList` entry when detection fails.
  - **Done when:** unit tests cover both prefixes, the XRechnung `#compliant#` suffix form, a
    missing BT-24, and an unknown identifier.

- [x] **A15 — CLI rework** · done
  - Default: auto-detect. `--en-version 2017|2026` forces and skips detection.
  - Retire `--ubl` (or keep it as a deprecated alias accepting only `2.1`/`2.5`, cross-checked
    against `--en-version` — decide when implementing).
  - Wire `UBL25Marshaller.invoice()` / `.creditNote()` for the 2026 output path.
  - Error text when detection fails, per D4:
    `cannot determine EN 16931 edition (BT-24 missing); pass --en-version 2017|2026`.

- [x] **A16 — Comprehensive artificial D25A test files** · done
  - `d25a-full-invoice.xml` and `d25a-full-creditnote.xml` exercising **every** BT of the mapping.
  - One file per new group: BG-33/35/36, BG-34, BG-37+38, BG-39.
  - Edge cases: BT-2 with `@format='208'` (date **and** time); BT-6 + BT-111 dual currency;
    BT-161a vs BT-161b; BT-105 vs BT-177 via `@listID`; BT-84 / BT-91 `ram:IBANID` vs
    `ram:ProprietaryID`; BT-90 with and without a Payee.
  - **Done when:** every one of the 284 mapping rows is asserted by at least one test.

- [x] **A17 — Documentation** · done, except the `docs/00readme.txt` citation
  - `README.md`: drop UBL 2.2/2.3/2.4, document the two bindings, the new package names, the CLI.
  - `CLAUDE.md`: update the converter hierarchy section; note that
    `docs/mapping-cii-ubl.xlsx` is **2017-only** and `docs/en16931-2026-syntax.md` is the 2026 source.
  - `docs/00readme.txt`: **still open.** It describes the 2026 sources as "Draft versions for Formal
    Vote, CEN/TC 434 documents N0463 and N0465", which the maintainer has confirmed is wrong — the
    data is from the published version. The correct citation is unknown (do the published documents
    still carry the N-numbers, or should they be cited as CEN/TS 16931-3-2:2026 and
    CEN/TS 16931-3-3:2026 directly?), so the wording was left untouched rather than invented.
  - `README.md` News and noteworthy: `v4.0.0 - work in progress`.

## 7. Estimate

| Phase | Items | Sessions |
|-------|-------|----------|
| 1 Prepare and shrink | A0–A2 | ~1 |
| 2 Skeleton and harness | A3–A4 | ~1 |
| 3 Port carried-over rows | A5–A7 | 3 |
| 4 New rows | A8–A13 | ~5 |
| 5 Detection, CLI, tests, docs | A14–A17 | ~3–4 |
| **Total** | **18 items** | **~13–14 sessions ≈ 6–8 working days** |

**This cannot be done in one go.** Two reasons:

1. **Volume** — roughly 4 000 new or changed Java lines plus ~14 hand-authored CII D25A instances
   (a "full" file runs past 400 lines of XML).
2. **No Schematron for 2026** — every one of the 70 new mappings is correct only because it was
   read off the mapping table. XSD validity does not catch a BT written into the wrong element, so
   each chunk needs its own review checkpoint rather than one unverified sweep.

## 8. Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| The 2026 binding has weaker test coverage than 2017 | A wrong mapping can ship undetected | XPath assertion per BT (D3); revisit once `phive-rules-en16931` publishes 2026 Schematron, then add a `testConvertAndValidateAll` equivalent |
| BT-24 detection fails on non-conformant CII | Dispatcher cannot route | `--en-version` override (D4) and an explicit `ErrorList` entry, never a silent guess |
| CEF code lists `VATEX` / `SUPPLY` may not be published yet | BT-174 176 195 210 213 214 cannot be validated | Pass values through unvalidated; document it |
| A2 silently changes 2017 output | Regression for every existing user | `git status` on `generated/toubl21/` must stay empty |
| `docs/mapping-cii-ubl.xlsx` covers 2017 only | Wrong source consulted for 2026 work | Labelled in A17 |

## 9. Progress log

| Item | Session date | Commit | Notes |
|------|--------------|--------|-------|
| A0 | 2026-09-04 | (folded into A1) | Baseline confirmed: full test run leaves `generated/toubl21/` byte-identical. No new code needed. |
| A1 | 2026-09-04 | `[4.0.0 A1]` fb6bcba | 315 files changed, -8209 lines. `generated/toubl21/` unchanged. |
| A2 | 2026-09-04 | `[4.0.0 A2]` | Split verified: 44 members in, 44 out, none lost or duplicated. 102 conversions, `generated/toubl21/` unchanged. |
| A3 | 2026-09-04 | `[4.0.0 A3]` | Base port is a pure import swap - every D25A accessor matches D16B by name. Skeleton also emits the UBL-mandatory containers, so both files convert to XSD-valid UBL 2.5. |
| A4 | 2026-09-04 | `[4.0.0 A4]` | `MockD25ASettings` with `assertXPath` / `assertNoXPath` / `assertXPathCount`. XPaths are relative to the document element, matching the mapping table. Negative-probed: wrong value and wrong count both fail. |
| A5 | 2026-09-04 | `[4.0.0 A5]` d9c9af4 | Bulk port instead of hand-porting. 17 compile errors, all cardinality widenings (4.4b). Also corrected the A3 skeleton's wrong BT-27 mapping. |
| A6 | 2026-09-04 | `[4.0.0 A6]` | All 180 shared rows diffed programmatically between the two mapping docs; 58 differed, of which 6 were real changes (the rest were base-path notation). Verified with two new header test files. |
| A7 | 2026-09-05 | `[4.0.0 A7]` | `d25a-full-invoice.xml` + `d25a-full-creditnote.xml` cover BG-3 … BG-32 with ~150 XPath assertions. 6 test files, 10 tests, all green. |
| A8 | 2026-09-05 | `[4.0.0 A8]` | Also added `parseDateTime` to the shared base for UNTDID 2379 format "208", which is what makes BT-166 possible at all. |
| A9 | 2026-09-05 | `[4.0.0 A9]` | One `cac:PaymentTerms` per CII container, so the three groups stay distinguishable; asserted by two `assertNoXPath` checks that they are never merged. |
| A10 | 2026-09-05 | `[4.0.0 A10]` | BT-179-1 has no CII counterpart, so a 1-based sequence number is synthesised. Note BT-215/216 were already done in A8. |
| A11 | 2026-09-05 | `[4.0.0 A11]` | **Trap found:** the CII schema declares `default="4465_AllowanceChargeReasonCode"` on `@listID`, so JAXB always reports one. Only the fixed `5153` of BT-177-1 may be propagated — see 4.4c. |
| A12 | 2026-09-05 | `[4.0.0 A12]` | **Source defect found on BT-218** — see 4.4d. |
| A13 | 2026-09-05 | `[4.0.0 A13]` | BT-193/BT-193-1 came for free from the A11 `@listID` handling, since header and line allowances share `_copyAllowanceCharge`. **All 284 rows now implemented.** |
| A14 | 2026-09-05 | `[4.0.0 A14]` | Detection verified against all 103 existing files: 101 detect as 2017, 2 legacy ZUGFeRD files are undeterminable — real-world evidence for the override (see 4.4e). |
| A15 | 2026-09-05 | `[4.0.0 A15]` | `--ubl` kept as a deprecated alias (2.1/2.5), cross-checked against `--en-version`. **Fixed a pre-existing CLI NPE** — see 4.4f. |
| A16 | 2026-09-05 | `[4.0.0 A16]` | Coverage guard added; it immediately found **BT-122-1/BT-122-1-1 unimplemented** and BT-91's missing ProprietaryID fallback — see 4.4g. |
| A17 | 2026-09-05 | `[4.0.0 A17]` | README, CLAUDE.md and the News entry updated. `docs/00readme.txt` left untouched pending the correct citation. |
