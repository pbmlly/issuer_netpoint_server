package com.csnt.ins.bizmodule.order.commonquery;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.bean.result.Result;
import com.csnt.ins.service.IReceiveService;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 1001字典信息查询
 *
 * @author source
 **/
public class QueryDictionaryService implements IReceiveService {
    private Logger logger = LoggerFactory.getLogger(QueryDictionaryService.class);

    private final String serverName = "[1001字典信息查询]";

    /**
     * 数据字典的根节点
     */
    private final int ROOT = 1;
    /**
     * 查询所有数据的type
     */
    private final String ALL_QUERY_TYPE = "root";

    /**
     * ============================
     * 程序入口
     * ============================
     */
    @Override
    public Result entry(Map dataMap) {
        try {
            //忽略接入参数大小写
            Record record = new Record().setColumns(dataMap);
            String type = record.getStr("type");
            if (StringUtil.isEmpty(type)) {
                logger.error("{}传入参数不能为空", serverName);
                return Result.paramNotNullError("type");
            }
            //查询所有字典内容
            if (ALL_QUERY_TYPE.equals(type)) {
                return Result.success(buildDictionaryInfo(listAllDictionaryInfo()));
            }
            //查询具体的子项
            return Result.success(listDictionaryInfo(type));
        } catch (Throwable t) {
            logger.error("{}数据字典查询失败:{}", serverName, t.getMessage(), t);
            return Result.sysError(serverName + t.getMessage());
        }
    }

    /**
     * 对数据字典进行重构
     *
     * @param dictionaryInfos
     * @return
     */
    private List<Record> buildDictionaryInfo(List<Record> dictionaryInfos) {
        List<Record> dictionaryResult = new ArrayList<>();
        Record rootRecord = new Record();
        List<Record> rootTypeList = dictionaryInfos.stream().filter(x -> ROOT == x.getInt("parentId")).collect(Collectors.toList());
        rootRecord.set("sys_dic", rootTypeList);
        dictionaryResult.add(rootRecord);
        logger.info("{}查询到数据字典类型:{}", serverName, rootRecord);

        for (Record rootType : rootTypeList) {
            Record subRecord = new Record();
            int id = rootType.get("id");
            String code = rootType.get("code");
            List<Record> subTypeList = dictionaryInfos.stream().filter(x -> id == x.getInt("parentId")).collect(Collectors.toList());
            subRecord.set(code, subTypeList);
            dictionaryResult.add(subRecord);
            logger.info("{}查询到数据字典子类型:{}", serverName, subRecord);
        }

        return dictionaryResult;
    }


    /**
     * 查询数据字典信息表
     *
     * @return
     */
    public List<Record> listAllDictionaryInfo() {
        return Db.findByCache(CommonAttribute.CACHE_NAME_TWOHOUR, "queryAllDictionary", DbUtil.getSql("queryAllDictionary"));
    }

    /**
     * 查询数据字典信息表
     *
     * @param code
     * @return
     */
    public List<Record> listDictionaryInfo(String code) {
        return Db.findByCache(CommonAttribute.CACHE_NAME_24HOUR, code, DbUtil.getSql("queryDictionary", true), code);
    }

    /**
     * 根据字段类型和字段值获取字段名
     *
     * @param code
     * @param key
     * @return
     */
    public String findDictionaryNameByCode(String code, String key) {
        for (Record record : listDictionaryInfo(code)) {
            String id = record.get("code");
            if (id.equals(key)) {
                return record.getStr("name");
            }
        }
        return null;
    }

}
