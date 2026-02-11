<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<% request.setAttribute("pageTitle", "Gestion des Véhicules"); %>
<jsp:include page="../layout/header.jsp" />

<div class="container-fluid">
    <% if (request.getAttribute("successMessage") != null) { %>
        <div class="d-flex align-items-center mb-3 text-success">
            <i class="fas fa-check-circle me-2"></i>
            <span class="fw-medium"><%= request.getAttribute("successMessage") %></span>
            <span class="ms-3 text-muted small">(Redirection dans <span id="countdown">7</span>s)</span>
        </div>
        <script>
            let seconds = 7;
            const countdownEl = document.getElementById('countdown');
            const timer = setInterval(() => {
                seconds--;
                if(countdownEl) countdownEl.innerText = seconds;
                if (seconds <= 0) {
                    clearInterval(timer);
                    window.location.href = '${pageContext.request.contextPath}/vehicule/list';
                }
            }, 1000);
        </script>
    <% } %>

    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="d-flex align-items-center mb-3 text-danger">
            <i class="fas fa-exclamation-circle me-2"></i>
            <span class="fw-medium"><%= request.getAttribute("errorMessage") %></span>
        </div>
    <% } %>

    <div class="row mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card h-100">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Types Carburant</div>
                            <div class="stats-value"><%= request.getAttribute("types") != null ? ((List)request.getAttribute("types")).size() : 0 %></div>
                            <div class="mt-2">
                                <span class="badge bg-indigo-soft text-primary">
                                    <i class="fas fa-filter me-1"></i>Catégories
                                </span>
                            </div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-info">
                            <i class="fas fa-gas-pump fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-12">
            <div class="card mb-4">
                <div class="card-header d-flex align-items-center justify-content-between">
                    <h5 class="m-0">
                        <i class="fas fa-car-side me-2 text-primary"></i>Nouveau véhicule
                    </h5>
                    <span class="badge bg-primary-soft text-primary">Enregistrement</span>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/vehicule/save" method="post">
                        <div class="row g-4 mb-4">
                            <div class="col-md-4">
                                <label for="reference" class="form-label">Référence</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-tag"></i></span>
                                    <input type="text" id="reference" name="reference" class="form-control" placeholder="Ex: VEH-001" required>
                                </div>
                                <div class="form-text mt-2">Identifiant unique du véhicule.</div>
                            </div>
                            <div class="col-md-4">
                                <label for="nbrPlace" class="form-label">Capacité</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-users"></i></span>
                                    <input type="number" id="nbrPlace" name="nbrPlace" class="form-control" min="1" placeholder="Nombre de places" required>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <label for="idTypeCarburant" class="form-label">Énergie</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-charging-station"></i></span>
                                    <select id="idTypeCarburant" name="idTypeCarburant" class="form-select" required>
                                        <option value="">-- Sélectionner --</option>
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
                        </div>

                        <div class="d-flex justify-content-between align-items-center border-top pt-4 mt-2">
                            <a href="${pageContext.request.contextPath}/vehicule/list" class="btn btn-link text-secondary text-decoration-none">
                                <i class="fas fa-arrow-left me-2"></i>Retour à la liste
                            </a>
                            <button type="submit" class="btn btn-primary px-5">
                                <i class="fas fa-plus-circle me-2"></i>Ajouter le véhicule
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
