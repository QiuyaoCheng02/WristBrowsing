package fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.activity.ClientActivity;
import com.example.activity.treeActivity;
import com.example.activity.webViewActivity;

import com.example.activity.R;


/**
 * A simple {@link Fragment} subclass.
 *
 * create an instance of this fragment.
 */
public class searchFragment extends Fragment {
    private EditText searchEditText;
    private Spinner searchEngineSpinner;
    private Button searchButton;
    private Button treeButton;
    private Button connectButton;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // 获取搜索框、下拉栏和搜索按钮的引用
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchEngineSpinner = view.findViewById(R.id.search_engine_spinner);
        searchButton = view.findViewById(R.id.search_button);
       // treeButton = view.findViewById(R.id.tree_button); // 找到树形按钮
        //connectButton = view.findViewById(R.id.connect_button);


        // 设置下拉栏的选项和选择监听器
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.search_engine_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        searchEngineSpinner.setAdapter(adapter);
        searchEngineSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 当选择项发生变化时，执行相应的操作
                String selectedEngine = (String) parent.getItemAtPosition(position);
                Toast.makeText(getActivity(), "Selected search engine: " + selectedEngine, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 当没有选择项时执行的操作
            }
        });
// 设置搜索按钮的点击监听器
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(getActivity(), "Search Button", Toast.LENGTH_SHORT).show();
                performSearch();
                //Toast.makeText(getActivity(), "Search Button Clicked!", Toast.LENGTH_SHORT).show();
            }
        });
// 设置树形按钮的点击监听器
        /*treeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行跳转到TreeActivity的操作
                Intent treeIntent = new Intent(getActivity(), treeActivity.class);
                startActivity(treeIntent);
            }
        });*/
     /*   connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在这里执行跳转到TreeActivity的操作
                Intent treeIntent = new Intent(getActivity(), ClientActivity.class);
                startActivity(treeIntent);
            }
        });*/
        return view;
    }




    private void performSearch() {
        String query = searchEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(query)) {
            String selectedEngine = searchEngineSpinner.getSelectedItem().toString();
            String searchUrl = "";

            // 根据选择的搜索引擎构建搜索URL
            if (selectedEngine.equals("Bing")) {
                searchUrl = "https://www.bing.com/search?q=" + query;
            } else if (selectedEngine.equals("Google")) {
                searchUrl = "https://www.google.com/search?q=" + query;
            } else if (selectedEngine.equals("Baidu")) {
                searchUrl = "https://www.baidu.com/s?wd=" + query;
            }

            // 启动WebViewActivity并将搜索URL传递过去
            Intent webViewIntent = new Intent(getActivity(), webViewActivity.class);
            webViewIntent.putExtra("url", searchUrl);
            startActivityForResult(webViewIntent, 1);
        }
    }

}

