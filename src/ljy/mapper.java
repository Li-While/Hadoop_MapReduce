package ljy;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import org.apache.hadoop.mapreduce.Mapper.Context;


public class mapper extends Mapper<LongWritable, Text, Text, Two> {
	
	public boolean isDigit(String str) {
		Pattern pattern = Pattern.compile("[0-9]*\\.?[0-9]+");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
	
	@Override
	protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

		Text grade = new Text();
		// loanAmnt,annualIncome
		Two va = new Two();

		// 按行读取数据，逗号分隔值
		String line = value.toString();
		String[] k = line.split(",");
//		System.out.println(line);
//		System.out.println(k.length);
		if (k.length > 9) {
//			System.out.println(k[5] + "：" + k[1] + "、" + k[10]);
//			System.out.println(Arrays.toString(k));
			if (isDigit(k[1]) && isDigit(k[10])) {
//				System.out.println(k[5]);
//				System.out.println(k[1]);
				System.out.println(k[10]);
//				System.out.println(k[1] + "、" + k[10]);
				// 选择字段 grade等级5 loanAmnt贷款金额1 annualIncome总收入10
				grade.set(k[5]);
				va.set(Double.valueOf(k[1]).longValue(), Double.valueOf(k[10]).longValue());

				// 输出 A 1000 3000
				context.write(grade, va);
			}
		}

	}

}
