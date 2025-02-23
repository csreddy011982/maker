<#-- FreeMarker template for ACH Deletion Report -->
L${header.adviceFeedName}${header.creationDate}TNT      550
<#list transactions as transaction>
H${transaction.accountNumberH}                            ${transaction.accountSequenceNumber}
D${transaction.sequenceNumber} DETAIL DATE: ${header.creationDate}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY          PAGE 1
D${transaction.newSequenceNumber}            LINCOLN LIFE
D${transaction.companySequenceNumber}             COMPANY: 135847320
D${transaction.attnSequenceNumber}             ATTN: MICHELLE JURY
D${transaction.addressSequenceNumber}             1401 S HARRISON ST 6H STOP 15
D${transaction.citySequenceNumber}             FORT WAYNE, IN 46802
D${transaction.additionalSequenceNumber}
D${transaction.specialSequenceNumber}
D${transaction.individualSequenceNumber}             INDIVIDUAL ID       INDIVIDUAL NAME       TC      CREDIT AMOUNT    DEBIT AMOUNT   ITEM COUNT     ABA #           ACCOUNT NUMBER
D${transaction.separatorSequenceNumber}             -------------------------------------------------------------------------------------------------------------

D${transaction.recordNumber}             ${transaction.accountNumber}           ${transaction.individualName}   ${transaction.tranCode}       ${transaction.creditAmount}        ${transaction.debitAmount}            1              ${transaction.abaNumber}      ${transaction.accountNumber}
D${transaction.recordNumber}                                 FILE REFERENCE NUMBER: ${transaction.fileReferenceNumber}

D${transaction.detailDeletions}           DETAIL DELETIONS TOTAL                         ${trailer.totalCreditAmount}       ${trailer.totalDebitAmount}
D${transaction.deletionsTotal}            DELETIONS TOTAL                                ${trailer.totalCreditAmount}       ${trailer.totalDebitAmount}
</#list>
T${trailer.recordNumber}            ${trailer.totalLines}
