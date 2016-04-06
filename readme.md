# Assignment 1 Report - Kunliang Wu, 684226

---
*Note£ºThis note is written in MarkDown, so the format might be a little strange to those who are used to Latex or MS-Word :D*
##The final result is shown in the following graph

![Final Result][1]
  [1]: https://raw.githubusercontent.com/Unibrighter/COMP90024_ClusterComputing_Proj1/master/Time%20Cost%20for%20Different%20Procedure.png


---
##Usage

 - The path of the input tweet file is in Main.java, change the path
   when it's neccessary. 
 - The Preprocess.java is a class that extract the useful contents out of the input file, and transform it into a String array. 
 - The ResultEntity.java is the class which holds the final analysis result and makes it's easier to send and receive the statics.
 - Use the scripts named as rock*.sh to run the program in SLURM enviroment. The command is like this:
    - sbatch rock_1-8.sh rock_1-8

##Some Thoughts About The Strategy
### Load File And Content Extraction
The given tweet file is about 10G, and it's on the hard disk. The first step to solve such a problem is to find out an efficient way to load the file into the memory so that we can operate on it.
There are two main strategies:

 1. Load the file all into the memory once and for all
 2. Using buffered I/O stream, reading the file while doing the process
 
After testing both of these two,I discovered that the text content in each line does not make up too much part of the entire file - the size of the useful part extracted from the raw material is only 1/70 of the original. 
And for Java, it's acceptable to load that into the memory once and for all. 
Also, using NIO memeory projection is not a bad idea in this scenario,but I am afraid it would consume a lot of memory, wasting too much resources on the text we are not interested in, so I did not use NIO Memory Map in this problem.
Another reason I sticked to the first strategy is that if the problem we are dealing with involves a lot of looking-back and searching operations, the I/O stream would be the bottle neck lowering the performance.

###Distribute the tasks
There are also two main ways to assign the tasks to separate cpus.

1. Read next line of the tweet file(mark the line number with ***'n'***) in the master thread, assign this line to task ***No.('n'% rank)*** or according to its particular segment pre-defined.
2. Each task has its own reading Input Stream, it specifies the segment from line ***'n'*** to line ***'m'***, or just reads through the whole file and simply skips the lines which do not belong to it.

In MPJ, the performance is extremly poor using the first way. This is because you can't really determine where the parallel part of the program begins. If the compiler sees there is MPI.Init(), the first line in Main method will be treated as where the parallel part begins. Also, you can't program with Class static method since this code will be treated as the method running with multiple threads as well. However I still think this is the most efficient the way if the task requires a lot of looking-back, searching, and comparison to itself, the reason is hard disk reading.

###Sum up the results
Two HashTable are used in each thread to collect the Tweeter and Topic numbers. A mehtod in which two hashtables are combined is used in the root thread to gather all the statics and making the final result. The Gather() Method in MPJ does not work well in my program and I could not figure out why. So I have defined another Class ResultEntity, which holds the final results to be summed up, and its serialized so it can be Sent and Received using MPJ.OBJECT type.

-------
##Some Thoughts About The Performance
The fisrt picture demonstrate the performances of the program using different SBATCH OPTIONs.
The red bar indicates that the cost for actual computation, searching and comparison goes along with the number of the cores. The value of the time for actual computation also has a co-relation with the number of the nodes involved. For a simple reading-analysis program, in which there exists no writing steps, one node(2268 Milli Sec) performs better than two nodes£¨3595 Milli Sec£© when the total figure of the tasks remains the same(8 cores). The average time spent on actual search in each task is more or less the same as 1/8 of the 1 core task, this matches how the parallelized search really improves the work efficiency.

However, the preprocessing cost is relatively small and can almost be ignored compared to the cost of actual analysis processing. This is another reason that contributes to the worse performance in my strategy. I have to admit in this particular problem, where going through the whole file doesn't take much time, the combination of the second main strategy in put and the second main way in distributing the jobs works out best.

Yet, there are something shocking awful in my program : the cost of intra-communicating. In the 1-node-8-core and 2-node-8-core cases, the time spent on scattering, initializing and information-gathering is much more than the actual calculation work! In 1-node-1-core case, the real time spent on computing is 94.45%(11282 / 11697) of the totol time. But in multiple tasks cases, the figure is no more than 30%.

I believe this is because the strategy and the libray I choose does not work together as I expected. I did the same program in my local labtop using C and a smaller truncated version of the tweet file, and it showed that the cost for intra-communicating is not that much as it in MPJ.

And this is the main reason that the total running time in 1-node-1-core case is even shorter than the two parallelized ones.