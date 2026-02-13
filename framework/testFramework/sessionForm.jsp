<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Session - Form</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        label { display: block; margin-top: 10px; }
        input { width: 320px; padding: 6px; }
        button { margin-top: 12px; padding: 8px 12px; }
        .row { margin-top: 14px; }
        a.btn { display: inline-block; margin-top: 12px; text-decoration: none; padding: 8px 12px; background: #1976d2; color: #fff; border-radius: 4px; }
    </style>
</head>
<body>
<h1>Gestion de Session</h1>

<form action="sessionSet" method="get">
    <label for="key">Clé</label>
    <input type="text" id="key" name="key" placeholder="ex: user" />

    <label for="value">Valeur</label>
    <input type="text" id="value" name="value" placeholder="ex: taloha" />

    <div class="row">
        <button type="submit">Enregistrer dans la session</button>
    </div>
</form>

<form action="sessionRemove" method="get">
    <label for="rmkey">Supprimer une clé</label>
    <input type="text" id="rmkey" name="key" placeholder="clé à supprimer" />
    <div class="row">
        <button type="submit">Supprimer</button>
    </div>
</form>

<div class="row">
    <a class="btn" href="sessionClear">Vider toute la session</a>
    <a class="btn" href="sessionShow" style="background:#388e3c">Voir la session</a>
</div>

</body>
</html>
