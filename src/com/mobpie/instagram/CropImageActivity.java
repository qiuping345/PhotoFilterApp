/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// originally from AOSP Camera code. modified to only do cropping and return 
// data to caller. Removed saving to file, MediaManager, unneeded options, etc.
package com.mobpie.instagram;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mobpie.util.ImageUtil;
import com.mobpie.widget.CropImageView;
import com.mobpie.widget.HighlightView;

/**
 * The activity can crop specific region of interest from an image.
 */
public class CropImageActivity extends Activity {
    private int mAspectX, mAspectY;

    // These options specifiy the output image size and whether we should
    // scale the output to fit it (or just crop it).
    private boolean mCircleCrop = false;
    private String   bmpFileData;
    
    boolean mCanCrop = false;
    boolean mSaving = false; // Whether the "save" button is already clicked.
    
    private CropImageView mImageView;
    private Bitmap mBitmap;
    HighlightView mCrop;
    private String storedPath = "/mnt/sdcard/"+"croppedtemp.jpg";
    
   OnClickListener listener = new OnClickListener(){
    	public void onClick(View v) {
    		switch(v.getId()){
			case R.id.discard:
			{
				setResult(RESULT_CANCELED);
				mBitmap = null;
				finish();
				break;
			}
			case R.id.save:
			{
				onSaveClicked();
				break;
			}
			default:
				break;
			}
    	}
    };


    private boolean ChangeClipFrame()
    {
    	boolean result = true;
    	mImageView.remove();
		
		HighlightView hv = new HighlightView(mImageView);

        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();

        Rect imageRect = new Rect(0, 0, width, height);
        

        // make the default size about 4/5 of the width or height
        int cropWidth = Math.min(width, height) ;
        int cropHeight = cropWidth;
   
        if (mAspectX != 0 && mAspectY != 0) {
            if (mAspectX > mAspectY) {
                cropHeight = cropWidth * mAspectY / mAspectX;
            } else {
                cropWidth = cropHeight * mAspectX / mAspectY;
            }
        }
        if(cropWidth < HighlightView.MinWidth)
        {
        	cropWidth = HighlightView.MinWidth;
        	cropHeight = cropWidth * mAspectY / mAspectX;
        	if(cropHeight>height)
        	{
//        		result = false;
        		return false;
        	}
        }
        if(cropHeight < HighlightView.MinHeight)
        {
        	cropHeight = HighlightView.MinHeight;
        	cropWidth = cropHeight * mAspectX / mAspectY;
        	if(cropWidth > width)
        	{
//        		result = false;
        		return false;
        	}
        }
        

        int x = (width - cropWidth) / 2;
        int y = (height - cropHeight) / 2;

        RectF cropRect = new RectF(x, y, x + cropWidth, y + cropHeight);
        Matrix mImageMatrix;
        mImageMatrix = mImageView.getImageMatrix();
		hv.setup(mImageMatrix, imageRect, cropRect, mCircleCrop,
                mAspectX != 0 && mAspectY != 0);
        mImageView.add(hv);
        
        mImageView.invalidate();
        mCrop = mImageView.getClip();
        mCrop.setFocus(true);
        
        return result;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);        
        Intent intent = getIntent();
        Uri orgUri = getIntent().getData();
        bmpFileData = orgUri.toString();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cropimage);
        mImageView = (CropImageView) findViewById(R.id.image);
        mImageView.setContext(this);
        mBitmap = ImageUtil.getPreviewBmp(bmpFileData, 640);
        // Make UI fullscreen.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ImageButton t_discard= (ImageButton) findViewById(R.id.discard);
        ImageButton t_save= (ImageButton) findViewById(R.id.save);
        t_discard.setOnClickListener(listener);
        t_save.setOnClickListener(listener);
        
        mAspectX = 1;
		mAspectY = 1;

		mImageView.setImageBitmapResetBase(mBitmap, true);
		mCanCrop = ChangeClipFrame();
		if(!mCanCrop)
		{
			Toast.makeText(CropImageActivity.this,  "ͼƬ̫С��", Toast.LENGTH_SHORT).show();
		}	
    }

    private void onSaveClicked() {
        // TODO this code needs to change to use the decode/crop/encode single
        // step api so that we don't require that the whole (possibly large)
        // bitmap doesn't have to be read into memory
        if (mCrop == null || !mCanCrop) {
            return;
        }

        if (mSaving)
            return;
        mSaving = true;

        new Thread(new Runnable() {
			public void run() {
				try {
			        Rect r = mCrop.getCropRect();			        
			        Bitmap croppedImage = Bitmap.createBitmap(mBitmap, r.left, r.top, r.width(), r.height());
					
			        File sFile = new File(storedPath);
			        try 
			        {
						sFile.createNewFile();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			        FileOutputStream fout=null;
			        try {
						fout=new FileOutputStream(sFile);
					} catch (Exception e) {
						// TODO: handle exception
					}
			        
			        croppedImage.compress(CompressFormat.JPEG, 100, fout);
			        try {
						fout.flush();
						fout.close();
					} catch (Exception e) {
						// TODO: handle exception
					}
					
			        Intent intent = new Intent();
					intent.setData(Uri.parse(storedPath));
			        setResult(RESULT_OK, intent);
					
					finish();
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
        }).start();
    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mBitmap != null)
        {
        	mBitmap.recycle();
        	System.gc();
        }
    }

}
