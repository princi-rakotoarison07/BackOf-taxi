<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Nouvelle Réservation"); %>
<jsp:include page="../layout/header.jsp" />

<div class="container-fluid">
    <div class="row mb-4 g-4">
        <div class="col-md-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="stats-title">Total Hôtels</div>
                    <div class="stats-value" id="hotelCount">0</div>
                </div>
            </div>
        </div>
        <div class="col-md-3">
            <div class="card h-100">
                <div class="card-body">
                    <div class="stats-title">Statut Système</div>
                    <div class="status-highlight">Opérationnel</div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h5 class="m-0">Nouvelle réservation</h5>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/reservation/save" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-3">
                                <label class="form-label">Référence Réservation</label>
                                <input type="text" name="idReservation" class="form-control" placeholder="RES0001" required>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Identifiant Client</label>
                                <input type="text" name="idClient" class="form-control" placeholder="CLT042" required>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">Passagers</label>
                                <input type="number" name="nbrPassager" class="form-control" min="1" required>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Établissement Hôtelier</label>
                                <select name="idHotel" id="hotelSelect" class="form-select" required>
                                    <option value="">Chargement...</option>
                                </select>
                            </div>
                        </div>

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label">Date et Heure</label>
                                <input type="datetime-local" name="dateResa" class="form-control">
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-3 border-top pt-4">
                            <button type="reset" class="btn border">Annuler</button>
                            <button type="submit" class="btn btn-primary">Confirmer</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const hotelSelect = document.getElementById('hotelSelect');
        const hotelCount = document.getElementById('hotelCount');
        
        fetch('${pageContext.request.contextPath}/api/hotels')
            .then(response => response.json())
            .then(hotels => {
                hotelCount.textContent = hotels.length;
                hotelSelect.innerHTML = '<option value="">-- Sélectionner --</option>';
                hotels.forEach(hotel => {
                    const option = document.createElement('option');
                    option.value = hotel.idHotel;
                    option.textContent = hotel.nomHotel;
                    hotelSelect.appendChild(option);
                });
            })
            .catch(error => {
                hotelSelect.innerHTML = '<option value="">Erreur</option>';
                hotelCount.textContent = '-';
            });
    });
</script>

<jsp:include page="../layout/footer.jsp" />
