package cn.ddy.richeditorweb.richtexteditor.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.BaseDialogFragment;
import android.support.v7.app.AlertDialog;

import cn.ddy.richeditorweb.R;

/**
 * 图片处理对话框
 * Created by 陆正威 on 2017/9/12.
 */

public class PictureHandleDialog extends BaseDialogFragment {
    public static final String Tag = "delete_dialog_fragment";
    private Long imageId;
    private CharSequence[] items;
    private OnDialogClickListener listener;

    public static PictureHandleDialog createDeleteDialog(Long imageId){
        final PictureHandleDialog newDialog = new PictureHandleDialog();
        newDialog.setImageId(imageId);
        return newDialog;
    }

    public PictureHandleDialog(){
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(listener != null)
                        switch (which){
                            case 0:
                                listener.onDeleteButtonClick(imageId);
                                break;
                            case 1:
                                listener.onReloadButtonClick(imageId);
                                break;
                        }

                    }
                })
                .setTitle(R.string.handles).create();
    }

    @SuppressWarnings("unused")
    public Long getImageId() {
        return imageId;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }

    public void setListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    public void setItems(CharSequence[] items) {
        this.items = items;
    }

    public interface OnDialogClickListener {
        void onDeleteButtonClick(Long id);
        void onReloadButtonClick(Long id);
    }
}
