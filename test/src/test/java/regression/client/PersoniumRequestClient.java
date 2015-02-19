package regression.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class PersoniumRequestClient {
    private static final String DEFAULT_BASE_URL = "http://192.168.59.103:8080/dc1-core";
    private static final String DEFAULT_TOKEN = "personium.io";

    private static final String MIME_TYPE_JSON = ContentType.APPLICATION_JSON.getMimeType();
    private static final String BEARER = "Bearer ";

    private static String baseUrl = getProperty(DEFAULT_BASE_URL, "personium.base.url");
    private String token = getProperty(DEFAULT_TOKEN, "personium.token");;
    private String body;
    private String path;
    private HttpRequestBase requestBase;
    private Map<String, String> headers = new HashMap<String, String>();

    private PersoniumRequestClient() {
    }

    private PersoniumRequestClient(HttpRequestBase requestBase) {
        this();
        this.requestBase = requestBase;
    }

    /**
     * Create instance for post method.
     * @return Instance
     */
    public static PersoniumRequestClient post() {
        return new PersoniumRequestClient(new HttpPost());
    }

    /**
     * Create instance for get method.
     * @return Instance
     */
    public static PersoniumRequestClient get() {
        return new PersoniumRequestClient(new HttpGet());
    }

    /**
     * Create instance for put method.
     * @return Instance
     */
    public static PersoniumRequestClient put() {
        return new PersoniumRequestClient(new HttpPut());
    }

    /**
     * Create instance for delete method.
     * @return Instance
     */
    public static PersoniumRequestClient delete() {
        return new PersoniumRequestClient(new HttpDelete());
    }

    /**
     * Create instance for MKCOL method.
     * @return Instance
     */
    public static PersoniumRequestClient mkcol() {
        return new PersoniumRequestClient(new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "MKCOL";
            }
        });
    }

    /**
     * Create instance for ACL method.
     * @return Instance
     */
    public static PersoniumRequestClient acl() {
        return new PersoniumRequestClient(new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "ACL";
            }
        });
    }

    /**
     * Create instance for PROPPATCH method.
     * @return Instance
     */
    public static PersoniumRequestClient proppatch() {
        return new PersoniumRequestClient(new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "PROPPATCH";
            }
        });
    }

    public PersoniumRequestClient header(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public PersoniumRequestClient body(String body) {
        this.body = body;
        return this;
    }

    public PersoniumRequestClient path(String path) {
        this.path = path;
        return this;
    }

    public PersoniumRequestClient token(String token) {
        this.token = token;
        return this;
    }

    public PersoniumResponse exec() {
        String requestUrl = PersoniumRequestClient.baseUrl + this.path;
        try {
            this.requestBase.setURI(new URI(requestUrl));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        if (token != null) {
            this.requestBase.setHeader(HttpHeaders.AUTHORIZATION, BEARER + this.token);
        }
        this.requestBase.setHeader(HttpHeaders.ACCEPT, MIME_TYPE_JSON);
        for (Entry<String, String> entry : this.headers.entrySet()) {
            this.requestBase.setHeader(entry.getKey(), entry.getValue());
        }

        if (this.body != null && this.requestBase instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest) this.requestBase).setEntity(new StringEntity(this.body, "UTF-8"));
        }

        CloseableHttpClient httpclient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(this.requestBase);
        } catch (ClientProtocolException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new PersoniumResponse(response);
    }

    public static String getBaseUrl() {
        return baseUrl;
    }

    private static String getProperty(String defaultValue, String systemPropertyKey) {
        String val = System.getProperty(systemPropertyKey);
        if (val != null) {
            return val;
        }
        return defaultValue;
    }
}
