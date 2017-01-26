package sentiment.filtered;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;
import cmu.arktweetnlp.io.JsonTweetReader;

public class SentimentMapperFiltered extends
		Mapper<LongWritable, Text, Text, IntWritable> {

	// dictionary filenames
	private String[] filenames = { "negativeEmotes", "negativeExclaim",
			"negativeStopwords", "negativeVerbs", "neutralEmotes",
			"neutralExclaim", "neutralStopwords", "neutralVerbs",
			"positiveEmotes", "positiveExclaim", "positiveStopwords",
			"positiveVerbs" };
	public static String[] animalArray = { "dog", "puppy", "cat", "kitten",
			"rabbit", "bunny", "fish" };

	public static String dog_count = "dog_count";
	public static String puppy_count = "puppy_count";
	public static String cat_count = "cat_count";
	public static String kitten_count = "kitten_count";
	public static String rabbit_count = "rabbit_count";
	public static String bunny_count = "bunny_count";
	public static String fish_count = "fish_count";

	// various vectors for dictionaries and filepaths
	public static Vector<String> filepaths = new Vector<String>();
	public static Vector<String> negative = new Vector<String>();
	public static Vector<String> neutral = new Vector<String>();
	public static Vector<String> positive = new Vector<String>();

	// new tagger object
	public static Tagger tagger = new Tagger();
	public static JsonTweetReader jsonread = new JsonTweetReader();

	// job configs and setup
	// fill dictionaries here
	@Override
	protected void setup(Context context) throws IOException,
			InterruptedException {
		super.setup(context);

		tagger.loadModel("/s/bach/j/under/barry/cs455_workspace/TwitterSentimentAnalysis_2.0/model.20120919");

		for (String file : filenames) {
			String temp = "/s/bach/a/class/cs455/asg3/dictionary/" + file;
			filepaths.add(temp);
		}

		for (String path : filepaths) {
			File dict = new File(path);
			Scanner input = new Scanner(dict);

			if (path.contains("negative")) {
				while (input.hasNextLine()) {
					String nextToken = input.nextLine();
					nextToken.toLowerCase();
					negative.add(nextToken);
				}
			} else if (path.contains("positive")) {
				while (input.hasNextLine()) {
					String nextToken = input.nextLine();
					nextToken.toLowerCase();
					positive.add(nextToken);
				}
			} else if (path.contains("neutral")) {
				while (input.hasNextLine()) {
					String nextToken = input.nextLine();
					nextToken.toLowerCase();
					neutral.add(nextToken);
				}
			}
			input.close();
		}
	}

	// **********************
	// ******* MAPPER *******
	// **********************

	@Override
	public void map(LongWritable key, Text value, Context context)
			throws IOException, InterruptedException {

		List<TaggedToken> lineTagged;
		int lineSentiment = 0;
		String content = jsonread.getText(value.toString());
		// String parts[] = value.toString().split("\",\"source");
		// String content = parts[0].substring(109);

		if (content != null) {
			content = content.toLowerCase();
			content = content.replace("#", "");
			content = content.replace("$", "");
			content = content.replace("&", "");
			content = content.replace("$", "");
			content = content.replace("+", "");
			content = content.replace("/", "");
			content = content.replace("?", "");
			content = content.replace("@", "");
			content = content.replace("1", "");
			content = content.replace("2", "");
			content = content.replace("4", "");
			content = content.replace("5", "");
			content = content.replace("6", "");
			content = content.replace("7", "");
			content = content.replace("8", "");
			content = content.replace("9", "");

			// pass content to tagger
			lineTagged = tagger.tokenizeAndTag(content);

			// calculate sentiment
			for (TaggedToken t : lineTagged) {
				if (!t.tag.equals("N")) {
					for (String negSent : negative) {
						if (t.token.equals(negSent)) {
							lineSentiment -= 1;
						}
					}
					for (String posSent : positive) {
						if (t.token.equals(posSent)) {
							lineSentiment += 1;
						}
					}
				}
			}

			// write all nouns to reducer
			for (TaggedToken t : lineTagged) {
				if (t.tag.equals("N")) {
					for (String s : animalArray) {
						if (t.token.equals(s)) {
							String count = t.token + "_count";
							context.write(new Text(t.token), new IntWritable(
									lineSentiment));
							context.write(new Text(count), new IntWritable(1));
						}
					}
				}
			}
		}
	}
}