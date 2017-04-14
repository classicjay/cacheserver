[
<#if conditionList?? >
    <#list conditionList as condition>
    {
       "tid":"${condition.tid}",
       "tname":"${condition.tname}",
       "data":[
       <#if (condition.data?size>0)>
          <#list condition.data as data>
             {"id":"${data.id}","text":"${data.text}"}<#if data_has_next>,</#if>
         </#list>
       <#else>
           {"id":"","text":""}
       </#if>
             ]
    }<#if condition_has_next>,</#if>
    </#list>
<#else>
    {
       "tid":"",
       "tname":"",
       "data":[
              {"id":"","text":""}
              ]
    }
</#if> 
]
