# Hadoop 贷款数据 MapReduce 计算

## 任务描述

分析贷款人的贷款金额和收入情况，可以更好地预测贷款人是否有能力还款。伴随大数据技术的成熟与普及，通过大数据技术可以对贷款人的各个贷款指标展开计算分析。

计算任务主要实现以下功能：

1. **收集数据**：在某信贷平台上采集贷款人相关贷款信息数据，如 id、贷款金额、贷款利率、总收入、贷款时间等。
2. **存储数据**：通过天池大赛"零基础入门金融风控-贷款违约预测"赛题中提供的数据源，下载测试集 A 进行存储。
3. **读取数据**：将下载的测试集 A 放到 Excel 中进行筛选等处理，再利用爬虫技术将 Excel 转换为 txt 格式，使用 Eclipse 进行开发并将数据上传到 HDFS。
4. **计算数据**：分别对贷款等级 grade 所对应贷款人的贷款金额 loanAmnt 和贷款人年收入 annualIncome 的累加。

## 开发环境

- 操作系统：阿里云 Linux 服务器
- 开发工具：Eclipse（安装 Hadoop 插件）
- Hadoop 版本：Hadoop 3.2.3
- 部署方式：HDFS 命令 `hdfs dfs -put` 上传数据文件，YARN 运行 MapReduce 作业
- 依赖 jar：Eclipse 通过 `.classpath` 引用 `D:/hadoop-3.2.3` 下的库

## 数据文件

### 数据来源

天池大赛"零基础入门金融风控-贷款违约预测"赛题提供的数据集，下载测试集 A 后存储到 HDFS。原始文件包含逗号分隔的贷款记录。

### 数据特征

数据源包含 4 类特征，本文使用其中的贷款等级、贷款金额和年收入字段。

| 类别 | 主要特征 |
| --- | --- |
| 个人统计文件 | 贷款名、个人 ID、贷款金额、注册信息（手机、邮箱、渠道、方式、时间） |
| 社会属性文件 | 家庭类型、家庭人数、公司（就业年限、职称、职位、年收入）、终端设备 |
| 用户消费文件 | 消费属性、价值属性（价值指数、流失指数、忠诚指数）、旧客标签、新客标签 |
| 用户行为文件 | 活跃属性、行为属性、偏好属性（价格偏好、类目偏好、特征偏好、下单时间偏好）、风险 |

### 本文使用的字段

- **grade**：贷款等级（作为 MapReduce 的 key）
- **loanAmnt**：贷款金额（累加值，作为 value 的一部分）
- **annualIncome**：贷款人年收入（累加值，作为 value 的另一部分）

## 数据上传流程

1. 从天池大赛下载测试集 A。
2. 在 Excel 中进行筛选和清洗。
3. 利用爬虫技术将 Excel 转换为 txt 格式。
4. 使用 HDFS 命令将数据上传到 Hadoop 平台：

```bash
hdfs dfs -put 本地文件.txt /HDFS/目标路径/
```

5. Eclipse 配置 Hadoop 连接后，可以从 DFS 浏览和上传文件。

## MapReduce 程序设计

### 自定义 Writable：Two.java

MapReduce 的 value 需要同时存放贷款金额和年收入两个字段。为此自定义 `Two` 类，实现 Hadoop 的 `Writable` 接口，封装两个 `long` 类型字段：

```java
// 核心成员
public class Two implements Writable {
    private long loanAmnt;
    private long annualIncome;

    public long getLoanAmnt() { return loanAmnt; }
    public void setLoanAmnt(long v) { this.loanAmnt = v; }

    public long getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(long v) { this.annualIncome = v; }

    // 序列化/反序列化/toString 方法
}
```

Reduer 累加时，每次把新的贷款金额和年收入加进去，输出的 `toString()` 格式为 `loanAmnt,annualIncome`。

### Mapper：mapper.java

继承 `Mapper<LongWritable, Text, Text, Two>`。处理流程：

1. 将每行文本按逗号 `split(",")`。
2. 判断数组长度是否大于所选区域，防止越界。
3. 判断第 2 列（下标 1）和第 11 列（下标 10）是否为数字（跳过表头和脏数据）。
4. 取第 6 列（下标 5）作为 key，即贷款等级 grade。
5. 取贷款金额和年收入构造 `Two` 作为 value。
6. 输出 `<grade, Two>`。

### Reducer：reducer.java

继承 `Reducer<Text, Two, Text, Two>`。对同一 key（贷款等级）下的所有 value 做累加：

- `totalLoanAmnt += value.loanAmnt`
- `totalAnnualIncome += value.annualIncome`

输出 `<grade, 累加后的 Two>`。

### Driver：driver.java

作业入口。配置 MapReduce 作业的输入路径、输出路径、Mapper/Reducer 类和序列化类型，然后提交到 YARN。

## 运行步骤

1. **上传数据到 HDFS**

```bash
hdfs dfs -put 本地数据.txt /user/hadoop/data/loan.txt
```

2. **编译打包**

在 Eclipse 中导出 jar，例如 `LoanAnalysis.jar`。

3. **提交作业**

```bash
hadoop jar LoanAnalysis.jar ljy.driver /user/hadoop/data/loan.txt /user/hadoop/out/
```

4. **查看输出**

```bash
hdfs dfs -cat /user/hadoop/out/part-r-00000
```

输出格式为每行 `贷款等级,贷款金额累加值,年收入累加值`，例如：

```
A,12345678,98765432
B,15423678,112345678
C,23456789,108765432
...
```

## 计算结果

从输出结果可以观察到：

- **C 等级**贷款金额最多。
- **E、F 等级**贷款总金额相对较少。
- **B 等级**贷款人的年收入总数最高。
- **F 等级**贷款人的年收入总数最低。

## 程序测试

### 1. 文本分隔测试

将每一行按逗号拆分，打印分隔后的数组内容，验证字段位置正确。

### 2. 数组存储测试

将分隔后的各字段存入数组，确认数组下标与字段对应关系准确。

### 3. 贷款等级分类测试

从数组中取出 grade 字段（下标 5），打印各等级值，验证分类逻辑正确。

### 4. 贷款金额和年收入累加测试

从数组中取出 loanAmnt（下标 1）和 annualIncome（下标 10），手动累加并打印，与 Reducer 输出对照。

## 遇到的问题与解决方法

### 问题一：数据源第一行是文本，不可计算

**现象**：数据源表头行不是数字，直接解析会报错。

**解决方案**：封装判断是否为数字的函数，在选择数组中的数据位置时加上 `if` 判断。

### 问题二：数据源行长度不一致

**现象**：不能保证每行字段数都大于所选区域，数组下标越界。

**解决方案**：在最外层加上 `if`，判断数组长度是否大于所选区域，符合条件才可执行累加功能。

### 问题三：数据文件过大，Eclipse 无法打开

**现象**：原始数据量太大，在 Eclipse 中直接浏览困难。

**解决方案**：筛选出一部分数据再上传到 HDFS 运行。

### 问题四：作业执行后没有输出文件

**现象**：运行完处理过程中未成功生成处理后的数据源文件。

**解决方案**：设置断点排查，最终在自定义类 `Two` 中发现少写了序列化方法，补上后可以正常生成文件。

### 问题五：jps 中缺少 namenode 或 datanode

**现象**：多次格式化 HDFS 后，服务启动不完整，jps 中缺少关键进程。

**解决方案**：手动检查配置文件中参数，确认所有节点配置一致，修改后重新启动。

### 问题六：Eclipse 的 DFS 浏览无法上传文件

**现象**：从 Eclipse 连接 HDFS 后，上传按钮报错。

**解决方案**：在阿里云服务器的安全组中手动添加 1/9999 的端口范围（覆盖 Hadoop 常用端口），刷新后上传即可。

2021年12月
