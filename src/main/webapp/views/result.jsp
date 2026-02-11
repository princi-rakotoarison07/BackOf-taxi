<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Résultat de l'opération"); %>
<jsp:include page="layout/header.jsp" />

<div class="container-fluid">
    <div class="row justify-content-center">
        <div class="col-lg-6">
            <div class="card shadow-lg border-0 mt-5">
                <div class="card-body p-5 text-center">
                    <% if (request.getAttribute("message") != null) { %>
                        <div class="mb-4">
                            <i class="fas fa-check-circle text-success fa-5x animate__animated animate__bounceIn"></i>
                        </div>
                        <h2 class="text-success font-weight-bold mb-3">Opération réussie !</h2>
                        <div class="alert alert-success border-0 bg-success bg-opacity-10 text-success py-3 px-4 rounded-3 mb-4">
                            <i class="fas fa-info-circle me-2"></i><%= request.getAttribute("message") %>
                        </div>
                    <% } %>

                    <% if (request.getAttribute("error") != null) { %>
                        <div class="mb-4">
                            <i class="fas fa-exclamation-triangle text-danger fa-5x animate__animated animate__shakeX"></i>
                        </div>
                        <h2 class="text-danger font-weight-bold mb-3">Une erreur est survenue</h2>
                        <div class="alert alert-danger border-0 bg-danger bg-opacity-10 text-danger py-3 px-4 rounded-3 mb-4">
                            <i class="fas fa-bug me-2"></i><%= request.getAttribute("error") %>
                        </div>
                    <% } %>

                    <div class="d-grid gap-2 mt-4">
                        <a href="javascript:history.back()" class="btn btn-light border py-3 px-4">
                            <i class="fas fa-arrow-left me-2"></i>Retour à la saisie
                        </a>
                        <a href="${pageContext.request.contextPath}/reservation/form" class="btn btn-primary py-3 px-4">
                            <i class="fas fa-home me-2"></i>Tableau de bord
                        </a>
                    </div>
                    
                    <div class="mt-4 text-muted small">
                        <i class="fas fa-clock me-1"></i> Redirection automatique dans <span id="countdown">10</span> secondes...
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Ajout d'une animation légère -->
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/animate.css/4.1.1/animate.min.css"/>

<script>
    let seconds = 10;
    const countdownElement = document.getElementById('countdown');
    
    const interval = setInterval(() => {
        seconds--;
        countdownElement.textContent = seconds;
        if (seconds <= 0) {
            clearInterval(interval);
            window.location.href = "${pageContext.request.contextPath}/reservation/form";
        }
    }, 1000);
</script>

<jsp:include page="layout/footer.jsp" />
