<#-- ACH Deletion Report FreeMarker Template -->
L${firstLine.adviceFeedName}${firstLine.creationDate}TNT                550
<#-- Iterate Over Each Account (transactions list) -->
<#list transactions as transaction>
<#-- ✅ Ensure recordNumber exists -->
<#if transaction.recordNumber??>
H${transaction.account}                            ${transaction.sequence}
${transaction.recordNumber}  DATE: ${transaction.creationDate}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY          PAGE 1
${transaction.recordNumber1}            LINCOLN LIFE
${transaction.recordNumber2}             COMPANY: 135847320
${transaction.recordNumber3}             ATTN: MICHELLE JURY
${transaction.recordNumber4}             1401 S HARRISON ST 6H STOP 15
${transaction.recordNumber5}             FORT WAYNE, IN 46802
</#if>
<#-- Transaction Details Header -->
${transaction.recordNumber6}             INDIVIDUAL ID       INDIVIDUAL NAME       TC      CREDIT AMOUNT    DEBIT AMOUNT   ITEM COUNT     ABA #           ACCOUNT NUMBER
${transaction.recordNumber7}             -------------------------------------------------------------------------------------------------------------
<#-- ✅ Fix: Iterate Over Individual Transactions Correctly -->
<#if transaction.detailsList??>
<#list transaction.detailsList as detail>
<#if detail.recordNumber??>
${detail.recordNumber}             ${detail.individualID!""}           ${detail.individualName!""}        ${detail.tranCode!""}       ${detail.creditAmount!""}        ${detail.debitAmount!""}            ${detail.itemCount!"1"}              ${detail.abaNumber!""}      ${detail.accountNumber!""}
${detail.recordNumber1}                                 FILE REFERENCE NUMBER: ${detail.fileReferenceNumber!""}
</#if>
</#list>
T${transaction.account}         ${transaction.}

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
