import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

/**
 * @author Kunliang Wu
 * @version 2016-04-03 16:14:43
 * @aim This class encapsulate the result each task thread produces into a
 *      entity, which is more easy to be gathered to the master thread
 * 
 * 
 */

public class ResultEntity implements java.io.Serializable
{

	private static final long serialVersionUID = 7141130619150956026L;

	// the counter to record how many times each topic marked with hash-tags
	// show
	// up
	private HashMap<String, Long> topic_count = null;

	private HashMap<String, Long> at_user_count = null;

	private long target_phrase_count = -1;

	public ResultEntity(HashMap<String, Long> topic_count, HashMap<String, Long> at_user_count, long target_count)
	{
		this.topic_count = topic_count;
		this.at_user_count = at_user_count;
		this.target_phrase_count = target_count;
	}

	public void sumUpResult(ResultEntity plusResultEntity)
	{
		// sum the numbers up
		this.target_phrase_count += plusResultEntity.getTargetPhraseCount();

		// sum the entity hash maps up
		HashMap<String, Long> plus_topic_count_map = plusResultEntity.getTopicCountMap();
		this.topic_count = ResultEntity.addTo(this.topic_count, plus_topic_count_map);

		HashMap<String, Long> plus_user_count_map = plusResultEntity.getAsUserCountMap();
		this.at_user_count = ResultEntity.addTo(this.at_user_count, plus_user_count_map);

	}

	public static ResultEntity addTo(ResultEntity target, ResultEntity plus)
	{
		target.target_phrase_count += plus.target_phrase_count;
		target.at_user_count = ResultEntity.addTo(target.at_user_count, plus.at_user_count);
		target.topic_count = ResultEntity.addTo(target.topic_count, plus.topic_count);
		return target;

	}

	public static HashMap<String, Long> addTo(HashMap<String, Long> target, HashMap<String, Long> plus)
	{
		Object[] key_set = plus.keySet().toArray();
		String key;
		for (int i = 0; i < key_set.length; i++)
		{
			key = (String) key_set[i];
			if (target.containsKey(key))
				target.put(key, target.get(key) + plus.get(key));
			else
				target.put(key, plus.get(key));
		}
		return target;
	}

	public static List<String> sortAccordingToValue(HashMap<String, Long> table)
	{
		List<String> list = new ArrayList<String>(table.keySet());
		Collections.sort(list, new Comparator<Object>()
		{
			@Override
			public int compare(Object o1, Object o2)
			{
				long value=(Long) table.get(o2) - (Long) table.get(o1);
				
				if (value>0)
					return 1;
				else if (value==0)
					return 0;
				else
					return -1;
			}

		});
		return list;

	}

	public static void printResult(String phrase, ResultEntity result_entity)
	{
		// print out count
		System.out.println("The phrase " + phrase + " show up in the file for :\t\t" + result_entity.target_phrase_count
				+ " times.");
		List<String> order_rank = ResultEntity.sortAccordingToValue(result_entity.at_user_count);

		System.out.println("Top Users who have been mentioned most frequently:");
		for (int i = 0; i < 10; i++)
		{
			System.out.println(order_rank.get(i) + " :\t" + result_entity.at_user_count.get(order_rank.get(i)));
		}

		System.out.println("===========================");

		order_rank = ResultEntity.sortAccordingToValue(result_entity.topic_count);
		System.out.println("Top Topics who have been mentioned most frequently:");
		for (int i = 0; i < 10; i++)
		{
			System.out.println(order_rank.get(i) + " :\t" + result_entity.topic_count.get(order_rank.get(i)));
		}

	}

	public long getTargetPhraseCount()
	{
		return target_phrase_count;
	}

	public HashMap<String, Long> getTopicCountMap()
	{
		return this.topic_count;
	}

	public HashMap<String, Long> getAsUserCountMap()
	{
		return this.at_user_count;
	}

}
