package br.rnp.agendamento.domain;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import br.rnp.agendamento.enums.AdministracaoSalaEnum;
import br.rnp.agendamento.enums.DiaEnum;
import br.rnp.agendamento.enums.EquipamentoSalaEnum;
import br.rnp.agendamento.enums.TipoSalaEnum;
import br.rnp.agendamento.enums.TipoUsuarioEnum;

@Entity
@Table(name = "salas", uniqueConstraints= @UniqueConstraint(columnNames = { "nome" }))
public class Sala {	
	
	public Sala(Sala sala){
		this.administracao = sala.getAdministracao();
		this.ativo =  sala.getAtivo();
		this.capacidade = sala.getCapacidade();
		this.confirmacao = sala.getConfirmacao();
		this.contatos = sala.getContatos();
		this.dias = sala.getDias();
		this.endereco = sala.getEndereco();
		this.equipamentos = sala.getEquipamentos();
		this.horaFinal = sala.getHoraFinal();
		this.horaInicial = sala.getHoraInicial();
		this.id = sala.getId();
		this.instituicaoId = sala.getInstituicaoId();
		this.instituicaoNome = sala.getInstituicaoNome();
		this.integradas = sala.getIntegradas();
		this.limiteMaximo = sala.getLimiteMaximo();
		this.nome = sala.getNome();
		this.organizacaoId = sala.getOrganizacaoId();
		this.organizacaoNome = sala.getOrganizacaoNome();
		this.responsaveis = sala.getResponsaveis();
		this.tipo = sala.getTipo();
		this.visibilidade = sala.getVisibilidade();
		this.naoReservavelFeriado = sala.getNaoReservavelFeriado();
		this.cor = sala.getCor();
	}
	
	public Sala() {
		super();
		// TODO Auto-generated constructor stub
	}	
	
	public Sala(Long id) {
		this.id = id;
	}
	
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="salas_id")
	@Index(columnNames={"salas_id"}, name="idx_salas_id_pk")
	private Long id;

	@Column(length = 60, nullable = false)
	private String nome;

	@ElementCollection(targetClass = EquipamentoSalaEnum.class)
	@JoinTable(name = "equipamento_sala", joinColumns = @JoinColumn(name = "sala_id", referencedColumnName = "salas_id"))
	@Enumerated(EnumType.STRING)
	private Set<EquipamentoSalaEnum> equipamentos = new HashSet<EquipamentoSalaEnum>();
	
	@ElementCollection(targetClass = DiaEnum.class)
	@JoinTable(name = "dia_sala", joinColumns = @JoinColumn(name = "sala_id", referencedColumnName = "salas_id"))
	@Enumerated(EnumType.STRING)
	private Set<DiaEnum> dias = new HashSet<>();

	@Column(name="hora_inicial")
	@Temporal(TemporalType.TIME)
	private Date horaInicial;

	@Column(name="hora_final")
	@Temporal(TemporalType.TIME)
	private Date horaFinal;

	@Enumerated(EnumType.STRING)
	private TipoUsuarioEnum visibilidade;

	private Integer capacidade;

	private Boolean confirmacao;

	@Column(name="limite_maximo")
	private Integer limiteMaximo;
	
	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="endereco_id")
	@ForeignKey(name="sala_endereco_fk")
	@Index(columnNames={"endereco_id"}, name="idx_sala_endereco_fk")
	private Endereco endereco;

	@OneToMany(mappedBy = "sala", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Contato> contatos = new HashSet<>();
	
	@OneToMany(mappedBy = "sala", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<Responsavel> responsaveis = new HashSet<>();
	
	@Column(name="organizacao_id")
	private Long organizacaoId;
	
	@Column(name="organizacao_nome")
	private String organizacaoNome;
	
	@Column(name="instituicao_nome")
	private String instituicaoNome;
	
	@Column(name="instituicao_id")
	private Long instituicaoId;
	
	private Boolean ativo = true;
	
	@Column(nullable=false)
	@Enumerated(EnumType.STRING)
	private TipoSalaEnum tipo;
	
	@ManyToMany
	@JoinTable(name = "salas_integradas", 
	            joinColumns = { @JoinColumn(name = "salas_id")}, 
	            inverseJoinColumns={@JoinColumn(name="integrada_id")})  
	private Set<Sala> integradas;
	
	@Column(name="administracao", nullable=false)
	@Enumerated(EnumType.STRING)
	private AdministracaoSalaEnum administracao;
		
	private Boolean naoReservavelFeriado = false;
	
	@Column(name="cor")
	private String cor;
	
	public String getCor() {
		return cor;
	}

	public void setCor(String cor) {
		this.cor = cor;
	}

	public Boolean getNaoReservavelFeriado() {
		return naoReservavelFeriado;
	}
	
	public void setNaoReservavelFeriado(Boolean naoReservavelFeriado) {
		this.naoReservavelFeriado = naoReservavelFeriado;
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

	public Set<EquipamentoSalaEnum> getEquipamentos() {
		return equipamentos;
	}

	public void setEquipamentos(Set<EquipamentoSalaEnum> equipamentos) {
		this.equipamentos = equipamentos;
	}

	public Set<DiaEnum> getDias() {
		return dias;
	}

	public void setDias(Set<DiaEnum> dias) {
		this.dias = dias;
	}

	public Date getHoraInicial() {
		return horaInicial;
	}

	public void setHoraInicial(Date horaInicial) {
		this.horaInicial = horaInicial;
	}

	public Date getHoraFinal() {
		return horaFinal;
	}

	public void setHoraFinal(Date horaFinal) {
		this.horaFinal = horaFinal;
	}

	public TipoUsuarioEnum getVisibilidade() {
		return visibilidade;
	}

	public void setVisibilidade(TipoUsuarioEnum visibilidade) {
		this.visibilidade = visibilidade;
	}

	public Integer getCapacidade() {
		return capacidade;
	}

	public void setCapacidade(Integer capacidade) {
		this.capacidade = capacidade;
	}

	public Boolean getConfirmacao() {
		return confirmacao;
	}

	public void setConfirmacao(Boolean confirmacao) {
		this.confirmacao = confirmacao;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}
	
	public Set<Contato> getContatos() {
		return contatos;
	}

	public void setContatos(Set<Contato> contatos) {
		this.contatos = contatos;
	}
	
	public Boolean getAtivo() {
		return ativo;
	}

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}
	
	public Set<Responsavel> getResponsaveis() {
		return responsaveis;
	}

	public void setResponsaveis(Set<Responsavel> responsaveis) {
		this.responsaveis = responsaveis;
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

	public TipoSalaEnum getTipo() {
		return tipo;
	}

	public void setTipo(TipoSalaEnum tipo) {
		this.tipo = tipo;
	}
	
	public String getOrganizacaoNome() {
		return organizacaoNome;
	}

	public void setOrganizacaoNome(String organizacaoNome) {
		this.organizacaoNome = organizacaoNome;
	}

	public String getInstituicaoNome() {
		return instituicaoNome;
	}

	public void setInstituicaoNome(String instituicaoNome) {
		this.instituicaoNome = instituicaoNome;
	}

	public void addContato(Contato c) {
		// TODO Auto-generated method stub
		if(this.contatos == null){
			this.contatos = new HashSet<>();
		}
		this.contatos.add(c);
		c.setSala(this);
	}

	public boolean isNew() {
		return this.id != null ? false : true;
	}

	public void addResponsavel(Responsavel r) {
		// TODO Auto-generated method stub
		if(this.responsaveis == null){
			this.responsaveis = new HashSet<>();
		}
		this.responsaveis.add(r);
		r.setSala(this);
	}

	public Set<Sala> getIntegradas() {
		return integradas;
	}

	public void setIntegradas(Set<Sala> integradas) {
		this.integradas = integradas;
	}

	public void addIntegrada(Sala s) {
		// TODO Auto-generated method stub
		if(this.integradas == null){
			this.integradas = new HashSet<Sala>();
		}
		this.integradas.add(s);
	}
	
	public AdministracaoSalaEnum getAdministracao() {
		return administracao;
	}

	public void setAdministracao(AdministracaoSalaEnum administracao) {
		this.administracao = administracao;
	}
	
	public Integer getLimiteMaximo() {
		return limiteMaximo;
	}
	public void setLimiteMaximo(Integer limiteMaximo) {
		this.limiteMaximo = limiteMaximo;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Sala other = (Sala) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}


	
	
}
