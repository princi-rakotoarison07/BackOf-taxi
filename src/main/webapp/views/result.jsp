<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<% request.setAttribute("pageTitle", "Résultat de l'opération"); %>
<jsp:include page="layout/header.jsp" />

<div class="container-fluid">
    <div class="row justify-content-center">
        <div class="col-lg-6">
            <div class="card mt-5">
                <div class="card-body p-5 text-center">
                    <% if (request.getAttribute("message") != null) { %>
                        <div class="mb-4">
                            <div class="icon-circle bg-success-soft text-success mx-auto" style="width: 80px; height: 80px;">
                                <i class="fas fa-check fa-3x"></i>
                            </div>
                        </div>
                        <h2 class="fw-bold mb-3">Succès !</h2>
                        <div class="bg-success-soft text-success p-3 rounded-3 mb-4">
                            <%= request.getAttribute("message") %>
                        </div>
                    <% } %>

                    <% if (request.getAttribute("error") != null) { %>
                        <div class="mb-4">
                            <div class="icon-circle bg-danger-soft text-danger mx-auto" style="width: 80px; height: 80px;">
                                <i class="fas fa-exclamation-triangle fa-3x"></i>
                            </div>
                        </div>
                        <h2 class="fw-bold mb-3">Erreur</h2>
                        <div class="bg-danger-soft text-danger p-3 rounded-3 mb-4">
                            <%= request.getAttribute("error") %>
                        </div>
                    <% } %>

                    <div class="d-flex flex-column gap-2 mt-4">
                        <a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/form" class="btn btn-primary py-3">
                            <i class="fas fa-home me-2"></i>Retour à l'accueil
                        </a>
                        <a href="javascript:history.back()" class="btn btn-link text-secondary text-decoration-none">
                            <i class="fas fa-arrow-left me-2"></i>Page précédente
                        </a>
                    </div>
                    
                    <div class="mt-4 text-muted small">
                        Redirection automatique dans <span id="countdown" class="fw-bold text-primary">10</span>s
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
            window.location.href = "${pageContext.request.contextPath}/BackOf-taxi/reservation/form";
        }
    }, 1000);
</script>

<jsp:include page="layout/footer.jsp" />
