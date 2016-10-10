package com.mobpie.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;



public class ImageUtil {
    public static final int MAX_UPLOAD_WIDTH = 800;

    public static final int MAX_UPLOAD_HEIGHT = 670;

    public static final int MAX_SHOW_WIDTH = 450;

    public static final int MAX_SHOW_HEIGHT = 450;
    
    public static final int MAX_UPLOAD_FILE_SIZE = 200 * 1024;//
 
    public static String getSDCardPath() throws IOException {
        if (null == Environment.getExternalStorageDirectory()) {
            throw new IOException("");
        } else {
            return Environment.getExternalStorageDirectory().getPath() + "/";
        }
    }
    
	/**
	 * 得到一个不超过指定边长的bitmap
	 * @param filePath 文件路径
	 * @param edgeLen  期望得到的边长
	 * @return
	 */
	public static Bitmap getPreviewBmp(String filePath, int edgeLen)
	{
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opt);
		int largerEdgeLen = Math.max(opt.outHeight, opt.outWidth);
		
		BitmapFactory.Options optDecode = new BitmapFactory.Options();
		optDecode.inSampleSize = largerEdgeLen / edgeLen;
		optDecode.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(filePath, optDecode);
		
		int orientation = 0;
		int rotateDegree = 0;
		
		try
		{
			ExifInterface exif = new ExifInterface(filePath);
			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
		
		switch(orientation)
		{
		case ExifInterface.ORIENTATION_ROTATE_180:
			rotateDegree = 180;
			break;
		case ExifInterface.ORIENTATION_ROTATE_270:
			rotateDegree = 270;
			break;
		case ExifInterface.ORIENTATION_ROTATE_90:
			rotateDegree = 90;			
			break;
		case ExifInterface.ORIENTATION_UNDEFINED: //0
			break;
		}
		
		if(rotateDegree != 0)
		{
			Matrix m = new Matrix();
			m.postRotate(rotateDegree, bmp.getWidth() / 2, bmp.getHeight() / 2);
			bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, false);
		}
		
		//TODO 这里需要scale到准确的edgeLen长度。
		
		return bmp;
	}


    public static Bitmap createThumbnail(String filename, float width, float height) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filename);
            Matrix matrix = new Matrix();
            matrix.postScale(width / bitmap.getWidth(), height / bitmap.getHeight());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            return bitmap;
        } catch (Exception ex) {
//            ex.printStackTrace();
        }
        return null;
    }

    public static Bitmap createThumbnail(Resources res, int resId, float width, float height) {
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId);
        Matrix matrix = new Matrix();
        matrix.postScale(width / bitmap.getWidth(), height / bitmap.getHeight());
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return bitmap;
    }

    public static void compressImagetoSize(Context context, String infileurl, String outfileurl,int w,int h) {
        InputStream is = null;
        try {
            File f = new File(infileurl);

            BitmapFactory.Options newOpts = getSizeOpt(f, w, h);

            is = new FileInputStream(f);

            CompressJPGFile(is, newOpts, outfileurl);

        } catch (Exception e) {
            //Log.e("compressImage", "compressImage:", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 蹇界暐
                }
            }
        }
    }
    
    public static void compressImagetoSize(Context context, Bitmap bitmap, String outfileurl,int w,int h) {
        try {
        	File f = new File(outfileurl);
            if (f.exists()) {
                f.delete();
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(CompressFormat.JPEG, 100, fos);
            fos.close();
            
            compressImagetoSize(context, outfileurl, outfileurl, w, h);

        } catch (Exception e) {
            //Log.e("compressImage", "compressImage:", e);
        } finally {

        }
    }
    public static String compressImage(Context context, String srcPath, String destPath) {
        return compressImage(context, srcPath, destPath, MAX_UPLOAD_WIDTH, MAX_UPLOAD_HEIGHT);
    }

    public static String compressImage(Context context, String srcPath, String destPath, int maxW, int maxH) {
        InputStream is = null;
        try {
            File f = new File(srcPath);
            BitmapFactory.Options newOpts = getSizeOpt(f, maxW, maxH);
            is = new FileInputStream(f);
            return CompressJPGFile(is, newOpts, destPath);

        } catch (Exception e) {
            //Log.e("compressImage", "compressImage:", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 蹇界暐
                }
            }
        }
        return null;

    }

    public static String compressImage(Context context, Uri uri, String compressPath) {
        InputStream is = null;
        try {
            BitmapFactory.Options newOpts = getSizeOpt(context, uri, MAX_UPLOAD_WIDTH, MAX_UPLOAD_HEIGHT);
            is = context.getContentResolver().openInputStream(uri);
            return CompressJPGFile(is, newOpts, compressPath);

        } catch (Exception e) {
            //Log.e("compressImage", "compressImage:", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 蹇界暐
                }
            }
        }
        return null;

    }
    
    /**
     * 鍥剧墖澶嶅埗浼氬澶т簬200k鐨勮繘琛岃嚜鍔ㄥ帇缂?
     * @param context
     * @param path
     * @param toPath
     * @return
     */
	public static String copyImageTo(Context context, String path, String toPath, boolean isCompress) {
		File file = new File(path);
		if (file != null && file.exists()) {
			if (isCompress && file.length() > MAX_UPLOAD_FILE_SIZE) {
				return compressImage(context, path, toPath);
			} else {
				transferFile(path, toPath);
			}
		} 
		return toPath;
	}


    public static String compressImage(Context context, String fileurl) {
        InputStream is = null;
        try {
            File f = new File(fileurl);

            BitmapFactory.Options newOpts = getSizeOpt(f, MAX_UPLOAD_WIDTH, MAX_UPLOAD_HEIGHT);

            is = new FileInputStream(f);

            return CompressJPGFile(is, newOpts, fileurl);

        } catch (Exception e) {
            //Log.e("compressImage", "compressImage:", e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    // 蹇界暐
                }
            }
        }
        return null;
    }

    public static String compressImage(Context context, Bitmap bitmap, String compressPath) {
        try {
            File f = new File(compressPath);
            if (f.exists()) {
                f.delete();
                f.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(f);
            bitmap.compress(CompressFormat.JPEG, 100, fos);
            fos.close();
            return ImageUtil.compressImage(context, compressPath);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return null;
    }

    
    public static BitmapFactory.Options getSizeOpt(String url, int maxWidth, int maxHeight) throws IOException {
        FileInputStream is = null;
        try {
            return getSizeOpt(new File(url), maxWidth, maxHeight);
        } catch (Exception e) {
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
                is = null;
            }
        }
    }

    public static Bitmap getLimitSizeBitmap(String url) throws IOException {
        BitmapFactory.Options newOpts = getSizeOpt(url, MAX_SHOW_WIDTH, MAX_SHOW_HEIGHT);
        Bitmap destBm = BitmapFactory.decodeFile(url, newOpts);
        return destBm;
    }

    /**
     * 鍏堝帇缂╁浘鐗囧ぇ灏?
     * @param is
     * @param maxSize
     * @return
     * @throws IOException
     */
    public static BitmapFactory.Options getSizeOpt(File file, int maxWidth, int maxHeight) throws IOException {

         BitmapFactory.Options newOpts = new BitmapFactory.Options();

        double ratio = getOptRatio(new FileInputStream(file), maxWidth, maxHeight);
        newOpts.inSampleSize = (int) ratio;

        newOpts.inJustDecodeBounds = true;

        InputStream is = new FileInputStream(file);

        BitmapFactory.decodeStream(is, null, newOpts);
        is.close();
        while (newOpts.outWidth > maxWidth) {
            newOpts.inSampleSize += 1;
            is = new FileInputStream(file);
            BitmapFactory.decodeStream(is, null, newOpts);
            is.close();
        }
        //缁勫悎浣跨敤涓嬮潰涓や釜鍙傛暟
        //1銆佸湪绯荤粺闇?鍐呭瓨鐨勬椂鍊欏彲浠ュ洖鏀舵帀鍥剧墖鍗犵敤鐨勫唴瀛?
        //2銆佸宸茬粡鍦ㄥ唴瀛樹腑鐨刡itmap杩涜澶嶇敤
        //newOpts.inInputShareable=true;  //[#4802623]鐐瑰嚮鎷嶇収涓婁紶鍥剧墖锛岀▼搴廲rash
        //newOpts.inPurgeable=true;
        // inJustDecodeBounds璁句负 false琛ㄧず鎶婂浘鐗囪杩涘唴瀛樹腑
        newOpts.inJustDecodeBounds = false;
        return newOpts;
    }
    /**
     * 鑾峰彇鍥剧墖鏂囦欢澶翠俊鎭?
     * @param in
     * @return
     */
    public static BitmapFactory.Options getImageOptions(InputStream in) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(in, null, opts);
        return opts;
    }

    /**
     * 璁＄畻璧峰鍘嬬缉姣斾緥
     * 鍏堟牴鎹疄闄呭浘鐗囧ぇ灏忎及绠楀嚭鏈?帴杩戠洰鏍囧ぇ灏忕殑鍘嬬缉姣斾緥
     * 鍑忓皯寰幆鍘嬬缉鐨勬鏁?
     * @param is
     * @param maxLength
     * @return
     */
    public static double getOptRatio(InputStream is, int maxWidth, int maxHeight) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;

        BitmapFactory.decodeStream(is, null, opts);

        try {
            is.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
//            e.printStackTrace();
        }

        int srcWidth = opts.outWidth;
        int srcHeight = opts.outHeight;
        int destWidth = 0;
        int destHeight = 0;
        // 缂╂斁鐨勬瘮渚?
        double ratio = 0.0;
        double ratio_w = 0.0;
        double ratio_h = 0.0;

        // 鎸夋瘮渚嬭绠楃缉鏀惧悗鐨勫浘鐗囧ぇ灏忥紝maxLength鏄暱鎴栧鍏佽鐨勬渶澶ч暱搴?
        if (srcWidth <= maxWidth && srcHeight <= maxHeight) {
            return 0.0;
        }

        if (srcWidth > srcHeight) {
            ratio_w = srcWidth / maxWidth;
            ratio_h = srcHeight / maxHeight;

        } else {
            ratio_w = srcHeight / maxWidth;
            ratio_h = srcWidth / maxHeight;

        }
        if (ratio_w > ratio_h) {
            ratio = ratio_w;
        } else {
            ratio = ratio_h;
        }

        return ratio;
    }
    
    private static double getRatio(int srcWidth, int srcHeight, int maxWidth, int maxHeight) {
        double ratio = 0.0;
        double ratio_w = 0.0;
        double ratio_h = 0.0;

        // 鎸夋瘮渚嬭绠楃缉鏀惧悗鐨勫浘鐗囧ぇ灏忥紝maxLength鏄暱鎴栧鍏佽鐨勬渶澶ч暱搴?
        if (srcWidth <= maxWidth && srcHeight <= maxHeight) {
            return 0.0;
        }
        
        ratio_w = (double)maxWidth / (double)srcWidth;
        ratio_h = (double)maxHeight / (double)srcHeight;

        if (ratio_w < ratio_h) {
            ratio = ratio_w;
        } else {
            ratio = ratio_h;
        }

        return ratio;
    }

    public static BitmapFactory.Options getSizeOpt(Context context, Uri uri, int maxWidth, int maxHeight) throws IOException {
        InputStream is = null;
        is = context.getContentResolver().openInputStream(uri);
        // 瀵瑰浘鐗囪繘琛屽帇缂╋紝鏄湪璇诲彇鐨勮繃绋嬩腑杩涜鍘嬬缉锛岃?涓嶆槸鎶婂浘鐗囪杩涗簡鍐呭瓨鍐嶈繘琛屽帇缂?
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        double ratio = getOptRatio(is, maxWidth, maxHeight);

        newOpts.inSampleSize = (int) ratio;
        newOpts.inJustDecodeBounds = true;
        is = context.getContentResolver().openInputStream(uri);
        BitmapFactory.decodeStream(is, null, newOpts);
        is.close();
        while (newOpts.outWidth > maxWidth || newOpts.outHeight > maxHeight) {
            newOpts.inSampleSize += 1;
            is = context.getContentResolver().openInputStream(uri);
            BitmapFactory.decodeStream(is, null, newOpts);
            is.close();
        }

        // 鑾峰彇缂╂斁鍚庡浘鐗?
        //缁勫悎浣跨敤涓嬮潰涓や釜鍙傛暟
        //1銆佸湪绯荤粺闇?鍐呭瓨鐨勬椂鍊欏彲浠ュ洖鏀舵帀鍥剧墖鍗犵敤鐨勫唴瀛?
        //2銆佸宸茬粡鍦ㄥ唴瀛樹腑鐨刡itmap杩涜澶嶇敤
        //newOpts.inInputShareable=true; //[#4802623]鐐瑰嚮鎷嶇収涓婁紶鍥剧墖锛岀▼搴廲rash
        //newOpts.inPurgeable=true;
        // inJustDecodeBounds璁句负 false琛ㄧず鎶婂浘鐗囪杩涘唴瀛樹腑
        newOpts.inJustDecodeBounds = false;
        return newOpts;
    }

    private static String CompressJPGFile(InputStream is, BitmapFactory.Options newOpts, String filePath) {

        Bitmap destBm = BitmapFactory.decodeStream(is, null, newOpts);
        if (destBm == null) {
            return null;
        } else {
            // 鏂囦欢鍛藉悕锛岄?杩嘒UID鍙伩鍏嶅懡鍚嶇殑閲嶅
            // String fileName = java.util.UUID.randomUUID().toString()
            // + ".jpg";

            // 鍙﹀瀹氫箟锛?
            // ConfigManager.photoDir = getFileStreamPath(photoDirName)
            // String photoDirName = "photo";瑕佹敞鎰忔槸鏍圭洰褰?
            File destFile = createNewFile(filePath);
            
            // 鍒涘缓鏂囦欢杈撳嚭娴?
            OutputStream os = null;
            try {
                os = new FileOutputStream(destFile);
                // 瀛樺偍
                int rate = 80;
                destBm.compress(CompressFormat.JPEG, rate, os);
                // 鍏抽棴娴?
//                os.close();
                destBm.recycle();

                
            } catch (Exception e) {
            	filePath = null;
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        // 蹇界暐
                    }
                }
            }
            return filePath;
        }
    }
    
    public static File createNewFile(String filePath) {
		if (filePath == null)
			return null;
		File newFile = new File(filePath);
		try {
			if (!newFile.exists()) {
				int slash = filePath.lastIndexOf('/');
				if (slash > 0 && slash < filePath.length() - 1) {
					String dirPath = filePath.substring(0, slash);
					File destDir = new File(dirPath);
					if (!destDir.exists()) {
						destDir.mkdirs();
					}
				}
			} else {
				newFile.delete();
			}
			newFile.createNewFile();
		} catch (IOException e) {
			return null;
		}
		return newFile;
    }
    
    /**
     * zoomIn BitmapDrawable
     * @param drawable
     * @param maxW
     * @param maxH
     * @return
     */
	public static Bitmap zoomIn(Bitmap bitmap, int maxW, int maxH) {
		
		int width  = bitmap.getWidth();
		int height = bitmap.getHeight();
		if (width <= maxW && height <= maxH)
			return bitmap;
		Matrix matrix = new Matrix(); // 鍒涘缓鎿嶄綔鍥剧墖鐢ㄧ殑Matrix瀵硅薄
		float ratio = (float) getRatio(width, height, maxW, maxH);
		matrix.postScale(ratio, ratio); // 璁剧疆缂╂斁姣斾緥
//		Bitmap oldbmp = drawable.getBitmap(); // drawable杞崲鎴恇itmap
		Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); // 寤虹珛鏂扮殑bitmap锛屽叾鍐呭鏄鍘焍itmap鐨勭缉鏀惧悗鐨勫浘
		return newbmp; // 鎶奲itmap杞崲鎴恉rawable骞惰繑鍥?
	}
	
    public static void transferFile(String srcPath, String desPath) {
    	InputStream in = null;
    	OutputStream out = null;
        try {
            
            File f = new File(srcPath);
            File df = createNewFile(desPath);
            in = new FileInputStream(f);
            out = new FileOutputStream(df);
            byte[] b = new byte[2048];
            int n;
            long bytes = 0;
            while ((n = in.read(b)) >= 0) {
                bytes += n;
                out.write(b, 0, n);
            }
        } catch (Exception e1) {
            //          e1.printStackTrace();
        } finally {
        	if (in != null)
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
        	if (out != null)
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
        }
    }
    
}
