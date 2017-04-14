{
<#list dataMap?keys as key>
    <#if key = "kpi">
    "${key}":{
        <#assign kpiMap = dataMap[key]>
        <#list kpiMap?keys as kpiMapKey>
        "${kpiMapKey}":"${kpiMap["${kpiMapKey}"]}"<#if kpiMapKey_has_next>,</#if>
        </#list>
    },
    </#if>
    <#if key = "thData">
    "${key}":[
        <#assign thDataList = dataMap[key]>
        <#if (thDataList?size>0)>
            <#list thDataList as thDataMap>
            {
            <#list thDataMap?keys as thDataMapKey>
                <#if thDataMapKey = "year">
                "${thDataMapKey}":"${thDataMap["${thDataMapKey}"]}",
                </#if>
                <#if thDataMapKey = "months">
                    <#assign months = thDataMap[thDataMapKey]>
                "${thDataMapKey}":[
                    <#list months as month>
                    "${month}"<#if month_has_next>,</#if>
                    </#list>
                ]
                </#if>
            </#list>
            }<#if thDataMap_has_next>,</#if>
            </#list>
        </#if>
    ],
    </#if>
    <#if key = "tbodyData">
    "${key}":[
        <#assign tbodyDataList = dataMap[key]>
        <#if (tbodyDataList?size>0)>
            <#list tbodyDataList as tbodyDataMap>
                {
                <#list tbodyDataMap?keys as tbodyDataMapKey>
                    <#if tbodyDataMapKey = "year">
                    "${tbodyDataMapKey}":"${tbodyDataMap["${tbodyDataMapKey}"]}",
                    </#if>
                    <#if tbodyDataMapKey = "months">
                    "${tbodyDataMapKey}":[
                        <#assign monthsList = tbodyDataMap[tbodyDataMapKey]>
                        <#if (monthsList?size>0)>
                        <#list monthsList as monthMap>
                            {
                            <#list monthMap?keys as monthMapkey>
                                <#if monthMapkey = "month">
                                "${monthMapkey}":"${monthMap["${monthMapkey}"]}",
                                </#if>
                                <#if monthMapkey = "net_value">
                                "${monthMapkey}":"${monthMap["${monthMapkey}"]}",
                                </#if>
                                <#if monthMapkey = "obs_values"><#assign obsValues = monthMap[monthMapkey]>
                                "${monthMapkey}":[
                                    <#list obsValues as obs>
                                    "${obs}"<#if obs_has_next>,</#if>
                                    </#list>
                                ]
                                </#if>
                            </#list>
                            }<#if monthMap_has_next>,</#if>
                        </#list>
                    </#if>
                    ]
                    </#if>
                </#list>
                }<#if tbodyDataMap_has_next>,</#if>
            </#list>
        </#if>
    ]
    </#if>
</#list>
}



