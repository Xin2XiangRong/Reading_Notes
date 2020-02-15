### MySQL数据库的体系结构

由以下几部分组成：连接池组件、管理服务和工具组件、SQL接口组件、查询分析器组件、优化器组件、缓冲（cache）组件、插件式存储引擎、物理文件。

![1581561872458](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581561872458.png)

### 存储引擎

#### InnoDB存储引擎

支持事务，其设计目标主要面向在线事务处理（OLTP）的应用。其特点是行锁设计、支持外键，即默认读取操作不会产生锁。   InnoDB通过使用多版本并发控制（MVCC）来获得高并发性，并且实现了SQL标准的4种隔离级别，默认为repeatable级别。

![1581563879513](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581563879513.png)

#### MyISAM存储引擎

不支持事务、表锁设计，支持全文索引，主要面向一些OLAP（联机分析处理）数据库应用

### InnoDB存储引擎

#### 1、InnoDB体系架构

##### 1）后台线程

1. Master Thread：主要负责将缓冲池中的数据异步刷新到磁盘，保证数据的一致性，包括脏页的刷新、合并插入缓冲、UNDO页的回收等。

2. IO Thread：在InnoDB存储引擎中大量使用了AIO来处理写IO请求，这样可以极大提高数据库的性能。而IO Thread的工作主要是负责这些IO请求的回调处理。  

   ```sql
   SHOW VARIABLES LIKE 'innodb_version'
   SHOW VARIABLES LIKE 'innodb_%_io_threads'
   ```

    可以通过命令SHOW ENGINE INNODB STATUS来观察InnoDB中的IO Thread

3. Purge Thread：事务被提交后，其所使用的undolog可能不再需要，因此需要PurgeThead来回收已经使用并分配的undo页

   ```sql
   show VARIABLES like 'innodb_purge_threads'
   ```

##### 2）内存

![1581565396152](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581565396152.png)

```sql
show VARIABLES like 'innodb_buffer_pool_size'
```

![1581565530038](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581565530038.png)

LRU List、Free List和Flush List

InnoDB对传统的LRU算法做了一些优化，加入了midpoint位置。新读取的页，虽然是最新访问的页，但并不是直接放入到LRU列表的首部，而是放入到LRU列表的midpoint位置。Midpoint位置可由参数innodb_old_blocks_pct控制

![1581565703764](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581565703764.png)

![1581566099138](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581566099138.png)

![1581566110666](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581566110666.png)

#### 2、checkpoint技术

![1581566275347](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581566275347.png)

Checkpoint（检查点）技术的目的是解决以下几个问题：

缩短数据库的恢复时间；缓冲池不够用时，将脏页刷新到磁盘；重做日志不可用时，刷新脏页

对于InnoDB存储引擎而言，其是通过LSN（Log Sequence Number）来标记版本的。而LSN是8字节的的数字，其单位是字节。每个页有LSN，重做日志中也有LSN，checkpoint也有LSN。可以通过命令show engine innodb status来观察![](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/11-1581566643894.png)

#### 3、Master Thread工作方式

![1581730892130](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581730892130.png)

![1581731643217](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581731643217.png)

![1581735735356](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581735735356.png)![1581735820366](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581735820366.png)

![1581737952646](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581737952646.png)

对于backgroud loop，若当前没有用户活动（数据库空闲时）或者数据库关闭（shutdown），就会切换到这个循环。Background loop会执行以下操作：删除无用的undo页（总是）；合并20个插入缓冲（总是）；跳回到主循环（总是）；不断刷新100个页直到符合条件（可能，跳转到flush loop中完成）

若flush loop中也没有什么事情可以做了，innoDB存储引擎会切换到suspend_loop，将Master Thread挂起，等待事件的发生。若用户启用（enable）了InnoDB存储引擎，却没有使用任何InnoDB存储引擎的表，那么Master Thread总是处于挂起的状态。

![1581738211185](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581738211185.png)

![1581738242826](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581738242826.png)

#### 4、InnoDB关键特性

InnoDB存储引擎的关键特性包括：插入缓冲（insert buffer）；两次写（double write）；自适应哈希索引（adaptive hash index）；异步io（async io）；刷新邻接页（flush neighbor page）

![1581739573604](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581739573604.png)



在默认情况下，mysql数据库并不启动**慢查询日志**，用户需要手动将这个参数设为on：

```sql
Show Variables like "long_query_time"
show variables like "log_slow_queries"
```

是否使用索引

```sql
Show Variables like "log_queries_not_using_indexes"
```

当越来越多的sql查询被记录到了慢查询日志文件中，此时要分析该文件就显得不是那么简单和直观的了。这时可以使用mysqldumpslow命令

```sql
mysqldumpslow nh122-190-slow.log
```

如果用户希望得到执行时间最长的10条sql语句，可以执行如下命令

```sql
mysqldumpslow –s al –n 10 david.log
```

Msyql5.1开始可以将慢查询的日志记录放入一张表中，这使得用户的查询更加方便和直观，慢查询表在mysql架构下，名为slow_log，其表结构定义如下：

```sql
Show create table mysql.slow_log
show variables like "log_output"
set global log_output="table"
select from mysql.slow_log
```

### 表

在innoDB存储引擎中，表都是根据主键顺序组织存放的，这种存储方式的表称为索引组织表。在innoDB存储引擎表中，每张表都有个主键，如果在创建表时没有显示地定义主键，则innoDB存储引擎会按如下方式选择或创建主键：首先判断表中是否有非空的唯一索引，如果有，则该列即为主键。如果不符合上述条件，innoDB存储引擎自动创建一个6字节大小的指针。

![1581740073074](01_picture/mysql%E6%8A%80%E6%9C%AF%E5%86%85%E5%B9%95__%E5%AD%98%E5%82%A8%E5%BC%95%E6%93%8E%E9%98%85%E8%AF%BB%E8%AE%B0%E5%BD%95/1581740073074.png)