<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.taxi.model.Vehicule" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<jsp:include page="../layout/header.jsp" />

<%
    List<Vehicule> vehicules = (List<Vehicule>) request.getAttribute("vehicules");
    List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
%>

<div class="container-fluid">
    <!-- Header de la page -->
    <div class="d-flex align-items-center justify-content-between mb-4">
        <div>
            <h3 class="fw-bold mb-0">Gestion des Véhicules</h3>
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item"><a href="#" class="text-decoration-none">inDrive</a></li>
                    <li class="breadcrumb-item active">Véhicules</li>
                </ol>
            </nav>
        </div>
        <a href="${pageContext.request.contextPath}/vehicule/form" class="btn btn-primary d-flex align-items-center">
            <i class="fas fa-plus me-2"></i> Ajouter un véhicule
        </a>
    </div>

    <!-- Filtres et Statistiques rapides -->
    <div class="row g-4 mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Total Véhicules</div>
                            <div class="stats-value"><%= ((List)request.getAttribute("vehicules")).size() %></div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-primary">
                            <i class="fas fa-car fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- Table des véhicules -->
    <div class="card shadow-sm border-0">
        <div class="card-header bg-white py-3 d-flex align-items-center justify-content-between">
            <h5 class="mb-0 fw-bold">Liste des Véhicules</h5>
            <div class="input-group style="width: 250px;">
                <span class="input-group-text bg-light border-0"><i class="fas fa-search text-muted"></i></span>
                <input type="text" class="form-control bg-light border-0" placeholder="Rechercher...">
            </div>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="bg-light text-muted">
                        <tr>
                            <th class="ps-4">ID</th>
                            <th>Référence</th>
                            <th>Places</th>
                            <th>Carburant</th>
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
                        %>
                        <tr>
                            <td class="ps-4">
                                <span class="fw-bold text-primary">#<%= v.getIdVehicule() %></span>
                            </td>
                            <td>
                                <div class="d-flex align-items-center">
                                    <div class="icon-circle bg-light me-2">
                                        <i class="fas fa-hashtag text-secondary"></i>
                                    </div>
                                    <span class="fw-medium"><%= v.getReference() %></span>
                                </div>
                            </td>
                            <td><%= v.getNbrPlace() %> places</td>
                            <td>
                                <span class="badge bg-indigo-soft text-primary">
                                    <i class="fas fa-gas-pump me-1"></i><%= libelleCarburant %>
                                </span>
                            </td>
                            <td class="text-end pe-4">
                                <div class="btn-group">
                                    <button class="btn btn-sm btn-light-primary me-2" title="Modifier">
                                        <i class="fas fa-edit"></i>
                                    </button>
                                    <button class="btn btn-sm btn-light-danger" title="Supprimer">
                                        <i class="fas fa-trash"></i>
                                    </button>
                                </div>
                            </td>
                        </tr>
                        <% 
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="5" class="text-center py-5">
                                <div class="text-muted">
                                    <i class="fas fa-car-side fa-3x mb-3 opacity-25"></i>
                                    <p class="mb-0">Aucun véhicule trouvé</p>
                                </div>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="card-footer bg-white py-3">
            <div class="d-flex align-items-center justify-content-between">
                <span class="text-muted small">Affichage de <%= vehicules != null ? vehicules.size() : 0 %> entrées</span>
                <nav>
                    <ul class="pagination pagination-sm mb-0">
                        <li class="page-item disabled"><a class="page-link" href="#">Précédent</a></li>
                        <li class="page-item active"><a class="page-link" href="#">1</a></li>
                        <li class="page-item"><a class="page-link" href="#">Suivant</a></li>
                    </ul>
                </nav>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
