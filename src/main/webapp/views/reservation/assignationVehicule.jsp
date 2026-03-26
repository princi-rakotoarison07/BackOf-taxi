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

                                    String depRaw = "";
                                    String arrRaw = "";
                                    String durationTotal = "-";
                                    if (assignedResas != null && !assignedResas.isEmpty() && departureTimes != null && arrivalTimes != null) {
                                        String firstId = assignedResas.get(0).getIdReservation();
                                        java.sql.Timestamp depTs = departureTimes.get(firstId);
                                        java.sql.Timestamp arrTs = arrivalTimes.get(firstId);
                                        depRaw = depTs != null ? new java.sql.Timestamp(depTs.getTime()).toString().substring(0, 19) : "";
                                        arrRaw = arrTs != null ? new java.sql.Timestamp(arrTs.getTime()).toString().substring(0, 19) : "";
                                        if (depTs != null && arrTs != null) {
                                            durationTotal = String.valueOf((arrTs.getTime() - depTs.getTime()) / 60000L);
                                        }
                                    }
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
                                <div class="text-muted small">Durée: <%= durationTotal %> min</div>

                                <button class="btn btn-sm btn-primary rounded-circle ms-2 btn-trajet"
                                        title="Enregistrer le trajet"
                                        data-vehicule="<%= vId %>"
                                        data-date="<%= selectedDate %>"
                                        data-dep="<%= depRaw %>"
                                        data-arr="<%= arrRaw %>">
                                    <i class="fas fa-save"></i>
                                </button>
                            </td>
                        </tr>
                        <tr class="collapse" id="<%= collapseId %>">
                            <td colspan="7" class="p-0">
                                <div class="bg-light p-3 border-top border-bottom">
                                    <%
                                        // Récupérer l'ordre de cette tournée
                                        String tourKey = key; // Utiliser la clé complète (vId|depTime|arrTime)
                                        List<Reservation> orderedTour = (tourOrders != null && tourOrders.containsKey(tourKey)) ? tourOrders.get(tourKey) : assignedResas;
                                        Map<String, java.sql.Timestamp> times = (detailedTimes != null) ? detailedTimes.get(tourKey) : null;
                                        
                                        java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
                                        
                                        if (orderedTour != null && !orderedTour.isEmpty()) {
                                            // L'heure de départ prévue à l'aéroport pour toute la tournée
                                            String airportPrevu = depTime.contains(" ") ? depTime.split(" ")[1] : depTime;
                                    %>
                                    <div class="tour-details">
                                        <%
                                            for (int i = 0; i < orderedTour.size(); i++) {
                                                Reservation currentResa = orderedTour.get(i);
                                                Hotel currentHotel = hotelMap != null ? hotelMap.get(currentResa.getIdHotel()) : null;
                                                String hotelName = currentHotel != null ? currentHotel.getNomHotel() : currentResa.getIdHotel();

                                                // Déterminer le trajet
                                                String fromLoc = "Aéroport";
                                                if (i > 0) {
                                                    Reservation prevResa = orderedTour.get(i-1);
                                                    Hotel prevHotel = hotelMap != null ? hotelMap.get(prevResa.getIdHotel()) : null;
                                                    fromLoc = prevHotel != null ? prevHotel.getNomHotel() : prevResa.getIdHotel();
                                                }
                                                String toLoc = hotelName;

                                                // Récupérer les horaires
                                                String legDep = "-";
                                                String legArr = "-";
                                                
                                                if (times != null) {
                                                    java.sql.Timestamp tDep = times.get(currentResa.getIdReservation() + "_departure");
                                                    java.sql.Timestamp tArr = times.get(currentResa.getIdReservation() + "_arrival");
                                                    if (tDep != null) legDep = timeFormat.format(tDep);
                                                    if (tArr != null) legArr = timeFormat.format(tArr);
                                                }
                                        %>
                                        <div class="d-flex align-items-center py-2 px-3 border-bottom bg-white mb-1 rounded shadow-sm small">
                                            <div class="fw-bold text-primary" style="width: 70px;"><%= currentResa.getIdClient() %></div>
                                            <div class="flex-grow-1" style="min-width: 200px;">
                                                <span class="text-muted"><%= fromLoc %></span>
                                                <i class="fas fa-long-arrow-alt-right mx-2 text-primary opacity-50"></i>
                                                <span class="fw-bold"><%= toLoc %></span>
                                            </div>
                                            <div class="px-2 border-start text-center" style="width: 160px;">
                                                <small class="text-muted">Prévu Aéroport</small>
                                                <div class="fw-bold"><%= airportPrevu %></div>
                                            </div>
                                            <div class="px-2 border-start text-center" style="width: 120px;">
                                                <small class="text-muted">Départ</small>
                                                <div class="fw-bold text-primary"><%= legDep %></div>
                                            </div>
                                            <div class="px-2 border-start text-center" style="width: 120px;">
                                                <small class="text-muted">Arrivée</small>
                                                <div class="fw-bold text-success"><%= legArr %></div>
                                            </div>
                                            <div class="px-2 border-start text-end" style="width: 160px;">
                                                <span class="badge bg-primary rounded-pill me-2"><%= currentResa.getNbrPassager() %> pers</span>
                                                <button class="btn btn-sm btn-link text-muted p-0 ms-2 btn-assign" 
                                                        title="Enregistrer l'assignation"
                                                        data-vehicule="<%= vId %>"
                                                        data-reservation="<%= currentResa.getIdReservation() %>"
                                                        data-pax="<%= currentResa.getNbrPassager() %>"
                                                        data-date="<%= selectedDate %>"
                                                        data-dep="<%= times != null && times.get(currentResa.getIdReservation() + "_departure") != null ? new java.sql.Timestamp(times.get(currentResa.getIdReservation() + "_departure").getTime()).toString().substring(0, 19) : "" %>"
                                                        data-arr="<%= times != null && times.get(currentResa.getIdReservation() + "_arrival") != null ? new java.sql.Timestamp(times.get(currentResa.getIdReservation() + "_arrival").getTime()).toString().substring(0, 19) : "" %>"
                                                        data-num="<%= i + 1 %>">
                                                    <i class="fas fa-save opacity-50"></i>
                                                </button>
                                            </div>
                                        </div>
                                        <% } %>
                                        
                                        <%
                                            // Retour final à l'aéroport
                                            String finalReturn = "-";
                                            String lastLoc = "Aéroport";
                                            if (!orderedTour.isEmpty()) {
                                                Reservation lastResa = orderedTour.get(orderedTour.size() - 1);
                                                Hotel h = hotelMap.get(lastResa.getIdHotel());
                                                lastLoc = h != null ? h.getNomHotel() : lastResa.getIdHotel();
                                            }

                                            if (times != null && times.containsKey("return_arrival")) {
                                                finalReturn = timeFormat.format(times.get("return_arrival"));
                                            } else {
                                                finalReturn = arrTime.contains(" ") ? arrTime.split(" ")[1] : arrTime;
                                            }
                                        %>
                                        <div class="mt-2 text-end pe-3">
                                            <span class="text-muted small">Retour à l'aéroport : </span>
                                            <span class="fw-bold text-danger"><%= finalReturn %></span>
                                            <span class="text-muted small ms-1">(<%= lastLoc %> → Aéroport)</span>
                                        </div>
                                    </div>
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

        // Handle Assignment Saving
        const assignButtons = document.querySelectorAll('.btn-assign');
        assignButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const data = this.dataset;
                const formData = new URLSearchParams();
                formData.append('idVehicule', data.vehicule);
                formData.append('idReservation', data.reservation);
                formData.append('nbrPassager', data.pax);
                
                // Utiliser la date du trajet si selectedDate est vide
                const assignDate = data.date && data.date !== 'null' ? data.date + ' 00:00:00' : data.dep;
                formData.append('dateAssignation', assignDate);
                
                formData.append('heureDepartPrevue', data.dep);
                formData.append('heureArriveePrevue', data.arr);
                formData.append('numTrajet', data.num);

                const originalHtml = this.innerHTML;
                this.disabled = true;
                this.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

                fetch('${pageContext.request.contextPath}/BackOf-taxi/reservation/save-assignation', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: formData
                })
                .then(response => response.json())
                .then(res => {
                    if (res.status === 'success') {
                        this.classList.add('text-secondary');
                        this.innerHTML = '<i class="fas fa-check opacity-50"></i>';
                        this.title = 'Déjà enregistré';
                        alert(res.message);
                    } else {
                        alert('Erreur: ' + res.message);
                        this.disabled = false;
                        this.innerHTML = originalHtml;
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert('Erreur réseau');
                    this.disabled = false;
                    this.innerHTML = originalHtml;
                });
            });
        });

        const trajetButtons = document.querySelectorAll('.btn-trajet');
        trajetButtons.forEach(btn => {
            btn.addEventListener('click', function() {
                const data = this.dataset;
                const formData = new URLSearchParams();
                formData.append('idVehicule', data.vehicule);

                const dateTrajet = data.date && data.date !== 'null' ? data.date + ' 00:00:00' : '';
                if (dateTrajet) {
                    formData.append('dateTrajet', dateTrajet);
                }

                const dep = data.dep || '';
                const arr = data.arr || '';

                if (!dep || !arr) {
                    alert("Impossible d'enregistrer: horaires manquants.");
                    return;
                }

                formData.append('heureDepartAeroport', dep);
                formData.append('heureArriveeAeroport', arr);

                const originalHtml = this.innerHTML;
                this.disabled = true;
                this.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';

                fetch('${pageContext.request.contextPath}/BackOf-taxi/reservation/save-trajet', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: formData
                })
                .then(response => response.json())
                .then(res => {
                    if (res.status === 'success') {
                        this.classList.replace('btn-primary', 'btn-secondary');
                        this.innerHTML = '<i class="fas fa-check"></i>';
                        this.title = 'Déjà enregistré';
                        alert(res.message);
                    } else {
                        alert('Erreur: ' + res.message);
                        this.disabled = false;
                        this.innerHTML = originalHtml;
                    }
                })
                .catch(err => {
                    console.error(err);
                    alert('Erreur réseau');
                    this.disabled = false;
                    this.innerHTML = originalHtml;
                });
            });
        });
    });
</script>

<jsp:include page="../layout/footer.jsp" />
