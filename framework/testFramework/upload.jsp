<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Upload de fichier</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        label { display: block; margin-top: 8px; }
        input[type="text"] { width: 320px; padding: 6px; }
        input[type="file"] { margin-top: 6px; }
        button { margin-top: 12px; padding: 8px 12px; }
        .info { margin-top: 18px; padding: 12px; border: 1px solid #ddd; border-radius: 8px; }
        .ok { background: #f6fff6; }
        .warn { background: #fffaf2; }
        .label { font-weight: bold; }
    </style>
</head>
<body>

<h1>Upload de fichier</h1>

<form action="/testFramework/upload" method="post" enctype="multipart/form-data">
    <label for="title">Titre</label>
    <input type="text" id="title" name="title" />

    <label for="photo">Fichier</label>
    <input type="file" id="photo" name="photo" />

    <button type="submit">Uploader</button>
</form>

<%
    Object stored = request.getAttribute("stored");
    Object original = request.getAttribute("original");
    Object size = request.getAttribute("size");
    Object title = request.getAttribute("title");
    boolean hasResult = (stored != null || original != null || size != null || title != null);
%>

<% if (hasResult) { %>
    <div class="info <%= (stored != null ? "ok" : "warn") %>">
        <h3>Résultat</h3>
        <p><span class="label">Titre:</span> <%= title %></p>
        <% if (stored != null) { %>
            <p><span class="label">Chemin enregistré:</span> <%= stored %></p>
            <p><span class="label">Nom original:</span> <%= original %></p>
            <p><span class="label">Taille (octets):</span> <%= size %></p>
        <% } else { %>
            <p>Aucun fichier enregistré. Vérifiez que vous avez bien choisi un fichier avant de soumettre.</p>
        <% } %>
    </div>
<% } %>

</body>
</html>