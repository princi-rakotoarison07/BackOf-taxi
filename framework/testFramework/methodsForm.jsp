<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Test GET / POST / SimpleMapping</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        .grid { display: grid; grid-template-columns: repeat(2, minmax(280px, 420px)); gap: 20px; }
        .card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; }
        .title { font-size: 18px; font-weight: bold; margin-bottom: 10px; }
        label { display: block; margin: 8px 0 4px; }
        input[type="text"] { padding: 6px 8px; width: 100%; box-sizing: border-box; }
        .actions { margin-top: 12px; }
        button { padding: 8px 12px; }
        a.button { display: inline-block; padding: 8px 12px; border: 1px solid #888; border-radius: 6px; text-decoration: none; }
    </style>
</head>
<body>
<h2>Démo des mappings: GET, POST, Simple</h2>
<div class="grid">
    <div class="card">
        <div class="title">GET Mapping</div>
        <form action="<%= request.getContextPath() %>/methods/get" method="get">
            <label for="gname">name</label>
            <input id="gname" name="name" type="text" value="Alice"/>
            <div class="actions">
                <button type="submit">Envoyer (GET)</button>
            </div>
        </form>
    </div>
    <div class="card">
        <div class="title">POST Mapping</div>
        <form action="<%= request.getContextPath() %>/methods/post" method="post">
            <label for="pname">name</label>
            <input id="pname" name="name" type="text" value="Bob"/>
            <div class="actions">
                <button type="submit">Envoyer (POST)</button>
            </div>
        </form>
    </div>
    <div class="card">
        <div class="title">SimpleMapping (ANY)</div>
        <p>
            <a class="button" href="<%= request.getContextPath() %>/methods/simple?q=ping">Tester en GET</a>
        </p>
        <form action="<%= request.getContextPath() %>/methods/simple" method="post">
            <label for="q">q</label>
            <input id="q" name="q" type="text" value="pong"/>
            <div class="actions">
                <button type="submit">Tester en POST</button>
            </div>
        </form>
    </div>
</div>
</body>
</html>
