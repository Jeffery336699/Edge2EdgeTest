package com.permissionx.edge2edgetest;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

// 在你的Android项目中加载HTTPS图片
// Glide.with(context)
//         .load("https://your-https-image-url.jpg")
//         .apply(new RequestOptions()
//         .diskCacheStrategy(DiskCacheStrategy.ALL) // 使用磁盘缓存策略
//         .dontTransform()) // 不进行任何形式的变换
//         .into(imageView);

// 如果你遇到SSL握手失败或证书验证问题，你可能需要自定义Glide的HTTP客户端来信任所有证书（注意，这可能导致安全漏洞，只在必要时使用，并寻找更安全的解决方案）
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.Registry;

import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

@GlideModule
public final class UnsafeOkHttpGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        System.out.println("有没有。。。。。");
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };

        try {
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final javax.net.ssl.SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    System.out.println("日你大爷的》》》");
                    return true;
                }
            });

            OkHttpClient unsafeOkHttpClient = builder.build();
            registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(unsafeOkHttpClient));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

