package com.mobpie.filter;

import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Xfermode;

public class FilterStruct 
{
	public ColorMatrixColorFilter[] colorMatrices;
	public Bitmap    mask;
	public Xfermode  mode;
	
	public FilterStruct(ColorMatrixColorFilter[] matrices, Bitmap mask, Xfermode mode)
	{
		colorMatrices = matrices;
		this.mask = mask;
		this.mode = mode;
	}
	
	public ColorMatrixColorFilter[] getColorMatrices() 
	{
		return colorMatrices;
	}
	
	public Bitmap getMask() 
	{
		return mask;
	}
	
	public Xfermode getMode() 
	{
		return mode;
	}
}
