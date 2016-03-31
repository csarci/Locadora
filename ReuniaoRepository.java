package br.rnp.agendamento.repository;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import br.rnp.agendamento.domain.Reuniao;

public interface ReuniaoRepository extends
		CrudRepository<Reuniao, Serializable> {

	List<Reuniao> findById(List<Long> id);

	@Query("select r from Reuniao r order by r.assunto asc")
	List<Reuniao> findAll(Pageable page);

	List<Reuniao> findByDataInicial(Date data);

	List<Reuniao> findByCriadoPor(Long id);

	@Query("select r from Reuniao r left join fetch r.salas s join s.responsaveis resp where r.aprovado is null and resp.perfilId = ?1")
	List<Reuniao> findByAprovadoIsNull(Long usuarioId, Pageable page);

	@Query("select count(r) from Reuniao r where r.aprovado is null")
	Long countParaAprovacao();
	
	

}
