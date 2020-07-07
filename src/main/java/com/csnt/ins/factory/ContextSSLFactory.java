package com.csnt.ins.factory;

import com.jfinal.kit.Prop;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

/**
 * Created by cmlin0317 on 2019/11/22.
 */
public class ContextSSLFactory {

    private static final SSLContext SSL_CONTEXT_S ;

    private static final SSLContext SSL_CONTEXT_C ;

    public final static Prop UNDERTOW = new Prop("certissuer.properties");
    static{
        SSLContext sslContext = null ;
        SSLContext sslContext2 = null ;
        try {
            sslContext = SSLContext.getInstance("SSLv3") ;
            sslContext2 = SSLContext.getInstance("SSLv3") ;
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        try{
            if(getKeyManagersServer() != null && getTrustManagersServer() != null ){
                sslContext.init(getKeyManagersServer(), getTrustManagersServer(), null);
            }
            if(getKeyManagersClient() != null && getTrustManagersClient() != null){
                sslContext2.init(getKeyManagersClient(), getTrustManagersClient(), null);
            }

        }catch(Exception e){
            e.printStackTrace() ;
        }
        sslContext.createSSLEngine().getSupportedCipherSuites() ;
        sslContext2.createSSLEngine().getSupportedCipherSuites() ;
        SSL_CONTEXT_S = sslContext ;
        SSL_CONTEXT_C = sslContext2 ;
    }
    public ContextSSLFactory(){

    }
    public static SSLContext getSslContext(){
        return SSL_CONTEXT_S ;
    }
    public static SSLContext getSslContext2(){
        return SSL_CONTEXT_C ;
    }
    private static TrustManager[] getTrustManagersServer(){
        InputStream is = null ;
        KeyStore ks = null ;
        TrustManagerFactory keyFac = null ;

        TrustManager[] kms = null ;
        try {

            // 获得KeyManagerFactory对象. 初始化位默认算法
            keyFac = TrustManagerFactory.getInstance("SunX509") ;
            is =getClassLoader().getResourceAsStream(UNDERTOW.get("certissuer.skeyStore"));
            ks = KeyStore.getInstance("JKS") ;
            String keyStorePass = UNDERTOW.get("certissuer.keyStorePassword");
            ks.load(is , keyStorePass.toCharArray()) ;
            keyFac.init(ks) ;
            kms = keyFac.getTrustManagers() ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(is != null ){
                try {
                    is.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return kms ;
    }
    private static TrustManager[] getTrustManagersClient(){
        InputStream is = null ;
        KeyStore ks = null ;
        TrustManagerFactory keyFac = null ;

        TrustManager[] kms = null ;
        try {
            // 获得KeyManagerFactory对象. 初始化位默认算法
//            keyFac = TrustManagerFactory.getInstance("SunX509") ;
//            is =new FileInputStream( (new File("main/java/conf/cChat.jks")) );
//            ks = KeyStore.getInstance("JKS") ;
//            String keyStorePass = "sNetty" ;
            keyFac = TrustManagerFactory.getInstance("SunX509") ;
//            is =new FileInputStream( (new File(UNDERTOW.get("undertow.ssl.keyStore"))) );
            is =getClassLoader().getResourceAsStream(UNDERTOW.get("certissuer.ckeyStore"));
            ks = KeyStore.getInstance("JKS") ;
            String keyStorePass = UNDERTOW.get("certissuer.keyStorePassword") ;
            ks.load(is , keyStorePass.toCharArray()) ;
            keyFac.init(ks) ;
            kms = keyFac.getTrustManagers() ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(is != null ){
                try {
                    is.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return kms ;
    }
    private static KeyManager[] getKeyManagersServer(){
        InputStream is = null ;
        KeyStore ks = null ;
        KeyManagerFactory keyFac = null ;

        KeyManager[] kms = null ;
        try {
            // 获得KeyManagerFactory对象. 初始化位默认算法
//            keyFac = KeyManagerFactory.getInstance("SunX509") ;
//            is =new FileInputStream( (new File("main/java/conf/sChat.jks")) );
//            ks = KeyStore.getInstance("JKS") ;
//            String keyStorePass = "sNetty" ;
//            keyFac = KeyManagerFactory.getInstance(UNDERTOW.get("undertow.ssl.keyStoreType")) ;
              keyFac = KeyManagerFactory.getInstance("SunX509") ;
//            is =new FileInputStream( (new File(UNDERTOW.get("undertow.ssl.keyStore"))) );
            is =getClassLoader().getResourceAsStream(UNDERTOW.get("certissuer.skeyStore"));
            ks = KeyStore.getInstance("JKS") ;
            String keyStorePass = UNDERTOW.get("certissuer.keyStorePassword") ;
            ks.load(is , keyStorePass.toCharArray()) ;
            keyFac.init(ks, keyStorePass.toCharArray()) ;
            kms = keyFac.getKeyManagers() ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(is != null ){
                try {
                    is.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return kms ;
    }
    private static KeyManager[] getKeyManagersClient(){
        InputStream is = null ;
        KeyStore ks = null ;
        KeyManagerFactory keyFac = null ;

        KeyManager[] kms = null ;
        try {
            // 获得KeyManagerFactory对象. 初始化位默认算法
            keyFac = KeyManagerFactory.getInstance("SunX509") ;
//            is =new FileInputStream( (new File(UNDERTOW.get("undertow.ssl.keyStore"))) );
            is =getClassLoader().getResourceAsStream(UNDERTOW.get("certissuer.ckeyStore"));
            ks = KeyStore.getInstance("JKS") ;
            String keyStorePass = UNDERTOW.get("certissuer.keyStorePassword") ;
            ks.load(is , keyStorePass.toCharArray()) ;
            keyFac.init(ks, keyStorePass.toCharArray()) ;
            kms = keyFac.getKeyManagers() ;
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            if(is != null ){
                try {
                    is.close() ;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return kms ;
    }


    private static ClassLoader getClassLoader() {
        ClassLoader ret = Thread.currentThread().getContextClassLoader();
        return ret != null ? ret : ContextSSLFactory.getClassLoader();
    }
}
