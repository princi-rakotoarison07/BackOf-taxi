<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Résultat</title>
</head>
<body>
    <h1>Résultat de l'opération</h1>
    <% if (request.getAttribute("message") != null) { %>
        <div style="color: green;"><%= request.getAttribute("message") %></div>
    <% } %>
    <% if (request.getAttribute("error") != null) { %>
        <div style="color: red;"><%= request.getAttribute("error") %></div>
    <% } %>
    <br>
    <a href="${pageContext.request.contextPath}/reservation/form">Retour au formulaire</a>
</body>
</html>
