package com.zj.cityselectordemo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.cityselectordemo.R;
import com.zj.cityselectordemo.entity.City;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Created by zhangjian on 2017/5/15.
 */

public class ListAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private List<ArrayList<City>> lists;
    private List<String> keyList;

    public ListAdapter(Context context, List<String> keyList, List<ArrayList<City>> lists) {
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.keyList = keyList;
        this.lists = lists;
    }

    @Override
    public int getCount() {
        return lists.size();
    }

    @Override
    public Object getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    ViewHolder holder;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.recent_city, null);
            holder = new ViewHolder();
            holder.alpha = (TextView) convertView.findViewById(R.id.recentHint);
            holder.name = (GridView) convertView.findViewById(R.id.recent_city);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.alpha.setText(keyList.get(position));
        holder.name.setAdapter(new ResultListAdapter(context, lists.get(position)));
        return convertView;
    }

    private class ViewHolder {
        TextView alpha; // 首字母标题
        GridView name; // 城市名字
    }
}
