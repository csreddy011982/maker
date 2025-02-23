
<#-- Iterate Over Each Account (transactions list) -->
<#list transactions as transaction>

<#-- ✅ Ensure recordNumber exists -->
<#if transaction.recordNumber??>
H${header.recordNumber}                            ${transaction.recordNumber}
D${transaction.recordNumber} DETAIL DATE: ${header.creationDate}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY          PAGE 1
D${transaction.recordNumber}            LINCOLN LIFE
D${transaction.recordNumber}             COMPANY: 135847320
D${transaction.recordNumber}             ATTN: MICHELLE JURY
D${transaction.recordNumber}             1401 S HARRISON ST 6H STOP 15
D${transaction.recordNumber}             FORT WAYNE, IN 46802
</#if>
<#-- Transaction Details Header -->
D${transaction.recordNumber}             INDIVIDUAL ID       INDIVIDUAL NAME       TC      CREDIT AMOUNT    DEBIT AMOUNT   ITEM COUNT     ABA #           ACCOUNT NUMBER
D${transaction.recordNumber}             -------------------------------------------------------------------------------------------------------------

<#-- ✅ Fix: Iterate Over Individual Transactions Correctly -->
<#if transaction.detailsList??>
<#list transaction.detailsList as detail>
<#if detail.recordNumber??>
D${detail.recordNumber}             ${detail.individualID!""}           ${detail.individualName!""}        ${detail.tranCode!""}       ${detail.creditAmount!""}        ${detail.debitAmount!""}            ${detail.itemCount!"1"}              ${detail.abaNumber!""}      ${detail.accountNumber!""}
D${detail.recordNumber}                                 FILE REFERENCE NUMBER: ${detail.fileReferenceNumber!""}
</#if>
</#list>
<#else>
<!-- No transaction details available -->
</#if>
</#list>
<#-- Summary Footer -->
<#if totalRecordNumber??>
${totalRecordNumber}           DETAIL DELETIONS TOTAL                         ${totalCreditAmount!""}       ${totalDebitAmount!""}
</#if>
<#if deletionRecordNumber??>
${deletionRecordNumber}           DELETIONS TOTAL                                ${totalCreditAmount!""}       ${totalDebitAmount!""}
</#if>
<#-- Trailer Section -->
<#if trailer.recordNumber??>
T${trailer.recordNumber}
</#if>
