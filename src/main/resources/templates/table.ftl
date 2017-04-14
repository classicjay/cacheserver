{
<#if data?? >
    "2016-06":[<#list data.items as items>${items} <#if items_has_next>,</#if></#list>],
    "2016-07":[<#list data.items as items>${items} <#if items_has_next>,</#if></#list>],
    "2016-08":[<#list data.items as items>${items} <#if items_has_next>,</#if></#list>],
    "2016-09":[<#list data.items as items>${items} <#if items_has_next>,</#if></#list>]
</#if>
}

