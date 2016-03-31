package br.rnp.perfis.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import br.rnp.perfis.domain.Usuario;

public interface UsuarioService {

	Usuario save(Usuario usuario);

	List<Usuario> listAll(String search, Pageable page);

	Usuario findById(Long id);
	
	Usuario findByHash(String hash);

	Usuario delete(Long id);

	Usuario findByUsername(String login);

	Long count(String search);
	
	void setMailService(MailService mailService);

	void alterarSenha(Long usuarioId, String atual, String nova);

	Usuario solicitarAcesso(Usuario usuario);
	
	void atualizarUsuario(String senha, String hash);
	
	List<Usuario> listAll(String search, Pageable page, Boolean status);

	Long count(String search, Boolean status);

	void resetPassword(Long id);

	
}
