package com.csnt.ins.service;


import com.csnt.ins.bean.result.Result;

import java.util.Map;

/**
 * @Author: chengqi
 * @Date: Created in 16:50 2018/11/1
 */
public interface IReceiveService<T extends Result> {

    /**
     * =========================
     * 业务接收主入口
     * =========================
     *
     * @param dataMap json数据
     * @return
     */
    T entry(Map dataMap);
}
