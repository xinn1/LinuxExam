import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.notepad.bean.NotepadBean;
import com.example.notepad.database.SQLiteHelper;
import com.example.notepad.utils.DBUtils;
import com.example.notepad.utils.HttpUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class RecordActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView note_back;
    TextView note_time;
    EditText content;
    ImageView delete;
    ImageView note_save;
    SQLiteHelper mSQLiteHelper;
    TextView noteName;
    String id;
    SQLiteDatabase db;
    RecordActivity.MHandler mHandler;
    static Boolean addFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        note_back = (ImageView) findViewById(R.id.note_back);
        note_time = (TextView) findViewById(R.id.tv_time);
        content = (EditText) findViewById(R.id.note_content);
        delete = (ImageView) findViewById(R.id.delete);
        note_save = (ImageView) findViewById(R.id.note_save);
        noteName = (TextView) findViewById(R.id.note_name);

        note_back.setOnClickListener(this);
        delete.setOnClickListener(this);
        note_save.setOnClickListener(this);
        mHandler = new RecordActivity.MHandler();
        initData();
    }

    protected void initData() {
        mSQLiteHelper = new SQLiteHelper(this);
        noteName.setText("添加记录");

        Intent intent = getIntent();
        if (intent != null) {
            id = intent.getStringExtra("id");
            if (id != null) {
                noteName.setText("修改记录");
                content.setText(intent.getStringExtra("content"));
                note_time.setText(intent.getStringExtra("time"));
                note_time.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.note_back:
            finish();
            break;
        case R.id.delete:
            content.setText("");
            break;
        case R.id.note_save:
            
            String noteContent = content.getText().toString().trim();
            NotepadBean notepadBean = new NotepadBean();
            notepadBean.setId(id);
            notepadBean.setNotepadContent(noteContent);
            notepadBean.setNotepadTime(DBUtils.getTime());

         
            if (id != null) {
                if (noteContent.length() > 0) {
                    httpUpdate(notepadBean);
                } else {
                    showToast("修改内容不能为空");
                }
            } else { 
            
                if (noteContent.length() > 0) {
                    httpAdd(notepadBean);
                } else {
                    showToast("填写内容不能为空");
                }
            }
          
            break;
        }
    }

  
    public void httpAdd(NotepadBean req) {
        httpAddOrUpdate("add", req);
    }

   
    public void httpUpdate(NotepadBean req) {
        httpAddOrUpdate("update", req);
    }

    private void httpAddOrUpdate(String action, NotepadBean req) {
        Call call = new OkHttpClient().newCall(HttpUtils.postRequestBuilder(action, req));

      
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                  
                    showToast("更新失败");
                    return;
                }

              
                Message msg = new Message();
                msg.obj = response.body().string();

                switch (action) {
                    case "add":
                        msg.what = HttpUtils.MSG_CREATE_OK;
                        msg.obj = req;
                        break;
                    case "update":
                        msg.what = HttpUtils.MSG_UPDATE_OK;
                        msg.obj = req;
                        break;
                }

                mHandler.sendMessage(msg);
            }

            @Override
            public void onFailure(Call call, IOException e) {

            }
        });
    }

    public void showToast(String message) {
        Toast.makeText(RecordActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    class MHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);
            switch (msg.what) {
                case HttpUtils.MSG_CREATE_OK:
                case HttpUtils.MSG_UPDATE_OK:
                    if (msg.obj != null) {
                        showToast("更新成功");
                        setResult(2);
                        finish();
                    }
                    break;
                default:
                break;
            }
        }
    }
}
