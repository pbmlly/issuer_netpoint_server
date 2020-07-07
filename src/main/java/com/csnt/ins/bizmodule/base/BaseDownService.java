package com.csnt.ins.bizmodule.base;

import com.csnt.ap.ct.CsntUpload;
import com.csnt.ap.ct.IUpload;
import com.csnt.ap.ct.bean.request.BaseDownRequest;
import com.csnt.ap.ct.bean.response.BaseDownResponse;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.enumobj.ResponseStatusEnum;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 公共上传服务
 *
 * @author duwanjiang
 * @date 2018/3/19
 */
public interface BaseDownService {

    Logger logger = LoggerFactory.getLogger(BaseDownService.class);

    String serviceName = "[公共下载服务]";
    /**
     * 获取上传对象
     */
    IUpload upload = CsntUpload.getInstance();


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
    default Result upload(String json, String jsonUploadFileName) {
        logger.info("{}上传流程开始", serviceName);
        long startTime = System.currentTimeMillis();
        Result result = new Result();
        try {
            //2.上传数据到部中心
            BaseDwResponse response = upload(json.getBytes(StringUtil.UTF8), jsonUploadFileName);
            if (response.getStateCode() != ResponseStatusEnum.SUCCESS.getCode()) {
                logger.error("{}上传数据异常:{}", serviceName, response.getErrorMsg());
                return Result.byEnum(ResponseStatusEnum.BIZ_UPLOAD_ERROR, response.getErrorMsg());
            }
            //3.将上传结果回写数据库
            result = updateJsonFlow(response);

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
    default BaseDwResponse upload(byte[] uploadData, String fileName) throws IOException {
        //1、将文件封装到request对象中
        BaseDownRequest baseRequest = new BaseDownRequest(uploadData, fileName);
        // 2、调用汇聚平台接口，发送文件到部中心，这里的超时时间为10分钟
        BaseDownResponse baseDownResponse = upload.downloadRequestSender(baseRequest, SysConfig.getRequestTimeout(), SysConfig.CONFIG.get("temp.path"));
        BaseDwResponse baseDwResponse = saveUploadResponse(fileName, baseDownResponse);
        logger.info("{}上传[{}]文件完成,响应为:{}", serviceName, fileName, baseDownResponse);
        return baseDwResponse;

    }
    /**
     * 保存s上传响应文件
     *
     * @param fileName
     * @param result
     */
    default BaseDwResponse saveUploadResponse(String fileName, BaseDownResponse result) {
        BaseDwResponse response = new BaseDwResponse();
        if (result.getFilePath() != null) {
            Path filePath = Paths.get(result.getFilePath());
            try {
                Map<String, Object> map = (Map<String, Object>) com.csnt.ap.ct.util.UtilJson.toObject(filePath.toFile(), Object.class);
                response.setData(map);
            } catch (Exception e) {
                logger.error("{}转换部中心响应文件为对象异常:{}", serviceName, e.toString(), e);
            } finally {
                //删除缓存文件
                try {
                    //删除json文件和目录
                    Files.deleteIfExists(filePath);
                } catch (IOException e) {
                    logger.error("delete upload response temp file {} failure : {}", result.getFilePath(), e.toString(), e);
                }
            }
        }
        response.setErrorMsg(result.getErrorMsg());
        response.setStateCode(result.getStateCode());
        response.setReqFileName(fileName);

        return response;
    }
    /**
     * 对jsonMap中的流水进行反向更新上传状态
     *
     * @param baseCertifyUploadResponse
     * @return
     */
    default Result updateJsonFlow( BaseDwResponse baseCertifyUploadResponse) {
        // TODO: 2019/7/31 后期会进行数据存储
        Result result = new Result();
        result.setCode(baseCertifyUploadResponse.getStateCode());
        result.setData(baseCertifyUploadResponse.getData());
        result.setMsg(baseCertifyUploadResponse.getErrorMsg());
        return result;
    }

}
