<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<jsp:include page="../layout/header.jsp" />

<%
    List<Vehicule> disponibles = (List<Vehicule>) request.getAttribute("disponibles");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
    String selectedDate = (String) request.getAttribute("selectedDate");
    
    Map<String, TypeCarburant> typeById = new HashMap<>();
    if (types != null) {
        for (TypeCarburant t : types) {
            typeById.put(t.getIdTypeCarburant(), t);
        }
    }
%>

<div class="container-fluid">
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Véhicules Disponibles</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/list" class="text-decoration-none">Véhicules</a></li>
                    <li class="breadcrumb-item active">Disponibilité</li>
                </ol>
            </nav>
        </div>
    </div>

    <div class="card shadow-sm border-0 mb-4">
        <div class="card-body">
            <form action="${pageContext.request.contextPath}/BackOf-taxi/vehicule/disponible" method="get" class="row g-3 align-items-end">
                <div class="col-md-3">
                    <label for="date" class="form-label">Date</label>
                    <input type="date" class="form-control" id="date" name="date" value="<%= selectedDate != null ? selectedDate : "" %>" required>
                </div>
                <div class="col-md-3">
                    <label for="time" class="form-label">Heure</label>
                    <input type="time" class="form-control" id="time" name="time" value="<%= request.getAttribute("selectedTime") != null ? request.getAttribute("selectedTime") : "" %>">
                </div>
                <div class="col-md-2">
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="fas fa-search me-2"></i>Afficher
                    </button>
                </div>
            </form>
        </div>
    </div>

    <% if (selectedDate != null && !selectedDate.isEmpty()) { %>
    <div class="card shadow-sm border-0">

        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th class="ps-4">ID</th>
                            <th>Référence</th>
                            <th>Capacité</th>
                            <th>Carburant</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% 
                            if (disponibles != null && !disponibles.isEmpty()) {
                                for (Vehicule v : disponibles) {
                                    TypeCarburant t = typeById.get(v.getIdTypeCarburant());
                        %>
                        <tr>
                            <td class="ps-4">
                                <span class="fw-bold text-primary">#<%= v.getIdVehicule() %></span>
                            </td>
                            <td><%= v.getReference() %></td>
                            <td><%= v.getNbrPlace() %> places</td>
                            <td>
                                <span class="badge bg-indigo-soft text-primary">
                                    <i class="fas fa-gas-pump me-1"></i><%= t != null ? t.getLibelle() : "N/A" %>
                                </span>
                            </td>
                        </tr>
                        <% 
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="4" class="text-center py-5">
                                <div class="text-muted">
                                    <i class="fas fa-car-side fa-3x mb-3 opacity-25"></i>
                                    <p class="mb-0">Aucun véhicule disponible pour cette date</p>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <% } else { %>
        <div class="alert alert-info">
            <i class="fas fa-info-circle me-2"></i>Veuillez sélectionner une date pour voir les véhicules disponibles.
        </div>
    <% } %>
</div>

<jsp:include page="../layout/footer.jsp" />
