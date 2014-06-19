/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modelo;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;

/**
 *
 * @author Fernando
 */
@Entity
public class Aluguel implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private boolean devolvido;

    @ManyToOne
    private Cliente cliente;

    @ManyToOne
    private Fita fita;
    @Column
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataAluguel;
    @Column
    @Temporal(javax.persistence.TemporalType.DATE)
    private Calendar dataLimite;

    public Aluguel() {
    }

    public Aluguel(Cliente cliente, Fita fita, boolean devolvido, Calendar dataAluguel, Calendar dataLimite) {

        this.cliente = cliente;
        this.fita = fita;
        this.devolvido = devolvido;
        this.dataAluguel = dataAluguel;
        this.dataLimite = dataLimite;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public boolean isDevolvido() {
        return devolvido;
    }

    public void setDevolvido(boolean devolvido) {
        this.devolvido = devolvido;
    }

    public Fita getFita() {
        return fita;
    }

    public void setFita(Fita fita) {
        this.fita = fita;
    }

    public Calendar getDataAluguel() {
        return dataAluguel;
    }

    public void setDataAluguel(Calendar dataAluguel) {
        this.dataAluguel = dataAluguel;
    }

    public Calendar getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(Calendar dataLimite) {
        this.dataLimite = dataLimite;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Aluguel)) {
            return false;
        }
        Aluguel other = (Aluguel) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "modelo.Aluguel[ id=" + id + " ]";
    }

}
