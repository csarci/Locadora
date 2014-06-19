/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import dao.ClienteDAO;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import modelo.Cliente;

/**
 *
 * @author Fernando
 */
@WebServlet("/adicionaCliente")
public class AdicionaCliente extends HttpServlet {

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
        String cpf = req.getParameter("cpf");
        String sexo = req.getParameter("sexo");
        String endereco = req.getParameter("endereco");
        String email = req.getParameter("email");
        String telefone = req.getParameter("telefone");


        ClienteDAO clienteDAO = new ClienteDAO();
        clienteDAO.save(new Cliente(nome, cpf, sexo, endereco, email, telefone,  null));

    }
}
