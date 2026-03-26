<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Assignation" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.Reservation" %>
<%@ page import="com.taxi.model.Hotel" %>
<jsp:include page="../layout/header.jsp" />

<%
    List<Assignation> assignations = (List<Assignation>) request.getAttribute("assignations");
    Map<String, Vehicule> vehiculeMap = (Map<String, Vehicule>) request.getAttribute("vehiculeMap");
    Map<String, Reservation> reservationMap = (Map<String, Reservation>) request.getAttribute("reservationMap");
    Map<String, Hotel> hotelMap = (Map<String, Hotel>) request.getAttribute("hotelMap");
    
    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("dd/MM/yyyy");
    java.text.SimpleDateFormat timeFormat = new java.text.SimpleDateFormat("HH:mm");
%>

<div class="container-fluid">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Liste des Assignations Enregistrées</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation-vehicule" class="text-decoration-none">Assignation</a></li>
                    <li class="breadcrumb-item active">Liste des Assignations</li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3">
            <h5 class="mb-0 fw-bold text-primary">Assignations validées</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th>ID Assignation</th>
                            <th>Date</th>
                            <th>Véhicule</th>
                            <th>Client</th>
                            <th>Passagers</th>
                            <th>Trajet</th>
                            <th>Départ Prévu</th>
                            <th>Arrivée Prévue</th>
                            <th>Numéro Trajet</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (assignations != null && !assignations.isEmpty()) {
                                for (Assignation a : assignations) {
                                    Vehicule v = vehiculeMap.get(a.getIdVehicule());
                                    Reservation r = reservationMap.get(a.getIdReservation());
                                    Hotel h = r != null ? hotelMap.get(r.getIdHotel()) : null;
                        %>
                        <tr>
                            <td><small class="text-muted"><%= a.getIdAssignation() %></small></td>
                            <td><span class="badge bg-light text-dark"><%= a.getDateAssignation() != null ? dateFormat.format(a.getDateAssignation()) : "-" %></span></td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <div class="bg-primary-soft p-2 rounded me-2">
                                        <i class="fas fa-car text-primary"></i>
                                    </div>
                                    <div>
                                        <span class="fw-bold text-primary"><%= a.getIdVehicule() %></span>
                                        <% if (v != null) { %><small class="text-muted d-block" style="font-size: 0.7rem;"><%= v.getReference() %></small><% } %>
                                    </div>
                                </div>
                            </td>
                            <td>
                                <span class="fw-bold"><%= r != null ? r.getIdClient() : "-" %></span>
                                <small class="text-muted d-block" style="font-size: 0.7rem;">#<%= a.getIdReservation() %></small>
                            </td>
                            <td>
                                <span class="badge bg-primary-soft text-primary rounded-pill"><%= a.getNbrPassager() %> pers</span>
                            </td>
                            <td>
                                <div class="d-flex align-items-center small">
                                    <span class="text-muted">Aéroport</span>
                                    <i class="fas fa-long-arrow-alt-right mx-2 text-primary opacity-50"></i>
                                    <span class="fw-bold"><%= h != null ? h.getNomHotel() : "-" %></span>
                                </div>
                            </td>
                            <td><span class="fw-bold text-primary"><%= a.getHeureDepartPrevue() != null ? timeFormat.format(a.getHeureDepartPrevue()) : "-" %></span></td>
                            <td><span class="fw-bold text-success"><%= a.getHeureArriveePrevue() != null ? timeFormat.format(a.getHeureArriveePrevue()) : "-" %></span></td>
                            <td class="text-center">
                                <span class="badge bg-info-soft text-info">Trajet #<%= a.getNumTrajet() %></span>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="9" class="text-center py-5">
                                <div class="text-muted">
                                    <i class="fas fa-clipboard-list fa-3x mb-3 opacity-25"></i>
                                    <p class="mb-0">Aucune assignation n'a été enregistrée pour le moment.</p>
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
