import java.io.BufferedReader;
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
	
	public static void main (String args[]){
	   
	   InputStream is = null;
	   BufferedReader br;
	   String line = new String();
	   List<String> urls = new ArrayList<String>();
	  
	   try {
	        //change this link to that of a lecture
		   URL url = new URL("http://www.multimedia.ethz.ch/lectures/infk/2013/spring/252-0062-00L");
		    is = url.openStream();  // throws an IOException
	        br = new BufferedReader(new InputStreamReader(is));
	        
	        while ((line = br.readLine()) != null) { 
	        	String endLine = "grabber-dm.m4v"; // adjust this if other site than eth
	        	int endIndex = line.indexOf(endLine);
	        	if (endIndex>-1){
	        		String beginLine = "http://replay-progressive.ethz.ch/h264-medium.http"; // adjust this if other site than eth
	        		int beginIndex = line.indexOf(beginLine);
	        		if (beginIndex > -1){
	        			endIndex += endLine.length();
		            	urls.add(line.substring(beginIndex, endIndex));
	        		}
	        	}
	        }
	        	
	        int i = urls.size() -1;
	        int n = 1;
	        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
	        while (i > 0)
	        {
	        	url = new URL (urls.get(i--));
	        	System.out.println("link to video file:" + url);
	        	Runnable worker =  new loadFromUrl (url, ""+n+".m4v"); // can add more meaningful names
	        	executor.execute(worker);
	        	n++;
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
			try {
				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		        FileOutputStream fos = new FileOutputStream("podcasts/" + fileName);
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

