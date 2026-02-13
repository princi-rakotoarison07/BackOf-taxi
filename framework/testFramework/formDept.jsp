<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Formulaire Département</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        label { display: block; margin-top: 10px; }
        input, textarea { width: 300px; padding: 6px; }
        button { margin-top: 12px; padding: 8px 12px; }
        .note { margin-top: 16px; color: #555; }
    </style>
</head>
<body>
<h1>Formulaire Département</h1>
<form action="submitDept" method="get">
    <label for="id">ID</label>
    <input type="number" id="id" name="id" required />

    <label for="nom">Nom</label>
    <input type="text" id="nom" name="nom" required />

    <label for="description">Description</label>
    <textarea id="description" name="description" rows="3"></textarea>

    <button type="submit">Envoyer</button>
</form>

<div class="note">
    Cette version utilise la méthode GET pour simplifier la démonstration.
    Après l'envoi, consultez la console/terminal de Tomcat pour voir les valeurs reçues.
</div>
</body>
</html>
