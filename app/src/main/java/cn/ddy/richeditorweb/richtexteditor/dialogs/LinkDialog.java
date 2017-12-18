package cn.ddy.richeditorweb.richtexteditor.dialogs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.BaseDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import cn.ddy.richeditorweb.R;


/**
 * 链接创建对话框
 * Created by 陆正威 on 2017/9/11.
 */

public class LinkDialog extends BaseDialogFragment {
    public static final String Tag = "link_dialog_fragment";

    private OnDialogClickListener listener;
    private String name;
    private String url;

    public static LinkDialog createLinkDialog(String name,String url){
        LinkDialog dialog = createLinkDialog();
        dialog.setUrl(url);
        dialog.setName(name);
        return dialog;
    }

    public static LinkDialog createLinkDialog(){

        return new LinkDialog();
    }

    public LinkDialog(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View dialog = inflater.inflate(R.layout.dialog_fragment_link, container, false);
        Button comb = (Button) dialog.findViewById(R.id.confirm_btn);
        Button canb = (Button) dialog.findViewById(R.id.cancel_btn);
        final EditText urledt = (EditText) dialog.findViewById(R.id.url_edt);
        final EditText nameedt = (EditText) dialog.findViewById(R.id.name_edt);

        if(name != null){
            nameedt.setText(name);
        }
        if(url != null){
            urledt.setText(url);
        }

        comb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null)
                    listener.onConfirmButtonClick(nameedt.getText().toString(),urledt.getText().toString());
            }
        });

        canb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener!=null)
                    listener.onCancelButtonClick();
            }
        });

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);//remove title
        return dialog;
    }

    public void setListener(OnDialogClickListener listener) {
        this.listener = listener;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @SuppressWarnings("unused")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public interface OnDialogClickListener {
        void onConfirmButtonClick(String name,String url);
        void onCancelButtonClick();
    }
}
