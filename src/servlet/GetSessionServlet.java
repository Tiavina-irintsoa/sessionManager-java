package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import models.Personne;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import session.SessionManager;

public class GetSessionServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    PrintWriter out = resp.getWriter();
    try {
      SessionManager manager = new SessionManager(req, resp);
      manager.setAttribute("something", "somevalue");
      HashMap<String, Object> sessionData = manager.getAll();
      out.println("Afficher tous les objets dans la session");
      for (Map.Entry<String, Object> entry : sessionData.entrySet()) {
        out.println(entry.getKey() + ": " + entry.getValue());
      }
      out.println("Contenu de l'attribut Personne:");
      Personne p =(Personne) manager.getAttribute("personne",Personne.class);
      out.print(p);
    } catch (Exception e) {
      out.print(e.toString());
      e.printStackTrace();
    }
  }
}
