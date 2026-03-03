<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="testFramework.com.testframework.model.Employe" %>
<%@ page import="testFramework.com.testframework.model.Departement" %>
<%@ page import="testFramework.com.testframework.model.Ville" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Employé - Détails</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; max-width: 500px; }
        .item { margin: 6px 0; }
        .label { font-weight: bold; }
    </style>
</head>
<body>
<%
    Employe emp = (Employe) request.getAttribute("emp");
%>
<h1>Employé - Détails</h1>
<% if (emp == null) { %>
    <p>Aucun employé fourni.</p>
<% } else { %>
    <div class="card">
        <div class="item"><span class="label">Nom:</span> <%= emp.getNom() %></div>
        <div class="item"><span class="label">Prénom:</span> <%= emp.getPrenom() %></div>
        <div class="item"><span class="label">Âge:</span> <%= emp.getAge() %></div>
        <div class="item"><span class="label">Poste:</span> <%= emp.getPoste() %></div>
        <hr/>
        <div class="item"><span class="label">Département:</span></div>
        <% if (emp.getDept() == null) { %>
            <div class="item">(Aucun département)</div>
        <% } else { %>
            <div class="item"><span class="label">ID:</span> <%= emp.getDept().getId() %></div>
            <div class="item"><span class="label">Nom:</span> <%= emp.getDept().getNom() %></div>
            <div class="item"><span class="label">Description:</span> <%= emp.getDept().getDescription() %></div>
            <div class="item"><span class="label">Ville:</span></div>
            <% if (emp.getDept().getVille() == null) { %>
                <div class="item">(Aucune ville)</div>
            <% } else { %>
                <div class="item"><span class="label">ID Ville:</span> <%= emp.getDept().getVille().getId() %></div>
                <div class="item"><span class="label">Nom Ville:</span> <%= emp.getDept().getVille().getNom() %></div>
                <div class="item"><span class="label">Code Postal:</span> <%= emp.getDept().getVille().getCodePostal() %></div>
            <% } %>
        <% } %>
    </div>
<% } %>
</body>
</html>
