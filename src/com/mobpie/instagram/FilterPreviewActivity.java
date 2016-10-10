package com.mobpie.instagram;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.mobpie.filter.FilterMask;
import com.mobpie.util.ImageUtil;
import com.mobpie.util.Util;
import com.mobpie.widget.HorizontalListView;

public class FilterPreviewActivity extends Activity
	implements ViewSwitcher.ViewFactory, AdapterView.OnItemClickListener
{
	
	private ImageView imgView;
	private ProgressDialog pd;
	private ImageSwitcher mSwitcher;
	private String orgPath;
	
	public static final int MSG_SHOW_PROGRESS_DLG = 0;
	public static final int MSG_HIDE_PROGRESS_DLG = 1;
	
	
	public static final int FILTER_ORG = 0;
	public static final int FILTER_BLACK_WIHTE = 1;
	public static final int FILTER_TWO = 2;
	public static final int FILTER_THREE = 3;
	public static final int FILTER_FOUR = 4;
	public static final int FILTER_FIVE = 5;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_preview);

        Uri orgUri = getIntent().getData();
        orgPath = orgUri.toString();
        mSwitcher = (ImageSwitcher) findViewById(R.id.switcher);
        mSwitcher.setFactory(this);
        mSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in));
        mSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out));
        
        mSwitcher.setImageDrawable(new BitmapDrawable(ImageUtil.getPreviewBmp(orgPath, 640)));
        //mSwitcher.setImageURI(orgUri);

//        Gallery g = (Gallery) findViewById(R.id.filter_gallery);
//        g.setAdapter(FiltersAdapter);
//        g.setOnItemClickListener(this);
//        g.setCallbackDuringFling(false);

        HorizontalListView g = (HorizontalListView)findViewById(R.id.filter_gallery);
//        GridView g = (GridView) findViewById(R.id.filter_gallery);
        g.setAdapter(FiltersAdapter);
        g.setOnItemClickListener(this);
        //g.setCallbackDuringFling(false);
	    
	    pd = new ProgressDialog(this);
	    pd.setTitle("应用滤镜");
	    pd.setMessage("正在计算，请稍候");
	    pd.setCancelable(false);
	    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    
    }
	
	private void save(Bitmap bmp, String toPath)
	{
        File f = new File(toPath);
        try
        {
        	if(!f.exists())
        	{
        		f.createNewFile();
        	}
        	
            FileOutputStream fos = new FileOutputStream(f);
            bmp.compress(CompressFormat.JPEG, 65, fos);            
 
        }
        catch(IOException ioe)
        {
        	ioe.printStackTrace();
        }

	}
	
	private void copyExifInfo(String fromPath, String toPath)
	{
		try
		{
	        ExifInterface orgExif = new ExifInterface(fromPath);
	        ExifInterface toExif = new ExifInterface(toPath);
	        
	        String[] tagsArray = new String[]{ExifInterface.TAG_DATETIME, 
	        		ExifInterface.TAG_FLASH,
	        		ExifInterface.TAG_GPS_LATITUDE,
	        		ExifInterface.TAG_GPS_LATITUDE_REF,	
	        		ExifInterface.TAG_GPS_LONGITUDE,
	        		ExifInterface.TAG_GPS_LONGITUDE_REF,	
	        		ExifInterface.TAG_MAKE,	
	        		ExifInterface.TAG_MODEL,
	        		//ExifInterface.TAG_ORIENTATION,
	        		ExifInterface.TAG_WHITE_BALANCE};
	        
	        for(int i = 0; i < tagsArray.length; i++)
	        {
	        	String val = orgExif.getAttribute(tagsArray[i]);
	        	if(val != null && !"".equals(val))
	        	{
	        		toExif.setAttribute(tagsArray[i], val);
	        	}
	        }
	        
	        toExif.saveAttributes();
		}
		catch(IOException ioe)
		{
			ioe.printStackTrace();
		}
	}
	
	
	

	
	
	int[]    optionsArray = new int[]{FILTER_ORG, FILTER_BLACK_WIHTE, FILTER_TWO, FILTER_THREE, FILTER_FOUR, FILTER_FIVE};	
	String[] optionsTags = new String[]{"原图","黑白", "滤镜2", "滤镜3", "滤镜4", "滤镜5"}; 
	int[]    optionsIconId = new int[]{R.drawable.filter_icon_ori, R.drawable.filter_icon_bw, R.drawable.filter_icon_soft, 
									   R.drawable.filter_icon_notice, R.drawable.filter_icon_redlomo, R.drawable.filter_icon_coldcolor};
	
	BaseAdapter FiltersAdapter = new BaseAdapter()
	{

		public int getCount() 
		{
			return optionsArray.length;
		}

		public Object getItem(int position) 
		{
			return optionsArray[position];
		}

		public long getItemId(int position) 
		{
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) 
		{
			int tagConvert  = -1;
			if(convertView != null)
			{
				tagConvert = ((Integer)convertView.getTag()).intValue();
			}
			
			if(optionsArray[position] == tagConvert)
			{
				return convertView;
			}
			else
			{
				LayoutInflater layoutInflater = (LayoutInflater)FilterPreviewActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
				ViewGroup itemLayout = (ViewGroup)layoutInflater.inflate(R.layout.filter_sel_item, null);
				ImageView icon = (ImageView)itemLayout.findViewById(R.id.icon);
				TextView text = (TextView)itemLayout.findViewById(R.id.text);
				
				icon.setImageResource(optionsIconId[position]);
				text.setText(optionsTags[position]);
				itemLayout.setTag(optionsArray[position]);
				return itemLayout;
			}
		}
	};
	
	final Handler uiHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Bitmap bmp = (Bitmap)msg.obj;
			mSwitcher.setImageDrawable(new BitmapDrawable(bmp));
			mSwitcher.invalidate();
		}
	};
	
	Handler pdHandler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
			case MSG_SHOW_PROGRESS_DLG :
				pd.show();
				break;
			case MSG_HIDE_PROGRESS_DLG:
				pd.dismiss();
				break;
			default:
				break;
			}
		}
	};

	@Override
	public View makeView() 
	{
		// TODO Auto-generated method stub
		ImageView imgView = new ImageView(this);
		imgView.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.FILL_PARENT,
														ImageSwitcher.LayoutParams.FILL_PARENT));
		return imgView;
	}

	/**
	 * gallery中的item被点中时的响应事件。
	 */
	public void onItemClick(AdapterView parent, View view, int position, long id) 
	{
		//pdHandler.sendEmptyMessage(MSG_SHOW_PROGRESS_DLG);		
		//final Bitmap orgBmp = BitmapFactory.decodeFile(orgPath);
		final Bitmap orgBmp = ImageUtil.getPreviewBmp(orgPath, 640);
		final int filterIdx = ((Integer)view.getTag()).intValue();
		
		new Thread()
		{
			public void run()
			{
				Bitmap resultBmp = null;
				
				switch (filterIdx) {
				case FILTER_ORG:
					resultBmp = orgBmp;
					break;
				case FILTER_BLACK_WIHTE:
					resultBmp = FilterMask.applyFilter(orgBmp, FilterMask.blackWhiteFilter);
					break;
				case FILTER_TWO:
					resultBmp = FilterMask.applyFilter(orgBmp, FilterMask.lomoFilter);
					break;
				case FILTER_THREE:
					resultBmp = FilterMask.applyFilter(orgBmp, FilterMask.lomo2Filter);
					break;
				case FILTER_FOUR:
					resultBmp = FilterMask.applyFilter(orgBmp, FilterMask.lomo3Filter);
					break;
				case FILTER_FIVE:
					resultBmp = FilterMask.applyFilter(orgBmp, FilterMask.lomo4Filter);
					break;
				default:
					break;
				}

				String toPath = "/mnt/sdcard/phototext.jpg";
				//save(resultBmp, toPath);
				//copyExifInfo(orgPath, toPath);
				
				Message msg = uiHandler.obtainMessage(0, resultBmp);
				uiHandler.sendMessage(msg);
				pdHandler.sendEmptyMessage(MSG_HIDE_PROGRESS_DLG);				
			}
		}.start();
	}	
}