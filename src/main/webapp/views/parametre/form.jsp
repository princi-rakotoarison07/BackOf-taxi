<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="../layout/header.jsp" />

<div class="container-fluid">
    <div class="mb-4">
        <h3 class="fw-bold mb-0">Configuration Système</h3>
    </div>

    <div class="row">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0">Paramètres de calcul</h5>
                </div>
                <div class="card-body p-4">
                    <% if (request.getAttribute("error") != null) { %>
                        <div class="bg-dark text-white p-3 mb-4">ERROR: <%= request.getAttribute("error") %></div>
                    <% } %>
                    
                    <% if (request.getAttribute("success") != null) { %>
                        <div class="status-highlight mb-4">SUCCESS: <%= request.getAttribute("success") %></div>
                    <% } %>

                    <form action="${pageContext.request.contextPath}/parametre/insert" method="post">
                        <div class="row g-4">
                            <div class="col-md-6">
                                <label for="vitesseMoyenne" class="form-label">Vitesse Moyenne (km/h)</label>
                                <input type="number" class="form-control" id="vitesseMoyenne" name="vitesseMoyenne"
                                       step="0.01" min="0.01" required value="${parametre.vitesseMoyenne}">
                            </div>
                            
                            <div class="col-md-6">
                                <label for="tempsAttente" class="form-label">Temps d'Attente (min)</label>
                                <input type="number" class="form-control" id="tempsAttente" name="tempsAttente"
                                       min="1" required value="${parametre.tempsAttente}">
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-3 border-top pt-4 mt-4">
                            <button type="reset" class="btn border">Reset</button>
                            <button type="submit" class="btn btn-primary">Enregistrer</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
