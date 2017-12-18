package cn.ddy.richeditorweb.richtexteditor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import com.google.gson.GsonBuilder;
import com.imnjh.imagepicker.SImagePicker;
import com.imnjh.imagepicker.activity.PhotoPickerActivity;
import com.lu.lubottommenu.LuBottomMenu;
import com.lu.lubottommenu.api.IBottomMenuItem;
import com.lu.lubottommenu.logiclist.MenuItem;
import com.lu.lubottommenu.logiclist.MenuItemFactory;
import com.lu.lubottommenu.menuitem.ImageViewButtonItem;
import com.lu.lubottommenu.theme.BaseTheme;
import com.lu.lubottommenu.theme.DarkTheme;
import com.lu.lubottommenu.theme.LightTheme;
import com.lu.richtexteditorlib.SimpleRichEditor;
import com.lu.richtexteditorlib.base.RichEditor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import cn.ddy.richeditorweb.R;
import cn.ddy.richeditorweb.base.depence.retrofit.uploader.RxUploader;
import cn.ddy.richeditorweb.base.depence.retrofit.uploader.api.Uploader;
import cn.ddy.richeditorweb.base.depence.retrofit.uploader.beans.UploadProgress;
import cn.ddy.richeditorweb.base.depence.tools.SizeUtil;
import cn.ddy.richeditorweb.base.depence.tools.Utils;
import cn.ddy.richeditorweb.richtexteditor.bean.Response;
import cn.ddy.richeditorweb.richtexteditor.dialogs.LinkDialog;
import cn.ddy.richeditorweb.richtexteditor.dialogs.PictureHandleDialog;
import okhttp3.ResponseBody;


/**
 * 在UI上面和功能上边有什么修改的可自行修改
 * 上传这一块也可自行更换
 * 需要一定的js编码基础，不然在做交互的时候会比较吃力
 *
 *
 */
public class MainActivity extends AppCompatActivity implements SimpleRichEditor.OnEditorClickListener, View.OnClickListener {

    private static final int REQUEST_CODE_IMAGE = 101;
    private static final String UPLOAD_URL = "http://www.lhbzimo.cn:3000/";
    private static final String PAR_NAME = "images";
    private LuBottomMenu mLuBottomMenu;
    private SimpleRichEditor mRichTextView;
    private HashMap<Long, String> mInsertedImages;
    private HashMap<Long, String> mFailedImages;
    private Toolbar mToolbar;
    private Button mButton;

    @SuppressLint({"UseSparseArrays"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        if (savedInstanceState != null) {
//            initView();
//            mInsertedImages = new HashMap<>();
//            mFailedImages = new HashMap<>();
//            mRichTextView.restoreState(savedInstanceState);
//        }
//        else {
            mInsertedImages = new HashMap<>();
            mFailedImages = new HashMap<>();
            initView();
            mRichTextView.load();
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //mRichTextView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //mRichTextView.restoreState(savedInstanceState);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_IMAGE) {
            final ArrayList<String> pathList =
                    data.getStringArrayListExtra(PhotoPickerActivity.EXTRA_RESULT_SELECTION);

            long id;
            // TODO: 2017/9/14 可以在线程中添加这段代码防止主线程卡顿
            for (String path :
                    pathList) {
                id = SystemClock.currentThreadTimeMillis();
                long size[] = SizeUtil.getBitmapSize(path);
                mRichTextView.insertImage(path, id, size[0], size[1]);
                mInsertedImages.put(id, path);
                tryUpload(path, id);
            }
            //是否是原图
            final boolean original =
                    data.getBooleanExtra(PhotoPickerActivity.EXTRA_RESULT_ORIGINAL, false);
        }
    }

    private void tryUpload(String path, final long id) {
        RxUploader.INSTANCE.upload(UPLOAD_URL, PAR_NAME, path, id)
                .start()
                .receiveEvent(new Uploader.OnUploadListener() {
                    @Override
                    public void onStart() {
                        //do something when start upload
                    }

                    @Override
                    public void onUploading(String filePath, UploadProgress progress) {
                        mRichTextView.setImageUploadProcess(id, (int) progress.getProgress());
                    }

                    @Override
                    public void onCompleted(String filePath, ResponseBody responseBody) {
                        try {
                            Response response = new GsonBuilder().create().fromJson(responseBody.string(), Response.class);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mRichTextView.setImageUploadProcess(id, 100);
                    }

                    @Override
                    public void onFailed(String filePath, Throwable throwable) {
                        mRichTextView.setImageFailed(id);
                        mInsertedImages.remove(id);
                        mFailedImages.put(id, filePath);
                    }
                });
    }

    private void initView() {
        mLuBottomMenu = (LuBottomMenu) findViewById(R.id.lu_bottom_menu);
        mRichTextView = (SimpleRichEditor) findViewById(R.id.rich_text_view);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mButton = (Button) findViewById(R.id.button);
        mRichTextView.setOnEditorClickListener(this);
        mRichTextView.setOnTextLengthChangeListener(new RichEditor.OnTextLengthChangeListener() {
            @Override
            public void onTextLengthChange(final long length) {
                mToolbar.post(new Runnable() {
                    @Override
                    public void run() {
                        mToolbar.setTitle(length + getString(R.string.char_unit));
                    }
                });
            }
        });
        mRichTextView.setLuBottomMenu(mLuBottomMenu);

        ImageViewButtonItem themeItem = MenuItemFactory.generateImageItem(this, R.drawable.theme, false);
        themeItem.setOnItemClickListener(new IBottomMenuItem.OnBottomItemClickListener() {
            @Override
            public boolean onItemClick(MenuItem item, boolean isSelected) {
                if (!isSelected) {
                    BaseTheme theme = new DarkTheme();
                    mRichTextView.setTheme(theme);
                    mToolbar.setBackgroundColor(theme.getBackGroundColors()[0]);
                    mButton.setTextColor(theme.getAccentColor());
                    mToolbar.setTitleTextColor(theme.getNormalColor());
                    mToolbar.setNavigationIcon(R.drawable.left_arrow_white);
                }
                else {
                    BaseTheme theme = new LightTheme();
                    mRichTextView.setTheme(theme);
                    mToolbar.setBackgroundColor(theme.getBackGroundColors()[0]);
                    mButton.setTextColor(theme.getAccentColor());
                    mToolbar.setTitleTextColor(theme.getAccentColor());
                    mToolbar.setNavigationIcon(R.drawable.left_arrow_black);
                }
                return false;
            }
        });

        mRichTextView.addRootCustomItem(0x10, themeItem);
        mButton.setOnClickListener(this);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        initImagePicker();
    }

    private void initImagePicker() {

    }

    private void showImagePicker() {
        SImagePicker
                .from(MainActivity.this)
                .maxCount(9)
                .rowCount(3)
                .pickText(R.string.pick_name)
                .pickMode(SImagePicker.MODE_IMAGE)
                .fileInterceptor(new SingleFileLimitInterceptor())
                .forResult(REQUEST_CODE_IMAGE);
    }

    private void showLinkDialog(final LinkDialog dialog, final boolean isChange) {
        dialog.setListener(new LinkDialog.OnDialogClickListener() {
            @Override
            public void onConfirmButtonClick(String name, String url) {
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(url)) {
                    Utils.MakeLongToast(getString(R.string.not_empty));
                } else {
                    //do something
                    if (!isChange)
                        mRichTextView.insertLink(url, name);
                    else
                        mRichTextView.changeLink(url, name);
                    onCancelButtonClick();
                }
            }

            @Override
            public void onCancelButtonClick() {
                dialog.dismiss();
            }
        });
        dialog.show(getSupportFragmentManager(), LinkDialog.Tag);
    }

    private void showPictureClickDialog(final PictureHandleDialog dialog, CharSequence[] items) {
        dialog.setListener(new PictureHandleDialog.OnDialogClickListener() {
            @Override
            public void onDeleteButtonClick(Long id) {
                mRichTextView.deleteImageById(id);
                removeFromLocalCache(id);
                RxUploader.TaskController controller = RxUploader.INSTANCE.handle(id);
                if (controller != null) {
                    controller.stopReceive();
                    controller.remove();
                }
            }

            @Override
            public void onReloadButtonClick(Long id) {
                mRichTextView.setImageReload(id);
                tryUpload(mFailedImages.get(id), id);
                mInsertedImages.put(id, mFailedImages.get(id));
                mFailedImages.remove(id);
            }
        });
        dialog.setItems(items);
        dialog.show(getSupportFragmentManager(), PictureHandleDialog.Tag);
    }

    private void removeFromLocalCache(long id) {
        if (mInsertedImages.containsKey(id))
            mInsertedImages.remove(id);
        else if (mFailedImages.containsKey(id))
            mFailedImages.remove(id);
    }

    @Override
    public void onLinkButtonClick() {
        showLinkDialog(LinkDialog.createLinkDialog(), false);
    }

    @Override
    public void onInsertImageButtonClick() {
        showImagePicker();
    }

    @Override
    public void onLinkClick(String name, String url) {
        showLinkDialog(LinkDialog.createLinkDialog(name, url), true);
    }

    @Override
    public void onImageClick(Long id) {
        if (mInsertedImages.containsKey(id))
            showPictureClickDialog(PictureHandleDialog.createDeleteDialog(id), new CharSequence[]{getString(R.string.delete)});
        else if (mFailedImages.containsKey(id)) {
            showPictureClickDialog(PictureHandleDialog.createDeleteDialog(id),
                    new CharSequence[]{getString(R.string.delete), getString(R.string.retry)});
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                //发表按钮
                //do something
                //such as gethtml
                break;
        }
    }
}
