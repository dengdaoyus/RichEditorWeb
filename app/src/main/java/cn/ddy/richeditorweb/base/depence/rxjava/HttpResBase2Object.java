package cn.ddy.richeditorweb.base.depence.rxjava;


import cn.ddy.richeditorweb.base.depence.basebeans.HttpResponseBase;
import io.reactivex.functions.Function;

/**
 * Created by 陆正威 on 2017/4/6.
 */
@SuppressWarnings({"unused"})
public class HttpResBase2Object<T> implements Function<HttpResponseBase<T>,T> {

    @Override
    public T apply(HttpResponseBase<T> tHttpResponseBase) throws Exception {
        if(tHttpResponseBase.isSuccessful() && tHttpResponseBase.getData() != null) {
            return tHttpResponseBase.getData();
        }else{
            throw new Exception(tHttpResponseBase.getError());
        }
    }
}
