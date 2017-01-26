package sentiment.raw;

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

public class SentimentMapperRaw extends
		Mapper<LongWritable, Text, Text, IntWritable> {

	// dictionary filenames
	private String[] filenames = { "negativeEmotes", "negativeExclaim",
			"negativeStopwords", "negativeVerbs", "neutralEmotes",
			"neutralExclaim", "neutralStopwords", "neutralVerbs",
			"positiveEmotes", "positiveExclaim", "positiveStopwords",
			"positiveVerbs" };
	public static String[] animals = {"dog", "puppy", "cat", "kitten", "rabbit", "bunny", "fish"};


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
//		String parts[] = value.toString().split("\",\"source");
//		String content = parts[0].substring(109);

		if (content != null) {
			content = content.toLowerCase();
			content = content.replace("#", "");
			content = content.replace("'", "");

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
					context.write(new Text(t.token), new IntWritable(
							lineSentiment));
				}
			}
		}
	}
}