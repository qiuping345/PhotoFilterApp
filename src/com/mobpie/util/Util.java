package com.mobpie.util;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class Util 
{
    public static String getRealPathFromContentURI(Activity act, Uri contentUri) 
    {
        String scheme = contentUri.getScheme();
        String result = "";
        
        if (ContentResolver.SCHEME_FILE.equals(scheme)) 
        {
            result = contentUri.getPath();
        } 
        else if(ContentResolver.SCHEME_CONTENT.equals(scheme))
        {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = act.managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            result = cursor.getString(column_index);
        }
        
        return result;
    }

}

