<#-- ACH Deletion Report FreeMarker Template -->
L${firstLine.adviceFeedName}${firstLine.creationDate}${firstLine.creationDateTime}TNT${""?right_pad(5)}550
<#-- Iterate Over Each Account (transactions list) -->
<#list transactions as transaction>
H${transaction.account?right_pad(50)}${transaction.sequence}
${transaction.recordNumber}                            WW GRAINGER ${transaction.companyCode}          PAGE 1
${transaction.recordNumber1}             ACH CASH CONCENTRATION
${transaction.recordNumber2}             REPORTED AS OF ${transaction.previousDate}
${transaction.recordNumber3}             PRINTED ON ${transaction.currentDate}
${transaction.recordNumber4}             ACCOUNT ${transaction.accountNumber} WW GRAINGER ${transaction.companyCode}
${transaction.recordNumber5}             REDEPOSIT  / REJECT REPAIR ACTIVITY
${transaction.recordNumber6}             AUTOMATED CLEARING HOUSE DEPOSIT VERIFICATION FOR: ${transaction.companyCode}
<#-- Transaction Details Header -->
${transaction.recordNumber7}             FR/ABA      UNIT BANK DDA      UNIT      UNIT NAME    AMOUNT
${transaction.recordNumber8}             ------------------------------------------------------------
<#-- Iterate Over Individual Transactions Correctly -->
<#list transaction.detailsList as detail>
${detail.recordNumber}           ${detail.frAba!""}   ${detail.unitBankDDA!"0000000000"?right_pad(10)}   ${detail.unit!""}        ${detail.unitName!""}         ${detail.creditAmount!""}
</#list>
<#-- Summary Footer -->
${transaction.deposit}           DEPOSIT ACCOUNT NUMBER: ${transaction.accountNumber}                DEPOSIT TOTAL: ${transaction.totalCreditAmount}
${transaction.total}             TOTAL CREDITS:  ${transaction.totalCreditAmount}                     TOTAL DEBITS:${transaction.totalDebitAmount}
T${transaction.account}           ${transaction.lastEndRecord}
</#list>



