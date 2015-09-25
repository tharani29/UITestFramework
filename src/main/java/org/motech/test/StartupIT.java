package org.motech.test;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static org.junit.Assert.*;

public class StartupIT extends BaseIT {

    public void setUp() throws Exception {
        waitForTomcat();
        createAdminUser();
        login();
    }

    public void shouldStartServerAndMakeAllBundlesActive() throws Exception {
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

            Thread.sleep(ONE_MINUTE);
        } while (--retryCount > 0);

        assertNotNull("The bundle list cannot be empty", bundles);
        assertFalse("Failed to start bundles (TIMEOUT)", starting);
        assertBundlesStatus(bundles);

        createAdminUser();
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

    private JSONArray getBundleStatusFromServer(PollingHttpClient httpClient) throws IOException, JSONException, InterruptedException {

        String uri = String.format("http://%s:%d/motech-platform-server/module/admin/api/bundles", HOST, PORT);
        String response = httpClient.execute(new HttpGet(uri), new BasicResponseHandler());

        assertNotNull(response, "Unable to retrieve bundle status from server");

        return new JSONArray(response);
    }

}
