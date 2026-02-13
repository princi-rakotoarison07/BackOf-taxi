<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Book View 2</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        .card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; max-width: 520px; }
        .title { font-size: 20px; font-weight: bold; margin-bottom: 8px; }
        .value { font-size: 16px; }
        a { display: inline-block; margin-top: 10px; }
    </style>
</head>
<body>
<div class="card">
    <div class="title">Affichage via ModelAndView (POST)</div>
    <div class="value">Paramètre (idFromParam) = <strong><%= request.getAttribute("idFromParam") %></strong></div>
    <a href="<%= request.getContextPath() %>/methods/form">↩ Retour à la page de test</a>
</div>
</body>
</html>
