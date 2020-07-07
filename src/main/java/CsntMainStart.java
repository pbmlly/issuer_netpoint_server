import com.csnt.ins.web.WebConfig;
import com.jfinal.server.undertow.UndertowServer;

/**
 * @ClassName CsntMainStart

 * @Author duwanjiang
 * @Date 2019/6/16 18:00
 * Version 1.0
 **/
public class CsntMainStart {
    /**
     * 微服务启动主方法
     */
    public static void main(String[] args) {
        UndertowServer.create(WebConfig.class, "undertow.properties").start();
    }
}
