package com.example.filemanager.Activity;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.filemanager.Adapter.FileViewAdapter;
import com.example.filemanager.FileView;
import com.example.filemanager.R;
import com.example.filemanager.Utils.DensityUtil;
import com.example.filemanager.Utils.FileManagerUtils;
import com.example.filemanager.Utils.FileUtils;
import com.example.filemanager.Utils.GetFilesUtils;
import com.github.clans.fab.FloatingActionMenu;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseActivity extends AppCompatActivity {
  private FloatingActionMenu fab;
  private LinearLayout bottomActionBar; //  底部操作栏
  private LinearLayout greyCover; //  灰色蒙层
  private ImageButton cutButton;
  private ImageButton copyButton;
  private ImageButton deleteButton;
  public RecyclerView recyclerView;
  public RelativeLayout bottomSheetLayout;

  private String[] permissions = new String[]{
          Manifest.permission.WRITE_EXTERNAL_STORAGE,
          Manifest.permission.READ_EXTERNAL_STORAGE
  };
  private List<String> mPermissionList = new ArrayList<>();
  protected String path;
  public FileViewAdapter adapter;

  public List<FileView> fileList;


  private static final String TAG = "BaseActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(getLayoutResourceId());
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    recyclerView = findViewById(R.id.file_container);
    LinearLayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

    bottomSheetLayout = findViewById(R.id.bottomSheetLayout);


    fab = findViewById(R.id.fab);
    fab.setOnMenuButtonClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (fab.isOpened()) {
          greyCover.setVisibility(View.GONE);
        } else {
          greyCover.setVisibility(View.VISIBLE);
        }
        fab.toggle(true);
      }
    });

    FloatingActionButton createDirBtn = findViewById(R.id.create_dir);
    createDirBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final View dialogView = LayoutInflater.from(BaseActivity.this)
                .inflate(R.layout.input_dialog, null);
        ((TextView) dialogView.findViewById(R.id.dialog_tip)).setText("请输入新建文件夹名称");

        final EditText editText = dialogView.findViewById(R.id.dialog_input);
        editText.requestFocus();

        final AlertDialog dialog =
                new AlertDialog.Builder(BaseActivity.this).setTitle("创建一个文件夹").setView(dialogView).setPositiveButton(
                        "确定", null).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                  }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
          @Override
          public void onShow(DialogInterface dialogInterface) {
            Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                String dirName = editText.getText().toString().trim();
                if (TextUtils.isEmpty(dirName)) {
                  Toast.makeText(BaseActivity.this, "名称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                  String dirPath = path + "/" + editText.getText().toString();
                  createFileOrDir(dirPath, true);
                  dialog.dismiss();
                }
              }
            });
          }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.orangeDark));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
      }
    });

    FloatingActionButton createFileBtn = findViewById(R.id.create_file);
    createFileBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final View dialogView = LayoutInflater.from(BaseActivity.this)
                .inflate(R.layout.input_dialog, null);
        ((TextView) dialogView.findViewById(R.id.dialog_tip)).setText("请输入新的文件名");

        final EditText editText = dialogView.findViewById(R.id.dialog_input);
        editText.requestFocus();

        final AlertDialog dialog =
                new AlertDialog.Builder(BaseActivity.this).setTitle("创建一个空白文件").setView(dialogView).setPositiveButton(
                        "确定", null).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialog, int which) {
                  }
                }).create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
          @Override
          public void onShow(DialogInterface dialogInterface) {
            Button positiveBtn = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                String dirName = editText.getText().toString().trim();
                if (TextUtils.isEmpty(dirName)) {
                  Toast.makeText(BaseActivity.this, "名称不能为空", Toast.LENGTH_SHORT).show();
                } else {
                  String filePath = path + "/" + editText.getText().toString();
                  createFileOrDir(filePath, false);
                  dialog.dismiss();
                }
              }
            });
          }
        });
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.orangeDark));
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
      }
    });

    bottomActionBar = findViewById(R.id.actionbar_bottom);
    cutButton = findViewById(R.id.button_cut);
    copyButton = findViewById(R.id.button_copy);
    deleteButton = findViewById(R.id.button_delete);
    greyCover = findViewById(R.id.grey_cover);

    greyCover.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeFloatingMenu();
      }
    });

    deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        deleteFile();
      }
    });

    checkPermission();
    setBottomSheet();
  }

  private void setBottomSheet(){
    findViewById(R.id.bottom_delete).setOnClickListener(view -> deleteFile());
    findViewById(R.id.bottom_cut).setOnClickListener(view -> cutOrCopyFile(true));
    findViewById(R.id.bottom_copy).setOnClickListener(view -> cutOrCopyFile(false));
    findViewById(R.id.bottom_paste).setOnClickListener(view -> pasteFile());
  }
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    switch (id) {
      case android.R.id.home:
        if (!adapter.leaveSelectMode()) {
          finish();
        }
        return true;
      case R.id.cut:
        findViewById(R.id.actionbar_bottom).setVisibility(View.GONE);
        return true;
      case R.id.sort_by_default:
        if (!item.isChecked()) {
          setFileViewSort(GetFilesUtils.SORT_BY_DEFAULT);
          item.setChecked(true);
        }
        return true;
      case R.id.sort_by_size:
        if (!item.isChecked()) {
          setFileViewSort(GetFilesUtils.SORT_BY_SIZE);
          item.setChecked(true);
        }
        return true;
      case R.id.sort_by_time:
        if (!item.isChecked()) {
          setFileViewSort(GetFilesUtils.SORT_BY_TIME);
          item.setChecked(true);
        }
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    if (!adapter.leaveSelectMode()) {
      super.onBackPressed();
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    switch (requestCode) {
      case 1:
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
          init();
        } else {
          Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
          finish();
        }
        break;
      default:
        break;
    }
  }

  protected void init(){
    Intent intent = getIntent();
    path = intent.getStringExtra("path");

    RecyclerView recyclerView = findViewById(R.id.file_container);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
  }

  protected abstract int getLayoutResourceId();

  private void checkPermission() {
    for (String permission : permissions) {
      if (ContextCompat.checkSelfPermission(this, permission) !=
              PackageManager.PERMISSION_GRANTED) {
        mPermissionList.add(permission);
      }
    }

    if (!mPermissionList.isEmpty()) {
      String[] permissions1 = mPermissionList.toArray(new String[0]);
      ActivityCompat.requestPermissions(this, permissions1, 1);
    } else {
      init();
    }
  }

  public void setButtomActionBarVisibility(int visibility) {
//    buttomActionBar.setVisibility(visibility);
    bottomActionBar.setVisibility(View.GONE);
  }

  public void setFloatingMenuVisibility(int visibility) {
    fab.setVisibility(visibility);
  }

  public void setSelectModeShow(boolean isSelectMode) {
    if (isSelectMode) {
      fab.setVisibility(View.GONE);
      bottomSheetLayout.setVisibility(View.VISIBLE);
      recyclerView.setPadding(0, 0, 0, DensityUtil.dip2px(this, 50));
    } else {
      fab.setVisibility(View.VISIBLE);
      bottomSheetLayout.setVisibility(View.GONE);
      recyclerView.setPadding(0, 0, 0, 0);
    }
  }


  private void setFileViewSort(String sort) {
    Collections.sort(fileList, GetFilesUtils.getInstance().fileOrder(sort));
    adapter.notifyDataSetChanged();
  }

  private void closeFloatingMenu() {
    fab.close(true);
    greyCover.setVisibility(View.GONE);
  }

  private void cutOrCopyFile(boolean isCut){
    List<Path> fileList = new ArrayList<>();
    for(FileView fileView:adapter.getSelected()){
      fileList.add(fileView.getFilePath());
    }
    if (isCut) {
      FileManagerUtils.Instance.cut(fileList);
    } else {
      FileManagerUtils.Instance.copy(fileList);
    }
  }

  private void pasteFile(){
    try {
      FileManagerUtils.Instance.paste(Paths.get(path));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void deleteFile() {
    final List<FileView> selected = adapter.getSelected();
    new AlertDialog.Builder(this)
            .setTitle("删除文件")
            .setMessage("你确定要删除" + selected.size() + "个文件/文件夹？此操作无法撤销。")
            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {
                try {
                  for (FileView fileView : selected) {
                    FileManagerUtils.Instance.delete(fileView.getFilePath());
                    adapter.getFileList().remove(fileView);
                  }
                  adapter.notifyDataSetChanged();
                  Snackbar.make(findViewById(R.id.main_layout), "Deleted!", 3000).show();
                } catch (IOException e) {
                  Log.d(TAG, "onClick: " + e.getMessage());
                  Snackbar.make(findViewById(R.id.main_layout), "删除文件失败，原因：" + e.getMessage(), 3000).show();
                }
              }
            })
            .setPositiveButton("取消", new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialogInterface, int i) {

              }
            }).show();
  }

  private void createFileOrDir(String path, boolean isDirectory) {
    closeFloatingMenu();
    File file = new File(path);
    if (isDirectory) {
      boolean result = FileUtils.Instance.createDirectory(path);
      if (result) {
        Snackbar.make(findViewById(R.id.main_layout), "创建成功", 3000).show();
      } else {
        Toast.makeText(BaseActivity.this, "创建失败", Toast.LENGTH_SHORT).show();
        return;
      }
    } else {
      try {
        FileUtils.Instance.createFile(path);
        Snackbar.make(findViewById(R.id.main_layout), "创建成功", 3000).show();

      } catch (IOException e) {
        Toast.makeText(BaseActivity.this, "创建失败, 原因是：" + e.getMessage(), Toast.LENGTH_SHORT).show();
        return;
      }
    }
    FileView fileView = new FileView(new File(path));
    fileList.add(fileView);
    setFileViewSort(GetFilesUtils.SORT_BY_DEFAULT);
    int position = fileList.indexOf(fileView);
    if (position != -1) {
      recyclerView.scrollToPosition(position);
      LinearLayoutManager mLayoutManager =
              (LinearLayoutManager) recyclerView.getLayoutManager();
      mLayoutManager.scrollToPositionWithOffset(position, 0);
    }
  }
}
