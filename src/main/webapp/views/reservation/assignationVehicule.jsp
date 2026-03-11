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
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Assignation par Véhicule</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/reservation/form" class="text-decoration-none">Réservations</a></li>
                    <li class="breadcrumb-item active">Assignation par Véhicule</li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <form method="get" action="${pageContext.request.contextPath}/reservation/assignation-vehicule" class="row g-3">
                <div class="col-md-4">
                    <label for="date" class="form-label">Date</label>
                    <input type="date" class="form-control" id="date" name="date" value="<%= selectedDate != null ? selectedDate : "" %>" required>
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary">
                        <i class="fas fa-sync-alt me-1"></i> Afficher
                    </button>
                </div>
            </form>
        </div>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3 d-flex align-items-center justify-content-between">
            <h5 class="mb-0 fw-bold">Véhicules et Réservations assignées</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th style="width: 50px;"></th>
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
                        <tr class="bg-white">
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-primary rounded-circle" type="button" 
                                        data-bs-toggle="collapse" data-bs-target="#<%= collapseId %>" 
                                        aria-expanded="false" style="width: 30px; height: 30px; padding: 0;">
                                    <i class="fas fa-plus"></i>
                                </button>
                            </td>
                            <td><span class="fw-bold text-primary"><%= v.getIdVehicule() %></span></td>
                            <td><%= v.getNbrPlace() != null ? v.getNbrPlace() : "-" %></td>
                            <td>
                                <span class="badge bg-indigo-soft text-primary">
                                    <i class="fas fa-gas-pump me-1"></i><%= t != null ? t.getLibelle() : "-" %>
                                </span>
                            </td>
                            <td><%= selectedDate != null ? selectedDate : "-" %></td>
                        </tr>
                        <tr class="collapse" id="<%= collapseId %>">
                            <td colspan="5" class="p-0">
                                <div class="bg-light p-3 border-top border-bottom">
                                    <table class="table table-sm table-bordered bg-white mb-0 shadow-sm">
                                        <thead class="table-secondary small">
                                            <tr>
                                                <th>ID Réservation</th>
                                                <th>Client</th>
                                                <th>Passagers</th>
                                                <th>Date Resa</th>
                                                <th>Départ Aéroport</th>
                                                <th>Retour Aéroport</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <%
                                                for (Reservation r : assignedResas) {
                                                    java.sql.Timestamp dep = departureTimes != null ? departureTimes.get(r.getIdReservation()) : null;
                                                    java.sql.Timestamp arr = arrivalTimes != null ? arrivalTimes.get(r.getIdReservation()) : null;
                                            %>
                                            <tr class="small">
                                                <td>#<%= r.getIdReservation() %></td>
                                                <td><%= r.getIdClient() %></td>
                                                <td><%= r.getNbrPassager() %></td>
                                                <td><%= r.getDateResa() != null ? df.format(r.getDateResa()) : "-" %></td>
                                                <td><%= dep != null ? df.format(dep) : "-" %></td>
                                                <td><%= arr != null ? df.format(arr) : "-" %></td>
                                            </tr>
                                            <% } %>
                                        </tbody>
                                    </table>
                                </div>
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
                                <div class="text-muted">
                                    <i class="fas fa-calendar-check fa-3x mb-3 opacity-25"></i>
                                    <p class="mb-0">Aucun véhicule n'a de réservation pour cette date.</p>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        // Toggle icon plus/minus on collapse
        const collapses = document.querySelectorAll('.collapse');
        collapses.forEach(el => {
            el.addEventListener('show.bs.collapse', function () {
                const btn = document.querySelector(`[data-bs-target="#${el.id}"]`);
                btn.innerHTML = '<i class="fas fa-minus"></i>';
                btn.classList.replace('btn-outline-primary', 'btn-primary');
            });
            el.addEventListener('hide.bs.collapse', function () {
                const btn = document.querySelector(`[data-bs-target="#${el.id}"]`);
                btn.innerHTML = '<i class="fas fa-plus"></i>';
                btn.classList.replace('btn-primary', 'btn-outline-primary');
            });
        });
    });
</script>

<jsp:include page="../layout/footer.jsp" />
