package org.motech.startup;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.motechproject.testing.utils.PollingHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class StartupHelper {

    private static final PollingHttpClient HTTP_CLIENT;
    private static final String HOST = "localhost";
    private static final String MOTECH = "motech";
    private static final int PORT = 8080;
    private static final long ONE_MINUTE = 60 * 1000L;
    private static final int WAIT_PERIOD = 120; //seconds
    private static final int MAX_RETRIES = 20;

    static {
        HTTP_CLIENT = new PollingHttpClient(new DefaultHttpClient(), WAIT_PERIOD);
        HTTP_CLIENT.setCookieStore(new BasicCookieStore());
    }

    public void startUp() throws IOException, InterruptedException {
        waitForTomcat();
        createAdminUser();
        login();
        waitForBundles();
    }

    public void waitForBundles() throws IOException, InterruptedException {

        JSONArray bundles = null;

        int retryCount = MAX_RETRIES;
        boolean starting = true;

        do {
            try {
                bundles = getBundleStatusFromServer(HTTP_CLIENT);
                starting = areBundlesStillStarting(bundles);

                if (!starting) {
                    break;
                }

            } catch (org.apache.http.client.HttpResponseException e) {
                if (!e.getMessage().contains("Not Found")) {
                    throw e;
                }
            }
        } while (--retryCount > 0);

        Thread.sleep(ONE_MINUTE);
        assertNotNull("The bundle list cannot be empty", bundles);
        assertFalse("Failed to start bundles (TIMEOUT)", starting);
        assertBundlesStatus(bundles);
    }

    protected void login() throws IOException, InterruptedException {
        String uri = String.format("http://%s:%d/motech-platform-server/module/server/motech-platform-server/j_spring_security_check", HOST, PORT);

        final HttpPost loginPost = new HttpPost(uri);

        List<NameValuePair> nvps = new ArrayList<>();
        nvps.add(new BasicNameValuePair("j_username", MOTECH));
        nvps.add(new BasicNameValuePair("j_password", MOTECH));

        loginPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF8"));

        HttpResponse response = HTTP_CLIENT.execute(loginPost);
        EntityUtils.consume(response.getEntity());
    }

    protected void createAdminUser() throws IOException, InterruptedException {
        String url = String.format("http://%s:%d/motech-platform-server/module/server/startup", HOST, PORT);
        String json = "{\"language\":\"en\", \"adminLogin\":\"motech\", \"adminPassword\":\"motech\", \"adminConfirmPassword\": \"motech\", \"adminEmail\":\"motech@motech.com\", \"loginMode\":\"repository\"}";

        StringEntity entity = new StringEntity(json, HTTP.UTF_8);
        entity.setContentType("application/json");

        HttpPost post = new HttpPost(url);
        post.setEntity(entity);

        HttpResponse response = HTTP_CLIENT.execute(post);
        EntityUtils.consume(response.getEntity());
    }

    protected void waitForTomcat() throws IOException, InterruptedException {
        String uri = String.format("http://%s:%d/motech-platform-server/module/server", HOST, PORT);
        HttpGet waitGet = new HttpGet(uri);
        HTTP_CLIENT.execute(waitGet);
    }

    private JSONArray getBundleStatusFromServer(PollingHttpClient httpClient) throws IOException, InterruptedException {

        String uri = String.format("http://%s:%d/motech-platform-server/module/admin/api/bundles", HOST, PORT);
        String response = httpClient.execute(new HttpGet(uri), new BasicResponseHandler());

        assertNotNull(response, "Unable to retrieve bundle status from server");

        return new JSONArray(response);
    }

    private boolean areBundlesStillStarting(JSONArray bundles) {

        for (int i = 0; i < bundles.length(); ++i) {
            JSONObject object = bundles.getJSONObject(i);

            String status = object.getString("state");

            if ("STARTING".equalsIgnoreCase(status)) {
                return true;
            }
        }

        return false;
    }

    private void assertBundlesStatus(JSONArray bundles) {

        for (int i = 0; i < bundles.length(); ++i) {
            JSONObject object = bundles.getJSONObject(i);

            String status = object.getString("state");
            String symbolicName = object.getString("symbolicName");


            if (symbolicName.startsWith("org.motechproject.motech")) {
                assertEquals(symbolicName + " not active after server startup. [" + status + "]", "ACTIVE", status);
            }
        }
    }

}
