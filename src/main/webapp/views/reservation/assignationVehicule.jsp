<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<%@ page import="com.taxi.model.Hotel" %>
<jsp:include page="../layout/header.jsp" />
<style>

.final-return {
    border-left: 3px solid #dc3545;
    background: #fff5f5;
    padding: 15px;
    border-radius: 6px;
    margin-top: 20px;
}
</style>
<%
    List<Reservation> reservations = (List<Reservation>) request.getAttribute("reservations");
    List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
    Map<String, Vehicule> assignments = (Map<String, Vehicule>) request.getAttribute("assignments");
    Map<String, java.sql.Timestamp> departureTimes = (Map<String, java.sql.Timestamp>) request.getAttribute("departureTimes");
    Map<String, java.sql.Timestamp> arrivalTimes = (Map<String, java.sql.Timestamp>) request.getAttribute("arrivalTimes");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
    String selectedDate = (String) request.getAttribute("selectedDate");
    List<Hotel> hotels = (List<Hotel>) request.getAttribute("hotels");
    Map<String, Hotel> hotelMap = (Map<String, Hotel>) request.getAttribute("hotelMap");
    Map<String, List<Reservation>> tourOrders = (Map<String, List<Reservation>>) request.getAttribute("tourOrders");
    Map<String, Map<String, java.sql.Timestamp>> detailedTimes = (Map<String, Map<String, java.sql.Timestamp>>) request.getAttribute("detailedTimes");
    Map<String, java.math.BigDecimal> tourDistancesKm = (Map<String, java.math.BigDecimal>) request.getAttribute("tourDistancesKm");

    Map<String, TypeCarburant> typeById = new HashMap<>();
    if (types != null) {
        for (TypeCarburant t : types) typeById.put(t.getIdTypeCarburant(), t);
    }

    Map<String, Vehicule> vehiculeMap = new HashMap<>();
    if (vehicules != null) {
        for (Vehicule v : vehicules) vehiculeMap.put(v.getIdVehicule(), v);
    }

    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Grouper par véhicule ET par créneau horaire
    // Clé: vehicleId + "|" + depTime + "|" + arrTime
    Map<String, List<Reservation>> slotToResa = new LinkedHashMap<>();
    if (reservations != null && assignments != null) {
        for (Reservation r : reservations) {
            Vehicule v = assignments.get(r.getIdReservation());
            if (v != null) {
                java.sql.Timestamp dep = departureTimes != null ? departureTimes.get(r.getIdReservation()) : null;
                java.sql.Timestamp arr = arrivalTimes != null ? arrivalTimes.get(r.getIdReservation()) : null;
                String depStr = dep != null ? df.format(dep) : "-";
                String arrStr = arr != null ? df.format(arr) : "-";
                String key = v.getIdVehicule() + "|" + depStr + "|" + arrStr;
                slotToResa.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
            }
        }
    }
%>
<div class="container-fluid">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Assignation par Véhicule</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/form" class="text-decoration-none">Réservations</a></li>
                    <li class="breadcrumb-item active">Assignation par Véhicule</li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <form method="get" action="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation-vehicule" class="row g-3">
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
                            <th>h_depart</th>
                            <th>vehicule</th>
                            <th>liste_resa_hotel</th>
                            <th>km_parcouru</th>
                            <th>h_retour</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            boolean hasAssignments = false;
                            if (!slotToResa.isEmpty()) {
                                hasAssignments = true;
                                for (Map.Entry<String, List<Reservation>> entry : slotToResa.entrySet()) {
                                    String key = entry.getKey();
                                    List<Reservation> assignedResas = entry.getValue();
                                    String[] parts = key.split("\\|");
                                    String vId = parts[0];
                                    String depTime = parts[1];
                                    String arrTime = parts[2];
                                    
                                    Vehicule v = vehiculeMap.get(vId);
                                    TypeCarburant t = v != null ? typeById.get(v.getIdTypeCarburant()) : null;
                                    List<Reservation> orderedTour = (tourOrders != null && tourOrders.containsKey(key)) ? tourOrders.get(key) : assignedResas;
                                    java.math.BigDecimal distanceKm = (tourDistancesKm != null && tourDistancesKm.containsKey(key)) ? tourDistancesKm.get(key) : java.math.BigDecimal.ZERO;
                        %>
                        <tr class="bg-white">
                            <td><%= depTime %></td>
                            <td>
                                <div class="fw-bold text-primary"><%= vId %></div>
                                <div class="small text-muted">
                                    <%= t != null ? t.getLibelle() : "-" %>
                                    <%= v != null && v.getNbrPlace() != null ? " | " + v.getNbrPlace() + " places" : "" %>
                                </div>
                            </td>
                            <td>
                                <ul class="mb-0 ps-3 small">
                                    <%
                                        if (orderedTour != null && !orderedTour.isEmpty()) {
                                            for (Reservation currentResa : orderedTour) {
                                                Hotel currentHotel = hotelMap != null ? hotelMap.get(currentResa.getIdHotel()) : null;
                                                String hotelName = currentHotel != null ? currentHotel.getNomHotel() : currentResa.getIdHotel();
                                    %>
                                    <li><strong><%= currentResa.getIdReservation() %></strong> - <%= hotelName %> (<%= currentResa.getNbrPassager() %> pax)</li>
                                    <%
                                            }
                                        }
                                    %>
                                </ul>
                            </td>
                            <td>
                                <span class="fw-bold"><%= distanceKm.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString() %> km</span>
                            </td>
                            <td><%= arrTime %></td>
                        </tr>
                        <%
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

<jsp:include page="../layout/footer.jsp" />
