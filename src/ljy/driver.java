package ljy;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class driver{
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {
		// 1.创建job任务
		Configuration conf = new Configuration();
		conf.set("dfs.replication", "1");
		conf.set("dfs.client.use.datanode.hostname", "true");
		// 设置的块大小 hadoop2.x->128M hadoop1.x -> 64M
//		conf.set("dfs.blocksize", "256m");
		Job job = Job.getInstance(conf);

		// 2.指定jar包位置
		job.setJarByClass(driver.class);

		// 3.关联使用的mapper类
		job.setMapperClass(mapper.class);

		// 4.关联使用的reducer类
		job.setReducerClass(reducer.class);

		// 5.设置mapper阶段输出的数据类型
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Two.class);

		// 6.设置reducer阶段输出的数据类型
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Two.class);

		// 7.设置数据输入的路径 读取文件
		FileInputFormat.setInputPaths(job, new Path("hdfs://8.130.13.235:9000/数据源testA.txt"));

		// 8.设置数据输出的路径 输出文件
		String uuid = UUID.randomUUID().toString().replaceAll("-", "");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
		String time = sdf2.format(new Date());
		String outPutPath = "hdfs://8.130.13.235:9000/out_" + time + "_"+ uuid; 
		FileOutputFormat.setOutputPath(job, new Path(outPutPath));

		// 9.提交任务
		boolean rs = job.waitForCompletion(true);
		System.exit(rs ? 0 : 1);
	}

}
