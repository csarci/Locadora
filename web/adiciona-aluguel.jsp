

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>


    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <jsp:useBean id="fitadao" type="dao.FitaDAO" class="dao.FitaDAO"/>
    <jsp:useBean id="clientedao" type="dao.ClienteDAO" class="dao.ClienteDAO"/>
    <%@page import="java.util.List"%>
    <%@page import="modelo.Fita"%>
    <%@page import="modelo.Cliente"%>


    <h3 align = "center" style="color:white;letter-spacing:3px;font-family:Broadway;" > Cadastrar aluguel </h3>

    <body style="background-image:url(http://www.pptbackgrounds.net/uploads/film-movies-movie-making-minimalism-creative-backgrounds-wallpapers.jpg); background-repeat:no-repeat;background-size: 100%;">
        <form action="adicionaAluguel" method="post">

            <p align="left" style="color:white;font-family:Arial;">Nome do cliente: 
                <select name = "nome"id="clientes" >
                    <c:forEach var="cliente" items="${clientedao.lista}"varStatus="id">
                        <option value="${cliente.nome}">${cliente.nome}</option>
                    </c:forEach>
                </select>
            <p align="left" style="color:white;font-family:Arial;">Data do aluguel: <input type="date" name="dataAluguel"></p>
            <p align="left" style="color:white;font-family:Arial;">Data limite: <input type="date" name="dataLimite"></p>
            <p align="left" style="color:white;font-family:Arial;">
                Filmes disponíveis:						
                <select name = "filmeSelecionado"id="filmesDisponiveis" >
                    <c:forEach var="fita" items="${fitadao.listaDisponivel}"varStatus="id">
                        <option value="${fita.nome}">${fita.nome}</option>
                    </c:forEach>
                </select>
            </p>


            <p align="left"><input type="reset" value="Limpar"><input type="submit" value="Enviar"></p>
        </form>


        <c:import url="rodape.jsp"/>
    </body>
</html>