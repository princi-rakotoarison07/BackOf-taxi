<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Employé - Formulaire</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        label { display: block; margin-top: 10px; }
        input { width: 320px; padding: 6px; }
        button { margin-top: 12px; padding: 8px 12px; }
        fieldset { margin-top: 18px; }
    </style>
</head>
<body>
<h1>Employé - Formulaire</h1>
<form action="/testFramework/employe/save" method="post">
    <label for="nom">Nom</label>
    <input type="text" id="nom" name="nom" />

    <label for="prenom">Prénom</label>
    <input type="text" id="prenom" name="prenom" />

    <label for="age">Âge</label>
    <input type="number" id="age" name="age" />

    <label for="poste">Poste</label>
    <input type="text" id="poste" name="poste" />

    <fieldset>
        <legend>Département</legend>
        <label for="dept.id">ID</label>
        <input type="number" id="dept.id" name="dept.id" />

        <label for="dept.nom">Nom</label>
        <input type="text" id="dept.nom" name="dept.nom" />

        <label for="dept.description">Description</label>
        <input type="text" id="dept.description" name="dept.description" />

        <fieldset style="margin-top:12px;">
            <legend>Ville</legend>
            <label for="dept.ville.id">ID Ville</label>
            <input type="number" id="dept.ville.id" name="dept.ville.id" />

            <label for="dept.ville.nom">Nom Ville</label>
            <input type="text" id="dept.ville.nom" name="dept.ville.nom" />

            <label for="dept.ville.codePostal">Code Postal</label>
            <input type="text" id="dept.ville.codePostal" name="dept.ville.codePostal" />
        </fieldset>
    </fieldset>

    <button type="submit">Enregistrer</button>
</form>
</body>
</html>
