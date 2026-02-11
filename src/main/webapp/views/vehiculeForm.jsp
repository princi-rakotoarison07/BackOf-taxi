<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<% request.setAttribute("pageTitle", "Gestion des Véhicules"); %>
<jsp:include page="layout/header.jsp" />

<div class="container-fluid">
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stats-card h-100 py-2 border-left-info" style="border-left-color: #36b9cc;">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="stats-title" style="color: #36b9cc;">Types Carburant</div>
                            <div class="stats-value"><%= request.getAttribute("types") != null ? ((List)request.getAttribute("types")).size() : 0 %></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-gas-pump fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row justify-content-center">
        <div class="col-lg-8">
            <div class="card mb-4 shadow">
                <div class="card-header d-flex align-items-center justify-content-between bg-white py-3">
                    <h5 class="m-0 font-weight-bold text-primary">
                        <i class="fas fa-car-side me-2"></i>Enregistrer un nouveau véhicule
                    </h5>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/vehicule/save" method="post">
                        <div class="row mb-4">
                            <div class="col-md-6 mb-3">
                                <label for="reference" class="form-label">Référence du véhicule</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light border-end-0"><i class="fas fa-tag text-secondary"></i></span>
                                    <input type="text" id="reference" name="reference" class="form-control border-start-0" placeholder="Ex: VEH-001" required>
                                </div>
                                <div class="form-text small">Utilisez un format unique (ex: Plaque ou ID interne).</div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label for="nbrPlace" class="form-label">Capacité (Places)</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light border-end-0"><i class="fas fa-users text-secondary"></i></span>
                                    <input type="number" id="nbrPlace" name="nbrPlace" class="form-control border-start-0" min="1" placeholder="Ex: 4" required>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="idTypeCarburant" class="form-label">Motorisation / Énergie</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light border-end-0"><i class="fas fa-charging-station text-secondary"></i></span>
                                <select id="idTypeCarburant" name="idTypeCarburant" class="form-select border-start-0" required>
                                    <option value="">-- Sélectionner le type d'énergie --</option>
                                    <% 
                                        List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
                                        if (types != null) {
                                            for (TypeCarburant t : types) {
                                    %>
                                        <option value="<%= t.getIdTypeCarburant() %>"><%= t.getLibelle() %> (<%= t.getCode() %>)</option>
                                    <% 
                                            }
                                        } 
                                    %>
                                </select>
                            </div>
                        </div>

                        <div class="d-grid gap-2 d-md-flex justify-content-md-end border-top pt-4 mt-2">
                            <a href="${pageContext.request.contextPath}/reservation/form" class="btn btn-outline-secondary px-4 me-md-2">
                                <i class="fas fa-arrow-left me-2"></i>Retour
                            </a>
                            <button type="submit" class="btn btn-primary px-5">
                                <i class="fas fa-check-circle me-2"></i>Valider l'ajout
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="layout/footer.jsp" />
