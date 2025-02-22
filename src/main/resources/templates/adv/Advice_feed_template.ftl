<!-- Header Record -->
${header.recordNumber}|${header.adviceFeedName}|A|${header.creationDate}

<!-- Transaction Records -->
<#list transactions as transaction>
${transaction.recordNumber}|${transaction.accountNumber}|${transaction.tranCode}|${transaction.baiCode}|${transaction.sequenceNumber}|
${transaction.dateTime}|${transaction.amount}|000000000000000000000000000000|

<!-- Details Records -->
<#list transaction.details as detail>
D|${detail.filter}|${detail.recordNumber}|${detail.filter}|${detail.description}
</#list>

<!-- Float Records -->
<#list transaction.floats as float>
F|${float.recordNumber}|100|${float.amount}
</#list>
</#list>

<!-- Trailer Record -->
Z|${trailer.recordNumber}|${trailer.totalLines}|${trailer.creationDate}
