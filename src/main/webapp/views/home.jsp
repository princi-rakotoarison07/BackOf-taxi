<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>inDrive - Accueil</title>
    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet">
    <!-- Google Fonts -->
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;700;900&display=swap" rel="stylesheet">
    <style>
        :root {
            --primary-color: #c1f11d;
            --bg-dark: #000000;
            --bg-light: #ffffff;
        }

        body {
            font-family: 'Inter', sans-serif;
            background-color: var(--bg-light);
            color: var(--bg-dark);
            height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0;
            overflow: hidden;
        }

        .home-container {
            width: 100%;
            max-width: 1000px;
            padding: 40px;
        }

        .header-logo {
            text-align: center;
            margin-bottom: 60px;
        }

        .header-logo h1 {
            font-weight: 900;
            letter-spacing: -2px;
            font-size: 3.5rem;
            margin: 0;
            text-transform: uppercase;
        }

        .header-logo span {
            color: var(--primary-color);
        }

        .nav-cards {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 30px;
        }

        .nav-card {
            background: var(--bg-dark);
            color: white;
            padding: 60px 40px;
            text-decoration: none;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            position: relative;
            overflow: hidden;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            text-align: center;
            border: 2px solid var(--bg-dark);
        }

        .nav-card:hover {
            background: var(--bg-light);
            color: var(--bg-dark);
            transform: translateY(-10px);
            border-color: var(--bg-dark);
        }

        .nav-card i {
            font-size: 3rem;
            margin-bottom: 25px;
            transition: color 0.3s;
        }

        .nav-card:hover i {
            color: var(--bg-dark);
        }

        .nav-card h2 {
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 1px;
            margin-bottom: 15px;
            font-size: 1.5rem;
        }

        .nav-card p {
            font-size: 0.9rem;
            opacity: 0.7;
            margin: 0;
            max-width: 250px;
        }

        .nav-card .arrow {
            position: absolute;
            bottom: 30px;
            right: 30px;
            font-size: 1.5rem;
            opacity: 0;
            transform: translateX(-20px);
            transition: all 0.3s;
        }

        .nav-card:hover .arrow {
            opacity: 1;
            transform: translateX(0);
        }

        .system-status {
            position: fixed;
            bottom: 40px;
            left: 50%;
            transform: translateX(-50%);
            display: flex;
            align-items: center;
            gap: 10px;
            font-weight: 700;
            font-size: 0.8rem;
            text-transform: uppercase;
            letter-spacing: 1px;
        }

        .status-dot {
            width: 10px;
            height: 10px;
            background: var(--primary-color);
            border-radius: 50%;
            box-shadow: 0 0 10px var(--primary-color);
        }

        @media (max-width: 768px) {
            .nav-cards {
                grid-template-columns: 1fr;
            }
            .header-logo h1 {
                font-size: 2.5rem;
            }
        }
    </style>
</head>
<body>

    <div class="home-container">
        <div class="header-logo">
            <h1>IN<span>DRIVE</span></h1>
        </div>

        <div class="nav-cards">
            <a href="/BackOf-taxi/reservation/form" class="nav-card">
                <i class="fas fa-user-shield"></i>
                <h2>Back Office</h2>
                <p>Gestion des véhicules, réservations et paramètres système.</p>
                <i class="fas fa-arrow-right arrow"></i>
            </a>

            <a href="http://localhost:8081/reservations" class="nav-card">
                <i class="fas fa-taxi"></i>
                <h2>Front Office</h2>
                <p>Espace client pour la consultation et la prise de commandes.</p>
                <i class="fas fa-arrow-right arrow"></i>
            </a>
        </div>
    </div>

    <div class="system-status">
        <div class="status-dot"></div>
        Statut Système : Opérationnel
    </div>

</body>
</html>
