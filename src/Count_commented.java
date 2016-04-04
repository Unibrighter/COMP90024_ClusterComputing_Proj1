import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import mpi.*;

public class Count
{

	public static void main(String args[]) throws IOException, ClassNotFoundException
	{
		// Get the total length of the file
		File f = new File("miniTwitter_5744.csv");
		long len = f.length();

		// Get the number of line in the file
		LineNumberReader lr = new LineNumberReader(new FileReader(f));
		lr.skip(len);
		int line = lr.getLineNumber();

		MPI.Init(args);
		double t_start = MPI.Wtime();

		// key: term with @ and #, value: number of term appeared
		Hashtable user = new Hashtable();
		Hashtable topic = new Hashtable();

		int size = MPI.COMM_WORLD.Size(); // get the number of thread
		int rank = MPI.COMM_WORLD.Rank(); // get the thread id

		long seg = len / size + 1; // segment the file

		// thead 0 print the total number of thread available
		if (rank == 0)
		{
			System.out.println("Number of threads: " + size);

		}
		MPI.COMM_WORLD.Barrier();

		// this part will be execute by all thread
		int count = getCount(args[3], seg, rank, f, size, user, topic, line);

		int[] msg = new int[1];
		msg[0] = count;

		// transform the hashtable user into byte array
		byte[] uBytes = null;
		ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
		ObjectOutputStream out1 = new ObjectOutputStream(bos1);
		out1.writeObject(user);
		out1.flush();
		uBytes = bos1.toByteArray();

		out1.close();
		bos1.close();

		// transform the hashtable topic into byte array
		byte[] tBytes = null;
		ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
		ObjectOutputStream out2 = new ObjectOutputStream(bos2);
		out2.writeObject(topic);
		out2.flush();
		tBytes = bos2.toByteArray();

		out2.close();
		bos2.close();

		// All threads except thread 0 send message to thread 0
		if (rank != 0)
		{
			MPI.COMM_WORLD.Isend(msg, 0, 1, MPI.INT, 0, 99);
			MPI.COMM_WORLD.Isend(uBytes, 0, uBytes.length, MPI.BYTE, 0, 98);
			MPI.COMM_WORLD.Isend(tBytes, 0, tBytes.length, MPI.BYTE, 0, 97);
		}

		MPI.COMM_WORLD.Barrier();

		// Thread 0 accept message from other thread
		if (rank == 0)
		{
			int c = count;
			int[] rec = new int[1];
			rec[0] = 0;

			for (int i = 1; i < size; i++)
			{

				// receive the total number of term input
				MPI.COMM_WORLD.Recv(rec, 0, 1, MPI.INT, i, 99);
				c += rec[0];

				// receive the hashtables
				byte[] recU = new byte[99999999];
				byte[] recT = new byte[99999999];
				Hashtable u = new Hashtable();
				Hashtable t = new Hashtable();

				MPI.COMM_WORLD.Recv(recU, 0, recU.length, MPI.BYTE, i, 98);
				MPI.COMM_WORLD.Recv(recT, 0, recT.length, MPI.BYTE, i, 97);

				ByteArrayInputStream ubis = new ByteArrayInputStream(recU);
				ObjectInputStream uin = new ObjectInputStream(ubis);
				Object uobj = uin.readObject();
				u = (Hashtable) uobj;
				uin.close();
				ubis.close();

				ByteArrayInputStream tbis = new ByteArrayInputStream(recT);
				ObjectInputStream tin = new ObjectInputStream(tbis);
				Object tobj = tin.readObject();
				t = (Hashtable) tobj;
				tin.close();
				tbis.close();

				// Combine the hashtable from other thread with thread 0
				combineHashtable(user, u);
				combineHashtable(topic, t);
			}

			System.out.println("Count: " + c);
			System.out.println("************************");

			System.out.println("Top 10 Users: ");
			sort(user);
			System.out.println("************************");

			System.out.println("Top Topics: ");
			sort(topic);
			System.out.println("************************");
		}

		MPI.COMM_WORLD.Barrier();

		double t_end = MPI.Wtime();
		if (rank == 0)
		{
			double time = t_end - t_start;

			System.out.println("Time Consume: " + time);

		}

		MPI.Finalize();

	}

	public static int getCount(String word, long seg, int rank, File f, int size, Hashtable user, Hashtable topic,
			int lineNum)
	{
		int num = 0;
		try
		{
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
			BufferedReader in = new BufferedReader(new InputStreamReader(bis, "utf-8"), 10 * 1024 * 1024);

			in.skip(rank * seg);
			int ln = 0;
			String line = in.readLine();
			while (line != null && (ln <= lineNum / size || rank == size - 1))
			{
				// length+=line.length()*2;
				String[] wordList = line.split("[^#@a-zA-Z0-9]+");
				for (int i = 0; i < wordList.length; i++)
				{
					String w = wordList[i];

					// count number of word
					if (word.equalsIgnoreCase(w))
					{
						num++;
					}

					// count @ and #
					if (w.length() > 1)
					{
						if (w.substring(0, 1).equals("@"))
						{
							if (user.containsKey(w))
							{
								int v = (Integer) user.get(w) + 1;
								user.put(w, v);
							} else
							{
								user.put(w, 1);
							}
						} else if (w.substring(0, 1).equals("#"))
						{
							if (topic.containsKey(w))
							{
								int v = (Integer) topic.get(w) + 1;
								topic.put(w, v);
							} else
							{
								topic.put(w, 1);
							}
						}
					}

				}
				ln++;
				line = in.readLine();
			}

		} catch (FileNotFoundException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return num;
	}

	public static void combineHashtable(Hashtable table1, Hashtable table2)
	{
		Iterator i = table2.keySet().iterator();
		while (i.hasNext())
		{
			String s = (String) i.next();
			if (table1.containsKey(s))
			{
				int value1 = (int) table1.get(s);
				int value2 = (int) table2.get(s);
				table1.put(s, value1 + value2);
			}
		}

	}

	public static void sort(Hashtable table)
	{
		List<String> list = new ArrayList<String>(table.keySet());
		Collections.sort(list, new Comparator<Object>()
		{
			@Override
			public int compare(Object o1, Object o2)
			{
				// TODO Auto-generated method stub
				return (Integer) table.get(o2) - (Integer) table.get(o1);
			}

		});

		for (int i = 0; i < 10; i++)
		{
			String str = list.get(i);
			System.out.println(str + " " + table.get(str));
		}

	}

}
