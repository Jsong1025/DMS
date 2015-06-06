package util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

/**
 * @author Jsong
 *	
 *	DMS工具类
 *		其中包含用于文件读取的各类方法
 *
 */
public class Util {
	
	/**
	 * 从指定文件中读出一个int数据
	 * 
	 * @param file
	 * @return
	 */
	public static int readInt(File file){
		BufferedReader reader = null;
		
		try {
			FileInputStream fis = new FileInputStream(file);
			InputStreamReader isr = new InputStreamReader(fis);
			reader = new BufferedReader(isr);
			return Integer.parseInt(reader.readLine());
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * 向指定文件中保存一个int数据
	 * 
	 * @param file
	 * @param num
	 */
	public static void saveInt(File file,int num){
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			out.println(num);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * 将List集合存入指定的文件中
	 * 
	 * @param file	指定的文件
	 * @param list	要存入的List集合
	 */
	public static void saveList(File file , List list){
		PrintWriter out = null;
		try {
			out = new PrintWriter(file);
			for (Object obj: list) {
				out.print(obj);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	/**
	 * 在字节数组ary中读取一个int数据
	 * 
	 * @param ary		字节数组
	 * @param offest	偏移量
	 * @return
	 */
	public static int toInt(byte[] ary,int offest){
		DataInputStream in = null;
		try {
			ByteArrayInputStream bins = new ByteArrayInputStream(ary);
			in = new DataInputStream(bins);
			in.skip(offest);
			return in.readInt();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * 在字节数组ary中读取一个short数据
	 * 
	 * @param ary		字节数组
	 * @param offest	偏移量
	 * @return
	 */
	public static short toShort(byte[] ary,int offest){
		DataInputStream in = null;
		try {
			ByteArrayInputStream bins = new ByteArrayInputStream(ary);
			in = new DataInputStream(bins);
			in.skip(offest);
			return in.readShort();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException(e);
				}
			}
		}
	}
	
	/**
	 * 在字节数组ary中读取一个String数据
	 * 
	 * @param ary		字节数组
	 * @param offest	偏移量
	 * @param length 	长度
	 * @return
	 */
	public static String toString(byte[] ary,int offest,int length){
		try {
			String str = new String(ary,offest,length,"UTF-8");
			return str.trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * 休眠指定时间
	 * 
	 * @param millis	休眠的时间（毫秒）
	 */
	public static void sleep(long millis){
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
