package com.csnt.netpoint.netty;

import com.alibaba.druid.filter.config.ConfigTools;
import com.csnt.ins.plugin.netty.NettyConsumer;
import com.csnt.ins.plugin.netty.NettyServerPlugin;
import com.csnt.ins.utils.StringUtil;
import com.csnt.ins.utils.SysConfig;
import com.csnt.ins.utils.UtilMd5;
import com.jfinal.json.FastJson;
import org.junit.Test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class Netty {


    @Test
    public void run() {
        NettyServerPlugin nettyServerPlugin = new NettyServerPlugin(SysConfig.CONFIG.getInt("netty.inetPort"), new NettyConsumer());
        nettyServerPlugin.start();
        while (true) ;
    }


    /**
     * 对文字进行解密
     *
     * @throws Exception
     */
    @Test
    public void testDecrypt() throws Exception {
        //解密
        String word = "MMUcTIwe+HMRBUYAVqdozWhxSB+rjY/HIBo08LsxlPJ/ocVXrvcKPwaMgWEKkApeDylU8RGPOAqsjsNy7Xg+fQ==";
        String decryptword = ConfigTools.decrypt(word);
        System.out.println(decryptword);
    }

    /**
     * 文字进行加密
     *
     * @throws Exception
     */
    @Test
    public void testEncrypt() throws Exception {
        //加密
        String password = "xxxxxxx";
        String encryptword = ConfigTools.encrypt(password);
        System.out.println(encryptword);

    }

    @Test
    public void testLogin1() {
        System.out.println(login());
    }

    @Test
    public void testLogin() {
        int count = 0;
        ExecutorService ex = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 100; i++) {
            ex.submit(new Runnable() {

                @Override
                public void run() {
                    int count = 0;
                    while (true) {
                        count++;
                        System.out.println(count + ":" + login());
                    }

                }
            });
        }
//        ex.shutdown();

        while (true) {
            count++;
            System.out.println(count + ":" + login());
        }
    }

    public String login() {
        String msgType = "0109";
        String userId = "0109";
        String author = "";
        String reqStr = "{\"username\":\"900034\",\"password\":\"123456\",\"longitude\":\"12.1232\",\"latitude\":\"12.12345632\",\"channelType\":\"010001\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        String response = accept(msg);
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
    public void testQueryVehicle() {
        String msgType = "1003";
        String userId = "0109";
        String author = "";
        String reqStr = "{\"vehicleId\":\"青A65131_0\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testQueryObu() {
        String msgType = "1005";
        String userId = "0109";
        String author = "";
        String reqStr = "{\"obuId\":\"6333345678166666\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testQueryCard() {
        String msgType = "1004";
        String userId = "0109";
        String author = "";
        String reqStr = "{\"cardId\":\"63011917230109146031\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testCheckObuStatus() {
        String msgType = "1102";
        String userId = "0109";
        String author = "";
        String reqStr = "{\"obuId\":\"6301181010201027\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testActiveObuStatus() {
        String msgType = "1101";
        String userId = "0109";
        String author = "";
        String reqStr = "{\"obuId\":\"6301181010201027\",\"isActive\":1,\"activeTime\":\"2019-02-01T00:11:33\",\"activeType\":1,\"activeChannel\":2}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testQueryDictionary() {
        String msgType = "1001";
        String userId = "0109";
        String author = "";
//        String reqStr = "{\"type\":\"VehicleColor\"}";
        //查询所有字典内容
        String reqStr = "{\"type\":\"root\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testPullOrder() {
        String msgType = "8901";
        String userId = "0109";
        String author = login();
        String reqStr = "{\"posId\":\"111\",\"userId\":\"111\",\"type\":1}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testQueryUserList() {
        String msgType = "1009";
        String userId = "900034";
        String author = login();
        String reqStr = "{\"startTime\":\"2019-08-01T00:00:00\",\"startTime\":\"2019-08-01T00:00:00\",\"pageNo\":1,\"pageSize\":10}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }


    @Test
    public void testReceiveCard() {
        String msgType = "8905";
        String userId = "900034";
        String author = login();
        String reqStr = "{\"orderId\":\"2b2198cdb3bc4333bd4120be64d939f7\",\"cardId\":\"63011926230208006006\",\"cardType\":112,\"brand\":1,\"model\":\"SLE77CLFX2407PM\",\"agencyId\":\"63010199999\",\"plateNum\":\"辽A2YL51\",\"plateColor\":0,\"enableTime\":\"2019-07-12T00:00:00\",\"expireTime\":\"2029-07-12T00:00:00\",\"issuedType\":1,\"channelId\":\"70000102\",\"issuedTime\":\"2019-07-13T18:05:23\",\"status\":1,\"statusChangeTime\":\"2019-07-13T18:17:46\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testReceiveObu() {
        String msgType = "8904";
        String userId = "900034";
        String author = login();
        String reqStr = "{\"orderId\":\"2b2198cdb3bc4333bd4120be64d939f7\",\"obuId\":\"6301191070803286\",\"brand\":3,\"model\":\"JLCZ-06S\",\"obuSign\":\"2\",\"plateNum\":\"辽A2YL51\",\"plateColor\":0,\"enableTime\":\"2019-07-12T00:00:00\",\"expireTime\":\"2029-07-12T00:00:00\",\"registeredType\":1,\"registeredChannelId\":\"70000102\",\"registeredTime\":\"2019-07-13T19:15:31\",\"installTime\":\"2019-07-13T19:15:31\",\"installType\":1,\"installChannelId\":\"0\",\"activeChannel\":2,\"status\":1,\"statusChangeTime\":\"2019-07-13T19:15:35\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testOnlineApplyQueryByVehileIdService() {
        String msgType = "8908";
        String userId = "900034";
        String author = login();
        String reqStr = "{\"plateNum\":\"青A700A3\",\"plateColor\":\"0\",\"userIdType\":101,\"userIdNum\":\"632123199011130012\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testOnlineAuditOnlineapplyService() {
        String msgType = "8909";
        String userId = "900034";
        String author = login();
        String reqStr = "{\"userId\":\"900034\",\"id\":\"0436f778-6848-43d4-a018-e38d8544c8d6\",\"identityFlag\":\"1\",\"auditDesc\":\"审核通过\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    @Test
    public void testCardStatusChangeService() {
        String msgType = "8811";
        String userId = "900034";
        String author = login();
        //卡解挂失测试
        //userIdType, userIdNum, vehiclePlate, vehicleColor, cardId, businessType, status, channelType, reason
        String reqStr = "{\"userIdType\":101,\"userIdNum\":\"63212219670205651X\",\"plateNum\":\"青FPQ123\",\"plateColor\":0,\"cardId\":\"63015464544444123456\"," +
                "\"businessType\":28,\"status\":1,\"channelType\":\"010001\",\"reason\":\"测试\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }
    @Test
    public void testCardCancelService() {
        String msgType = "8813";
        String userId = "900034";
        String author = login();
        //卡注销测试
        //userIdType, userIdNum, vehiclePlate, vehicleColor, status, cardId, channelType, reason
        String reqStr = "{\"userIdType\":101,\"userIdNum\":\"63212219670205651X\",\"plateNum\":\"青FPQ123\",\"plateColor\":0,\"cardId\":\"63015464544444123456\"," +
                "\"status\":4,\"channelType\":\"010001\",\"reason\":\"测试\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }
    @Test
    public void testObuStatusChangeService() {
        String msgType = "8810";
        String userId = "900034";
        String author = login();
        //OBU解挂失测试
        //userIdType, userIdNum, vehiclePlate, vehicleColor, status, obuId, businessType, channelType, reason
        String reqStr = "{\"userIdType\":101,\"userIdNum\":\"522321199209011927\",\"plateNum\":\"青ZWER11\",\"plateColor\":0,\"obuId\":\"8888021907100233\"," +
                "\"businessType\":29,\"status\":1,\"channelType\":\"010001\",\"reason\":\"测试\"}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    public String accept(String msg) {
        String result = "";
        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
//            socket = new Socket("10.63.0.132", 8023);
            socket = new Socket("127.0.0.1", 8023);
//            socket = new Socket("10.63.0.147", 8023);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            byte[] responseBytes = msg.getBytes(StringUtil.UTF8);
            out.writeInt(0);
            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();

            in.readInt();
            int length = in.readInt();
            result = StringUtil.readString(in, length);
            System.out.println(result);
        } catch (Throwable t) {
            throw new RuntimeException("转发银行失败:" + t.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    @Test
    public void test8800() {
        String msgType = "8800";
        String userId = "900034";
        String author = login();
        //老的车牌唯一性
//        String reqStr = "{\"userIdType\":101,\"userIdNum\":\"632801196810090025\",\"vehiclePlate\":\"AB69G75\",\"vehicleColor\":0,\"vehicleType\":1}";
        //新的监管平台
        String reqStr = "{\"userIdType\":101,\"userIdNum\":\"630104196503203017\",\"vehiclePlate\":\"AB69G75\",\"vehicleColor\":0,\"vehicleType\":1}";
        String md5 = UtilMd5.EncoderByMd5(reqStr);
        String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
        accept(msg);
    }

    public String batchSend() {
        String result = "";
        try (
                //socket = new Socket("10.63.0.132", 8023);
                Socket socket = new Socket("127.0.0.1", 8023);
//            socket = new Socket("10.63.0.147", 8023);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                DataInputStream in = new DataInputStream(socket.getInputStream());) {
            for (int i = 0; i < 10; i++) {
                String msgType = "8800";
                String userId = "900034";
                String author = login();
                //老的车牌唯一性
                //新的监管平台
                String reqStr = "{\"userIdType\":101,\"userIdNum\":\"630104196503203017\",\"vehiclePlate\":\"AB69G75\",\"vehicleColor\":0,\"vehicleType\":1}";
                String md5 = UtilMd5.EncoderByMd5(reqStr);
                String msg = String.format("%s;;%s;;%s;;%s;;%s", msgType, userId, author, md5, reqStr);
                byte[] responseBytes = msg.getBytes(StringUtil.UTF8);
                out.writeInt(0);
                out.writeInt(responseBytes.length);
                out.write(responseBytes);
                out.flush();

                in.readInt();
                int length = in.readInt();
                result = StringUtil.readString(in, length);
                System.out.println(result);
            }

        } catch (Throwable t) {
            throw new RuntimeException("转发银行失败:" + t.getMessage());
        }
        return result;
    }

    @Test
    public void threadPoolTest() throws InterruptedException, ExecutionException {
        ExecutorService executorService = new ThreadPoolExecutor(10, 10, 60, TimeUnit.SECONDS, new SynchronousQueue<>());

        List<Callable<Object>> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Callable() {
                @Override
                public Object call() throws Exception {
                    return batchSend();
                }
            });
        }

        List<Future<Object>> futures = executorService.invokeAll(list);
        for (Future future : futures) {
            System.out.println(future.get());
        }
    }
}

