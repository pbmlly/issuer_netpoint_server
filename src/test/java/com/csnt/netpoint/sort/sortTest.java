package com.csnt.netpoint.sort;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.utils.DateUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.jfinal.plugin.activerecord.Record;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName sor
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/24 15:40
 * Version 1.0
 **/
public class sortTest {

    @Test
    public void sortListTest(){
        List<Map> lists = new ArrayList<>();
        Map map = new HashMap();
        map.put("opTime",DateUtil.parseDate("2019-05-14 11:11:11",DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS));
        lists.add(map);
        map = new HashMap();
        map.put("opTime",DateUtil.parseDate("2019-06-06 11:11:11",DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS));
        lists.add(map);
        map = new HashMap();
        map.put("opTime",DateUtil.parseDate("2019-05-27 11:11:11",DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS));
        lists.add(map);
        map = new HashMap();
        map.put("opTime",DateUtil.parseDate("2019-05-27 11:11:11",DateUtil.FORMAT_YYYY_MM_DD_H_HMMSS));
        lists.add(map);

        lists = sortListACE(lists);
        System.out.println(lists);
    }

    /**
     * 对类表根据操作时间进行排序
     *
     * @param list
     * @return
     */
    private List sortListACE(List<Map> list){
        return list.stream().sorted((x,y)->{
            if(x.get("opTime") != null && y.get("opTime") != null){
                Date xDate = (Date) x.get("opTime");
                Date yDate = (Date) y.get("opTime");
                return xDate.compareTo(yDate);
            }else {
                return 0;
            }
        }).collect(Collectors.toList());
    }


    @Test
    public void randomTest(){
        System.out.println(generrateUserId());
    }

    /**
     * 查询用户编码
     *
     * @return
     */
    private String generrateUserId(){
        String userId = "";
        //产生5位随机数
        int random = new Random().nextInt(99999);
        userId = String.format("%s%s%05d", CommonAttribute.ISSUER_CODE,DateUtil.formatDate(new Date(),DateUtil.FORMAT_YYM_MDD),random);
        return userId;
    }

    @Test
    public void testTime(){
        Record order = new Record().set("registeredTime","2019-07-09T16:43");
        if (StringUtil.isNotEmpty(order.getStr("registeredTime"))){

            System.out.println(order.getStr("registeredTime"));
        }
    }
    @Test
    public void testMap(){
        Map map=new HashMap();
        map.put("1",1);
        map.put("2",1);
        map.put("3",1);
            System.out.println(map);
    }
}
