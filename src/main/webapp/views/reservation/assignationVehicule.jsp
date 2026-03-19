<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<%@ page import="com.taxi.model.Hotel" %>
<jsp:include page="../layout/header.jsp" />
<style>
.journey-timeline {
    font-size: 0.9rem;
}
.journey-step {
    border-left: 3px solid #007bff;
    padding-left: 15px;
    position: relative;
    margin-bottom: 20px;
}
.journey-step:before {
    content: '';
    position: absolute;
    left: -6px;
    top: 0;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    background: #007bff;
    border: 2px solid white;
}
.journey-location {
    font-weight: 500;
    color: #333;
}
.journey-arrow {
    font-size: 1.2rem;
    color: #007bff;
    font-weight: bold;
}
.journey-time {
    font-weight: 600;
    color: #007bff;
    text-align: center;
    font-size: 0.95rem;
}
.reservation-detail {
    background: #f8f9fa;
    border-left: 3px solid #28a745;
    margin: 10px 0 10px 20px;
}
.reservation-detail .journey-step {
    border-left: none;
    padding-left: 0;
    margin-bottom: 0;
}
.reservation-detail .journey-step:before {
    display: none;
}
.step-header {
    background: #e9ecef;
    padding: 8px 12px;
    border-radius: 6px;
    margin-bottom: 8px;
    font-weight: 500;
}
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
            <h3 class="fw-bold mb-0">Planning</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/form" class="text-decoration-none">Réservations</a></li>
                    <li class="breadcrumb-item active">Planning</li>
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
                            <th style="width: 50px;"></th>
                            <th>Véhicule</th>
                            <th>Capacité</th>
                            <th>Carburant</th>
                            <th>Départ Aéroport</th>
                            <th>Retour Aéroport</th>
                            <th>Places Disponibles</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            boolean hasAssignments = false;
                            if (!slotToResa.isEmpty()) {
                                hasAssignments = true;
                                int count = 0;
                                for (Map.Entry<String, List<Reservation>> entry : slotToResa.entrySet()) {
                                    String key = entry.getKey();
                                    List<Reservation> assignedResas = entry.getValue();
                                    String[] parts = key.split("\\|");
                                    String vId = parts[0];
                                    String depTime = parts[1];
                                    String arrTime = parts[2];
                                    
                                    Vehicule v = vehiculeMap.get(vId);
                                    TypeCarburant t = v != null ? typeById.get(v.getIdTypeCarburant()) : null;
                                    String collapseId = "collapse-" + (count++);

                                    int totalPax = 0;
                                    for (Reservation r : assignedResas) {
                                        if (r.getNbrPassager() != null) totalPax += r.getNbrPassager();
                                    }
                                    int availableSeats = (v != null && v.getNbrPlace() != null) ? (v.getNbrPlace() - totalPax) : 0;
                        %>
                        <tr class="bg-white">
                            <td class="text-center">
                                <button class="btn btn-sm btn-outline-primary rounded-circle" type="button" 
                                        data-bs-toggle="collapse" data-bs-target="#<%= collapseId %>" 
                                        aria-expanded="false" style="width: 30px; height: 30px; padding: 0;">
                                    <i class="fas fa-plus"></i>
                                </button>
                            </td>
                            <td><span class="fw-bold text-primary"><%= vId %></span></td>
                            <td><%= v != null && v.getNbrPlace() != null ? v.getNbrPlace() : "-" %></td>
                            <td>
                                <span class="badge bg-indigo-soft text-primary">
                                    <i class="fas fa-gas-pump me-1"></i><%= t != null ? t.getLibelle() : "-" %>
                                </span>
                            </td>
                            <td><%= depTime %></td>
                            <td><%= arrTime %></td>
                            <td>
                                <span class="badge <%= availableSeats > 0 ? "bg-success-soft text-success" : "bg-danger-soft text-danger" %>">
                                    <%= availableSeats %> places
                                </span>
                            </td>
                        </tr>
                        <tr class="collapse" id="<%= collapseId %>">
                            <td colspan="7" class="p-0">
                                <div class="bg-light p-3 border-top border-bottom">
                                    <%
                                        // Récupérer l'ordre de cette tournée
                                        String tourKey = vId + "|" + depTime;
                                        List<Reservation> orderedTour = tourOrders != null ? tourOrders.get(tourKey) : assignedResas;
                                        Map<String, java.sql.Timestamp> times = detailedTimes != null ? detailedTimes.get(tourKey) : null;
                                        
                                        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
                                        
                                        if (orderedTour != null && !orderedTour.isEmpty()) {
                                    %>
                                    <div class="tour-summary">
                                        <%
                                            for (int i = 0; i < orderedTour.size(); i++) {
                                                Reservation currentResa = orderedTour.get(i);
                                                Hotel currentHotel = hotelMap != null ? hotelMap.get(currentResa.getIdHotel()) : null;
                                                java.sql.Timestamp departureTime = times != null ? times.get(currentResa.getIdReservation() + "_departure") : null;
                                                java.sql.Timestamp arrivalTime = times != null ? times.get(currentResa.getIdReservation() + "_arrival") : null;
                                                java.sql.Timestamp returnTime = times != null ? times.get("return_arrival") : null;
                                                
                                                // Déterminer le segment de trajet
                                                String fromLocation = "Aéroport";
                                                String toLocation = currentHotel != null ? currentHotel.getNomHotel() : currentResa.getIdHotel();
                                                
                                                if (i > 0) {
                                                    // Pour les réservations suivantes, le départ vient de l'hôtel précédent
                                                    Reservation prevResa = orderedTour.get(i-1);
                                                    Hotel prevHotel = hotelMap != null ? hotelMap.get(prevResa.getIdHotel()) : null;
                                                    fromLocation = prevHotel != null ? prevHotel.getNomHotel() : prevResa.getIdHotel();
                                                }
                                        %>
                                        <div class="reservation-line mb-2 p-2 bg-white rounded shadow-sm w-100">
                                            <div class="d-flex align-items-center w-100">
                                                <span class="fw-bold text-primary" style="width: 80px; text-align: left;"><%= currentResa.getIdClient() %></span>
                                                <span style="width: 250px; text-align: left;"><%= fromLocation %> → <%= toLocation %></span>
                                                <span class="text-muted" style="width: 180px; text-align: left;">Départ: <%= departureTime != null ? timeFormat.format(departureTime) : "-" %></span>
                                                <span class="text-muted" style="width: 180px; text-align: left;">Arrivée: <%= arrivalTime != null ? timeFormat.format(arrivalTime) : "-" %></span>
                                                <span class="text-muted flex-grow-1 me-1">Retour à l'aéroport: <%= returnTime != null ? timeFormat.format(returnTime) : "-" %></span>
                                                <span class="badge bg-primary me-1" style="width: 70px; text-align: center;"><%= currentResa.getNbrPassager() %> pers</span>
                                                <small class="text-muted" style="width: 80px;">#<%= currentResa.getIdReservation() %></small>
                                            </div>
                                        </div>
                                        <% } %>
                                    </div>
                                    <% } else { %>
                                    <!-- Affichage par défaut si pas d'ordre calculé -->
                                    <%
                                        for (Reservation r : assignedResas) {
                                            Hotel h = hotelMap != null ? hotelMap.get(r.getIdHotel()) : null;
                                            java.sql.Timestamp dep = departureTimes != null ? departureTimes.get(r.getIdReservation()) : null;
                                            java.sql.Timestamp arr = arrivalTimes != null ? arrivalTimes.get(r.getIdReservation()) : null;
                                    %>
                                    <div class="mb-3 p-3 bg-white rounded shadow-sm">
                                        <div class="row align-items-center mb-2">
                                            <div class="col-md-3">
                                                <div class="fw-bold text-primary"><%= r.getIdClient() %></div>
                                                <div class="small text-muted">ID: #<%= r.getIdReservation() %></div>
                                            </div>
                                            <div class="col-md-9">
                                                <div class="d-flex justify-content-end">
                                                    <span class="badge bg-primary"><%= r.getNbrPassager() %> pers.</span>
                                                </div>
                                            </div>
                                        </div>
                                        
                                        <div class="journey-timeline">
                                            <div class="journey-step">
                                                <div class="row align-items-center mb-2">
                                                    <div class="col-md-4 text-end">
                                                        <div class="journey-location">Aéroport Ivato</div>
                                                    </div>
                                                    <div class="col-md-2 text-center">
                                                        <div class="journey-arrow">→</div>
                                                    </div>
                                                    <div class="col-md-4">
                                                        <div class="journey-location"><%= h != null ? h.getNomHotel() : r.getIdHotel() %></div>
                                                    </div>
                                                    <div class="col-md-2">
                                                        <div class="journey-time"><%= dep != null ? timeFormat.format(dep) : "-" %></div>
                                                    </div>
                                                </div>
                                            </div>
                                            
                                            <div class="journey-step mt-3">
                                                <div class="row align-items-center mb-2">
                                                    <div class="col-md-4 text-end">
                                                        <div class="journey-location"><%= h != null ? h.getNomHotel() : r.getIdHotel() %></div>
                                                    </div>
                                                    <div class="col-md-2 text-center">
                                                        <div class="journey-arrow">→</div>
                                                    </div>
                                                    <div class="col-md-4">
                                                        <div class="journey-location">Aéroport Ivato</div>
                                                    </div>
                                                    <div class="col-md-2">
                                                        <div class="journey-time"><%= arr != null ? timeFormat.format(arr) : "-" %></div>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                    <% } %>
                                    <% } %>
                                </div>
                            </td>
                        </tr>
                        <%
                                }
                            }
                            if (!hasAssignments) {
                        %>
                        <tr>
                            <td colspan="7" class="text-center py-5">
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
