package com.leaseapp.servlet;

import com.leaseapp.dao.UserDAO;
import com.leaseapp.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/Signup")
public class SignupServlet extends HttpServlet {

    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("Signup.jsp").forward(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String fullName = req.getParameter("fullName");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String phone = req.getParameter("phone");

        UserDAO dao = new UserDAO();

        try {
            if (dao.emailExists(email)) {
                req.setAttribute("error", "Email already registered. Please login.");
                req.getRequestDispatcher("Signup.jsp").forward(req, resp);
                return;
            }

            // Hash the password before storing - never store plain text passwords
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

            User user = new User(fullName, email, hashedPassword, phone);
            boolean success = dao.registerUser(user);

            if (success) {
                resp.sendRedirect("Login.jsp?registered=true");
            } else {
                req.setAttribute("error", "Signup failed. Please try again.");
                req.getRequestDispatcher("Signup.jsp").forward(req, resp);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "Database error: " + e.getMessage());
            req.getRequestDispatcher("Signup.jsp").forward(req, resp);
        }
    }
}