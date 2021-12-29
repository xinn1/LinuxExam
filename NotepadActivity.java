mport android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notepad.adapter.NotepadAdapter;
import com.example.notepad.bean.NotepadBean;
import com.example.notepad.database.SQLiteHelper;
import com.example.notepad.utils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class NotepadActivity extends AppCompatActivity {
    ListView listView;
  
    SQLiteHelper mSQLiteHelper;
    NotepadAdapter adapter;
    SQLiteDatabase db;
    MHandler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepad);
     
        listView = (ListView) findViewById(R.id.listview);
        ImageView add = (ImageView) findViewById(R.id.add);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotepadActivity.this, com.example.notepad.RecordActivity.class);
                startActivityForResult(intent, 1);
            }
        });
        mHandler = new MHandler();
        initData();
        adapter = new NotepadAdapter(this);
        listView.setAdapter(adapter);
    }

    protected void initData() {
       
        httpQuery();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NotepadBean notepadBean = adapter.getItem(position);
                Intent intent = new Intent(NotepadActivity.this, com.example.notepad.RecordActivity.class);
                intent.putExtra("id", notepadBean.getId());
                intent.putExtra("time", notepadBean.getNotepadTime());
             
                intent.putExtra("content", notepadBean.getNotepadContent());
               
                NotepadActivity.this.startActivityForResult(intent, 1);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
             
                AlertDialog dialog;
                AlertDialog.Builder builder = new AlertDialog.Builder(NotepadActivity.this).setMessage("是否删除此记录？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NotepadBean notepadBean = adapter.getItem(position);
                               
                                httpDelete(notepadBean.getId(), position);
                            
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                dialog = builder.create();
                dialog.show();
                return true;
            }

        });
    }

    
    public void httpDelete(String id, int position) {
        NotepadBean req = new NotepadBean();
        req.setId(id);

        Call call = new OkHttpClient().newCall(HttpUtils.postRequestBuilder("delete", req));

    
      
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                  
                    Toast.makeText(NotepadActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                    return;
                }

               
                Message msg = new Message();
                msg.what = HttpUtils.MSG_DELETE_OK;
                msg.obj = position;
                mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

  
    public void httpQuery() {
        Call call = new OkHttpClient().newCall(HttpUtils.getRequestBuilder("getAll"));

     
   
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                  
                    Toast.makeText(NotepadActivity.this, "查询失败", Toast.LENGTH_SHORT).show();
                    return;
                }

               
                Message msg = new Message();
                msg.what = HttpUtils.MSG_QUERY_OK;
                msg.obj = response.body().string();
                mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == 2) {
            httpQuery();
        }
    }

  
    class MHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case HttpUtils.MSG_QUERY_OK:
                    if (msg.obj != null) {
                       
                        Gson gson = new Gson();
                        List <NotepadBean> list = gson.fromJson((String) msg.obj, new TypeToken<List<NotepadBean>>() {
                        }.getType());
                       
                        adapter.setList(list);
                    }
                    break;
                case HttpUtils.MSG_DELETE_OK:
                    if (msg.obj != null) {
                        Log.i("NotepadActivity", String.format("%d", (int)msg.obj));
                        Toast.makeText(NotepadActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                        httpQuery();
                    }
                    break;
            }
        }
    }
}
