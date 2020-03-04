### Druid配置

Druid内置提供了用于监控的StatFilter、日志输出的Log系列Filter、防御SQl注入攻击的WallFilter。

**SQL监控**

SQL监控显示系统已执行过的每条SQL语句的执行情况。通过执行数、执行时间、最慢时间、事务中、错误数、最大并发、执行时间分布等统计维度来展现。

* 执行数：本条sql语句已执行的次数
* 执行时间：本条sql语句累计执行时间(单位：毫秒)
* 最慢：本条sql语句执行最慢一次的耗时(单位：毫秒)
* 执行时间分布【- - - - - - - -】：这8个 – 分别代表8个耗时区间的次数，从左至右依次是：0-1毫秒次数、1-10毫秒次数、10-100毫秒次数、100-1000毫秒次数、1-10秒次数、10-100秒次数、100-1000秒次数、大于1000秒次数。
* 执行+RS时分布与此同理。通过耗时区间，可以发现SQL执行的效率情况，可以反映出数据库或应用是否稳定。

```xml
<!--慢sql记录-->
<bean id="stat-filter" class="com.alibaba.druid.filter.stat.StatFilter">
    <!--慢sql时间设置，即执行时间大于200毫秒的都是慢sql-->
    <property name="slowSqlMillis" value="200"/>
    <property name="logSlowSql" value="true"/>
</bean>

<bean id="log-filter" class="com.alibaba.druid.filter.logging.Log4j2Filter">
    <property name="dataSourceLogEnabled" value="true"/>
    <property name="statementExecutableSqlLogEnable" value="true"/>
</bean>

<bean id="druid-stat-interceptor" class="com.alibaba.druid.support.spring.stat.DruidStatInterceptor">

</bean>
<bean id="druid-stat-pointcut" class="org.springframework.aop.support.JdkRegexpMethodPointcut" scope="prototype">
    <property name="patterns">
        <list>
            <value>com.hikvision.vfp.*.service.*</value>
            <value>com.hikvision.vfp.*.*.service.*</value>
            <value>com.hikvision.bfp.techaccess.*.service.*</value>
            <value>com.hikvision.bfp.techweb.*.service.*</value>
            <value>com.hikvision.bfp.log.*.service.*</value>
            <value>com.hikvision.bfp.statistic.*.service.*</value>
        </list>
    </property>
</bean>
<aop:config>
    <aop:advisor advice-ref="druid-stat-interceptor" pointcut-ref="druid-stat-pointcut"/>
</aop:config>
```

```java
<!-- 属性类型是字符串，通过别名的方式配置扩展插件，常用的插件有：监控统计用的filter:stat，日志用的filter:slf4j，防御sql注入的filter:wall -->
<property name="filters" value="config,stat,wall"/>

<property name="proxyFilters">
    <list>
        <ref bean="stat-filter"/>
        <ref bean="log-filter"/>
    </list>
</property>

<!--pscache-->
<property name="poolPreparedStatements" value="true" />
<property name="maxPoolPreparedStatementPerConnectionSize" value="20" />
```

