package com.example.testing_kiosk.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;

/**
 * @author Aaron
 */
public class DrawConvert {
    private DrawConvert() {/*No instance provides.*/}

    @NonNull
    public static byte[] raw2rgba(final byte[] raw8bit){
        byte [] bits = new byte[raw8bit.length*4];
        int i;
        for(i=0; i< raw8bit.length; i++)
        {
            bits[i*4] = bits[i*4+1] = bits[i*4+2] = /*(byte) ~*/raw8bit[i]; //Invert the source bits
            bits[i*4+3] = (byte)0xff; // the alpha.
        }
        return bits;
    }

    @NonNull
    public static Bitmap raw2bitmap(byte[] raw8bit, int width, int height){
        final byte[] rgba = raw2rgba(raw8bit);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(rgba));
        return bitmap;
    }

    @NonNull
    public static ByteBuffer raw2rgba(final ByteBuffer raw8bit){
        final byte[] rawBytes = raw8bit.array();
        final byte[] rgbaBytes = raw2rgba(rawBytes);
        return ByteBuffer.wrap(rgbaBytes);
    }

    @NonNull
    public static byte[] rgba2raw(final byte[] rgba){
        byte [] bits = new byte[rgba.length/4];
        int i;
        for(i=0; i< bits.length; i++)
        {
            bits[i]=rgba[i*4];
        }
        return bits;
    }

    @NonNull
    public static ByteBuffer rgba2raw(final ByteBuffer rgba){
        final byte[] rgbaBytes = rgba.array();
        final byte[] rawBytes = rgba2raw(rgbaBytes);
        return ByteBuffer.wrap(rawBytes);
    }
}
