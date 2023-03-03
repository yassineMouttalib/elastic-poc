//--------------------------------------------------------------------------
//Copyright (c) 2008 by LEGISWAY.
//All Rights Reserved.
//
//N O T I C E
//
//THIS MATERIAL IS CONSIDERED A TRADE SECRET BY LEGISWAY.
//UNAUTHORIZED ACCESS, USE, REPRODUCTION OR DISTRIBUTION IS PROHIBITED.
//--------------------------------------------------------------------------
package com.django.poc.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * Created on 07/12/2016.
 *
 * @author bboc
 */
@Component
public class DisableSSLVerification  {
    public DisableSSLVerification(){
        setUp();
    }

     private void setUp()  {
        try {
            SSLContext ctx = null;
            TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager(){
                public X509Certificate[] getAcceptedIssuers(){return null;}
                public void checkClientTrusted(X509Certificate[] certs, String authType){}
                public void checkServerTrusted(X509Certificate[] certs, String authType){}
            }};
            try {
                ctx = SSLContext.getInstance("SSL");
                ctx.init(null, trustAllCerts, null);
            } catch (NoSuchAlgorithmException | KeyManagementException e) {
                //LOGGER.info("Error loading ssl context {}", e.getMessage());
            }

            SSLContext.setDefault(ctx);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
