# Hadoop MapReduce 作业：按贷款等级汇总金额与收入

## 项目简介

这是 Eclipse Java 项目，项目名“李晶玉”，学号 2006110227。项目实现一个 Hadoop MapReduce 作业：读取逗号分隔的贷款数据，按贷款等级分组，累加每组内的贷款金额和年收入。

代码位于 `src/ljy` 包，只有一个作业，包含 Mapper、Reducer、Driver 和一个自定义 Writable 类型。

## 目录结构

```text
2006110227-李晶玉/
├── .classpath                      Eclipse 类路径，引用 D:/hadoop-3.2.3 下的 jar
├── .project                        Eclipse 项目描述，带 Hadoop 插件 Nature
├── src/ljy/
│   ├── driver.java                 作业入口，配置并提交 Job
│   ├── mapper.java                 解析每行数据，输出 <等级, Two>
│   ├── reducer.java                按等级累加贷款金额与年收入
│   └── Two.java                    实现 Writable，封装两个 long 字段
└── bin/ljy/*.class                 编译产物
```

## 主要内容

### Two.java

自定义 Hadoop 可序列化类型，实现 `Writable`。字段为 `loanAmnt`（贷款金额）和 `annualIncome`（年收入），提供 getter/setter、`readFields`、`write` 和 `toString`。`toString` 输出 `loanAmnt,annualIncome`，即最终结果的文本格式。

### mapper.java

继承 `Mapper<LongWritable, Text, Text, Two>`。处理逻辑：

1. 将一行文本按逗号 `split(",")`。
2. 要求字段数大于 9，否则丢弃该行。
3. 用正则 `[0-9]*\\.?[0-9]+` 校验第 2 列（下标 1）和第 11 列（下标 10）是否为数字。
4. 以第 6 列（下标 5）作为 key，即贷款等级 grade。
5. 以第 2 列贷款金额和第 11 列年收入构造 `Two` 作为 value。
6. 调用 `context.write(grade, va)` 输出。

代码中保留了 `System.out.println(k[10])` 调试输出，运行时会在容器日志中打印每一行的年收入。

### reducer.java

继承 `Reducer<Text, Two, Text, Two>`。对同一个等级 key 的所有 `Two` 值分别累加 `loanAmnt` 和 `annualIncome`，然后用累加结果构造新的 `Two` 输出。输出 key 为等级，value 为“贷款金额合计,年收入合计”。

### driver.java

作业入口类，`main` 方法完成以下配置：

- 创建 `Configuration`，设置 `dfs.replication=1`、`dfs.client.use.datanode.hostname=true`。
- 通过 `Job.getInstance(conf)` 创建作业。
- 设置 Jar 主类、Mapper 类、Reducer 类。
- 设置 Map 输出和最终输出的 key/value 类型均为 `Text` 和 `Two`。
- 输入路径固定为 `hdfs://8.130.13.235:9000/数据源testA.txt`。
- 输出路径为 `hdfs://8.130.13.235:9000/out_<时间>_<UUID>`，每次运行自动生成新目录。
- 调用 `job.waitForCompletion(true)` 提交并等待作业结束，按结果返回退出码。

## 编译与运行

1. 在 Eclipse 中导入本项目（Import Existing Projects into Workspace）。
2. 确认 `.classpath` 中的 Hadoop jar 路径在本机存在。原路径为 `D:/hadoop-3.2.3/share/hadoop/...`，路径不同需重新添加。
3. 项目中已包含 `bin/ljy/*.class`，源码修改后重新 Build Project。
4. 将项目导出为 Runnable JAR，主类选择 `ljy.driver`。
5. 在装有 Hadoop 客户端的机器上提交：

```bash
hadoop jar <导出的jar包> ljy.driver
```

也可以直接在 Eclipse 中运行 `driver.main`，前提是本机能访问配置中的 HDFS 地址。

## 依赖与注意事项

- 依赖 Hadoop 3.2.3。`.classpath` 逐条列出的 jar 均来自 `D:/hadoop-3.2.3/share/hadoop`，没有使用 Maven 或 Gradle。
- `.classpath` 使用未指定版本的 JRE 容器，未写死 JDK 版本。
- `.project` 中声明了 `org.apache.hadoop.eclipse.Nature`，需要安装 Hadoop Eclipse 插件才能识别该 Nature。
- 输入路径、HDFS 主机 `8.130.13.235:9000` 写死在 `driver.java` 中，换环境需修改。
- 字段含义按代码注释：下标 1 为 `loanAmnt`，下标 5 为等级，下标 10 为 `annualIncome`。
- 没有跳过表头的逻辑，表头行会因数字校验失败被过滤。
- 输出目录带时间戳和 UUID，多次运行不会因目录已存在而失败。
