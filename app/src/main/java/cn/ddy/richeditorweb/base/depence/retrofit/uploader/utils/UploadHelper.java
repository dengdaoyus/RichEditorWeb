package cn.ddy.richeditorweb.base.depence.retrofit.uploader.utils;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.HashSet;


import cn.ddy.richeditorweb.base.depence.retrofit.RetrofitClient;
import cn.ddy.richeditorweb.base.depence.retrofit.uploader.beans.BaseUploadBean;
import cn.ddy.richeditorweb.base.depence.retrofit.uploader.beans.RequestBodyWrapper;
import cn.ddy.richeditorweb.base.depence.retrofit.uploader.beans.UploadInfoBean;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

import static com.facebook.common.util.ByteConstants.KB;

/**
 *  上传单个文件
 * Created by 陆正威 on 2017/7/30.
 */
@SuppressWarnings({"unused","WeakerAccess"})
public class UploadHelper {
    public final static String DEFAULT_MEDIA_TYPE = "application/octet-stream";
    public final static String DEFAULT_FILE_KEY = "file";
    public static long MIN_GRAD = 128 * KB;

    public interface Api{
        @Multipart
        @POST
        Observable<ResponseBody> uploadFile(@Url String url, @Part MultipartBody.Part file);
    }

    public static HashSet<Class> getAllInterceptorClasses(OkHttpClient client){
        final int interceptorNum = client.interceptors().size();
        final HashSet<Class> interceptorClasses = new HashSet<>();
        if(interceptorNum>0){
            for(Interceptor interceptor: RetrofitClient.getInstance().getOkHttpClient().interceptors()){
                interceptorClasses.add(interceptor.getClass());
            }
        }
        return interceptorClasses;
    }

    public static Flowable<BaseUploadBean> generateFlowable(@NonNull RequestBodyWrapper uploadBeanEmitter, final String filePath){
        Flowable<BaseUploadBean> flowable = uploadBeanEmitter.getUploadProcessor()
                .publish()
                .autoConnect();

         return  flowable
                 .filter(new Predicate<BaseUploadBean>() {
                     long oldProgress = 0;
                     @Override
                     public boolean test(BaseUploadBean baseUploadBean) throws Exception {
                         if(baseUploadBean instanceof UploadInfoBean){
                             long progress = ((UploadInfoBean) baseUploadBean).getData().getWrittenBytes();
                             if(progress - oldProgress > MIN_GRAD) {
                                 oldProgress = progress;
                                 return true;
                             }
                             return false;
                         }
                         oldProgress = 0;
                         return true;
                     }
                 })
                //.throttleLast(100, TimeUnit.MILLISECONDS).mergeWith(flowable.takeLast(1))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public static MultipartBody.Part generateMultiPart(String parName,String filePath,RequestBodyWrapper uploadFileRequestBody){
        File file = new File(filePath);
        return MultipartBody.Part.createFormData(parName, file.getName(),uploadFileRequestBody);
    }

    public static MultipartBody.Part generateMultiPart(String parName,File file,RequestBodyWrapper uploadFileRequestBody){
        return MultipartBody.Part.createFormData(parName, file.getName(),uploadFileRequestBody);
    }
}
