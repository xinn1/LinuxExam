import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@WebServlet(urlPatterns = "/GetNotepadList")
public class GetNotepadList extends HttpServlet {
   final static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    final static String URL = "jdbc:mysql://124.70.131.236/linux_final";
    final static String USER = "root";
    final static String PASS = "Wx1234/@";
   final static String SQL_QURERY_ALL_NOTEPAD = "SELECT * FROM t_Notepad;";
   Connection conn = null;

   public void init() {
      try {
         Class.forName(JDBC_DRIVER);
         conn = DriverManager.getConnection(URL, USER, PASS);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void destroy() {
      try {
         conn.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("application/json");
      response.setCharacterEncoding("UTF-8");
      PrintWriter out = response.getWriter();

      List<Notepad> notepadList = getAllNotepad();
      Gson gson = new Gson();
      String json = gson.toJson(notepadList, new TypeToken<List<Notepad>>() {
      }.getType());
      out.println(json);
      out.flush();
      out.close();
   }

   private List<Notepad> getAllNotepad() {
      List<Notepad> notepadList = new ArrayList<Notepad>();
      Statement stmt = null;
      try {
         stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(SQL_QURERY_ALL_NOTEPAD);
         while (rs.next()) {
            Notepad note = new Notepad();
            note.id = rs.getInt("id");
            note.notepadContent = rs.getString("notepadContent");
            note.notepadTime = rs.getString("notepadTime");
            notepadList.add(note);
         }
         rs.close();
         stmt.close();
      } catch (SQLException se) {
         se.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         try {
            if (stmt != null)
               stmt.close();
         } catch (SQLException se) {
            se.printStackTrace();
         }
      }

      return notepadList;
   }
   public class Notepad {
      int id;
      String notepadContent;
      String notepadTime;
   }
}

