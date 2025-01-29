<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session != null && session.getAttribute("username") != null) {
        // Redirect to home page
        response.sendRedirect("/Connect/Home.jsp");
    } else {
        // Redirect to login page
        response.sendRedirect("/Connect/LogIn.html");
    }
%>
