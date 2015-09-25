package org.motech.test;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BaseIT {
    protected static final Long ONE_MINUTE = 60 * 1000L;
    protected static final String HOST = "localhost";
    protected static final String MOTECH = "motech";
    protected static final int PORT = 8080;
    protected static final PollingHttpClient httpClient;

    static {
        httpClient = new PollingHttpClient();
        httpClient.setCookieStore(new BasicCookieStore());
    }

    protected void login() throws IOException, InterruptedException {
        String uri = String.format("http://%s:%d/motech-platform-server/module/server/motech-platform-server/j_spring_security_check", HOST, PORT);

        final HttpPost loginPost = new HttpPost(uri);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("j_username", MOTECH));
        nvps.add(new BasicNameValuePair("j_password", MOTECH));

        loginPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF8"));

        HttpResponse response = httpClient.execute(loginPost);
        EntityUtils.consume(response.getEntity());
    }

    protected void logout() throws IOException, InterruptedException {
        String uri = String.format("http://%s:%d/motech-platform-server/module/server/j_spring_security_logout", HOST, PORT);

        final HttpGet logoutGet = new HttpGet(uri);

        HttpResponse response = httpClient.execute(logoutGet);
        EntityUtils.consume(response.getEntity());
    }


    

    protected void waitForTomcat() throws IOException, InterruptedException {

        String uri = String.format("http://%s:%d/motech-platform-server/module/server", HOST, PORT);
        HttpGet waitGet = new HttpGet(uri);
        HttpResponse response = httpClient.execute(waitGet);

    }


    protected void createAdminUser() throws IOException, InterruptedException {
        String url = String.format("http://%s:%d/motech-platform-server/module/server/startup", HOST, PORT);
        String json = "{\"language\":\"en\", \"adminLogin\":\"motech\", \"adminPassword\":\"motech\", \"adminConfirmPassword\": \"motech\", \"adminEmail\":\"motech@motech.com\", \"loginMode\":\"repository\"}";

        StringEntity entity = new StringEntity(json, HTTP.UTF_8);
        entity.setContentType("application/json");

        HttpPost post = new HttpPost(url);
        post.setEntity(entity);

        HttpResponse response = httpClient.execute(post);
        EntityUtils.consume(response.getEntity());
    }
}
