package regression.test;

import static org.fest.assertions.Assertions.assertThat;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Test;

import regression.client.PersoniumRequestClient;
import regression.client.PersoniumResponse;

public class RegressionTest {

    private static final String CELL_NAME = "RegressionTestCell";
    private static final String BOX_NAME = "box";
    private static final String ACCOUNT_NAME = "RegressionTestUser";
    private static final String PASSWORD = "password";

    private static final String ODATA_COL_NAME = "odata";
    private static final String ENTITY_TYPE_NAME = "entity";

    private static final String SERVICE_COL_NAME = "service";
    private static final String SERVICE_SRC_NAME = "test.js";
    private static final String SERVICE_NAME = "test";

    private static final Object WEBDAV_COL_NAME = "dav";

    @After
    public void after() {
        PersoniumRequestClient.delete()
                .path("/" + CELL_NAME)
                .header("X-Dc-Recursive", "true")
                .exec();
    }

    @Test
    public void Cellが作成できること() {
        createCell();
    }

    @Test
    public void Accountロック後ロックタイムアウトが経過してから正常にトークン取得ができること() throws InterruptedException {
        createBox();

        String body = String.format("grant_type=password&username=%s&password=%s", ACCOUNT_NAME, PASSWORD + "invalid");
        PersoniumResponse response = PersoniumRequestClient.post()
                .path(String.format("/%s/__auth", CELL_NAME))
                .token(null)
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_BAD_REQUEST);

        Thread.sleep(1000);

        body = String.format("grant_type=password&username=%s&password=%s", ACCOUNT_NAME, PASSWORD);
        response = PersoniumRequestClient.post()
                .path(String.format("/%s/__auth", CELL_NAME))
                .token(null)
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.toJson().get("access_token")).isNotNull();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void イベント受付ができること() {
        createCell();

        JSONObject body = new JSONObject();
        body.put("level", "INFO");
        body.put("action", "test");
        body.put("object", "test object");
        body.put("result", "test result");

        PersoniumResponse response = PersoniumRequestClient.post()
                .path(String.format("/%s/__event", CELL_NAME))
                .body(body.toJSONString())
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);

        response = PersoniumRequestClient.get()
                .path(String.format("/%s/__log/current/default.log", CELL_NAME))
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.toString()).contains("test result");
    }

    @Test
    public void UserODataが作成できること() {
        createODataCollection();

        String userDataId = "id001";
        String body = String.format("{\"__id\":\"%s\"}", userDataId);
        String token = getLocalAccessToken();

        PersoniumResponse response = PersoniumRequestClient.post()
                .token(token)
                .path(String.format("/%s/%s/%s/%s", CELL_NAME, BOX_NAME, ODATA_COL_NAME, ENTITY_TYPE_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        response = PersoniumRequestClient.get()
                .token(token)
                .path(String.format("/%s/%s/%s/%s('%s')"
                        , CELL_NAME, BOX_NAME, ODATA_COL_NAME, ENTITY_TYPE_NAME, userDataId))
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);
    }

    @Test
    public void WebDAVファイル登録ができること() {
        createWebDAVCollection();

        String token = getLocalAccessToken();

        String body = "Hello world !!";
        Object fileName = "hello.html";
        PersoniumResponse response = PersoniumRequestClient.put()
                .token(token)
                .path(String.format("/%s/%s/%s/%s", CELL_NAME, BOX_NAME, WEBDAV_COL_NAME, fileName))
                .header(HttpHeaders.CONTENT_TYPE, ContentType.TEXT_HTML.getMimeType())
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        response = PersoniumRequestClient.get()
                .token(token)
                .path(String.format("/%s/%s/%s/%s", CELL_NAME, BOX_NAME, WEBDAV_COL_NAME, fileName))
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.toString()).isEqualTo(body);
    }

    @Test
    public void サービス実行ができること() {
        createServiceCollection();

        String token = getLocalAccessToken();

        PersoniumResponse response = PersoniumRequestClient.get()
                .token(token)
                .path(String.format("/%s/%s/%s/%s", CELL_NAME, BOX_NAME, SERVICE_COL_NAME, SERVICE_NAME))
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);
        assertThat(response.toString()).isEqualTo("hello");
    }

    private String getLocalAccessToken() {
        String body = String.format("grant_type=password&username=%s&password=%s", ACCOUNT_NAME, PASSWORD);
        PersoniumResponse response = PersoniumRequestClient.post()
                .path(String.format("/%s/__auth", CELL_NAME))
                .token(null)
                .body(body)
                .exec();
        return (String) response.toJson().get("access_token");
    }

    private void createCell() {
        // Cell
        String body = String.format("{\"Name\":\"%s\"}", CELL_NAME);
        PersoniumResponse response = PersoniumRequestClient.post()
                .path("/__ctl/Cell")
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);
    }

    private void createBox() {
        String body;
        PersoniumResponse response;
        createCell();

        // Box
        body = String.format("{\"Name\":\"%s\"}", BOX_NAME);
        response = PersoniumRequestClient.post()
                .path(String.format("/%s/__ctl/Box", CELL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        // Account
        body = String.format("{\"Name\":\"%s\"}", ACCOUNT_NAME);
        response = PersoniumRequestClient.post()
                .path(String.format("/%s/__ctl/Account", CELL_NAME))
                .header("X-Dc-Credential", PASSWORD)
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        // Role
        String roleName = "role";
        body = String.format("{\"Name\":\"%s\"}", roleName);
        response = PersoniumRequestClient.post()
                .path(String.format("/%s/__ctl/Role", CELL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        // Account - Role $links
        body = String.format("{\"uri\":\"%s/%s/__ctl/Role('%s')\"}",
                PersoniumRequestClient.getBaseUrl(), CELL_NAME, roleName);
        response = PersoniumRequestClient.post()
                .path(String.format("/%s/__ctl/Account('%s')/$links/_Role", CELL_NAME, ACCOUNT_NAME, roleName))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_NO_CONTENT);

        // Set ACL
        String roleBaseUrl = String.format("%s/%s/__role/__/", PersoniumRequestClient.getBaseUrl(), CELL_NAME);
        body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<D:acl xmlns:D=\"DAV:\" xmlns:dc=\"urn:x-dc1:xmlns\" xml:base=\"" + roleBaseUrl + "\">"
                + "  <D:ace>"
                + "    <D:principal>"
                + "      <D:href>" + roleName + "</D:href>"
                + "    </D:principal>"
                + "    <D:grant>"
                + "      <D:privilege><D:read/></D:privilege>"
                + "      <D:privilege><D:write/></D:privilege>"
                + "      <D:privilege><dc:exec/></D:privilege>"
                + "    </D:grant>"
                + "  </D:ace>"
                + "</D:acl>";
        response = PersoniumRequestClient.acl()
                .path(String.format("/%s/%s", CELL_NAME, BOX_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_OK);
    }

    private void createODataCollection() {
        String body;
        PersoniumResponse response;
        createBox();

        // OData Collection
        String colType = "odata";
        body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<D:mkcol xmlns:D=\"DAV:\" xmlns:dc=\"urn:x-dc1:xmlns\">"
                + "<D:set>"
                + "  <D:prop>"
                + "    <D:resourcetype>"
                + "      <D:collection />"
                + "      <dc:" + colType + "/>"
                + "    </D:resourcetype>"
                + "  </D:prop>"
                + "</D:set>"
                + "</D:mkcol>";
        response = PersoniumRequestClient.mkcol()
                .path(String.format("/%s/%s/%s", CELL_NAME, BOX_NAME, ODATA_COL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        // EntityType
        body = String.format("{\"Name\":\"%s\"}", ENTITY_TYPE_NAME);
        response = PersoniumRequestClient.post()
                .path(String.format("/%s/%s/%s/$metadata/EntityType", CELL_NAME, BOX_NAME, ODATA_COL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

    }

    private void createWebDAVCollection() {
        String body;
        PersoniumResponse response;
        createBox();

        // WebDAV Collection
        body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<D:mkcol xmlns:D=\"DAV:\" xmlns:dc=\"urn:x-dc1:xmlns\">"
                + "<D:set>"
                + "  <D:prop>"
                + "    <D:resourcetype>"
                + "      <D:collection />"
                + "    </D:resourcetype>"
                + "  </D:prop>"
                + "</D:set>"
                + "</D:mkcol>";
        response = PersoniumRequestClient.mkcol()
                .path(String.format("/%s/%s/%s", CELL_NAME, BOX_NAME, WEBDAV_COL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);
    }

    private void createServiceCollection() {
        String body;
        PersoniumResponse response;
        createBox();

        // Service Collection
        String colType = "service";
        body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
                + "<D:mkcol xmlns:D=\"DAV:\" xmlns:dc=\"urn:x-dc1:xmlns\">"
                + "<D:set>"
                + "  <D:prop>"
                + "    <D:resourcetype>"
                + "      <D:collection />"
                + "      <dc:" + colType + "/>"
                + "    </D:resourcetype>"
                + "  </D:prop>"
                + "</D:set>"
                + "</D:mkcol>";
        response = PersoniumRequestClient.mkcol()
                .path(String.format("/%s/%s/%s", CELL_NAME, BOX_NAME, SERVICE_COL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

        // Service Proppatch
        body = "<D:propertyupdate xmlns:D=\"DAV:\" xmlns:dc=\"urn:x-dc1:xmlns\""
                + "    xmlns:Z=\"http://www.w3.com/standards/z39.50/\">"
                + "  <D:set>"
                + "    <D:prop>"
                + "      <dc:service language=\"JavaScript\" subject=\"$" + ACCOUNT_NAME + "\">"
                + "        <dc:path name=\"" + SERVICE_NAME + "\" src=\"" + SERVICE_SRC_NAME + "\"/>"
                + "      </dc:service>"
                + "    </D:prop>"
                + "  </D:set>"
                + "</D:propertyupdate>";
        response = PersoniumRequestClient.proppatch()
                .path(String.format("/%s/%s/%s", CELL_NAME, BOX_NAME, SERVICE_COL_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_MULTI_STATUS);

        // Service source
        body = "function() { return {status:200, headers:{'Content-Type' : 'text/html'}, body: ['hello']};}";
        response = PersoniumRequestClient.put()
                .path(String.format("/%s/%s/%s/__src/%s", CELL_NAME, BOX_NAME, SERVICE_COL_NAME, SERVICE_SRC_NAME))
                .body(body)
                .exec();
        assertThat(response.code()).isEqualTo(HttpStatus.SC_CREATED);

    }
}
