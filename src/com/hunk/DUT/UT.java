package com.hunk.DUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Random;

public class UT {
	public static String captureName(String name) {
        char[] cs=name.toCharArray();
        cs[0]-=32;
        return String.valueOf(cs);
	}
	
	public static String checkStringforbs(String str){
		if(str!=null){
			str = str.replaceAll("\\\\", "\\\\\\\\");
		}
		return str;
	}
	
	public static boolean zstr(String str){
		if(str == null)
			return true;
		else
			return "".equals(str)?true:false;
	}
	
	public static String errormsg(Exception e){
		//return e.getClass().getName();
		return e.getLocalizedMessage();
	}
	
	public static void printstr(PrintWriter pw,String str){
		pw.print(str);
		pw.flush();
		pw.close();
		pw = null;
	}
	
	public static <T>String sfcloseDb(T t) {
		String errormsg = "";
		if(t!=null){
			try {
				t.getClass().getMethod("close").invoke(t);
			} catch (Exception e) {
				//e.printStackTrace();
				errormsg+=errormsg(e);
			} finally{
				t = null;
			}
		}
		return errormsg;
	}
		
	public static <T>String setbean(Map<String,String[]> map,T t){
		String savetype = "";
		Set<String> set = map.keySet();
        for (Iterator<String> it = set.iterator();it.hasNext();) {

            String key = it.next();
            String cnt = "";
            
            for(int i=0;i<map.get(key).length;i++){
            	if(i>0)
            		cnt+=",";
            	cnt+=map.get(key)[i];
            }
            if(key.lastIndexOf("[]")>0)
            	key = key.substring(0, key.lastIndexOf("[]"));
            System.out.println(key+":"+cnt);
            if(key.equals("savetype")){
            	savetype = cnt;
            }else{
            	try {
					t.getClass().getDeclaredMethod("set"+UT.captureName(key), String.class).invoke(t,cnt);
				} catch (Exception e) {
					//e.printStackTrace();
				} 
            }
        }
        return savetype;
	}
	
	public static String selcdt(HashMap<String, String> cdt,int i){
		String sqlcmd = "";
		String t1;
		String t2 = " where ";
		if(cdt==null)
			return sqlcmd;
		Set<Entry<String, String>> set = cdt.entrySet();
		for(Entry<String, String> e:set){
			if(e.getValue().equals("")){
				continue;
			}
			if(i>0){
				t2 = " and ";
			}
			t1 = t2 + "`" + e.getKey() + "` like '%" + e.getValue() + "%'";
			sqlcmd += t1;
			i++;
		}
		return sqlcmd;
	}
		
	public static void fileclose(OutputStream os, InputStream is) throws IOException{
		if(os!=null){
			os.flush();
			os.close();
			os=null;
		}
		if(is!=null){
			is.close();
			is=null;
		}
	}
	
	public static final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static String generateString(int length) {   
        StringBuffer sb = new StringBuffer();   
        Random random = new Random();   
        for (int i = 0; i < length; i++) {   
                sb.append(allChar.charAt(random.nextInt(allChar.length())));   
        }   
        return sb.toString();   
	}
	
	public static String formattime() {   
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMddHHmmss"); 
        String ctime = formatter.format(new Date());
        return ctime;
	}
	
	public static String randomID() {   
        StringBuffer sb = new StringBuffer();
        sb.append(formattime());
        sb.append(generateString(18));
        return sb.toString();   
	}
	
	public static int randomNum(int min, int max) {   
        return (new Random()).nextInt(max-min+1)+min;   
	}
	
	public static String NSinsert(String str, String dfval){
		return "".equals(str)?dfval:str;
	}
	
	public static boolean isPastT(long time){
		long timenow = (new Date()).getTime()/1000;
		if(time<timenow)
			return true;
		return false;
	}
}
