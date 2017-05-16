package com.zj.cityselectordemo.activity;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.zj.cityselectordemo.R;
import com.zj.cityselectordemo.adapter.ListAdapter;
import com.zj.cityselectordemo.adapter.ResultListAdapter;
import com.zj.cityselectordemo.db.DBHelper;
import com.zj.cityselectordemo.db.DatabaseHelper;
import com.zj.cityselectordemo.entity.City;
import com.zj.cityselectordemo.widgets.MyLetterListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class CityListActivity extends AppCompatActivity {

	private ListAdapter adapter;
	private ResultListAdapter resultAdapter;
	private ListView lvCity;
	private ListView lvResult;
	private TextView tvInitial; // 对话框首字母textview
	private FrameLayout frameLayout;
	private MyLetterListView lvLetter; // A-Z listview
	private EditText etSearch;
	private TextView tvNoResult;

	private WindowManager windowManager;
	private View view;
	private HashMap<String, Integer> alphaIndexer = new HashMap<String, Integer>();// 存放存在的汉语拼音首字母和与之对应的列表位置
	private Handler handler;
	private OverlayThread overlayThread; // 显示首字母对话框
	private ArrayList<City> allList; // 所有城市列表
	private ArrayList<City> cityList;// 城市列表
	private ArrayList<City> locationList;// 城市列表
	private ArrayList<City> hotList;// 热门城市列表
	private ArrayList<City> resultList;// 搜索结果城市列表
	private ArrayList<City> historyList;// 搜索历史城市列表
	private ArrayList<City> itemList;
	private HashMap<String,ArrayList<City>> listMap = new HashMap<String,ArrayList<City>>();
	private List<ArrayList<City>> lists = new ArrayList<ArrayList<City>>();
	private List<String> keyList = new ArrayList<String>();
	private DatabaseHelper helper;

	private AMapLocationClient locationClient = null;
	private AMapLocationClientOption locationOption = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_city_list);
		initView();
		initData();
		setListener();
		//初始化定位
		initLocation();
	}

	private void initView(){
		helper = new DatabaseHelper(this);
		lvCity = (ListView) findViewById(R.id.list_view);
		lvResult = (ListView) findViewById(R.id.search_result);
		etSearch = (EditText) findViewById(R.id.sh);
		tvNoResult = (TextView) findViewById(R.id.tv_noresult);
		lvLetter = (MyLetterListView) findViewById(R.id.MyLetterListView01);

		initOverlay();
	}

	private void initData(){
		allList = new ArrayList<City>();
		hotList = new ArrayList<City>();
		locationList = new ArrayList<City>();
		resultList = new ArrayList<City>();
		historyList = new ArrayList<City>();

		handler = new Handler();
		overlayThread = new OverlayThread();

		resultAdapter = new ResultListAdapter(this, resultList);
		lvResult.setAdapter(resultAdapter);

		locationList.add(new City("正在定位","0"));
		hotCityInit();
		historyCityInit();
		cityInit();

		adapter = new ListAdapter(this, keyList,lists);
		lvCity.setAdapter(adapter);
	}

	private void setListener(){
		lvLetter.setOnTouchingLetterChangedListener(new MyLetterListView.OnTouchingLetterChangedListener() {
			@Override
			public void onTouchingLetterChanged(String s) {
				if (alphaIndexer.get(s) != null) {
					int position = alphaIndexer.get(s);
					lvCity.setSelection(position);
					tvInitial.setText(s);
					frameLayout.setVisibility(View.VISIBLE);
					handler.removeCallbacks(overlayThread);
					// 延迟一秒后执行，让tvInitial为不可见
					handler.postDelayed(overlayThread, 1000);
				}
			}
		});
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if (s.toString() == null || "".equals(s.toString())) {
					lvLetter.setVisibility(View.VISIBLE);
					lvCity.setVisibility(View.VISIBLE);
					lvResult.setVisibility(View.GONE);
					tvNoResult.setVisibility(View.GONE);
				} else {
					resultList.clear();
					lvLetter.setVisibility(View.GONE);
					lvCity.setVisibility(View.GONE);
					getResultCityList(s.toString());
					if (resultList.size() <= 0) {
						tvNoResult.setVisibility(View.VISIBLE);
						lvResult.setVisibility(View.GONE);
					} else {
						tvNoResult.setVisibility(View.GONE);
						lvResult.setVisibility(View.VISIBLE);
						resultAdapter.notifyDataSetChanged();
					}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
		lvCity.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Toast.makeText(getApplicationContext(), allList.get(position).getName(), Toast.LENGTH_SHORT).show();
				if (position > 2) {
					insertCity(allList.get(position).getName());
				}
			}
		});
		lvResult.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Toast.makeText(getApplicationContext(), resultList.get(position).getName(), Toast.LENGTH_SHORT).show();
				insertCity(resultList.get(position).getName());
			}
		});
	}

	private void insertCity(String name) {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from recentcity where name = '" + name + "'", null);
		if (cursor.getCount() > 0) { //
			db.delete("recentcity", "name = ?", new String[] { name });
		}
		db.execSQL("insert into recentcity(name, date) values('" + name + "', " + System.currentTimeMillis() + ")");
		db.close();
	}

	private void cityInit() {
		City city = new City("定位", "0"); // 当前定位城市
		allList.add(city);
		city = new City("最近", "1"); // 最近访问的城市
		allList.add(city);
		city = new City("热门", "2"); // 热门城市
		allList.add(city);
		cityList = getCityList();
		allList.addAll(cityList);
		for (int i = 0; i < allList.size(); i++) {
			// 当前汉语拼音首字母
			String currentStr = getAlpha(allList.get(i).getPinyi());
			keyList.add(currentStr);
		}
		removeDuplicateWithOrder(keyList);

		for (int i = 0; i < allList.size(); i++) {
			// 当前汉语拼音首字母
			String currentStr = getAlpha(allList.get(i).getPinyi());
			if (listMap.get(currentStr) == null){
				listMap.put(currentStr,new ArrayList<City>());
			}else {
				itemList = listMap.get(currentStr);
				City city1 = allList.get(i);
				itemList.add(city1);
				listMap.put(currentStr, itemList);
			}
		}

		for (int i = 0; i < keyList.size(); i++) {
			String key = keyList.get(i);
			if ("定位".equals(key)){
				lists.add(locationList);
			}else if ("最近".equals(key)){
				lists.add(historyList);
			}else if ("热门".equals(key)){
				lists.add(hotList);
			}else{
				lists.add(listMap.get(key));
			}
			alphaIndexer.put(keyList.get(i),i);
		}
	}

	/**
	 * 热门城市
	 */
	public void hotCityInit() {
		City city = new City("上海", "2");
		hotList.add(city);
		city = new City("北京", "2");
		hotList.add(city);
		city = new City("广州", "2");
		hotList.add(city);
		city = new City("深圳", "2");
		hotList.add(city);
		city = new City("武汉", "2");
		hotList.add(city);
		city = new City("天津", "2");
		hotList.add(city);
		city = new City("西安", "2");
		hotList.add(city);
		city = new City("南京", "2");
		hotList.add(city);
		city = new City("杭州", "2");
		hotList.add(city);
		city = new City("成都", "2");
		hotList.add(city);
		city = new City("重庆", "2");
		hotList.add(city);
	}

	/**
	 * 搜索历史城市
	 */
	private void historyCityInit() {
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor cursor = db.rawQuery("select * from recentcity order by date desc limit 0, 9", null);
		while (cursor.moveToNext()) {
			City city = new City(cursor.getString(1),cursor.getString(2));
			historyList.add(city);
		}
		cursor.close();
		db.close();
	}

	@SuppressWarnings("unchecked")
	private ArrayList<City> getCityList() {
		DBHelper dbHelper = new DBHelper(this);
		ArrayList<City> list = new ArrayList<City>();
		try {
			dbHelper.createDataBase();
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor cursor = db.rawQuery("select * from city", null);
			City city;
			while (cursor.moveToNext()) {
				city = new City(cursor.getString(1), cursor.getString(2));
				list.add(city);
			}
			cursor.close();
			db.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(list, comparator);
		return list;
	}

	@SuppressWarnings("unchecked")
	private void getResultCityList(String keyword) {
		DBHelper dbHelper = new DBHelper(this);
		try {
			dbHelper.createDataBase();
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			Cursor cursor = db.rawQuery(
					"select * from city where name like \"%" + keyword
							+ "%\" or pinyin like \"%" + keyword + "%\"", null);
			City city;
			Log.e("info", "length = " + cursor.getCount());
			while (cursor.moveToNext()) {
				city = new City(cursor.getString(1), cursor.getString(2));
				resultList.add(city);
			}
			cursor.close();
			db.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Collections.sort(resultList, comparator);
	}

	/**
	 * a-z排序
	 */
	@SuppressWarnings("rawtypes")
    Comparator comparator = new Comparator<City>() {
		@Override
		public int compare(City lhs, City rhs) {
			String a = lhs.getPinyi().substring(0, 1);
			String b = rhs.getPinyi().substring(0, 1);
			int flag = a.compareTo(b);
			if (flag == 0) {
				return a.compareTo(b);
			} else {
				return flag;
			}
		}
	};

	// 初始化汉语拼音首字母弹出提示框
	private void initOverlay() {
		LayoutInflater inflater = LayoutInflater.from(this);
		view = inflater.inflate(R.layout.overlay, null);
		tvInitial = (TextView)view.findViewById(R.id.tv_title);
		frameLayout = (FrameLayout)view.findViewById(R.id.fl_bg);
		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
				300, LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSLUCENT);
		windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		windowManager.addView(view, lp);
	}

	// 获得汉语拼音首字母
	private String getAlpha(String str) {
		if (str == null) {
			return "#";
		}
		if (str.trim().length() == 0) {
			return "#";
		}
		char c = str.trim().substring(0, 1).charAt(0);
		// 正则表达式，判断首字母是否是英文字母
		Pattern pattern = Pattern.compile("^[A-Za-z]+$");
		if (pattern.matcher(c + "").matches()) {
			return (c + "").toUpperCase();
		} else if (str.equals("0")) {
			return "定位";
		} else if (str.equals("1")) {
			return "最近";
		} else if (str.equals("2")) {
			return "热门";
		}else {
			return "#";
		}
	}

	private void removeDuplicateWithOrder(List<String> list)  {
		Set set  =   new HashSet();
		List newList  =   new  ArrayList();
		for  (Iterator iter = list.iterator(); iter.hasNext();)  {
			Object element  =  iter.next();
			if  (set.add(element))
				newList.add(element);
		}
		list.clear();
		list.addAll(newList);
	}

	// 设置tvInitial不可见
	private class OverlayThread implements Runnable {
		@Override
		public void run() {
			frameLayout.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化定位
	 */
	private void initLocation(){
		//初始化client
		locationClient = new AMapLocationClient(this.getApplicationContext());
		locationOption = new AMapLocationClientOption();
		locationOption.setOnceLocation(true);
		locationOption.setOnceLocationLatest(true);
		//设置定位参数
		locationClient.setLocationOption(locationOption);
		locationClient.startLocation();
		// 设置定位监听
		locationClient.setLocationListener(new AMapLocationListener() {
			@Override
			public void onLocationChanged(AMapLocation aMapLocation) {
				if (null != aMapLocation) {
					//errCode等于0代表定位成功，其他的为定位失败，具体的可以参照官网定位错误码说明
					if(aMapLocation.getErrorCode() == 0){
						locationList.clear();
						locationList.add(0,new City(aMapLocation.getCity().substring(0,aMapLocation.getCity().length() - 1),"0"));
					} else {
						//定位失败
						locationList.clear();
						locationList.add(0,new City("定位失败","0"));
					}
				} else {
					locationList.clear();
					locationList.add(0,new City("定位失败","0"));
				}
				lists.remove(0);
				lists.add(0,locationList);
				adapter.notifyDataSetChanged();
			}
		});
	}

	/**
	 * 销毁定位
	 */
	private void destroyLocation(){
		if (null != locationClient) {
			/**
			 * 如果AMapLocationClient是在当前Activity实例化的，
			 * 在Activity的onDestroy中一定要执行AMapLocationClient的onDestroy
			 */
			locationClient.onDestroy();
			locationClient = null;
			locationOption = null;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		destroyLocation();
		windowManager.removeViewImmediate(view);
	}
}