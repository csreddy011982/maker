<#-- SPL Report Template -->
${header.adviceFeedName}                  ${header.recordNumber}

<#list transactions as transaction>
H${transaction.accountNumber}       ${transaction.sequenceNumber}                                                      PAGE 1
D${transaction.sequenceNumber + 1}                     ${transaction.companyName} ${transaction.companyCode}
D${transaction.sequenceNumber + 2}                      ACH CASH CONCENTRATION
D${transaction.sequenceNumber + 3}                      REPORTED AS OF ${transaction.reportDate}
D${transaction.sequenceNumber + 4}                        PRINTED ON ${transaction.printDate}
D${transaction.sequenceNumber + 5}         ACCOUNT ${transaction.accountNumberFormatted} ${transaction.companyName} ${transaction.companyCode}
D${transaction.sequenceNumber + 6}          REDEPOSIT  / REJECT REPAIR ACTIVITY
D${transaction.sequenceNumber + 7}                AUTOMATED CLEARING HOUSE DEPOSIT VERIFICATION FOR: ${transaction.companyCode}
D${transaction.sequenceNumber + 8}          FR/ABA      UNIT BANK DDA      UNIT      UNIT NAME    AMOUNT
D${transaction.sequenceNumber + 9}           -----------------------------------------------------------
D${transaction.sequenceNumber}            ${transaction.frAba}     ${transaction.bankDda}   ${transaction.unit}      ${transaction.unitName}       ${transaction.amount}
</#list>

D${trailer.sequenceNumber}          DEPOSIT ACCOUNT NUMBER: ${trailer.accountNumberFormatted}  DEPOSIT TOTAL: ${trailer.totalCreditAmount}
D${trailer.sequenceNumber + 1}          TOTAL CREDITS: ${trailer.totalCreditAmount}  TOTAL DEBITS: ${trailer.totalDebitAmount}
T${trailer.accountNumber}       ${trailer.recordNumber}
T${header.adviceFeedName}000001