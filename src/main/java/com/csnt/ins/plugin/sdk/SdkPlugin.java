package com.csnt.ins.plugin.sdk;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.log.Log;
import com.jfinal.plugin.IPlugin;

public class SdkPlugin implements IPlugin {
    private static Log LOG = Log.getLog("SdkPlugin");
    private CsntUpload upload = CsntUpload.getInstance();

    @Override
    public boolean start() {
        upload.setAccount(SysConfig.CONNECT.get("account"))
                .setPassword(SysConfig.CONNECT.get("password"))
                .setSenderId(SysConfig.CONNECT.getInt("gather.id"))
                .setConnectPort(SysConfig.CONNECT.getInt("connect.port"))
                .setConnectIp(SysConfig.CONNECT.get("connect.ip"));
        upload.start(SysConfig.CONNECT.getInt("connect.thread.num"), SysConfig.CONNECT.getInt("request.timeout"));
        LOG.info("SdkPlugin已启动");
        return true;
    }

    @Override
    public boolean stop() {
        upload.stop();
        LOG.error("SdkPlugin已停止");
        return true;
    }
}
