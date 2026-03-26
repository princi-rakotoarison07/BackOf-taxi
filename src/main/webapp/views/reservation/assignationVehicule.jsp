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
    Map<String, List<String>> splitDetails = (Map<String, List<String>>) request.getAttribute("splitDetails");

    Map<String, TypeCarburant> typeById = new HashMap<>();
    if (types != null) {
        for (TypeCarburant t : types) typeById.put(t.getIdTypeCarburant(), t);
    }

    Map<String, Vehicule> vehiculeMap = new HashMap<>();
    if (vehicules != null) {
        for (Vehicule v : vehicules) vehiculeMap.put(v.getIdVehicule(), v);
    }

    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Grouper par véhicule ET par créneau horaire.
    // Clé: vehicleId + "|" + depTime + "|" + arrTime
    // On conserve aussi le nb de pax reellement assignes par reservation dans ce vehicule.
    Map<String, List<Reservation>> slotToResa = new LinkedHashMap<>();
    Map<String, Map<String, Integer>> slotToAssignedPax = new LinkedHashMap<>();
    Map<String, Set<String>> slotToResaIds = new HashMap<>();

    if (reservations != null) {
        for (Reservation r : reservations) {
            String resaId = r.getIdReservation();
            java.sql.Timestamp dep = departureTimes != null ? departureTimes.get(resaId) : null;
            java.sql.Timestamp arr = arrivalTimes != null ? arrivalTimes.get(resaId) : null;
            String depStr = dep != null ? df.format(dep) : "-";
            String arrStr = arr != null ? df.format(arr) : "-";

            List<String> splits = splitDetails != null ? splitDetails.get(resaId) : null;
            boolean addedFromSplit = false;
            if (splits != null && !splits.isEmpty()) {
                for (String split : splits) {
                    if (split == null || !split.contains(":")) {
                        continue;
                    }
                    String[] parts = split.split(":", 2);
                    String vehiculeId = parts[0] != null ? parts[0].trim() : "";
                    int paxAssigned = 0;
                    try {
                        paxAssigned = Integer.parseInt(parts[1].trim());
                    } catch (Exception ignore) {
                        paxAssigned = 0;
                    }
                    if (vehiculeId.isEmpty() || paxAssigned <= 0) {
                        continue;
                    }

                    String key = vehiculeId + "|" + depStr + "|" + arrStr;
                    Set<String> resaIds = slotToResaIds.computeIfAbsent(key, k -> new HashSet<>());
                    if (!resaIds.contains(resaId)) {
                        slotToResa.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                        resaIds.add(resaId);
                    }
                    slotToAssignedPax.computeIfAbsent(key, k -> new HashMap<>()).put(resaId, paxAssigned);
                    addedFromSplit = true;
                }
            }

            if (!addedFromSplit && assignments != null) {
                Vehicule v = assignments.get(resaId);
                if (v != null) {
                    String key = v.getIdVehicule() + "|" + depStr + "|" + arrStr;
                    Set<String> resaIds = slotToResaIds.computeIfAbsent(key, k -> new HashSet<>());
                    if (!resaIds.contains(resaId)) {
                        slotToResa.computeIfAbsent(key, k -> new ArrayList<>()).add(r);
                        resaIds.add(resaId);
                    }
                    slotToAssignedPax.computeIfAbsent(key, k -> new HashMap<>())
                            .put(resaId, r.getNbrPassager() != null ? r.getNbrPassager() : 0);
                }
            }
        }
    }
%>
<div class="container-fluid">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">ETU 3131 -  3147 -   3163</h3>
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
                                    List<Reservation> listForDisplay = new ArrayList<>();
                                    Map<String, Reservation> uniqueResas = new LinkedHashMap<>();

                                    // Priorite a la repartition reelle par vehicule (split inclus).
                                    if (assignedResas != null) {
                                        for (Reservation rr : assignedResas) {
                                            if (rr != null && rr.getIdReservation() != null) {
                                                uniqueResas.put(rr.getIdReservation(), rr);
                                            }
                                        }
                                    }

                                    // Complete avec l'ordre de tournee si disponible, sans ecraser les splits.
                                    if (tourOrders != null && tourOrders.containsKey(key)) {
                                        for (Reservation rr : tourOrders.get(key)) {
                                            if (rr != null && rr.getIdReservation() != null && !uniqueResas.containsKey(rr.getIdReservation())) {
                                                uniqueResas.put(rr.getIdReservation(), rr);
                                            }
                                        }
                                    }

                                    listForDisplay.addAll(uniqueResas.values());
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
                                        if (listForDisplay != null && !listForDisplay.isEmpty()) {
                                            for (Reservation currentResa : listForDisplay) {
                                                Hotel currentHotel = hotelMap != null ? hotelMap.get(currentResa.getIdHotel()) : null;
                                                String hotelName = currentHotel != null ? currentHotel.getNomHotel() : currentResa.getIdHotel();
                                                int paxAssigned = currentResa.getNbrPassager() != null ? currentResa.getNbrPassager() : 0;
                                                if (slotToAssignedPax.containsKey(key) && slotToAssignedPax.get(key).containsKey(currentResa.getIdReservation())) {
                                                    paxAssigned = slotToAssignedPax.get(key).get(currentResa.getIdReservation());
                                                }
                                    %>
                                    <li><strong><%= currentResa.getIdReservation() %></strong> - <%= hotelName %> (<%= paxAssigned %> pax)</li>
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
