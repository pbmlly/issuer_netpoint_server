package com.csnt.ins.controller;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.bizmodule.base.BaseUploadService;
import com.csnt.ins.bizmodule.offline.service.UserVpoflService;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.model.issuer.EtcOflUserinfo;
import com.csnt.ins.model.json.EtcCardinfoJson;
import com.csnt.ins.model.json.EtcObuinfoJson;
import com.csnt.ins.model.json.EtcUserinfoJson;
import com.csnt.ins.model.json.EtcVehicleinfoJson;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.*;
import com.jfinal.core.Controller;
import com.jfinal.json.Jackson;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.util.Date;

/**
 * 4类基础信息上传，删除
 * @ClassName IssuerChangeUserIdController
 * @Description TODO
 * @Author cml
 * @Date 2019/7/23 21:06
 * Version 1.0
 **/
public class IssuerGetVPOFLController extends Controller
        implements  BaseUploadService {

    /**
     * 获取上传对象
     */
    IUpload upload = CsntUpload.getInstance();

    /**
     * 8832获取车辆列表
     */
    UserVpoflService userVpoflService = new UserVpoflService();
    /**
     * 获取上传对象
     */
    private final String BASIC_OBUUPLOAD_REQ = "BASIC_OBUUPLOAD_REQ_";
    private final String BASIC_VEHICLEUPLOAD_REQ = "BASIC_VEHICLEUPLOAD_REQ_";
    private final String NOR_MAORL = "不存在";
    private final String BASIC_USERUPLOAD_REQ = "BASIC_USERUPLOAD_REQ_";
    private final String BASIC_CARDUPLOAD_REQ = "BASIC_CARDUPLOAD_REQ_";

    public void index() {
        String userId = getPara("userId");
        String pageNo = getPara("pageNo");
        if (StringUtil.isEmpty(userId)) {
            renderJson("用户编号不能为空");
            return;
        }
        if (StringUtil.isEmpty(pageNo)) {
            renderJson("页码不能为空");
            return;
        }
        Integer page = Integer.parseInt(pageNo);

        EtcOflUserinfo etcOflUserinfo = EtcOflUserinfo.findOflUserFirstByUserId(userId);

        if (etcOflUserinfo == null) {
            renderJson("该客户编号未在线下客户表etc_ofl_userinfo");
            return;
        }

        Result result = oflAuthTouch(etcOflUserinfo);
        //判断刷新凭证是否成功，失败则直接退出
        if (!result.getSuccess()) {
             renderJson("刷新凭证失败");
            return;
        }

        Result vehList = userVpoflService.entry(Kv.by("pageNo",page)
                .set("accessToken", etcOflUserinfo.getAccessToken())
                .set("openId", etcOflUserinfo.getOpenId())
                .set("accountId", etcOflUserinfo.getDepUserId())
                .set("pageSize", 100));
        if (vehList.getCode() != ResponseStatusEnum.SUCCESS.getCode()) {
            logger.error("{}获取用户的车辆列表失败:{}", serviceName, vehList);
            renderJson("获取用户的车辆列表失败");
            return;
        }

        renderJson(vehList);
    }


}
