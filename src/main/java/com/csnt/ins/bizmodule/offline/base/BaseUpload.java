package com.csnt.ins.bizmodule.offline.base;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseUploadRequest;
import com.csnt.ap.ct.bean.response.BaseUploadResponse;
import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.i18n.Res;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * 公共上传对象
 *
 * @author cloud
 */
public abstract class BaseUpload {

    protected static Logger logger = LoggerFactory.getLogger(BaseUpload.class);

    /**
     * 该服务的名字
     */
    protected String serviceName;

    /**
     * 上传文件名
     */
    protected String uploadFileNamePrefix;

    /**
     * 文件名后缀
     */
    private final String fileNameSuffix = ".json";

    /**
     * 上传网络传输对象
     */
    public IUpload upload = CsntUpload.getInstance();


    /**
     * ============================
     * 程序入口
     * ============================
     */
    public Result entry(Map dataMap) {
        //1、将参数转为record对象，且参数名忽略了大小写
        Record params = new Record().setColumns(dataMap);
        Result result = checkParam(params);
        //如果参数异常
        if (result != null && ResponseStatusEnum.SUCCESS.getCode() != result.getCode()) {
            logger.error("{}检查参数异常:{}", serviceName, result.getMsg());
            return result;
        }
        //2、调用上传
        return upload(params, uploadFileNamePrefix + CommonAttribute.ISSUER_ISS_SENDER+"_"+ DateUtil.getCurrentTime_yyyyMMddHHmmssSSS() + fileNameSuffix);
    }

    /**
     * 检查参数是否合法
     *
     * @param params
     * @return
     */
    protected abstract Result checkParam(Record params);

    /**
     * 打包流水数据为json文件，并上传
     * <p>
     * 1、生成对象
     * 2、上传数据到部中心
     * 3、将上传结果回写数据库
     * </p>
     *
     * @return 响应结果
     */
    protected Result upload(Record record, String jsonUploadFileName) {
        logger.info("{}上传流程开始", serviceName);
        long startTime = System.currentTimeMillis();
        Result result = new Result();
        try {
            //1、生成对象
            String json = rebuildRecord2Json(record, jsonUploadFileName);
            //2.上传数据到部中心
            BaseUploadResponse response = upload(json.getBytes(StringUtil.UTF8), jsonUploadFileName);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传数据异常:{}", serviceName, response.getErrorMsg());
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }
            //3.将上传结果回写数据库
            result = updateJsonFlow(record, response);

        } catch (Exception e) {
            logger.error("{}上传json文件异常，原因[{}]", serviceName, e.toString(), e);
            result = Result.sysError("上传json文件异常");
        }
        logger.info("{}上传流程完成,耗时[{}]",
                serviceName, DateUtil.diffTime(startTime, System.currentTimeMillis()));
        return result;
    }

    /**
     * 上传数据到部中心
     *
     * @param uploadData
     * @param fileName
     * @return
     * @throws IOException
     */
    protected BaseUploadResponse upload(byte[] uploadData, String fileName) throws IOException {
        //1、将文件封装到request对象中
        BaseUploadRequest baseRequest = new BaseUploadRequest(uploadData, fileName);
        // 2、调用汇聚平台接口，发送文件到部中心，这里的超时时间为10分钟
        BaseUploadResponse baseUploadResponse = upload.uploadRequestSender(baseRequest, SysConfig.getRequestTimeout());
        logger.info("{}上传[{}]文件完成,响应为:{}", serviceName, fileName, baseUploadResponse);
        return baseUploadResponse;

    }


    /**
     * 重构流水结构为部中心要求格式的Map
     *
     * @param record
     * @param fileName
     * @return
     */
    protected abstract String rebuildRecord2Json(Record record, String fileName);

    /**
     * 对jsonMap中的流水进行反向更新上传状态
     *
     * @param record
     * @param baseUploadResponse
     * @return
     */
    protected  Result updateJsonFlow(Record record, BaseUploadResponse baseUploadResponse){
        // TODO: 2019/7/31 后期会进行数据存储
        return Result.bizError(baseUploadResponse.getStateCode(), baseUploadResponse.getMsg());
    }

}
