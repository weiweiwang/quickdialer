package weiweiwang.github.quickdialer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import weiweiwang.github.search.AbstractSearchService;
import weiweiwang.github.search.SearchCallback;
import weiweiwang.github.search.SearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DialerActivity extends Activity implements AdapterView.OnItemClickListener
{
    private static final String TAG = DialerActivity.class.getSimpleName();
    private static final int MAX_HITS=20;
    /** Called when the activity is first created. */
    private AbstractSearchService searchService;
    private TextView mInput;
    private ListView list;
    private ResultAdapter resultAdapter=new ResultAdapter();
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mInput = (TextView) findViewById(R.id.input);
        list=((ListView)findViewById(R.id.list));
        list.setAdapter(resultAdapter);
        list.setOnItemClickListener(this);
        searchService= SearchService.getInstance(this);
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        Map<String, Object> searchRes = (Map<String, Object>) list.getAdapter().getItem(position);
        if(searchRes.containsKey(SearchService.FIELD_NUMBER))
        {
            call(searchRes.get(SearchService.FIELD_NUMBER).toString());
        }
    }

    private void call(String number) {
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.fromParts("tel", number, null));
        startActivity(intent);
    }
    public void onBtnClick(View view) {
        switch (view.getId()) {
            case R.id.btnDel:
                delChar();
                break;
            default:
                addChar(((TextView) view).getText().toString());
        }
        String searchText = mInput.getText().toString();
        if (TextUtils.isEmpty(searchText)) {
            search(null);
        } else if ("*#*".equals(searchText)) {
            searchService.asyncRebuild(true);
        } else if (searchText.indexOf('*') == -1) {
            search(searchText);
        }
    }
    private void addChar(String c) {
        c = c.toLowerCase(Locale.CHINA);
        mInput.setText(mInput.getText() + String.valueOf(c.charAt(0)));
    }
    private void delChar() {
        String text = mInput.getText().toString();
        if (text.length() > 0) {
            text = text.substring(0, text.length() - 1);
            mInput.setText(text);
        }
    }
    private void search(String query) {
        SearchCallback searchCallback = new SearchCallback() {
            private long start=System.currentTimeMillis();
            @Override
            public void onSearchResult(String query, long hits,
                                       final List<Map<String, Object>> result) {
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        resultAdapter.setItems(result);
                        resultAdapter.notifyDataSetChanged();
                    }
                });
                Log.v(SearchService.TAG, "query:" + query + ",result: " + result.size() + ",time used:" + (System.currentTimeMillis() - start));
            }
        };
        searchService.query(query, MAX_HITS, true, searchCallback);
    }


    private static class ViewHolder {
        public TextView name;
        public TextView number;
    }

    private class ResultAdapter extends BaseAdapter {
        private List<Map<String,Object>> items = new ArrayList<Map<String, Object>>();
        @Override
        public synchronized  int getCount() {
            return items.size();
        }

        public synchronized void setItems(List<Map<String,Object>> items)
        {
            this.items=items;
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View rowView = convertView;
            if (rowView == null) {
                rowView = LayoutInflater.from(getBaseContext()).inflate(
                        R.layout.list_item, parent, false);
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) rowView.findViewById(R.id.name);
                viewHolder.number = (TextView) rowView
                        .findViewById(R.id.number);
                rowView.setTag(viewHolder);
            }
            ViewHolder holder = (ViewHolder) rowView.getTag();
            Map<String, Object> searchRes = items.get(position);
            StringBuilder nameBuilder = new StringBuilder();
            if (searchRes.containsKey(SearchService.FIELD_NAME)) {
                nameBuilder.append(searchRes.get(
                        SearchService.FIELD_NAME).toString());
            } else {
                nameBuilder.append("陌生号码");
            }
            nameBuilder.append(' ');
            if (searchRes.containsKey(SearchService.FIELD_PINYIN)) {
                nameBuilder.append(searchRes.get(SearchService.FIELD_PINYIN).toString());
            }
            StringBuilder numberBuilder = new StringBuilder();
            if(searchRes.containsKey(SearchService.FIELD_HIGHLIGHTED_NUMBER))
            {
                numberBuilder.append(searchRes.get(SearchService.FIELD_HIGHLIGHTED_NUMBER).toString());
            }else if(searchRes.containsKey(SearchService.FIELD_NUMBER))
            {
                numberBuilder.append(searchRes.get(SearchService.FIELD_NUMBER));
            }
            holder.name.setText(Html.fromHtml(nameBuilder.toString()));
            holder.number.setText(Html.fromHtml(numberBuilder.toString()));
            return rowView;
        }

    }

}
