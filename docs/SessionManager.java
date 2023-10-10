package session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import connexion.Connect;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 * La classe SessionManager gère les sessions utilisateur.
 */
public class SessionManager {

  /**
   * Nom du cookie de session.
   */
  String IdSessionName = "RRTSESSIONID";
  /**
   * Objet HttpServletRequest.
   */

  HttpServletRequest request;
  /**
   * Objet HttpServletResponse.
   */
  HttpServletResponse response;
  /**
   * Valeur de l'ID de session.
   */
  String sessionIdValue;

  /**
   * Constructeur de SessionManager.
   *
   * @param req Objet HttpServletRequest.
   * @param resp Objet HttpServletResponse.
   * @throws Exception en cas d'erreur lors de l'initialisation de la session.
   */
  public SessionManager(HttpServletRequest req, HttpServletResponse resp)
    throws Exception {
    request = req;
    response = resp;
    start();
  }

  /**
   * Initialise la session.
   *
   * @throws Exception en cas d'erreur lors de l'initialisation de la session.
   */
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

  /**
   * Obtient la valeur d'un attribut de session.
   *
   * @param key Clé de l'attribut.
   * @param classe Classe de l'objet retourné.
   * @return Valeur de l'attribut.
   * @throws Exception en cas d'erreur lors de la récupération de l'attribut.
   */
  public Object getAttribute(String key,Class<?> classe) throws Exception {
    HashMap<String, Object> all = getAll();
    
    LinkedTreeMap response =  (LinkedTreeMap) all.get(key);
    Gson gson = new Gson();
    String jsonString = gson.toJson(response);
    Object result = gson.fromJson(jsonString, classe);
    return result;
  }

  /**
   * Invalide la session en supprimant toutes les données.
   *
   * @throws Exception en cas d'erreur lors de l'invalidation de la session.
   */
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

  /**
   * Supprime un attribut de session.
   *
   * @param key Clé de l'attribut à supprimer.
   * @throws Exception en cas d'erreur lors de la suppression de l'attribut.
   */
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

  /**
   * Définit la valeur d'un attribut de session.
   *
   * @param key Clé de l'attribut.
   * @param value Valeur de l'attribut.
   * @throws Exception en cas d'erreur lors de la définition de l'attribut.
   */
  public void setAttribute(String key, Object value) throws Exception {
    save(key, value);
  }

  /**
   * Enregistre la valeur d'un attribut de session.
   *
   * @param key Clé de l'attribut.
   * @param value Valeur de l'attribut.
   * @throws Exception en cas d'erreur lors de l'enregistrement de l'attribut.
   */
  public void save(String key, Object value) throws Exception {
    String query =
      "UPDATE session_value SET valeur = valeur || ?::jsonb WHERE idsession = ?";
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

  /**
   * Parse les données JSON en un objet HashMap grâce à la librairie Gson.
   *
   * @param jsonData Données JSON à parser.
   * @return Objet HashMap résultant.
   */
  public HashMap<String, Object> parseJsonData(String jsonData) {
    Gson gson = new Gson();
    HashMap<String, Object> parsedData = gson.fromJson(
      jsonData,
      new TypeToken<HashMap<String, Object>>() {}.getType()
    );
    return parsedData;
  }

  /**
   * Encode les données en JSON grâce à la librairie Gson.
   *
   * @param key Clé des données.
   * @param value Valeur des données.
   * @return Chaîne JSON encodée.
   */
  public String jsonEncode(String key, Object value) {
    Gson gson = new Gson();
    JsonObject jsonObject = new JsonObject();
    jsonObject.add(key, gson.toJsonTree(value));
    String jsonData = gson.toJson(jsonObject);
    return jsonData;
  }

  /**
   * Récupère toutes les données de session.
   *
   * @return Objet HashMap contenant toutes les données de session.
   * @throws Exception en cas d'erreur lors de la récupération des données de session.
   */
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

  /**
   * Insère une nouvelle session dans la base de données.
   *
   * @return ID de la nouvelle session.
   * @throws Exception en cas d'erreur lors de l'insertion de la session.
   */
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

  /**
   * Obtient un cookie par nom.
   *
   * @param name Nom du cookie.
   * @return Objet Cookie.
   */
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

  /**
   * Obtient le nom du cookie de session.
   *
   * @return Nom du cookie de session.
   */
  public String getIdSessionName() {
    return IdSessionName;
  }

  /**
   * Définit le nom du cookie de session.
   *
   * @param idSessionName Nouveau nom du cookie de session.
   */
  public void setIdSessionName(String idSessionName) {
    IdSessionName = idSessionName;
  }

  /**
   * Obtient l'objet HttpServletRequest.
   *
   * @return Objet HttpServletRequest.
   */
  public HttpServletRequest getRequest() {
    return request;
  }

  /**
   * Définit l'objet HttpServletRequest.
   *
   * @param request Nouvel objet HttpServletRequest.
   */
  public void setRequest(HttpServletRequest request) {
    this.request = request;
  }

  /**
   * Obtient l'objet HttpServletResponse.
   *
   * @return Objet HttpServletResponse.
   */
  public HttpServletResponse getResponse() {
    return response;
  }

  /**
   * Définit l'objet HttpServletResponse.
   *
   * @param response Nouvel objet HttpServletResponse.
   */
  public void setResponse(HttpServletResponse response) {
    this.response = response;
  }

  /**
   * Obtient la valeur de l'ID de session.
   *
   * @return Valeur de l'ID de session.
   */
  public String getSessionIdValue() {
    return sessionIdValue;
  }

  /**
   * Définit la valeur de l'ID de session.
   *
   * @param sessionIdValue Nouvelle valeur de l'ID de session.
   */
  public void setSessionIdValue(String sessionIdValue) {
    this.sessionIdValue = sessionIdValue;
  }
}
// javadoc -d docs -sourcepath src -subpackages src
