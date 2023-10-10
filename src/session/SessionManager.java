package session;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import connexion.Connect;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class SessionManager {

  String IdSessionName = "RRTSESSIONID";
  HttpServletRequest request;
  HttpServletResponse response;
  String sessionIdValue;

  public SessionManager(HttpServletRequest req, HttpServletResponse resp)
    throws Exception {
    request = req;
    response = resp;
    start();
  }

  public void start() throws Exception {
    // if (getCookieValue(getIdSessionName()) != null) {
    //     // verifySession();
    // }
    Cookie sessionIdCookie = getCookieByName(getIdSessionName());

    if (sessionIdCookie == null) {
      try {
        String sessionIdValue = insertSession();
        sessionIdCookie = new Cookie(getIdSessionName(), sessionIdValue);
        this.response.addCookie(sessionIdCookie);
      } catch (Exception e) {
        throw e;
      }
    } else {
      sessionIdValue = sessionIdCookie.getValue();
    }
  }

  public Object getAttribute(String key,Class<?> classe) throws Exception {
    HashMap<String, Object> all = getAll();
    
    LinkedTreeMap response =  (LinkedTreeMap) all.get(key);
    Gson gson = new Gson();
    String jsonString = gson.toJson(response);
    Object result = gson.fromJson(jsonString, classe);
    return result;
  }

  public void invalidate() throws Exception {
    String query =
      "UPDATE session_value SET valeur = '{}'::jsonb WHERE idsession = ?";
    Connect myConnectManager = new Connect();
    try {
      Connection connection = myConnectManager.getConnectionPostgresql();
      PreparedStatement preparedStatement = connection.prepareStatement(query);

      try {
        preparedStatement.setString(1, sessionIdValue);
        preparedStatement.executeUpdate();
        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        throw e;
      } finally {
        preparedStatement.close();
        connection.close();
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public void removeAttribute(String key) throws Exception {
    String query =
      "UPDATE session_value SET valeur = valeur::jsonb - ? WHERE idsession = ?";
    Connect myConnectManager = new Connect();
    try {
      Connection connection = myConnectManager.getConnectionPostgresql();
      PreparedStatement preparedStatement = connection.prepareStatement(query);

      try {
        preparedStatement.setString(1, key);
        preparedStatement.setString(2, sessionIdValue);
        preparedStatement.executeUpdate();
        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        throw e;
      } finally {
        preparedStatement.close();
        connection.close();
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public void setAttribute(String key, Object value) throws Exception {
    save(key, value);
  }

  public void save(String key, Object value) throws Exception {
    String query =
      "UPDATE session_value SET valeur = valeur::jsonb || ?::jsonb WHERE idsession = ?";
    String jsonData = jsonEncode(key, value);
    Connect myConnectManager = new Connect();
    try {
      Connection connection = myConnectManager.getConnectionPostgresql();
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      preparedStatement.setString(1, jsonData);
      preparedStatement.setString(2, sessionIdValue);
      try {
        preparedStatement.executeUpdate();
        connection.commit();
      } catch (Exception e) {
        connection.rollback();
        throw e;
      } finally {
        preparedStatement.close();
        connection.close();
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public HashMap<String, Object> parseJsonData(String jsonData) {
    Gson gson = new Gson();
    HashMap<String, Object> parsedData = gson.fromJson(
      jsonData,
      new TypeToken<HashMap<String, Object>>() {}.getType()
    );
    return parsedData;
  }

  public String jsonEncode(String key, Object value) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(key, gson.toJsonTree(value));
    String jsonData = gson.toJson(jsonObject);
    return jsonData;
  }

  public HashMap<String, Object> getAll() throws Exception {
    String query = "select valeur from session_value where idsession = ? ";
    Connect myConnectManager = new Connect();
    try {
      Connection connection = myConnectManager.getConnectionPostgresql();
      PreparedStatement preparedStatement = connection.prepareStatement(query);
      try {
        preparedStatement.setString(1, sessionIdValue);
        ResultSet res = preparedStatement.executeQuery();
        if (res.next()) {
          String jsonData = res.getString("valeur");
          if (jsonData != null) {
            HashMap<String, Object> sessionData = parseJsonData(jsonData);
            return sessionData;
          }
        }
        return null;
      } catch (Exception e) {
        throw e;
      } finally {
        preparedStatement.close();
        connection.close();
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public String insertSession() throws Exception {
    String query =
      "insert into session_value (idsession , valeur ) values ( ( SELECT left(md5(random()::text), 14) || nextval( 'idsession' )) , '{}' ) returning idsession ";
    Connect myConnectManager = new Connect();
    try {
      Connection connection = myConnectManager.getConnectionPostgresql();
      PreparedStatement preparedStatement = connection.prepareStatement(query);

      try {
        ResultSet res = preparedStatement.executeQuery();
        connection.commit();
        if (res.next() == false) {
          throw new Exception("Session init failed");
        }
        System.out.println(res.toString());
        return res.getString("idsession");
      } catch (Exception e) {
        connection.rollback();
        throw e;
      } finally {
        preparedStatement.close();
        connection.close();
      }
    } catch (Exception e) {
      throw e;
    }
  }

  public String getCookieValue(String key) {
    Cookie cookie = getCookieByName("key");
    return cookie.getValue();
  }

  private Cookie getCookieByName(String name) {
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(name)) {
          return cookie;
        }
      }
    }

    return null;
  }

  public String getIdSessionName() {
    return IdSessionName;
  }

  public void setIdSessionName(String idSessionName) {
    IdSessionName = idSessionName;
  }

  public HttpServletRequest getRequest() {
    return request;
  }

  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  public HttpServletResponse getResponse() {
    return response;
  }

  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  public String getSessionIdValue() {
    return sessionIdValue;
  }

  public void setSessionIdValue(String sessionIdValue) {
    this.sessionIdValue = sessionIdValue;
  }
}