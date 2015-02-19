package regression.client;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersoniumResponse {

    private int code;
    private String body;

    public PersoniumResponse(CloseableHttpResponse response) {
        try {
            this.code = response.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                this.body = EntityUtils.toString(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public int code() {
        return this.code;
    }

    public int getCode() {
        return code();
    }

    @Override
    public String toString() {
        return this.body;
    }

    public JSONObject toJson() {
        try {
            return (JSONObject) new JSONParser().parse(this.body);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
