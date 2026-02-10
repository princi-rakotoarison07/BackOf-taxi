<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nouvelle Réservation</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 50px; }
        .form-group { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; }
        input, select { width: 300px; padding: 8px; }
        button { padding: 10px 20px; background-color: #007bff; color: white; border: none; cursor: pointer; }
    </style>
</head>
<body>
    <h1>Formulaire de Réservation</h1>
    <form action="${pageContext.request.contextPath}/reservation/save" method="POST">
        <div class="form-group">
            <label>ID Réservation (ex: RES0001):</label>
            <input type="text" name="idReservation" required>
        </div>
        <div class="form-group">
            <label>ID Client:</label>
            <input type="text" name="idClient" required>
        </div>
        <div class="form-group">
            <label>Nombre de passagers:</label>
            <input type="number" name="nbrPassager" min="1" required>
        </div>
        <div class="form-group">
            <label>Hôtel:</label>
            <select name="idHotel" required>
                <option value="HOT0001">Hilton Madagascar</option>
                <option value="HOT0002">Carlton Anosy</option>
                <option value="HOT0003">Radisson Blu</option>
            </select>
        </div>
        <div class="form-group">
            <label>Date de réservation (YYYY-MM-DD HH:mm:ss):</label>
            <input type="text" name="dateResa" placeholder="2026-02-10 12:00:00">
        </div>
        <button type="submit">Enregistrer</button>
    </form>
</body>
</html>
