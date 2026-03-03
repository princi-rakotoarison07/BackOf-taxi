<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Session - View</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 700px; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        th { background: #f4f4f4; text-align: left; }
        tr:nth-child(even) { background: #fafafa; }
        .null { color: #a00; font-style: italic; }
        a.btn { display: inline-block; margin-top: 12px; text-decoration: none; padding: 8px 12px; background: #1976d2; color: #fff; border-radius: 4px; }
        .msg { margin: 10px 0; color: #333; }
    </style>
</head>
<body>
<h1>Contenu de la Session</h1>
<% String message = (String) request.getAttribute("message"); %>
<% if (message != null) { %>
    <div class="msg"><%= message %></div>
<% } %>
<%
    Object obj = request.getAttribute("sessionMap");
    Map<String, Object> sessionMap = null;
    if (obj instanceof Map) {
        sessionMap = (Map<String, Object>) obj;
    }
%>
<% if (sessionMap == null || sessionMap.isEmpty()) { %>
    <p>Aucune donnée dans la session.</p>
<% } else { %>
    <table>
        <thead>
        <tr>
            <th>Clé</th>
            <th>Valeur</th>
            <th>Type</th>
        </tr>
        </thead>
        <tbody>
        <% for (Map.Entry<String, Object> e : sessionMap.entrySet()) { %>
            <tr>
                <td><%= e.getKey() %></td>
                <td>
                    <% if (e.getValue() == null) { %>
                        <span class="null">null</span>
                    <% } else { %>
                        <%= String.valueOf(e.getValue()) %>
                    <% } %>
                </td>
                <td><%= (e.getValue() == null ? "null" : e.getValue().getClass().getSimpleName()) %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
<% } %>

<a class="btn" href="sessionForm">Retour au formulaire</a>
</body>
</html>
