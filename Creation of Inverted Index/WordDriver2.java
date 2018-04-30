import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class WordDriver2{
        public static class WordMapper2 extends Mapper<LongWritable, Text, Text, Text>
        {
                private Text word = new Text();
                @Override
                public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException
                {
                   String line = value.toString();
                   String arr[] = line.split("\t", 2);
                   //IntWritable outputValue = new IntWritable(Integer.parseInt(arr[0]));
                   Text outputValue = new Text(arr[0]);
                   StringTokenizer str = new StringTokenizer(arr[1]);
                   while(str.hasMoreTokens())
                   {
                    word.set(str.nextToken());
                    context.write(word, outputValue);
                   }
                }
        }
        public static class WordReducer2 extends Reducer<Text, Text, Text, Text>
        {
                @Override
                public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException
                {
                         //MapWritable counter = new MapWritable();
                         HashMap<String, Integer> counter = new HashMap();
                         for(Text val: values)
                         {
                                String value = val.toString();
                                if(counter.containsKey(value))
                                 {
                                        //temp = (IntWritable)counter.get(val);
                                        int t = counter.get(value);
                                        t += 1;
                                        counter.put(value, new Integer(t));
                                 }else{
								 
                                         counter.put(value,  new Integer(1));
                                         }
                         }
                         StringBuilder sb = new StringBuilder("");
                         for(String x: counter.keySet())
                         {
                                 sb.append(x+":"+counter.get(x)+" ");
                         }
                         Text outputValue = new Text(sb.toString());
                         context.write(key, outputValue);
                }
        }
        public static void main(String args[]) throws IOException, InterruptedException, ClassNotFoundException
        {
                if(args.length < 2)
                {
                        System.out.println("Enter more args");
                }else{
                        Configuration c = new Configuration();
                        Job j = Job.getInstance(c, "word count");
                        j.setJarByClass(WordDriver2.class);
                        j.setMapperClass(WordMapper2.class);
                        j.setReducerClass(WordReducer2.class);
                        j.setMapOutputKeyClass(Text.class);
                        j.setMapOutputValueClass(Text.class);
                        j.setOutputKeyClass(Text.class);
                        j.setOutputValueClass(Text.class);
                        FileInputFormat.addInputPath(j, new Path(args[0]));
                        FileOutputFormat.setOutputPath(j, new Path(args[1]));
                        System.exit(j.waitForCompletion(true)? 0 : 1);
                }
        }
}
