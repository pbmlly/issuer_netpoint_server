package com.csnt.netpoint.string;

import com.csnt.ins.enumobj.ObuYGZStatusEnum;
import com.csnt.ins.utils.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.junit.Test;

import java.util.Date;

/**
 * @ClassName StringTest
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/7/27 11:21
 * Version 1.0
 **/
public class StringTest {

    @Test
    public void stringIsEmptyArgTest() {
        Record record = new Record();
        record.set("a", "111");
        record.set("b", "11");
        record.set("c", new Date());
        System.out.println(StringUtil.isEmptyArg(record, "a,b,c"));
    }
    @Test
    public void stringSub() {
        System.out.println( ObuYGZStatusEnum.getName(5, true));
    }

}
