<#-- ACH Deletion Report FreeMarker Template -->
L${firstLine.adviceFeedName}${firstLine.creationDate}TNT                550

<#-- Iterate Over Each Account (transactions list) -->
<#list transactions as transaction>
H${transaction.account}                            ${transaction.sequence}
${transaction.recordNumber}  DATE: ${transaction.creationDate}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY          PAGE 1
${transaction.recordNumber1}            LINCOLN LIFE
${transaction.recordNumber2}             COMPANY: 135847320
${transaction.recordNumber3}             ATTN: MICHELLE JURY
${transaction.recordNumber4}             1401 S HARRISON ST 6H STOP 15
${transaction.recordNumber5}             FORT WAYNE, IN 46802
<#-- Transaction Details Header -->
${transaction.recordNumber6}             INDIVIDUAL ID       INDIVIDUAL NAME       TC      CREDIT AMOUNT    DEBIT AMOUNT   ITEM COUNT     ABA #           ACCOUNT NUMBER
${transaction.recordNumber7}             -------------------------------------------------------------------------------------------------------------
<#-- Iterate Over Individual Transactions Correctly -->
<#list transaction.detailsList as detail>
${detail.recordNumber}             ${detail.individualID!""}           ${detail.individualName!""}        ${detail.tranCode!""}       ${detail.creditAmount!""}        ${detail.debitAmount!""}            ${detail.itemCount!""}              ${detail.abaNumber!""}      ${detail.accountNumber!""}
${detail.recordNumber1}                                 FILE REFERENCE NUMBER: ${detail.fileReferenceNumber!""}
</#list>
<#-- Summary Footer -->
${transaction.deleteTotal}           DETAIL DELETIONS TOTAL ${transaction.totalCreditAmount}                                                                  ${transaction.totalItemCount}
${transaction.deleteTotal2}          DELETIONS TOTAL  ${transaction.totalDebitAmount}
T${transaction.account}           ${transaction.deleteTotal3}
</#list>



