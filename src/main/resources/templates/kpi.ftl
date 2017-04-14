[
<#if kpiList?? >
    <#list kpiList as kpi>
    {"id":"${kpi.id}","kpiName":"${kpi.name}"}<#if kpi_has_next>,</#if>
    </#list>
</#if> 
]
