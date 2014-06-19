/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 *
 * @author Fernando
 */
public class HibernateUtil {

    private static SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();

    private static ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();

    public static Session getSession() {

        Session session = threadLocal.get();

        if (session == null) {

            session = sessionFactory.openSession();

            threadLocal.set(session);

        }

        return session;

    }

    public static void beginTransaction() {

        getSession().beginTransaction();

    }

    public static void commitTransaction() {

        getSession().getTransaction().commit();

    }

    public static void rollBackTransaction() {

        getSession().getTransaction().rollback();

    }

    public static void closeSession() {

        getSession().close();

    }

}
