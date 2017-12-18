package android.support.v4.app;

import android.support.v7.app.AppCompatDialogFragment;

/**
 *
 * Created by 陆正威 on 2017/9/17.
 */

public class BaseDialogFragment extends AppCompatDialogFragment {
    @Override
    public void show(FragmentManager manager, String tag) {
        try {
            super.show(manager, tag);
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

}
