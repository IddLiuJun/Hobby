package com.example.dianciguanli.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

public class QRCodeUtils {
    public static Bitmap generateQRCode(String content, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Bitmap addWhiteBorder(Bitmap bitmap, int borderSize) {
        Bitmap borderBitmap = Bitmap.createBitmap(
                bitmap.getWidth() + borderSize * 2,
                bitmap.getHeight() + borderSize * 2,
                Bitmap.Config.RGB_565
        );
        Canvas canvas = new Canvas(borderBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, borderSize, borderSize, null);
        return borderBitmap;
    }

    public static Bitmap addTextToQRCode(Bitmap qrBitmap, String text) {
        int width = qrBitmap.getWidth();
        int textHeight = 60;
        int height = qrBitmap.getHeight() + textHeight;
        
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.WHITE);
        
        canvas.drawBitmap(qrBitmap, 0, 0, null);
        
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(32);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        
        canvas.drawText(text, width / 2, qrBitmap.getHeight() + 40, paint);
        
        return result;
    }
}