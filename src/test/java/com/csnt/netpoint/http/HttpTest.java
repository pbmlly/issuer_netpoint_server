package com.csnt.netpoint.http;

import com.csnt.ins.attribute.CommonAttribute;
import com.csnt.ins.jwttoken.kit.JwtKit;
import com.csnt.ins.utils.DbUtil;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.json.FastJson;
import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Kv;
import org.junit.Test;

import java.util.*;

/**
 * @ClassName HttpTest
 * @Description TODO
 * @Author duwanjiang
 * @Date 2019/6/22 10:21
 * Version 1.0
 **/
public class HttpTest {

    private final String URL = "http://localhost:8024/";

    @Test
    public void httpPostTest() {
        Map<String, String> headers = new HashMap();
        headers.put("username", "ksfx");
        headers.put("password", "12345");

        Map map = new HashMap();
        List<Map> list = new ArrayList<>();
        list.add(new Kv().set("id", "11111"));
        map.put("cardinfo", list);
        String data = JsonKit.toJson(map);
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
//        Map outMap = (Map) JSON.parse(outData);
    }

    @Test
    public void TestRefreshToken() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "0110");

        Map map = new HashMap();
        map.put(CommonAttribute.HTTP_PARAM_USERNAME, "200001");
        map.put(CommonAttribute.HTTP_PARAM_PASSWORD, "123456");
        map.put("token", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyMDAwMDEiLCJjcmVhdGVkIjoxNTY0MzgyODM2ODE0LCJleHAiOjE1NjQ0NjkyMzd9.oDe0tltAomL4adYyk1Bcv6hrhhS01SQ_82ZWKgsUh5tsTClJ1LBI600zfVJwmCw-tfNqPI-5JfOZ7noeSJM6qw");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestQueryUserId() {
        Map map = postQueryUserIdServer("101", "434618197807081051");
        System.out.println(map);
    }

    @Test
    public void TestQueryObuId() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1102");

        Map map = new HashMap();
        map.put("cardId", "900034");
        map.put("obuId", "123456");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestQuery1004CardId() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1005");

        Map map = new HashMap();
        map.put("obuId", "6301104010300008");
         String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestVehQuery() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8906");

        Map map = new HashMap();
        map.put("posId", "6301019999901050004");
        map.put("userId", "100002");
        map.put("plateNum", "青AXX965");
        map.put("plateColor", "0");
        map.put("type", 1);
        map.put("userIdType", 101);
        map.put("userIdNum", "632802198805023018");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void Test8806() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8806");

        Map map = new HashMap();
        map.put("businessType", 1);
        map.put("cardType", 141);
        map.put("cardId", "63015464544444123456");
        map.put("brand", 2);
        map.put("model", "model111");
        map.put("vehicleId", "青FPQ123_0");
        map.put("userId", "63010114021800096");
        map.put("enableTime", "2019-08-11T10:00:00");
        map.put("expireTime", "2029-08-11T10:00:00");
        map.put("issueChannelType", 2);
        map.put("issueChannelId", "6301019999901030001");
        map.put("agencyId", "63010199999");
        map.put("issuedTime", "2019-08-11T10:00:00");
        map.put("channelType", "010001");
        map.put("operation", 2);
        map.put("opTime", new Date());
        map.put("orgId", "6301019999901030001");
        map.put("operatorId", "123433");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void Test8808() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8808");

        Map map = new HashMap();
        map.put("businessType", 1);
        map.put("obuId", "6301546454444412");
        map.put("brand", 2);
        map.put("model", "model111");
        map.put("obuSign", 2);
        map.put("vehicleId", "青FPQ123_0");
        map.put("userId", "63010114021800096");
        map.put("enableTime", "2019-08-11T10:00:00");
        map.put("expireTime", "2029-08-11T10:00:00");
        map.put("issueChannelType", 2);
        map.put("issueChannelId", "6301019999901030001");
        map.put("activeTime", "2029-08-11T10:00:00");
        map.put("activeType", 1);
        map.put("activeChannel", 1);
        map.put("registeredType", 2);
        map.put("registeredChannelId", "6301019999901030001");
        map.put("registeredTime", "2029-08-11T10:00:00");
        map.put("installType", 2);
        map.put("installChannelId", "6301019999901030001");
        map.put("installTime", "2019-08-11T10:00:00");
        map.put("channelType", "010001");
        map.put("operation", 2);
        map.put("opTime", new Date());
        map.put("orgId", "6301019999901030001");
        map.put("operatorId", "123433");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestVehOpen8801() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8801");

        Map map = new HashMap();
        map.put("userIdNum", "500227198203159943");
        map.put("userName", "人才人");
        map.put("userIdType", 101);
        map.put("userType", 1);
        map.put("tel", "13421422011");
        map.put("address", "测试测试地址地址");
        map.put("registeredType", 2);
        map.put("channelId", "6301019999901030001");
        map.put("registeredTime", "2019-08-11T10:00:00");
        map.put("orgId", "6301019999901030001");
        map.put("operatorId", "100001");
        map.put("channelType", "010001");

        map.put("department", null);
        map.put("agentName", null);
        map.put("agentIdType", null);
        map.put("agentIdNum", null);
        String tup = "/9j/4QFCRXhpZgAATU0AKgAAAAgABgEaAAUAAAABAAAAVodpAAQAAAABAAAAZgEbAAUAAAABAAAA" +
                "XgEBAAMAAAABALoAAAEoAAMAAAABAAIAAAITAAMAAAABAAEAAAAAAAAAAABIAAAAAQAAAEgAAAAB" +
                "AAiSCAADAAAAAQAAAACQAAACAAAABQAAAMykBgADAAAAAQAAAACgAQADAAAAAQABAACRAQACAAAA" +
                "BD8/PwCgAgADAAAAAQEaAACgAwADAAAAAQC6AACgAAACAAAABQAAANEAAAAAMDIyMQAwMTAwAAAF" +
                "ARoABQAAAAEAAAEYARsABQAAAAEAAAEgAQEAAwAAAAEAugAAASgAAwAAAAEAAgAAAhMAAwAAAAEA" +
                "AQAAAAAAAAAAAEgAAAABAAAASAAAAAEAAQA3AAMAAAABAAEAAAAAAAD/4AAQSkZJRgABAQAAAQAB" +
                "AAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYn" +
                "KSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgo" +
                "KCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAC6ARoDASIAAhEBAxEB/8QAHAAAAAcBAQAA" +
                "AAAAAAAAAAAAAQIDBAUGBwgA/8QASBAAAQMDAwIDBgMDCAgFBQAAAQIDEQAEIQUSMQZBE1FhBxQi" +
                "cYGRMqGxI1LBCBUzQmJy0fAWJIKSorLh8TRDc4PCRFNj0uL/xAAZAQADAQEBAAAAAAAAAAAAAAAB" +
                "AgMABAX/xAAlEQACAgICAQUBAAMAAAAAAAAAAQIRAxIhMUEEEyJRYTIUM3H/2gAMAwEAAhEDEQA/" +
                "AIXQChrR2ZHx7JntnMYqSbuQ/eW7KCCslLcCRJJj/CoVhRatEgzASBj5VJdKqVcdR2CCCpHvDaTG" +
                "ZyKo+I2IdFaa2m3s2reB8CQkH5Cl0q2LlXHBoPDiCMJA+1C88g4/eEYrgUW+SWrZC9J3L14bwund" +
                "+EgxHM9qnWv6/b4ajtFtrXSfGQ14ig5kzH8KkU3DBmCqYjinkrfBnBhWhJPE0R1pK7gEpSSO8cUu" +
                "y9bgEhZk+lACySohxOZHel1YukqEWhvWpRwDigMLdMCEpmfWnLfhITHiJJ+dEShBSsb05gfiH1ra" +
                "sGsqE2EbpPE4pFdlavPnxLdlYyCSgTHzp+ylKUAFSZk9xQIQEuSowCSJPFHlB1kkiGHTmlvqBXbQ" +
                "QMbVqEfSYrx6fbZum3mbq5b28JC4+kgCpttAQvNLKZ3Y79qZSY62oa2ydQbUknUCpsnhbYMelP7a" +
                "4vVKBdFsps/ulQIHyIpu22oSkzB/KlEpUgTyPlzRU2FNkmlyeRFHkGo7co4g/Kk1FYIKTTbj7ErI" +
                "pjqGk6fqTZRqFja3SDyl9lKx+Ypv47hHJBjzoPeHgYEkelb3EDdFN1f2P9MX7q3BpemDcZ2e6BsD" +
                "5FooNVLUf5PugOEqYsig8xbXq0Z/9wL/ADNbCm8VxuIPeaUReKgZBo7oO6Ob9U/k9lpZXZ3usWwM" +
                "knY3cGex+FST+VVzUfZL1HpynPcuoGwlcH/WGH2CTEfuqHHrXWv84eaMehoxvmVCHEmD2Io7I26O" +
                "NX+levtPKmkCxvmkx8KblpwLOcgKIPfyFQl1YavY3Km9a6OU8UiV/wCqwATJB3JHH1rtS7s9EugR" +
                "cWjBnkhG0/cVD3/S3Tb6t4ZW2oceEqCPl3rbI26Xk4rcGhB9X85aQ/aFU/s2FrSUHH75Pr2ps5p2" +
                "gPqHu9/cW6FEhJeCV8ecRXYWp9BaJfOlxd3fhRSEHxj4uPL4u1Qmr+x/QNScCkvNAeGG4VaIHBmf" +
                "g259TJoXEyyr7OTXOnwpKl2moWzyAdsmUyfLv50wf0i8ZO1aET2+ICfUTXVF9/J+0q7Q2mzftm0I" +
                "T8U+KkqVjJ+IjseI5qlaj/J+11lShpOqWz7JzBlBB7ASc1uBvdMCXaPoEllZ+Qn9KbLBSYIIPqK2" +
                "q79h/WlukeAm0vFEA/s30yn0+KKitU9lnXNgAXdDfuRAI93SHAPmEGhSD7hk8UUirrqfSusaa0pW" +
                "rdP39qmAQpy3UkGeMx/GotrS7e5CtiHmlDGRiaNB3K4aJA86sbnTjhRvbfQEg7T4gKYOf8KBPS1+" +
                "oAzb5z/S/wDStQbNauU+HbwVfEoyCDzU97NyFdS2JWkFXjTgenNVrVN3vACEn4UgbYn/ADzVz9kr" +
                "CT1W1I3LS0pYHITj/rVsnEWQN1KlBRTmDkUntgExkUYndCpyK8TmAZBFcy4SGXQiqd6TkA+VCgAL" +
                "VJmfOvBJUMHI7UoEmUmZBrDACEiRXklMScGjFJk0Uzs9aJgQDM8igMkcUDijEedACQkRyfSsMhRB" +
                "G2DzRt20Z5pNKQASczRhnKj96zHR4rUZEx9eaOl5UEFSs4gmkjgSRmhRGBExk0pRJDkPuBJ2rI7U" +
                "IunEpjecfnTczAnjnFe4jyGTQNqhVx51RSA8pG0ZKYpU3jneDPGKbJyY+poVGFA96wHFDn3tYytK" +
                "TRFXKuQmB86QKsngmg3E/KsD24vwOveVHCkzNAHUqGUQY7U2+ZzQgxBn51qA8UfoWU632UsCgDqV" +
                "D8ZGO4mkFKMyOKISDBFakI8MPoceKCcnHGRRQpClQpKZHekCSPWjzOYo6onLBEcoQyrO7b55pwlN" +
                "s2kFbgX8uajd4zgiiGCOSKOqE9mJNLuUBISyBB8+aD9i4jcFpB+fFQTiiCBk0YKMJjmtQXiRN7Uq" +
                "GVJ3DPNHCAtGYnioYrIzJ9K8HSsQSqO9bUX26H1xbtPMKafQlxH9ZKhgzWRe0TouzRchaG0pYuEE" +
                "oIGUKHI9RwYNamytSBgmPU1WPaheNs6RZK5JfjnttM0YppmVxZzZdNeE1qNpdNJDjcggCQZMCPpm" +
                "ohttkNpHvTnA7GrZ1irbqV4RG4obEcT8PeqsGFx+JFUHLVqSkOXaypW0gnM1qXskt2G79ToSo3KG" +
                "sKJxtM4/IVkLzqveAlW1Y3zIEGtz9mTTW57wwSuQhZPEBMiPvTZn8Reky+/CkqUBgmigQCIAPIop" +
                "ISSkYTkEUEyIBO4cetS8DIj9W1VOmIZccaWpLi9h2CduDn7040m+GoWfipbW38ShtWIODRb+xZvf" +
                "CLwUQ2remDG1XY0rp9miyZLbWRuKshI5MngCm41/RVtt+DW61e3t79u2Xu3qABKUkwSQAMd80/jE" +
                "fWobUdD8bUF3TDxbcWnaCrcSgyZ2ncInyiMVNdpOPQVpJeBse3OxTequsv5j1lmz91LyCwt5S0yS" +
                "IE7ccE+v2NOdA6s9+sby6vLdVsmznxYClYkwQImITnHlSOvdPX991Cm9tVteCG/CU064shczJKfw" +
                "7fwkpIzsGRmUNF6WvLLpjVdIuPDKH0lCFJAC3UwobSqe42iccq85rFCTsOrbC8vLBg7m3bu394SF" +
                "BQjKBHGcr59PWj33V2lWupe5PPhTucIIVkRIwcRI5qM0zo93T9UsLplxIdtmShKs7EtlQlkCSY27" +
                "j/eg8ACmms6VqL/VTl0/4iWiwEMpaQ68lEqMzChkhKCRGIHrICmy66XfW+p2yLmzd3sKGDEZ8iKe" +
                "JCinsN1RXSluGOn7FCbcW5LSSpvaQQTzM5n55qb2gCRwO1JRVPgQzJk8mKOrnBihIjIoiomTAPFA" +
                "Y8SJ/OaAgGTyqg3J8jQD4sj50A0AVeXI5r2JycGvEyJ4jkUYbe0ZoGA3JGRJoCYOOKMZg8Ck1YEz" +
                "iiYFRIHmKJIIk4rx3bhFeiPKiBgQJwaPJ880QfT6V7cAZn7UUSkeJKuAfOvAhMnAooUJoVGcJiiI" +
                "eJBnNGQfM0UyCOK9uABJwaxg6jnFeSYH4fnSI5xmlQvAkCihWHk7OKontQVvsdOSfiHirJH+zH3z" +
                "V4UraiKz72iPoTqemoUoltDTi1tkxgqgH/hNHyTZivUilP3F4UhSj42wnv8ACAn+FVtT7kn4Ff5+" +
                "lP8AUbgLQlwqIUsrd2ycEkmq+Ll0CPGVj0P+NMOXe3R4l8mVGCY4rf8A2bNNM2La0FUOqUtZUI+K" +
                "Yx9qwzRbcv6ikFQwNxIIMYrfejGmmdNsvdwS0trcCrnOZ/Wh6h1FEsnCRZHIClFR5NE3HuTI4oJB" +
                "EK570QkrHEZpSi6DLebQpIWsJCuxMUs0oLSFJVukSkg8iq9r2l++XVu+lpbmxtaSEqgAymMSJ7zn" +
                "innTzDlppFuw8lSXUICfi7EDjk/rT6qrFjJuTVEotQ78Ez8jXgqTMHyqmCyvBqD7ykOBpy4QopEj" +
                "4SpU/PmflHlVvQSE9p/WtKOo+ObkuVQtMQAYNLRI+KIrKeoXtXV1BfLtbq7atmkKcUQte1oQkDCZ" +
                "HKVmtPB8Qdx2z2pSo4UpJPeCKTIE8xXgoBMd6geotWu7G5aRbNBaFAFZ8NS4lQAOO1BJydIEpKCt" +
                "lgSc17fgiTk00snXnLJlx9ADykDcEggBUZ5z96g9M6k9+1JFobcoUtJUCVZ5Pb5bfqayi3Y+8YtJ" +
                "+SyOGcUXE5M1EX+tC0uS0LZb8KSlRQtKSkqBIkKIxjByCTHan1hdIvLK2uW23EJebS4ErEKAIkA+" +
                "tIyyHG4A/hNGEHKea8ATkijCDkc0ANiauQr6EUAmSOPIijHCoOAaTWYgiMYJoBFDJEjv50nwDJoU" +
                "qIzmDXsEek0QHokg0he3VvZWztxcupaZaSVrWswEgck0ufhE8iue/b51iq61I6BZuEWttCrjaf6R" +
                "zkJPoP1+VNFWyc5UiR6v9twQ64x03ZhY4FzcTB9Qkdvn9qybWOsNf1h5bt9q12pSj+FLhQgfJIgC" +
                "oKSpcnv50oU8YxVUkjm2bJzQ+rNa0a6Q9ZahcJgyUqWVJUPIg1uXS3tg0e+tmUayFWV2QAtW0lsn" +
                "zB5A+dc57TE7YivNmJkcVmkzXR22y61cNIdYcS42tIUlSTIUDwRQnJ4rn72J9YXtvrlpoVw6XbC5" +
                "3JaSo5aUASI9DERXQU9qnJUUTsAoAMjvR4SR/GiKmcGhSrHasmBhXDCoAn0msk9rV4GNTuUIX8ab" +
                "BIUo+at0D8xWruq2pJwTzFYT7X356kvtp3BfgNecEJSSP+E/eiuxDNdTfcSkJQAraiBMHyFINBfh" +
                "I/Zt/hHYUe9cZLiwlrClBG4Kz/nFTM2wxs49Kcxaui0sqvHnLgLS0AAqBmDzFb1oTKGEoZbBDCGg" +
                "GyeQOM1lXSaLR5CPeAEJUvYdo8zjB861qyw4tJgBKQUmp+p7SIzduI7xOBg0UJHiZEzR4jJOa8on" +
                "BT9KJeqBwlUEAChgQRM+VV/XtSuba/YZt/DIUWiQpYScqVPbuExUnY3Dtzp9u+oDeppKzHEkVTWl" +
                "YsZptr6HipgfvAfegSAe+P0qB0vVru71R61fYQgNpBkTjKsZGcAVPtJP4oMGg1T5HhJSVobO2No4" +
                "FIVbslCl+IoFAIUr94+tOp2TmZzUK9r7aXHG02r5eRcm1CJSCpQbDkgzEbfM0Wy6lsb42xt0Plt9" +
                "SUIdKQE71NhwIOZnaR2jtNLQ9k83BJUcUi7btPuFa2m1q2lBUpInb5fKm+nXzOpW5ft1EthxbfxJ" +
                "KTuSopOD6g0ldavbW14m1Upe9SQZSkqAngGO+DQp+BrXkkGrdttgNIBS3kABR701Z0qzafS823C0" +
                "TtMnE8gemePl5U694Ztmg7cOIbQogblnbkmAM+pilA4guFsKSV/i2zmJ5iltoZpMhtW0D+c1Poub" +
                "p33V4JCm0iCAnKQD/eknEnA4EVLWrTiGEB9xLjiRClJTtB+kmKSdvrdFwGFvIDpIGwnueBTkL4EY" +
                "9DQaY656DKJjtFBIJkjjmiFUk5NClXn8jQCHI7fUUmRCiDwe9GJ4xPIohVIgjjigZI8EqHGRRikK" +
                "SO1C3xBOK9GDAI+dEDYRf9GZrinX7l7U+pb93aXH7m6WoJ5ypZgV2Xrb4tdHvbhchLLK3DHOEk1y" +
                "b7O9POoa8u6XlNsndJz8asD+NFy0i5EtXOSiTHTvs+cuUBzVngyD/wCU1BV9TxVlV7M9NMFNxcCB" +
                "gYpMpWzeFbOupYfJ2hC4Mnyg1c9HuLlxjbdhtawPxo4V9K5JZp92d2PDDpozXXuinbS0X4KUrbTn" +
                "cJx8k8k1UtY6auNO062uHUqDr64DZ5SM81suvOX6T+xuWLZqclaZqHvtPVd26HXblL5QFKgRE7SJ" +
                "H3o488l2DJgjLpGb+z54t9baEEyD740AfKVAV13BCcVx90iFN9e6GMpA1BnMf2xXZCk/DXbLk86P" +
                "HA0UTkQaJKueB3pytPnSKk4I7UgWIXaw1bLXk7Uk45rnj2mPpc6uv070qQq8cUVGTAQIiP8Aaj6V" +
                "0HdJ3thGZKkpx5FQFcz9XOJd6o1EtuBQU46sLIx8TipP2CYp49k/JWFIZURueVl0kBSPsfzp5tUa" +
                "K3aFTTC0ltUElRChMzz9qe/Ee4+1UMzSPZ5duXV3aN3Swre7j4RJKQVDt6Vs9kUy+FERtkY9Ky72" +
                "W6cyly0uHd3jpQp5CYwZhJJ+h/OtPbzb3rm34NhyMEYqGfmSIZP9iQ8/DMmRRxgYGPSk+FfDiOxp" +
                "TjAwTTHSEcZZcPxtoc9FJBpVDaENhASAgDaEgYA8qYvanZ21wpt59CHRtlJPnxT4KSpHiFQCYndO" +
                "IpqYqafQizp1oy54rDO1w8ncc/PNOjAxzSSXkuLKEqBO3dAOYPB/WlFGBjmg78jqvBFXujM3LbwK" +
                "ygOvB9coSsFWwJ4UCOAPrTW36WsWLu0dbkt2gT4SNqRBSjYM98fn3jFTm5IJkyf0owJUZJgelC2N" +
                "Q20+xRZsqb3BW55x2SP31lUfSYprf6Hb3l43cqU4FpUJgAggBUAA8ZVNSoIiUgR50YkHAOa1tdDU" +
                "n2R2qad73YoYMLKXWly5BwlxKifnANEbsfD1p+8CW072ENbh+JRClHP0IqUUfWKIDjv9aWyiRBO6" +
                "S47qIuyv4zcJWUgiAhIAHaScefelup2Ll/SFt2RIWpxrdCZOzenfgET8M4qXrw4ouTZoxUeiK6fb" +
                "db0tpD7fhqClwCCDt3q2mCTEiDHaYxxTDRbO5ZvGVvNFMpWpZ2AZJGCQozyeQKsB4OYijgcZoJ0q" +
                "DKFtM9yPrXime1GBwKAzSjHgIxOKFRPeikHnyow4oisb3jKbmzet3BuQ6hSFA9wRFc9+zzTvcF6o" +
                "wtG1wXKkkeiSQP410VBis/1jplnSHV3lupag+6SsK7E5/wAalmtwobDW9shjolk6pK3GUFaVhxJI" +
                "4UO49adW4Qyrw2xCU4A8qctuJCM0wbDqLh5SXRtVwCmYribtHpxSsNfaTbarbLbuk721DaRPI5ps" +
                "/o7Fmha7dARKYIHGBAqT01KmwQ44FLJzAj8qNfS4Nick4rJ8UFpdmZ+zHpldz7UQ86ApixCrg/OS" +
                "Ej7n8q6Tmql0b00rRbm8uXnErduAlMJH4QJP6k1a1KxXpxk2lZ4uRLZ6iauTSak5NKHJxRFCaLJs" +
                "Y3ig0PEWoISIlU8QZmuUdTDjtze+EPFKCBuAzAAn85rqLqdwI0e7lQSA04Nx7S2rNcnXrx2OLBMP" +
                "PKgR5n/tTQJ1yPmG3WWGELQAkNyrHp/1rylvbj8Dn+7/ANa8krLpSCpKSI5qMceHiK+Bvk//AE//" +
                "APVUMdP9BWbKWSvIdbZSgDsAc/8Axq1J3I029BgSlST8o5qH6SYa9wfdQVKUpYSvEbdox+tTCUJd" +
                "sVodOVKPoSCQM/571z5P7Izd5BzIJ9J4NKDIHI/Ok0/lSqM8YqiLkJd6I49euvMltAU6h07VqSTt" +
                "EZipa9tzcWDzCYSpxspzIiRHanaMDz+lAv0qjk2TjFK6IDQ9Nfs751dwiQUpSlSXCRgnkfKKmboQ" +
                "0shzw4SfjwdvrmlCSM021a9TYac9duIUpLYkgeU0j5dlIpRVIzjpXTLy21u1uVPOpS7Bhbagkw3w" +
                "D35I57TT64c1lTtw6q5R4Fx4ykjaofDLTeDP1BjsT3xP6T1Rbarf+4ptrht4tB34wB8PmRMxOAe5" +
                "nyp2da073YuB0uJ8RTMJbUslSfxQAJMedYcqnVms6lZ6y42glNu02m4ltwzs2uJ2gbQAeTJJ4HlQ" +
                "dQ6xrVq6WLd1SUGyacbdkbtxXkqxtBJgYHAMc4txu9Mfu0WbiELuHkQkLYMLSBOFEQRB8+9EYudH" +
                "1Z5NuG0uqUmR4jCglSU+RIgxuP3rDEVc6pqDfR9vqC7tlpQQPeH9m+AcFxIBAxzEcA4nk95rd7Yd" +
                "Kpv71dqi5c8PwxG1MKUkGZPIBJPYRVnesbV1TTjjDalNCGyUg7fl5UzuNDsbi0VaOM7bdS/EUhta" +
                "kAn1CSMenFIOrIbprXbnU7a+Kgw88w6EoS2rbuBSkzOe5P2pPpnqW51N+4besyENI3koUCc/Egc9" +
                "0KTHGQe1TKdEsmbZ+3aDiGn1bnAHVfEe+SZE94pCy0K2tHLkMre8G4kraUuUkkJEz+LhIAzgcUOC" +
                "iTInSOrRe6hbWr7aGVPMIdKV7kr3KSDCRBChk5BxjvikB12gXlswdPeHiulslSwCkbyhOPMmAR2M" +
                "+VSth0tZWV43cW5WgIUpQbEbQCIAGMAfwHYAVHP9CWi75V0i6eaWXg8hKUICUkOlyOPMnyMedbgz" +
                "sfv9Thq9dthaOuFC9g2ySuIkj4YMTHNSeo6xa2LrTdytSVOLCBAMCQf8O3+NRbnTI8Zy4Qq3Vc+K" +
                "t1suMSAFTKVQZPIzI/CKW1nS7i8vLV0KRtYXvgPONlQ2KTHw4GVTNZ0Dkm2nkOtpUggpUJBHelQa" +
                "a26FeGgLCQoAA/EVfmcmi319a6farub59q3YbEqccUEpH1NKjSHh55qp9faxZ2Ni3aPOD3u7VtYb" +
                "7kpG4n5QDVe172w9O2DahYLe1B/gJaTtTP8AePb5TWPO9QX3UHWVprN+o7feQyhI/CgKSqEijOHx" +
                "ZKM/kkjTre6Fy1CVcjPmKQVYnxCrxLklWAUr4pi+w9bOC5tMjug8GnDGvKQNrlo9P9kSK8r/AIez" +
                "CVEpb2q7dXiF1wjslSpFTHTLab/VgFKBS0PEUPPyqsuak/dgJaZU0DyVc1A33VD3SvU2ivNEqaUp" +
                "w3LY/rtwAftM/SqYI7TSJ+onUGzoMRFEXzUdo+tWOrWibjT7pq4aV3QqY9D5Gnylc16VHlIIgwTQ" +
                "KMGhHnOKIrAnJrMVlZ9odwm36S1N5SwkIZUZ+nH14rlzxVIVaMCJUUiYwPOujfbK4lr2f6md4QXA" +
                "ltIPclQ49a5+0oMPa2yh1ISkJk+mKpDomeadUourURJzgd8yP8/9oqfRP2TUxd+7pbdNoXQzK9oc" +
                "AUSJiIqF2D/7qfuf/wBaYB2J0+EW+jNrt9xSsla553cH6YqTQla7FslIzB+WZprapYb0thFuVbA2" +
                "FJKuTOc/enqdwt2/iAwJH0/6VzSdzOZczYk0vkEcUslRGB2pAEBR4o7ZmcQasdQ5Qo5xSk0giYmZ" +
                "FCVCcYo2ahQnIGPWaj+pNOOq6S7YpdUx4pTK0p3EAKB49Yj607D3x/FBFChYWoqoWMkVjRulE6dq" +
                "Tb6VtOIbK1AlBCpKQmcHbODwBzR9Q6a96dS8FtrU2t9QDoO39oQZweU7RH8Ks2QntmvGMJ+9axqI" +
                "y105SXHXLl1bq1NJYQqSFBAGcjuVSSR5DyqJ0Xpsabrhct0v+CCsqdddlS5IIAg8fMdhVqP4xnAo" +
                "U/jV9qVsdIUJBn0rwG5J86SCwJCvOjbingyKBRIKoSSCD8qDbAzyDQle6SOaHcFAeo5oDcgDA+sU" +
                "P0oiefOfSlQkzGKxmwNsnyopSJNHOJkCg3A8iKwCN1+6Om6Lf3qAkqt7dx4A8EpSTn7VyJ1V1ZrH" +
                "VF2XNWu1LQPwsp+FtHyT/k11P7SHg30NrqsT7m6M+qSP41xsomTHNVxrg5szd0OLcyBuJBjymrmb" +
                "I2vQlrepH7UXibjngA7R/n1qhuP7fhRlQ71dndcb1D2cqt0pS3cWhaacT5gKEL+v6zRmrVAw8Ntm" +
                "o6S8LuxbXyFJmhFvtcJSvZ5io72b6pb6xoaQkBNwxCHUesYPyNWxVmlRkxXjTg4to9uDUoqSIhQS" +
                "y0pYrKdUuTqfWzJUCq3aV4UEdiCD+ta7qtsSwUp8qybWHmNI6htG8Fxx5C3CBlKZ/jV/SL5EPU/z" +
                "yQy77U+lteeOm3b1s6hZEoVAV5EjgyK0TpT23Xbbga6itEvtYHjsfCofNPB+kVS/aa6wdXtFtgFT" +
                "zErIEZCiB+hH0qkqXBCAIr06tHkybjKkduaPqVvq2m297ZKK7d9AcQSCJSfSnUxiq/0AyLfovQ2/" +
                "Kyan6oBqccgGosezLvb27t6VtWFLjxb1BAjsEqJ/SsPsyX3niwtz4UZnaASYnk1r/wDKBXut9HbU" +
                "5EOOLCf3jtifkJ/OsYtTFq+W1IUVd/zqsehGOrkpVYIMhQgGeNpJnj8pqCK8nmpi6KmrZpAgEAGc" +
                "T+Hz/wAKh9w9fvTAO2H20tW6UJSoJQgJSfQDFSL60+7oO348CkLlwFmCnNKOoX4SfEAIMR6YrkX9" +
                "HNjXyEimUkmjowADmjQCIMTQD4D6VY6xQ4jb9qKTKSFD0pNFyy4va24lSgSInMjn9aK5fWyXC2p5" +
                "reMFO8TPyo0w2jyllKfhAJpZA43TSSijxE/EATgAnml9wC/xChQ9oWgEgTQbTvooWN0g15SoBVPa" +
                "gZAElKzuODxQqkGR9qBak9zXhniAKBSJ7cCfiGaKZBlJxRoEmYrwVPHHrWKBZyFfSgUrskEkGa8e" +
                "YAOe9FHIUTkYIFKxhcLG0TwaMmcwaRkJnyP5UKYGUn86wtCh9VGixnnFCI7814cYHasYqvtLsrzU" +
                "OidUtNNYU/dPNBKG0xJ+ITE+k1zXc+z/AKqaEr0K+/2W936V16oZGJjmhSABkVSMqJZMalycWvdK" +
                "a4wSXNG1FHztl/4UwXp99bpcQu2uWt6YIU2pM5nOPSu4Ph3EbRSakIJyMntR3Je3RyN7MdVVpPU7" +
                "LTqilm6/YqBxBP4T98fWugA2riruLO2URuYZUZkbkA0o6w0Y3NI/3a58uJZHaOrBneKOrMy11xFt" +
                "ZOOuK2oQkqUTwABJrmzXNQXqWrXN6RBWuU+YA4H2rsvUtH07Ubdxi9s2nWnBtUhScEVWLj2YdHvz" +
                "OitJjuha0/oafDBYxM+V5ao5Y1vUHdUvjdvBKVbQISMev3JJ+tR7aVOuSZMV1S97IOkHgQixeQD+" +
                "7cL/AImmh9jPS4A8IXzY9Hgf1FX2RyuDsvmhMljR7Fg48NhCPskCnbh3CQDIoG4H7NIwiO9A4qFY" +
                "iDg5qYzRh38oJ6dX0gFRUpu2WfDHI3K5/L8jWVM27iLDdASVkgDvnFX/ANvxCOrE/GFFNmhISkzG" +
                "VGD65H3rOmnXfDtRJGwyM5GfKqoUV1EhC0gDBBInEdqifh/dT9qkNQO9X4SBsGDzzTTwz/Z/3hRA" +
                "duPq3NklIntNLvOrc8BKUhOZXiMbSP1iom21e3vrFp+1cCkKiY5Hoal2S44tIUYGee9cfKmiOONM" +
                "8oZxEiklyUqSSRIiRzTooEk96IURMV0nQViwtboPNJdYeZQFqUlQWk+HA4OTO7MnzNRz2k3nvb10" +
                "4l5TQuHFKaAJ3fFKVABWeB271dwOJHFBtE8UVkoR4k6KL1na6hcXTj7DRKWGYQQFYUQc8c5jE091" +
                "hF/NlL6N3gkFIbMFW5M95yJE+QNW2E+WaDaFR8u1N7nSAsKtu+ypOp1N/p5sMHeoO7k7QUHw93wj" +
                "Mzg/pSNncXiOnbotLWpw3ASFoWrcmSkGIHb07k1ctkz3BoEtpTgIATHEYpVkKPFfNla6bvbl3VC3" +
                "cJeSlbO/atSiAQU/vZ7/AK1aiTB70RDLaV7wgBZG0qjMeVKbT2znikm7dlsUdVRVde1HUbHqC0Up" +
                "xxjRnFMs+I222uXluFMLk7gky2AU9ya9e6tfI6tt7KxfRctbgbm3DGLdsoJ3KcnCiQIT3B471O3G" +
                "j2VxfNXr7AXctZQpZJCSOCEzAOTmJpuzodkxqj9+0bhLzy/EdSH1+GtWwIkomJgDt2oWPTIO06hv" +
                "3NXtg57v7hcX7+nobCD4iS0hwlZVMGVNKERwRmpHTNXurnqTVdPds0MtWrbTja/ElTgWpxMkRj+j" +
                "J74I+VL2vTOnsar7+0l7xA4t5DZcJbQ4sELUE9iZP3PnTz+Z2Te312lbqHrxlDC1BX4QjftKfI/G" +
                "a3BuUQWka9eXVxqVq7bWqnrMJJXb3BW0VK3fsyopEKG0Tg4UKHprqpGr6VfX6rJxDNs4tA8FfjeN" +
                "t5KAADziCBPyzT7SNAXpGkK0+2vllpKNjKyygFs+ZAACj3M80poWiDS13jzz5fubt0OurCAhMhIS" +
                "ISOMJFbgKk6G7HVVqvp2y1ZNrdf62sNM2xSnxVLKiAnnaOCZJiBRh1ZaGwt7hLN2t24fXbptUNhT" +
                "3iIKgtMAxjYrMx5cim6+mino9nRUradcZQNjziSAlYMhwAGQQciCM96aaj0ihHTFppWlOraftTLV" +
                "4p1YdbUqfEdlOVLO5WCYJOaHAOWTDnVGks6XZag/dpRbXqkJYKkncsqwBt5nz8u9SNzqtjZ3Vsxd" +
                "3TDL10vw2G1qAU4rySO9QOpdOJHTVnpenJQlFsu2CPEP9RDralZ8yEH5ml+oNMdvLvRnWWm1qt7x" +
                "LrqzAKWwhfH+0U4rGdk47e2jdyzbvPsoffnwmlrAUuBJ2jkwPKkxf2QvTae92/vkbvALifEiJnbM" +
                "xFV/WdNL/VWiXzNukqYLvjPwJCfDUlKZ5iVmoJNheHqJCFWT4cTrC75V1s+DwfBUkQrzylMc80Uk" +
                "I2zRA4NwCVZ8q86qeTgelZx0srVT1ZcOXqHnF3BeNyHWCgW6ULAZS2vhSSJMCcknFObBSdIvOr37" +
                "lV25aoUlxSluKUojwQpW09vxEACOKNCWXTcFGJwT96EA9z6VTeiWlOaTqT1rdttpuDLLLT/ji1Oy" +
                "OSSCo8kDE+fJDRHrp7okhd9cOXinLhLLzr2xSyl5YQCqD2AGBx2o6m2LqOSJpN0AAkk7YzVe6Cu3" +
                "7npi2cvLl65ukKcbecdCZK0rUlUbcFMgwe4ip9xPiDYoylXPrQGQVIATgSRketJoJKiogBKvyo5A" +
                "BA3ZHMUDoQlIRt/HgD1oCSOZfbc8R1/qKW3AslLaTtzthtMj8qqNm44XgoqTAQARAE45qa9pl1v9" +
                "oWsGEuk3ZTjgEYj6RUQ5cJeu3C2hKIJEiRxjjvVRGJ3LyhvUk4RATuzHf+JqL3r8z+VL3aiQqOJP" +
                "1pjmiY05rVL2wfUrT7t5hRMRugEfI1tvSXWbXUWmoS24ljVmhK2lRlQ7p8wawg9TMOA+8WduR2Uk" +
                "KTP5kUivUtOcclTam1IV+JC52/LAqc47Bo6aZ6tskSnUw5ZvDBlBUg+oUAfzijI6r0B0laNYstv9" +
                "p0D9a5rOsLAHh6zfNhIjL6oA/wB406Gu6iUJDWrYH76Ad0f3k5oc+QpHSKdc0lQGzU7Iz/8AnT/j" +
                "S6dQs3Adl3bqPaHUn+Nc5HqPXCI8ayeER/Qtqo6eptRaSnxNK0pYTyXLY/F9lVqYaOjkOoVG1aFD" +
                "0UDSwAMz2rmhfUzagoDQLAKPJSpSc+kcUv8A6U2YQgfzM6lW0BSm71ScjyxQ5MdHFQSnFCFfGK59" +
                "b6v0xlpKRaa0lZSd227MJM/PNOU9W6YEoi86jQoiSEPFW30yrP0ocj2b7vAgmOaALAy3HOZrEG+q" +
                "7FCBs6g1xIVyFoJI+fP5U7t+q2S4kI6sv0bsDxbbdn/cpdmPGX4bKHAQVRwSDQFQJwBBrMGtfe8Q" +
                "tt9XW6irADjKc/8ACKe2+qastSm2tc0tSk4IW0AR+YobD7o0VCpHaaNIGTzVDGp9QIH/AI3R3DE4" +
                "SR+e6KcJ1XqYGU22luZjC1CT9zW3BtEu3bcftSaiFEpjA5xVQTrXUoWUq0a0Uc8XCh+W2lBr2uIE" +
                "u6B8Pfbc8/dNbdGTRZ4xnHfHlXkgkycTmqyjqa+Mheg3Q84dSYFAjqx4KUF6NfhIzI2GB962yDvH" +
                "7LYoAIE88mm4IJJzNVxXWtqn4XdO1NBOP6EK/Q0h/pxpQPxN36B/atF/wFHZMXZfZaFQQD3pPdtP" +
                "Ge9VwdcaCcqu3UdvjtnR/wDGlUdZdPlQB1RkE8bkqT+oo2ByX2T4EEqKRJ7jmki2FOKXOFCIqNT1" +
                "Pobg+HV7I/8AvAUqnXNJWgFvUrIjz8dP+NMTbQ9YabSlWyAknMCJpK40+zubMMPWzDrMg+GpAUmf" +
                "ODihRe2ryP2L7LoI5QsGhU7sRzuE80TJHrZlu2bDTKENtpTCUpTtAjyFHSSEEwCUmkBcFJSCk9+R" +
                "NeDiVOnMk5EVg0OSfimBkUmlCnVQpQG1UpjmhJ3NjnB7U21i7b0+xuH1K2qbZW58glJJUfQVhGcm" +
                "68tu66s1G5Cy4HL55YUjE/ESD+dMmfCClqR4hJMkKii2m3xlKU6B8KpMfmYopKUtHaobeRBOKoiZ" +
                "H3B+M9hzSGf8mlXcqPO2k8+v2ohOirz2X6A8ISy813Gx5X8SRUJdeySzCCLXUbpsq/fSlf8AAVrL" +
                "n4T86TPFedHJL7Pb/wAfHLtHO3UXs+1TSG1raKLtB5KQQf1qlFx1Ky24laSkn4SeD/kV1dqgHhnA" +
                "4rCvaUy0jqFexpCfgPCQO9PDNJ8EcvpscVwile+u4CnHAAAPlTi31Z1tQCLjaAPln1pk7TQ/iFWU" +
                "2cssMUWVnXbl0oQXErVHxbyCCZ7T6U5a1NTiAfCbUZiAkADy4z/2qpgCDgV4kjgkVROyThXkubeo" +
                "oBgtw4CQpA3CI+fzpwNTQEFRSoEY2hYJ/SqVpr7u9f7VfH7xqxW6iu4VvJVjvmj2AmG75pSSoqKQ" +
                "DBK0gAH70sLlkz8TZjnBEflUA+B7wjA4T/y0oVK3OjcYlPf50dEZTZZGnGVZAQf7qwTThC0IWlxK" +
                "XEqTkKg1WZKLa92EpyjjH9alNJyhwnJ3J/5TSaIopstaNXfauPHRdveLM5VMx5inbXVN63cB1x5D" +
                "gmShTaYI8uKpzT7xiXXDx/WPrVi1G3ZQs7Gm04HCQKVxQVK/BNo68dRcgrtmPAKvwo3JUB6GefpV" +
                "h0zqu9urhKRpmos2xMJc8dSYHYwY/WoL2c27JaS6Wmy7P49on71enkp3nA+3rXPkevRaGGM1yLt6" +
                "mtKFw7fhJ81gz65pAdSOtuFAuniO+5ptX3MVG6gT4Jz3pB0AWogDmufdlX6SBYmdbU68IurYrP8A" +
                "VeYj8wRT9p263LQWLJUf2VCfzxWd7U+6vfCP6Ty9aY6NeXKOrG20XDyW/wB0LIHftVY8ujzc2JR6" +
                "ZpbK3bhfgHTLcrKskO7R34wadO2ZUEhekK3IP9VaSD+lQumPum7XLrh+NX9Y+dXbT1KW0N6irHcz" +
                "VUjnjFPsrot7AOK8TSLj1AZBj86b+5aG6FpVYFKlHActyDz5RVzgZxXlJSUiUg58qehtEV5Gg9NK" +
                "YA9ytwsCCCyQQfXFET070wG4S3ZpUTmFgEfnUrcfs1Dw/g/u4pa5A91v8f1D/wAppjOP6RyOleny" +
                "0FW6UEdlNXBz9jRf9D9PSqWnL0OHjbcrH8a5x2Jhv4U/ic7UyXqV8iyt9l7cpjfw6oeXrTNCbO2j" +
                "qRPSzDKZN9qOOQbpcfrWV+2Tq6x0nRn9A0VYdu7sbLl1Kira33BVmScj0E1n7urai/p+ntv3924h" +
                "xJ3pW8ohX7Q8gnNV7qH/AMY1/wCkf+Y0UuR0RDdu+Uu7RI2hIgz5V5xJZaCVfiBg0rbD8f8A6g/Q" +
                "V69/En+7/E1VDEa6SFFMZPIom1v+19qUcA8XgUjQCf/Z";

        map.put("positiveImageStr", tup);
        map.put("negativeImageStr", tup);

        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQueryIssuerByVeh() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1006");

        Map map = new HashMap();
        map.put("plateNum", "云AH8J76");
        map.put("plateColor", 0);
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }


    @Test
    public void TestOrderConfirm() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8907");

        Map map = new HashMap();
        map.put("posId", "6301019999901050004");
        map.put("userId", "100002");
        map.put("type", 1);
        map.put("orderId", "010c62ff4c344292accafcfa3371111b");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQueryVehCheck() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8800");

        Map map = new HashMap();
        map.put("userIdType", 101);
        map.put("userIdNum", "632123198911251536");
        map.put("vehiclePlate","青BU9592");
        map.put("vehicleColor", 0);
        map.put("vehicleType", 1);
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQuery8805() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8805");

        Map map = new HashMap();
        map.put("cardId", "6301111111111");
        map.put("plateNum", "青A3452A");
        map.put("plateColor",0);
        map.put("channelType","010001");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestQuery1001() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1001");

        Map map = new HashMap();
        map.put("type", "root");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestQuery1009() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1009");

        Map map = new HashMap();
        map.put("pageNo", 1);
        map.put("pageSize", 10);
        map.put("userIdNum", "630105197709181336");
        map.put("startTime", "2019-07-07T00:00:00");
        map.put("endTime", "2019-07-16T00:00:00");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestQuery8804() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8804");

        Map map = new HashMap();
        map.put("id", "青FPQ123_0");
        map.put("userId", "63010114021800096");
        map.put("type", 1);
        map.put("contact", "联系人");
        map.put("registeredType", 2);
        map.put("channelId", "6301019999901030001");
        map.put("registeredTime", "2019-08-09T23:32:23");
        map.put("vin", "vinvinvinvinvin");
        map.put("engineNum", "engineNum");
        map.put("issueDate", "2019-08-09");
        map.put("name", "name");
        map.put("plateNum", "青FPQ123");
        map.put("registerDate", "2019-07-09");
        map.put("vehicleType", "家庭自用");
        map.put("outsideDimensions", "223X344X5454");
        map.put("accountId", "633253453453");
        map.put("linkMobile", "136556565");
        map.put("bankUserName", "b银行e");
        map.put("certsn", "500334143213455432");
        map.put("posId", "6301019999901030001");
        map.put("genTime", "2019-08-09T23:32:23");
        map.put("trx_serno", "546465466454");
        map.put("employeeId", "9999999");
        map.put("org_trx_serno", "4645645644444");
        map.put("acc_type", 1);
        map.put("bankPost", "63010102001");
        map.put("channelType", "010001");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQuery8807() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8807");

        Map map = new HashMap();
        map.put("OBUID", "6301061310201059");
        map.put("plateNum", "青BG7636");
        map.put("plateColor",0);
        map.put("channelType","010001");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestSendBank() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8802");

        Map map = new HashMap();
        map.put("accountId", "2806001119200168106");
        map.put("userType", 2);
        map.put("linkMobile","13892914451");
        map.put("username", "工行（测试）有限责任公司");
        map.put("certsn", "555555666666456789");
        map.put("protocolNumber", "000000");
        map.put("posid", "630101003");
        map.put("genTime", "20190724153749");
        map.put("trx_serno", "0724153749100301");
        map.put("employeeId", "999999999");
        map.put("channelType", "010001");
        map.put("bankid", "010001");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }


    @Test
    public void TestResiveExp() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8931");

        Map map = new HashMap();
        map.put("orderId", "630000999996300000099911");
        map.put("delivery", 1);
        map.put("expressType", 1);
        map.put("expressId", "435354333");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestOrderQuery() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8932");
        Map map = new HashMap();
        map.put("orderId", "630000999996300000099911");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }
    @Test
    public void TestApplyOrder() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8920");

        Map map = new HashMap();
        map.put("orderId", "630000999996300000099912");
        map.put("createTime", "2019-08-02T10:01:01");
        map.put("userName", "测试一");
        map.put("userType", 1);
        map.put("cardType", 1);
        map.put("payCardType", 2);
        map.put("mobile", "13512352209");
        map.put("userIdType", 101);
        map.put("userIdNum", "500221196503219032");

        map.put("address", "青海省城西区地址");
        map.put("channelId", "630101880030101");
        map.put("channelType", "040301");
        map.put("plateNum", "青A223A4");
        map.put("plateColor", 0);
        map.put("vehicleType", 1);
        map.put("outsideDimensions", "223X332X214");
        map.put("vin", "DERT345343");
        map.put("limitPerNum", 5);
        map.put("totalWeight", null);
        map.put("engineNo", "engineNo453");

        map.put("wheelCount", null);
        map.put("axleCount", null);
        map.put("axleDistance", null);
        map.put("axisType", null);
        map.put("orderType", 1);
        map.put("orderStatus", 2);

        String tup = "/9j/4QFCRXhpZgAATU0AKgAAAAgABgEaAAUAAAABAAAAVodpAAQAAAABAAAAZgEbAAUAAAABAAAA\n" +
                "XgEBAAMAAAABALoAAAEoAAMAAAABAAIAAAITAAMAAAABAAEAAAAAAAAAAABIAAAAAQAAAEgAAAAB\n" +
                "AAiSCAADAAAAAQAAAACQAAACAAAABQAAAMykBgADAAAAAQAAAACgAQADAAAAAQABAACRAQACAAAA\n" +
                "BD8/PwCgAgADAAAAAQEaAACgAwADAAAAAQC6AACgAAACAAAABQAAANEAAAAAMDIyMQAwMTAwAAAF\n" +
                "ARoABQAAAAEAAAEYARsABQAAAAEAAAEgAQEAAwAAAAEAugAAASgAAwAAAAEAAgAAAhMAAwAAAAEA\n" +
                "AQAAAAAAAAAAAEgAAAABAAAASAAAAAEAAQA3AAMAAAABAAEAAAAAAAD/4AAQSkZJRgABAQAAAQAB\n" +
                "AAD/2wBDAAYEBQYFBAYGBQYHBwYIChAKCgkJChQODwwQFxQYGBcUFhYaHSUfGhsjHBYWICwgIyYn\n" +
                "KSopGR8tMC0oMCUoKSj/2wBDAQcHBwoIChMKChMoGhYaKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgo\n" +
                "KCgoKCgoKCgoKCgoKCgoKCgoKCgoKCgoKCj/wAARCAC6ARoDASIAAhEBAxEB/8QAHAAAAAcBAQAA\n" +
                "AAAAAAAAAAAAAQIDBAUGBwgA/8QASBAAAQMDAwIDBgMDCAgFBQAAAQIDEQAEIQUSMQZBE1FhBxQi\n" +
                "cYGRMqGxI1LBCBUzQmJy0fAWJIKSorLh8TRDc4PCRFNj0uL/xAAZAQADAQEBAAAAAAAAAAAAAAAB\n" +
                "AgMABAX/xAAlEQACAgICAQUBAAMAAAAAAAAAAQIRAxIhMUEEEyJRYTIUM3H/2gAMAwEAAhEDEQA/\n" +
                "AIXQChrR2ZHx7JntnMYqSbuQ/eW7KCCslLcCRJJj/CoVhRatEgzASBj5VJdKqVcdR2CCCpHvDaTG\n" +
                "ZyKo+I2IdFaa2m3s2reB8CQkH5Cl0q2LlXHBoPDiCMJA+1C88g4/eEYrgUW+SWrZC9J3L14bwund\n" +
                "+EgxHM9qnWv6/b4ajtFtrXSfGQ14ig5kzH8KkU3DBmCqYjinkrfBnBhWhJPE0R1pK7gEpSSO8cUu\n" +
                "y9bgEhZk+lACySohxOZHel1YukqEWhvWpRwDigMLdMCEpmfWnLfhITHiJJ+dEShBSsb05gfiH1ra\n" +
                "sGsqE2EbpPE4pFdlavPnxLdlYyCSgTHzp+ylKUAFSZk9xQIQEuSowCSJPFHlB1kkiGHTmlvqBXbQ\n" +
                "QMbVqEfSYrx6fbZum3mbq5b28JC4+kgCpttAQvNLKZ3Y79qZSY62oa2ydQbUknUCpsnhbYMelP7a\n" +
                "4vVKBdFsps/ulQIHyIpu22oSkzB/KlEpUgTyPlzRU2FNkmlyeRFHkGo7co4g/Kk1FYIKTTbj7ErI\n" +
                "pjqGk6fqTZRqFja3SDyl9lKx+Ypv47hHJBjzoPeHgYEkelb3EDdFN1f2P9MX7q3BpemDcZ2e6BsD\n" +
                "5FooNVLUf5PugOEqYsig8xbXq0Z/9wL/ADNbCm8VxuIPeaUReKgZBo7oO6Ob9U/k9lpZXZ3usWwM\n" +
                "knY3cGex+FST+VVzUfZL1HpynPcuoGwlcH/WGH2CTEfuqHHrXWv84eaMehoxvmVCHEmD2Io7I26O\n" +
                "NX+levtPKmkCxvmkx8KblpwLOcgKIPfyFQl1YavY3Km9a6OU8UiV/wCqwATJB3JHH1rtS7s9EugR\n" +
                "cWjBnkhG0/cVD3/S3Tb6t4ZW2oceEqCPl3rbI26Xk4rcGhB9X85aQ/aFU/s2FrSUHH75Pr2ps5p2\n" +
                "gPqHu9/cW6FEhJeCV8ecRXYWp9BaJfOlxd3fhRSEHxj4uPL4u1Qmr+x/QNScCkvNAeGG4VaIHBmf\n" +
                "g259TJoXEyyr7OTXOnwpKl2moWzyAdsmUyfLv50wf0i8ZO1aET2+ICfUTXVF9/J+0q7Q2mzftm0I\n" +
                "T8U+KkqVjJ+IjseI5qlaj/J+11lShpOqWz7JzBlBB7ASc1uBvdMCXaPoEllZ+Qn9KbLBSYIIPqK2\n" +
                "q79h/WlukeAm0vFEA/s30yn0+KKitU9lnXNgAXdDfuRAI93SHAPmEGhSD7hk8UUirrqfSusaa0pW\n" +
                "rdP39qmAQpy3UkGeMx/GotrS7e5CtiHmlDGRiaNB3K4aJA86sbnTjhRvbfQEg7T4gKYOf8KBPS1+\n" +
                "oAzb5z/S/wDStQbNauU+HbwVfEoyCDzU97NyFdS2JWkFXjTgenNVrVN3vACEn4UgbYn/ADzVz9kr\n" +
                "CT1W1I3LS0pYHITj/rVsnEWQN1KlBRTmDkUntgExkUYndCpyK8TmAZBFcy4SGXQiqd6TkA+VCgAL\n" +
                "VJmfOvBJUMHI7UoEmUmZBrDACEiRXklMScGjFJk0Uzs9aJgQDM8igMkcUDijEedACQkRyfSsMhRB\n" +
                "G2DzRt20Z5pNKQASczRhnKj96zHR4rUZEx9eaOl5UEFSs4gmkjgSRmhRGBExk0pRJDkPuBJ2rI7U\n" +
                "IunEpjecfnTczAnjnFe4jyGTQNqhVx51RSA8pG0ZKYpU3jneDPGKbJyY+poVGFA96wHFDn3tYytK\n" +
                "TRFXKuQmB86QKsngmg3E/KsD24vwOveVHCkzNAHUqGUQY7U2+ZzQgxBn51qA8UfoWU632UsCgDqV\n" +
                "D8ZGO4mkFKMyOKISDBFakI8MPoceKCcnHGRRQpClQpKZHekCSPWjzOYo6onLBEcoQyrO7b55pwlN\n" +
                "s2kFbgX8uajd4zgiiGCOSKOqE9mJNLuUBISyBB8+aD9i4jcFpB+fFQTiiCBk0YKMJjmtQXiRN7Uq\n" +
                "GVJ3DPNHCAtGYnioYrIzJ9K8HSsQSqO9bUX26H1xbtPMKafQlxH9ZKhgzWRe0TouzRchaG0pYuEE\n" +
                "oIGUKHI9RwYNamytSBgmPU1WPaheNs6RZK5JfjnttM0YppmVxZzZdNeE1qNpdNJDjcggCQZMCPpm\n" +
                "ohttkNpHvTnA7GrZ1irbqV4RG4obEcT8PeqsGFx+JFUHLVqSkOXaypW0gnM1qXskt2G79ToSo3KG\n" +
                "sKJxtM4/IVkLzqveAlW1Y3zIEGtz9mTTW57wwSuQhZPEBMiPvTZn8Reky+/CkqUBgmigQCIAPIop\n" +
                "ISSkYTkEUEyIBO4cetS8DIj9W1VOmIZccaWpLi9h2CduDn7040m+GoWfipbW38ShtWIODRb+xZvf\n" +
                "CLwUQ2remDG1XY0rp9miyZLbWRuKshI5MngCm41/RVtt+DW61e3t79u2Xu3qABKUkwSQAMd80/jE\n" +
                "fWobUdD8bUF3TDxbcWnaCrcSgyZ2ncInyiMVNdpOPQVpJeBse3OxTequsv5j1lmz91LyCwt5S0yS\n" +
                "IE7ccE+v2NOdA6s9+sby6vLdVsmznxYClYkwQImITnHlSOvdPX991Cm9tVteCG/CU064shczJKfw\n" +
                "7fwkpIzsGRmUNF6WvLLpjVdIuPDKH0lCFJAC3UwobSqe42iccq85rFCTsOrbC8vLBg7m3bu394SF\n" +
                "BQjKBHGcr59PWj33V2lWupe5PPhTucIIVkRIwcRI5qM0zo93T9UsLplxIdtmShKs7EtlQlkCSY27\n" +
                "j/eg8ACmms6VqL/VTl0/4iWiwEMpaQ68lEqMzChkhKCRGIHrICmy66XfW+p2yLmzd3sKGDEZ8iKe\n" +
                "JCinsN1RXSluGOn7FCbcW5LSSpvaQQTzM5n55qb2gCRwO1JRVPgQzJk8mKOrnBihIjIoiomTAPFA\n" +
                "Y8SJ/OaAgGTyqg3J8jQD4sj50A0AVeXI5r2JycGvEyJ4jkUYbe0ZoGA3JGRJoCYOOKMZg8Ck1YEz\n" +
                "iiYFRIHmKJIIk4rx3bhFeiPKiBgQJwaPJ880QfT6V7cAZn7UUSkeJKuAfOvAhMnAooUJoVGcJiiI\n" +
                "eJBnNGQfM0UyCOK9uABJwaxg6jnFeSYH4fnSI5xmlQvAkCihWHk7OKontQVvsdOSfiHirJH+zH3z\n" +
                "V4UraiKz72iPoTqemoUoltDTi1tkxgqgH/hNHyTZivUilP3F4UhSj42wnv8ACAn+FVtT7kn4Ff5+\n" +
                "lP8AUbgLQlwqIUsrd2ycEkmq+Ll0CPGVj0P+NMOXe3R4l8mVGCY4rf8A2bNNM2La0FUOqUtZUI+K\n" +
                "Yx9qwzRbcv6ikFQwNxIIMYrfejGmmdNsvdwS0trcCrnOZ/Wh6h1FEsnCRZHIClFR5NE3HuTI4oJB\n" +
                "EK570QkrHEZpSi6DLebQpIWsJCuxMUs0oLSFJVukSkg8iq9r2l++XVu+lpbmxtaSEqgAymMSJ7zn\n" +
                "innTzDlppFuw8lSXUICfi7EDjk/rT6qrFjJuTVEotQ78Ez8jXgqTMHyqmCyvBqD7ykOBpy4QopEj\n" +
                "4SpU/PmflHlVvQSE9p/WtKOo+ObkuVQtMQAYNLRI+KIrKeoXtXV1BfLtbq7atmkKcUQte1oQkDCZ\n" +
                "HKVmtPB8Qdx2z2pSo4UpJPeCKTIE8xXgoBMd6geotWu7G5aRbNBaFAFZ8NS4lQAOO1BJydIEpKCt\n" +
                "lgSc17fgiTk00snXnLJlx9ADykDcEggBUZ5z96g9M6k9+1JFobcoUtJUCVZ5Pb5bfqayi3Y+8YtJ\n" +
                "+SyOGcUXE5M1EX+tC0uS0LZb8KSlRQtKSkqBIkKIxjByCTHan1hdIvLK2uW23EJebS4ErEKAIkA+\n" +
                "tIyyHG4A/hNGEHKea8ATkijCDkc0ANiauQr6EUAmSOPIijHCoOAaTWYgiMYJoBFDJEjv50nwDJoU\n" +
                "qIzmDXsEek0QHokg0he3VvZWztxcupaZaSVrWswEgck0ufhE8iue/b51iq61I6BZuEWttCrjaf6R\n" +
                "zkJPoP1+VNFWyc5UiR6v9twQ64x03ZhY4FzcTB9Qkdvn9qybWOsNf1h5bt9q12pSj+FLhQgfJIgC\n" +
                "oKSpcnv50oU8YxVUkjm2bJzQ+rNa0a6Q9ZahcJgyUqWVJUPIg1uXS3tg0e+tmUayFWV2QAtW0lsn\n" +
                "zB5A+dc57TE7YivNmJkcVmkzXR22y61cNIdYcS42tIUlSTIUDwRQnJ4rn72J9YXtvrlpoVw6XbC5\n" +
                "3JaSo5aUASI9DERXQU9qnJUUTsAoAMjvR4SR/GiKmcGhSrHasmBhXDCoAn0msk9rV4GNTuUIX8ab\n" +
                "BIUo+at0D8xWruq2pJwTzFYT7X356kvtp3BfgNecEJSSP+E/eiuxDNdTfcSkJQAraiBMHyFINBfh\n" +
                "I/Zt/hHYUe9cZLiwlrClBG4Kz/nFTM2wxs49Kcxaui0sqvHnLgLS0AAqBmDzFb1oTKGEoZbBDCGg\n" +
                "GyeQOM1lXSaLR5CPeAEJUvYdo8zjB861qyw4tJgBKQUmp+p7SIzduI7xOBg0UJHiZEzR4jJOa8on\n" +
                "BT9KJeqBwlUEAChgQRM+VV/XtSuba/YZt/DIUWiQpYScqVPbuExUnY3Dtzp9u+oDeppKzHEkVTWl\n" +
                "YsZptr6HipgfvAfegSAe+P0qB0vVru71R61fYQgNpBkTjKsZGcAVPtJP4oMGg1T5HhJSVobO2No4\n" +
                "FIVbslCl+IoFAIUr94+tOp2TmZzUK9r7aXHG02r5eRcm1CJSCpQbDkgzEbfM0Wy6lsb42xt0Plt9\n" +
                "SUIdKQE71NhwIOZnaR2jtNLQ9k83BJUcUi7btPuFa2m1q2lBUpInb5fKm+nXzOpW5ft1EthxbfxJ\n" +
                "KTuSopOD6g0ldavbW14m1Upe9SQZSkqAngGO+DQp+BrXkkGrdttgNIBS3kABR701Z0qzafS823C0\n" +
                "TtMnE8gemePl5U694Ztmg7cOIbQogblnbkmAM+pilA4guFsKSV/i2zmJ5iltoZpMhtW0D+c1Poub\n" +
                "p33V4JCm0iCAnKQD/eknEnA4EVLWrTiGEB9xLjiRClJTtB+kmKSdvrdFwGFvIDpIGwnueBTkL4EY\n" +
                "9DQaY656DKJjtFBIJkjjmiFUk5NClXn8jQCHI7fUUmRCiDwe9GJ4xPIohVIgjjigZI8EqHGRRikK\n" +
                "SO1C3xBOK9GDAI+dEDYRf9GZrinX7l7U+pb93aXH7m6WoJ5ypZgV2Xrb4tdHvbhchLLK3DHOEk1y\n" +
                "b7O9POoa8u6XlNsndJz8asD+NFy0i5EtXOSiTHTvs+cuUBzVngyD/wCU1BV9TxVlV7M9NMFNxcCB\n" +
                "gYpMpWzeFbOupYfJ2hC4Mnyg1c9HuLlxjbdhtawPxo4V9K5JZp92d2PDDpozXXuinbS0X4KUrbTn\n" +
                "cJx8k8k1UtY6auNO062uHUqDr64DZ5SM81suvOX6T+xuWLZqclaZqHvtPVd26HXblL5QFKgRE7SJ\n" +
                "H3o488l2DJgjLpGb+z54t9baEEyD740AfKVAV13BCcVx90iFN9e6GMpA1BnMf2xXZCk/DXbLk86P\n" +
                "HA0UTkQaJKueB3pytPnSKk4I7UgWIXaw1bLXk7Uk45rnj2mPpc6uv070qQq8cUVGTAQIiP8Aaj6V\n" +
                "0HdJ3thGZKkpx5FQFcz9XOJd6o1EtuBQU46sLIx8TipP2CYp49k/JWFIZURueVl0kBSPsfzp5tUa\n" +
                "K3aFTTC0ltUElRChMzz9qe/Ee4+1UMzSPZ5duXV3aN3Swre7j4RJKQVDt6Vs9kUy+FERtkY9Ky72\n" +
                "W6cyly0uHd3jpQp5CYwZhJJ+h/OtPbzb3rm34NhyMEYqGfmSIZP9iQ8/DMmRRxgYGPSk+FfDiOxp\n" +
                "TjAwTTHSEcZZcPxtoc9FJBpVDaENhASAgDaEgYA8qYvanZ21wpt59CHRtlJPnxT4KSpHiFQCYndO\n" +
                "IpqYqafQizp1oy54rDO1w8ncc/PNOjAxzSSXkuLKEqBO3dAOYPB/WlFGBjmg78jqvBFXujM3LbwK\n" +
                "ygOvB9coSsFWwJ4UCOAPrTW36WsWLu0dbkt2gT4SNqRBSjYM98fn3jFTm5IJkyf0owJUZJgelC2N\n" +
                "Q20+xRZsqb3BW55x2SP31lUfSYprf6Hb3l43cqU4FpUJgAggBUAA8ZVNSoIiUgR50YkHAOa1tdDU\n" +
                "n2R2qad73YoYMLKXWly5BwlxKifnANEbsfD1p+8CW072ENbh+JRClHP0IqUUfWKIDjv9aWyiRBO6\n" +
                "S47qIuyv4zcJWUgiAhIAHaScefelup2Ll/SFt2RIWpxrdCZOzenfgET8M4qXrw4ouTZoxUeiK6fb\n" +
                "db0tpD7fhqClwCCDt3q2mCTEiDHaYxxTDRbO5ZvGVvNFMpWpZ2AZJGCQozyeQKsB4OYijgcZoJ0q\n" +
                "DKFtM9yPrXime1GBwKAzSjHgIxOKFRPeikHnyow4oisb3jKbmzet3BuQ6hSFA9wRFc9+zzTvcF6o\n" +
                "wtG1wXKkkeiSQP410VBis/1jplnSHV3lupag+6SsK7E5/wAalmtwobDW9shjolk6pK3GUFaVhxJI\n" +
                "4UO49adW4Qyrw2xCU4A8qctuJCM0wbDqLh5SXRtVwCmYribtHpxSsNfaTbarbLbuk721DaRPI5ps\n" +
                "/o7Fmha7dARKYIHGBAqT01KmwQ44FLJzAj8qNfS4Nick4rJ8UFpdmZ+zHpldz7UQ86ApixCrg/OS\n" +
                "Ej7n8q6Tmql0b00rRbm8uXnErduAlMJH4QJP6k1a1KxXpxk2lZ4uRLZ6iauTSak5NKHJxRFCaLJs\n" +
                "Y3ig0PEWoISIlU8QZmuUdTDjtze+EPFKCBuAzAAn85rqLqdwI0e7lQSA04Nx7S2rNcnXrx2OLBMP\n" +
                "PKgR5n/tTQJ1yPmG3WWGELQAkNyrHp/1rylvbj8Dn+7/ANa8krLpSCpKSI5qMceHiK+Bvk//AE//\n" +
                "APVUMdP9BWbKWSvIdbZSgDsAc/8Axq1J3I029BgSlST8o5qH6SYa9wfdQVKUpYSvEbdox+tTCUJd\n" +
                "sVodOVKPoSCQM/571z5P7Izd5BzIJ9J4NKDIHI/Ok0/lSqM8YqiLkJd6I49euvMltAU6h07VqSTt\n" +
                "EZipa9tzcWDzCYSpxspzIiRHanaMDz+lAv0qjk2TjFK6IDQ9Nfs751dwiQUpSlSXCRgnkfKKmboQ\n" +
                "0shzw4SfjwdvrmlCSM021a9TYac9duIUpLYkgeU0j5dlIpRVIzjpXTLy21u1uVPOpS7Bhbagkw3w\n" +
                "D35I57TT64c1lTtw6q5R4Fx4ykjaofDLTeDP1BjsT3xP6T1Rbarf+4ptrht4tB34wB8PmRMxOAe5\n" +
                "nyp2da073YuB0uJ8RTMJbUslSfxQAJMedYcqnVms6lZ6y42glNu02m4ltwzs2uJ2gbQAeTJJ4HlQ\n" +
                "dQ6xrVq6WLd1SUGyacbdkbtxXkqxtBJgYHAMc4txu9Mfu0WbiELuHkQkLYMLSBOFEQRB8+9EYudH\n" +
                "1Z5NuG0uqUmR4jCglSU+RIgxuP3rDEVc6pqDfR9vqC7tlpQQPeH9m+AcFxIBAxzEcA4nk95rd7Yd\n" +
                "Kpv71dqi5c8PwxG1MKUkGZPIBJPYRVnesbV1TTjjDalNCGyUg7fl5UzuNDsbi0VaOM7bdS/EUhta\n" +
                "kAn1CSMenFIOrIbprXbnU7a+Kgw88w6EoS2rbuBSkzOe5P2pPpnqW51N+4besyENI3koUCc/Egc9\n" +
                "0KTHGQe1TKdEsmbZ+3aDiGn1bnAHVfEe+SZE94pCy0K2tHLkMre8G4kraUuUkkJEz+LhIAzgcUOC\n" +
                "iTInSOrRe6hbWr7aGVPMIdKV7kr3KSDCRBChk5BxjvikB12gXlswdPeHiulslSwCkbyhOPMmAR2M\n" +
                "+VSth0tZWV43cW5WgIUpQbEbQCIAGMAfwHYAVHP9CWi75V0i6eaWXg8hKUICUkOlyOPMnyMedbgz\n" +
                "sfv9Thq9dthaOuFC9g2ySuIkj4YMTHNSeo6xa2LrTdytSVOLCBAMCQf8O3+NRbnTI8Zy4Qq3Vc+K\n" +
                "t1suMSAFTKVQZPIzI/CKW1nS7i8vLV0KRtYXvgPONlQ2KTHw4GVTNZ0Dkm2nkOtpUggpUJBHelQa\n" +
                "a26FeGgLCQoAA/EVfmcmi319a6farub59q3YbEqccUEpH1NKjSHh55qp9faxZ2Ni3aPOD3u7VtYb\n" +
                "7kpG4n5QDVe172w9O2DahYLe1B/gJaTtTP8AePb5TWPO9QX3UHWVprN+o7feQyhI/CgKSqEijOHx\n" +
                "ZKM/kkjTre6Fy1CVcjPmKQVYnxCrxLklWAUr4pi+w9bOC5tMjug8GnDGvKQNrlo9P9kSK8r/AIez\n" +
                "CVEpb2q7dXiF1wjslSpFTHTLab/VgFKBS0PEUPPyqsuak/dgJaZU0DyVc1A33VD3SvU2ivNEqaUp\n" +
                "w3LY/rtwAftM/SqYI7TSJ+onUGzoMRFEXzUdo+tWOrWibjT7pq4aV3QqY9D5Gnylc16VHlIIgwTQ\n" +
                "KMGhHnOKIrAnJrMVlZ9odwm36S1N5SwkIZUZ+nH14rlzxVIVaMCJUUiYwPOujfbK4lr2f6md4QXA\n" +
                "ltIPclQ49a5+0oMPa2yh1ISkJk+mKpDomeadUourURJzgd8yP8/9oqfRP2TUxd+7pbdNoXQzK9oc\n" +
                "AUSJiIqF2D/7qfuf/wBaYB2J0+EW+jNrt9xSsla553cH6YqTQla7FslIzB+WZprapYb0thFuVbA2\n" +
                "FJKuTOc/enqdwt2/iAwJH0/6VzSdzOZczYk0vkEcUslRGB2pAEBR4o7ZmcQasdQ5Qo5xSk0giYmZ\n" +
                "FCVCcYo2ahQnIGPWaj+pNOOq6S7YpdUx4pTK0p3EAKB49Yj607D3x/FBFChYWoqoWMkVjRulE6dq\n" +
                "Tb6VtOIbK1AlBCpKQmcHbODwBzR9Q6a96dS8FtrU2t9QDoO39oQZweU7RH8Ks2QntmvGMJ+9axqI\n" +
                "y105SXHXLl1bq1NJYQqSFBAGcjuVSSR5DyqJ0Xpsabrhct0v+CCsqdddlS5IIAg8fMdhVqP4xnAo\n" +
                "U/jV9qVsdIUJBn0rwG5J86SCwJCvOjbingyKBRIKoSSCD8qDbAzyDQle6SOaHcFAeo5oDcgDA+sU\n" +
                "P0oiefOfSlQkzGKxmwNsnyopSJNHOJkCg3A8iKwCN1+6Om6Lf3qAkqt7dx4A8EpSTn7VyJ1V1ZrH\n" +
                "VF2XNWu1LQPwsp+FtHyT/k11P7SHg30NrqsT7m6M+qSP41xsomTHNVxrg5szd0OLcyBuJBjymrmb\n" +
                "I2vQlrepH7UXibjngA7R/n1qhuP7fhRlQ71dndcb1D2cqt0pS3cWhaacT5gKEL+v6zRmrVAw8Ntm\n" +
                "o6S8LuxbXyFJmhFvtcJSvZ5io72b6pb6xoaQkBNwxCHUesYPyNWxVmlRkxXjTg4to9uDUoqSIhQS\n" +
                "y0pYrKdUuTqfWzJUCq3aV4UEdiCD+ta7qtsSwUp8qybWHmNI6htG8Fxx5C3CBlKZ/jV/SL5EPU/z\n" +
                "yQy77U+lteeOm3b1s6hZEoVAV5EjgyK0TpT23Xbbga6itEvtYHjsfCofNPB+kVS/aa6wdXtFtgFT\n" +
                "zErIEZCiB+hH0qkqXBCAIr06tHkybjKkduaPqVvq2m297ZKK7d9AcQSCJSfSnUxiq/0AyLfovQ2/\n" +
                "Kyan6oBqccgGosezLvb27t6VtWFLjxb1BAjsEqJ/SsPsyX3niwtz4UZnaASYnk1r/wDKBXut9HbU\n" +
                "5EOOLCf3jtifkJ/OsYtTFq+W1IUVd/zqsehGOrkpVYIMhQgGeNpJnj8pqCK8nmpi6KmrZpAgEAGc\n" +
                "T+Hz/wAKh9w9fvTAO2H20tW6UJSoJQgJSfQDFSL60+7oO348CkLlwFmCnNKOoX4SfEAIMR6YrkX9\n" +
                "HNjXyEimUkmjowADmjQCIMTQD4D6VY6xQ4jb9qKTKSFD0pNFyy4va24lSgSInMjn9aK5fWyXC2p5\n" +
                "reMFO8TPyo0w2jyllKfhAJpZA43TSSijxE/EATgAnml9wC/xChQ9oWgEgTQbTvooWN0g15SoBVPa\n" +
                "gZAElKzuODxQqkGR9qBak9zXhniAKBSJ7cCfiGaKZBlJxRoEmYrwVPHHrWKBZyFfSgUrskEkGa8e\n" +
                "YAOe9FHIUTkYIFKxhcLG0TwaMmcwaRkJnyP5UKYGUn86wtCh9VGixnnFCI7814cYHasYqvtLsrzU\n" +
                "OidUtNNYU/dPNBKG0xJ+ITE+k1zXc+z/AKqaEr0K+/2W936V16oZGJjmhSABkVSMqJZMalycWvdK\n" +
                "a4wSXNG1FHztl/4UwXp99bpcQu2uWt6YIU2pM5nOPSu4Ph3EbRSakIJyMntR3Je3RyN7MdVVpPU7\n" +
                "LTqilm6/YqBxBP4T98fWugA2riruLO2URuYZUZkbkA0o6w0Y3NI/3a58uJZHaOrBneKOrMy11xFt\n" +
                "ZOOuK2oQkqUTwABJrmzXNQXqWrXN6RBWuU+YA4H2rsvUtH07Ubdxi9s2nWnBtUhScEVWLj2YdHvz\n" +
                "OitJjuha0/oafDBYxM+V5ao5Y1vUHdUvjdvBKVbQISMev3JJ+tR7aVOuSZMV1S97IOkHgQixeQD+\n" +
                "7cL/AImmh9jPS4A8IXzY9Hgf1FX2RyuDsvmhMljR7Fg48NhCPskCnbh3CQDIoG4H7NIwiO9A4qFY\n" +
                "iDg5qYzRh38oJ6dX0gFRUpu2WfDHI3K5/L8jWVM27iLDdASVkgDvnFX/ANvxCOrE/GFFNmhISkzG\n" +
                "VGD65H3rOmnXfDtRJGwyM5GfKqoUV1EhC0gDBBInEdqifh/dT9qkNQO9X4SBsGDzzTTwz/Z/3hRA\n" +
                "duPq3NklIntNLvOrc8BKUhOZXiMbSP1iom21e3vrFp+1cCkKiY5Hoal2S44tIUYGee9cfKmiOONM\n" +
                "8oZxEiklyUqSSRIiRzTooEk96IURMV0nQViwtboPNJdYeZQFqUlQWk+HA4OTO7MnzNRz2k3nvb10\n" +
                "4l5TQuHFKaAJ3fFKVABWeB271dwOJHFBtE8UVkoR4k6KL1na6hcXTj7DRKWGYQQFYUQc8c5jE091\n" +
                "hF/NlL6N3gkFIbMFW5M95yJE+QNW2E+WaDaFR8u1N7nSAsKtu+ypOp1N/p5sMHeoO7k7QUHw93wj\n" +
                "Mzg/pSNncXiOnbotLWpw3ASFoWrcmSkGIHb07k1ctkz3BoEtpTgIATHEYpVkKPFfNla6bvbl3VC3\n" +
                "cJeSlbO/atSiAQU/vZ7/AK1aiTB70RDLaV7wgBZG0qjMeVKbT2znikm7dlsUdVRVde1HUbHqC0Up\n" +
                "xxjRnFMs+I222uXluFMLk7gky2AU9ya9e6tfI6tt7KxfRctbgbm3DGLdsoJ3KcnCiQIT3B471O3G\n" +
                "j2VxfNXr7AXctZQpZJCSOCEzAOTmJpuzodkxqj9+0bhLzy/EdSH1+GtWwIkomJgDt2oWPTIO06hv\n" +
                "3NXtg57v7hcX7+nobCD4iS0hwlZVMGVNKERwRmpHTNXurnqTVdPds0MtWrbTja/ElTgWpxMkRj+j\n" +
                "J74I+VL2vTOnsar7+0l7xA4t5DZcJbQ4sELUE9iZP3PnTz+Z2Te312lbqHrxlDC1BX4QjftKfI/G\n" +
                "a3BuUQWka9eXVxqVq7bWqnrMJJXb3BW0VK3fsyopEKG0Tg4UKHprqpGr6VfX6rJxDNs4tA8FfjeN\n" +
                "t5KAADziCBPyzT7SNAXpGkK0+2vllpKNjKyygFs+ZAACj3M80poWiDS13jzz5fubt0OurCAhMhIS\n" +
                "ISOMJFbgKk6G7HVVqvp2y1ZNrdf62sNM2xSnxVLKiAnnaOCZJiBRh1ZaGwt7hLN2t24fXbptUNhT\n" +
                "3iIKgtMAxjYrMx5cim6+mino9nRUradcZQNjziSAlYMhwAGQQciCM96aaj0ihHTFppWlOraftTLV\n" +
                "4p1YdbUqfEdlOVLO5WCYJOaHAOWTDnVGks6XZag/dpRbXqkJYKkncsqwBt5nz8u9SNzqtjZ3Vsxd\n" +
                "3TDL10vw2G1qAU4rySO9QOpdOJHTVnpenJQlFsu2CPEP9RDralZ8yEH5ml+oNMdvLvRnWWm1qt7x\n" +
                "LrqzAKWwhfH+0U4rGdk47e2jdyzbvPsoffnwmlrAUuBJ2jkwPKkxf2QvTae92/vkbvALifEiJnbM\n" +
                "xFV/WdNL/VWiXzNukqYLvjPwJCfDUlKZ5iVmoJNheHqJCFWT4cTrC75V1s+DwfBUkQrzylMc80Uk\n" +
                "I2zRA4NwCVZ8q86qeTgelZx0srVT1ZcOXqHnF3BeNyHWCgW6ULAZS2vhSSJMCcknFObBSdIvOr37\n" +
                "lV25aoUlxSluKUojwQpW09vxEACOKNCWXTcFGJwT96EA9z6VTeiWlOaTqT1rdttpuDLLLT/ji1Oy\n" +
                "OSSCo8kDE+fJDRHrp7okhd9cOXinLhLLzr2xSyl5YQCqD2AGBx2o6m2LqOSJpN0AAkk7YzVe6Cu3\n" +
                "7npi2cvLl65ukKcbecdCZK0rUlUbcFMgwe4ip9xPiDYoylXPrQGQVIATgSRketJoJKiogBKvyo5A\n" +
                "BA3ZHMUDoQlIRt/HgD1oCSOZfbc8R1/qKW3AslLaTtzthtMj8qqNm44XgoqTAQARAE45qa9pl1v9\n" +
                "oWsGEuk3ZTjgEYj6RUQ5cJeu3C2hKIJEiRxjjvVRGJ3LyhvUk4RATuzHf+JqL3r8z+VL3aiQqOJP\n" +
                "1pjmiY05rVL2wfUrT7t5hRMRugEfI1tvSXWbXUWmoS24ljVmhK2lRlQ7p8wawg9TMOA+8WduR2Uk\n" +
                "KTP5kUivUtOcclTam1IV+JC52/LAqc47Bo6aZ6tskSnUw5ZvDBlBUg+oUAfzijI6r0B0laNYstv9\n" +
                "p0D9a5rOsLAHh6zfNhIjL6oA/wB406Gu6iUJDWrYH76Ad0f3k5oc+QpHSKdc0lQGzU7Iz/8AnT/j\n" +
                "S6dQs3Adl3bqPaHUn+Nc5HqPXCI8ayeER/Qtqo6eptRaSnxNK0pYTyXLY/F9lVqYaOjkOoVG1aFD\n" +
                "0UDSwAMz2rmhfUzagoDQLAKPJSpSc+kcUv8A6U2YQgfzM6lW0BSm71ScjyxQ5MdHFQSnFCFfGK59\n" +
                "b6v0xlpKRaa0lZSd227MJM/PNOU9W6YEoi86jQoiSEPFW30yrP0ocj2b7vAgmOaALAy3HOZrEG+q\n" +
                "7FCBs6g1xIVyFoJI+fP5U7t+q2S4kI6sv0bsDxbbdn/cpdmPGX4bKHAQVRwSDQFQJwBBrMGtfe8Q\n" +
                "tt9XW6irADjKc/8ACKe2+qastSm2tc0tSk4IW0AR+YobD7o0VCpHaaNIGTzVDGp9QIH/AI3R3DE4\n" +
                "SR+e6KcJ1XqYGU22luZjC1CT9zW3BtEu3bcftSaiFEpjA5xVQTrXUoWUq0a0Uc8XCh+W2lBr2uIE\n" +
                "u6B8Pfbc8/dNbdGTRZ4xnHfHlXkgkycTmqyjqa+Mheg3Q84dSYFAjqx4KUF6NfhIzI2GB962yDvH\n" +
                "7LYoAIE88mm4IJJzNVxXWtqn4XdO1NBOP6EK/Q0h/pxpQPxN36B/atF/wFHZMXZfZaFQQD3pPdtP\n" +
                "Ge9VwdcaCcqu3UdvjtnR/wDGlUdZdPlQB1RkE8bkqT+oo2ByX2T4EEqKRJ7jmki2FOKXOFCIqNT1\n" +
                "Pobg+HV7I/8AvAUqnXNJWgFvUrIjz8dP+NMTbQ9YabSlWyAknMCJpK40+zubMMPWzDrMg+GpAUmf\n" +
                "ODihRe2ryP2L7LoI5QsGhU7sRzuE80TJHrZlu2bDTKENtpTCUpTtAjyFHSSEEwCUmkBcFJSCk9+R\n" +
                "NeDiVOnMk5EVg0OSfimBkUmlCnVQpQG1UpjmhJ3NjnB7U21i7b0+xuH1K2qbZW58glJJUfQVhGcm\n" +
                "68tu66s1G5Cy4HL55YUjE/ESD+dMmfCClqR4hJMkKii2m3xlKU6B8KpMfmYopKUtHaobeRBOKoiZ\n" +
                "H3B+M9hzSGf8mlXcqPO2k8+v2ohOirz2X6A8ISy813Gx5X8SRUJdeySzCCLXUbpsq/fSlf8AAVrL\n" +
                "n4T86TPFedHJL7Pb/wAfHLtHO3UXs+1TSG1raKLtB5KQQf1qlFx1Ky24laSkn4SeD/kV1dqgHhnA\n" +
                "4rCvaUy0jqFexpCfgPCQO9PDNJ8EcvpscVwile+u4CnHAAAPlTi31Z1tQCLjaAPln1pk7TQ/iFWU\n" +
                "2cssMUWVnXbl0oQXErVHxbyCCZ7T6U5a1NTiAfCbUZiAkADy4z/2qpgCDgV4kjgkVROyThXkubeo\n" +
                "oBgtw4CQpA3CI+fzpwNTQEFRSoEY2hYJ/SqVpr7u9f7VfH7xqxW6iu4VvJVjvmj2AmG75pSSoqKQ\n" +
                "DBK0gAH70sLlkz8TZjnBEflUA+B7wjA4T/y0oVK3OjcYlPf50dEZTZZGnGVZAQf7qwTThC0IWlxK\n" +
                "XEqTkKg1WZKLa92EpyjjH9alNJyhwnJ3J/5TSaIopstaNXfauPHRdveLM5VMx5inbXVN63cB1x5D\n" +
                "gmShTaYI8uKpzT7xiXXDx/WPrVi1G3ZQs7Gm04HCQKVxQVK/BNo68dRcgrtmPAKvwo3JUB6GefpV\n" +
                "h0zqu9urhKRpmos2xMJc8dSYHYwY/WoL2c27JaS6Wmy7P49on71enkp3nA+3rXPkevRaGGM1yLt6\n" +
                "mtKFw7fhJ81gz65pAdSOtuFAuniO+5ptX3MVG6gT4Jz3pB0AWogDmufdlX6SBYmdbU68IurYrP8A\n" +
                "VeYj8wRT9p263LQWLJUf2VCfzxWd7U+6vfCP6Ty9aY6NeXKOrG20XDyW/wB0LIHftVY8ujzc2JR6\n" +
                "ZpbK3bhfgHTLcrKskO7R34wadO2ZUEhekK3IP9VaSD+lQumPum7XLrh+NX9Y+dXbT1KW0N6irHcz\n" +
                "VUjnjFPsrot7AOK8TSLj1AZBj86b+5aG6FpVYFKlHActyDz5RVzgZxXlJSUiUg58qehtEV5Gg9NK\n" +
                "YA9ytwsCCCyQQfXFET070wG4S3ZpUTmFgEfnUrcfs1Dw/g/u4pa5A91v8f1D/wAppjOP6RyOleny\n" +
                "0FW6UEdlNXBz9jRf9D9PSqWnL0OHjbcrH8a5x2Jhv4U/ic7UyXqV8iyt9l7cpjfw6oeXrTNCbO2j\n" +
                "qRPSzDKZN9qOOQbpcfrWV+2Tq6x0nRn9A0VYdu7sbLl1Kira33BVmScj0E1n7urai/p+ntv3924h\n" +
                "xJ3pW8ohX7Q8gnNV7qH/AMY1/wCkf+Y0UuR0RDdu+Uu7RI2hIgz5V5xJZaCVfiBg0rbD8f8A6g/Q\n" +
                "V69/En+7/E1VDEa6SFFMZPIom1v+19qUcA8XgUjQCf/Z";
        map.put("imgHeadstock", tup);
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
//        String outData = HttpKit.post(URL + "pos", data, headers);
        String outData = HttpKit.post("http://10.63.0147:8024/" + "pos", data, headers);

        System.out.println(outData);
    }


    @Test
    public void TestLogin() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "0109");

        Map map = new HashMap();
        map.put(CommonAttribute.HTTP_PARAM_USERNAME, "6399022");
        map.put(CommonAttribute.HTTP_PARAM_PASSWORD, "12345");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post("http://10.63.0147:8024/" + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestAcitiveObu() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1101");

        Map map = new HashMap();
        map.put("obuId", "6333345678166666");
        map.put("isActive", 1);
        map.put("activeTime", "2019-07-03T00:00:11");
        map.put("activeType", 1);
        map.put("activeChannel", 2);
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestCheckObu() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1102");

        Map map = new HashMap();
        map.put("obuId", "6301194080412407");
        map.put("cardId", "63011917230109128673");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestOnlineApplyQueryByVehileIdService() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "8908");

        Map map = new HashMap();
        map.put("obuId", "6301194080412407");
        map.put("cardId", "63011917230109128673");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    /**
     * 向查客编服务查询客编
     *
     * @param userIdType
     * @param userIdNum
     * @return
     */
    public Map postQueryUserIdServer(String userIdType, String userIdNum) {
        Map map = new HashMap();
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1010");
        map.put("userIdType", userIdType);
        map.put("userIdNum", userIdNum);
        String data = FastJson.getJson().toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));

        String outData = HttpKit.post("http://10.63.0.132:8020/bank", data, headers);
        map = FastJson.getJson().parse(outData, Map.class);
        map = (Map) map.get("data");
        return map;
    }

    @Test
    public void TestQueryUser() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1006");

        Map map = new HashMap();
        map.put("userIdType", "101");
        map.put("userIdNum", "63010219880930081X");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQueryCarList() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1008");

        Map map = new HashMap();
        /*map.put("userType", "1");
        map.put("userIdType", "101");
        map.put("userIdNum", "632121195810150019");
        map.put("channelId", "6301019999901020002");
        map.put("operatorId", "800114");
        map.put("plateNum", "青AMW848");
        map.put("plateColor", "0");*/
        map.put("startTime", "2019-08-15T00:00:00");
        map.put("endTime", "2019-08-16T23:59:59");
        map.put("pageNo", "1");
        map.put("pageSize", "20");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQueryCardList() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1010");

        Map map = new HashMap();
/*        map.put("userType", "1");
        map.put("userIdType", "101");
        map.put("userIdNum", "632802199001240513");
        map.put("channelId", "6301010200328020001");
        map.put("operatorId", "710047");
        map.put("plateNum", "青H28278");
        map.put("plateColor", "0");
        map.put("cardId","63011618230206010692");*/
        map.put("plateColor", "0");
        map.put("startTime", "2019-08-10T00:00:00");
        map.put("endTime", "2019-08-16T23:59:59");
        map.put("pageNo", "1");
        map.put("pageSize", "10");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    @Test
    public void TestQueryObuList() {
        Map<String, String> headers = new HashMap();
        headers.put(CommonAttribute.HTTP_HEADER_MSGTYPE, "1011");

        Map map = new HashMap();
/*        map.put("userType", "1");
        map.put("userIdType", "101");
        map.put("userIdNum", "612322198008031626");
        map.put("channelId", "6301019999901050002");
        map.put("operatorId", "800025");
        map.put("plateNum", "青A3L613");
        map.put("plateColor", "0");
        map.put("obuId","6301104010300008");*/
        map.put("startTime", "2019-08-10T00:00:00");
        map.put("endTime", "2019-08-16T23:59:59");
        map.put("pageNo", "1");
        map.put("pageSize", "10");
        String data = JsonKit.toJson(map);
        headers.put(CommonAttribute.HTTP_HEADER_MD5, UtilMd5.EncoderByMd5(data));
        String outData = HttpKit.post(URL + "pos", data, headers);
        System.out.println(outData);
    }

    public String login() {
        String msgType = "0109";
        String reqStr = "{\"username\":\"900034\",\"password\":\"123456\",\"longitude\":\"12.1232\",\"latitude\":\"12.12345632\",\"channelType\":\"010001\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        Map headers = Kv.by(CommonAttribute.HTTP_HEADER_MD5,md5)
                .set(CommonAttribute.HTTP_HEADER_MSGTYPE,msgType);
        String response =  HttpKit.post(URL + "pos", reqStr, headers);
        String token = "";
        if (StringUtil.isNotEmpty(response)) {
            String[] arr = response.split(";;");
            response = arr[arr.length - 1];
            Map map = FastJson.getJson().parse(response, Map.class);
            token = (String) ((Map) map.get("data")).get("token");
        }
        return token;
    }

    @Test
    public void test8800() {
        String msgType = "8800";
        String userId = "900034";
        String author = login();
        String reqStr = "{\"userIdType\":101,\"userIdNum\":\"630104196503203017\",\"vehiclePlate\":\"AB69G75\",\"vehicleColor\":0,\"vehicleType\":1}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        Map headers = Kv.by(CommonAttribute.HTTP_HEADER_MSGTYPE,msgType)
                .set(CommonAttribute.HTTP_HEADER_AUTHORIZATION,author)
                .set(CommonAttribute.HTTP_HEADER_USERID,userId)
                .set(CommonAttribute.HTTP_HEADER_MD5,md5);
        String response =  HttpKit.post(URL + "pos", reqStr, headers);
        System.out.println(response);
    }
}
