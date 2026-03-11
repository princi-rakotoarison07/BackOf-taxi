<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Nouvelle Réservation"); %>
<jsp:include page="../layout/header.jsp" />

<div class="container-fluid">
    <div class="row mb-4 g-4">
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card h-100">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Total Hôtels</div>
                            <div class="stats-value" id="hotelCount">0</div>
                            <div class="mt-2">
                                <span class="badge bg-success-soft text-success">
                                    <i class="fas fa-sync-alt me-1"></i>Temps réel
                                </span>
                            </div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-primary">
                            <i class="fas fa-hotel fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card stats-card h-100">
                <div class="card-body">
                    <div class="d-flex align-items-center justify-content-between">
                        <div>
                            <div class="stats-title">Statut Système</div>
                            <div class="stats-value text-success">Opérationnel</div>
                            <div class="mt-2">
                                <span class="text-muted small">
                                    <i class="fas fa-circle text-success me-1" style="font-size: 0.6rem;"></i> Serveur actif
                                </span>
                            </div>
                        </div>
                        <div class="stats-icon-wrapper bg-gradient-success">
                            <i class="fas fa-server fa-lg"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row g-4">
        <div class="col-12">
            <div class="card mb-4">
                <div class="card-header d-flex align-items-center justify-content-between">
                    <h5 class="m-0"><i class="fas fa-plus-circle me-2 text-primary"></i>Nouvelle réservation</h5>
                    <span class="badge bg-primary-soft text-primary">Formulaire</span>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/reservation/save" method="POST">
                        <div class="row g-4 mb-4">
                            <div class="col-md-3">
                                <label class="form-label">Référence Réservation</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-hashtag"></i></span>
                                    <input type="text" name="idReservation" class="form-control" placeholder="Ex: RES0001" required>
                                </div>
                            </div>
                            <div class="col-md-3">
                                <label class="form-label">Identifiant Client</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-user"></i></span>
                                    <input type="text" name="idClient" class="form-control" placeholder="Ex: CLT042" required>
                                </div>
                            </div>
                            <div class="col-md-2">
                                <label class="form-label">Passagers</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-users"></i></span>
                                    <input type="number" name="nbrPassager" class="form-control" min="1" placeholder="Nombre" required>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label">Établissement Hôtelier</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-building"></i></span>
                                    <select name="idHotel" id="hotelSelect" class="form-select" required>
                                        <option value="">Chargement des hôtels...</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label class="form-label">Date et Heure</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="fas fa-calendar-alt"></i></span>
                                    <input type="datetime-local" name="dateResa" class="form-control">
                                </div>
                                <div class="form-text mt-2"><i class="fas fa-info-circle me-1"></i>Par défaut : date et heure actuelles.</div>
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center border-top pt-4 mt-2">
                            <button type="reset" class="btn btn-link text-secondary text-decoration-none">Réinitialiser</button>
                            <button type="submit" class="btn btn-primary px-5">
                                <i class="fas fa-save me-2"></i>Confirmer la réservation
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-12">
            <div class="row g-4">
                <div class="col-md-6">
                    <div class="card h-100">
                        <div class="card-header">
                            <h5 class="m-0"><i class="fas fa-info-circle me-2 text-primary"></i>Synchronisation</h5>
                        </div>
                        <div class="card-body">
                            <div class="d-flex align-items-start">
                                <div class="icon-circle bg-success-soft text-success me-3" style="width: 40px; height: 40px;">
                                    <i class="fas fa-check" style="font-size: 0.9rem;"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-bold">Disponibilité en temps réel</h6>
                                    <p class="small text-muted mb-0">Les hôtels sont synchronisés en temps réel avec notre base de données pour garantir l'exactitude des réservations.</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="col-md-6">
                    <div class="card h-100">
                        <div class="card-header">
                            <h5 class="m-0"><i class="fas fa-clock me-2 text-primary"></i>Planification</h5>
                        </div>
                        <div class="card-body">
                            <div class="d-flex align-items-start">
                                <div class="icon-circle bg-indigo-soft text-primary me-3" style="width: 40px; height: 40px;">
                                    <i class="fas fa-calendar-plus" style="font-size: 0.9rem;"></i>
                                </div>
                                <div>
                                    <h6 class="mb-1 fw-bold">Réservation anticipée</h6>
                                    <p class="small text-muted mb-0">Vous pouvez planifier vos réservations jusqu'à 30 jours à l'avance pour une meilleure gestion de la flotte.</p>
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
        const hotelSelect = document.getElementById('hotelSelect');
        const hotelCount = document.getElementById('hotelCount');
        
        fetch('${pageContext.request.contextPath}/api/hotels')
            .then(response => response.json())
            .then(hotels => {
                hotelCount.textContent = hotels.length;
                hotelSelect.innerHTML = '<option value="">-- Choisir un hôtel --</option>';
                hotels.forEach(hotel => {
                    const option = document.createElement('option');
                    option.value = hotel.idHotel;
                    option.textContent = hotel.nomHotel;
                    hotelSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Erreur lors du chargement des hôtels:', error);
                hotelSelect.innerHTML = '<option value="">Erreur de chargement</option>';
                hotelCount.textContent = 'Erreur';
            });
    });
</script>

<jsp:include page="../layout/footer.jsp" />
