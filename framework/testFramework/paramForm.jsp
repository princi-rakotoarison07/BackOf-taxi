<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Test @Param</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        .card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; max-width: 520px; }
        .title { font-size: 20px; font-weight: bold; margin-bottom: 12px; }
        .row { margin-bottom: 10px; }
        label { display: inline-block; width: 120px; }
        input[type="text"], input[type="number"] { padding: 6px 8px; width: 220px; }
        .actions { margin-top: 14px; display: flex; gap: 10px; }
        button { padding: 8px 14px; }
        .hint { color: #666; font-size: 12px; margin-top: 10px; }
    </style>
</head>
<body>
<div class="card">
    <div class="title">Formulaire de test pour @Param</div>
    <form action="<%= request.getContextPath() %>/book/submit" method="get">
        <div class="row">
            <label for="id">id (number)</label>
            <input id="id" name="id" type="number" value="123" />
        </div>
        <div class="actions">
            <button type="submit">Envoyer</button>
        </div>
    </form>
</div>
</body>
</html>
