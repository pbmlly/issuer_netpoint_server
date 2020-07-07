package com.csnt.ins.thread.order;


import com.csnt.ins.bizmodule.order.handset.BatchAuditOnlineapplyService;
import com.csnt.ins.thread.RunnableCatchThrowable;

/**
 * @ClassName BaseInfoUploadThread
 * @Description TODO
 * @Author cml
 * @Date 2019/6/20 0:37
 * Version 1.0
 **/
public class BatchAuditOnlineapplyThread implements RunnableCatchThrowable {

    BatchAuditOnlineapplyService service = new BatchAuditOnlineapplyService();

    @Override
    public void runCatch() {
        service.entry();
    }
}
