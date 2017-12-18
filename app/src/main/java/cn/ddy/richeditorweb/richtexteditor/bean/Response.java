package cn.ddy.richeditorweb.richtexteditor.bean;

import java.util.List;

/**
 * 图片上传的Response
 * Response Body Gson 映射类
 * Created by 陆正威 on 2017/9/15.
 */

public class Response {

    /**
     * res_code : 0
     * data : ["uploads/529137ac-0856-42a6-9d54-694cbc15b29d.octet-stream"]
     */

    private int res_code;
    private List<String> data;

    public int getRes_code() {
        return res_code;
    }

    public void setRes_code(int res_code) {
        this.res_code = res_code;
    }

    public List<String> getData() {
        return data;
    }

    public void setData(List<String> data) {
        this.data = data;
    }
}
