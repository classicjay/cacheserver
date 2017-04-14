[
<#if areaList?? >
    <#list areaList as area>
    {"proId":"${area.pro_id}","proName":"${area.pro_name}"}<#if area_has_next>,</#if>
    </#list>
<#else>
    {"proId":"","proName":""}
</#if>
]
