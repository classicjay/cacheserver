{<#if minMaxDateList?? >
    <#list minMaxDateList as dateList>
    "mindate":"${dateList.MIN_DATE}","maxdate":"${dateList.MAX_DATE}"<#if dateList_has_next>,</#if>
    </#list>
<#else>
    "mindate":"","maxdate":""
</#if>}