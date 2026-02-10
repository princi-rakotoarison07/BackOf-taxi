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
            <label>Hôtel :</label>
            <select name="idHotel" id="hotelSelect" required>
                <option value="">Chargement des hôtels...</option>
            </select>
        </div>
        <div class="form-group">
            <label>Date de réservation :</label>
            <input type="datetime-local" name="dateResa">
        </div>
        <button type="submit">Enregistrer</button>
    </form>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const hotelSelect = document.getElementById('hotelSelect');
            
            // Récupérer la liste des hôtels via l'API REST du framework
            fetch('${pageContext.request.contextPath}/api/hotels')
                .then(response => response.json())
                .then(hotels => {
                    hotelSelect.innerHTML = '<option value="">-- Choisir un hôtel --</option>';
                    hotels.forEach(hotel => {
                        const option = document.createElement('option');
                        option.value = hotel.idHotel;
                        option.textContent = hotel.nomHotel;
                        hotelSelect.appendChild(option);
                    });
                })
                .catch(error => {
                    console.error('Erreur lors du chargement des hôtels:', error);
                    hotelSelect.innerHTML = '<option value="">Erreur de chargement</option>';
                });
        });
    </script>
</body>
</html>
