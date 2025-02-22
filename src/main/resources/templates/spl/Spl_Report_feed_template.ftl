<#list reports as report>
${report.header}
<#list report.details as detail>
${detail}
</#list>
${report.footer}
</#list>