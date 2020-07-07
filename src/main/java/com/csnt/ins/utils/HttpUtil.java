package com.csnt.ins.utils;

import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName HttpUtil
 * @Description http帮助工具类
 * @Author duwanjiang
 * @Date 2019/7/11 14:16
 * Version 1.0
 **/
public class HttpUtil {

    private static final String GET  = "GET";
    private static final String POST = "POST";

    /**
     * 通过request获取请求参数
     *
     * @param request
     * @return
     */
    public static Map getParams(HttpServletRequest request){
        Map map = new HashMap();
        String method = request.getMethod();
        //判断请求方法

        if(POST.equals(method)){
            //转换参数为map
            map = FastJson.getJson().parse(HttpKit.readData(request), Map.class);
        }else{
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()){
                String name = parameterNames.nextElement();
                map.put(name,request.getParameter(name));
            }
        }

        return map;
    }

}
