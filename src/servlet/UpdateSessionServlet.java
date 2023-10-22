package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import session.SessionManager;

public class UpdateSessionServlet extends HttpServlet {

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
    PrintWriter out = resp.getWriter();
    try {
      out.print("tiita");

      SessionManager manager = new SessionManager(req, resp,this);
      manager.setAttribute("profile", "user");
      out.print("updated");
    } catch (Exception e) {
      out.print(e.toString());
      e.printStackTrace();
    }
  }
}
