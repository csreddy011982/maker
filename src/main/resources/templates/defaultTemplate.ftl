<#-- FreeMarker template for ACH Deletion Report -->
L${firstLine.adviceFeedName}${firstLine.creationDate}TNT      550
<#list transactions as transaction>
<#list transaction.header as head>
H${head.accountNumber}                                          ${head.recordSequenceNumber}
</#list>
<#list transaction.company as company>
D${company.transactionSequenceNumber}1     DATE: ${company.currentDate}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY          PAGE ${company.pageNumber}
D${company.lincolnLife}0              LINCOLN LIFE
D${company.companyWithNumber}              COMPANY: 135847320                                                      ACCOUNT ${company.accountNumber}
D${company.add}               ATTN: MICHELLE JURY
D${company.add1}              1401 S HARRISON ST 6H STOP 15
D${company.add2}              FORT WAYNE, IN 46802
D${company.add3}
D${company.add4}0
D${company.add5}    INDIVIDUAL ID       INDIVIDUAL NAME       TC      CREDIT AMOUNT    DEBIT AMOUNT   ITEM COUNT     ABA #           ACCOUNT NUMBER
D${company.add6}     --------------------------------------------------------------------------------------------------------------------------------
<#list transaction.details as detail>
D${detail.recordNumber} ${detail.individualID}       ${detail.individualName}      ${detail.tranCode}      ${detail.creditAmount}       ${detail.debitAmount}      ${detail.itemCount}       ${detail.abaNumber}      ${detail.accountNumber}
D${detail.sequenceFileReferenceNumber}                         FILE REFERENCE NUMBER: ${detail.fileReferenceNumber}
</#list>
<#list transaction.detailsDeleted as detailsDeleted>
D${detailsDeleted.recordNumber}                  DETAIL DELETIONS TOTAL  ${detailsDeleted.totalCreditAmount}                     ${detailsDeleted.totalDebitAmount}         ${detailsDeleted.totalCount}
D${detailsDeleted.sequenceFileReferenceNumber}    DELETIONS TOTAL${detailsDeleted.deletionTotalCredit}                            ${detailsDeleted.totalDebitAmount}         ${detailsDeleted.totalCount}
</#list>
</#list>
<#list transaction.trailer as trailer>
T${trailer.accountNumber}                                          ${trailer.lastDigit}
</#list>
</#list>
