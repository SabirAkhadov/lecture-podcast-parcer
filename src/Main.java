import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class Main {

	static private String dirName =  "podcasts";

	public static void main (String args[]){

	   InputStream is = null;
	   BufferedReader br;
	   String line = new String();
	   List<String> urls = new ArrayList<String>();
		 List<String> fileNames = new ArrayList<String>();
	   new File (dirName).mkdir();
	   try {
	        //change this link to RSS of lecture podcasts, or some list with .mp4 links
		   URL url = new URL("http://www.video.ethz.ch/lectures/d-itet/2016/autumn/263-4640-00L.rss.xml?key=a3f6f7&quality=High");
		    is = url.openStream();
	        br = new BufferedReader(new InputStreamReader(is));

					String fileName = "20.12";
					String title = "NetSec";
	        while ((line = br.readLine()) != null) {
						//get the date
						String pubDate = "<pubDate>";
						int pubDateIndex = line.indexOf(pubDate);
						if (pubDateIndex > -1){
							fileName = line.substring(pubDateIndex + pubDate.length(), line.indexOf("</pubDate")) + "_" + title;
							fileNames.add(fileName);
						}

						//get the title
						String titleString = "<title>";
						int titleIndex = line.indexOf(titleString);
						if (titleIndex > -1){
							title = line.substring(titleIndex+titleString.length(), line.indexOf("</title>"));
						}

	        	String urlStart = "url="; // adjust this if other site than eth
	        	int startIndex = line.indexOf(urlStart);
	        	if (startIndex > -1){
	        		String endLine = ".mp4"; // adjust this if other site than eth
	        		int beginIndex = startIndex+urlStart.length() + 1;
							int endIndex = line.indexOf(endLine) + 4;
	        		if (endIndex > -1){
	        				urls.add(line.substring(beginIndex, endIndex));
	        		}
	        	}
	        }

	        int i = urls.size() -1;
	        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	        while (i > 0)
	        {
						fileName = fileNames.get(i);
	        	url = new URL (urls.get(i));
						i--;
	        	Runnable worker =  new loadFromUrl (url, fileName + ".mp4");
	        	executor.execute(worker);
	        }
	        executor.shutdown();
	        if (executor.awaitTermination(Long.MAX_VALUE, TimeUnit.HOURS)){
	        	System.out.println("Work finished");
	        }

	    } catch (MalformedURLException mue) {
	         mue.printStackTrace();
	    } catch (IOException ioe) {
	        ioe.printStackTrace();
	    } catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
	        try {
	            if (is != null) is.close();
	        } catch (IOException ioe) {
	            // nothing to see here
	        }
	      }
	}

	public static class loadFromUrl implements Runnable {

		private URL url;
		private String fileName;

		public loadFromUrl(URL url, String fileName) {
			this.url = url;
			this.fileName = fileName;
		}


		@Override
		public void run() {
			System.out.println(Thread.currentThread().getName()+" is working");
			System.out.println("link to video file:" + url);
			try {
				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			        FileOutputStream fos = new FileOutputStream(dirName +"/"+ fileName);
			        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			        fos.close();

		 	} catch (MalformedURLException mue) {
	        	 	mue.printStackTrace();
	    		} catch (IOException ioe) {
	        		ioe.printStackTrace();
	    		}

		}
	}
}
