<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    if (session != null && session.getAttribute("username") != null) 
    {
        if("Admin".equals(session.getAttribute("designation")))
        {
            response.sendRedirect("/Connect/AdminPage.jsp");
        }
        else
        {
            response.sendRedirect("/Connect/Home.jsp");
        }
    }
     else 
    {
            response.sendRedirect("/Connect/LogIn.html");
    }
%>
