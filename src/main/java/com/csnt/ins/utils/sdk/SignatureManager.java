package com.csnt.ins.utils.sdk;

import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilJson;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SignatureManager {

    public static String getSignContent(String jsonStr, String filename) {
        Map<String, Object> map = UtilJson.toJsonObject(jsonStr, new TypeReference<Map<String, Object>>() {
        });

        Map<String, Object> treeMap = new TreeMap(map);
        Set<Entry<String, Object>> entrySet = treeMap.entrySet();
        String stringA = (String) entrySet.stream().filter((e) -> {
            return !((String) e.getKey()).equals("sign");
        }).filter((e) -> {
            return !((String) e.getKey()).equals("reqSender");
        }).filter((e) -> {
            return !((String) e.getKey()).equals("serviceSystem");
        }).filter((e) -> {
            return !((String) e.getKey()).equals("authStr");
        }).filter((e) -> {
            return e.getValue() != null;
        }).map((e) -> {
            String value = e.getValue().toString();
            if (e.getValue() instanceof LocalDateTime) {
                LocalDateTime ldt = (LocalDateTime) e.getValue();
                value = ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            }
            return (String) e.getKey() + "=" + value;
        }).collect(Collectors.joining("&"));
        if (StringUtil.isNotEmpty(filename)) {
            stringA = stringA + "&filename=" + filename;
        }

        String signValue = SecurityTools.encryptStr(stringA, SecurityTools.HashType.MD5, true).toUpperCase();
        return signValue;
    }
}
