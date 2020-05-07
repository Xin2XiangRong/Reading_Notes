#### 为什么使用mycat

如何使用关系型数据库解决海量存储的问题呢？此时就需要做数据库集群，为了提高查询性能将一个数据库的数据分散到不同的数据库中存储，为应对此问题就出现了——MyCat 

综上所述：Mycat作用为：能满足数据库数据大量存储；提高了查询性能。

主要是由于随着互联网发展，数据量越来越大，对性能要求越来越高，传统数据库存在着先天性的缺陷，即单机（单库）性能瓶颈，并且扩展困难。这样既有单机单库瓶颈，却又扩展困难，自然无法满足日益增长的海量数据存储及其性能要求，所以才会出现了各种不同的NoSQL 产品，NoSQL 根本性的优势在于在云计算时代，简单、易于大规模分布式扩展，并且读写性能非常高。

**垂直**：将不同的表切分到不同的数据库

优点——

* 拆分后业务清晰，拆分规则明确
* 系统之间整合或扩展容易
* 数据维护简单

缺点——

* 部分业务表无法join，只能通过接口方式解决，提高了系统复杂度
* 受每种业务不同的限制存在单库性能瓶颈，不易数据扩展跟性能提高
* 事务处理复杂

**水平**：将同一种表按照某种条件切分到不同的数据库中

优点：——

* 拆分规则抽象好，join 操作基本可以数据库做；
* 不存在单库大数据，高并发的性能瓶颈；
* 应用端改造较少；
*  提高了系统的稳定性跟负载能力。

缺点：——

*  拆分规则难以抽象；
*  分片事务一致性难以解决；
*  数据多次扩展难度跟维护量极大；
*  跨库 join 性能较差

​         ![img](01_picture/MyCat%E5%AD%A6%E4%B9%A0%E6%80%BB%E7%BB%93/clip_image002.jpg)  

**schema.xml**

schema标签：定义mycat实例中的逻辑库 
 table标签：定义mycat实例中的逻辑表 
 dataNode标签：定义mycat中的数据节点，也是通常说的数据分片 
 dataHost标签：作为最底层标签存在，定义了具体的真正存放数据的数据库实例，读写分离配置和心跳语句

**server.xml**

name：逻辑用户名，即登录mycat的用户名

password：逻辑密码，即登录mycat的用户名对应的密码 
 schemas：逻辑数据库，可配置多个，用英文逗号隔开，对应于schema.xml文件中配置的逻辑数据库，两者对应 
 readOnly：该数据库是否为只读，如果true就是只读

Mycat的安装其实只要解压下载的目录就可以，解压后的目录如下：

|  目录  |                    说明                     |
| :----: | :-----------------------------------------: |
|  bin   |        mycat命令，启动、重启、停止等        |
| catlet |         catlet为Mycat的一个扩展功能         |
|  conf  |           Mycat 配置信息,重点关注           |
|  lib   |     Mycat引用的jar包，Mycat是java开发的     |
|  logs  | 日志文件，包括Mycat启动的日志和运行的日志。 |

Mycat是不是配置以后，就能完全解决分表分库和读写分离问题？

 Mycat配合数据库本身的复制功能，可以解决读写分离的问题，但是针对分表分库的问题，不是完美的解决。或者说，至今为止，业界没有完美的解决方案。
 分表分库写入能完美解决，但是，不能完美解决主要是联表查询的问题，Mycat支持两个表联表的查询，多余两个表的查询不支持。 其实，很多数据库中间件关于分表分库后查询的问题，都是需要自己实现的，而且基本都不支持联表查询，Mycat已经算做地非常先进了。
 分表分库的后联表查询问题，大家通过合理数据库设计来避免。

**由于数据切分后数据Join 的难度在此也分享一下数据切分的经验：**

* 第一原则：能不切分尽量不要切分。
* 第二原则：如果要切分一定要选择合适的切分规则，提前规划好。
* 第三原则：数据切分尽量通过数据冗余或表分组（Table Group）来降低跨库Join 的可能。
* 第四原则：由于数据库中间件对数据Join 实现的优劣难以把握，而且实现高性能难度极大，业务读取尽量少使用多表Join

MyCAT 是使用JAVA 语言进行编写开发，使用前需要先安装JAVA 运行环境(JRE),由于MyCAT 中使用了JDK7 中的一些特性，所以要求必须在JDK7 以上的版本上运行。

#### Mycat原理

Mycat的原理中最重要的一个动词是“拦截”，它拦截了用户发送过来的SQL语句，首先对SQL语句做了一些特定的分析：如分片分析、路由分析、读写分离分析、缓存分析等，然后将此SQL发往后端的真实数据库，并将返回的结果做适当的处理，最终返回给用户。

#### Mycat的配置

##### schema.xml

###### 1）schema标签

chema 标签用于定义 MyCat 实例中的逻辑库，MyCat 可以有多个逻辑库，每个逻辑库都有自己的相关配置。可以使用 schema 标签来划分这些不同的逻辑库。

1. dataNode 该属性用于绑定逻辑库到某个具体的 database 上，1.3 版本如果配置了 dataNode，则不可以配置分片表，1.4 可以配置默认分片，只需要配置需要分片的表即可
2. checkSQLschema 对于“select * from TESTDB.travelrecord;”，如果此属性设为true，，mycat会将语句修改为“select * from travelrecord;”，避免在后端数据库发生table找不到的错误。
3. sqlMaxLimt 不设置该值的话，MyCat 默认会把查询到的信息全部都展示出来，造成过多的输出。所以，在正常使用中，还是建议加上一个值，用于减少过多的数据返回。当然 SQL 语句中也显式的指定 limit 的大小，不受该属性的约束。需要注意的是，如果运行的 schema 为非拆分库的，那么该属性不会生效。需要手动添加 limit 语句

###### 2）table标签

Table 标签定义了 MyCat 中的逻辑表，所有需要拆分的表都需要在这个标签中定义。

1. name 逻辑表的表名，同个schema中唯一，与实际物理的表名相同

2. dataNode 如果需要定义的dn过多，可以按如下方式：

   ```xml
   <table name="travelrecord" dataNode="multipleDn$0-99,multipleDn2$100-199" rule="auto-shardinglong" ></table>
   <dataNode name="multipleDn$0-99" dataHost="localhost1" database="db$0-99" ></dataNode>
   <dataNode name="multipleDn2$100-199" dataHost="localhost1" database=" db$100-199" ></dataNode>
   ```

3. ruleRequired属性 用于指定表是否绑定分片规则，如果配置为 true，但没有配置具体rule 的话，程序会报错。

4. primaryKey属性对应了该逻辑表所对应的真实表的主键。如果分片的规则是依据非主键，就会发送查询语句到所有配置的DN上；如果使用该属性配置真实表的主键，那么mycat会缓存主键与具体的DN的信息，那么再次使用非主键进行查询的时候就不会进行广播式的查询了，就会直接发送语句给具体的DN。但是缓存没有命中的时候还是会发送给所有DN

5. type 全局表（global） 普通表（其他）

6. autoIncrement 默认为禁用false，设true指定这个表有使用自增长主键。 mycat 目前提供了自增长主键功能，但是如果对应的 mysql 节点上数据表，没有定义 auto_increment，那么在 mycat 层调用 last_insert_id()也是不会返回结果的。 

7. subTables 使用方式添加 subTables="t_order$1-2,t_order3"。目前分表 1.6 以后开始支持 并且 dataNode 在分表条件下只能配置一个，分表条件下不支持各种条件的join 语句

8. needAddLimit 默认为true，mycat自动的为我们加上 LIMIT 100。当然，如果语句中有 limit，就不会在次添加了。False禁用

###### 3）childTable标签

childTable 标签用于定义 E-R 分片的子表。通过标签上的属性与父表进行关联。

1. name 定义子表的表名

2.  joinKey 插入子表的时候会使用这个列的值查找父表存储的数据节点（DN）。

3. parentKey属性指定的值一般为与父表建立关联关系的列名。程序首先获取 joinkey 的值，再通过 parentKey 属性指定的列名产生查询语句，通过执行该语句得到父表存储在哪个分片上。从而确定子表存储的位置

   ```xml
   <!--对于orders.customer_id=customer.id-->
   <table name="customer" dataNode="dn1,dn2" rule="sharding-by-intfile">
   <childTable name="orders" joinKey="customer_id" parentKey="id"/>
   </table>
   ```

4. primaryKey

5. needAddLimit  

###### 4）dataNode标签

定义数据节点即数据分片。一个dataNode标签就是一个独立的数据分片。 

```xml
<dataNode name="dn1" dataHost="lch3307" database="db1" ></dataNode>
```

 使用名字为 lch3307 数据库实例上的 db1 物理数据库，这就组成一个数据分片，最后，我们使用名字 dn1 标识这个分片。dataHost ：数据库实例；database：具体数据库实例上的具体库

###### 5）dataHost标签

在mycat逻辑课中作为最底层的标签存在，直接定义了具体的数据库实例、读写分离配置和心跳语句

1. name唯一标识

2. maxCon指定每个读写实例连接池的最大连接。也就是说，标签内嵌套的 writeHost、readHost 标签都会使用这个属性的值来实例化出连接池的最大连接数。

3. minCon指定每个读写实例连接池的最小连接，初始化连接池的大小。

4. balance 负载均衡类型 

   a. balance="0", 不开启读写分离机制，所有读操作都发送到当前可用的 writeHost 上。

   b. balance="1"，全部的 readHost 与 stand by writeHost 参与 select 语句的负载均衡，简单的说，当双主双从模式(M1->S1，M2->S2，并且 M1 与 M2 互为主备)，正常情况下，M2,S1,S2 都参与 select 语句的负载均衡。

   c. balance="2"，所有读操作都随机的在 writeHost、readhost 上分发。

   d. balance="3"，所有读请求随机的分发到 wiriterHost 对应的 readhost 执行，writerHost 不负担读压力，注意 balance=3 只在 1.4 及其以后版本有，1.3 没有

5. writeType 负载均衡类型 。 3种： “0”：所有写操作发送到配置的第一个 writeHost，第一个挂了切到还生存的第二个 writeHost，重新启动后已切换后的为准，切换记录在配置文件中:dnindex.properties . “1”：所有写操作随机发送到配置的writeHost，1.5后废弃不推荐。 swithType属性

6. dbType 后端连接的数据库类型，目前支持二进制的mysql协议，还有其他使用JDBC连接的数据库。

7.  dbDriver属性指定后端数据库使用的Driver，native或JDBC。  使用 native 的话，因为这个值执行的是二进制的 mysql 协议，所以可以使用 mysql 和 maridb。其他类型的数据库则需要使用 JDBC 驱动来支持。从 1.6 版本开始支持 postgresql 的 native 原始协议。如果使用 JDBC 的话需要将符合 JDBC 4 标准的驱动 JAR 包放到 MYCAT\lib 目录下，并检查驱动 JAR 包中包括如下目录结构的文件：META-INF\services\java.sql.Driver。在这个文件内写上具体的 Driver 类名，例如：com.mysql.jdbc.Driver

8.  switchType “1”：默认，自动切换；“2”：基于 MySQL 主从同步的状态决定是否切换；“3”：基于 MySQL galary cluster 的切换机制（适合集群）。  

###### 6）Heatbeat标签

指明用于和后端数据库进行心跳检查的语句

###### 7）writeHost标签、readHost标签

指定后端数据库的相关配置给 mycat，用于实例化后端连接池。唯一不同的是，writeHost 指定写实例、readHost 指定读实例，组织这些读写实例来满足系统的要求。

在一个 dataHost 内可以定义多个 writeHost 和 readHost。但是，如果 writeHost 指定的后端数据库宕机，那么这个 writeHost 绑定的所有 readHost 都将不可用。另一方面，由于这个 writeHost 宕机系统会自动的检测到，并切换到备用的 writeHost 上去

1. host 标识不同实例 一般 writeHost 我们使用\*M1，readHost 我们用*S1
2. url后端实例连接地址，如果是使用 native 的 dbDriver，则一般为 address:port 这种形式。用 JDBC 或其他的dbDriver，则需要特殊指定。当使用 JDBC 时则可以这么写：jdbc:mysql://localhost:3306/。
3. user    password
4. weight 权重配置在readhost中作为读节点的权重。

##### server.xml

保存了mycat需要的系统配置信息。其在代码内直接的映射类为SystemConfig类

###### 1）user标签

主要用于定义登录 mycat 的用户和权限。可以修改 user 标签的 name属性来指定用户名；修改 password 内的文本来修改密码；修改 readOnly 为true 或false 来限制用户是否只是可读的；修改 schemas 内的文本来控制用户可放问的schema；修改schemas 内的文本来控制用户可访问的schema，同时访问多个 schema 的话使用 , 隔开。

* Benchmark：mycat连接服务降级处理，以benchmark设定值为基准，当前端的整体connection数达到基准值时，对来自该账户的请求开始拒绝连接，0或不设表示不限制。

* usingDecrypt：是否对密码加密，0：否（默认）；1：开启

* privileges子节点 对用户的schema及下级的table进行精细化的DML权限控制。

* Check：标识是否开启DML权限检查，默认false。

  ```xml
  <user name="zhuam">
  <property name="password">111111</property>
  <property name="schemas">TESTDB,TESTDB1</property>
  <!-- 表级权限: Table 级的 dml(curd)控制，未设置的 Table 继承 schema 的 dml -->
  <!-- TODO: 非 CURD SQL 语句, 透明传递至后端 -->
  <privileges check="true">
  <schema name="TESTDB" dml="0110" >
  <table name="table01" dml="0111"></table>
  <table name="table02" dml="1111"></table>
  </schema>
  <schema name="TESTDB1" dml="0110">
  <table name="table03" dml="1110"></table>
  <table name="table04" dml="1010"></table>
  </schema>
  </privileges>
  </user>
  ```

  Schema/table上的dml属性描述

  | 参数 | 说明                           | 事例（禁止增删改查） |
  | ---- | ------------------------------ | -------------------- |
  | Dml  | Insert，update，select，delete | 0000                 |

###### 2）System标签

这个标签内嵌套的所有 property 标签都与系统配置有关

1. defaultSqlParser属性 指定sql解析器
2. processors属性，主要用于指定系统可用的线程数，默认值为机器CPU核心线程数。
3. processorBufferChunk属性。 指定每次分配socket direct buffer的大小，默认是4096个字节。这个属性也影响buffer pool的长度。如果一次性获取的数过大，buffer不够用，经常出现警告，则可以适当调大。
4. sequenceHanblerType 指定使用Mycat全局序列的类型。0为本地文件方式，1为数据库方式，2为时间戳序列方式，3为分布式ZK ID生成器，4为zk递增id生成。

#### Mycat的分片join

尽量避免使用left join或right join，而用inner join。在使用 Left join 或 Right join 时，ON 会优先执行，where 条件在最后执行，所以在使用过程中，条件尽可能的在 ON 语句中判断，减少 where 的执行。少用子查询，而用 join。

```xml
<!--ER join-->
<table name="customer" dataNode="dn1,dn2" rule="sharding-by-intfile">
<childTable name="orders" joinKey="customer_id" parentKey="id"/>
</table>
```

customer 采用 sharding-by-intfile 这个分片策略，分片在 dn1,dn2 上，orders 依赖父表进行分片，两个表的关联关系为 orders.customer_id=customer.id。  

#### 全局序列号

Mycat提供了全局sequence，并且提供了包含本地配置和数据库配置等多种实现方式

##### 1、本地文件方式

原理：此方式 MyCAT 将 sequence 配置到文件中，当使用到 sequence 中的配置后，MyCAT 会更新classpath 中的 sequence_conf.properties 文件中 sequence 当前的值。Server.xml中配置：

```xml
<system><property name="sequnceHandlerType">0</property></system>
```

sequnceHandlerType配置为0，表示使用本地文件方式。使用示例：

```sql
Insert into table(id,name) values (next value for MYCATSEQ_GLOBAL,'test');
```

缺点：当Mycat重新发布后，配置文件中的sequence会恢复到初始值。

优点：本地加载，读取速度较快。

##### 2、数据库方式

原理：在数据库中建立一张表，存放 sequence 名称(name)，sequence 当前值(current_value)，步长(increment int 类型每次读取多少个 sequence，假设为 K)等信息。Server.xml配置：

```xml
<system><property name="sequnceHandlerType">1</property></system>
```

sequnceHandlerType配置为1，表示使用数据库方式生成sequence。创建MYCAT_SEQUENCE表

```sql
CREATE TABLE MYCAT_SEQUENCE (name VARCHAR(50) NOT NULL,current_value INT NOT
NULL,increment INT NOT NULL DEFAULT 100, PRIMARY KEY(name)) ENGINE=InnoDB;
```

插入一条 sequence——

```sql
INSERT INTO MYCAT_SEQUENCE(name,current_value,increment) VALUES ('GLOBAL', 100000,100);
```

创建相关function

……

在sequence_db_conf.properties中作相关配置

##### 3、本地时间戳方式

ID= 64 位二进制 (42(毫秒)+5(机器 ID)+5(业务编码)+12(重复累加)

换算成十进制为 18 位数的 long 类型，每毫秒可以并发 12 位二进制的累加。

配置server.xml：

```xml
<property name="sequnceHandlerType">2</property>
```

在mycat下配置：sequence_time_conf.properties。WORKID=0-31 任意整数。DATAACENTERID=0-31 任意整数

#### Mycat分片规则

1. mycat全局表
2. ER分片表
3. ……

Mycat常用的分片规则：

1. 分片枚举hash-int：通过在配置文件中配置可能的枚举 id，自己配置分片，本规则适用于特定的场景，比如有些业务需要按照省份或区县来做保存，而全国省份区县固定的，这类业务使用本条规则。
2.  固定分片hash算法func1 ：本条规则类似于十进制的求模运算，区别在于是二进制的操作，是取 id 的二进制低 10 位，即 id 二进制&1111111111
3. 范围约定rang-long：此分片适用于，提前规划好分片字段某个范围属于哪个分片，
4. 取模 mod-long：此规则为对分片字段求摸运算
5. 按日期（天）分片sharding-by-date
6. 取模范围约束sharding-by-pattern：此种规则是取模运算与范围约束的结合，主要为了后续数据迁移做准备，即可以自主决定取模后数据的节点分布。