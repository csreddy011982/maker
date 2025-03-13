LADELL${header.reportDate}${header.creationDate}TNT       550
H${report.accountNumberNoPadding}              000001
D00000011    DATE: ${report.date}   ACH DELETION REPORT - THE NORTHERN TRUST COMPANY         PAGE     1
D00000020        LINCOLN LIFE
D0000003                COMPANY: 135847320
D0000004                ATTN: MICHELLE JURY
D0000005               1401 S HARRISON ST 6H STOP 15
D0000006               FORT WAYNE, IN 46802
D0000007
D00000080
D0000009          INDIVIDUAL ID       INDIVIDUAL NAME       TC      CREDIT AMOUNT    DEBIT AMOUNT   ITEM COUNT     ABA #           ACCOUNT NUMBER
D0000010          ----       ----            ----    ---------    ------


<#list detailRecords as detailRecord>
D${detailRecord.sequence}  ${detailRecord.individualId}  ${detailRecord.dda}  ${detailRecord.unit}  ${detailRecord.unitName}${detailRecord.amount}
</#list>

D${report.sequenceOne}0  DEPOSIT ACCOUNT NUMBER: ${report.accountNumber}      DEPOSIT TOTAL:     ${report.total}
D${report.sequenceTwo}0  TOTAL CREDITS: ${report.total}              TOTAL DEBITS:              0.00
T${report.accountNumberNoPadding}  ${report.totalLines}
EACON0000001
