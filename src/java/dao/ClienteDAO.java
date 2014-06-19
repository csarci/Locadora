/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;


import util.HibernateDAO;
import modelo.Cliente;

/**
 *
 * @author Fernando
 */
public class ClienteDAO extends HibernateDAO<Cliente, Long> {
    
        private static ClienteDAO clienteDAO = null;

    public synchronized static ClienteDAO getClienteDAO() {
        if (clienteDAO == null) {
            clienteDAO = new ClienteDAO();
        }
        return clienteDAO;
    }

    public ClienteDAO() {
        super();
    }
    
}
