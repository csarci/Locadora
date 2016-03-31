package br.rnp.perfis.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.rnp.perfis.domain.Email;
import br.rnp.perfis.domain.Grupo;
import br.rnp.perfis.domain.PropriedadeUsuario;
import br.rnp.perfis.domain.TipoLogin;
import br.rnp.perfis.domain.Usuario;
import br.rnp.perfis.enums.TipoUsuarioConstantes;
import br.rnp.perfis.exception.RnpException;
import br.rnp.perfis.exception.UsuarioException;
import br.rnp.perfis.repository.GrupoRepository;
import br.rnp.perfis.repository.PropriedaUsuarioRepository;
import br.rnp.perfis.repository.UsuarioRepository;
import br.rnp.perfis.service.EmailService;
import br.rnp.perfis.service.MailService;
import br.rnp.perfis.service.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private GrupoRepository grupoRepository;

	@Autowired
	private PropriedaUsuarioRepository propriedadeUsuarioRepository;

	@Autowired
	private MailService mailService;
	
	@Autowired
	private EmailService emailService;

	@PersistenceContext(unitName="entityManagerFactory")
	private EntityManager em;
	
	private Logger log = LoggerFactory.getLogger(UsuarioServiceImpl.class);
	
	@Resource
	private Environment environment;

	@Transactional(value="transactionManager")
	@Override
	public Usuario save(Usuario usuario) {

		verificarSeExisteUsuarioComMesmoLogin(usuario);
		verificarSeExisteUsuarioComMesmoNomeEmail(usuario);

		if (!usuario.isNew()) {
			validarGrupos(usuario, usuario.getGrupos());
			validarPropriedades(usuario, usuario.getPropriedades());
		}

		if (usuario.getPassword() != null) {
			// atualizaSenha
			usuario.setPassword(encodePassword(usuario.getPassword()));
		} else {
			// deixa senha atual
			if (usuario.getId() != null) {
				usuario.setPassword(usuarioRepository
						.findPasswordByUsuario(usuario.getId()));
			}

		}

//		validarUsuarioExterno(usuario);
		validarTiposLoginVazio(usuario);

		List<TipoLogin> tiposlogin = usuario.getTiposLogin();
		boolean isInterno = false; 
		
		for(TipoLogin tipoLogin : tiposlogin){
			
			if(tipoLogin.getTipoLoginId() == TipoUsuarioConstantes.INTERNO)
				isInterno = true;
		}
		
		
		
		if (usuario.isNew() && isInterno && usuario.getPassword() == null) {
			UUID uuid = UUID.randomUUID(); //MudanÃ§a para verificar mais que um tipo de usuÃ¡rio
			String senhaGerada = uuid.toString().substring(0, 8);
			String senhaCriptografada = encodePassword(senhaGerada);
			usuario.setPassword(senhaCriptografada);
			enviarEmail(usuario, senhaGerada, "EMAIL_CADASTRO");
		}

		usuario = usuarioRepository.save(usuario);


		return usuario;
	}

	private void enviarEmail(Usuario usuario, String senha, String nomeEmail) {
		Email email = emailService.findByIdentificador(nomeEmail);

		if (email != null) {
			String corpo = email.getCorpo();

			if (corpo.contains("{NOME_USUARIO}")) {
				corpo = corpo.replace("{NOME_USUARIO}", usuario.getNome());
			}
			
			if (corpo.contains("{EMAIL_USUARIO}")) {
				corpo = corpo.replace("{EMAIL_USUARIO}", usuario.getLogin());
			}
			
			if (corpo.contains("{SENHA_USUARIO}")) {
				corpo = corpo.replace("{SENHA_USUARIO}", senha);
			}
			
			if (corpo.contains("{URL_COMPLETAR_CADASTRO}")) {
				corpo = corpo.replace("{URL_COMPLETAR_CADASTRO}", environment.getProperty("url.completar.cadastro") + usuario.getHash());
			}

			mailService.sendMail(new String[] { usuario.getEmail() }, email.getAssunto(), corpo);
			
		}
	}

	private String encodePassword(String password) {
		return new ShaPasswordEncoder(1).encodePassword(password, null);
	}

	private void validarUsuarioExterno(Usuario usuario) {
		
		List<TipoLogin> tiposlogin = usuario.getTiposLogin();
		boolean isExterno = false; 
		
		for(TipoLogin tipoLogin : tiposlogin){
			
			if(tipoLogin.getTipoLoginId() == TipoUsuarioConstantes.EXTERNO)
				isExterno = true;
		}
		
		if (isExterno //Mudar tipo de usuÃ¡rio para verificar mais que um tipo
				&& usuario.getFonte() == null) {
			throw UsuarioException.USUARIO_EXTERNO_FONTE_OBRIGATORIO;
		}
	}

	
	private void validarTiposLoginVazio(Usuario usuario) {
		
		List<TipoLogin> tiposlogin = usuario.getTiposLogin();
		
		if(tiposlogin == null || tiposlogin.isEmpty())
			throw UsuarioException.AO_MENOS_UM_TIPO_LOGIN;
		
	}

	
	
	private void verificarSeExisteUsuarioComMesmoNomeEmail(Usuario usuario) {
		Usuario mesmoLogin = usuarioRepository.findByNomeAndEmail(
				usuario.getNome(), usuario.getEmail());

		if ((mesmoLogin != null && usuario.isNew())
				|| (mesmoLogin != null && usuario.getId() != null && !usuario
						.getId().equals(mesmoLogin.getId()))) {
			throw UsuarioException.EXISTE_USUARIO_COM_MESMO_NOME_E_EMAIL;
		}
	}

	private void verificarSeExisteUsuarioComMesmoLogin(Usuario usuario) {
		if (usuario.getLogin() != null) {
			Usuario mesmoLogin = usuarioRepository.findByLogin(usuario
					.getLogin());

			if ((mesmoLogin != null && usuario.isNew())
					|| (mesmoLogin != null && usuario.getId() != null && !usuario
							.getId().equals(mesmoLogin.getId()))) {
				throw UsuarioException.EXISTE_USUARIO_COM_MESMO_LOGIN;
			}
		}
	}

	@Transactional(value="transactionManager", readOnly = true)
	@Override
	public List<Usuario> listAll(String search, Pageable page) {
		if (search != null && !search.isEmpty()) {
			return (List<Usuario>) usuarioRepository.findAll(search, page);
		} else {
			return (List<Usuario>) usuarioRepository.findAll(page);
		}
	}

	@Transactional(value="transactionManager", readOnly = true)
	@Override
	public Usuario findById(Long id) {
		Usuario usuario = usuarioRepository.findOne(id);
		if (usuario == null) {
			throw UsuarioException.USUARIO_NAO_ENCONTRADO;
		}
		Hibernate.initialize(usuario.getFonte());
		Hibernate.initialize(usuario.getGrupos());
		Hibernate.initialize(usuario.getPropriedades());
		Hibernate.initialize(usuario.getTiposLogin());
		
		return usuario;
	}

	@Transactional(value="transactionManager", readOnly = true)
	@Override
	public Usuario findByHash(String hash) {
		Usuario usuario = usuarioRepository.findByHash(hash);
		if (usuario == null) {
			throw UsuarioException.HASH_NAO_ENCONTRADO;
		}
		Hibernate.initialize(usuario.getFonte());
		Hibernate.initialize(usuario.getGrupos());
		Hibernate.initialize(usuario.getPropriedades());
		return usuario;
	}

	@Transactional(value="transactionManager")
	@Override
	public Usuario delete(Long id) {
		Usuario usuario = findById(id);
		try {
			usuarioRepository.delete(usuario);
			em.flush();
		} catch (PersistenceException e) {
			throw new RnpException("constraint.violation.exception", 412);
		}
		return usuario;
	}

	@Transactional(value="transactionManager", readOnly = true)
	@Override
	public Usuario findByUsername(String login) {
		Usuario usuario = usuarioRepository.findByLogin(login);
		if (usuario != null) {
			Hibernate.initialize(usuario.getTiposLogin());
			Hibernate.initialize(usuario.getFonte());
			Hibernate.initialize(usuario.getPropriedades());
			Hibernate.initialize(usuario.getGrupos());
			
			//TODO retirar
			log.debug("USUARIO ENCONTRADO");
		}else
			log.debug("USUARIO NAO ENCONTRADO");
		
		return usuario;
	}

	@Transactional(value="transactionManager", readOnly = true)
	@Override
	public Long count(String search) {
		if (search != null && !search.isEmpty()) {
			return usuarioRepository.count(search);
		} else {
			return usuarioRepository.count();
		}

	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}

	private void validarGrupos(Usuario usuario, List<Grupo> grupos) {

		List<Grupo> existentes = new ArrayList<Grupo>();
		List<Grupo> toBeAdd = usuario.getGrupos();

		List<Grupo> actual = grupoRepository.findByUsuario(usuario.getId());

		if (actual != null && actual.size() > 0) {
			for (int i = actual.size() - 1; i >= 0; i--) {
				Grupo c = actual.get(i);
				if (toBeAdd != null && toBeAdd.contains(c)) {
					actual.remove(i);
					grupos.remove(c);
					existentes.add(c);
				}
			}

			for (Grupo c : actual) {
				Hibernate.initialize(c.getUsuarios());
				c.getUsuarios().remove(usuario);
			}
		}

		for (Grupo g : existentes) {
			usuario.addGrupo(g);
		}

	}

	private void validarPropriedades(Usuario usuario,
			List<PropriedadeUsuario> propriedades) {

		List<PropriedadeUsuario> existentes = new ArrayList<PropriedadeUsuario>();
		List<PropriedadeUsuario> toBeAdd = usuario.getPropriedades();

		List<PropriedadeUsuario> actual = propriedadeUsuarioRepository
				.findByUsuario(usuario.getId());

		if (actual != null && actual.size() > 0) {
			for (int i = actual.size() - 1; i >= 0; i--) {
				PropriedadeUsuario c = actual.get(i);
				if (toBeAdd != null && toBeAdd.contains(c)) {
					actual.remove(i);
					propriedades.remove(c);
					existentes.add(c);
				}
			}

			for (PropriedadeUsuario c : actual) {
				c.setUsuario(null);
			}
		}

		for (PropriedadeUsuario p : existentes) {
			usuario.addPropriedade(p);
		}

	}

	@Transactional(value="transactionManager")
	@Override
	public void alterarSenha(Long usuarioId, String atual, String nova) {
		// TODO Auto-generated method stub
		Usuario usuario = usuarioRepository.findOne(usuarioId);
		
		List<TipoLogin> tiposlogin = usuario.getTiposLogin();
		boolean isInterno = false; 
		
		for(TipoLogin tipoLogin : tiposlogin){
			
			if(tipoLogin.getTipoAcesso().equals("INTERNO"))
				isInterno = true;
		}
		
		

		if (usuario.getPassword().equals(encodePassword(atual)) && isInterno) {
			usuario.setPassword(encodePassword(nova)); //Verificar para mais que um tipo de usuÃ¡rio
			usuarioRepository.save(usuario);
		} else {
			throw UsuarioException.SENHA_ATUAL_NAO_CONFERE;
		}
	}

	@Transactional(value="transactionManager")
	@Override
	public Usuario solicitarAcesso(Usuario usuario) {
		// TODO Auto-generated method stub

		validarUsuarioExterno(usuario);
		verificarSeExisteUsuarioComMesmoLogin(usuario);
		verificarSeExisteUsuarioComMesmoNomeEmail(usuario);
		validarTiposLoginVazio(usuario);

		if (usuario.getPassword() != null) {
			usuario.setPassword(encodePassword(usuario.getPassword()));
		}

		usuario = usuarioRepository.save(usuario);

		List<Usuario> admins = usuarioRepository.findAdmins();
		
		Email email = emailService.findByIdentificador("EMAIL_SOLICITAR_ACESSO");
		
		if (admins != null && admins.size() > 0) {
			String[] emails = getEmails(admins);
			if (emails.length > 0) {
				
				if (email != null) {
					String corpo = email.getCorpo();

					if (corpo.contains("{NOME_USUARIO}")) {
						corpo = corpo.replace("{NOME_USUARIO}", usuario.getNome());
					}
					
					mailService.sendMail(new String[] { usuario.getEmail() },
							email.getAssunto(), corpo);
					
				}
			}
		}

		return usuario;
	}

	private String[] getEmails(List<Usuario> admins) {
		List<String> emails = new ArrayList<>();
		for (Usuario u : admins) {
			emails.add(u.getEmail());
		}
		String[] array = new String[emails.size()];
		emails.toArray(array);
		return array;
	}

	@Transactional(value="transactionManager")
	@Override
	public void atualizarUsuario(String senha, String hash) {

		Usuario usuario = findByHash(hash);
		
		if (usuario == null) {
			throw UsuarioException.HASH_NAO_ENCONTRADO;
		}
				
		usuario.setPassword(new ShaPasswordEncoder(1).encodePassword(senha, null));
		usuarioRepository.save(usuario);

	}

	@Override
	public List<Usuario> listAll(String search, Pageable page, Boolean status) {
		if(search != null && !search.isEmpty()){
			if(status != null){
				return (List<Usuario>) usuarioRepository.findAll(search, status, page);
			}else{
				return (List<Usuario>)  usuarioRepository.findAll(search, page);
			}
		}else{
			if(status != null){
				return (List<Usuario>)  usuarioRepository.findAll(status, page);
			}else{
				return (List<Usuario>)  usuarioRepository.findAll(page);
			}
		}
	}

	@Override
	public Long count(String search, Boolean status) {
		if(search != null && !search.isEmpty()){
			if(status != null){
				return usuarioRepository.count(search, status);
			}else{
				return usuarioRepository.count(search);
			}
		}else{
			if(status != null){
				return usuarioRepository.count(status);
			}else{
				return usuarioRepository.count();
			}
		}
	}

	@Transactional(value="transactionManager")
	@Override
	public void resetPassword(Long id) {
		// TODO Auto-generated method stub
		Usuario usuario = usuarioRepository.findOne(id);
		
		UUID uuid = UUID.randomUUID();
		String senhaGerada = uuid.toString().substring(0, 8);
		String senhaCriptografada = encodePassword(senhaGerada);
		
		usuario.setPassword(senhaCriptografada);
		usuarioRepository.save(usuario);
		enviarEmail(usuario, senhaGerada, "EMAIL_RESET");
	}

}
