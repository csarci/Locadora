/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;


import util.HibernateDAO;
import modelo.Aluguel;

/**
 *
 * @author Fernando
 */
public class AluguelDAO extends HibernateDAO<Aluguel, Long>  {
    
            private static AluguelDAO aluguelDAO = null;

    public synchronized static AluguelDAO getAluguelDAO() {
        if (aluguelDAO == null) {
            aluguelDAO = new AluguelDAO();
        }
        return aluguelDAO;
    }

    public AluguelDAO() {
        super();
    }
    
}
