package com.zj.cityselectordemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	// 类没有实例化,是不能用作父类构造器的参数,必须声明为静态
	private static final String name = "select_cities";// 数据库名称
	private static final int version = 3; // 数据库版本

	public DatabaseHelper(Context context) {
		super(context, name, null, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.e("info", "create table");
		db.execSQL("CREATE TABLE IF NOT EXISTS city (id integer primary key autoincrement, name varchar(40), pinyin varchar(40), date INTEGER)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}
}
