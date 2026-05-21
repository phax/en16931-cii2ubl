# EN 16931-1:2017 — Business Terms, Business Term Groups and Business Rules

Extracted from `OENORM EN 16931-1:2017-11-01` (English).
Source PDF: `OENORM_EN_16931_1_2017_11_01_en-unlocked.pdf`.

- Clause 6.3 (Table 2) — Semantic data model: lists every Business Term (BT-*) and Business Term Group (BG-*).
- Clause 6.4 (Tables 3–14) — Business rules: lists every BR-* rule (integrity constraints, conditions and VAT rules).

## Conventions

- **Level** `+` = top level (under INVOICE root); `++`, `+++`, `++++` = nested deeper.
- **Cardinality** uses the source notation (`1..1`, `0..1`, `0..n`, `1..n`).
- **Sub-component rows** (`Scheme identifier`, `Mime code`, `Filename`) are written immediately below the parent BT. The 2017 standard does not assign explicit IDs to these — community tooling (and the file `en16931-3way-mapping.md` in this folder) refers to them as `BT-X-1`, `BT-X-2`, etc. That synthetic ID is shown in parentheses for cross-reference.

## Counts

| Item | Count |
|------|-------|
| Business Terms (BT) with explicit ID | 164 (BT-1..BT-165, gap at BT-4) |
| Business Term sub-components (Scheme identifier etc.) | 16 |
| Business Term Groups (BG) | 32 (BG-1..BG-32) |
| Business Rules — Integrity (BR-*) | 58 |
| Business Rules — Conditions (BR-CO-*) | 24 |
| Business Rules — VAT Standard (BR-S-*) | 10 |
| Business Rules — VAT Zero (BR-Z-*) | 10 |
| Business Rules — VAT Exempt (BR-E-*) | 10 |
| Business Rules — VAT Reverse Charge (BR-AE-*) | 10 |
| Business Rules — VAT Intra-Community (BR-IC-*) | 12 |
| Business Rules — VAT Exports (BR-G-*) | 10 |
| Business Rules — Not Subject to VAT (BR-O-*) | 14 |
| Business Rules — Canary Islands IGIC (BR-IG-*) | 10 |
| Business Rules — Ceuta and Melilla IPSI (BR-IP-*) | 10 |
| **Total BR rules** | **178** |

## 1. Business Terms and Groups (clause 6.3)

| ID | Level | Card. | Business Term | Parent | Semantic Data Type |
|----|-------|-------|---------------|--------|--------------------|
| BT-1 | `+` | 1..1 | Invoice number | INVOICE | Identifier |
| BT-2 | `+` | 1..1 | Invoice issue date | INVOICE | Date |
| BT-3 | `+` | 1..1 | Invoice type code | INVOICE | Code |
| BT-5 | `+` | 1..1 | Invoice currency code | INVOICE | Code |
| BT-6 | `+` | 0..1 | VAT accounting currency code | INVOICE | Code |
| BT-7 | `+` | 0..1 | Value added tax point date | INVOICE | Date |
| BT-8 | `+` | 0..1 | Value added tax point date code | INVOICE | Code |
| BT-9 | `+` | 0..1 | Payment due date | INVOICE | Date |
| BT-10 | `+` | 0..1 | Buyer reference | INVOICE | Text |
| BT-11 | `+` | 0..1 | Project reference | INVOICE | Document reference |
| BT-12 | `+` | 0..1 | Contract reference | INVOICE | Document reference |
| BT-13 | `+` | 0..1 | Purchase order reference | INVOICE | Document reference |
| BT-14 | `+` | 0..1 | Sales order reference | INVOICE | Document reference |
| BT-15 | `+` | 0..1 | Receiving advice reference | INVOICE | Document reference |
| BT-16 | `+` | 0..1 | Despatch advice reference | INVOICE | Document reference |
| BT-17 | `+` | 0..1 | Tender or lot reference | INVOICE | Document reference |
| BT-18 | `+` | 0..1 | Invoiced object identifier | INVOICE | Identifier |
| (BT-18-1) | `+` | 0..1 | Scheme identifier | BT-18 | |
| BT-19 | `+` | 0..1 | Buyer accounting reference | INVOICE | Text |
| BT-20 | `+` | 0..1 | Payment terms | INVOICE | Text |
| BG-1 | `+` | 0..n | INVOICE NOTE | INVOICE |  |
| BT-21 | `++` | 0..1 | Invoice note subject code | BG-1 | Text |
| BT-22 | `++` | 1..1 | Invoice note | BG-1 | Text |
| BG-2 | `+` | 1..1 | PROCESS CONTROL | INVOICE |  |
| BT-23 | `++` | 0..1 | Business process type | BG-2 | Text |
| BT-24 | `++` | 1..1 | Specification identifier | BG-2 | Identifier |
| BG-3 | `+` | 0..n | PRECEDING INVOICE REFERENCE | INVOICE |  |
| BT-25 | `++` | 1..1 | Preceding Invoice reference | BG-3 | Document reference |
| BT-26 | `++` | 0..1 | Preceding Invoice issue date | BG-3 | Date |
| BG-4 | `+` | 1..1 | SELLER | INVOICE |  |
| BT-27 | `++` | 1..1 | Seller name | BG-4 | Text |
| BT-28 | `++` | 0..1 | Seller trading name | BG-4 | Text |
| BT-29 | `++` | 0..n | Seller identifier | BG-4 | Identifier |
| (BT-29-1) | `++` | 0..1 | Scheme identifier | BT-29 | |
| BT-30 | `++` | 0..1 | Seller legal registration identifier | BG-4 | Identifier |
| (BT-30-1) | `++` | 0..1 | Scheme identifier | BT-30 | |
| BT-31 | `++` | 0..1 | Seller VAT identifier | BG-4 | Identifier |
| BT-32 | `++` | 0..1 | Seller tax registration identifier | BG-4 | Identifier |
| BT-33 | `++` | 0..1 | Seller additional legal information | BG-4 | Text |
| BT-34 | `++` | 0..1 | Seller electronic address | BG-4 | Identifier |
| (BT-34-1) | `++` | 1..1 | Scheme identifier | BT-34 | |
| BG-5 | `++` | 1..1 | SELLER POSTAL ADDRESS | BG-4 |  |
| BT-35 | `+++` | 0..1 | Seller address line 1 | BG-5 | Text |
| BT-36 | `+++` | 0..1 | Seller address line 2 | BG-5 | Text |
| BT-162 | `+++` | 0..1 | Seller address line 3 | BG-5 | Text |
| BT-37 | `+++` | 0..1 | Seller city | BG-5 | Text |
| BT-38 | `+++` | 0..1 | Seller post code | BG-5 | Text |
| BT-39 | `+++` | 0..1 | Seller country subdivision | BG-5 | Text |
| BT-40 | `+++` | 1..1 | Seller country code | BG-5 | Code |
| BG-6 | `++` | 0..1 | SELLER CONTACT | BG-4 |  |
| BT-41 | `+++` | 0..1 | Seller contact point | BG-6 | Text |
| BT-42 | `+++` | 0..1 | Seller contact telephone number | BG-6 | Text |
| BT-43 | `+++` | 0..1 | Seller contact email address | BG-6 | Text |
| BG-7 | `+` | 1..1 | BUYER | INVOICE |  |
| BT-44 | `++` | 1..1 | Buyer name | BG-7 | Text |
| BT-45 | `++` | 0..1 | Buyer trading name | BG-7 | Text |
| BT-46 | `++` | 0..1 | Buyer identifier | BG-7 | Identifier |
| (BT-46-1) | `++` | 0..1 | Scheme identifier | BT-46 | |
| BT-47 | `++` | 0..1 | Buyer legal registration identifier | BG-7 | Identifier |
| (BT-47-1) | `++` | 0..1 | Scheme identifier | BT-47 | |
| BT-48 | `++` | 0..1 | Buyer VAT identifier | BG-7 | Identifier |
| BT-49 | `++` | 0..1 | Buyer electronic address | BG-7 | Identifier |
| (BT-49-1) | `++` | 1..1 | Scheme identifier | BT-49 | |
| BG-8 | `++` | 1..1 | BUYER POSTAL ADDRESS | BG-7 |  |
| BT-50 | `+++` | 0..1 | Buyer address line 1 | BG-8 | Text |
| BT-51 | `+++` | 0..1 | Buyer address line 2 | BG-8 | Text |
| BT-163 | `+++` | 0..1 | Buyer address line 3 | BG-8 | Text |
| BT-52 | `+++` | 0..1 | Buyer city | BG-8 | Text |
| BT-53 | `+++` | 0..1 | Buyer post code | BG-8 | Text |
| BT-54 | `+++` | 0..1 | Buyer country subdivision | BG-8 | Text |
| BT-55 | `+++` | 1..1 | Buyer country code | BG-8 | Code |
| BG-9 | `++` | 0..1 | BUYER CONTACT | BG-7 |  |
| BT-56 | `+++` | 0..1 | Buyer contact point | BG-9 | Text |
| BT-57 | `+++` | 0..1 | Buyer contact telephone number | BG-9 | Text |
| BT-58 | `+++` | 0..1 | Buyer contact email address | BG-9 | Text |
| BG-10 | `+` | 0..1 | PAYEE | INVOICE |  |
| BT-59 | `++` | 1..1 | Payee name | BG-10 | Text |
| BT-60 | `++` | 0..1 | Payee identifier | BG-10 | Identifier |
| (BT-60-1) | `++` | 0..1 | Scheme identifier | BT-60 | |
| BT-61 | `++` | 0..1 | Payee legal registration identifier | BG-10 | Identifier |
| (BT-61-1) | `++` | 0..1 | Scheme identifier | BT-61 | |
| BG-11 | `+` | 0..1 | SELLER TAX REPRESENTATIVE PARTY | INVOICE |  |
| BT-62 | `++` | 1..1 | Seller tax representative name | BG-11 | Text |
| BT-63 | `++` | 1..1 | Seller tax representative VAT identifier | BG-11 | Identifier |
| BG-12 | `++` | 1..1 | SELLER TAX REPRESENTATIVE POSTAL ADDRESS | BG-11 |  |
| BT-64 | `+++` | 0..1 | Tax representative address line 1 | BG-12 | Text |
| BT-65 | `+++` | 0..1 | Tax representative address line 2 | BG-12 | Text |
| BT-164 | `+++` | 0..1 | Tax representative address line 3 | BG-12 | Text |
| BT-66 | `+++` | 0..1 | Tax representative city | BG-12 | Text |
| BT-67 | `+++` | 0..1 | Tax representative post code | BG-12 | Text |
| BT-68 | `+++` | 0..1 | Tax representative country subdivision | BG-12 | Text |
| BT-69 | `+++` | 1..1 | Tax representative country code | BG-12 | Code |
| BG-13 | `+` | 0..1 | DELIVERY INFORMATION | INVOICE |  |
| BT-70 | `++` | 0..1 | Deliver to party name | BG-13 | Text |
| BT-71 | `++` | 0..1 | Deliver to location identifier | BG-13 | Identifier |
| (BT-71-1) | `++` | 0..1 | Scheme identifier | BT-71 | |
| BT-72 | `++` | 0..1 | Actual delivery date | BG-13 | Date |
| BG-14 | `++` | 0..1 | INVOICING PERIOD | BG-13 |  |
| BT-73 | `+++` | 0..1 | Invoicing period start date | BG-14 | Date |
| BT-74 | `+++` | 0..1 | Invoicing period end date | BG-14 | Date |
| BG-15 | `++` | 0..1 | DELIVER TO ADDRESS | BG-13 |  |
| BT-75 | `+++` | 0..1 | Deliver to address line 1 | BG-15 | Text |
| BT-76 | `+++` | 0..1 | Deliver to address line 2 | BG-15 | Text |
| BT-165 | `+++` | 0..1 | Deliver to address line 3 | BG-15 | Text |
| BT-77 | `+++` | 0..1 | Deliver to city | BG-15 | Text |
| BT-78 | `+++` | 0..1 | Deliver to post code | BG-15 | Text |
| BT-79 | `+++` | 0..1 | Deliver to country subdivision | BG-15 | Text |
| BT-80 | `+++` | 1..1 | Deliver to country code | BG-15 | Code |
| BG-16 | `+` | 0..1 | PAYMENT INSTRUCTIONS | INVOICE |  |
| BT-81 | `++` | 1..1 | Payment means type code | BG-16 | Code |
| BT-82 | `++` | 0..1 | Payment means text | BG-16 | Text |
| BT-83 | `++` | 0..1 | Remittance information | BG-16 | Text |
| BG-17 | `++` | 0..n | CREDIT TRANSFER | BG-16 |  |
| BT-84 | `+++` | 1..1 | Payment account identifier | BG-17 | Identifier |
| BT-85 | `+++` | 0..1 | Payment account name | BG-17 | Text |
| BT-86 | `+++` | 0..1 | Payment service provider identifier | BG-17 | Identifier |
| BG-18 | `++` | 0..1 | PAYMENT CARD INFORMATION | BG-16 |  |
| BT-87 | `+++` | 1..1 | Payment card primary account number | BG-18 | Text |
| BT-88 | `+++` | 0..1 | Payment card holder name | BG-18 | Text |
| BG-19 | `++` | 0..1 | DIRECT DEBIT | BG-16 |  |
| BT-89 | `+++` | 0..1 | Mandate reference identifier | BG-19 | Identifier |
| BT-90 | `+++` | 0..1 | Bank assigned creditor identifier | BG-19 | Identifier |
| BT-91 | `+++` | 0..1 | Debited account identifier | BG-19 | Identifier |
| BG-20 | `+` | 0..n | DOCUMENT LEVEL ALLOWANCES | INVOICE |  |
| BT-92 | `++` | 1..1 | Document level allowance amount | BG-20 | Amount |
| BT-93 | `++` | 0..1 | Document level allowance base amount | BG-20 | Amount |
| BT-94 | `++` | 0..1 | Document level allowance percentage | BG-20 | Percentage |
| BT-95 | `++` | 1..1 | Document level allowance VAT category code | BG-20 | Code |
| BT-96 | `++` | 0..1 | Document level allowance VAT rate | BG-20 | Percentage |
| BT-97 | `++` | 0..1 | Document level allowance reason | BG-20 | Text |
| BT-98 | `++` | 0..1 | Document level allowance reason code | BG-20 | Code |
| BG-21 | `+` | 0..n | DOCUMENT LEVEL CHARGES | INVOICE |  |
| BT-99 | `++` | 1..1 | Document level charge amount | BG-21 | Amount |
| BT-100 | `++` | 0..1 | Document level charge base amount | BG-21 | Amount |
| BT-101 | `++` | 0..1 | Document level charge percentage | BG-21 | Percentage |
| BT-102 | `++` | 1..1 | Document level charge VAT category code | BG-21 | Code |
| BT-103 | `++` | 0..1 | Document level charge VAT rate | BG-21 | Percentage |
| BT-104 | `++` | 0..1 | Document level charge reason | BG-21 | Text |
| BT-105 | `++` | 0..1 | Document level charge reason code | BG-21 | Code |
| BG-22 | `+` | 1..1 | DOCUMENT TOTALS | INVOICE |  |
| BT-106 | `++` | 1..1 | Sum of Invoice line net amount | BG-22 | Amount |
| BT-107 | `++` | 0..1 | Sum of allowances on document level | BG-22 | Amount |
| BT-108 | `++` | 0..1 | Sum of charges on document level | BG-22 | Amount |
| BT-109 | `++` | 1..1 | Invoice total amount without VAT | BG-22 | Amount |
| BT-110 | `++` | 0..1 | Invoice total VAT amount | BG-22 | Amount |
| BT-111 | `++` | 0..1 | Invoice total VAT amount in accounting currency | BG-22 | Amount |
| BT-112 | `++` | 1..1 | Invoice total amount with VAT | BG-22 | Amount |
| BT-113 | `++` | 0..1 | Paid amount | BG-22 | Amount |
| BT-114 | `++` | 0..1 | Rounding amount | BG-22 | Amount |
| BT-115 | `++` | 1..1 | Amount due for payment | BG-22 | Amount |
| BG-23 | `+` | 1..n | VAT BREAKDOWN | INVOICE |  |
| BT-116 | `++` | 1..1 | VAT category taxable amount | BG-23 | Amount |
| BT-117 | `++` | 1..1 | VAT category tax amount | BG-23 | Amount |
| BT-118 | `++` | 1..1 | VAT category code | BG-23 | Code |
| BT-119 | `++` | 0..1 | VAT category rate | BG-23 | Percentage |
| BT-120 | `++` | 0..1 | VAT exemption reason text | BG-23 | Text |
| BT-121 | `++` | 0..1 | VAT exemption reason code | BG-23 | Code |
| BG-24 | `+` | 0..n | ADDITIONAL SUPPORTING DOCUMENTS | INVOICE |  |
| BT-122 | `++` | 1..1 | Supporting document reference | BG-24 | Document reference |
| BT-123 | `++` | 0..1 | Supporting document description | BG-24 | Text |
| BT-124 | `++` | 0..1 | External document location | BG-24 | Text |
| BT-125 | `++` | 0..1 | Attached document | BG-24 | Binary object |
| (BT-125-1) | `++` | 1..1 | Attached document Mime code | BT-125 | |
| (BT-125-2) | `++` | 1..1 | Attached document Filename | BT-125 | |
| BG-25 | `+` | 1..n | INVOICE LINE | INVOICE |  |
| BT-126 | `++` | 1..1 | Invoice line identifier | BG-25 | Identifier |
| BT-127 | `++` | 0..1 | Invoice line note | BG-25 | Text |
| BT-128 | `++` | 0..1 | Invoice line object identifier | BG-25 | Identifier |
| (BT-128-1) | `++` | 0..1 | Scheme identifier | BT-128 | |
| BT-129 | `++` | 1..1 | Invoiced quantity | BG-25 | Quantity |
| BT-130 | `++` | 1..1 | Invoiced quantity unit of measure code | BG-25 | Code |
| BT-131 | `++` | 1..1 | Invoice line net amount | BG-25 | Amount |
| BT-132 | `++` | 0..1 | Referenced purchase order line reference | BG-25 | Document reference |
| BT-133 | `++` | 0..1 | Invoice line Buyer accounting reference | BG-25 | Text |
| BG-26 | `++` | 0..1 | INVOICE LINE PERIOD | BG-25 |  |
| BT-134 | `+++` | 0..1 | Invoice line period start date | BG-26 | Date |
| BT-135 | `+++` | 0..1 | Invoice line period end date | BG-26 | Date |
| BG-27 | `++` | 0..n | INVOICE LINE ALLOWANCES | BG-25 |  |
| BT-136 | `+++` | 1..1 | Invoice line allowance amount | BG-27 | Amount |
| BT-137 | `+++` | 0..1 | Invoice line allowance base amount | BG-27 | Amount |
| BT-138 | `+++` | 0..1 | Invoice line allowance percentage | BG-27 | Percentage |
| BT-139 | `+++` | 0..1 | Invoice line allowance reason | BG-27 | Text |
| BT-140 | `+++` | 0..1 | Invoice line allowance reason code | BG-27 | Code |
| BG-28 | `++` | 0..n | INVOICE LINE CHARGES | BG-25 |  |
| BT-141 | `+++` | 1..1 | Invoice line charge amount | BG-28 | Amount |
| BT-142 | `+++` | 0..1 | Invoice line charge base amount | BG-28 | Amount |
| BT-143 | `+++` | 0..1 | Invoice line charge percentage | BG-28 | Percentage |
| BT-144 | `+++` | 0..1 | Invoice line charge reason | BG-28 | Text |
| BT-145 | `+++` | 0..1 | Invoice line charge reason code | BG-28 | Code |
| BG-29 | `++` | 1..1 | PRICE DETAILS | BG-25 |  |
| BT-146 | `+++` | 1..1 | Item net price | BG-29 | Unit price amount |
| BT-147 | `+++` | 0..1 | Item price discount | BG-29 | Unit price amount |
| BT-148 | `+++` | 0..1 | Item gross price | BG-29 | Unit price amount |
| BT-149 | `+++` | 0..1 | Item price base quantity | BG-29 | Quantity |
| BT-150 | `+++` | 0..1 | Item price base quantity unit of measure code | BG-29 | Code |
| BG-30 | `++` | 1..1 | LINE VAT INFORMATION | BG-25 |  |
| BT-151 | `+++` | 1..1 | Invoiced item VAT category code | BG-30 | Code |
| BT-152 | `+++` | 0..1 | Invoiced item VAT rate | BG-30 | Percent |
| BG-31 | `++` | 1..1 | ITEM INFORMATION | BG-25 |  |
| BT-153 | `+++` | 1..1 | Item name | BG-31 | Text |
| BT-154 | `+++` | 0..1 | Item description | BG-31 | Text |
| BT-155 | `+++` | 0..1 | Item Seller's identifier | BG-31 | Identifier |
| BT-156 | `+++` | 0..1 | Item Buyer's identifier | BG-31 | Identifier |
| BT-157 | `+++` | 0..1 | Item standard identifier | BG-31 | Identifier |
| (BT-157-1) | `+++` | 1..1 | Scheme identifier | BT-157 | |
| BT-158 | `+++` | 0..n | Item classification identifier | BG-31 | Identifier |
| (BT-158-1) | `+++` | 1..1 | Scheme identifier | BT-158 | |
| (BT-158-2) | `+++` | 0..1 | Scheme version identifier | BT-158 | |
| BT-159 | `+++` | 0..1 | Item country of origin | BG-31 | Code |
| BG-32 | `+++` | 0..n | ITEM ATTRIBUTES | BG-31 |  |
| BT-160 | `++++` | 1..1 | Item attribute name | BG-32 | Text |
| BT-161 | `++++` | 1..1 | Item attribute value | BG-32 | Text |

## 2. Business Rules (clause 6.4)

### 2.1 Integrity constraints (Table 3)

| ID | Target | Business term(s) | Description |
|----|--------|-------------------|-------------|
| BR-1 | Process control | BT-24 | An Invoice shall have a Specification identifier (BT-24). |
| BR-2 | Invoice | BT-1 | An Invoice shall have an Invoice number (BT-1). |
| BR-3 | Invoice | BT-2 | An Invoice shall have an Invoice issue date (BT-2). |
| BR-4 | Invoice | BT-3 | An Invoice shall have an Invoice type code (BT-3). |
| BR-5 | Invoice | BT-5 | An Invoice shall have an Invoice currency code (BT-5). |
| BR-6 | Seller | BT-27 | An Invoice shall contain the Seller name (BT-27). |
| BR-7 | Buyer | BT-44 | An Invoice shall contain the Buyer name (BT-44). |
| BR-8 | Seller | BG-5 | An Invoice shall contain the Seller postal address (BG-5). |
| BR-9 | Seller Postal Address | BT-40 | The Seller postal address (BG-5) shall contain a Seller country code (BT-40). |
| BR-10 | Buyer | BG-8 | An Invoice shall contain the Buyer postal address (BG-8). |
| BR-11 | Buyer Postal Address | BT-55 | The Buyer postal address shall contain a Buyer country code (BT-55). |
| BR-12 | Document totals | BT-106 | An Invoice shall have the Sum of Invoice line net amount (BT-106). |
| BR-13 | Document totals | BT-109 | An Invoice shall have the Invoice total amount without VAT (BT-109). |
| BR-14 | Document totals | BT-112 | An Invoice shall have the Invoice total amount with VAT (BT-112). |
| BR-15 | Document totals | BT-115 | An Invoice shall have the Amount due for payment (BT-115). |
| BR-16 | Invoice | BG-25 | An Invoice shall have at least one Invoice line (BG-25). |
| BR-17 | Payee | BT-59 | The Payee name (BT-59) shall be provided in the Invoice, if the Payee (BG-10) is different from the Seller (BG-4). |
| BR-18 | Seller tax representative | BT-62 | The Seller tax representative name (BT-62) shall be provided in the Invoice, if the Seller (BG-4) has a Seller tax representative party (BG-11). |
| BR-19 | Seller tax representative | BG-12 | The Seller tax representative postal address (BG-12) shall be provided in the Invoice, if the Seller (BG-4) has a Seller tax representative party (BG-11). |
| BR-20 | Seller tax representative postal address | BT-69 | The Seller tax representative postal address (BG-12) shall contain a Tax representative country code (BT-69), if the Seller (BG-4) has a Seller tax representative party (BG-11). |
| BR-21 | Invoice Line | BT-126 | Each Invoice line (BG-25) shall have an Invoice line identifier (BT-126). |
| BR-22 | Invoice Line | BT-129 | Each Invoice line (BG-25) shall have an Invoiced quantity (BT-129). |
| BR-23 | Invoice Line | BT-130 | An Invoice line (BG-25) shall have an Invoiced quantity unit of measure code (BT-130). |
| BR-24 | Invoice Line | BT-131 | Each Invoice line (BG-25) shall have an Invoice line net amount (BT-131). |
| BR-25 | Item information | BT-153 | Each Invoice line (BG-25) shall contain the Item name (BT-153). |
| BR-26 | Price details | BT-146 | Each Invoice line (BG-25) shall contain the Item net price (BT-146). |
| BR-27 | Item net price | BT-146 | The Item net price (BT-146) shall NOT be negative. |
| BR-28 | Price details | BT-148 | The Item gross price (BT-148) shall NOT be negative. |
| BR-29 | Invoicing Period | BT-74 | If both Invoicing period start date (BT-73) and Invoicing period end date (BT-74) are given then the Invoicing period end date (BT-74) shall be later or equal to the Invoicing period start date (BT-73). |
| BR-30 | Invoice Line Period | BT-135 | If both Invoice line period start date (BT-134) and Invoice line period end date (BT-135) are given then the Invoice line period end date (BT-135) shall be later or equal to the Invoice line period start date (BT-134). |
| BR-31 | Document level allowances | BT-92 | Each Document level allowance (BG-20) shall have a Document level allowance amount (BT-92). |
| BR-32 | Document level allowances | BT-95 | Each Document level allowance (BG-20) shall have a Document level allowance VAT category code (BT-95). |
| BR-33 | Document level allowances | BT-97, BT-98 | Each Document level allowance (BG-20) shall have a Document level allowance reason (BT-97) or a Document level allowance reason code (BT-98). |
| BR-36 | Document level charges | BT-99 | Each Document level charge (BG-21) shall have a Document level charge amount (BT-99). |
| BR-37 | Document level charges | BT-102 | Each Document level charge (BG-21) shall have a Document level charge VAT category code (BT-102). |
| BR-38 | Document level charges | BT-104, BT-105 | Each Document level charge (BG-21) shall have a Document level charge reason (BT-104) or a Document level charge reason code (BT-105). |
| BR-41 | Invoice line allowances | BT-136 | Each Invoice line allowance (BG-27) shall have an Invoice line allowance amount (BT-136). |
| BR-42 | Invoice line allowances | BT-144, BT-145 | Each Invoice line allowance (BG-27) shall have an Invoice line allowance reason (BT-139) or an Invoice line allowance reason code (BT-140). |
| BR-43 | Invoice line charges | BT-141 | Each Invoice line charge (BG-28) shall have an Invoice line charge amount (BT-141). |
| BR-44 | Invoice line charges | BT-139, BT-140 | Each Invoice line charge (BG-28) shall have an Invoice line charge reason (BT-144) or an Invoice line charge reason code (BT-145). |
| BR-45 | VAT breakdown | BT-116 | Each VAT breakdown (BG-23) shall have a VAT category taxable amount (BT-116). |
| BR-46 | VAT breakdown | BT-117 | Each VAT breakdown (BG-23) shall have a VAT category tax amount (BT-117). |
| BR-47 | VAT breakdown | BT-118 | Each VAT breakdown (BG-23) shall be defined through a VAT category code (BT-118). |
| BR-48 | VAT breakdown | BT-119 | Each VAT breakdown (BG-23) shall have a VAT category rate (BT-119), except if the Invoice is not subject to VAT. |
| BR-49 | Payment instructions | BT-81 | A Payment instruction (BG-16) shall specify the Payment means type code (BT-81). |
| BR-50 | Account information | BT-84 | A Payment account identifier (BT-84) shall be present if Credit transfer (BG-16) information is provided in the Invoice. |
| BR-51 | Card information | BT-87 | The last 4 to 6 digits of the Payment card primary account number (BT-87) shall be present if Payment card information (BG-18) is provided in the Invoice. |
| BR-52 | Additional supporting documents | BT-122 | Each Additional supporting document (BG-24) shall contain a Supporting document reference (BT-122). |
| BR-53 | Document totals | BT-111 | If the VAT accounting currency code (BT-6) is present, then the Invoice total VAT amount in accounting currency (BT-111) shall be provided. |
| BR-54 | Item attributes | BT-160, BT-161 | Each Item attribute (BG-32) shall contain an Item attribute name (BT-160) and an Item attribute value (BT-161). |
| BR-55 | Preceding invoice reference | BT-25 | Each Preceding Invoice reference (BG-3) shall contain a Preceding Invoice reference (BT-25). |
| BR-56 | Seller tax representative | BT-63 | Each Seller tax representative party (BG-11) shall have a Seller tax representative VAT identifier (BT-63). |
| BR-57 | Deliver to address | BT-80 | Each Deliver to address (BG-15) shall contain a Deliver to country code (BT-80). |
| BR-61 | Payment instructions | BT-84 | If the Payment means type code (BT-81) means SEPA credit transfer, Local credit transfer or Non-SEPA international credit transfer, the Payment account identifier (BT-84) shall be present. |
| BR-62 | Seller electronic address | BT-34 | The Seller electronic address (BT-34) shall have a Scheme identifier. |
| BR-63 | Buyer electronic address | BT-49 | The Buyer electronic address (BT-49) shall have a Scheme identifier. |
| BR-64 | Item standard identifier | BT-157 | The Item standard identifier (BT-157) shall have a Scheme identifier |
| BR-65 | Item classification identifier | BT-158 | The Item classification identifier (BT-158) shall have a Scheme identifier |

### 2.2 Conditions (Table 4)

| ID | Target | Business term(s) | Description |
|----|--------|-------------------|-------------|
| BR-CO-3 | Invoice | BT-7, BT-8 | Value added tax point date (BT-7) and Value added tax point date code (BT-8) are mutually exclusive. |
| BR-CO-4 | Invoice Line | BT-151 | Each Invoice line (BG-25) shall be categorized with an Invoiced item VAT category code (BT-151). |
| BR-CO-5 | Document level Allowances | BT-97, BT-98 | Document level allowance reason code (BT-98) and Document level allowance reason (BT-97) shall indicate the same type of allowance. |
| BR-CO-6 | Document level Charges | BT-104, BT-105 | Document level charge reason code (BT-105) and Document level charge reason (BT-104) shall indicate the same type of charge. |
| BR-CO-7 | Invoice line Allowances | BT-139, BT-140 | Invoice line allowance reason code (BT-140) and Invoice line allowance reason (BT-139) shall indicate the same type of allowance reason. |
| BR-CO-8 | Invoice line Charges | BT-144, BT-145 | Invoice line charge reason code (BT-145) and Invoice line charge reason (BT144) shall indicate the same type of charge reason. |
| BR-CO-9 | VAT identifiers | BT-31, BT-48, BT-63 | The Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48) shall have a prefix in accordance with ISO code ISO 3166-1 alpha-2 by which the country of issue may be identified. Nevertheless, Greece may use the prefix 'EL'. |
| BR-CO-10 | Document totals | BT-106 | Sum of Invoice line net amount (BT-106) = ∑ Invoice line net amount (BT-131). |
| BR-CO-11 | Document totals | BT-107 | Sum of allowances on document level (BT-107) = ∑ Document level allowance amount (BT-92). |
| BR-CO-12 | Document totals | BT-108 | Sum of charges on document level (BT-108) = ∑ Document level charge amount (BT-99). |
| BR-CO-13 | Document totals | BT-109 | Invoice total amount without VAT (BT-109) = ∑ Invoice line net amount (BT-131) - Sum of allowances on document level (BT-107) + Sum of charges on document level (BT-108). |
| BR-CO-14 | Document totals | BT-110 | Invoice total VAT amount (BT-110) = ∑ VAT category tax amount (BT-117). |
| BR-CO-15 | Document totals | BT-112 | Invoice total amount with VAT (BT-112) = Invoice total amount without VAT (BT-109) + Invoice total VAT amount (BT-110). |
| BR-CO-16 | Document totals | BT-115 | Amount due for payment (BT-115) = Invoice total amount with VAT (BT-112) -Paid amount (BT-113) + Rounding amount (BT-114). |
| BR-CO-17 | VAT breakdown | BT-117 | VAT category tax amount (BT-117) = VAT category taxable amount (BT-116) x (VAT category rate (BT-119) / 100), rounded to two decimals. |
| BR-CO-18 | VAT breakdown | BG-23 | An Invoice shall at least have one VAT breakdown group (BG-23). |
| BR-CO-19 | Delivery or invoice period | BT-73, BT-74 | If Invoicing period (BG-14) is used, the Invoicing period start date (BT-73) or the Invoicing period end date (BT-74) shall be filled, or both. |
| BR-CO-20 | Invoice line period | BT-134, BT-135 | If Invoice line period (BG-26) is used, the Invoice line period start date (BT-134) or the Invoice line period end date (BT-135) shall be filled, or both. |
| BR-CO-21 | Document level allowance | BT-97, BT-98 | Each Document level allowance (BG-20) shall contain a Document level allowance reason (BT-97) or a Document level allowance reason code (BT-98), or both. |
| BR-CO-22 | Document level charge | BT-104, BT-105 | Each Document level charge (BG-21) shall contain a Document level charge reason (BT-104) or a Document level charge reason code (BT-105), or both. |
| BR-CO-23 | Invoice line allowance | BT-139, BT-140 | Each Invoice line allowance (BG-27) shall contain an Invoice line allowance reason (BT-139) or an Invoice line allowance reason code (BT-140), or both. |
| BR-CO-24 | Invoice line charge | BT-144, BT-145 | Each Invoice line charge (BG-28) shall contain an Invoice line charge reason (BT-144) or an Invoice line charge reason code (BT-145), or both. |
| BR-CO-25 | Invoice | BT-9, BT-20 | In case the Amount due for payment (BT-115) is positive, either the Payment due date (BT-9) or the Payment terms (BT-20) shall be present. |
| BR-CO-26 | Seller | BT-29, BT-30, BT-31 | In order for the buyer to automatically identify a supplier, the Seller identifier (BT-29), the Seller legal registration identifier (BT-30) and/or the Seller VAT identifier (BT-31) shall be present. |

### 2.3 VAT — Standard and reduced rate (Table 6)

| ID | Description |
|----|-------------|
| BR-S-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Standard rated" shall contain in the VAT breakdown (BG-23) at least one VAT category code (BT-118) equal with "Standard rated". |
| BR-S-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Standard rated" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-S-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Standard rated" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-S-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Standard rated" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-S-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Standard rated" the Invoiced item VAT rate (BT-152) shall be greater than zero. |
| BR-S-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Standard rated" the Document level allowance VAT rate (BT-96) shall be greater than zero. |
| BR-S-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Standard rated" the Document level charge VAT rate (BT-103) shall be greater than zero. |
| BR-S-8 | For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "Standard rated", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "Standard rated" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119). |
| BR-S-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where VAT category code (BT-118) is "Standard rated" shall equal the VAT category taxable amount (BT-116) multiplied by the VAT category rate (BT-119). |
| BR-S-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) "Standard rate" shall not have a VAT exemption reason code (BT-121) or VAT exemption reason text (BT-120). |

### 2.4 VAT — Zero rate (Table 7)

| ID | Description |
|----|-------------|
| BR-Z-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Zero rated" shall contain in the VAT breakdown (BG-23) exactly one VAT category code (BT-118) equal with "Zero rated". |
| BR-Z-2 | An Invoice that contains an Invoice line where the Invoiced item VAT category code (BT-151) is "Zero rated" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-Z-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Zero rated" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-Z-4 | An Invoice that contains a Document level charge where the Document level charge VAT category code (BT-102) is "Zero rated" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-Z-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Zero rated" the Invoiced item VAT rate (BT-152) shall be 0 (zero). |
| BR-Z-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Zero rated" the Document level allowance VAT rate (BT-96) shall be 0 (zero). |
| BR-Z-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Zero rated" the Document level charge VAT rate (BT-103) shall be 0 (zero). |
| BR-Z-8 | In a VAT breakdown (BG-23) where VAT category code (BT-118) is "Zero rated" the VAT category taxable amount (BT-116) shall equal the sum of Invoice line net amount (BT-131) minus the sum of Document level allowance amounts (BT-92) plus the sum of Document level charge amounts (BT-99) where the VAT category codes (BT-151, BT-95, BT-102) are "Zero rated". |
| BR-Z-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where VAT category code (BT-118) is "Zero rated" shall equal 0 (zero). |
| BR-Z-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) "Zero rated" shall not have a VAT exemption reason code (BT-121) or VAT exemption reason text (BT-120). |

### 2.5 VAT — Exempted from VAT (Table 8)

| ID | Description |
|----|-------------|
| BR-E-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Exempt from VAT" shall contain exactly one VAT breakdown (BG-23) with the VAT category code (BT-118) equal to "Exempt from VAT". |
| BR-E-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Exempt from VAT" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-E-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Exempt from VAT" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-E-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Exempt from VAT" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-E-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Exempt from VAT", the Invoiced item VAT rate (BT-152) shall be 0 (zero). |
| BR-E-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Exempt from VAT", the Document level allowance VAT rate (BT-96) shall be 0 (zero). |
| BR-E-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Exempt from VAT", the Document level charge VAT rate (BT-103) shall be 0 (zero). |
| BR-E-8 | In a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Exempt from VAT" the VAT category taxable amount (BT-116) shall equal the sum of Invoice line net amounts (BT-131) minus the sum of Document level allowance amounts (BT-92) plus the sum of Document level charge amounts (BT-99) where the VAT category codes (BT-151, BT-95, BT-102) are "Exempt from VAT". |
| BR-E-9 | The VAT category tax amount (BT-117) In a VAT breakdown (BG-23) where the VAT category code (BT-118) equals "Exempt from VAT" shall equal 0 (zero). |
| BR-E-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) "Exempt from VAT" shall have a VAT exemption reason code (BT-121) or a VAT exemption reason text (BT-120). |

### 2.6 VAT — Reverse charge (Table 9)

| ID | Description |
|----|-------------|
| BR-AE-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Reverse charge" shall contain in the VAT breakdown (BG-23) exactly one VAT category code (BT-118) equal with "VAT reverse charge". |
| BR-AE-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Reverse charge" shall contain the Seller VAT Identifier (BT-31), the Seller Tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48) and/or the Buyer legal registration identifier (BT-47). |
| BR-AE-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Reverse charge" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48) and/or the Buyer legal registration identifier (BT-47). |
| BR-AE-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Reverse charge" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48) and/or the Buyer legal registration identifier (BT-47). |
| BR-AE-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Reverse charge" the Invoiced item VAT rate (BT-152) shall be 0 (zero). |
| BR-AE-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Reverse charge" the Document level allowance VAT rate (BT-96) shall be 0 (zero). |
| BR-AE-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Reverse charge" the Document level charge VAT rate (BT-103) shall be 0 (zero). |
| BR-AE-8 | In a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Reverse charge" the VAT category taxable amount (BT-116) shall equal the sum of Invoice line net amounts (BT-131) minus the sum of Document level allowance amounts (BT-92) plus the sum of Document level charge amounts (BT-99) where the VAT category codes (BT-151, BT-95, BT-102) are "Reverse charge". |
| BR-AE-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Reverse charge" shall be 0 (zero). |
| BR-AE-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) "Reverse charge" shall have a VAT exemption reason code (BT-121), meaning "Reverse charge" or the VAT exemption reason text (BT-120) "Reverse charge" (or the equivalent standard text in another language). |

### 2.7 VAT — Intra-community supply (Table 10)

| ID | Description |
|----|-------------|
| BR-IC-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Intra-community supply" shall contain in the VAT breakdown (BG-23) exactly one VAT category code (BT-118) equal with "Intra-community supply". |
| BR-IC-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Intra-community supply" shall contain the Seller VAT Identifier (BT-31) or the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48). |
| BR-IC-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Intra-community supply" shall contain the Seller VAT Identifier (BT-31) or the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48). |
| BR-IC-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Intra-community supply" shall contain the Seller VAT Identifier (BT-31) or the Seller tax representative VAT identifier (BT-63) and the Buyer VAT identifier (BT-48). |
| BR-IC-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Intra-community supply" the Invoiced item VAT rate (BT-152) shall be 0 (zero). |
| BR-IC-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Intra-community supply" the Document level allowance VAT rate (BT-96) shall be 0 (zero). |
| BR-IC-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Intra-community supply" the Document level charge VAT rate (BT-103) shall be 0 (zero). |
| BR-IC-8 | In a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Intra-community supply" the VAT category taxable amount (BT-116) shall equal the sum of Invoice line net amounts (BT-131) minus the sum of Document level allowance amounts (BT-92) plus the sum of Document level charge amounts (BT-99) where the VAT category codes (BT-151, BT-95, BT-102) are "Intra-community supply". |
| BR-IC-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Intra-community supply" shall be 0 (zero). |
| BR-IC-10 | A VAT Breakdown (BG-23) with the VAT Category code (BT-118) "Intra-community supply" shall have a VAT exemption reason code (BT-121), meaning "Intra-community supply" or the VAT exemption reason text (BT-120) "Intra-community supply" (or the equivalent standard text in another language). |
| BR-IC-11 | In an Invoice with a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Intra-community supply" the Actual delivery date (BT-72) or the Invoicing period (BG-14) shall not be blank. |
| BR-IC-12 | In an Invoice with a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Intra-community supply" the Deliver to country code (BT-80) shall not be blank. |

### 2.8 VAT — Exports (Table 11)

| ID | Description |
|----|-------------|
| BR-G-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Export outside the EU" shall contain in the VAT breakdown (BG-23) exactly one VAT category code (BT-118) equal with "Export outside the EU". |
| BR-G-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Export outside the EU" shall contain the Seller VAT Identifier (BT-31) or the Seller tax representative VAT identifier (BT-63). |
| BR-G-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Export outside the EU" shall contain the Seller VAT Identifier (BT-31) or the Seller tax representative VAT identifier (BT-63). |
| BR-G-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Export outside the EU" shall contain the Seller VAT Identifier (BT-31) or the Seller tax representative VAT identifier (BT-63). |
| BR-G-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Export outside the EU" the Invoiced item VAT rate (BT-152) shall be 0 (zero). |
| BR-G-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Export outside the EU" the Document level allowance VAT rate (BT-96) shall be 0 (zero). |
| BR-G-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Export outside the EU" the Document level charge VAT rate (BT-103) shall be 0 (zero). |
| BR-G-8 | In a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Export outside the EU" the VAT category taxable amount (BT-116) shall equal the sum of Invoice line net amounts (BT-131) minus the sum of Document level allowance amounts (BT-92) plus the sum of Document level charge amounts (BT-99) where the VAT category codes (BT-151, BT-95, BT-102) are "Export outside the EU". |
| BR-G-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Export outside the EU" shall be 0 (zero). |
| BR-G-10 | A VAT Breakdown (BG-23) with the VAT Category code (BT-118) "Export outside the EU" shall have a VAT exemption reason code (BT-121), meaning "Export outside the EU" or the VAT exemption reason text (BT-120) "Export outside the EU" (or the equivalent standard text in another language). |

### 2.9 VAT — Not subject to VAT (Table 12)

| ID | Description |
|----|-------------|
| BR-O-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "Not subject to VAT" shall contain exactly one VAT breakdown group (BG-23) with the VAT category code (BT-118) equal to "Not subject to VAT". |
| BR-O-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-46). |
| BR-O-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48). |
| BR-O-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "Not subject to VAT" shall not contain the Seller VAT identifier (BT-31), the Seller tax representative VAT identifier (BT-63) or the Buyer VAT identifier (BT-48). |
| BR-O-5 | An Invoice line (BG-25) where the VAT category code (BT-151) is "Not subject to VAT" shall not contain an Invoiced item VAT rate (BT-152). |
| BR-O-6 | A Document level allowance (BG-20) where VAT category code (BT-95) is "Not subject to VAT" shall not contain a Document level allowance VAT rate (BT-96). |
| BR-O-7 | A Document level charge (BG-21) where the VAT category code (BT-102) is "Not subject to VAT" shall not contain a Document level charge VAT rate (BT-103). |
| BR-O-8 | In a VAT breakdown (BG-23) where the VAT category code (BT-118) is " Not subject to VAT" the VAT category taxable amount (BT-116) shall equal the sum of Invoice line net amounts (BT-131) minus the sum of Document level allowance amounts (BT-92) plus the sum of Document level charge amounts (BT-99) where the VAT category codes (BT-151, BT-95, BT-102) are "Not subject to VAT". |
| BR-O-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where the VAT category code (BT-118) is "Not subject to VAT" shall be 0 (zero). |
| BR-O-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) " Not subject to VAT" shall have a VAT exemption reason code (BT-121), meaning " Not subject to VAT" or a VAT exemption reason text (BT-120) " Not subject to VAT" (or the equivalent standard text in another language). |
| BR-O-11 | An Invoice that contains a VAT breakdown group (BG-23) with a VAT category code (BT-118) "Not subject to VAT" shall not contain other VAT breakdown groups (BG-23). |
| BR-O-12 | An Invoice that contains a VAT breakdown group (BG-23) with a VAT category code (BT-118) "Not subject to VAT" shall not contain an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is not "Not subject to VAT". |
| BR-O-13 | An Invoice that contains a VAT breakdown group (BG-23) with a VAT category code (BT-118) "Not subject to VAT" shall not contain Document level allowances (BG-20) where Document level allowance VAT category code (BT-95) is not "Not subject to VAT". |
| BR-O-14 | An Invoice that contains a VAT breakdown group (BG-23) with a VAT category code (BT-118) "Not subject to VAT" shall not contain Document level charges (BG-21) where Document level charge VAT category code (BT-102) is not "Not subject to VAT". |

### 2.10 Canary Islands tax IGIC (Table 13)

| ID | Description |
|----|-------------|
| BR-IG-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "IGIC" shall contain in the VAT breakdown (BG-23) at least one VAT category code (BT-118) equal with "IGIC". |
| BR-IG-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "IGIC" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-IG-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "IGIC" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-IG-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "IGIC" shall contain the Seller VAT Identifier (BT-31), the Seller Tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-IG-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "IGIC" the invoiced item VAT rate (BT-152) shall be 0 (zero) or greater than zero. |
| BR-IG-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "IGIC" the Document level allowance VAT rate (BT-96) shall be 0 (zero) or greater than zero. |
| BR-IG-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "IGIC" the Document level charge VAT rate (BT-103) shall be 0 (zero) or greater than zero. |
| BR-IG-8 | For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "IGIC", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "IGIC" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119). |
| BR-IG-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where VAT category code (BT-118) is "IGIC" shall equal the VAT category taxable amount (BT-116) multiplied by the VAT category rate (BT-119). |
| BR-IG-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) "IGIC" shall not have a VAT exemption reason code (BT-121) or VAT exemption reason text (BT-120). |

### 2.11 Ceuta and Melilla tax IPSI (Table 14)

| ID | Description |
|----|-------------|
| BR-IP-1 | An Invoice that contains an Invoice line (BG-25), a Document level allowance (BG-20) or a Document level charge (BG-21) where the VAT category code (BT-151, BT-95 or BT-102) is "IPSI" shall contain in the VAT breakdown (BG-23) at least one VAT category code (BT-118) equal with "IPSI". |
| BR-IP-2 | An Invoice that contains an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "IPSI" shall contain the Seller VAT Identifier (BT-31), the Seller tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-IP-3 | An Invoice that contains a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "IPSI" shall contain the Seller VAT Identifier (BT-31), the Seller Tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-IP-4 | An Invoice that contains a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "IPSI" shall contain the Seller VAT Identifier (BT-31), the Seller Tax registration identifier (BT-32) and/or the Seller tax representative VAT identifier (BT-63). |
| BR-IP-5 | In an Invoice line (BG-25) where the Invoiced item VAT category code (BT-151) is "IPSI" the Invoiced item VAT rate (BT-152) shall be 0 (zero) or greater than zero. |
| BR-IP-6 | In a Document level allowance (BG-20) where the Document level allowance VAT category code (BT-95) is "IPSI" the Document level allowance VAT rate (BT-96) shall be 0 (zero) or greater than zero. |
| BR-IP-7 | In a Document level charge (BG-21) where the Document level charge VAT category code (BT-102) is "IPSI" the Document level charge VAT rate (BT-103) shall be 0 (zero) or greater than zero. |
| BR-IP-8 | For each different value of VAT category rate (BT-119) where the VAT category code (BT-118) is "IPSI", the VAT category taxable amount (BT-116) in a VAT breakdown (BG-23) shall equal the sum of Invoice line net amounts (BT-131) plus the sum of document level charge amounts (BT-99) minus the sum of document level allowance amounts (BT-92) where the VAT category code (BT-151, BT-102, BT-95) is "IPSI" and the VAT rate (BT-152, BT-103, BT-96) equals the VAT category rate (BT-119). |
| BR-IP-9 | The VAT category tax amount (BT-117) in a VAT breakdown (BG-23) where VAT category code (BT-118) is "IPSI" shall equal the VAT category taxable amount (BT-116) multiplied by the VAT category rate (BT-119). |
| BR-IP-10 | A VAT Breakdown (BG-23) with VAT Category code (BT-118) "IPSI" shall not have a VAT exemption reason code (BT-121) or VAT exemption reason text (BT-120). |

## 3. Comparison with `en16931-3way-mapping.md`

Both documents cover exactly the same 164 BTs (BT-1..BT-165 with BT-4 absent) and 32 BGs (BG-1..BG-32), plus the 16 supplementary scheme/component rows. **No BT or BG is missing on either side.**

### 3.1 BTs/BGs unique to one source

- **Only in the EN 16931 PDF:** _none_
- **Only in `en16931-3way-mapping.md`:** _none_

### 3.2 Cardinality differences

These are real deltas: the cardinality in the 3-way mapping does not match the cardinality printed in the EN 16931-1:2017 PDF.

| ID | EN 16931 PDF | 3-way mapping | Note |
|----|-------------:|--------------:|------|
| BT-34 | 0..1 | 1..1 | PDF: Seller electronic address is conditional (0..1). 3-way mapping declares it 1..1 (likely aligned with Peppol BIS, not EN 16931 core). |
| BT-46 | 0..1 | 0..n | PDF: Buyer identifier is 0..1 in the semantic model. 3-way mapping uses 0..n (UBL allows repeating `cac:PartyIdentification`). |
| BT-49 | 0..1 | 1..1 | PDF: Buyer electronic address is 0..1. 3-way mapping declares 1..1 (likely Peppol BIS alignment). |
| BT-125-2 | 1..1 | 0..1 | PDF: Filename is 1..1 mandatory for the Binary Object data type. 3-way mapping has 0..1. |

### 3.3 Name differences — substantive

These rename the term beyond simple abbreviation:

| ID | EN 16931 PDF | 3-way mapping |
|----|--------------|---------------|
| BG-14 | INVOICING PERIOD | DELIVERY OR INVOICE PERIOD |
| BT-18-1 | Scheme identifier | Invoiced object identifier scheme identifier |
| BT-25 | Preceding Invoice reference | Preceding Invoice number |
| BT-29-1 | Scheme identifier | Seller identifier scheme ID |
| BT-30-1 | Scheme identifier | Seller legal registration ID scheme ID |
| BT-34-1 | Scheme identifier | Seller electronic address scheme ID |
| BT-46-1 | Scheme identifier | Buyer identifier scheme ID |
| BT-47-1 | Scheme identifier | Buyer legal registration ID scheme ID |
| BT-49-1 | Scheme identifier | Buyer electronic address scheme ID |
| BT-60-1 | Scheme identifier | Payee identifier scheme ID |
| BT-61-1 | Scheme identifier | Payee legal registration ID scheme ID |
| BT-71-1 | Scheme identifier | Deliver to location ID scheme ID |
| BT-128-1 | Scheme identifier | Invoice line object identifier scheme ID |
| BT-130 | Invoiced quantity unit of measure code | Invoiced quantity unit of measure |
| BT-157-1 | Scheme identifier | Item standard identifier scheme ID |
| BT-158-1 | Scheme identifier | Item classification identifier scheme ID |
| BT-158-2 | Scheme version identifier | Item classification identifier scheme version ID |

### 3.4 Name differences — trivial (abbreviations only)

These are just abbreviations used in the 3-way file (`representative`→`rep`, `identifier`→`ID`, `telephone number`→`telephone`, `email address`→`email`, etc.). Listed for completeness only:

| ID | EN 16931 PDF | 3-way mapping |
|----|--------------|---------------|
| BT-30 | Seller legal registration identifier | Seller legal registration ID |
| BT-32 | Seller tax registration identifier | Seller tax registration ID |
| BT-33 | Seller additional legal information | Seller additional legal info |
| BT-42 | Seller contact telephone number | Seller contact telephone |
| BT-43 | Seller contact email address | Seller contact email |
| BT-47 | Buyer legal registration identifier | Buyer legal registration ID |
| BT-57 | Buyer contact telephone number | Buyer contact telephone |
| BT-58 | Buyer contact email address | Buyer contact email |
| BT-61 | Payee legal registration identifier | Payee legal registration ID |
| BT-62 | Seller tax representative name | Seller tax rep name |
| BT-63 | Seller tax representative VAT identifier | Seller tax rep VAT ID |
| BT-64 | Tax representative address line 1 | Tax rep address line 1 |
| BT-65 | Tax representative address line 2 | Tax rep address line 2 |
| BT-66 | Tax representative city | Tax rep city |
| BT-67 | Tax representative post code | Tax rep post code |
| BT-68 | Tax representative country subdivision | Tax rep country subdivision |
| BT-69 | Tax representative country code | Tax rep country code |
| BT-71 | Deliver to location identifier | Deliver to location ID |
| BT-86 | Payment service provider identifier | Payment service provider ID |
| BT-87 | Payment card primary account number | Payment card PAN |
| BT-89 | Mandate reference identifier | Mandate reference ID |
| BT-90 | Bank assigned creditor identifier | Bank assigned creditor ID |
| BT-91 | Debited account identifier | Debited account ID |
| BT-164 | Tax representative address line 3 | Tax rep address line 3 |

