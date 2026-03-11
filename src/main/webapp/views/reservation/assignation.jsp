<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<jsp:include page="../layout/header.jsp" />
<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    Map<String, Vehicule> assignments = (Map<String, Vehicule>) request.getAttribute("assignments");
    Map<String, java.sql.Timestamp> departureTimes = (Map<String, java.sql.Timestamp>) request.getAttribute("departureTimes");
    Map<String, java.sql.Timestamp> arrivalTimes = (Map<String, java.sql.Timestamp>) request.getAttribute("arrivalTimes");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
    String selectedDate = (String) request.getAttribute("selectedDate");
    Map<String, TypeCarburant> typeById = new HashMap<>();
    if (types != null) {
        for (TypeCarburant t : types) typeById.put(t.getIdTypeCarburant(), t);
    }
    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<div class="container-fluid">
    <div class="mb-4">
        <h3 class="fw-bold mb-1">Assignation des Réservations</h3>
        <p class="text-muted small">Vue détaillée par réservation</p>
    </div>

    <div class="card mb-4">
        <div class="card-body">
            <form method="get" action="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation" class="row g-3">
                <div class="col-md-4">
                    <label for="date" class="form-label">Filtrer par date</label>
                    <input type="date" class="form-control" id="date" name="date" value="<%= selectedDate != null ? selectedDate : "" %>" required>
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary">Mettre à jour</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h5 class="mb-0">Liste des réservations</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="ps-4">ID</th>
                            <th>Client</th>
                            <th>Pax</th>
                            <th>Date</th>
                            <th>Véhicule</th>
                            <th>Capacité</th>
                            <th>Carburant</th>
                            <th>Départ</th>
                            <th>Retour</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (reservations != null && !reservations.isEmpty()) {
                                for (Reservation r : reservations) {
                                    Vehicule v = assignments != null ? assignments.get(r.getIdReservation()) : null;
                                    java.sql.Timestamp dep = departureTimes != null ? departureTimes.get(r.getIdReservation()) : null;
                                    java.sql.Timestamp arr = arrivalTimes != null ? arrivalTimes.get(r.getIdReservation()) : null;
                                    TypeCarburant t = null;
                                    if (v != null) {
                                        t = typeById.get(v.getIdTypeCarburant());
                                    }
                        %>
                        <tr>
                            <td class="ps-4 fw-bold">#<%= r.getIdReservation() %></td>
                            <td><%= r.getIdClient() %></td>
                            <td><%= r.getNbrPassager() %></td>
                            <td><%= r.getDateResa() != null ? df.format(r.getDateResa()) : "-" %></td>
                            <td><%= v != null ? v.getIdVehicule() : "-" %></td>
                            <td><%= v != null && v.getNbrPlace() != null ? v.getNbrPlace() : "-" %></td>
                            <td>
                                <% if (t != null) { %>
                                    <span class="badge-system"><%= t.getLibelle() %></span>
                                <% } else { %>
                                    -
                                <% } %>
                            </td>
                            <td><%= dep != null ? df.format(dep) : "-" %></td>
                            <td><%= arr != null ? df.format(arr) : "-" %></td>
                        </tr>
                        <%      }
                            } else { %>
                        <tr>
                            <td colspan="9" class="text-center py-5">
                                <p class="text-muted mb-0">Aucune donnée disponible pour cette date.</p>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<jsp:include page="../layout/footer.jsp" />
