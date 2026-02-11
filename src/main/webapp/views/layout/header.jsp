<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>BackOffice Taxi - <%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "Admin" %></title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap" rel="stylesheet">
    <style>
        :root {
            --sidebar-width: 260px;
            --primary-color: #4e73df;
            --secondary-color: #858796;
            --dark-bg: #1a1c23;
            --light-bg: #f8f9fc;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--light-bg);
            overflow-x: hidden;
        }

        /* Sidebar */
        #sidebar {
            width: var(--sidebar-width);
            height: 100vh;
            position: fixed;
            top: 0;
            left: 0;
            background: var(--dark-bg);
            color: #fff;
            transition: all 0.3s;
            z-index: 1000;
        }

        #sidebar .sidebar-header {
            padding: 20px;
            background: rgba(0,0,0,0.1);
            border-bottom: 1px solid rgba(255,255,255,0.05);
        }

        #sidebar .nav-link {
            padding: 12px 20px;
            color: rgba(255,255,255,0.7);
            display: flex;
            align-items: center;
            border-left: 4px solid transparent;
            transition: all 0.2s;
        }

        #sidebar .nav-link:hover, #sidebar .nav-link.active {
            color: #fff;
            background: rgba(255,255,255,0.05);
            border-left-color: var(--primary-color);
        }

        #sidebar .nav-link i {
            width: 20px;
            margin-right: 12px;
            font-size: 1.1rem;
        }

        #sidebar .category-title {
            padding: 20px 20px 10px;
            font-size: 0.75rem;
            text-transform: uppercase;
            font-weight: 700;
            color: rgba(255,255,255,0.4);
            letter-spacing: 1px;
        }

        /* Main Content */
        #content {
            margin-left: var(--sidebar-width);
            width: calc(100% - var(--sidebar-width));
            min-height: 100vh;
            transition: all 0.3s;
        }

        /* Header */
        .topbar {
            height: 70px;
            background: #fff;
            box-shadow: 0 .15rem 1.75rem 0 rgba(58,59,69,.15);
            padding: 0 30px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            position: sticky;
            top: 0;
            z-index: 900;
        }

        .breadcrumb-item + .breadcrumb-item::before {
            content: "\f105";
            font-family: "Font Awesome 6 Free";
            font-weight: 900;
            font-size: 0.8rem;
        }

        /* Cards & Forms */
        .card {
            border: none;
            border-radius: 0.75rem;
            box-shadow: 0 .15rem 1.75rem 0 rgba(58,59,69,.1);
        }

        .card-header {
            background-color: #fff;
            border-bottom: 1px solid #e3e6f0;
            padding: 1.25rem;
            font-weight: 700;
            color: var(--primary-color);
        }

        .form-label {
            font-weight: 600;
            color: #4e73df;
            font-size: 0.9rem;
        }

        .form-control, .form-select {
            padding: 0.75rem 1rem;
            border-radius: 0.5rem;
            border: 1px solid #d1d3e2;
        }

        .form-control:focus, .form-select:focus {
            box-shadow: 0 0 0 0.25rem rgba(78, 115, 223, 0.25);
            border-color: #bac8f3;
        }

        .btn-primary {
            background-color: var(--primary-color);
            border: none;
            padding: 0.75rem 1.5rem;
            border-radius: 0.5rem;
            font-weight: 600;
            transition: all 0.3s;
        }

        .btn-primary:hover {
            background-color: #2e59d9;
            transform: translateY(-1px);
            box-shadow: 0 0.5rem 1rem rgba(0,0,0,0.15);
        }

        /* Stats Card */
        .stats-card {
            border-left: 0.25rem solid var(--primary-color);
        }

        .stats-title {
            font-size: 0.7rem;
            font-weight: 700;
            color: var(--primary-color);
            text-transform: uppercase;
            margin-bottom: 0.25rem;
        }

        .stats-value {
            font-size: 1.25rem;
            font-weight: 700;
            color: #5a5c69;
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
        }
    </style>
</head>
<body>

    <!-- Sidebar -->
    <nav id="sidebar">
        <div class="sidebar-header">
            <h4 class="m-0"><i class="fas fa-taxi me-2 text-primary"></i>Taxi Admin</h4>
        </div>
        
        <div class="category-title">Principal</div>
        <a href="${pageContext.request.contextPath}/reservation/form" class="nav-link <%= request.getRequestURI().contains("reservation") ? "active" : "" %>">
            <i class="fas fa-calendar-check"></i> Réservations
        </a>
        <a href="${pageContext.request.contextPath}/vehicule/form" class="nav-link <%= request.getRequestURI().contains("vehicule") ? "active" : "" %>">
            <i class="fas fa-car"></i> Véhicules
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
