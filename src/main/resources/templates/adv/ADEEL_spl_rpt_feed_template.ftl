LADEL${header.reportDate}${header.creationDate}TNT       550
H${report.accountNumberNoPadding}              000001
D00000011    DATE: ${report.date}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY         PAGE     1
D00000020            LINCOLN LIFE
D0000003             COMPANY: ${report.companyCode}               ACCOUNT NUMBER: 0000000${report.accountNumber}
D0000004             ATTN: MICHELLE JURY
D0000005             1401 S HARRISON ST 6H STOP 15
D0000006             FORT WAYNE, IN 46802
D0000007
D00000080                                                          CREDIT    DEBIT    ITEM
D0000009          INDIVIDUAL ID       INDIVIDUAL NAME       TC     AMOUNT    AMOUNT   COUNT     ABA #           ACCOUNT NUMBER
D0000010          ----------------    -----------------     --     ------    -------   ----     ----            --------------
<#list detailRecords as detailRecord>
D${detailRecord.sequence}  ${detailRecord.individualId}  ${detailRecord.individualName}  ${detailRecord.tc}  ${detailRecord.creditedAmount}  ${detailRecord.debitAmount}  ${detailRecord.itemCount}  ${detailRecord.aba}  ${detailRecord.randomAccountNumber}
D${detailRecord.nextSequence}           FILE REFERENCE NUMBER: ${detailRecord.referenceNumber}
</#list>
D${report.sequenceOne}           DETAIL DELETIONS TOTAL ${report.total}               0.00            ${report.totalItemCount}
D${report.sequenceTwo}           DELETIONS TOTAL  ${report.total}                     0.00
T${report.accountNumberNoPadding}           ${report.totalLines}
