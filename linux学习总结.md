### 一、Linux基本命令

shell命令的基本格式如下：command [选项] [参数]

* 切换用户  su  -username

* 进入shell的两种方式：1.打开终端；2.进入控制台，Ctrl+alt fn(1234567)，默认f1为桌面

* read命令用来读取用户输入的数据，并把读取到的数据赋值给一个变量，它通常的用法为：read str（str为变量名）

  如果我们只是想读取固定长度的字符串，那么可以给 read 命令增加-n选项。比如读取一个字符作为性别的标志，那么可以这样写：read -n 1 sex（1是-n选项的参数，sex是 read 命令的参数。-n选项表示读取固定长度的字符串，那么它后面必然要跟一个数字用来指明长度，否则选项是不完整的）。

* 为命令设置别名：alias lm="ls -al"

* 为变量设置值，并取出  myname=cs  echo $myname 则取出cs

  1.变量内若有空格符可使用双引号或单引号将变量内容结合起来，双引号内的特殊字符如$等，可以保有原本的特性，单引号内的特殊字符则仅为一般字符（纯文本）

  2.可用跳脱字符\将特殊符号（如[Enter],$,\，空格符,’等）变成一般字符。

  3.在一串指令的指向中，还需要藉由其他额外的指令所提供的信息时，可以使用反单引号`或$[]指令。其中`是数字键1左边那个按键。

  例如 version=$(uname –r) 再echo $version可得3.10.0-229……

  4.若该变量为扩增变量内容时，则可用”$变量名称”或“${变量}”累加内容

  PATH=”$PATH”:/home/bin 或PATH=${PATH}:/home/bin

  5.若该变量需要在其他子程序执行，则需要以export来使变量变成环境变量： export PATH

  6.取消变量的方法为使用unset,如：unset myname

* 查询指令是否为bash shell的内建命令：type（type [-tpa] name）示例：type ls

* 如果指令太长，可以利用[\\[Enter]]来将[Enter]这个按键“跳脱出来”，让[Enter]按键不再具有“开始执行”的功能。顺利跳脱[Enter]后，下一行最前面就会主动出现>的符号，就可以继续输入指令了。

* Ctrl+u/Ctrl+k 分别是从光标处向前删除指令串及向后删除指令串

  Ctrl+a/ctrl+e 分别是让光标移动到整个指令串的最前面或最后面。

* 变量键盘读取、数组与宣告：read,array,declare

  Read [-pt] variable

   read –p “please keyin your name: ” –t 30 named        echo ${named}

  declare或typeset是一样的功能，都是宣告变量的类型。

    declare [-aixr] variable





