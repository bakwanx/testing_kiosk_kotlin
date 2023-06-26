package com.example.testing_kiosk.utils;

import android.os.Environment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

public class StorageUtil {
    private static final String DIR_NAME = "Dt-fpScanner";
    private static final SimpleDateFormat mDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.CHINA);

    public static int bufferSize = 64*1024;

    /**
     * generate output file
     * @param type Environment.DIRECTORY_MOVIES / Environment.DIRECTORY_DCIM etc.
     * @param ext .mp4(.m4a for audio) or .png ...
     * @return return null when this app has no writing permission to external storage.
     */
    public static File getCaptureFile(final String type, final String ext) {
        final File dir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_NAME);
        dir.mkdirs();
        if (dir.canWrite()) {
            return new File(dir, getDateTimeString() + ext);
        }
        return null;
    }

    public static File getPathFile(final File path, final String ext){
        return new File(path, getDateTimeString() + ext);
    }

    public static byte[] loadFileBytesBy(final String path)
            throws IOException {
        FileInputStream in = new FileInputStream(new File(path));
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        int len;
        byte[] buffer = new byte[bufferSize];
        while (true)
        {
            len=in.read(buffer,0,bufferSize);
            if (len<0 )
                break;
            bout.write(buffer,0,len);
        }

        return bout.toByteArray();
    }

    /**
     * get current date and time as String
     * @return
     */
    private static String getDateTimeString() {
        final GregorianCalendar now = new GregorianCalendar();
        return mDateTimeFormat.format(now.getTime());
    }
}
