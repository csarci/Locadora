package br.rnp.agendamento.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import br.rnp.agendamento.domain.Sala;

public interface SalaRepository extends	CrudRepository<Sala, Serializable>  {

	Sala findByNome(String nome);

	@Query("select organizacaoId from Sala")
	List<Long> getOrganizacoesIdsComSalasCadastradas();

	@Query("select instituicaoId from Sala")
	List<Long> getInstituicaoIdsComSalasCadastradas();
	
	@Query("select cor from Sala")
	List<String> getCoresSalas();
	
}
