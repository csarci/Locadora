/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Fernando
 * @param <T>
 * @param <Type>
 */
public interface GenericDAO<T, Type extends Serializable> {

    void beginTransaction();

    void commitTransaction();

    void save(T entity);

    void delete(T entity);

    List<T> getLista();

}
