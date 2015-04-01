package com.scut.picturelibrary.adapter;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.scut.picturelibrary.R;

public class PhotoWallAdapter extends CursorAdapter {

	public PhotoWallAdapter(Context context, Cursor c) {
		super(context, c, true);
	}

	@Override
	public void bindView(View v, Context context, Cursor cursor) {
		if (cursor == null)
			return;
		String path = cursor.getString(cursor
				.getColumnIndex(MediaStore.Images.Media.DATA));
		ImageView photo = (ImageView) v.getTag();
		// 使用外部库ImageLoader进行图片缓存和异步加载显示
		ImageLoader.getInstance().displayImage("file:///" + path, photo);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View v = inflater.inflate(R.layout.grid_item, parent, false);
		ImageView photo = (ImageView) v.findViewById(R.id.img_grid_item_photo);
		v.setTag(photo);
		return v;
	}

	/**
	 * 获取要显示的图片的路径
	 * 
	 * @param index
	 * @return
	 */
	public String getPath(int index) {
		Cursor c = getCursor();
		c.moveToPosition(index);
		return c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
	}
}