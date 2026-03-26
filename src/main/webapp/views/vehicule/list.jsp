<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<jsp:include page="../layout/header.jsp" />

<%
    List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
    Map<String, String> disponibilites = (Map<String, String>) request.getAttribute("disponibilites");
%>

<div class="container-fluid">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Gestion des Véhicules</h3>
        </div>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/form" class="btn btn-primary">
            + Ajouter
        </a>
    </div>

    <div class="row mb-4">
        <div class="col-md-3">
            <div class="card">
                <div class="card-body">
                    <div class="stats-title">Total Véhicules</div>
                    <div class="stats-value"><%= vehicules != null ? vehicules.size() : 0 %></div>
                </div>
            </div>
        </div>
    </div>

    <div class="card mb-4">
        <div class="card-header">
            <h5 class="mb-0">Modifier la disponibilite d'un groupe</h5>
        </div>
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/BackOf-taxi/vehicule/disponibilite/update-group" method="post" class="row g-3 align-items-end">
                <div class="col-md-5">
                    <label for="vehiculeIds" class="form-label">IDs vehicules</label>
                    <input type="text" id="vehiculeIds" name="vehiculeIds" class="form-control" placeholder="Ex: VH001, VH002, VH003" required>
                    <small class="text-muted">Separateur accepte: virgule, point-virgule ou espace.</small>
                </div>
                <div class="col-md-3">
                    <label for="heureDebutInput" class="form-label">Disponible chaque jour de</label>
                    <input type="time" id="heureDebutInput" name="heureDebutInput" class="form-control" required>
                </div>
                <div class="col-md-2">
                    <label for="heureFinInput" class="form-label">a</label>
                    <input type="time" id="heureFinInput" name="heureFinInput" class="form-control" value="23:59" required>
                </div>
                <div class="col-md-2 d-flex align-items-end">
                    <button type="submit" class="btn btn-primary w-100">Appliquer</button>
                </div>
            </form>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <h5 class="mb-0">Liste des Véhicules</h5>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table align-middle mb-0">
                    <thead>
                        <tr>
                            <th class="ps-4">ID</th>
                            <th>Référence</th>
                            <th>Places</th>
                            <th>Carburant</th>
                            <th>Plage dispo quotidienne</th>
                            <th class="text-end pe-4">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            if (vehicules != null && !vehicules.isEmpty()) {
                                for (Vehicule v : vehicules) {
                                    String libelleCarburant = v.getIdTypeCarburant();
                                    if (types != null) {
                                        for (TypeCarburant t : types) {
                                            if (t.getIdTypeCarburant().equals(v.getIdTypeCarburant())) {
                                                libelleCarburant = t.getLibelle();
                                                break;
                                            }
                                        }
                                    }
                                    String plage = disponibilites != null ? disponibilites.get(v.getIdVehicule()) : null;
                        %>
                        <tr class="border-bottom">
                            <td class="ps-4 fw-bold">#<%= v.getIdVehicule() %></td>
                            <td><%= v.getReference() %></td>
                            <td><%= v.getNbrPlace() %> places</td>
                            <td>
                                <span class="badge-system"><%= libelleCarburant %></span>
                            </td>
                            <td><%= plage != null ? plage : "00:00 - 23:59" %></td>
                            <td class="text-end pe-4">
                                <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/edit?id=<%= v.getIdVehicule() %>" class="btn btn-sm border me-1">Edit</a>
                                <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/delete?id=<%= v.getIdVehicule() %>" 
                                   class="btn btn-sm btn-dark" 
                                   onclick="return confirm('Supprimer ce véhicule ?')">Del</a>
                            </td>
                        </tr>
                        <% 
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="6" class="text-center py-5">
                                <p class="text-muted mb-0">Aucun véhicule enregistré.</p>
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
