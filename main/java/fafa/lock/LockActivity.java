package fafa.lock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.List;

public class LockActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        SharedPreferences sp = getSharedPreferences("password",this.MODE_PRIVATE);
        final String password = sp.getString("password","");
        GestureLock lock = (GestureLock)findViewById(R.id.view);
        lock.setOnDrawFinishedListener(new GestureLock.OnDrawFinishedListener(){
            @Override
            public boolean OnDrawFinished(List<Integer> passList){
                StringBuilder sb = new StringBuilder();
                for(Integer i:passList){
                    sb.append(i);
                }
                if(sb.toString().equals(password)){
                    Toast.makeText(LockActivity.this,"正确",Toast.LENGTH_SHORT).show();
                    return true;
                }
                else{
                    Toast.makeText(LockActivity.this,"错误",Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });
    }
}
