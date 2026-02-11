<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Nouvelle Réservation"); %>
<jsp:include page="layout/header.jsp" />

<div class="container-fluid">
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stats-card h-100 py-2">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="stats-title">Total Hôtels</div>
                            <div class="stats-value" id="hotelCount">...</div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-hotel fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stats-card h-100 py-2 border-left-success" style="border-left-color: #1cc88a;">
                <div class="card-body">
                    <div class="row no-gutters align-items-center">
                        <div class="col mr-2">
                            <div class="stats-title" style="color: #1cc88a;">Statut Système</div>
                            <div class="stats-value text-success" style="font-size: 0.9rem;">Opérationnel <i class="fas fa-check-circle ms-1"></i></div>
                        </div>
                        <div class="col-auto">
                            <i class="fas fa-server fa-2x text-gray-300"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-lg-8">
            <div class="card mb-4">
                <div class="card-header d-flex align-items-center justify-content-between">
                    <h5 class="m-0"><i class="fas fa-plus-circle me-2"></i>Saisir une réservation</h5>
                    <span class="badge bg-primary rounded-pill">Nouveau</span>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}/reservation/save" method="POST">
                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Référence Réservation</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light border-end-0"><i class="fas fa-hashtag text-secondary"></i></span>
                                    <input type="text" name="idReservation" class="form-control border-start-0" placeholder="Ex: RES0001" required>
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Identifiant Client</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light border-end-0"><i class="fas fa-user text-secondary"></i></span>
                                    <input type="text" name="idClient" class="form-control border-start-0" placeholder="Ex: CLT042" required>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Nombre de passagers</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light border-end-0"><i class="fas fa-users text-secondary"></i></span>
                                    <input type="number" name="nbrPassager" class="form-control border-start-0" min="1" placeholder="Min: 1" required>
                                </div>
                            </div>
                            <div class="col-md-6 mb-3">
                                <label class="form-label">Établissement Hôtelier</label>
                                <div class="input-group">
                                    <span class="input-group-text bg-light border-end-0"><i class="fas fa-building text-secondary"></i></span>
                                    <select name="idHotel" id="hotelSelect" class="form-select border-start-0" required>
                                        <option value="">Chargement des hôtels...</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label class="form-label">Date et Heure de réservation</label>
                            <div class="input-group">
                                <span class="input-group-text bg-light border-end-0"><i class="fas fa-clock text-secondary"></i></span>
                                <input type="datetime-local" name="dateResa" class="form-control border-start-0">
                            </div>
                            <div class="form-text mt-2"><i class="fas fa-info-circle me-1"></i>Laissez vide pour utiliser la date actuelle.</div>
                        </div>

                        <div class="d-grid gap-2 d-md-flex justify-content-md-end border-top pt-4">
                            <button type="reset" class="btn btn-light px-4 me-md-2">Réinitialiser</button>
                            <button type="submit" class="btn btn-primary px-5">
                                <i class="fas fa-save me-2"></i>Confirmer la réservation
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-4">
            <div class="card mb-4">
                <div class="card-header">
                    <h5 class="m-0"><i class="fas fa-lightbulb me-2"></i>Aide & Conseils</h5>
                </div>
                <div class="card-body">
                    <ul class="list-group list-group-flush small">
                        <li class="list-group-item d-flex align-items-start px-0">
                            <i class="fas fa-check-circle text-success mt-1 me-2"></i>
                            <div>Vérifiez toujours le nombre de places disponibles avant de confirmer.</div>
                        </li>
                        <li class="list-group-item d-flex align-items-start px-0">
                            <i class="fas fa-check-circle text-success mt-1 me-2"></i>
                            <div>Les hôtels sont chargés dynamiquement depuis notre base de données.</div>
                        </li>
                        <li class="list-group-item d-flex align-items-start px-0">
                            <i class="fas fa-check-circle text-success mt-1 me-2"></i>
                            <div>Vous pouvez consulter la liste complète via le menu API.</div>
                        </li>
                    </ul>
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

<jsp:include page="layout/footer.jsp" />
