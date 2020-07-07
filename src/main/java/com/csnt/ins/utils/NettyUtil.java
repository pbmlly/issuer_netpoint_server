package com.csnt.ins.utils;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hx on 2017/9/29.
 */
public class NettyUtil {
    private static Logger logger = LoggerFactory.getLogger(NettyUtil.class);

    public static String getClientIp(ChannelHandlerContext ctx) {
        InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
        return insocket.getAddress().getHostAddress();
    }


    public static int getClientPort(ChannelHandlerContext ctx) {
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
    }


    public static Map accept(String msg, String bankId) {
        Map outMap = new HashMap<>();
        outMap.put("isActive", true);
        outMap.put("errMsg", "");
        outMap.put("retMsg", "");
        //转发ip端口
        String proIp = SysConfig.CONFIG.get(bankId + ".proxyIp");
        String proPort = SysConfig.CONFIG.get(bankId + ".proxyPort");
        if (StringUtil.isEmpty(proIp, proPort)) {
            outMap.put("isActive", false);
            outMap.put("errMsg", "银行目的地址端口异常 银行:" + bankId);
            return outMap;
        }

        logger.info("转银行开始：bankId:{},ip:{},port:{},msg:{}", bankId, proIp, proPort, msg);

        String result = "";
        Socket socket = null;
        DataOutputStream out = null;
        DataInputStream in = null;
        try {
            socket = new Socket(proIp, Integer.parseInt(proPort));
            socket.setSoTimeout(30000);
            out = new DataOutputStream(socket.getOutputStream());
            in = new DataInputStream(socket.getInputStream());
            byte[] responseBytes = msg.getBytes(StringUtil.GBK);
//            out.writeInt(0);
//            out.writeInt(responseBytes.length);
            out.write(responseBytes);
            out.flush();

//            in.readInt();
//            int length = in.readInt();

            String readS = StringUtil.readString(in, 8);
            logger.info("转发银行头8位 errMsg:{}",readS);
            int length = MathUtil.asInteger(readS);
            in.skipBytes(36);
            byte[] data = new byte[length];
            int offset = 0;
            int readed;
            while (offset < length && (readed = in.read(data, offset, length - offset)) != -1) {
                offset += readed;
            }

            result = new String(data, StringUtil.GBK);
//            result = StringUtil.readString(in, length);
            System.out.println(result);
        } catch (Throwable t) {
            outMap.put("isActive", false);
            outMap.put("errMsg", "转发银行失败:" + t.getMessage());
            logger.error("转发银行失败:bankId:{},ip:{},port:{},errMsg:{}", bankId, proIp, proPort, t.getMessage(), t);
            return outMap;
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
        outMap.put("retMsg", result);
        logger.info("转银行完成：bankId:{},ip:{},port:{},retMsg:{}", bankId, proIp, proPort, result);
        return outMap;
    }

}
