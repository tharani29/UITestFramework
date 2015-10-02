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
import org.json.JSONException;
import org.json.JSONObject;
import org.motech.util.PollingHttpClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class StartupHelper {
    protected static final String HOST = "localhost";
    protected static final int PORT = 8080;
    protected static final String MOTECH = "motech";
    protected static final PollingHttpClient httpClient;
    protected static final Long TWO_MINUTES = 120 * 1000L;

    static {
        httpClient = new PollingHttpClient(new DefaultHttpClient(), 120);
        httpClient.setCookieStore(new BasicCookieStore());
    }

    public void startUp() throws IOException, InterruptedException, JSONException {
        waitForTomcat();
        createAdminUser();
        login();
        waitForBundles();
    }

    public void waitForBundles() throws JSONException, InterruptedException, IOException {

        JSONArray bundles = null;

        int retryCount = 10;
        boolean starting = true;

        do {
            try {
                bundles = getBundleStatusFromServer(httpClient);
                starting = areBundlesStillStarting(bundles);

                if (!starting) {
                    break;
                }

            } catch (org.apache.http.client.HttpResponseException e) {
                if (!e.getMessage().contains("Not Found")) {
                    throw e;
                }
            }

            Thread.sleep(TWO_MINUTES);
        } while (--retryCount > 0);

        assertNotNull("The bundle list cannot be empty", bundles);
        assertFalse("Failed to start bundles (TIMEOUT)", starting);
        assertBundlesStatus(bundles);
    }

    protected void login() throws IOException, InterruptedException {
        String uri = String.format("http://%s:%d/motech-platform-server/module/server/motech-platform-server/j_spring_security_check", HOST, PORT);

        final HttpPost loginPost = new HttpPost(uri);

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", MOTECH));
        nvps.add(new BasicNameValuePair("j_password", MOTECH));

        loginPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF8"));

        HttpResponse response = httpClient.execute(loginPost);
        EntityUtils.consume(response.getEntity());
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

    protected void waitForTomcat() throws IOException, InterruptedException {

        String uri = String.format("http://%s:%d/motech-platform-server/module/server", HOST, PORT);
        HttpGet waitGet = new HttpGet(uri);
        HttpResponse response = httpClient.execute(waitGet);

    }

    private JSONArray getBundleStatusFromServer(PollingHttpClient httpClient) throws IOException, JSONException, InterruptedException {

        String uri = String.format("http://%s:%d/motech-platform-server/module/admin/api/bundles", HOST, PORT);
        String response = httpClient.execute(new HttpGet(uri), new BasicResponseHandler());

        assertNotNull(response, "Unable to retrieve bundle status from server");

        return new JSONArray(response);
    }

    private boolean areBundlesStillStarting(JSONArray bundles) throws JSONException {

        for (int i = 0; i < bundles.length(); ++i) {
            JSONObject object = bundles.getJSONObject(i);

            String status = object.getString("state");
            String symbolicName = object.getString("symbolicName");

            if ("STARTING".equalsIgnoreCase(status)) {
                return true;
            }
        }

        return false;
    }

    private void assertBundlesStatus(JSONArray bundles) throws JSONException {

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
