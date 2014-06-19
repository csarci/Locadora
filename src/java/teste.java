
import dao.AluguelDAO;
import dao.ClienteDAO;
import dao.FitaDAO;
import modelo.Aluguel;
import modelo.Cliente;
import modelo.Fita;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Fernando
 */
public class teste {
    
    public static void main(String[] args) {
        ClienteDAO clienteDAO = new ClienteDAO();
        FitaDAO fitaDAO = new FitaDAO();
        AluguelDAO aluguelDAO = new AluguelDAO();
        Cliente cliente = new Cliente("fernandooo", "fernandooo", "fernandooo", "fernandooo", "fernandooo", "fernandooo", null);
        Fita fita = new Fita("kombi",true, null);
        fitaDAO.save(fita);
        clienteDAO.save(cliente);
        aluguelDAO.save(new Aluguel(cliente, fita, true, null, null));
        
        
    }
}
