<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="../layout/header.jsp" %>

<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="card border-0 shadow-sm">
                <div class="card-header bg-white border-bottom">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0 fw-bold">
                            <i class="fas fa-cogs text-primary me-2"></i>
                            Insertion des Paramètres
                        </h5>
                        <a href="${pageContext.request.contextPath}/parametre/list" class="btn btn-outline-secondary btn-sm">
                            <i class="fas fa-list me-1"></i> Liste des Paramètres
                        </a>
                    </div>
                </div>
                <div class="card-body p-4">
                    <%-- Messages d'erreur ou de succès --%>
                    <% if (request.getAttribute("error") != null) { %>
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="fas fa-exclamation-triangle me-2"></i>
                            <%= request.getAttribute("error") %>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    <% } %>
                    
                    <% if (request.getAttribute("success") != null) { %>
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="fas fa-check-circle me-2"></i>
                            <%= request.getAttribute("success") %>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    <% } %>

                    <form action="${pageContext.request.contextPath}/parametre/insert" method="post" id="parametreForm">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label for="vitesseMoyenne" class="form-label">
                                        <i class="fas fa-tachometer-alt text-primary me-1"></i>
                                        Vitesse Moyenne (km/h)
                                    </label>
                                    <div class="input-group">
                                        <input type="number" 
                                               class="form-control" 
                                               id="vitesseMoyenne" 
                                               name="vitesseMoyenne"
                                               step="0.01"
                                               min="0.01"
                                               max="999.99"
                                               required
                                               placeholder="Ex: 60.50"
                                               value="${parametre.vitesseMoyenne}">
                                        <span class="input-group-text">km/h</span>
                                    </div>
                                    <div class="form-text">Vitesse moyenne de déplacement des véhicules</div>
                                </div>
                            </div>
                            
                            <div class="col-md-6">
                                <div class="mb-4">
                                    <label for="tempsAttente" class="form-label">
                                        <i class="fas fa-clock text-primary me-1"></i>
                                        Temps d'Attente (minutes)
                                    </label>
                                    <div class="input-group">
                                        <input type="number" 
                                               class="form-control" 
                                               id="tempsAttente" 
                                               name="tempsAttente"
                                               min="1"
                                               max="999"
                                               required
                                               placeholder="Ex: 15"
                                               value="${parametre.tempsAttente}">
                                        <span class="input-group-text">min</span>
                                    </div>
                                    <div class="form-text">Temps d'attente moyen avant le départ</div>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-12">
                                <div class="d-flex justify-content-between align-items-center mt-4">
                                    <div class="form-text text-muted">
                                        <i class="fas fa-info-circle me-1"></i>
                                        Les champs marqués d'un astérisque (*) sont obligatoires
                                    </div>
                                    <div>
                                        <button type="reset" class="btn btn-outline-secondary me-2">
                                            <i class="fas fa-undo me-1"></i> Réinitialiser
                                        </button>
                                        <button type="submit" class="btn btn-primary">
                                            <i class="fas fa-save me-1"></i> Enregistrer
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <!-- Carte d'information -->
    <div class="row mt-4">
        <div class="col-12">
            <div class="card border-0 bg-light">
                <div class="card-body">
                    <h6 class="card-title fw-bold mb-3">
                        <i class="fas fa-info-circle text-info me-2"></i>
                        Informations sur les Paramètres
                    </h6>
                    <div class="row">
                        <div class="col-md-6">
                            <div class="d-flex align-items-start mb-3">
                                <div class="icon-circle bg-primary-soft me-3">
                                    <i class="fas fa-tachometer-alt text-primary"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-semibold">Vitesse Moyenne</h6>
                                    <p class="text-muted small mb-0">
                                        Définit la vitesse moyenne utilisée pour calculer les temps de trajet. 
                                        Cette valeur influence les estimations de durée des courses.
                                    </p>
                                </div>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="d-flex align-items-start mb-3">
                                <div class="icon-circle bg-success-soft me-3">
                                    <i class="fas fa-clock text-success"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-semibold">Temps d'Attente</h6>
                                    <p class="text-muted small mb-0">
                                        Temps d'attente standard avant le départ du véhicule. 
                                        Inclut le temps de préparation et de prise en charge du client.
                                    </p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('parametreForm');
    
    // Validation côté client
    form.addEventListener('submit', function(e) {
        const vitesseMoyenne = parseFloat(document.getElementById('vitesseMoyenne').value);
        const tempsAttente = parseInt(document.getElementById('tempsAttente').value);
        
        if (vitesseMoyenne <= 0 || vitesseMoyenne > 999.99) {
            e.preventDefault();
            showAlert('La vitesse moyenne doit être comprise entre 0.01 et 999.99 km/h', 'danger');
            return;
        }
        
        if (tempsAttente < 1 || tempsAttente > 999) {
            e.preventDefault();
            showAlert('Le temps d\'attente doit être compris entre 1 et 999 minutes', 'danger');
            return;
        }
    });
    
    // Fonction pour afficher des alertes
    function showAlert(message, type) {
        const alertDiv = document.createElement('div');
        const iconClass = type === 'danger' ? 'exclamation-triangle' : 'check-circle';
        alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
        alertDiv.innerHTML = `
            <i class="fas fa-${iconClass} me-2"></i>
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;
        
        const cardBody = form.closest('.card-body');
        cardBody.insertBefore(alertDiv, cardBody.firstChild);
        
        // Auto-dismiss après 5 secondes
        setTimeout(() => {
            if (alertDiv.parentNode) {
                alertDiv.remove();
            }
        }, 5000);
    }
});
</script>

<%@ include file="../layout/footer.jsp" %>
