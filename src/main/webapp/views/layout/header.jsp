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
            --sidebar-width: 280px;
            --primary-color: #6366f1;
            --primary-hover: #4f46e5;
            --secondary-color: #94a3b8;
            --dark-bg: #0f172a;
            --light-bg: #f8fafc;
            --sidebar-active-bg: rgba(99, 102, 241, 0.1);
            --card-shadow: 0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1);
            --card-shadow-hover: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--light-bg);
            color: #1e293b;
            overflow-x: hidden;
        }

        /* Sidebar Improvement */
        #sidebar {
            width: var(--sidebar-width);
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            background: #fff;
            color: #1e293b;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
            z-index: 1000;
            border-right: 1px solid #e2e8f0;
        }

        #sidebar .sidebar-header {
            padding: 24px;
            background: #fff;
            margin-bottom: 10px;
            border-bottom: 1px solid #f1f5f9;
        }

        #sidebar .nav-link {
            padding: 12px 24px;
            margin: 4px 16px;
            color: #64748b;
            display: flex;
            align-items: center;
            border-radius: 12px;
            transition: all 0.3s;
            font-weight: 500;
        }

        #sidebar .nav-link:hover {
            color: var(--primary-color);
            background: var(--sidebar-active-bg);
            transform: translateX(4px);
        }

        #sidebar .nav-link.active {
            color: #fff;
            background: var(--primary-color);
            box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
        }

        #sidebar .nav-link i {
            width: 24px;
            margin-right: 12px;
            font-size: 1.25rem;
        }

        #sidebar .category-title {
            padding: 24px 24px 8px;
            font-size: 0.7rem;
            text-transform: uppercase;
            font-weight: 800;
            color: #94a3b8;
            letter-spacing: 1.5px;
        }

        /* Main Content */
        #content {
            margin-left: var(--sidebar-width);
            width: calc(100% - var(--sidebar-width));
            min-height: 100vh;
            transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
        }

        /* Header / Topbar */
        .topbar {
            height: 80px;
            background: rgba(255, 255, 255, 0.8);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid #e2e8f0;
            padding: 0 40px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 900;
        }

        /* Modern Cards */
        .card {
            border: none;
            border-radius: 16px;
            background: #fff;
            box-shadow: var(--card-shadow);
            transition: all 0.3s ease;
        }

        .card:hover {
            box-shadow: var(--card-shadow-hover);
        }

        .card-header {
            background-color: #fff;
            border-bottom: 1px solid #f1f5f9;
            padding: 20px 24px;
            border-radius: 16px 16px 0 0 !important;
        }

        .card-header h5 {
            font-weight: 700;
            color: #1e293b;
            font-size: 1.1rem;
        }

        /* Forms Styling */
        .form-label {
            font-weight: 600;
            color: #475569;
            font-size: 0.85rem;
            margin-bottom: 8px;
        }

        .form-control, .form-select {
            padding: 12px 16px;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
            background-color: #f8fafc;
            transition: all 0.2s;
            font-size: 0.95rem;
        }

        .form-control:focus, .form-select:focus {
            background-color: #fff;
            border-color: var(--primary-color);
            box-shadow: 0 0 0 4px rgba(99, 102, 241, 0.1);
            outline: none;
        }

        .input-group-text {
            border-radius: 12px 0 0 12px;
            border: 1px solid #e2e8f0;
            background-color: #f1f5f9;
            color: #64748b;
        }

        /* Buttons */
        .btn {
            padding: 10px 24px;
            border-radius: 12px;
            font-weight: 600;
            transition: all 0.3s;
        }

        .btn-primary {
            background-color: var(--primary-color);
            border: none;
            box-shadow: 0 4px 6px -1px rgba(99, 102, 241, 0.2);
        }

        .btn-primary:hover {
            background-color: var(--primary-hover);
            transform: translateY(-2px);
            box-shadow: 0 10px 15px -3px rgba(99, 102, 241, 0.3);
        }

        /* Stats Cards Improvement */
        .stats-card {
            border-left: 0;
            position: relative;
            overflow: hidden;
            border: 1px solid #f1f5f9;
        }

        .stats-card .card-body {
            padding: 1.5rem;
        }

        .stats-title {
            font-size: 0.85rem;
            font-weight: 600;
            color: #64748b;
            text-transform: none;
            letter-spacing: normal;
            margin-bottom: 0.5rem;
        }

        .stats-value {
            font-size: 1.75rem;
            font-weight: 800;
            color: #0f172a;
            line-height: 1;
        }

        .stats-icon-wrapper {
            width: 56px;
            height: 56px;
            border-radius: 14px;
            display: flex;
            align-items: center;
            justify-content: center;
            transition: all 0.3s ease;
        }

        .card:hover .stats-icon-wrapper {
            transform: scale(1.1) rotate(-5deg);
        }

        .bg-gradient-primary {
            background: linear-gradient(135deg, #6366f1 0%, #4f46e5 100%);
            color: white;
        }

        .bg-gradient-success {
            background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
            color: white;
        }

        .bg-gradient-info {
            background: linear-gradient(135deg, #0ea5e9 0%, #0284c7 100%);
            color: white;
        }

        .badge {
            padding: 6px 12px;
            border-radius: 8px;
            font-weight: 600;
        }

        /* Utility Classes */
        .bg-indigo-soft { background-color: rgba(99, 102, 241, 0.1); }
        .bg-primary-soft { background-color: rgba(99, 102, 241, 0.1); }
        .bg-success-soft { background-color: rgba(34, 197, 94, 0.1); }
        .bg-warning-soft { background-color: rgba(245, 158, 11, 0.1); }
        .bg-danger-soft { background-color: rgba(239, 68, 68, 0.1); }
        
        .text-indigo { color: #6366f1; }
        .text-success { color: #22c55e !important; }
        
        .icon-circle {
            height: 48px;
            width: 48px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 12px;
        }

        .icon-circle i {
            font-size: 1.25rem;
        }

        @media (max-width: 992px) {
            #sidebar {
                left: -var(--sidebar-width);
            }
            #sidebar.active {
                left: 0;
            }
            #content {
                margin-left: 0;
                width: 100%;
            }
            .topbar {
                padding: 0 20px;
            }
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
        <a href="${pageContext.request.contextPath}/reservation/form" class="nav-link <%= request.getServletPath().contains("reservationForm.jsp") ? "active" : "" %>">
            <i class="fas fa-calendar-check"></i> Réservations
        </a>
        <a href="${pageContext.request.contextPath}/reservation/assignation" class="nav-link <%= request.getRequestURI().contains("reservation/assignation") || request.getServletPath().contains("reservationAssignation.jsp") ? "active" : "" %>">
            <i class="fas fa-tasks"></i> Assignation
        </a>
        <a href="${pageContext.request.contextPath}/vehicule/list" class="nav-link <%= request.getRequestURI().contains("vehicule/list") ? "active" : "" %>">
            <i class="fas fa-list"></i> Liste Véhicules
        </a>
        <a href="${pageContext.request.contextPath}/vehicule/form" class="nav-link <%= request.getRequestURI().contains("vehicule/form") || request.getServletPath().contains("vehicule/form.jsp") ? "active" : "" %>">
             <i class="fas fa-plus-circle"></i> Ajouter Véhicule
         </a>

        <div class="category-title">Configuration</div>
        <a href="${pageContext.request.contextPath}/parametre/form" class="nav-link <%= request.getRequestURI().contains("parametre") ? "active" : "" %>">
            <i class="fas fa-cogs"></i> Paramètres
        </a>

        <div class="category-title">Données & APIs</div>
        <a href="${pageContext.request.contextPath}/api/reservations" target="_blank" class="nav-link">
            <i class="fas fa-code"></i> API Réservations
        </a>
        <a href="${pageContext.request.contextPath}/api/hotels" target="_blank" class="nav-link">
            <i class="fas fa-hotel"></i> API Hôtels
        </a>
        <a href="${pageContext.request.contextPath}/api/type-carburants" target="_blank" class="nav-link">
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
                        <li><a class="dropdown-item py-2 text-danger" href="#"><i class="fas fa-sign-out-alt fa-sm fa-fw me-2"></i> Déconnexion</a></li>
                    </ul>
                </div>
            </div>
        </header>

        <main class="p-4">
