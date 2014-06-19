/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.Query;

/**
 *
 * @author Fernando
 * @param <T>
 * @param <Type>
 */
public abstract class HibernateDAO<T, Type extends Serializable> implements GenericDAO<T, Type> {

    private final Class persistentClass;

    @SuppressWarnings("empty-statement")
    public HibernateDAO() {

        super();

        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];;

    }

    @Override

    public void beginTransaction() {

        HibernateUtil.beginTransaction();

    }

    @Override

    public void commitTransaction() {

        HibernateUtil.commitTransaction();

    }

    @Override

    public void save(T entity) {

        beginTransaction();
        HibernateUtil.getSession().saveOrUpdate(entity);
        commitTransaction();

    }

    @Override

    public void delete(T entity) {

        beginTransaction();
        HibernateUtil.getSession().delete(entity);
        commitTransaction();

    }

    @Override

    public List<T> getLista() {
        List list ;
        beginTransaction();

        Query  query = HibernateUtil.getSession().createQuery("SELECT obj FROM " + persistentClass.getSimpleName() + " obj");
        list = query.list();
        commitTransaction();

        return list;

    }



}
