<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="com.taxi.model.TypeCarburant" %>
<%@ page import="com.taxi.model.Vehicule" %>
<% request.setAttribute("pageTitle", "Gestion des Véhicules"); %>
<jsp:include page="../layout/header.jsp" />

<%
    Vehicule vehicule = (Vehicule) request.getAttribute("vehicule");
    boolean isEdit = (vehicule != null);
    String formAction = isEdit ? "/vehicule/update" : "/vehicule/save";
    String pageHeader = isEdit ? "Modifier le véhicule" : "Nouveau véhicule";
    String submitText = isEdit ? "Enregistrer" : "Créer";
%>

<div class="container-fluid">
    <% if (request.getAttribute("successMessage") != null) { %>
        <div class="status-highlight mb-4">
            SUCCESS: <%= request.getAttribute("successMessage") %>
        </div>
    <% } %>

    <% if (request.getAttribute("errorMessage") != null) { %>
        <div class="bg-dark text-white p-3 mb-4">
            ERROR: <%= request.getAttribute("errorMessage") %>
        </div>
    <% } %>

    <div class="row">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h5 class="m-0"><%= pageHeader %></h5>
                </div>
                <div class="card-body p-4">
                    <form action="${pageContext.request.contextPath}<%= formAction %>" method="post">
                        <% if (isEdit) { %>
                            <input type="hidden" name="idVehicule" value="<%= vehicule.getIdVehicule() %>">
                        <% } %>
                        <div class="row g-4 mb-4">
                            <div class="col-md-6">
                                <label for="reference" class="form-label">Référence</label>
                                <input type="text" id="reference" name="reference" class="form-control" 
                                       placeholder="VEH-001" required
                                       value="<%= isEdit ? vehicule.getReference() : "" %>">
                            </div>
                            <div class="col-md-6">
                                <label for="nbrPlace" class="form-label">Capacité (Places)</label>
                                <input type="number" id="nbrPlace" name="nbrPlace" class="form-control" 
                                       min="1" required
                                       value="<%= isEdit ? vehicule.getNbrPlace() : "" %>">
                            </div>
                            <div class="col-md-12">
                                <label for="idTypeCarburant" class="form-label">Type d'énergie</label>
                                <select id="idTypeCarburant" name="idTypeCarburant" class="form-select" required>
                                    <option value="">-- Choisir --</option>
                                    <% 
                                        List<TypeCarburant> types = (List<TypeCarburant>) request.getAttribute("types");
                                        if (types != null) {
                                            for (TypeCarburant t : types) {
                                                String selected = (isEdit && t.getIdTypeCarburant().equals(vehicule.getIdTypeCarburant())) ? "selected" : "";
                                    %>
                                        <option value="<%= t.getIdTypeCarburant() %>" <%= selected %>><%= t.getLibelle() %></option>
                                    <% 
                                            }
                                        } 
                                    %>
                                </select>
                            </div>
                        </div>

                        <div class="d-flex justify-content-end gap-3 border-top pt-4">
                            <a href="${pageContext.request.contextPath}/BackOf-taxi/vehicule/list" class="btn border">Annuler</a>
                            <button type="submit" class="btn btn-primary"><%= submitText %></button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../layout/footer.jsp" />
