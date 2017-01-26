package sentiment.raw;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class SentimentJobRaw {
	public static void main(String[] args) throws IOException,
			ClassNotFoundException, InterruptedException {

		Job job = Job.getInstance(new Configuration()); // create new job
		job.setJarByClass(SentimentReducerRaw.class);
		job.setJobName("Sentiment Analysis"); // declare the new job's name
		FileInputFormat.addInputPath(job, new Path(args[0])); // input filename
		FileOutputFormat.setOutputPath(job, new Path(args[1])); // output_filename
		job.setMapperClass(SentimentMapperRaw.class); // set the mapper
		job.setCombinerClass(SentimentReducerRaw.class); // set the combiner
		job.setReducerClass(SentimentReducerRaw.class); // set the reducer
		job.setOutputKeyClass(Text.class); // keys = Text
		job.setOutputValueClass(IntWritable.class); // values = ints
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
}