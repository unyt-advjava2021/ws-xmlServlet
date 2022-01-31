/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.eltonb.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.eltonb.models.Course;
import com.mysql.jdbc.Driver;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 *
 * @author elton.ballhysa
 */
@WebServlet(name = "CourseServlet", urlPatterns = {"/courses"})
public class CourseServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (! "text/xml".equals(request.getContentType())) {
            response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        response.setContentType("text/xml;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            
            try {
                String departmentCode = readDepartment(request);
                List<Course> courses = retrieveCourses(departmentCode);

                out.println("<courses>");
                for (Course course : courses) {
                    String courseXML = toXML(course);
                    out.println(courseXML);
                }
                out.println("</courses>");
            } catch (Exception e) {
                out.println("<error>" + e.getMessage() + "</error>");
            }
            out.flush();
        } 
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private String toXML(Course course) {
        StringBuilder sb = new StringBuilder();
        sb.append("<course>");
        sb.append("<code>" + course.getCode() + "</code>\n");
        sb.append("<title>" + course.getTitle() + "</title>\n");
        sb.append("<credits>" + course.getCredits() + "</credits>\n");
        sb.append("<departmentCode>" + course.getDepartmentCode() + "</departmentCode>\n");
        sb.append("<description>" + course.getDescription() + "</description>\n");
        sb.append("</course>");
        return sb.toString();
    }

    private List<Course> retrieveCourses(String departmentCode) throws SQLException {
        List<Course> courses = new LinkedList<>();
        final String sql = "select * from courses where department_code = ?";
        final String url = "jdbc:mysql://localhost:3306/assign2";
        final String user = "root";
        final String pass = "eltonelt";
        DriverManager.registerDriver(new Driver());
        try (Connection conn = DriverManager.getConnection(url, user, pass);
                PreparedStatement stat = conn.prepareStatement(sql)) 
        {            
            stat.setString(1, departmentCode);
            ResultSet rs = stat.executeQuery();
            while (rs.next()) {
                Course course = newCourse(rs);
                courses.add(course);
            }
        }
        return courses; 
    }

    private Course newCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setCode(rs.getString("code"));
        course.setTitle(rs.getString("title"));
        course.setCredits(rs.getInt("credits"));
        course.setDepartmentCode(rs.getString("department_code"));
        course.setDescription(rs.getString("description"));
        return course;
    }

    private String readDepartment(HttpServletRequest request) throws IOException {
        String requestBody = request.getReader().lines().collect(Collectors.joining());
        int startIndex = requestBody.indexOf("<departmentCode>");
        int endIndex = requestBody.indexOf("</departmentCode>");
        if (startIndex < 0 || endIndex < 0)
            throw new IllegalArgumentException("Wrong Request, cannot read departmentCode value");
        return requestBody.substring(startIndex + "<departmentCode>".length(), endIndex);
    }

}
