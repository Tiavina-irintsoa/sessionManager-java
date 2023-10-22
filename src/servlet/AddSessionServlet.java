package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import models.Personne;
import session.SessionManager;

public class AddSessionServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    PrintWriter out = resp.getWriter();
    try {
      out.print("tiita");

      SessionManager manager = new SessionManager(req, resp,this);
      manager.setAttribute("profile", "admin");
      System.out.println("adding personne");
      Personne personne = new Personne(
        "Jean Boite",
        Date.valueOf("2023-08-08")
      );
      manager.setAttribute("personne", personne);
      out.print("add to the session succeed");
    } catch (Exception e) {
      out.print(e.toString());
      e.printStackTrace();
    }
  }
}
