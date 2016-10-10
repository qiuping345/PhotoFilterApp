package com.mobpie.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.EmbossMaskFilter;
import android.graphics.MaskFilter;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.opengl.Matrix;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;

public class FilterMask {
	
	private static float[] grayFilter = new float[]{0.3086f, 0.6094f, 0.0820f, 0, 0, 
												   0.3086f, 0.6094f, 0.0820f, 0, 0,
												   0.3086f, 0.6094f, 0.0820f, 0, 0,
												   0,       0,       0,       1, 0};
	
	private static float[] defaultArray = new float[]{1, 0, 0, 0, 0,
													 0, 1, 0, 0, 0,
													 0, 0, 1, 0, 0,
													 0, 0, 0, 1, 0};
	
	/*
	public static float[] invertFilter = new float[]{-1,  0,   0,   0,   255,
													 0,   -1,  0,   0,   255,
													 0,   0,   -1,  0,   255,
													 0,   0,   0,   1,   0};
	
	public static float[] rgSwapFilter = new float[]{0, 1, 0, 0, 0,
													 1, 0, 0, 0, 0,
													 0, 0, 1, 0, 0,
													 0, 0, 0, 1, 0};
	
	public static float[] rbSwapFilter = new float[]{0, 0, 1, 0, 0,
												     0, 1, 0, 0, 0,
													 1, 0, 0, 0, 0,
													 0, 0, 0, 1, 0};
		
	public static float[] gbSwapFilter = new float[]{1, 0, 0, 0, 0,
													 0, 0, 1, 0, 0,
													 0, 1, 0, 0, 0,
													 0, 0, 0, 1, 0};
	*/
	
	
	public static FilterStruct blackWhiteFilter = new FilterStruct(new ColorMatrixColorFilter[]{new ColorMatrixColorFilter(grayFilter)},null, null);

	public static FilterStruct lomoFilter = new FilterStruct(new ColorMatrixColorFilter[]{
																	getScaleBrightFilter(1.0f, 0.95f, 1.2f),
																	getContrastFilter(0f, -0.20f, -0.50f),
																	getSaturationFilter(1.2f)
																},
																null, null);

	public static FilterStruct lomo2Filter = new FilterStruct(new ColorMatrixColorFilter[]{
																	getScaleBrightFilter(1.2f, 1.2f, 0.95f),
																	getContrastFilter(1f, -0.20f, -0.50f),
																	//getSaturationFilter(1.2f)
																},
																null, null);

	public static FilterStruct lomo3Filter = new FilterStruct(new ColorMatrixColorFilter[]{
																	getScaleBrightFilter(0.95f, 1.4f, 0.95f),
																	getContrastFilter(1f, -0.20f, -0.50f),
																},
																null, null);
	public static FilterStruct lomo4Filter = new FilterStruct(new ColorMatrixColorFilter[]{
																	getScaleBrightFilter(1.4f, 0.95f, 0.95f),
																	//getSaturationFilter(1.3f)
																},
																null, null);

	
	public static Bitmap applyFilter(Bitmap src, FilterStruct filter)
	{
		 return applyFilterProcedure(src, filter.getColorMatrices(), filter.getMask(), filter.getMode());  
	}
	
	public static Bitmap applyFilterProcedure(Bitmap src, ColorMatrixColorFilter[] filters, 
								Bitmap mask, Xfermode mode)  
    {  
        Bitmap newb = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(newb);  
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        
        //fiters
        for(int i = 0; filters != null && i < filters.length; i++)
        {
        	if(filters[i] != null)
        	{
    	        paint.setColorFilter(filters[i]);
    	        canvas.drawBitmap(newb, 0, 0, paint);       
        	}
        }
        
        //mask
        if(mask != null)
        {
    		paint.setXfermode(mode);
    		canvas.drawBitmap(mask, new Rect(0, 0, mask.getWidth(), mask.getHeight()), 
    								new Rect(0, 0, newb.getWidth(), newb.getHeight()), paint);
        }
        
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        return newb;  
    }
	

	
	public static ColorMatrixColorFilter getSaturationFilter(float saturation)
	{
		float[] defaultArr = new float[]{1, 0, 0, 0, 0,
										 0, 1, 0, 0, 0,
										 0, 0, 1, 0, 0,
										 0, 0, 0, 1, 0};
		ColorMatrix matrix = new ColorMatrix(defaultArr);
		matrix.setSaturation(saturation);
		ColorMatrixColorFilter result = new ColorMatrixColorFilter(matrix);
		return result;
	}
	
	public static ColorMatrixColorFilter getScaleBrightFilter(float rScale, float gScale, float bScale)
	{
		float[] arr = new float[]{rScale, 0, 0, 0, 0,
						   0, gScale, 0, 0, 0,
						   0, 0, bScale, 0, 0,
						   0, 0, 0, 1, 0 };
		return new ColorMatrixColorFilter(arr);
	}
	

	
	public static ColorMatrixColorFilter getContrastFilter(float rContrast, float gContrast, float bContrast)
	{
		final int dimen = 3;
		float[] contrasts = new float[]{rContrast, gContrast, bContrast};
		float[] scales = new float[dimen];
		float[] translates = new float[dimen];
		
		for(int i = 0; i < dimen; i++)
		{
			scales[i] = contrasts[i] + 1.f;
			scales[i] *= scales[i];
	        translates[i] = (-.5f * scales[i] + .5f) * 255.f;
		}
		
        float[] arr =  new float[]{
        		scales[0], 0, 0, 0, translates[0], 
        		0, scales[1], 0, 0, translates[1], 
        		0, 0, scales[2], 0, translates[2], 
        		0, 0, 0, 1, 0};
        
        return new ColorMatrixColorFilter(arr);
	}
	
	
	
	public static void dumpArray(float[] arr)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<arr.length; i++)
		{
			sb.append(arr[i] + ",");
		}
		android.util.Log.d("FilterMask", sb.toString());
	}
}


