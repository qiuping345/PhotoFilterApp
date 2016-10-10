package com.mobpie.util;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.graphics.Bitmap;
import android.util.Log;

public class MemoryCache {
	private static MemoryCache instance;
    private static final String TAG = "MemoryCache";
    private static Map<String, Bitmap> cache=Collections.synchronizedMap(
            new LinkedHashMap<String, Bitmap>(10,1.5f,true));//Last argument true for LRU ordering
    private static long size=0;//current allocated size
    private static long limit=5000000;//max memory in bytes

    private MemoryCache(){
        //use 25% of available heap size
        setLimit(Runtime.getRuntime().maxMemory()/4);
    }
    
    public static MemoryCache instance()
    {
    	if(null == instance)
    	{
    		instance = new MemoryCache();
    	}
    	
    	return instance;
    }
    
    public static void setLimit(long new_limit){
        instance.limit=new_limit;
        Log.i(TAG, "MemoryCache will use up to "+instance.limit/1024./1024.+"MB");
    }

    public static Bitmap get(String id){
        try{
            if(!cache.containsKey(id))
            {
                return null;
            }
            else
            {
	            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
	            return cache.get(id);
            }
        }catch(NullPointerException ex){
            ex.printStackTrace();
            return null;
        }
    }

    public static void put(String id, Bitmap bitmap){
        try
        {
            if(cache.containsKey(id))
            {
                size-=getSizeInBytes(cache.get(id));
            }
            
            cache.put(id, bitmap);
            size+=getSizeInBytes(bitmap);
            checkSize();
        }
        catch(Throwable th)
        {
            th.printStackTrace();
        }
    }
    
    private static void checkSize() {
        Log.i(TAG, "cache size="+size+" length="+cache.size());
        if(size>limit){
            Iterator<Entry<String, Bitmap>> iter=cache.entrySet().iterator();//least recently accessed item will be the first one iterated  
            while(iter.hasNext()){
                Entry<String, Bitmap> entry=iter.next();
                size-=getSizeInBytes(entry.getValue());
                iter.remove();
                
                if(size<=limit)
                {
                    break;
                }
            }
            Log.i(TAG, "Clean cache. New size "+cache.size());
        }
    }

    public static void clear() {
        try
        {
            //NullPointerException sometimes happen here http://code.google.com/p/osmdroid/issues/detail?id=78 
            cache.clear();
            size=0;
        }
        catch(NullPointerException ex)
        {
            ex.printStackTrace();
        }
    }

    private static long getSizeInBytes(Bitmap bitmap) 
    {
        if(bitmap==null)
        {
            return 0;
        }
        else
        {
        	return bitmap.getRowBytes() * bitmap.getHeight();
        }
    }
}