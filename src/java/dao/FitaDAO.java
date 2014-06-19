/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.util.Iterator;
import java.util.List;
import util.HibernateDAO;
import modelo.Fita;
import org.hibernate.Query;
import util.HibernateUtil;

/**
 *
 * @author Fernando
 */
public class FitaDAO extends HibernateDAO<Fita, Long> {

    private static FitaDAO fitaDAO = null;

    public synchronized static FitaDAO getFitaDAO() {
        if (fitaDAO == null) {
            fitaDAO = new FitaDAO();
        }
        return fitaDAO;
    }

    public FitaDAO() {
        super();
    }

    public List<Fita> getListaDisponivel() {
        List list = this.getLista();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Fita fita = (Fita) it.next();
            if (fita.isAlugado()) {
                it.remove();
            }

        }

        return list;

    }
    
        public List<Fita> getListaIndisponivel() {
        List list = this.getLista();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Fita fita = (Fita) it.next();
            if (!fita.isAlugado()) {
                it.remove();
            }

        }

        return list;

    }

}
