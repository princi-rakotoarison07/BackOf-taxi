<%@ page import="com.taxi.model.Hotel" %>
<%@ page import="java.util.List" %>
<% request.setAttribute("pageTitle", "Nouvelle Réservation"); %>
<jsp:include page="../layout/header.jsp" />
<%
    List<Hotel> hotelsList = (List<Hotel>) request.getAttribute("hotels");
%>

<style>
    .hotel-select {
        color: #000 !important;
        border: 1px solid #000 !important;
        background-color: #fff !important;
    }
    .hotel-select option {
        color: #000;
        background-color: #fff;
    }
</style>

<div class="container-fluid">
    <div class="mb-4">
        <h3 class="fw-bold mb-1">Nouvelle Réservation</h3>
        <p class="text-muted small">Insérez une ou plusieurs réservations simultanément</p>
    </div>

    <div class="card">
        <div class="card-header d-flex justify-content-between align-items-center">
            <h5 class="mb-0">Tableau de saisie</h5>
            <button type="button" class="btn btn-primary" onclick="addRow()">+ Ajouter une ligne</button>
        </div>
        <div class="card-body p-0">
            <form action="${pageContext.request.contextPath}/reservation/save-multiple" method="POST">
                <div class="table-responsive">
                    <table class="table align-middle mb-0" id="reservationTable">
                        <thead>
                            <tr>
                                <th class="ps-4">Référence</th>
                                <th>ID Client</th>
                                <th>Passagers</th>
                                <th>Hôtel</th>
                                <th>Date et Heure</th>
                                <th style="width: 50px;"></th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr class="reservation-row">
                                <td class="ps-4">
                                    <input type="text" name="idReservation" class="form-control" placeholder="RES0001" required>
                                </td>
                                <td>
                                    <input type="text" name="idClient" class="form-control" placeholder="CLT042" required>
                                </td>
                                <td>
                                    <input type="number" name="nbrPassager" class="form-control" min="1" required>
                                </td>
                                <td>
                                    <select name="idHotel" class="form-select hotel-select" required>
                                        <option value="">-- Sélectionner --</option>
                                        <% if (hotelsList != null) { 
                                            for (Hotel h : hotelsList) { %>
                                                <option value="<%= h.getIdHotel() %>"><%= h.getNomHotel() %></option>
                                        <%  } 
                                        } %>
                                    </select>
                                </td>
                                <td>
                                    <input type="datetime-local" name="dateResa" class="form-control">
                                </td>
                                <td class="pe-4">
                                    <button type="button" class="btn btn-dark btn-sm" onclick="removeRow(this)">-</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="p-4 border-top d-flex justify-content-end gap-3">
                    <div class="me-auto align-self-center">
                        <span class="stats-title me-2">Hôtels en base :</span>
                        <span class="status-highlight" id="hotelCount"><%= hotelsList != null ? hotelsList.size() : 0 %></span>
                    </div>
                    <button type="reset" class="btn border">Annuler</button>
                    <button type="submit" class="btn btn-primary btn-lg">Confirmer toutes les réservations</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script>
    // Pré-générer les options à partir des données injectées par le serveur
    let hotelOptions = '<option value="">-- Sélectionner --</option>';
    <% if (hotelsList != null) { 
        for (Hotel h : hotelsList) { %>
            hotelOptions += '<option value="<%= h.getIdHotel() %>"><%= h.getNomHotel().replace("'", "\\'") %></option>';
    <%  } 
    } %>

    function addRow() {
        const tbody = document.querySelector('#reservationTable tbody');
        const newRow = document.createElement('tr');
        newRow.className = 'reservation-row';
        newRow.innerHTML = `
            <td class="ps-4">
                <input type="text" name="idReservation" class="form-control" placeholder="RES0001" required>
            </td>
            <td>
                <input type="text" name="idClient" class="form-control" placeholder="CLT042" required>
            </td>
            <td>
                <input type="number" name="nbrPassager" class="form-control" min="1" required>
            </td>
            <td>
                <select name="idHotel" class="form-select hotel-select" required>
                    ${hotelOptions}
                </select>
            </td>
            <td>
                <input type="datetime-local" name="dateResa" class="form-control">
            </td>
            <td class="pe-4">
                <button type="button" class="btn btn-dark btn-sm" onclick="removeRow(this)">-</button>
            </td>
        `;
        tbody.appendChild(newRow);
    }

    function removeRow(btn) {
        const rows = document.querySelectorAll('.reservation-row');
        if (rows.length > 1) {
            btn.closest('tr').remove();
        } else {
            alert('Il faut au moins une ligne.');
        }
    }
</script>

<jsp:include page="../layout/footer.jsp" />
