

import java.io.File;
import java.nio.MappedByteBuffer;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Kunliang Wu
 * @version 2016-04-02 11:11:44
 * @aim This is the actual implements of the statics job, they may keep there
 *      own statics, yet they still have to report the final result to their
 *      master
 */
public class TaskWorker {
	// the path for the resource is the same for every task
	protected static File TWEET_INPUT_FILE = null;
	
	//shared memory
	protected static MappedByteBuffer mappedByteBuffer = null;

	// input source and its stream for tweets, every thread has an individual
	// scanner
	protected Scanner scanner = null;
	

	// 可能需要保留运行时的各个参数，包括自己的进程号码
	// 以及整体并行环境的一些参数
	private int rank=-1;
	private int size=-1;
	//*******************

	// target word to be searched for
	private String target_str = null;
	private long target_count = -1;

	// The string to be processed during the scan-through
	private StringBuffer tweet_raw = null;

	// the counter to record how many times each topic marked with hashtags show
	// up
	private Map<String, Long> topic_count = null;

	private Map<String, Long> at_user_count = null;

	
	//pre-project the file into the memory before the operation begins
	public static void setMappedByteBuffer(MappedByteBuffer mappedByteBuffer)
	{
		TaskWorker.mappedByteBuffer = mappedByteBuffer;
	}

	
	public TaskWorker(int size ,int rank) {
		// 根据外界初始参数来判断从何处读取
		this.size=size;
		this.rank=rank;
		
		
		
	}

	public void initTask() {
		// 初始化任务的输入流，建立一个套接字的Scanner

		//

	}

//	private boolean scanOneTweet(String target_str) {
//
//		// 读取并处理一份tweet的内容
//		// 其实质是截取一段文本，添加前置空格和后置空格,
//
//		// 1.判断是否有目标word出现，如果有，则本计数器加一
//		// 2.将本条tweet中的出现的topic的对应的计数器加一，如果该topic并没有出现在map中，则添加新条目
//		// 3.对@的user做同样的处理
//
//	}
//
//	
//	public void protype()
//	{
//		int sizeofrecordinbytes = 290;
//		 // for this example this is 1 based, not zero based 
//		int recordIWantToStartAt = 12400;
//		int totalRecordsIWant = 1000;
//		 
//		File myfile = new File("someGiantFile.txt");
//		 
//		 
//		// where to seek to
//		long seekToByte =  (recordIWantToStartAt == 1 ? 0 : ((recordIWantToStartAt-1) * sizeofrecordinbytes));
//		 
//		// byte the reader will jump to once we know where to go
//		long startAtByte = 0;
//		 
//		// seek to that position using a RandomAccessFile
//		try {
//		        // NOTE since we are using fixed length records, you could actually skip this 
//		        // and just use our seekToByte as the value for the BufferedReader.skip() call below
//		 
//		    RandomAccessFile rand = new RandomAccessFile(myfile,"r");
//		    rand.seek(seekToByte);
//		    startAtByte = rand.getFilePointer();
//		    rand.close();
//		     
//		} catch(IOException e) {
//		    // do something
//		}
//		 
//		// Do it using the BufferedReader 
//		BufferedReader reader = null;
//		try {
//		    // lets fire up a buffered reader and skip right to that spot.
//		    reader = new BufferedReader(new FileReader(myfile));
//		    reader.skip(startAtByte);
//		     
//		    String line;
//		    long totalRead = 0;
//		    char[] buffer = new char[sizeofrecordinbytes];
//		    while(totalRead < totalRecordsIWant && (-1 != reader.read(buffer, 0, sizeofrecordinbytes))) {
//		        System.out.println(new String(buffer));
//		        totalRead++;
//		    }
//		} catch(Exception e) {
//		    // handle this
//		     
//		} finally {
//		    if (reader != null) {
//		        try {reader.close();} catch(Exception ignore) {}
//		    }
//		}
//	}
}
