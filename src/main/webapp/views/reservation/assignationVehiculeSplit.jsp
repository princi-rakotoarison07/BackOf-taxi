<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<%@ page import="com.taxi.model.Hotel" %>
<%@ page import="com.taxi.controller.ReservationController.ReservationPortion" %>
<jsp:include page="../layout/header.jsp" />
<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
    Map<String, TypeCarburant> typeById = (Map<String, TypeCarburant>) request.getAttribute("typeById");
    Map<String, Hotel> hotelMap = (Map<String, Hotel>) request.getAttribute("hotelMap");
    String selectedDate = (String) request.getAttribute("selectedDate");

    Map<String, List<ReservationPortion>> splitPortions = (Map<String, List<ReservationPortion>>) request.getAttribute("splitPortions");
    Map<String, Integer> splitReliquats = (Map<String, Integer>) request.getAttribute("splitReliquats");

    Map<String, Vehicule> vehiculeMap = new HashMap<>();
    if (vehicules != null) {
        for (Vehicule v : vehicules) vehiculeMap.put(v.getIdVehicule(), v);
    }
%>

<div class="container-fluid">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Assignation fractionnée (Split)</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/form" class="text-decoration-none">Réservations</a></li>
                    <li class="breadcrumb-item active">Assignation fractionnée</li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <form method="get" action="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation-vehicule-split" class="row g-3">
                <div class="col-md-4">
                    <label for="date" class="form-label">Date</label>
                    <input type="date" class="form-control" id="date" name="date" value="<%= selectedDate != null ? selectedDate : "" %>" required>
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary">Afficher</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold">Reliquats (places non assignées)</h5>
        </div>
        <div class="card-body">
            <%
                if (splitReliquats != null && !splitReliquats.isEmpty() && reservations != null) {
            %>
            <div class="table-responsive">
                <table class="table table-sm align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th>Réservation</th>
                            <th>Client</th>
                            <th>Hôtel</th>
                            <th>Places non assignées</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            Map<String, Reservation> resaMap = new HashMap<>();
                            for (Reservation r : reservations) resaMap.put(r.getIdReservation(), r);

                            for (Map.Entry<String, Integer> e : splitReliquats.entrySet()) {
                                Reservation r = resaMap.get(e.getKey());
                                Hotel h = (r != null && hotelMap != null) ? hotelMap.get(r.getIdHotel()) : null;
                        %>
                        <tr>
                            <td><%= e.getKey() %></td>
                            <td><%= r != null ? r.getIdClient() : "-" %></td>
                            <td><%= h != null ? h.getNomHotel() : (r != null ? r.getIdHotel() : "-") %></td>
                            <td><span class="badge bg-danger"><%= e.getValue() %></span></td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
            <%
                } else {
            %>
                <div class="text-muted">Aucun reliquat pour cette date.</div>
            <% } %>
        </div>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold">Portions assignées par véhicule</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th>Véhicule</th>
                            <th>Capacité</th>
                            <th>Carburant</th>
                            <th>Portions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (vehicules != null) {
                                for (Vehicule v : vehicules) {
                                    TypeCarburant t = (typeById != null && v != null) ? typeById.get(v.getIdTypeCarburant()) : null;
                                    List<ReservationPortion> portions = (splitPortions != null) ? splitPortions.get(v.getIdVehicule()) : null;
                        %>
                        <tr>
                            <td><span class="fw-bold text-primary"><%= v.getIdVehicule() %></span></td>
                            <td><%= v.getNbrPlace() != null ? v.getNbrPlace() : "-" %></td>
                            <td><%= t != null ? t.getLibelle() : "-" %></td>
                            <td>
                                <%
                                    if (portions != null && !portions.isEmpty()) {
                                        for (ReservationPortion p : portions) {
                                            Reservation r = p.getReservation();
                                            Hotel h = (r != null && hotelMap != null) ? hotelMap.get(r.getIdHotel()) : null;
                                %>
                                    <div class="small border rounded px-2 py-1 mb-1 bg-white">
                                        <span class="fw-bold"><%= r != null ? r.getIdClient() : "-" %></span>
                                        <span class="text-muted">(</span>
                                        <span class="text-muted"><%= r != null ? r.getIdReservation() : "-" %></span>
                                        <span class="text-muted">)</span>
                                        <span class="mx-2">→</span>
                                        <span class="fw-bold"><%= h != null ? h.getNomHotel() : (r != null ? r.getIdHotel() : "-") %></span>
                                        <span class="badge bg-success ms-2"><%= p.getPlacesAssignees() %> places</span>
                                    </div>
                                <%
                                        }
                                    } else {
                                %>
                                    <span class="text-muted">Aucune portion</span>
                                <% } %>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
