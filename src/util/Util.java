package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

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

}
