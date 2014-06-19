/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import dao.AluguelDAO;
import dao.FitaDAO;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import modelo.Aluguel;

/**
 *
 * @author Fernando
 */
@WebServlet("/devolucao")
public class Devolucao extends HttpServlet {

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

        String nomeCliente = req.getParameter("nome");
        String nomeDoFilme = req.getParameter("filmeSelecionado");

        AluguelDAO aluguelDAO = new AluguelDAO();

        FitaDAO fitaDAO = new FitaDAO();

        for (Aluguel aluguel : aluguelDAO.getLista()) {

            if ((aluguel.getCliente().getNome().equals(nomeCliente)) && (aluguel.getFita().getNome().equals(nomeDoFilme)) && (!aluguel.isDevolvido())) {

                aluguel.getFita().setAlugado(false);
                fitaDAO.save(aluguel.getFita());
                aluguel.setDevolvido(true);
                aluguelDAO.save(aluguel);
                break;
            }
        }

    }

}
