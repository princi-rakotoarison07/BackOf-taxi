<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="testFramework.com.testframework.model.Departement" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Liste des départements</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; }
        th { background: #f4f4f4; text-align: left; }
        tr:nth-child(even) { background: #fafafa; }
    </style>
</head>
<body>
<h1>Liste des départements</h1>
<%
    Object obj = request.getAttribute("departements");
    List<Departement> departements = null;
    if (obj instanceof List) {
        departements = (List<Departement>) obj;
    }
%>
<% if (departements == null || departements.isEmpty()) { %>
    <p>Aucun département trouvé.</p>
<% } else { %>
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>Nom</th>
            <th>Description</th>
        </tr>
        </thead>
        <tbody>
        <% for (Departement d : departements) { %>
            <tr>
                <td><%= d.getId() %></td>
                <td><%= d.getNom() %></td>
                <td><%= d.getDescription() %></td>
            </tr>
        <% } %>
        </tbody>
    </table>
<% } %>
</body>
</html>
