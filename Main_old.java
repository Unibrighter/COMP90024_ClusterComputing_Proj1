import java.io.File;
import java.util.HashMap;

import mpi.MPI;

public class Main
{
	private static final String OUTPUT_PATH = "./out_test.txt";

	// private static final String INPUT_PATH = "./twitter.csv";
	private static final String INPUT_PATH = "./twitter.csv";

	public static void main(String[] args)
	{

		String target_phrase = "money";

		// read through the whole file to do the pre-process
		// the file path is in the home directory

		File file = new File(INPUT_PATH);
		System.out.println(INPUT_PATH);

		Preprocess pre_processor = new Preprocess(file, 1500);

		pre_processor.readThrough();

		// manager.printTestOutput(OUTPUT_PATH);

		long stamp_before_preprocess = System.currentTimeMillis();

		// now we get the string array we care about ,now we can use
		String[] str_array = pre_processor.getTheTweetListAsArray();

		long stamp_after_preprocess = System.currentTimeMillis();
		System.out.println("\n>>>>After preprocessing, system time: " + stamp_after_preprocess);

		// ------------------------------------------------------------------------
		// using default args
		MPI.Init(args);

		int rank = MPI.COMM_WORLD.Rank();
		int size = MPI.COMM_WORLD.Size();
		int master_rank = 0;
		final int START_OFFSET = 0;

		int total_tweet_count = str_array.length;
		int work_load_per_task = total_tweet_count / size;

		// the data which each task operates on
		String[] string_array_per_task = new String[work_load_per_task];

		ResultEntity[] result_per_task = new ResultEntity[1];

		// start the statics information gathering,and get the result entity
		MPI.COMM_WORLD.Scatter(str_array, START_OFFSET, work_load_per_task, MPI.OBJECT, string_array_per_task,
				START_OFFSET, work_load_per_task, MPI.OBJECT, master_rank);

		// ===============
		// Each task does its own job according to the partition, which is
		// marked by its rank

		// *********

		// result_per_task[0] = Main.analyseText(target_phrase, rank, size,
		// work_load_per_task, string_array_per_task);


		HashMap<String, Long> topic_count = new HashMap<String, Long>();

		HashMap<String, Long> at_user_count = new HashMap<String, Long>();

		long target_count = 0;
		long time_start = System.currentTimeMillis();
		System.out.println("\n>>>>As No.\t" + rank + "\tBefore calculating the result, system time: " + System.currentTimeMillis());
		for (int i = 0; i < string_array_per_task.length; i++)
		{
			String[] word_list_per_line = string_array_per_task[i].split("[^_#@a-zA-Z0-9]+");

			for (int j = 0; j < word_list_per_line.length; j++)
			{
				String dummy = word_list_per_line[j];
				if (dummy.length() == 0 || (dummy.length() == 1 && (dummy.charAt(0) == '#' || dummy.charAt(0) == '@')))
					continue;
				if (0 == target_phrase.compareToIgnoreCase(dummy))
				{
					target_count++;
				}

				if ('@' == dummy.charAt(0))
				{
					if (at_user_count.containsKey(dummy))
					{// already exist in the table
						at_user_count.put(dummy, at_user_count.get(dummy) + 1);
					} else
					{
						at_user_count.put(dummy, (long) 1);
					}

				}

				if ('#' == dummy.charAt(0))
				{
					if (topic_count.containsKey(dummy))
					{// already exist in the table
						topic_count.put(dummy, topic_count.get(dummy) + 1);
					} else
					{
						topic_count.put(dummy, (long) 1);
					}

				}

			}

		}

		long time_end = System.currentTimeMillis();
		System.out.println("No." + rank + " task finished ,pure calculation using time：\t" + (time_end - time_start));
		System.out
				.println("\n***As No.\t" + rank + "\t, my work load is \t" + string_array_per_task.length + "\t lines");

		System.out.println("------------------------------");
		System.out.println(target_phrase + " : " + target_count + " times");
		System.out.println("Topics discovered : " + topic_count.size() + " times");
		System.out.println("Users discovered : " + at_user_count.size() + " times");
		System.out.println("==============================");

		result_per_task[0] = new ResultEntity(topic_count, at_user_count, target_count);

		// *********

		// using the send / recv signal to gather the final results

		final int MESSAGE_TAG_BASE = 100;

		System.out.println("\n>>>>As No.\t" + rank + "\tBefore sending the message, system time: " + System.currentTimeMillis());

		if (0 != rank)
		{
			MPI.COMM_WORLD.Isend(result_per_task, START_OFFSET, 1, MPI.OBJECT, master_rank, rank + MESSAGE_TAG_BASE);
		}
		System.out.println("\n>>>>As No.\t" + rank + "\tAfter sending the message, system time: " + System.currentTimeMillis());
		long stamp_after_individual_finish = System.currentTimeMillis();

		// Thread 0 accept message from other thread
		if (rank == 0)
		{
			ResultEntity final_result = result_per_task[0];// for thread 0 ,
															// which is the
															// master thread
			ResultEntity[] rec = new ResultEntity[1];
			rec[0] = null;

			System.out.println("Begins to sum the results in thread rank 0!!!");
			System.out.println("\n>>>>Before receiving the message, system time: " + System.currentTimeMillis());
			for (int i = 1; i < size; i++)
			{

				// receive the total number of term input
				MPI.COMM_WORLD.Recv(rec, 0, 1, MPI.OBJECT, i, i + MESSAGE_TAG_BASE);

				final_result = ResultEntity.addTo(final_result, rec[0]);

			}
			long stamp_after_sum_up = System.currentTimeMillis();
			System.out.println("\n>>>>After receiving the message, system time: " + stamp_after_sum_up);
			System.out.println("\n>>>>As No.\t" + rank + "\tAfter sum up, system time: " + System.currentTimeMillis());
			ResultEntity.printResult(target_phrase, final_result);
			System.out.println("*******Time Information********");

			System.out
					.println("Preprocess:\t\t" + (stamp_after_preprocess - stamp_before_preprocess) + "\tMilli secs.");
			System.out.println(
					"Multiple Tasks including Arg init , Scatter & Sending:\t\t" + (stamp_after_individual_finish - stamp_after_preprocess) + "\tMilli secs.");
			
			System.out.println("Totol time:\t\t" + (stamp_after_sum_up - stamp_before_preprocess) + "\tMilli secs.");

			

			System.out.println("*******Time Information********");
		}

		// ===============

		MPI.Finalize();

		// ------------------------------------------------------------------------

	}

}
