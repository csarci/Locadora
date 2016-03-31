package br.rnp.agendamento.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.rnp.agendamento.domain.Email;
import br.rnp.agendamento.domain.Feriado;
import br.rnp.agendamento.domain.Responsavel;
import br.rnp.agendamento.domain.Reuniao;
import br.rnp.agendamento.domain.Sala;
import br.rnp.agendamento.enums.EquipamentoSalaEnum;
import br.rnp.agendamento.enums.TipoSalaEnum;
import br.rnp.agendamento.exception.ReuniaoException;
import br.rnp.agendamento.exception.RnpException;
import br.rnp.agendamento.exception.SalaException;
import br.rnp.agendamento.repository.ReuniaoRepository;
import br.rnp.agendamento.service.CidadeService;
import br.rnp.agendamento.service.EmailService;
import br.rnp.agendamento.service.EstadoService;
import br.rnp.agendamento.service.FeriadoService;
import br.rnp.agendamento.service.MailService;
import br.rnp.agendamento.service.ReuniaoService;
import br.rnp.agendamento.service.SalaService;

@Service
public class ReuniaoServiceImpl implements ReuniaoService {

	@Autowired
	private ReuniaoRepository reuniaoRepository;

	@Autowired
	private SalaService salaService;

	@Autowired
	private FeriadoService feriadoService;

	@Autowired
	private EstadoService estadoService;

	@Autowired
	private CidadeService cidadeService;

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private EmailService emailService;

	@Autowired
	private MailService mailService;

	@Resource
	private Environment env;

	private static final String EMAIL_SERVICE_DESK = "email.service.desk";

	SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	

	//@Transactional
	@Override
	public Reuniao save(Reuniao reuniao) {
		
		// if(reuniao.isNew()){
		
		validarConflitos(reuniao);
		// }

	//	int tempoReuniao = Minutes.minutesBetween(new DateTime(reuniao.getDataInicial()), new DateTime(reuniao.getDataFinal())).getMinutes();

		int tempoReuniao = Hours.hoursBetween(new DateTime(reuniao.getDataInicial()), new DateTime(reuniao.getDataFinal())).getHours();
		boolean confirmacao = false;

		List<String> responsaveis = new ArrayList<String>();
		
		String salasVirtuais = new String();
		int contadorSalasVirtuais = 0;
		
		verificaAgendamentoSalaFeriado(reuniao);
		
		//AlteraÃ§Ãµes realizadas para corrigir o erro do ticket #345548
		List<Sala> salasIntegradas = new ArrayList<Sala>();

		if (reuniao.getSalas() != null) {
			for (Sala s : reuniao.getSalas()) {
				
				Sala sala = salaService.findById(s.getId());
				
				if (!sala.getIntegradas().isEmpty()) {
					for (Sala si : sala.getIntegradas()) {
						salasIntegradas.add(si);
					}
				}				
			}
			for (Sala salaIntegrada : salasIntegradas) {
				reuniao.addSala(salaIntegrada);
			}
		}		

		if (reuniao.getSalas() != null) {
			for (Sala s : reuniao.getSalas()) {			

				Sala sala = salaService.findById(s.getId());
				
				if (sala.getResponsaveis() != null) {
					for (Responsavel r : sala.getResponsaveis()) {
						if (r.getEmail() != null) {
							responsaveis.add(r.getEmail());
						}
					}
				}

//				if (!sala.getIntegradas().isEmpty()) {
//					for (Sala si : sala.getIntegradas()) {
//						reuniao.addSala(si);
//					}
//				}
				
				if (tempoReuniao > sala.getLimiteMaximo()) {
					throw SalaException.EXCEDEU_LIMITE_TEMPO_SALA;
				}

				if (sala.getConfirmacao() != null && sala.getConfirmacao()) {
					confirmacao = true;
				}
				
				if (sala.getAdministracao().toString().equals("MCU")) {
					salasVirtuais = salasVirtuais + ("\n" + sala.getNome() + "\n" + 
							 "IP: " + sala.getEndereco().getIp() + "\n" +
							 "ID: " + sala.getEndereco().getIdmcu() + "\n" +
			  				 "ISDN: " + sala.getEndereco().getIsdnmcu() + "\n");
					contadorSalasVirtuais = contadorSalasVirtuais + 1;
				}
			}
			if (contadorSalasVirtuais > 0) {
				salasVirtuais = salasVirtuais + ("\n" + 
					 "InformaÃ§Ãµes TÃ©cnicas para acesso a sala virtual - Siga as instruÃ§Ãµes:" + "\n\n" +
					 "1 - Registre o seu cliente H.323 no gatekeeper 200.130.35.15, utilizando como identificador <cod_pais><cod_cidade_sem_zero><nÂº_telefone>" + "\n\n" +
					 "Por exemplo: para o nÂº. telefÃ´nico 3205-9660 e cÃ³digo cidade 21, o identificador deve ser 552132059660" + "\n\n" +
					 "2 - Feito o registro, faÃ§a a chamada usando o ID da sala que se encontra acima nesta mensagem." + "\n");
			}
		}

		verificaDataPassada(reuniao.getDataInicial(), reuniao.getDataFinal());		
		
		if (confirmacao) {
			reuniao.setAprovado(null);
			
			Email email = emailService.findByIdentificador("EMAIL_CONFIRMACAO");

			if (email != null) {
				String corpo = email.getCorpo();
				if (corpo.contains("{REUNIAO_ASSUNTO}")) {
					corpo = corpo.replace("{REUNIAO_ASSUNTO}", reuniao.getAssunto());
				}

				if (corpo.contains("{DATA_INICIAL}")) {
					corpo = corpo.replace("{DATA_INICIAL}", dt.format(reuniao.getDataInicial()));
				}

				if (corpo.contains("{DATA_FINAL}")) {
					corpo = corpo.replace("{DATA_FINAL}", dt.format(reuniao.getDataFinal()));
				}

				if (responsaveis.size() > 0) {
					mailService.sendMail(responsaveis.toArray(new String[responsaveis.size()]), email.getAssunto(), corpo);
				}
			}
			
			if (reuniao.getSalas() != null) {
				
				List<String> salas = new ArrayList<String>();
				List<String> nomes = new ArrayList<String>();
				String descricoesSalas = new String();
				String nomesSalas = new String();
				
				for (Sala s : reuniao.getSalas()) {
					Sala sala = salaService.findById(s.getId());
					String descricaoSala = sala.getAdministracao().toString() + " - " + sala.getNome();
					
					if (descricaoSala.contains("PRESENCIAL")) {
						descricaoSala = descricaoSala.replace("PRESENCIAL", "Presencial");
					}
					if (descricaoSala.contains("MCU")) {
						descricaoSala = descricaoSala.replace("MCU", "Virtual");
					}
					if (descricaoSala.contains("VIRTUAL")) {
						descricaoSala = descricaoSala.replace("VIRTUAL", "TelepresenÃ§a");
					}
					
					salas.add(descricaoSala);
					nomes.add(sala.getNome());
				}
				
				for (int i = 0; i < salas.size(); i++) {
					descricoesSalas = descricoesSalas + salas.get(i);
					if (i < (salas.size() - 1)){
						if (i == (salas.size() - 2)){
							descricoesSalas = descricoesSalas + " e ";
						} else {
							descricoesSalas = descricoesSalas + ", ";
						}
					}
				}
				
				for (int i = 0; i < nomes.size(); i++) {
					nomesSalas = nomesSalas + nomes.get(i);
					if (i < (nomes.size() - 1)){
						if (i == (nomes.size() - 2)){
							nomesSalas = nomesSalas + " e ";
						} else {
							nomesSalas = nomesSalas + ", ";
						}
					}												
				}
					
				Email emailConfirmacaoPendente = emailService.findByIdentificador("EMAIL_CONFIRMACAO_REUNIAO_PENDENTE");

				if (emailConfirmacaoPendente != null) {
					String corpo = emailConfirmacaoPendente.getCorpo();
					
					if (corpo.contains("{SALAS}")) {
						corpo = corpo.replace("{SALAS}", descricoesSalas);
					}
					
					if (corpo.contains("{NOME_SOLICITANTE}")) {
						corpo = corpo.replace("{NOME_SOLICITANTE}", reuniao.getCriadoPorNome());
					}

					if (corpo.contains("{DATA_INICIAL}")) {
						corpo = corpo.replace("{DATA_INICIAL}", dt.format(reuniao.getDataInicial()));
					}

					if (corpo.contains("{DATA_FINAL}")) {
						corpo = corpo.replace("{DATA_FINAL}", dt.format(reuniao.getDataFinal()));
					}
					
					String assuntoConfirmacaoPendente = emailConfirmacaoPendente.getAssunto();
					if (assuntoConfirmacaoPendente.contains("{NOME_SALA}")) {
						assuntoConfirmacaoPendente = assuntoConfirmacaoPendente.replace("{NOME_SALA}", nomesSalas);
					}

					if (reuniao.getCriadoPorEmail() != null) {
						mailService.sendMailSolicitante(reuniao.getCriadoPorEmail(), assuntoConfirmacaoPendente, corpo);
					}
				}
			}
		} else {
			
			enviaEmailAprovacaoOuReprovacao(reuniao, true);
		}

		if (requerGravacao(reuniao)) {
			Email email = emailService.findByIdentificador("EMAIL_GRAVACAO");
			
			String nomesSalasGravacao = new String();
			
			if (reuniao.getSalas() != null) {
				for (Sala s : reuniao.getSalas()) {
					Sala sala = salaService.findById(s.getId());
					nomesSalasGravacao = nomesSalasGravacao + (sala.getNome() + "\n");
				}
			}

			if (email != null) {
				String corpo = email.getCorpo();
				if (corpo.contains("{REUNIAO_ASSUNTO}")) {
					corpo = corpo.replace("{REUNIAO_ASSUNTO}", reuniao.getAssunto());
				}

				if (corpo.contains("{DATA_INICIAL}")) {
					corpo = corpo.replace("{DATA_INICIAL}", dt.format(reuniao.getDataInicial()));
				}

				if (corpo.contains("{DATA_FINAL}")) {
					corpo = corpo.replace("{DATA_FINAL}", dt.format(reuniao.getDataFinal()));
				}
				
				if (corpo.contains("{SALAS_GRAVACAO}")) {
					corpo = corpo.replace("{SALAS_GRAVACAO}", nomesSalasGravacao);
				}
				
				if (corpo.contains("{INFORMACOES_GERAIS}")) {
					corpo = corpo.replace("{INFORMACOES_GERAIS}", informacoesGeraisDaReuniao(reuniao));
				}

				mailService.sendMail(new String[] { env.getRequiredProperty(EMAIL_SERVICE_DESK) }, email.getAssunto(), corpo);
			}
		}

		return reuniaoRepository.save(reuniao);
	}

	private boolean requerGravacao(Reuniao reuniao) {
		// TODO Auto-generated method stub
		for (EquipamentoSalaEnum e : reuniao.getEquipamentos()) {
			if (e.equals(EquipamentoSalaEnum.GRAVACAO)) {
				return true;
			}
		}
		return false;
	}
	
	private void verificaAgendamentoSalaFeriado(Reuniao reuniao) {
		for (Sala s : reuniao.getSalas()) {
			Sala sala = salaService.findById(s.getId());

			Boolean flag = sala.getNaoReservavelFeriado();
			if (flag && sala.getTipo().equals(TipoSalaEnum.SALA)){
				List<Feriado> feriados = new ArrayList<>();
				LocalDate dataInicial = new LocalDate(reuniao.getDataInicial());
				LocalDate dataFinal = new LocalDate(reuniao.getDataFinal());
				Integer dias = Days.daysBetween(dataInicial, dataFinal).getDays();
				int i = 0;
				if (dias > 0) {
					for (i = 0; i <= dias; i++) {
						LocalDate data = new LocalDate(dataInicial).plusDays(i);
						feriados.addAll(feriadoService.listAll(null, data.getMonthOfYear(), data.getDayOfMonth(), null,null,null));
						if(sala.getEndereco() != null){
							feriados.addAll(feriadoService.listAll(null, data.getMonthOfYear(), data.getDayOfMonth(), null,sala.getEndereco().getCidade().getEstado().getId(),null));
							feriados.addAll(feriadoService.listAll(null, data.getMonthOfYear(), data.getDayOfMonth(), null, sala.getEndereco().getCidade().getEstado().getId(), sala.getEndereco().getCidade().getId()));
						}
					}
				} else {
					feriados.addAll(feriadoService.listAll(null, dataInicial.getMonthOfYear(), dataInicial.getDayOfMonth(), null,null,null));
					if(sala.getEndereco() != null){
						feriados.addAll(feriadoService.listAll(null, dataInicial.getMonthOfYear(), dataInicial.getDayOfMonth(), null,sala.getEndereco().getCidade().getEstado().getId(),null));
						feriados.addAll(feriadoService.listAll(null, dataInicial.getMonthOfYear(), dataInicial.getDayOfMonth(), null, sala.getEndereco().getCidade().getEstado().getId(), sala.getEndereco().getCidade().getId()));
					}
					
				}

				if (feriados.size() > 0) {
					throw SalaException.NAO_RESERVAVEL_FERIADO;
				}

			}
		}

	}

	@Transactional(readOnly = true)
	@Override
	public Reuniao findById(Long id) {
		Reuniao reuniao = reuniaoRepository.findOne(id);
		if (reuniao == null) {
			throw ReuniaoException.REUNIAO_NAO_ENCONTRADA;
		}
		Hibernate.initialize(reuniao.getEquipamentos());
		Hibernate.initialize(reuniao.getParticipantes());
		Hibernate.initialize(reuniao.getSalas());
		return reuniao;
	}

	@Transactional
	@Override
	public Reuniao delete(Long id) {
		
		Reuniao reuniao = findById(id);
		
		try {
			
			List<String> responsaveis = new ArrayList<String>();
			
			if (reuniao.getSalas() != null) {
				for (Sala s : reuniao.getSalas()) {			

					Sala sala = salaService.findById(s.getId());
					
					if (sala.getResponsaveis() != null) {
						for (Responsavel r : sala.getResponsaveis()) {
							if (r.getEmail() != null) {
								responsaveis.add(r.getEmail());
							}
						}
					}
				}
			}

			if(requerGravacao(reuniao)){
				
				Email emailConfirmacaoeExclusaoReservaGravacao = emailService.findByIdentificador("EMAIL_EXCLUSAO_GRAVACAO");
				String corpo = emailConfirmacaoeExclusaoReservaGravacao.getCorpo();
				
				if (corpo.contains("{NOME_SALA}")) {
					corpo = corpo.replace("{NOME_SALA}", retornaNomeSalaMCU(reuniao));
				}

				if (corpo.contains("{DATA_INICIAL}")) {
					corpo = corpo.replace("{DATA_INICIAL}", dt.format(reuniao.getDataInicial()));
				}

				if (corpo.contains("{DATA_FINAL}")) {
					corpo = corpo.replace("{DATA_FINAL}", dt.format(reuniao.getDataFinal()));
				}
				
				mailService.sendMail(new String[] { env.getRequiredProperty(EMAIL_SERVICE_DESK) },emailConfirmacaoeExclusaoReservaGravacao.getAssunto(), corpo);
				
			}
			if (reuniao.getCriadoPorEmail() != null) {
				
				Email emailExclusao = emailService.findByIdentificador("EMAIL_EXCLUSAO");
				String corpoExclusao = emailExclusao.getCorpo();
				
				
				if (corpoExclusao.contains("{REUNIAO_ASSUNTO}")) {
					corpoExclusao = corpoExclusao.replace("{REUNIAO_ASSUNTO}", reuniao.getAssunto());
				}

				if (corpoExclusao.contains("{DATA_INICIAL}")) {
					corpoExclusao = corpoExclusao.replace("{DATA_INICIAL}", dt.format(reuniao.getDataInicial()));
				}

				if (corpoExclusao.contains("{DATA_FINAL}")) {
					corpoExclusao = corpoExclusao.replace("{DATA_FINAL}", dt.format(reuniao.getDataFinal()));
				}
				
				if (corpoExclusao.contains("{INFORMACOES_GERAIS}")) {
					corpoExclusao = corpoExclusao.replace("{INFORMACOES_GERAIS}", informacoesGeraisDaReuniao(reuniao));
				}
							
				if (responsaveis.size() > 0) {
					responsaveis.add(reuniao.getCriadoPorEmail());
					mailService.sendMail(responsaveis.toArray(new String[responsaveis.size()]), emailExclusao.getAssunto(), corpoExclusao);
				} else {
					mailService.sendMailSolicitante(reuniao.getCriadoPorEmail(), emailExclusao.getAssunto(), corpoExclusao);
				}
			}			
			
			reuniaoRepository.delete(reuniao);
			em.flush();
		} catch (PersistenceException e) {
			throw new RnpException("constraint.violation.exception", 412);
		}

		return reuniao;
	}

	@Transactional
	@Override
	public Reuniao changeStatus(Long id, Boolean b) {
		Reuniao reuniao = findById(id);
		reuniao.setAprovado(b);
		return reuniaoRepository.save(reuniao);
	}

	@Transactional(readOnly = true)
	@SuppressWarnings("unchecked")
	@Override
	public List<Reuniao> verificarConflitosDeHorarios(List<Sala> listSalas, Date dataInicial, Date dataFinal) {
		// TODO Auto-generated method stub

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> criteria = cb.createQuery(Reuniao.class);
		Root<Reuniao> from = criteria.from(Reuniao.class);
		from.fetch("salas");
		criteria.distinct(true);

		List<Predicate> conditions = new ArrayList<Predicate>();

		if (listSalas != null && listSalas.size() > 0) {
			Join<Reuniao, Sala> salas = from.join("salas");
			conditions.add(salas.in(listSalas));
		}

		if (dataInicial != null && dataFinal != null) {
			
			//Correcao dos conflitos de horarios do filtro para busca de reunioes
			//Ticket 334073
			Calendar calendar = Calendar.getInstance(); 	      
		    calendar.setTime(dataInicial);  
		    calendar.add(Calendar.SECOND, 1);  
		    Date dataInicialIncrementada = calendar.getTime(); 
		    
		    Calendar calendar2 = Calendar.getInstance(); 	      
		    calendar2.setTime(dataFinal);  
		    calendar2.add(Calendar.SECOND, -1);  
		    Date dataFinalDecrementada = calendar2.getTime();
		    
		    conditions.add(cb.or(cb.and(cb.lessThanOrEqualTo(from.<Date> get("dataInicial"), dataInicialIncrementada), cb.greaterThanOrEqualTo(from.<Date> get("dataFinal"), dataInicialIncrementada)),
					       cb.and(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicialIncrementada, dataFinalDecrementada), cb.between(from.<Date> get("dataFinal"), dataInicialIncrementada, dataFinalDecrementada)))));
			
			//conditions.add(cb.or(cb.and(cb.lessThanOrEqualTo(from.<Date> get("dataInicial"), dataInicial), cb.greaterThanOrEqualTo(from.<Date> get("dataFinal"), dataInicial)),
			//			   cb.and(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)))));
			//cb.and(cb.lessThanOrEqualTo(from.<Date> get("dataInicial"), dataInicial), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal))));			
			//conditions.add(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)));
		}

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		return (List<Reuniao>) query.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Reuniao> list(Date dataInicial, Date dataFinal, Long id, Sala sala) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> criteria = cb.createQuery(Reuniao.class);
		Root<Reuniao> from = criteria.from(Reuniao.class);
		from.fetch("salas");
		criteria.distinct(true);
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (dataInicial != null && dataFinal != null) {
			conditions.add(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)));
		}

		if (id != null) {
			Predicate condition = cb.equal(from.get("criadoPor"), id);
			conditions.add(condition);
		}

		if (sala != null) {
			Expression<Collection<Sala>> salas = from.get("salas");
			Predicate condition = cb.isMember(sala, salas);
			conditions.add(condition);
		}

		conditions.add(cb.isTrue(from.<Boolean> get("aprovado")));

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		List<Reuniao> reunioes = (List<Reuniao>) query.getResultList();
		return reunioes;

	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Reuniao> listTodas(Date dataInicial, Date dataFinal, Long id, Sala sala) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> criteria = cb.createQuery(Reuniao.class);
		Root<Reuniao> from = criteria.from(Reuniao.class);
		from.fetch("salas");
		criteria.distinct(true);
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (dataInicial != null && dataFinal != null) {
			conditions.add(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)));
		}

		if (id != null) {
			Predicate condition = cb.equal(from.get("criadoPor"), id);
			conditions.add(condition);
		}

		if (sala != null) {
			Expression<Collection<Sala>> salas = from.get("salas");
			Predicate condition = cb.isMember(sala, salas);
			conditions.add(condition);
		}

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		List<Reuniao> reunioes = (List<Reuniao>) query.getResultList();
		return reunioes;

	}

	private void validarConflitos(Reuniao reuniao) {
		// TODO Auto-generated method stub

		List<Reuniao> reunioes = verificarConflitosDeHorarios(new ArrayList<Sala>(reuniao.getSalas()), reuniao.getDataInicial(), reuniao.getDataFinal());

		if (reunioes != null && reunioes.size() > 0) {
			Reuniao reuniaoEncontrada = new Reuniao();
			reuniaoEncontrada = reunioes.get(0);
			if (!reuniaoEncontrada.getId().equals(reuniao.getId())) {
				throw ReuniaoException.JA_EXISTE_REUNIAO_NESSA_DATA_HORARIO;
			}
		}

	}

	@Override
	@Transactional
	public List<Reuniao> listTipo(EquipamentoSalaEnum tipoSala) {
		List<Reuniao> reunioes = em.createQuery(" select r from Reuniao r where exists (select e from r.equipamentos e where e = :tipoSala) ", Reuniao.class).setParameter("tipoSala", tipoSala)
				.getResultList();
		return reunioes;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Reuniao> listRelatorio(Date dataInicial, Date dataFinal, EquipamentoSalaEnum equipamento, Long organizacaoId, Long instituicaoId) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> criteria = cb.createQuery(Reuniao.class);
		Root<Reuniao> from = criteria.from(Reuniao.class);
		from.fetch("salas");
		from.fetch("equipamentos");
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (dataInicial != null && dataFinal != null) {
			conditions.add(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)));

		}

		if (equipamento != null) {
			Join<Reuniao, EquipamentoSalaEnum> equipamentos = from.joinCollection("equipamentos");
			conditions.add(equipamentos.in(equipamento));
		}

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);
		criteria.distinct(true);

		Query query = em.createQuery(criteria);
		List<Reuniao> reunioes = (List<Reuniao>) query.getResultList();
		return reunioes;

	}

	@Transactional(readOnly = true)
	@Override
	public List<Reuniao> listParaAprovacao(Long usuarioId, Pageable page) {
		// TODO Auto-generated method stub
		return reuniaoRepository.findByAprovadoIsNull(usuarioId, page);
	}

	@Transactional
	@Override
	public Reuniao changeAprovacao(Long id, Boolean aprovacao) {
		
		Reuniao reuniao = enviaEmailAprovacaoOuReprovacao(findById(id), aprovacao);
		
		return reuniaoRepository.save(reuniao);
	}

	private Reuniao enviaEmailAprovacaoOuReprovacao(Reuniao reuniao, Boolean aprovacao) {
	
		reuniao.setAprovado(aprovacao);
		
		if (reuniao.getSalas() != null) {
			
			List<String> salas = new ArrayList<String>();
			List<String> nomes = new ArrayList<String>();
			String descricoesSalas = new String();
			String nomesSalas = new String();
			
			String enderecosSalasFisicas = new String();
			int contadorEnderecosSalasFisicas = 0;
			
			String responsaveisSalas = new String();
			int contadorResponsaveis = 0;
			
			int contadorSalas = reuniao.getSalas().size();
			
			String salasVirtuais = new String();
			int contadorSalasVirtuais = 0;
			
			for (Sala s : reuniao.getSalas()) {
				Sala sala = salaService.findById(s.getId());
				String descricaoSala = sala.getAdministracao().toString() + " - " + sala.getNome();
				
				if (descricaoSala.contains("PRESENCIAL")) {
					descricaoSala = descricaoSala.replace("PRESENCIAL", "Presencial");
				}
				if (descricaoSala.contains("MCU")) {
					descricaoSala = descricaoSala.replace("MCU", "Virtual");
				}
				if (descricaoSala.contains("VIRTUAL")) {
					descricaoSala = descricaoSala.replace("VIRTUAL", "TelepresenÃ§a");
				}				
				salas.add(descricaoSala);
				nomes.add(sala.getNome());
				
				if (sala.getAdministracao().toString().equals("PRESENCIAL")  || 
					sala.getAdministracao().toString().equals("VIRTUAL")) { //|| sala.getAdministracao().toString().equals("MCU")  ) {
					
					String complemento = "";
					
					if (sala.getEndereco() != null) {

						if (sala.getEndereco().getComplemento() != null) {
							complemento = ", "
									+ sala.getEndereco().getComplemento();
						}

						enderecosSalasFisicas = enderecosSalasFisicas
								+ ("\n"
										+ sala.getNome()
										+ "\n"
										+ sala.getEndereco().getLogradouro()
										+ complemento
										+ "\n"
										+ sala.getEndereco().getBairro()
										+ ", "
										+ sala.getEndereco().getCidade()
												.getNome() + " - "
										+ sala.getEndereco().getUf().getUf()
										+ "\n" + sala.getEndereco().getCep() + "\n");

						contadorEnderecosSalasFisicas = contadorEnderecosSalasFisicas + 1;

					}
				}
				
				if (sala.getResponsaveis() != null && !sala.getAdministracao().toString().equals("MCU")) {
					for (Responsavel r : sala.getResponsaveis()) {
						if (r.getEmail() != null) {
							if (contadorSalas > 1) {
								if (contadorResponsaveis > 0) {
									responsaveisSalas = responsaveisSalas + "\n";
								}
								responsaveisSalas = responsaveisSalas + "\n" + sala.getNome() + "\n" + "Nome: " +  r.getNome() + "\n" + "E-mail: " + r.getEmail();
							} else {
								if (contadorResponsaveis > 0) {
									responsaveisSalas = responsaveisSalas + "\n";
								}
								responsaveisSalas = responsaveisSalas + "\n" + "Nome: " +  r.getNome() + "\n" + "E-mail: " + r.getEmail();
							}
						}
						contadorResponsaveis = contadorResponsaveis + 1;
					}
				}
				
				if (sala.getAdministracao().toString().equals("MCU")) {
					salasVirtuais = salasVirtuais + ("\n" + sala.getNome() + "\n" + 
													 "IP: " + sala.getEndereco().getIp() + "\n" +
													 "ID: " + sala.getEndereco().getIdmcu() + "\n" +
									  				 "ISDN: " + sala.getEndereco().getIsdnmcu() + "\n");
					
					contadorSalasVirtuais = contadorSalasVirtuais + 1;
				}
			}
			
			if (contadorEnderecosSalasFisicas > 0) {
				enderecosSalasFisicas = ("\n" + "O(s) endereÃ§o(s) da(s) sala(s) Ã©(sÃ£o) o(s) seguinte(s):") + enderecosSalasFisicas;
			}
			
			if (contadorResponsaveis > 0) {
				responsaveisSalas = ("Contato(s) do(s) responsÃ¡vel(is) pela(s) sala(s):") + responsaveisSalas;
			}
			
			if (contadorSalasVirtuais > 0) {
				salasVirtuais = salasVirtuais + ("\n" +
					 "InformaÃ§Ãµes TÃ©cnicas para acesso a sala virtual - Siga as instruÃ§Ãµes:" + "\n\n" +
					 "1 - Registre o seu cliente H.323 no gatekeeper 200.130.35.15, utilizando como identificador <cod_pais><cod_cidade_sem_zero><nÂº_telefone>" + "\n\n" +
					 "Por exemplo: para o nÂº. telefÃ´nico 3205-9660 e cÃ³digo cidade 21, o identificador deve ser 552132059660" + "\n\n" +
					 "2 - Feito o registro, faÃ§a a chamada usando o ID da sala que se encontra acima nesta mensagem." + "\n");
			}		
				
			for (int i = 0; i < salas.size(); i++) {
				descricoesSalas = descricoesSalas + salas.get(i);
				if (i < (salas.size() - 1)){
					if (i == (salas.size() - 2)){
						descricoesSalas = descricoesSalas + " e ";
					} else {
						descricoesSalas = descricoesSalas + ", ";
					}
				}
			}
			
			for (int i = 0; i < nomes.size(); i++) {
				nomesSalas = nomesSalas + nomes.get(i);
				if (i < (nomes.size() - 1)){
					if (i == (nomes.size() - 2)){
						nomesSalas = nomesSalas + " e ";
					} else {
						nomesSalas = nomesSalas + ", ";
					}
				}												
			}
			
			Email emailConfirmacao = new Email();
			if (aprovacao) {
				emailConfirmacao = emailService.findByIdentificador("EMAIL_CONFIRMACAO_REUNIAO_APROVADA");
			} else {
				emailConfirmacao = emailService.findByIdentificador("EMAIL_CONFIRMACAO_REUNIAO_REPROVADA");
			}			

			if (emailConfirmacao != null) {
				
				List<String> responsaveis = new ArrayList<String>();
				
				if (reuniao.getSalas() != null) {
					for (Sala s : reuniao.getSalas()) {			

						Sala sala = salaService.findById(s.getId());
						
						if (sala.getResponsaveis() != null) {
							for (Responsavel r : sala.getResponsaveis()) {
								if (r.getEmail() != null) {
									responsaveis.add(r.getEmail());
								}
							}
						}
					}
				}
				
				String corpo = emailConfirmacao.getCorpo();
				
				if (corpo.contains("{SALAS}")) {
					corpo = corpo.replace("{SALAS}", descricoesSalas);
				}
				
				if (corpo.contains("{NOME_SOLICITANTE}")) {
					corpo = corpo.replace("{NOME_SOLICITANTE}", reuniao.getCriadoPorNome());
				}

				if (corpo.contains("{DATA_INICIAL}")) {
					corpo = corpo.replace("{DATA_INICIAL}", dt.format(reuniao.getDataInicial()));
				}

				if (corpo.contains("{DATA_FINAL}")) {
					corpo = corpo.replace("{DATA_FINAL}", dt.format(reuniao.getDataFinal()));
				}
				
				if (corpo.contains("{ENDERECOS_SALAS_FISICAS}")) {
					corpo = corpo.replace("{ENDERECOS_SALAS_FISICAS}", enderecosSalasFisicas);
				}
				
				if (corpo.contains("{RESPONSAVEIS_SALAS}")) {
					corpo = corpo.replace("{RESPONSAVEIS_SALAS}", responsaveisSalas);
				}
				
				if (corpo.contains("{SALAS_VIRTUAIS}")) {
					corpo = corpo.replace("{SALAS_VIRTUAIS}", salasVirtuais);
				}
				
				String assuntoConfirmacao = emailConfirmacao.getAssunto();
				if (assuntoConfirmacao.contains("{NOME_SALA}")) {
					assuntoConfirmacao = assuntoConfirmacao.replace("{NOME_SALA}", nomesSalas);
				}
				
				if (responsaveis.size() > 0 && emailConfirmacao.getIdentificador().equals("EMAIL_CONFIRMACAO_REUNIAO_REPROVADA")) {
					responsaveis.add(reuniao.getCriadoPorEmail());
					mailService.sendMail(responsaveis.toArray(new String[responsaveis.size()]), assuntoConfirmacao, corpo);
				} else {
					mailService.sendMailSolicitante(reuniao.getCriadoPorEmail(), assuntoConfirmacao, corpo);
				}
				
			}
		}
		return reuniao;
	}

	@Transactional
	@Override
	public Long countParaAprovacao() {
		// TODO Auto-generated method stub
		return reuniaoRepository.countParaAprovacao();
	}

	@Override
	public List<Reuniao> listRelatorio2(Date dataInicial, Date dataFinal, EquipamentoSalaEnum equipamento, Long organizacaoId, Long instituicaoId) {

		CriteriaBuilder builder = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> query = builder.createQuery(Reuniao.class);
		Root<Reuniao> from = query.from(Reuniao.class);
		Predicate predicate = builder.and();
		from.fetch("salas", JoinType.INNER);
		from.fetch("equipamentos", JoinType.LEFT);

		if (dataInicial != null && dataFinal != null) {
			predicate = builder.and(predicate, builder.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), builder.between(from.<Date> get("dataFinal"), dataInicial, dataFinal));

		}

		if (equipamento != null) {
			Join<Reuniao, EquipamentoSalaEnum> equipamentos = from.joinCollection("equipamentos");
			predicate = builder.and(predicate, equipamentos.in(equipamento));
		}

		if (organizacaoId != null) {
			predicate = builder.and(predicate, builder.equal(from.join("salas").get("organizacaoId"), organizacaoId));

		}

		if (instituicaoId != null) {
			predicate = builder.and(predicate, builder.equal(from.join("salas").get("instituicaoId"), instituicaoId));

		}

		TypedQuery<Reuniao> typedquery = em.createQuery(query.select(from).where(predicate).orderBy(builder.asc(from.get("criadoPorNome"))));
		List<Reuniao> reunioes = typedquery.getResultList();
		return reunioes;

	}

	@Override
	public List<Reuniao> listUserImpressao(Date dataInicial, Date dataFinal, Long id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> criteria = cb.createQuery(Reuniao.class);
		Root<Reuniao> from = criteria.from(Reuniao.class);
		criteria.distinct(true);
		
		from.fetch("salas");
	
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (dataInicial != null && dataFinal != null) {
			conditions.add(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)));
		}

		if (id != null) {
			Predicate condition = cb.equal(from.get("criadoPor"), id);
			conditions.add(condition);
		}

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		TypedQuery<Reuniao> query = em.createQuery(criteria);
		return query.getResultList();
	}

	@Transactional(readOnly = true)
	@Override
	public List<Reuniao> listAdminImpressao(Date dataInicial, Date dataFinal) {

		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Reuniao> criteria = cb.createQuery(Reuniao.class);
		Root<Reuniao> from = criteria.from(Reuniao.class);
		from.fetch("salas");
    	criteria.distinct(true);
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (dataInicial != null && dataFinal != null) {
			conditions.add(cb.or(cb.between(from.<Date> get("dataInicial"), dataInicial, dataFinal), cb.between(from.<Date> get("dataFinal"), dataInicial, dataFinal)));
		}
		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		TypedQuery<Reuniao> query = em.createQuery(criteria);
		return query.getResultList();

	}

	@Override
	public List<Reuniao> listImpressao(Date dataInicial, Date dataFinal, Long id) {
		List<Reuniao> reunioes = new ArrayList<>();

		if (id == null) {
			reunioes = listAdminImpressao(dataInicial, dataFinal);
		} else {
			reunioes = listUserImpressao(dataInicial, dataFinal, id);
		}

		return reunioes;
	}

	@Override
	public Boolean verificaDataPassada(Date dataInicial, Date dataFinal) {

		LocalDateTime agora = new LocalDateTime();
		LocalDateTime dinicial = new LocalDateTime(dataInicial);
		LocalDateTime dfinal = new LocalDateTime(dataFinal);

		if (dinicial.isBefore(agora) || dfinal.isBefore(agora)) {
			throw ReuniaoException.REUNIAO_DATA_PASSADA;

		}

		return false;
	}

	@Override
	public List<Reuniao> retornaReunioesSemEquipamento(List<Reuniao> reunioes) {
		List<Reuniao> reunioesSemEquipamento = new ArrayList<>();
		List<Reuniao> reunioesAlteradas = new ArrayList<>();

		for (Reuniao re : reunioes) {
			reunioesAlteradas.add(new Reuniao(re));
		}

		for (Reuniao r : reunioesAlteradas) {
			if (r.getEquipamentos().isEmpty() || r.getEquipamentos().equals(null)) {
				reunioesSemEquipamento.add(r);
			}
		}

		return reunioesSemEquipamento;

	}

	public void setMailService(MailService mailService) {
		this.mailService = mailService;
	}
	
private String informacoesGeraisDaReuniao(Reuniao reuniao){
		
		StringBuilder strBuilder =  new StringBuilder();
		
		String emailCriador = reuniao.getCriadoPorEmail() == null ? "NÃ£o cadastrado": reuniao.getCriadoPorEmail(); 
		String criadorPorNome = reuniao.getCriadoPorNome() == null ? "NÃ£o cadastrado": reuniao.getCriadoPorNome(); 
		
		strBuilder.append("\tSolicitante: " + criadorPorNome  + " " + emailCriador +  "\n\n");
		
		Set<Sala> salas =  reuniao.getSalas();
		
		if (salas != null && !salas.isEmpty()){
			   
			 for(Sala salaObjetoID: salas){
				 
				 Sala sala =  salaService.findById(salaObjetoID.getId());
				 
				 String nomeSala = sala.getNome() == null ? "NÃ£o cadastrado":sala.getNome();
				 strBuilder.append("\tNome da sala: " + nomeSala +"\n");	
				 
			 }	
		}
		else{
			strBuilder.append("Esta reuniÃ£o nÃ£o possui Salas\n");
		}
		
		return strBuilder.toString();
	}


private String retornaNomeSalaMCU(Reuniao reuniao){
	
	StringBuilder strBuilder =  new StringBuilder();
	
	
	Set<Sala> salas =  reuniao.getSalas();
	
	if (salas != null && !salas.isEmpty()){
		   
		 for(Sala salaObjetoID: salas){
			 
			 Sala sala =  salaService.findById(salaObjetoID.getId());
			 
				if (sala.getAdministracao().toString().equals("MCU")) {
					String nomeSala = sala.getNome() == null ? "NÃ£o cadastrado"
							: sala.getNome();
					strBuilder.append(nomeSala);
				}
			 
		 }	
	}
	else{
		strBuilder.append("Esta reuniÃ£o nÃ£o possui Salas\n");
	}
	
	return strBuilder.toString();
}


}
