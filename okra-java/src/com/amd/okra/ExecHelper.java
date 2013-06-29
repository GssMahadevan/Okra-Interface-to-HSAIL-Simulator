package com.amd.okra;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ExecHelper {
  
	public static boolean exec(String... cmd){
		return exec(true,cmd);
	}
	public static boolean exec(boolean redirectErr,String... cmd){
		boolean  ret=false;
		String cmdFull= Arrays.toString(cmd);
		System.out.println("spawning" +cmdFull);
		ProcessBuilder pb=null;
		Process p=null;
        try {
            pb = new ProcessBuilder(cmd);
            if(redirectErr){
            	pb.redirectErrorStream(true);
            }
            p = pb.start();
            
            new StreamConsumer(cmdFull+"-STDOUT",p.getInputStream(),false).start();
            if(!redirectErr){
            	new StreamConsumer(cmdFull + "-STDERR",p.getInputStream(),true).start();
            }
            p.waitFor();
            ret=true;
        } catch (Exception e) {
            System.err.println("could not execute <" + cmdFull + ">");
        } finally{
        	//if( p != null) p.destroy();
        }
        return ret;
	}

	private static class StreamConsumer extends Thread {
		private InputStream is;
		private boolean isErr;
		private String cmd;
    	public StreamConsumer(String name,InputStream is,boolean err){
    		super("ExceHelper-"+name);
    		this.cmd=name;
    		setDaemon(true);
    		this.is=is;
    		this.isErr=err;
    	}
    	
    	public void run(){
    		try{
    			BufferedInputStream buf = new BufferedInputStream(is);
    			InputStreamReader inread = new InputStreamReader(buf);
    			BufferedReader bufferedreader = new BufferedReader(inread);
    			String line;
    			while ((line = bufferedreader.readLine()) != null) {
    				if(isErr){
    					System.err.println(line);
    				}else{
    					System.out.println(line);
    				}
    			}
    		}catch(Exception e){
    			System.err.println("Got error while consuming:"+cmd+", isErr:"+isErr);
    			e.printStackTrace();
    		}finally{
    			close(is);
    		}
    	}

		private void close(InputStream res) {
			if(res == null) return;
			try{
				res.close();
			}catch(Exception e){}
		}
	}
	
	public static void main(String[] args){
		if(isWindows()){
			ExecHelper.exec(new String[]{"Cmd.exe", "/c", "dir","/od"});
		}else{
			ExecHelper.exec(new String[]{"ls", "-l", "-t","-r"});
		}
	}
	
	public static boolean isWindows(){
		boolean ret=false;
		String osName=System.getProperty("os.name");
		System.out.println(osName);
		Pattern pat = Pattern.compile("^.*windows.*$",Pattern.CASE_INSENSITIVE);
		Matcher mat=pat.matcher(osName);
		if(mat.matches()){
			ret=true;
		}
		return ret;
	}
}
