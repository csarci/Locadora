<%-- 
    Document   : index
    Created on : Jun 18, 2014, 11:19:40 PM
    Author     : Fernando
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <script>

            function desenvolvidoPor() {

                alert("Desenvolvido por :\n\
                Fernando Moutinho\n\
                Aiquis Rodrigues\n\
                Bruno Conti");
            }</script>

        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">

    </head>


    <body style="background-image:url(http://www.pptbackgrounds.net/uploads/film-movies-movie-making-minimalism-creative-backgrounds-wallpapers.jpg); background-repeat:no-repeat;background-size: 100%;">
    <c:import url="cabecalho.jsp"/>

    <br><br><br><br><br><br><br><br><br><br><br><br>
    <p align="center" style="color:white;font-family:Arial;">SELECIONE A OPÇÃO DESEJADA: </p>
    <table align="center">
        <tr>
            <td align="left">
                <form action="adiciona-cliente.jsp">
                    <input type="submit" value="Cadastro de clientes">
                </form>
                <form action="adiciona-filme.jsp">
                    <input type="submit" value="Cadastro de filmes">
                </form>
                <form action="adiciona-aluguel.jsp">
                    <input type="submit" value="Locação">
                </form>
                <form action="devolucao-filme.jsp">
                    <input type="submit" value="Devolução">
                </form>
                <form onclick="desenvolvidoPor()">
                    <input type="submit" value="Desenvolvedores">
                </form>
            </td>

    </table>
    <c:import url="rodape.jsp"/>
</body>

</html>
