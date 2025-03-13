LACON${header.reportDate}${header.creationDate}TNT       550
H${report.accountNumberNoPadding}              000001
D00000011                WW DEMCOMP 9901                                                 PAGE     1
D0000002                ACH CASH CONCENTRATION
D0000003                REPORTED AS OF  ${report.reportedDate}
D0000004                PRINTED ON  ${report.printDate}
D00000050               ACCOUNT 0000000${report.accountNumber}  WW DEMCOMP 9901
D0000006               REDPOSIT / REJECT REPAIR ACTIVITY
D00000070                                   AUTOMATED CLEARING HOUSE DEPOSIT VERIFICATION FOR: 0901
D00000080            FR/ABA     UNIT BANK DDA    UNIT    UNIT NAME    AMOUNT
D0000009                ----       ----            ----    ---------    ------

<#list detailRecords as detailRecord>
D${detailRecord.sequence}  ${detailRecord.aba}  ${detailRecord.dda}  ${detailRecord.unit}  ${detailRecord.unitName}${detailRecord.amount}
</#list>

D${report.sequenceOne}0  DEPOSIT ACCOUNT NUMBER: ${report.accountNumber}      DEPOSIT TOTAL:     ${report.total}
D${report.sequenceTwo}0  TOTAL CREDITS: ${report.total}              TOTAL DEBITS:              0.00
T${report.accountNumberNoPadding}  ${report.totalLines}
EACON0000001
