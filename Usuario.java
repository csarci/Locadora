package br.rnp.perfis.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "usuarios", uniqueConstraints = { @UniqueConstraint(columnNames = { "login" }, name = "usuarios_login_uk") })
public class Usuario {

	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "usuarios_id")
	@Index(columnNames = { "usuarios_id" }, name = "idx_usuarios_id_pk")
	private Long id;

	@Column(name = "nome", length = 60, nullable = false)
	private String nome;

	@Column(name = "email", length = 60, nullable = false)
	private String email;

	@Column(name = "ativo", columnDefinition = "BOOLEAN")
	private Boolean ativo = false;

	@Column(name = "login", length = 60)
	private String login;

	@Column(name = "senha", length = 60)
	private String password;


	@ManyToOne
	@JoinColumn(name = "fonte_id")
	@ForeignKey(name = "usuarios_fontes_fk")
	@Index(columnNames = "fonte_id", name = "idx_fontes_id_fk")
	private Fonte fonte;

	@ForeignKey(name = "usuarios_tipos_login_fk", inverseName = "tipos_login_usuarios_fk")
	@ManyToMany
	@JoinTable(name = "usuarios_tipos_login", joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "usuarios_id"), inverseJoinColumns = @JoinColumn(name = "tipo_login_id", referencedColumnName = "tipos_login_id"))
	private List<TipoLogin> tiposLogin = new ArrayList<TipoLogin>();
	
	@ForeignKey(name = "usuarios_grupos_fk", inverseName = "grupos_usuarios_fk")
	@ManyToMany
	@JoinTable(name = "usuarios_grupos", joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "usuarios_id"), inverseJoinColumns = @JoinColumn(name = "grupo_id", referencedColumnName = "grupos_id"))
	private List<Grupo> grupos = new ArrayList<Grupo>();

	@OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<PropriedadeUsuario> propriedades = new ArrayList<PropriedadeUsuario>();

	@Column(name = "hash")
	private String hash;

	@Column(name = "admin", columnDefinition = "BOOLEAN")
	private Boolean admin = false;

	private Long organizacaoId;
	
	private Long instituicaoId;

	public Usuario() {
		super();
	}

	public Usuario(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}


	public Fonte getFonte() {
		return fonte;
	}

	public void setFonte(Fonte fonte) {
		this.fonte = fonte;
	}

	public List<Grupo> getGrupos() {
		return grupos;
	}

	public void setGrupos(List<Grupo> grupos) {
		this.grupos = grupos;
	}

	public List<PropriedadeUsuario> getPropriedades() {
		return propriedades;
	}

	public void setPropriedades(List<PropriedadeUsuario> propriedades) {
		this.propriedades = propriedades;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public Boolean getAdmin() {
		return admin;
	}

	public void setAdmin(Boolean admin) {
		this.admin = admin;
	}

	public void addGrupo(Grupo grupo) {
		if (grupos == null) {
			grupos = new ArrayList<Grupo>();
		}

		grupos.add(grupo);
	}

	public void addPropriedade(PropriedadeUsuario propriedadeUsuario) {
		if (propriedades == null) {
			propriedades = new ArrayList<PropriedadeUsuario>();
		}

		propriedades.add(propriedadeUsuario);
		propriedadeUsuario.setUsuario(this);
	}
	
	public void addTiposLogin(TipoLogin tipoLogin){
		if (tiposLogin == null){
			tiposLogin = new ArrayList<TipoLogin>();
		}
		tiposLogin.add(tipoLogin);
	}

	public boolean isNew() {
		return this.id == null ? true : false;
	}

	public Long getOrganizacaoId() {
		return organizacaoId;
	}

	public void setOrganizacaoId(Long organizacaoId) {
		this.organizacaoId = organizacaoId;
	}

	public Long getInstituicaoId() {
		return instituicaoId;
	}

	public void setInstituicaoId(Long instituicaoId) {
		this.instituicaoId = instituicaoId;
	}

	/**
	 * @return the tiposLogin
	 */
	public List<TipoLogin> getTiposLogin() {
		return tiposLogin;
	}

	/**
	 * @param tiposLogin the tiposLogin to set
	 */
	public void setTiposLogin(List<TipoLogin> tiposLogin) {
		this.tiposLogin = tiposLogin;
	}

}
