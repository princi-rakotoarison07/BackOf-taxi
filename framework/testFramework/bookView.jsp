<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Book View</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 24px; }
        .card { border: 1px solid #ddd; border-radius: 8px; padding: 16px; max-width: 500px; }
        .title { font-size: 20px; font-weight: bold; margin-bottom: 8px; }
        .value { font-size: 16px; }
    </style>
</head>
<body>
<div class="card">
    <div class="title">Book Details</div>
    <div class="value">Path variable id = <strong><%= request.getAttribute("bookId") %></strong></div>
</div>
</body>
</html>
