<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<jsp:include page="../layout/header.jsp" />
<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
    Map<String, Vehicule> assignments = (Map<String, Vehicule>) request.getAttribute("assignments");
    Map<String, java.sql.Timestamp> departureTimes = (Map<String, java.sql.Timestamp>) request.getAttribute("departureTimes");
    Map<String, java.sql.Timestamp> arrivalTimes = (Map<String, java.sql.Timestamp>) request.getAttribute("arrivalTimes");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
    String selectedDate = (String) request.getAttribute("selectedDate");

    Map<String, TypeCarburant> typeById = new HashMap<>();
    if (types != null) {
        for (TypeCarburant t : types) typeById.put(t.getIdTypeCarburant(), t);
    }

    Map<String, List<Reservation>> vehiculeToResa = new HashMap<>();
    if (reservations != null && assignments != null) {
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            if (v != null) {
                vehiculeToResa.computeIfAbsent(v.getIdVehicule(), k -> new ArrayList<>()).add(r);
            }
        }
    }

    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
%>
<div class="container-fluid">
    <div class="mb-4">
        <h3 class="fw-bold mb-1">Assignation par Véhicule</h3>
        <p class="text-muted small">Vue groupée par moyen de transport</p>
    </div>

    <div class="card mb-4">
        <div class="card-body">
            <form method="get" action="${pageContext.request.contextPath}/reservation/assignation-vehicule" class="row g-3">
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
            <h5 class="mb-0">Planning des véhicules</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead>
                        <tr>
                            <th style="width: 60px;"></th>
                            <th>Véhicule</th>
                            <th>Capacité</th>
                            <th>Carburant</th>
                            <th>Date</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            boolean hasAssignments = false;
                            if (vehicules != null) {
                                for (Vehicule v : vehicules) {
                                    List<Reservation> assignedResas = vehiculeToResa.get(v.getIdVehicule());
                                    if (assignedResas != null && !assignedResas.isEmpty()) {
                                        hasAssignments = true;
                                        TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                                        String collapseId = "collapse-" + v.getIdVehicule();
                        %>
                        <tr class="bg-white border-bottom">
                            <td class="text-center">
                                <button class="btn btn-sm btn-dark" type="button" 
                                        data-bs-toggle="collapse" data-bs-target="#<%= collapseId %>">
                                    +
                                </button>
                            </td>
                            <td class="fw-bold"><%= v.getIdVehicule() %></td>
                            <td><%= v.getNbrPlace() != null ? v.getNbrPlace() : "-" %> places</td>
                            <td>
                                <% if (t != null) { %>
                                    <span class="badge-system"><%= t.getLibelle() %></span>
                                <% } %>
                            </td>
                            <td><%= selectedDate != null ? selectedDate : "-" %></td>
                        </tr>
                        <tr class="collapse" id="<%= collapseId %>">
                            <td colspan="5" class="p-4 bg-light">
                                <table class="table table-sm table-bordered bg-white mb-0">
                                    <thead class="bg-dark text-white">
                                        <tr style="font-size: 0.7rem;">
                                            <th>ID</th>
                                            <th>Client</th>
                                            <th>Pax</th>
                                            <th>Date Resa</th>
                                            <th>Départ</th>
                                            <th>Retour</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <%
                                            for (Reservation r : assignedResas) {
                                                java.sql.Timestamp dep = departureTimes != null ? departureTimes.get(r.getIdReservation()) : null;
                                                java.sql.Timestamp arr = arrivalTimes != null ? arrivalTimes.get(r.getIdReservation()) : null;
                                        %>
                                        <tr style="font-size: 0.85rem;">
                                            <td>#<%= r.getIdReservation() %></td>
                                            <td><%= r.getIdClient() %></td>
                                            <td><%= r.getNbrPassager() %></td>
                                            <td><%= r.getDateResa() != null ? df.format(r.getDateResa()) : "-" %></td>
                                            <td class="fw-bold text-dark"><%= dep != null ? df.format(dep) : "-" %></td>
                                            <td class="fw-bold text-dark"><%= arr != null ? df.format(arr) : "-" %></td>
                                        </tr>
                                        <% } %>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <%
                                    }
                                }
                            }
                            if (!hasAssignments) {
                        %>
                        <tr>
                            <td colspan="5" class="text-center py-5">
                                <p class="text-muted">Aucune assignation pour cette période.</p>
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
