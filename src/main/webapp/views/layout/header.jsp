<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>inDrive - <%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Admin" %></title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --sidebar-width: 260px;
            --primary-color: #c1f11d; /* Vert Fluo */
            --bg-dark: #000000;
            --bg-light: #ffffff;
            --text-dark: #000000;
            --text-light: #ffffff;
            --border-color: #e0e0e0;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--bg-light);
            color: var(--text-dark);
            margin: 0;
            overflow-x: hidden;
        }

        /* Sidebar Minimaliste */
        #sidebar {
            width: var(--sidebar-width);
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            background: var(--bg-dark);
            color: var(--text-light);
            z-index: 1000;
            border-right: 1px solid var(--bg-dark);
        }

        #sidebar .sidebar-header {
            padding: 30px 24px;
            border-bottom: 1px solid #333;
        }

        #sidebar .nav-link {
            padding: 15px 24px;
            color: #888;
            display: flex;
            align-items: center;
            transition: all 0.2s;
            font-weight: 500;
            text-decoration: none;
            border-left: 4px solid transparent;
        }

        #sidebar .nav-link:hover {
            color: var(--text-light);
            background: #111;
        }

        #sidebar .nav-link.active {
            color: var(--primary-color);
            background: #111;
            border-left-color: var(--primary-color);
        }

        #sidebar .nav-link i {
            width: 20px;
            margin-right: 12px;
        }

        #sidebar .category-title {
            padding: 25px 24px 10px;
            font-size: 0.65rem;
            text-transform: uppercase;
            color: #555;
            letter-spacing: 2px;
            font-weight: 700;
        }

        /* Main Content */
        #content {
            margin-left: var(--sidebar-width);
            width: calc(100% - var(--sidebar-width));
            min-height: 100vh;
        }

        /* Topbar */
        .topbar {
            height: 70px;
            background: var(--bg-light);
            border-bottom: 1px solid var(--border-color);
            padding: 0 40px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        /* Elements Clés */
        .btn {
            padding: 12px 24px;
            border-radius: 0; /* Carré pour sobriété */
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 1px;
            transition: all 0.2s;
            font-size: 0.8rem;
        }

        .btn-primary {
            background-color: var(--bg-dark);
            color: var(--text-light);
            border: 2px solid var(--bg-dark);
        }

        .btn-primary:hover {
            background-color: var(--primary-color);
            color: var(--bg-dark);
            border-color: var(--primary-color);
        }

        .card {
            border: 1px solid var(--border-color);
            border-radius: 0;
            box-shadow: none;
            margin-bottom: 30px;
        }

        .card-header {
            background: var(--bg-dark);
            color: var(--text-light);
            padding: 15px 20px;
            border: none;
            border-radius: 0 !important;
        }

        .form-control, .form-select {
            border-radius: 0;
            border: 1px solid var(--bg-dark); /* Bordure Noire */
            padding: 12px;
            color: var(--text-dark); /* Texte Noir */
            background-color: var(--bg-light);
        }

        .form-control:focus, .form-select:focus {
            border-color: var(--bg-dark);
            background-color: #f0f0f0;
            box-shadow: 0 0 0 2px var(--primary-color);
            outline: none;
        }

        /* Statut Système - Vert Fluo */
        .stats-title {
            font-size: 0.75rem;
            text-transform: uppercase;
            letter-spacing: 1px;
            font-weight: 700;
            color: #666;
        }

        .status-highlight {
            background-color: var(--primary-color);
            color: var(--bg-dark);
            padding: 4px 12px;
            font-weight: 900;
            text-transform: uppercase;
            display: inline-block;
        }

        .badge-system {
            background: var(--primary-color);
            color: var(--bg-dark);
            font-weight: 800;
            border-radius: 0;
            padding: 5px 10px;
        }

        .table thead th {
            background: #f8f8f8;
            border-bottom: 2px solid var(--bg-dark);
            text-transform: uppercase;
            font-size: 0.75rem;
            letter-spacing: 1px;
            padding: 15px;
        }

        /* Suppression des éléments parasites */
        .stats-icon-wrapper, .icon-circle { display: none !important; }

        .bg-indigo-soft { background-color: rgba(99, 102, 241, 0.1); }
        .bg-success-soft { background-color: rgba(34, 197, 94, 0.1); }
        .bg-danger-soft { background-color: rgba(239, 68, 68, 0.1); }
        .bg-primary-soft { background-color: rgba(193, 241, 29, 0.1); }
        .bg-info-soft { background-color: rgba(13, 202, 240, 0.1); }
        
        @media (max-width: 992px) {
            #sidebar { left: -var(--sidebar-width); }
            #content { margin-left: 0; width: 100%; }
        }
    </style>
</head>
<body>

    <!-- Sidebar -->
    <nav id="sidebar">
        <div class="sidebar-header">
            <h4 class="m-0 fw-bold"><i class="fas fa-taxi me-2 text-primary"></i>inDrive</h4>
        </div>
        
        <div class="category-title">Principal</div>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/form" class="nav-link <%= request.getServletPath().contains("reservation/form.jsp") ? "active" : "" %>">
            <i class="fas fa-calendar-check"></i> Réservations
        </a>
        
        <div class="nav-item">
            <a href="#assignationSubmenu" data-bs-toggle="collapse" class="nav-link <%= request.getRequestURI().contains("reservation/assignation") ? "active" : "" %>">
                <i class="fas fa-tasks"></i> Assignation <i class="fas fa-chevron-down ms-auto small"></i>
            </a>
            <div class="collapse <%= request.getRequestURI().contains("reservation/assignation") ? "show" : "" %>" id="assignationSubmenu">
                <a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation" class="nav-link ps-5 py-2 small <%= request.getServletPath().contains("reservation/assignation.jsp") ? "fw-bold text-primary" : "" %>">
                    <i class="fas fa-list-ul me-2" style="font-size: 0.8rem;"></i> Par Réservation
                </a>
                <a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation-vehicule" class="nav-link ps-5 py-2 small <%= request.getServletPath().contains("reservation/assignationVehicule.jsp") ? "fw-bold text-primary" : "" %>">
                    <i class="fas fa-car me-2" style="font-size: 0.8rem;"></i> Par Véhicule
                </a>
                <a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignation-vehicule-split" class="nav-link ps-5 py-2 small <%= request.getServletPath().contains("reservation/assignationVehiculeSplit.jsp") ? "fw-bold text-primary" : "" %>">
                    <i class="fas fa-random me-2" style="font-size: 0.8rem;"></i> Par Véhicule (Split)
                </a>
                <a href="${pageContext.request.contextPath}/BackOf-taxi/reservation/assignations" class="nav-link ps-5 py-2 small <%= request.getRequestURI().contains("reservation/assignations") ? "fw-bold text-primary" : "" %>">
                    <i class="fas fa-clipboard-list me-2" style="font-size: 0.8rem;"></i> Liste Validée
                </a>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/list" class="nav-link <%= request.getRequestURI().contains("vehicule/list") ? "active" : "" %>">
            <i class="fas fa-list"></i> Liste Véhicules
        </a>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/disponible" class="nav-link <%= request.getRequestURI().contains("vehicule/disponible") || request.getServletPath().contains("disponible.jsp") ? "active" : "" %>">
            <i class="fas fa-check-circle"></i> Disponibilité
        </a>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/form" class="nav-link <%= request.getRequestURI().contains("vehicule/form") || request.getServletPath().contains("vehicule/form.jsp") ? "active" : "" %>">
             <i class="fas fa-plus-circle"></i> Ajouter Véhicule
         </a>

        <div class="category-title">Configuration</div>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/parametre/form" class="nav-link <%= request.getRequestURI().contains("parametre") ? "active" : "" %>">
            <i class="fas fa-cogs"></i> Paramètres
        </a>

        <div class="category-title">Données & APIs</div>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/api/reservations" target="_blank" class="nav-link">
            <i class="fas fa-code"></i> API Réservations
        </a>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/api/hotels" target="_blank" class="nav-link">
            <i class="fas fa-hotel"></i> API Hôtels
        </a>
        <a href="${pageContext.request.contextPath}/BackOf-taxi/api/type-carburants" target="_blank" class="nav-link">
            <i class="fas fa-gas-pump"></i> API Carburants
        </a>
    </nav>

    <!-- Main Content -->
    <div id="content">
        <!-- Topbar -->
        <header class="topbar">
            <button type="button" id="sidebarCollapse" class="btn btn-link d-lg-none text-dark">
                <i class="fas fa-bars"></i>
            </button>
            
            <nav aria-label="breadcrumb">
                <ol class="breadcrumb m-0">
                    <li class="breadcrumb-item text-secondary">Admin</li>
                    <li class="breadcrumb-item active" aria-current="page"><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Tableau de bord" %></li>
                </ol>
            </nav>

            <div class="d-flex align-items-center">
                <div class="dropdown">
                    <button class="btn btn-link text-dark text-decoration-none dropdown-toggle" type="button" data-bs-toggle="dropdown">
                        <i class="fas fa-user-circle me-1"></i> Administrateur
                    </button>
                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 mt-3">
                        <li><a class="dropdown-item py-2" href="#"><i class="fas fa-cog fa-sm fa-fw me-2 text-gray-400"></i> Paramètres</a></li>
                        <li><hr class="dropdown-divider"></li>
                        <li><a class="dropdown-item py-2 text-danger" href="${pageContext.request.contextPath}/"><i class="fas fa-sign-out-alt fa-sm fa-fw me-2"></i> Déconnexion</a></li>
                    </ul>
                </div>
            </div>
        </header>

        <main class="p-4">
