package com.example.myapk;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    EditText edtPassword;
    Button btnLogin, btnSetPassword;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtPassword = findViewById(R.id.edt_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSetPassword = findViewById(R.id.btn_set_password);
        dbHelper = new DBHelper(this);

        // 检查是否已有密码
        if (!dbHelper.checkPassword("")) {
            // 已有密码，显示登录和修改密码按钮
            btnLogin.setVisibility(Button.VISIBLE);
            btnSetPassword.setVisibility(Button.VISIBLE);
            btnSetPassword.setText("修改密码");
        } else {
            // 无密码，显示设置密码按钮
            btnSetPassword.setVisibility(Button.VISIBLE);
        }

        btnLogin.setOnClickListener(v -> {
            String password = edtPassword.getText().toString().trim();
            if (dbHelper.checkPassword(password)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } else {
                Toast.makeText(this, "密码错误", Toast.LENGTH_SHORT).show();
            }
        });

        btnSetPassword.setOnClickListener(v -> showSetPasswordDialog());
    }

    private void showSetPasswordDialog() {
        // 检查是否已有密码
        boolean hasPassword = !dbHelper.checkPassword("");
        
        if (hasPassword) {
            // 已有密码，需要先验证旧密码
            AlertDialog.Builder oldPasswordBuilder = new AlertDialog.Builder(this);
            oldPasswordBuilder.setTitle("验证旧密码");

            final EditText oldPasswordInput = new EditText(this);
            oldPasswordInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            oldPasswordInput.setHint("请输入旧密码");
            oldPasswordBuilder.setView(oldPasswordInput);

            oldPasswordBuilder.setPositiveButton("确定", (dialog, which) -> {
                String oldPassword = oldPasswordInput.getText().toString().trim();
                if (dbHelper.checkPassword(oldPassword)) {
                    // 旧密码正确，显示设置新密码对话框
                    showNewPasswordDialog();
                } else {
                    Toast.makeText(this, "旧密码错误", Toast.LENGTH_SHORT).show();
                }
            });
            oldPasswordBuilder.setNegativeButton("取消", null);

            oldPasswordBuilder.show();
        } else {
            // 无密码，直接设置新密码
            showNewPasswordDialog();
        }
    }

    private void showNewPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("设置新密码");

        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setHint("请输入新密码");
        builder.setView(input);

        builder.setPositiveButton("确定", (dialog, which) -> {
            String newPassword = input.getText().toString().trim();
            if (!newPassword.isEmpty()) {
                dbHelper.setPassword(newPassword);
                Toast.makeText(this, "密码设置成功", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("取消", null);

        builder.show();
    }
}
