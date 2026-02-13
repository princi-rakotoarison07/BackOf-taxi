<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Map" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Map des paramètres</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 600px; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        th { background: #f4f4f4; text-align: left; }
        tr:nth-child(even) { background: #fafafa; }
        .null { color: #a00; font-style: italic; }
    </style>
</head>
<body>
<h1>Paramètres reçus</h1>
<%
    Object obj = request.getAttribute("params");
    Map<String, Object> params = null;
    if (obj instanceof Map) {
        params = (Map<String, Object>) obj;
    }
%>
<% if (params == null || params.isEmpty()) { %>
    <p>Aucun paramètre trouvé.</p>
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
        <% for (Map.Entry<String, Object> e : params.entrySet()) { %>
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
</body>
</html>
