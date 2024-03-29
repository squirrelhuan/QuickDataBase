package cn.demomaster.quickdatabaselibrary.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.demomaster.quickdatabaselibrary.adapter.TabAttrs;
import cn.demomaster.quickdatabaselibrary.adapter.TableAdapter;
import cn.demomaster.quickdatabaselibrary.annotation.DBTable;
import cn.demomaster.quickdatabaselibrary.annotation.SQLObj;
import cn.demomaster.quickdatabaselibrary.model.TableColumn;
import cn.demomaster.quickdatabaselibrary.model.TableInfo;
import cn.demomaster.quickdatabaselibrary.model.TableItem;

public class TableView extends HorizontalScrollView implements TabAttrs {
    public TableView(@NonNull Context context) {
        super(context);
        init();
    }

    public TableView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public TableView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    LinearLayout linearLayout;
    TableTitleRow tableTitleRow;
    TableRecyclerView bodyRecyclerView;

    public void init() {
        setOverScrollMode(OVER_SCROLL_NEVER);
        if (linearLayout == null) {
            linearLayout = new LinearLayout(getContext());
            addView(linearLayout);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            tableTitleRow = new TableTitleRow(getContext());
            //tableTitleRow.setOverScrollMode(OVER_SCROLL_NEVER);
            linearLayout.addView(tableTitleRow);
            bodyRecyclerView = new TableRecyclerView(getContext());
            bodyRecyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            bodyRecyclerView.setOverScrollMode(OVER_SCROLL_NEVER);
            linearLayout.addView(bodyRecyclerView);
        }

        initAdapter();
    }

    String[] mTitles;

    public void setTitles(String[] titles) {
        this.mTitles = titles;
        initTabTitle(titles);
    }

    int[] tabWidths;

    private void initTabTitle(String[] titles) {
        if (tableTitleRow.getChildCount() == titles.length) {
            for (int i = 0; i < titles.length; i++) {
                TextView textView = (TextView) tableTitleRow.getChildAt(i);
                textView.setText(titles[i]);
            }
        } else {
            tableTitleRow.removeAllViews();
            tabWidths = new int[titles.length];
            for (int i = 0; i < titles.length; i++) {
                TextView textView = new TextView(getContext());
                textView.setText(titles[i]);
                textView.setTextColor(Color.BLACK);
                textView.setBackgroundColor(Color.GREEN);
                textView.setPadding(20, 20, 20, 20);
                tabWidths[i] = 300;
                ViewGroup.LayoutParams layoutParams = new TableRow.LayoutParams(300, ViewGroup.LayoutParams.WRAP_CONTENT);
                textView.setLayoutParams(layoutParams);
                tableTitleRow.addView(textView);
            }

            tableTitleRow.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    tableTitleRow.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    tableAdapter.notifyDataSetChanged();
                }
            });
        }

    }

    TableAdapter tableAdapter = null;

    public void initAdapter() {
        tableAdapter = new TableAdapter(getContext(), this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        //设置布局管理器
        //设置为垂直布局，这也是默认的
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        bodyRecyclerView.setLayoutManager(layoutManager);
        //设置Adapter
        bodyRecyclerView.setAdapter(tableAdapter);
    }

    @Override
    public int getTabWidth(int index) {
        return tabWidths[index];
    }

    @Override
    public int getTabCount() {
        return tabWidths.length;
    }

    public void setData1(List<String[]> stringList) {
        tableAdapter.updateList(this, stringList);
        // bodyRecyclerView.setAdapter(tableAdapter);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        boolean tResult = super.onTouchEvent(ev);
        if (tResult) {
            bodyRecyclerView.dispatchTouchEvent(ev);
        }
        return tResult;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //return super.onInterceptTouchEvent(ev);
        return true;
    }

    public <T> void setData(List<T> modelList) {
        List<String[]> stringList = new ArrayList<>();
        if (modelList != null&&modelList.size()>0) {
            Class<?> clz1 = modelList.get(0).getClass();
            DBTable dbTable = clz1.getAnnotation(DBTable.class);
            if (dbTable == null) {
                System.out.println("类中未找到数据表的相关注解:");
                return;
            }
            String tableName = dbTable.name();
            if (TextUtils.isEmpty(tableName)) {
                tableName = clz1.getName();
            }

            TableInfo tableInfo = new TableInfo();
            tableInfo.setTableName(tableName);
            List<TableColumn> tableColumns = new ArrayList<>();
            try {
                for (Field field : clz1.getDeclaredFields()) {
                    Annotation[] anns = field.getDeclaredAnnotations();
                    if (anns.length <= 0) {
                        continue;
                    }
                    String columnName = null;
                    for (Annotation annotation : anns) {
                        TableColumn tableColumn = new TableColumn();
                        if (annotation instanceof SQLObj) {
                            SQLObj sqlObj = (SQLObj) annotation;
                            tableColumn.setSqlObj(sqlObj);
                            if (sqlObj.name().length() < 1) {
                                columnName = field.getName();
                            } else {
                                columnName = sqlObj.name();
                            }
                        }
                        boolean accessFlag = field.isAccessible();
                        field.setAccessible(true);

                        tableColumn.setName(columnName);
                    /*if (obj != null && !(obj instanceof String)) {
                        tableColumn.setValueObj(field.get(obj));
                    }*/
                        tableColumn.setField(field);
                        tableColumns.add(tableColumn);
                        field.setAccessible(accessFlag);
                    }
                }
                tableInfo.setTableColumns(tableColumns);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //字段排序
            tableColumns = sortColumn(tableColumns);

            for (int i = 0; i < modelList.size(); i++) {
                T model = modelList.get(i);
                String[] columnStrs = new String[tableColumns.size()];
                for (int j = 0; j < columnStrs.length; j++) {
                    Field field = tableColumns.get(j).getField();
                    try {
                        boolean accessFlag = field.isAccessible();
                        field.setAccessible(true);
                        columnStrs[j] = field.get(model) + "";
                        field.setAccessible(accessFlag);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                stringList.add(columnStrs);
            }

            mTitles = new String[tableColumns.size()];
            for (int i = 0; i < tableColumns.size(); i++) {
                mTitles[i] = tableColumns.get(i).getName();
            }
        }
        initTabTitle(mTitles);
        setData1(stringList);
    }

    public void setData3(List<TableItem> tableItemList) {
        List<String[]> stringList = new ArrayList<>();
        if (tableItemList != null&&tableItemList.size()>0) {
            for (int i = 0; i < tableItemList.size(); i++) {
                String[] columnStrs = new String[tableItemList.get(i).size()];
                for (int j = 0;j < columnStrs.length; j++) {
                    columnStrs[j]=tableItemList.get(i).get(j).getValueObj()+"";
                }
                stringList.add(columnStrs);
            }
        }
        initTabTitle(mTitles);
        setData1(stringList);
    }
    public <T> void setData2(List<String[]> modelList) {
        List<String[]> stringList = modelList;
        initTabTitle(mTitles);
        setData1(stringList);
    }

    /**
     * 字段排序
     *
     * @param tableColumns
     * @return
     */
    private List<TableColumn> sortColumn(List<TableColumn> tableColumns) {
        LinkedHashMap<String, TableColumn> map = new LinkedHashMap<>();
        for (TableColumn tableColumn : tableColumns) {
            map.put(tableColumn.getName(), tableColumn);
        }

        //把id排序在最左边
        if (map.containsKey("id")) {
            LinkedHashMap<String, TableColumn> map2 = new LinkedHashMap<>();
            TableColumn tableColumn = map.get("id");
            map2.put("id", tableColumn);
            map2.putAll(map);
            map = map2;
        }

        List<TableColumn> tableColumns_c = new ArrayList<>();
        for (Map.Entry entry : map.entrySet()) {
            tableColumns_c.add((TableColumn) entry.getValue());
        }

        return tableColumns_c;
    }

}
