package session;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import connexion.Connect;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class SessionManager {

  String IdSessionName = "RRTSESSIONID";
  HttpServletRequest request;
  HttpServletResponse response;
  String sessionIdValue;
  ServletContext servletContext;

  public SessionManager(HttpServletRequest req, HttpServletResponse resp, HttpServlet servlet)
    throws Exception {
    servletContext = servlet.getServletContext();
    request = req;
    response = resp;
    start();
  }
  
  public double getSessionStartTime() throws Exception{
    String query =
     "select extract(epoch from ((now() - date_insertion)::interval ))/60 as timeout from session_value where idsession = ?";
   Connect myConnectManager = new Connect();
   try {
     Connection connection = myConnectManager.getConnectionPostgresql();
     PreparedStatement preparedStatement = connection.prepareStatement(query);

     try {
       preparedStatement.setString(1, sessionIdValue);
       ResultSet res = preparedStatement.executeQuery();
       res.next();
       return res.getDouble("timeout");
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
  public SessionConfiguration getParameterTimeOut() throws Exception{
    InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/session_config.json");
    System.out.println("reading");
    if (inputStream != null) {
      try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
          StringBuilder jsonContent = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
              jsonContent.append(line);
          }
          Gson gson = new Gson();
          SessionConfiguration config = gson.fromJson(jsonContent.toString(), SessionConfiguration.class);
          System.out.println(config.getSessionExpiration());
          return config;
      } catch (IOException e) {
          throw e;
      }
    }
    return null;
  }
  public void start() throws Exception {
    
    Cookie sessionIdCookie = getCookieByName(getIdSessionName());

    if (sessionIdCookie == null) {
      try {
        generateCookie(sessionIdCookie);
      } catch (Exception e) {
        throw e;
      }
    } else {
      sessionIdValue = sessionIdCookie.getValue();
      if (getParameterTimeOut().getSessionExpiration()<getSessionStartTime()) {
        generateCookie(sessionIdCookie);
    }
    }
  }

  public void generateCookie(Cookie sessionIdCookie) throws Exception{
    this.sessionIdValue = insertSession();
    sessionIdCookie = new Cookie(getIdSessionName(), sessionIdValue);
    this.response.addCookie(sessionIdCookie);
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
        // connection.commit();
      } catch (Exception e) {
        // connection.rollback();
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
        // connection.commit();
      } catch (Exception e) {
        // connection.rollback();
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
        // connection.commit();
      } catch (Exception e) {
        // connection.rollback();
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
        System.out.println("select valeur from session_value where idsession = "+sessionIdValue+" ");
        HashMap<String, Object> sessionData = new HashMap<String, Object>();
        if (res.next()) {
          String jsonData = res.getString("valeur");
          if (jsonData != null) {
            sessionData = parseJsonData(jsonData);
          }
        }
        return sessionData;
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
        // connection.commit();
        if (res.next() == false) {
          throw new Exception("Session init failed");
        }
        System.out.println(res.toString());
        return res.getString("idsession");
      } catch (Exception e) {
        // connection.rollback();
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