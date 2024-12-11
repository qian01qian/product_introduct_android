package com.example.product_introduction_practice;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase database;
    private EditText inputName, inputPrice;
    private ListView listView;
    private ArrayList<HashMap<String, String>> dataList;
    private ProductAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化視圖
        inputName = findViewById(R.id.inputName);
        inputPrice = findViewById(R.id.inputPrice);
        Button addButton = findViewById(R.id.addButton);
        listView = findViewById(R.id.listView);

        // 初始化資料庫
        database = openOrCreateDatabase("ProductDB", MODE_PRIVATE, null);
        createTable();

        // 加載資料
        dataList = new ArrayList<>();
        loadData();

        // 設定適配器
        adapter = new ProductAdapter();
        listView.setAdapter(adapter);

        // 新增按鈕
        addButton.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String price = inputPrice.getText().toString().trim();
            if (!name.isEmpty() && !price.isEmpty()) {
                addData(name, price);
                loadData();
                adapter.notifyDataSetChanged();
                inputName.setText("");
                inputPrice.setText("");
            }
        });

        // 長按刪除產品
        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            HashMap<String, String> selectedProduct = dataList.get(position);
            String productId = selectedProduct.get("_id");

            // 刪除資料庫中的產品
            deleteData(productId);

            // 更新列表
            loadData();
            adapter.notifyDataSetChanged();

            Toast.makeText(this, "Product ID " + productId + " deleted", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void createTable() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS product (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "price TEXT)";
        database.execSQL(createTableSQL);
    }

    private void addData(String name, String price) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("price", price);
        database.insert("product", null, values);
    }

    private void deleteData(String productId) {
        database.delete("product", "_id=?", new String[]{productId});
    }

    private void loadData() {
        dataList.clear();
        Cursor cursor = database.rawQuery("SELECT * FROM product", null);
        if (cursor.moveToFirst()) {
            do {
                HashMap<String, String> map = new HashMap<>();
                map.put("_id", cursor.getString(cursor.getColumnIndexOrThrow("_id")));
                map.put("name", cursor.getString(cursor.getColumnIndexOrThrow("name")));
                map.put("price", cursor.getString(cursor.getColumnIndexOrThrow("price")));
                dataList.add(map);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    // 自訂的適配器類別
    private class ProductAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int position) {
            return dataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this)
                        .inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView idText = convertView.findViewById(android.R.id.text1);
            TextView detailsText = convertView.findViewById(android.R.id.text2);

            HashMap<String, String> product = dataList.get(position);
            String id = product.get("_id");
            String name = product.get("name");
            String price = product.get("price");

            idText.setText("ID: " + id);
            detailsText.setText(name + " - $ " + price);

            return convertView;
        }
    }
}
