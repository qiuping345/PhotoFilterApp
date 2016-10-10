package com.mobpie.instagram;

import java.util.ArrayList;
import java.util.HashSet;

import com.mobpie.util.MemoryCache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class TimelineListAdapter extends BaseAdapter 
{
	
	private ArrayList<PicFeed> dataList = new ArrayList<PicFeed>();
	private Context context;
	private boolean isScrollingUp = false;  //代表是否在向上滚动
	private boolean prefetchEnabled = true;    //是否预加载图片
	private static final int PREFETCH_CNT = 3; //向前预加载图片数量。
	private HashSet<String> decodingSet = new HashSet<String>();
	
	public TimelineListAdapter(Context context)
	{
		super();
		this.context = context;
	}
	
	public TimelineListAdapter(Context context, ArrayList<PicFeed> data)
	{
		super();
		this.context = context;
		dataList = data;
	}
	
	public void setData(ArrayList<PicFeed> data)
	{
		dataList = data;
	}
	
	public void setScrollDirection(boolean isUp)
	{
		isScrollingUp = isUp;
	}
	
	public void setPrefetch(boolean enabled)
	{
		prefetchEnabled = enabled;
	}
	
	
	public void setContext(Context context)
	{
		this.context = context;
	}

	@Override
	public int getCount() 
	{
		return dataList.size();
	}

	@Override
	public Object getItem(int position) 
	{
		if(position >= 0 && position < dataList.size())
		{
			return dataList.get(position);
		}
		else
		{
			return null;
		}
	}

	@Override
	public long getItemId(int position) 
	{
		if(position >= 0 && position < dataList.size())
		{
			return position;
		}
		else
		{
			return -1;
		}		
	}
	
	private static Handler uiHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Object[] objs = (Object[]) msg.obj;
			if(objs != null && 2 == objs.length)
			{
				Bitmap bmp = (Bitmap) objs[0];
				ImageView imgView = (ImageView) objs[1];
				if(imgView != null)
				{
					if(bmp != null)
					{
						imgView.setImageBitmap(bmp);	
					}
					else
					{
						imgView.setImageResource(R.drawable.ic_launcher);
					}
				}
			}
		}
	};

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		final PicFeed item = (PicFeed)getItem(position);
		if(null == item)
		{
			return null;
		}
		else if(convertView != null)
		{
			String tag = (String) convertView.getTag();
			if(tag != null && tag.equals(item.fileKey))
			{
				return convertView;
			}
		}
		
		//和convertView不同，则需要重新创建。
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ViewGroup container = (ViewGroup)inflater.inflate(R.layout.feed_item, null);
		final ImageView imgView = (ImageView) container.findViewById(R.id.img);
		
		final MemoryCache cache = MemoryCache.instance();
		final String orgPicPath = item.getOrgPicPath();
		final String cacheKey = getCacheKey(orgPicPath, 0);
		Bitmap bmp = (Bitmap)cache.get(cacheKey);
		if(bmp != null)
		{
			imgView.setImageBitmap(cache.get(getCacheKey(orgPicPath, 0)));
		}
		else if(!decodingSet.contains(orgPicPath))  //如果没有在decode
		{
			new Thread()
			{
				public void run()
				{
					Bitmap bmp = decodeBitmap(orgPicPath);
					if(bmp != null)
					{
						cache.put(cacheKey, bmp);
					}
					Object[] objs = new Object[]{bmp, imgView};
					Message updateMsg = new Message();
					updateMsg.what = 0;
					updateMsg.obj = objs;
					uiHandler.sendMessage(updateMsg);
				}
			}.start();	
		}
		
		android.util.Log.d("onScroll", "adaper. isScrollingUp : " + isScrollingUp + ", position : " + position);
		
		if(prefetchEnabled)
		{
			for(int i = 0; i < PREFETCH_CNT; i++)
			{
				int idx = isScrollingUp ? position - i : position + i;
				if(idx >=0 && idx < getCount())
				{
					PicFeed feed = (PicFeed)getItem(position - 1);
					if(null != feed)
					{
						prefetchBitmap(feed.fileKey, 0);
						
						android.util.Log.d("onScroll", "adaper. idx : " + idx);
					    
					}
				}
			}
		}
		
		container.setTag(item.fileKey);
		return container;
	}
	
	
	private void prefetchBitmap(String path, int scale)
	{
		final String filePath = path;
		final MemoryCache cache = MemoryCache.instance();
		final String cacheKey = getCacheKey(path, scale);
		Bitmap bmp = (Bitmap)cache.get(cacheKey);
		
		if(null == bmp && !decodingSet.contains(path))
		{
			new Thread()
			{
				public void run()
				{
					Bitmap bmp = decodeBitmap(filePath);
					if(bmp != null)
					{
						cache.put(cacheKey, bmp);
					}
				}
			}.start();			
		}		
	}
	
	private Bitmap decodeBitmap(String path)
	{
		decodingSet.add(path);
		android.util.Log.d("onScroll", "decoding : " + path);
		
		Bitmap result = null;
		BitmapFactory.Options outOpts = new BitmapFactory.Options();
		outOpts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, outOpts);
		
		BitmapFactory.Options decodeOpt = new BitmapFactory.Options();
		decodeOpt.inSampleSize = 2;//4;
//		decodeOpt.inSampleSize = Math.max(outOpts.outHeight, outOpts.outWidth)
//								 / Math.min(metrics.heightPixels, metrics.widthPixels);

		try
		{
			result = BitmapFactory.decodeFile(path, decodeOpt);
		}
		catch(OutOfMemoryError oome)
		{
			System.gc();
			//TODO retry
		}
		
		decodingSet.remove(path);
		return result;
	}
	
	private String getCacheKey(String key, int scale)
	{
		return key + "_" + scale;
	}
	


}
