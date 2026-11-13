package ljy;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class reducer extends Reducer<Text, Two, Text, Two> {

	@Override
	protected void reduce(Text key, Iterable<Two> values, Context context) throws IOException, InterruptedException {
		// 累加器
		long total1 = 0;
		long total2 = 0;

		// 遍历 进行累加
		for (Two value : values) {
			total1 += value.getLoanAmnt();
			total2 += value.getAnnualIncome();
//			total1 += value.getCases();
//			total2 += value.getDeaths();
		}

		Two val = new Two();

		val.set(total1, total2);

		// 输出结果
		context.write(key, val);
	}
}
