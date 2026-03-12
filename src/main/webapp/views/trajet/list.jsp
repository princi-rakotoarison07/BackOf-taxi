<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Trajet" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.Hotel" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<jsp:include page="../layout/header.jsp" />
<%
    List<Trajet>                    trajets      = (List<Trajet>) request.getAttribute("trajets");
    Map<String, Hotel>              hotelMap     = (Map<String, Hotel>) request.getAttribute("hotelMap");
    Map<String, TypeCarburant>      typeById     = (Map<String, TypeCarburant>) request.getAttribute("typeById");
    String                          selectedDate = (String) request.getAttribute("selectedDate");

    java.text.SimpleDateFormat dfFull = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
    java.text.SimpleDateFormat dfTime = new java.text.SimpleDateFormat("HH:mm");
%>

<div class="container-fluid">

    <!-- ── Titre ─────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Liste des Trajets</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/reservation/form" class="text-decoration-none">Accueil</a></li>
                    <li class="breadcrumb-item active">Trajets</li>
                </ol>
            </nav>
        </div>
    </div>

    <!-- ── Filtre par date ───────────────────────────────── -->
    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <form method="get" action="${pageContext.request.contextPath}/trajet/list" class="row g-3">
                <div class="col-md-4">
                    <label for="date" class="form-label">Filtrer par date</label>
                    <input type="date" class="form-control" id="date" name="date"
                           value="<%= selectedDate != null ? selectedDate : "" %>">
                </div>
                <div class="col-md-3 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary me-2">
                        <i class="fas fa-filter me-1"></i> Filtrer
                    </button>
                    <a href="${pageContext.request.contextPath}/trajet/list" class="btn btn-outline-secondary">
                        <i class="fas fa-times me-1"></i> Tout
                    </a>
                </div>
            </form>
        </div>
    </div>

    <!-- ── Statistiques rapides ─────────────────────────── -->
    <div class="row g-4 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Total Trajets</div>
                            <div class="stats-value"><%= trajets != null ? trajets.size() : 0 %></div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-primary">
                            <i class="fas fa-route fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Total Passagers</div>
                            <div class="stats-value">
                                <%
                                    int totalPassagers = 0;
                                    if (trajets != null) {
                                        for (Trajet t : trajets) {
                                            if (t.getReservations() != null) {
                                                for (Reservation r : t.getReservations()) {
                                                    totalPassagers += r.getNbrPassager() != null ? r.getNbrPassager() : 0;
                                                }
                                            }
                                        }
                                    }
                                %><%= totalPassagers %>
                            </div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-success">
                            <i class="fas fa-users fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Km total</div>
                            <div class="stats-value">
                                <%
                                    java.math.BigDecimal totalKm = java.math.BigDecimal.ZERO;
                                    if (trajets != null) {
                                        for (Trajet t : trajets) {
                                            if (t.getKmParcouru() != null) totalKm = totalKm.add(t.getKmParcouru());
                                        }
                                    }
                                %><%= totalKm.setScale(1, java.math.RoundingMode.HALF_UP) %>
                            </div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-info">
                            <i class="fas fa-road fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ── Tableau des trajets ───────────────────────────── -->
    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold">
                <i class="fas fa-list-alt me-2 text-primary"></i>Détail des Trajets
                <% if (selectedDate != null && !selectedDate.isEmpty()) { %>
                    <span class="badge bg-primary-soft text-primary ms-2"><%= selectedDate %></span>
                <% } %>
            </h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th class="ps-4">#</th>
                            <th><i class="fas fa-clock me-1"></i> H. Départ</th>
                            <th><i class="fas fa-car me-1"></i> Véhicule</th>
                            <th><i class="fas fa-hotel me-1"></i> Réservations / Hôtels desservis</th>
                            <th><i class="fas fa-road me-1"></i> Km parcouru</th>
                            <th><i class="fas fa-clock me-1"></i> H. Retour</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (trajets != null && !trajets.isEmpty()) {
                                int idx = 1;
                                for (Trajet trajet : trajets) {
                                    Vehicule veh = trajet.getVehicule();
                                    List<Reservation> resas = trajet.getReservations();
                                    String heureDepart = trajet.getHeureDepart() != null ? dfTime.format(trajet.getHeureDepart()) : "-";
                                    String heureRetour = trajet.getHeureRetour() != null ? dfTime.format(trajet.getHeureRetour()) : "-";
                                    String dateDep    = trajet.getHeureDepart() != null ? dfFull.format(trajet.getHeureDepart()) : "-";
                                    String km = trajet.getKmParcouru() != null
                                            ? trajet.getKmParcouru().setScale(2, java.math.RoundingMode.HALF_UP).toString() + " km"
                                            : "-";
                        %>
                        <tr>
                            <!-- # -->
                            <td class="ps-4">
                                <span class="badge bg-indigo-soft text-primary fw-bold"><%= idx++ %></span>
                            </td>

                            <!-- H. Départ -->
                            <td>
                                <div class="fw-bold text-primary"><%= heureDepart %></div>
                                <small class="text-muted"><%= trajet.getHeureDepart() != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(trajet.getHeureDepart()) : "" %></small>
                            </td>

                            <!-- Véhicule -->
                            <td>
                                <% if (veh != null) { %>
                                <div class="d-flex align-items-center">
                                    <div class="icon-circle bg-light me-2">
                                        <i class="fas fa-bus text-secondary"></i>
                                    </div>
                                    <div>
                                        <div class="fw-medium"><%= veh.getReference() %></div>
                                        <small class="text-muted"><%= veh.getIdVehicule() %> &bull; <%= veh.getNbrPlace() %> places</small>
                                    </div>
                                </div>
                                <% } else { %><span class="text-muted">-</span><% } %>
                            </td>

                            <!-- Réservations / Hôtels -->
                            <td>
                                <%
                                    if (resas != null && !resas.isEmpty()) {
                                        for (Reservation r : resas) {
                                            Hotel hotel = hotelMap != null ? hotelMap.get(r.getIdHotel()) : null;
                                            String hotelNom = hotel != null ? hotel.getNomHotel() : r.getIdHotel();
                                %>
                                <div class="mb-1">
                                    <span class="badge bg-success-soft text-success me-1">#<%= r.getIdReservation() %></span>
                                    <span class="text-muted small">
                                        <i class="fas fa-user me-1"></i><%= r.getNbrPassager() %> pax
                                        &rarr; <i class="fas fa-hotel me-1"></i><%= hotelNom %>
                                    </span>
                                </div>
                                <%      }
                                    } else { %><span class="text-muted">-</span><% } %>
                            </td>

                            <!-- Km -->
                            <td>
                                <span class="badge bg-warning-soft text-warning fw-bold"><%= km %></span>
                            </td>

                            <!-- H. Retour -->
                            <td>
                                <div class="fw-bold text-danger"><%= heureRetour %></div>
                            </td>
                        </tr>
                        <%  }
                        } else { %>
                        <tr>
                            <td colspan="6" class="text-center py-5">
                                <div class="text-muted">
                                    <i class="fas fa-route fa-3x mb-3 opacity-25"></i>
                                    <p class="mb-0">Aucun trajet trouvé</p>
                                    <small>Sélectionnez une date ou vérifiez que des réservations et des véhicules sont disponibles.</small>
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
