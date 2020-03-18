#### 前言

相关资源链接：官网—— http://hadoop.apache.org/    源代码—— https://github.com/tomwhite/hadoop-book      共享数据集——http://aws.amazon.com/public-data-sets 、 http://infochimps.org/ 

### 1、安装

1. 首先安装jdk。通过winscp将安装包上传到服务器，并解压到/usr/local目录

   ```shell
   cd /usr/local
   tar -zxvf /tmp/jdk-8u221-linux-x64.tar.gz
   ```

   通过修改/etc/profile文件配置变量（永久有效）

   ```shell
   #vi /etc/profile
   export JAVA_HOME=/usr/local/jdk1.8.0_221
   export PATH=$JAVA_HOME/bin:$PATH
   export CLASSPATH=.:$JAVA_HOME/lib/dt.jar:$JAVA_HOME/lib
   #使更改生效
   #source /etc/profile
   #java -version验证安装成功
   ```

2. 将hadoop安装包上传到服务器，并解压到/usr/local目录

   ```shell
   cd /usr/local
   tar -zxvf /tmp/hadoop-2.10.0.tar.gz
   ```

   修改变量

   ```shell
   export HADOOP_HOME=/usr/local/hadoop-2.10.0
   export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
   #sbin目录下有运行hadoop守护进程的脚本，因此如果计划在本地机器上运行守护进程的话，需要将该目录包含进命令行路径中
   #通过hadoop version验证是否安装成功
   ```

   ![捕获](01_picture/Hadoop%E6%9D%83%E5%A8%81%E6%8C%87%E5%8D%97%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0/%E6%8D%95%E8%8E%B7.PNG)

3. 配置——hadoop的各个组件均可利用xml文件进行配置。core-site.xml文件用于配置通用属性，hdfs-site.xml文件用于配置HDFS属性，mapred-site.xml文件则用于配置MapReduce属性，yarn-site.xml文件用于配置YARN属性。这些配置文件都放在etc/hadoop子目录中。

   hadoop有以下三种运行模式

   * 独立（或本地）模式：无需运行任何守护进程，所有程序都在同一个JVM上执行。在独立模式下测试和调试MapReduce程序很方便，因此该模式在开发阶段比较合适。由于默认属性专为本模式设定，且本模式无需运行任何守护进程，因此独立模式下不需要更多操作。
   * 伪分布模式：hadoop守护进程运行在本地机器上，模拟一个小规模的集群
   * 全分布模式：hadoop守护进程运行在一个集群上。

   在特定模式下运行hadoop需要关注两个因素：正确设置属性和启动hadoop守护进程

具体参考附录A







十小时入门大数据笔记

HDFS架构

![](01_picture/Hadoop%E6%9D%83%E5%A8%81%E6%8C%87%E5%8D%97%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0/hdfs%E6%9E%B6%E6%9E%84%E5%9B%BE.PNG)

副本机制

![](01_picture/Hadoop%E6%9D%83%E5%A8%81%E6%8C%87%E5%8D%97%E5%AD%A6%E4%B9%A0%E7%AC%94%E8%AE%B0/hdfsdatanodes.png)



伪分布式安装步骤

1. jdk安装

   ```shell
   #解压
   tar -zxvf jdk-linux-x64.tar.gz -C ~/app
   #添加到系统环境变量：~/.bash_profile
   export JAVA_HOME=/home/hadoop/app/jdk1.7.0_79
   export PATH=$JAVA_HOME/bin:$PATH
   #使得环境变量生效
   source ~/.bash_profile
   #验证java是否配置成功
   java -version
   ```

2. 安装ssh

   ```shell
   sudo yum install ssh
   ssh-keygen -t rsa
   cp ~/.ssh/id_rsa.pub ~/.ssh/authorized_keys
   ```

3. 下载并解压hadoop

   下载：直接去cdh网站下载

   解压：tar -zxvf hadoop-2.6.0-cdh5.7.0.tar.gz -C ~/app

4. hadoop配置文件的修改（hadoop_home/etc/hadoop)

   hadoop-evn.sh

   ​      export JAVA_HOME=/home/hadoop/app/jdk1.7.0_79

    etc/hadoop/core-site.xml:

   ```xml
   <configuration>
       <property>
           <name>fs.defaultFS</name>
           <value>hdfs://VM_0_16_centos:8020</value>
       </property>
       <property>
       	<name>hadoop.tmp.dir</name>
           <value>/root/hadoop/tmp</value>
       </property>
   </configuration>
   ```

    etc/hadoop/hdfs-site.xml: 

   ```xml
   <configuration>
       <property>
           <name>dfs.replication</name>
           <value>1</value>
       </property>
   </configuration>
   ```

5. 启动hdfs

   ```shell
   #格式化文件系统（仅第一次执行即可，不要重复执行）
   hdfs namenode -format
   #启动hdfs
   sbin/start-dfs.sh
   #验证是否启动成功
   jps
   # DataNode  SecondaryNameNode  NameNode
   ```

   

