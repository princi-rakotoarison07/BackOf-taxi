<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ include file="../layout/header.jsp" %>

<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="card border-0 shadow-sm">
                <div class="card-header bg-white border-bottom">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0 fw-bold">
                            <i class="fas fa-list text-primary me-2"></i>
                            Liste des Paramètres
                        </h5>
                        <a href="${pageContext.request.contextPath}/parametre/form" class="btn btn-primary btn-sm">
                            <i class="fas fa-plus me-1"></i> Nouveau Paramètre
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

                    <div class="table-responsive">
                        <table class="table table-hover" id="parametresTable">
                            <thead class="table-light">
                                <tr>
                                    <th>ID</th>
                                    <th>Vitesse Moyenne</th>
                                    <th>Temps d'Attente</th>
                                    <th>Date de Création</th>
                                    <th>Actions</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% 
                                java.util.List<com.taxi.model.Parametre> parametres = 
                                    (java.util.List<com.taxi.model.Parametre>) request.getAttribute("parametres");
                                
                                if (parametres != null && !parametres.isEmpty()) {
                                    for (com.taxi.model.Parametre param : parametres) {
                                %>
                                <tr>
                                    <td>
                                        <span class="badge bg-primary">
                                            <%= param.getIdParametre() %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <i class="fas fa-tachometer-alt text-primary me-2"></i>
                                            <span><%= param.getVitesseMoyenne() != null ? param.getVitesseMoyenne().toString() : "0.00" %> km/h</span>
                                        </div>
                                    </td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <i class="fas fa-clock text-success me-2"></i>
                                            <span><%= param.getTempsAttente() %> min</span>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="text-muted small">
                                            <%= param.getIdParametre().startsWith("PAR") ? 
                                                new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(
                                                    new java.util.Date(Long.parseLong(param.getIdParametre().substring(3)))
                                                ) : "N/A" %>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="btn-group" role="group">
                                            <a href="${pageContext.request.contextPath}/parametre/edit?id=<%= param.getIdParametre() %>" 
                                               class="btn btn-sm btn-outline-primary" title="Modifier">
                                                <i class="fas fa-edit"></i>
                                            </a>
                                            <a href="${pageContext.request.contextPath}/parametre/delete?id=<%= param.getIdParametre() %>" 
                                               class="btn btn-sm btn-outline-danger" 
                                               title="Supprimer"
                                               onclick="return confirm('Êtes-vous sûr de vouloir supprimer ce paramètre ?')">
                                                <i class="fas fa-trash"></i>
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                                <% 
                                    }
                                } else {
                                %>
                                <tr>
                                    <td colspan="5" class="text-center py-4">
                                        <div class="text-muted">
                                            <i class="fas fa-inbox fa-2x mb-2"></i>
                                            <p class="mb-0">Aucun paramètre trouvé</p>
                                            <small>Commencez par ajouter un nouveau paramètre</small>
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
    </div>

    <!-- Statistiques -->
    <div class="row mt-4">
        <div class="col-md-6">
            <div class="card border-0 bg-gradient-primary text-white">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="card-title mb-1">Vitesse Moyenne Actuelle</h6>
                            <h3 class="mb-0">
                                <% 
                                if (parametres != null && !parametres.isEmpty()) {
                                    com.taxi.model.Parametre dernierParam = parametres.get(parametres.size() - 1);
                                %>
                                    <%= dernierParam.getVitesseMoyenne() != null ? dernierParam.getVitesseMoyenne().toString() : "0.00" %> km/h
                                <% } else { %>
                                    N/A
                                <% } %>
                            </h3>
                        </div>
                        <div class="icon-circle bg-white bg-opacity-25">
                            <i class="fas fa-tachometer-alt fa-2x"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-md-6">
            <div class="card border-0 bg-gradient-success text-white">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h6 class="card-title mb-1">Temps d'Attente Actuel</h6>
                            <h3 class="mb-0">
                                <% 
                                if (parametres != null && !parametres.isEmpty()) {
                                    com.taxi.model.Parametre dernierParam = parametres.get(parametres.size() - 1);
                                %>
                                    <%= dernierParam.getTempsAttente() %> min
                                <% } else { %>
                                    N/A
                                <% } %>
                            </h3>
                        </div>
                        <div class="icon-circle bg-white bg-opacity-25">
                            <i class="fas fa-clock fa-2x"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', function() {
    // Initialiser le tableau avec DataTable si disponible
    if (typeof $.fn.DataTable !== 'undefined') {
        $('#parametresTable').DataTable({
            language: {
                search: "Rechercher:",
                lengthMenu: "Afficher _MENU_ entrées",
                info: "Affichage de _START_ à _END_ sur _TOTAL_ entrées",
                paginate: {
                    first: "Premier",
                    last: "Dernier",
                    next: "Suivant",
                    previous: "Précédent"
                }
            }
        });
    }
});

function editParametre(id) {
    // Rediriger vers le formulaire en mode édition
    window.location.href = '${pageContext.request.contextPath}/parametre/edit?id=' + id;
}

function deleteParametre(id) {
    if (confirm('Êtes-vous sûr de vouloir supprimer ce paramètre ?')) {
        // Appel à l'API de suppression
        fetch('${pageContext.request.contextPath}/parametre/delete?id=' + id, {
            method: 'DELETE'
        })
        .then(response => {
            if (response.ok) {
                location.reload();
            } else {
                alert('Erreur lors de la suppression');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Erreur lors de la suppression');
        });
    }
}

function showAlert(message, type) {
    const alertDiv = document.createElement('div');
    const iconClass = type === 'danger' ? 'exclamation-triangle' : 'check-circle';
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.innerHTML = `
        <i class="fas fa-${iconClass} me-2"></i>
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    const container = document.querySelector('.container-fluid');
    container.insertBefore(alertDiv, container.firstChild);
    
    // Auto-dismiss après 5 secondes
    setTimeout(() => {
        if (alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 5000);
}
</script>

<%@ include file="../layout/footer.jsp" %>
