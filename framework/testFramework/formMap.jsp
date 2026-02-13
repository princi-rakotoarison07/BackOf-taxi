<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Formulaire vers Map</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        label { display: block; margin-top: 10px; }
        input { width: 320px; padding: 6px; }
        button { margin-top: 12px; padding: 8px 12px; }
        .note { margin-top: 16px; color: #555; }
    </style>
</head>
<body>
<h1>Formulaire vers Map</h1>
<form action="submitMap" method="get">
    <label for="nom">nom (String)</label>
    <input type="text" id="nom" name="nom" />

    <label for="age">age (String, ex: 21)</label>
    <input type="text" id="age" name="age" />

    <label for="tags">tags (plusieurs valeurs -> deviendra null)</label>
    <input type="text" id="tags" name="tags" value="java" />
    <input type="text" id="tags2" name="tags" value="web" />

    <button type="submit">Envoyer</button>
</form>

<div class="note">
    - Les paramètres à valeur unique seront mis dans la Map comme String.<br/>
    - Les paramètres avec plusieurs valeurs (ex: "tags") seront mis à null.
</div>
</body>
</html>
