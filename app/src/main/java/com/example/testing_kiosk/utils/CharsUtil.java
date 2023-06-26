package com.example.testing_kiosk.utils;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * @author Aaron
 */
public class CharsUtil {
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);
    private CharsUtil() {/*No instance provides.*/}

    /**
     * Get current date and time as String
     * @return
     */
    public static String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }

    /**
	 * 截取文件名称的前、后缀
	 * 
	 * @param ext
	 *            false 前缀 / true后缀
	 * 
	 * @see #subname(String, char, boolean)
	 */
	public static String subfilename(String filename, boolean ext){
		return subname(filename, '.', ext);
	}

    public static String subname(String filename, char symbol, boolean ext) {
		if (filename != null && filename.length() > 0 && symbol != 0) {
			int dot =filename.lastIndexOf(symbol);
			if ((dot >= 0) && (dot <= (filename.length() - 1))) {
				return ext ? filename.substring(dot + 1) : filename.substring(
						0, dot);
			}
		}
		return filename;
	}
}
