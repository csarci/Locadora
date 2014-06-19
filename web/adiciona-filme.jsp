
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>


    <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
    <c:import url="cabecalho.jsp"/>

    <h3 align = "center" style="color:white;letter-spacing:3px;font-family:Broadway;" > Cadastro de Filme </h3>

    <body style="background-image:url(http://www.pptbackgrounds.net/uploads/film-movies-movie-making-minimalism-creative-backgrounds-wallpapers.jpg); background-repeat:no-repeat;background-size: 100%;">
        <form action="adicionaFilme" method="post">

            <p align="left" style="color:white;font-family:Arial;">Nome: <input type="text" name="nome"></p>
            <p align="left"><input type="reset" value="Limpar"><input type="submit" value="Enviar"></p>
        </form>
    </form>

    <c:import url="rodape.jsp"/>
</body>
</html>