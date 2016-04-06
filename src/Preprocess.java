
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Kunliang Wu
 * @version 2016-04-03 10:27:07
 * @note After a while I realize that Pre-process and the go-through of the
 *       entire the file before the task starts are inevitable. So the basic
 *       strategy is to examine the entire file and extract those parts which
 *       are useful for the further analysis.
 * 
 */

public class Preprocess
{
	// there is no need to worry about the multiple access to the tweet content,
	// since we only care about reading rather than writing
	private ArrayList<String> tweet_content_array = null;
	private static File tweet_input_file = null;

	private Scanner scanner = null;
	private String str_dummy = null;

	private FileWriter file_writer = null;

	public Preprocess(File file, int init_size)
	{

		this.tweet_input_file = file;

		try
		{
			this.scanner = new Scanner(tweet_input_file);
		} catch (FileNotFoundException e)
		{

			e.printStackTrace();
		}
		tweet_content_array = new ArrayList<String>(init_size);

	}

	public void readThrough()
	{
		//System.out.println("Begin read through");

		scanner.nextLine();
		//System.out.println("Ignoring first line");

		int count_line = 0;
		while (scanner.hasNextLine())
		{
			String nextline = this.readOneRawLine();
			if (nextline == null)
				continue;// this is a "headless" line, the tweet part of this
							// line has already been taken care of

			tweet_content_array.add(nextline);
			count_line++;
		}

		System.out.println("\tEnd of the whole file\t\t" +tweet_content_array.size() + "\tlines scanned");

	}

	private String readOneRawLine()
	{
		str_dummy = scanner.nextLine();

		// only get the content we care about:
		// the "text" part in the json structure

		int start_index = str_dummy.indexOf("text\"\":\"\"") + 9;

		// to improve the efficiency we can shorten the target end anchor str
		int end_index = str_dummy.indexOf("\"\",\"\"in_reply_to_status_id");
		// System.out.println("raw reading\t"+str_dummy);

		// if (start_index <= 0 || end_index <= 0)
		// {
		//
		// System.out.println("Wrong index anchor:");
		// System.out.println("start:" + start_index);
		// System.out.println("end:" + end_index);
		// System.out.println("line length:" + str_dummy.length());
		// System.out.println("The line is:\n" + str_dummy);
		// }

		// error control:
		// sometimes the tweet content includes a '\n'
		// this means that the tweet ends with end_index as -1
		// and the following raw_line is a "headless" line, with end_index
		// equals 0
		// which shall be abandoned
		if (-1 == end_index)
			return " " + str_dummy.substring(start_index) + " ";
		if (0 == end_index || start_index > end_index)
			return null;
		else
			return " " + str_dummy.substring(start_index, end_index) + " ";

	}

	public String[] getTheTweetListAsArray()
	{
		String[] result = new String[tweet_content_array.size()];
		tweet_content_array.toArray(result);
		return result;

	}

	public void printTestOutput(String file_path)
	{
		try
		{
			file_writer = new FileWriter(new File(file_path));

			if (tweet_content_array.isEmpty())
				System.out.println("Empty array. Nothing to be print.");

			else
			{
				for (int i = 0; i < tweet_content_array.size(); i++)
				{
					file_writer.write(tweet_content_array.get(i) + "\n");
				}

			}
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("\tTest output file finished!!");

	}

}
