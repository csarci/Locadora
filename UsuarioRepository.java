package br.rnp.perfis.repository;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import br.rnp.perfis.domain.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Serializable> {

	Usuario findByNome(String nome);

	List<Usuario> findById(List<Long> id);

	Usuario findByLogin(String username);

	@Query("select u from Usuario u order by u.nome asc")
	List<Usuario> findAll(Pageable page);

	@Query("select u from Usuario u where u.nome like %?1% order by u.nome asc")
	List<Usuario> findAll(String search, Pageable page);

	@Query("select count(u) from Usuario u where u.nome like %?1%")
	Long count(String search);

	//Fazer a consulta para o tipo de usuario baseado na consulta abaixo para grupos de usuarios
	
	@Query("select u from Usuario u join u.grupos g where g.id = ?1")
	List<Usuario> findByGrupo(Long id);

	@Query("select u.password from Usuario u where u.id = ?1")
	String findPasswordByUsuario(Long id);

	Usuario findByNomeAndEmail(String nome, String email);

	Usuario findByHash(String hash);

	@Query("select u from Usuario u where u.admin = true")
	List<Usuario> findAdmins();
	
	@Query("select count(u) from Usuario u where u.nome like %?1% and u.ativo = ?2")
	Long count(String search, Boolean status);

	@Query("select count(u) from Usuario u where u.ativo = ?1")
	Long count(Boolean status);

	@Query("select u from Usuario u where u.nome like %?1% and u.ativo = ?2 order by u.nome asc")
	List<Usuario> findAll(String search, Boolean status, Pageable page);

	@Query("select u from Usuario u where u.ativo = ?1 order by u.nome asc")
	List<Usuario> findAll(Boolean status, Pageable page);
	
	
}
