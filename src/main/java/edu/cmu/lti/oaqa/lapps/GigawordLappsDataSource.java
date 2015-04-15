package edu.cmu.lti.oaqa.lapps;

import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.lappsgrid.api.DataSource;
import org.lappsgrid.serialization.Error;
import org.lappsgrid.serialization.Serializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.lappsgrid.discriminator.Discriminators.Uri;

/**
 * @author Di Wang.
 */
public class GigawordLappsDataSource implements DataSource {

    private final HttpSolrClient solrClient;
    private final String solrUrl;
    protected String metadata = "TODO";

    public GigawordLappsDataSource() {
        solrUrl = System.getProperty("solrUrl", GigawordIndexer.DEFAULT_SOLR_URL);
        solrClient = new HttpSolrClient(solrUrl);
    }

    @Override
    public String execute(String input) {
        System.out.println("GigawordLappsDataSource");


        Map<String, Object> map = Serializer.parse(input, HashMap.class);
        String discriminator = (String) map.get("discriminator");
        if (discriminator == null) {
            return new Error("No discriminator value provided.").asJson();
        }

        String result = null;
        switch (discriminator) {
            case Uri.SIZE:
                break;
            case Uri.LIST:
                break;
            case Uri.GET:
                String key = map.get("payload").toString();
                if (key == null) {
                    result = error("No key value provided");
                } else {
                    try {
                        String queryUrl = appendParam(solrUrl + "select", key);
                        result = getHTML(queryUrl);
                    } catch (Exception e) {
                        result = error(e.getMessage());
                    }
                }
                break;
            case Uri.GETMETADATA:
                result = metadata;
                break;
            default:
                String message = String.format("Invalid discriminator: %s, Uri.List is %s", discriminator, Uri.LIST);
                result = error(message);
                break;
        }
        return result;
    }

    @Override
    public String getMetadata() {
        return metadata;
    }

    protected String error(String message) {
        return new Error(message).asJson();
    }

    public static String appendParam(String url, String param) {
        String[] pa = param.split("&");
        for (String p : pa) {
            if (p.trim().length() == 0) continue;
            String[] kv = p.split("=");
            if (kv.length == 2) {
                url = url + (url.indexOf('?') > 0 ? "&" : "?") + kv[0] + "=" + kv[1];
            } else {
                System.err.println("Skipping param " + p + " which is not on form key=value");
            }
        }
        return url;
    }

    public String getHTML(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        return sb.toString();
    }
}