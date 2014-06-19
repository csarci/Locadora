/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import dao.FitaDAO;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import modelo.Fita;

/**
 *
 * @author Fernando
 */
@WebServlet("/adicionaFilme")
public class AdicionaFilme extends HttpServlet {

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        log("Iniciando a servlet");
    }

    @Override
    public void destroy() {
        super.destroy();
        log("Destruindo a servlet");
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {

        String nome = req.getParameter("nome");

        FitaDAO fitaDAO = new FitaDAO();
        fitaDAO.save(new Fita(nome, false, null));

    }
}
