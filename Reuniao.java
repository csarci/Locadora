package br.rnp.agendamento.domain;

import java.util.ArrayList;
import java.util.Collection;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Index;

import br.rnp.agendamento.enums.EquipamentoSalaEnum;

@Entity
@Table(name = "reunioes")
public class Reuniao {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="reunioes_id")
	@Index(columnNames={"reunioes_id"}, name="idx_reunioes_id_pk")
	private Long id;

	@Column(length = 255)
	private String assunto;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="data_inicial")
	private Date dataInicial;

	@Temporal(TemporalType.TIMESTAMP)
	@JoinColumn(name="data_final")
	private Date dataFinal;

	@Column(name="informacao_restrita", columnDefinition = "BOOLEAN")
	private Boolean informacaoRestrita;

	@ElementCollection(targetClass = EquipamentoSalaEnum.class)
	@JoinTable(name = "equipamentos_reuniao", joinColumns = @JoinColumn(name = "reuniao_id", referencedColumnName = "reunioes_id"))
	@Column(name = "equipamento_reuniao", nullable = false)
	@Enumerated(EnumType.STRING)
	private Collection<EquipamentoSalaEnum> equipamentos = new ArrayList<EquipamentoSalaEnum>();

	@Column(columnDefinition = "BOOLEAN")
	private Boolean gravacao;

	@OneToMany(mappedBy = "reuniao", cascade = CascadeType.ALL)
	private Set<Participante> participantes = new HashSet<Participante>();

	@ManyToMany
	@JoinTable(name = "reuniao_sala",
	joinColumns = @JoinColumn(name = "reuniao_id", referencedColumnName = "reunioes_id"),
	inverseJoinColumns = @JoinColumn(name = "sala_id", referencedColumnName = "salas_id"))
	private Set<Sala> salas;

	@Column(columnDefinition = "BOOLEAN")
	private Boolean aprovado;
	
	@Column(name="criado_por")
	private Long criadoPor;
	
	@Column(name="criado_por_nome")
	private String criadoPorNome;
	
	@Column(name="criado_email")
	private String criadoPorEmail;
	
	public Reuniao(){}
	
	public Reuniao(Reuniao re) {
		this.aprovado = re.getAprovado();
		this.assunto = re.getAssunto();
		this.criadoPor = re.getCriadoPor();
		this.criadoPorEmail = re.getCriadoPorEmail();
		this.criadoPorNome = re.getCriadoPorNome();
		this.dataFinal = re.getDataFinal();
		this.dataInicial = re.getDataInicial();
		this.gravacao = re.getGravacao();
		this.id = re.getId();
		this.informacaoRestrita = re.getInformacaoRestrita();
		this.equipamentos = re.getEquipamentos();
		this.participantes = re.getParticipantes();
		this.salas = re.getSalas();
		
		
	}

	public boolean isNew() {
		return this.id == null ? true : false;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAssunto() {
		return assunto;
	}

	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	public Boolean getInformacaoRestrita() {
		return informacaoRestrita;
	}

	public void setInformacaoRestrita(Boolean informacaoRestrita) {
		this.informacaoRestrita = informacaoRestrita;
	}

	public Boolean getGravacao() {
		return gravacao;
	}

	public void setGravacao(Boolean gravacao) {
		this.gravacao = gravacao;
	}

	public Collection<EquipamentoSalaEnum> getEquipamentos() {
		return equipamentos;
	}

	public void setEquipamentos(Collection<EquipamentoSalaEnum> equipamentos) {
		this.equipamentos = equipamentos;
	}

	public Date getDataInicial() {
		return dataInicial;
	}

	public void setDataInicial(Date dataInicial) {
		this.dataInicial = dataInicial;
	}

	public Date getDataFinal() {
		return dataFinal;
	}

	public void setDataFinal(Date dataFinal) {
		this.dataFinal = dataFinal;
	}

	public Set<Participante> getParticipantes() {
		return participantes;
	}

	public void setParticipantes(Set<Participante> participantes) {
		this.participantes = participantes;
	}

	public Set<Sala> getSalas() {
		return salas;
	}

	public void setSalas(Set<Sala> salas) {
		this.salas = salas;
	}

	public void addParticipante(Participante participante) {
		if (participantes == null) {
			participantes = new HashSet<Participante>();
		}
		participantes.add(participante);
		participante.setReuniao(this);
	}

	public void addSala(Sala sala) {
		if (salas == null) {
			salas = new HashSet<Sala>();
		}

		salas.add(sala);
	}

	public void addEquipamento(EquipamentoSalaEnum tipoEquipamento) {
		if (equipamentos == null) {
			equipamentos = new ArrayList<EquipamentoSalaEnum>();
		}

		equipamentos.add(tipoEquipamento);
	}

	public Boolean getAprovado() {
		return aprovado;
	}

	public void setAprovado(Boolean aprovado) {
		this.aprovado = aprovado;
	}

	public Long getCriadoPor() {
		return criadoPor;
	}

	public void setCriadoPor(Long criadoPor) {
		this.criadoPor = criadoPor;
	}

	public String getCriadoPorNome() {
		return criadoPorNome;
	}

	public void setCriadoPorNome(String criadoPorNome) {
		this.criadoPorNome = criadoPorNome;
	}

	public String getCriadoPorEmail() {
		return criadoPorEmail;
	}

	public void setCriadoPorEmail(String criadoPorEmail) {
		this.criadoPorEmail = criadoPorEmail;
	}

	
}
