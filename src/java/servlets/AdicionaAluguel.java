/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import dao.AluguelDAO;
import dao.ClienteDAO;
import dao.FitaDAO;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import modelo.Aluguel;
import modelo.Cliente;
import modelo.Fita;

/**
 *
 * @author Fernando
 */
@WebServlet("/adicionaAluguel")
public class AdicionaAluguel extends HttpServlet {

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
        String dataEmTexto1 = req.getParameter("dataAluguel");
        String dataEmTexto2 = req.getParameter("dataLimite");

        Calendar dataAluguel = null;
        Calendar dataLimite = null;

        try {
            Date date1 = new SimpleDateFormat("MM/dd/yyyy").parse(dataEmTexto1);
            dataAluguel = Calendar.getInstance();
            dataAluguel.setTime(date1);
            Date date2 = new SimpleDateFormat("MM/dd/yyyy").parse(dataEmTexto2);
            dataLimite = Calendar.getInstance();
            dataLimite.setTime(date2);

        } catch (ParseException ex) {
            Logger.getLogger(AdicionaAluguel.class.getName()).log(Level.SEVERE, null, ex);
        }

        Cliente clienteQueAlugou = null;
        ClienteDAO clienteDAO = new ClienteDAO();
        FitaDAO fitaDAO = new FitaDAO();
        Fita fitaAlugada = null;

        for (Cliente cliente : clienteDAO.getLista()) {

            if (cliente.getNome().equals(nomeCliente)) {
                clienteQueAlugou = cliente;
                break;
            }
        }

        for (Fita fita : fitaDAO.getListaDisponivel()) {

            if (fita.getNome().equals(nomeDoFilme)) {
                fitaAlugada = fita;
                fita.setAlugado(true);
                fitaDAO.save(fita);
                break;
            }
        }

        AluguelDAO aluguelDAO = new AluguelDAO();
        aluguelDAO.save(new Aluguel(clienteQueAlugou, fitaAlugada, false, dataAluguel, dataLimite));

    }

}
