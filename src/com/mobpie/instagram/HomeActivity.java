package com.mobpie.instagram;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import com.mobpie.util.ImageUtil;
import com.mobpie.util.MemoryCache;
import com.mobpie.util.Util;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;

public class HomeActivity extends Activity {
    /** Called when the activity is first created. */
	private Button btnSnapShot;   //* 拍照上传图片的按钮
	private Button btnAlbum;      //* 上传相册中的图片按钮
	private ListView feedList;    //* 显示图片feed的list
	
	private static final int REQ_IMG_ALBUM = 0;
	private static final int REQ_IMG_SNAPSHOT = 1;
	private static final int REQ_FILTER_PREVIEW = 2;
	private static final int REQ_CROP_IMAGE = 3;
	
	private String snapshotOutputPath;
	private int mLastFirstVisibleItem = 0;
	
	//测试用的folder，图片都是从整个目录读取出来的
	private String picFolder = "/mnt/sdcard/Pictures/Instagram";
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MemoryCache.instance().setLimit(5*1024*1024);  //2M
        
        btnSnapShot = (Button) findViewById(R.id.snapshot);
        btnAlbum = (Button) findViewById(R.id.album);
        feedList = (ListView) findViewById(R.id.list);
        
        btnSnapShot.setOnClickListener(enterSnapshotListener);
        btnAlbum.setOnClickListener(enterAlbumListner);
        
        final TimelineListAdapter timelineAdapter = new TimelineListAdapter(this, makeFakeData());
        timelineAdapter.setPrefetch(true);
        feedList.setAdapter(timelineAdapter);
        feedList.setOnScrollListener(new AbsListView.OnScrollListener(){

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				//do nothing.				
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if (view.getId() == feedList.getId()) {
					final int currentFirstVisibleItem = feedList.getFirstVisiblePosition();
					boolean isScrollingUp = false;
				    if (currentFirstVisibleItem > mLastFirstVisibleItem) {
				    	isScrollingUp = false;
				    } else if (currentFirstVisibleItem < mLastFirstVisibleItem) {
				    	isScrollingUp = true;
				    }
				    
				    
				    android.util.Log.d("onScroll", "isScrollingUp : " + isScrollingUp + ", curr: " + currentFirstVisibleItem + ", last: " + mLastFirstVisibleItem);
				    timelineAdapter.setScrollDirection(isScrollingUp);

				    mLastFirstVisibleItem = currentFirstVisibleItem;
				}				
			}
        	
        });
        
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
    	if(resultCode != RESULT_OK)
    	{
    		return;
    	}
    	
    	Uri uri = null;
    	switch(requestCode)
    	{
    	case REQ_IMG_ALBUM:
    		if(data != null)
    		{
	    		uri = data.getData();
	    		String path = Util.getRealPathFromContentURI(this, uri);
	    		
	    		if(validAspectRatio(path))
	    		{
	    			enterFilterActivity(path);
	    		}
	    		else
	    		{
	    			enterCropImageActivity(path);
	    		}
    		}
    		break;
    	case REQ_IMG_SNAPSHOT:
    		if(validAspectRatio(snapshotOutputPath))
    		{
    			enterFilterActivity(snapshotOutputPath);
    		}
    		else
    		{
    			enterCropImageActivity(snapshotOutputPath);
    		}
    		break;
    	case REQ_CROP_IMAGE:
    		if(data != null)
    		{
	    		uri = data.getData();
	    		String path = uri.toString();
	    		enterFilterActivity(path);
    		}
    		break;
    	default:
    		break;
    	}
    
    }
    
    private void enterFilterActivity(String path)
    {
    	Intent intent = new Intent(HomeActivity.this, FilterPreviewActivity.class);
    	intent.setData(Uri.parse(path));
    	startActivityForResult(intent, REQ_FILTER_PREVIEW);
    }
    
    private void enterCropImageActivity(String path)
    {
    	Intent intent = new Intent(HomeActivity.this, CropImageActivity.class);
    	intent.setData(Uri.parse(path));
    	startActivityForResult(intent, REQ_CROP_IMAGE);
    }
    
    View.OnClickListener enterSnapshotListener = new View.OnClickListener() 
    {
		public void onClick(View v) 
		{
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File outFile = new File(Environment.getExternalStorageDirectory(), "camera.jpg");
            snapshotOutputPath = outFile.getAbsolutePath();
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile.getAbsoluteFile()));
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 100);
            startActivityForResult(intent, REQ_IMG_SNAPSHOT);
		}
	};
    
    View.OnClickListener enterAlbumListner = new View.OnClickListener() 
    {
		public void onClick(View v) 
		{
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, REQ_IMG_ALBUM);
		}
	};
	
	/**
	 * 检查图片宽高比是否是1:1
	 * @param path  图片路径。
	 * @return  true，图片宽高1:1； false，不是1:1
	 */
	private boolean validAspectRatio(String path)
	{
		
		BitmapFactory.Options opt = new BitmapFactory.Options();
		opt.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opt);
		return Math.abs(opt.outHeight - opt.outWidth) <= 1;
	}
	
	public void onConfigurationChanged (Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}
	
	
	public ArrayList<PicFeed> makeFakeData()
	{
		ArrayList<PicFeed> dataSet = new ArrayList<PicFeed>();
		File folder = new File(picFolder);
		String[] fileNames = folder.list();
		
		for(int i = 0; i < fileNames.length; i++)
		{
			String lowerStr = fileNames[i].toLowerCase();
			if(lowerStr.endsWith(".jpg") || lowerStr.endsWith(".png"))
			{
				PicFeed feed = new PicFeed();
				feed.fileKey = picFolder + "/" + fileNames[i];
				dataSet.add(feed);
			}
		}
		
		return dataSet;	
	}
	
}