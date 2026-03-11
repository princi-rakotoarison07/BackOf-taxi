<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
            <form id="reservationForm" action="${pageContext.request.contextPath}/reservation/save-multiple" method="POST" onsubmit="return collectData()">
                <input type="hidden" name="reservationsData" id="reservationsData">
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
                                    <input type="text" class="form-control field-id" placeholder="RES0001" required>
                                </td>
                                <td>
                                    <input type="text" class="form-control field-client" placeholder="CLT042" required>
                                </td>
                                <td>
                                    <input type="number" class="form-control field-pax" min="1" required>
                                </td>
                                <td>
                                    <select class="form-select hotel-select field-hotel" required>
                                        <option value="">-- Sélectionner --</option>
                                        <% if (hotelsList != null) { 
                                            for (Hotel h : hotelsList) { %>
                                                <option value="<%= h.getIdHotel() %>"><%= h.getNomHotel() %></option>
                                        <%  } 
                                        } %>
                                    </select>
                                </td>
                                <td>
                                    <input type="datetime-local" class="form-control field-date">
                                </td>
                                <td class="pe-4">
                                    <button type="button" class="btn btn-dark btn-sm" onclick="removeRow(this)">-</button>
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                <div class="p-4 border-top d-flex align-items-center">
                    <div class="flex-grow-1">
                        <span class="stats-title me-2">Base de données :</span>
                        <span class="status-highlight"><%= hotelsList != null ? hotelsList.size() : 0 %> Hôtels</span>
                    </div>
                    <div class="d-flex gap-3">
                        <button type="reset" class="btn border">Vider</button>
                        <button type="submit" class="btn btn-primary btn-lg">Confirmer l'envoi</button>
                    </div>
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
                <input type="text" class="form-control field-id" placeholder="RES0001" required>
            </td>
            <td>
                <input type="text" class="form-control field-client" placeholder="CLT042" required>
            </td>
            <td>
                <input type="number" class="form-control field-pax" min="1" required>
            </td>
            <td>
                <select class="form-select hotel-select field-hotel" required>
                    \${hotelOptions}
                </select>
            </td>
            <td>
                <input type="datetime-local" class="form-control field-date">
            </td>
            <td class="pe-4">
                <button type="button" class="btn btn-dark btn-sm" onclick="removeRow(this)">-</button>
            </td>
        `;
        tbody.appendChild(newRow);
    }

    function collectData() {
        const rows = document.querySelectorAll('.reservation-row');
        let data = '';
        rows.forEach((row, index) => {
            const id = row.querySelector('.field-id').value;
            const client = row.querySelector('.field-client').value;
            const pax = row.querySelector('.field-pax').value;
            const hotel = row.querySelector('.field-hotel').value;
            const date = row.querySelector('.field-date').value;
            
            if (id && client && pax && hotel) {
                data += `\${id}|\${client}|\${pax}|\${hotel}|\${date};`;
            }
        });
        document.getElementById('reservationsData').value = data;
        return true;
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
