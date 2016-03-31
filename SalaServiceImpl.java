package br.rnp.agendamento.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.joda.time.DateTimeConstants;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.rnp.agendamento.domain.Sala;
import br.rnp.agendamento.enums.AdministracaoSalaEnum;
import br.rnp.agendamento.enums.DiaEnum;
import br.rnp.agendamento.enums.EquipamentoSalaEnum;
import br.rnp.agendamento.enums.TipoSalaEnum;
import br.rnp.agendamento.enums.TipoUsuarioEnum;
import br.rnp.agendamento.exception.SalaException;
import br.rnp.agendamento.repository.SalaRepository;
import br.rnp.agendamento.service.SalaService;

@Service
public class SalaServiceImpl implements SalaService {

	@Autowired
	private SalaRepository salaRepository;

	@PersistenceContext
	private EntityManager em;

	@Transactional
	@Override
	public Sala save(Sala sala) {

		if (sala.isNew()) {
			validarMesmoNome(sala);
		}

		return salaRepository.save(sala);
	}

	@Transactional
	@Override
	public Sala delete(Long id) {
		Sala sala = findById(id);
		salaRepository.delete(sala);
		return sala;
	}

	@Transactional(readOnly = true)
	@Override
	public Sala findById(Long id) {
		Sala sala = salaRepository.findOne(id);
		if (sala == null) {
			throw SalaException.SALA_NAO_ENCONTRADA;
		}
		Hibernate.initialize(sala.getDias());
		Hibernate.initialize(sala.getContatos());
		Hibernate.initialize(sala.getResponsaveis());
		Hibernate.initialize(sala.getEquipamentos());
		Hibernate.initialize(sala.getEndereco());
		if (sala.getTipo().equals(TipoSalaEnum.INTEGRADA)) {
			Hibernate.initialize(sala.getIntegradas());
		}
		return sala;
	}

	private void validarMesmoNome(Sala sala) {
		Sala existe = salaRepository.findByNome(sala.getNome());

		if (existe != null) {
			throw SalaException.EXISTE_SALA_COM_MESMO_NOME;
		}
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Sala> listAll(TipoSalaEnum tipo, Long organizacaoId, Long instituicaoId, String nome, Boolean st, Pageable page, List<AdministracaoSalaEnum> administracoes) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Sala> criteria = cb.createQuery(Sala.class);
		Root<Sala> from = criteria.from(Sala.class);
		criteria.orderBy(cb.asc(from.get("nome")));

		List<Predicate> conditions = new ArrayList<Predicate>();

		if (tipo != null) {
			Predicate condition = cb.equal(from.get("tipo"), tipo);
			conditions.add(condition);
		}

		if (organizacaoId != null) {
			Predicate condition = cb.equal(from.get("organizacaoId"), organizacaoId);
			conditions.add(condition);
		}

		if (instituicaoId != null) {
			Predicate condition = cb.equal(from.get("instituicaoId"), instituicaoId);
			conditions.add(condition);
		}

		if (nome != null && !nome.isEmpty()) {
			Predicate condition = cb.like(from.<String> get("nome"), nome + "%");
			conditions.add(condition);
		}

		if (st != null) {
			Predicate condition = cb.equal(from.get("ativo"), st);
			conditions.add(condition);
		}

		if (administracoes != null && administracoes.size() > 0) {
			Expression<AdministracaoSalaEnum> exp = from.get("administracao");
			conditions.add(exp.in(administracoes));
		}

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		query.setFirstResult(page.getOffset());
		query.setMaxResults(page.getPageSize());
		List<Sala> salas = (List<Sala>) query.getResultList();
		return salas;
	}

	@Transactional(readOnly = true)
	@Override
	public Long count(TipoSalaEnum tipo, Long organizacaoId, Long instituicaoId, String nome, Boolean st, List<AdministracaoSalaEnum> administracoes) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Long> criteria = cb.createQuery(Long.class);
		Root<Sala> from = criteria.from(Sala.class);
		criteria.select(cb.count(from));

		List<Predicate> conditions = new ArrayList<Predicate>();

		if (tipo != null) {
			Predicate condition = cb.equal(from.get("tipo"), tipo);
			conditions.add(condition);
		}

		if (organizacaoId != null) {
			Predicate condition = cb.equal(from.get("organizacaoId"), organizacaoId);
			conditions.add(condition);
		}

		if (instituicaoId != null) {
			Predicate condition = cb.equal(from.get("instituicaoId"), instituicaoId);
			conditions.add(condition);
		}

		if (nome != null && !nome.isEmpty()) {
			Predicate condition = cb.like(from.<String> get("nome"), nome + "%");
			conditions.add(condition);
		}

		if (st != null) {
			Predicate condition = cb.equal(from.get("ativo"), st);
			conditions.add(condition);
		}

		if (administracoes != null && administracoes.size() > 0) {
			Expression<AdministracaoSalaEnum> exp = from.get("administracao");
			conditions.add(exp.in(administracoes));
		}

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		return (Long) query.getSingleResult();
	}

	@Transactional(readOnly = true)
	@Override
	public Sala changeStatus(Long id, Boolean b) {
		// TODO Auto-generated method stub
		Sala sala = findById(id);
		sala.setAtivo(b);
		return salaRepository.save(sala);
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Sala> findSalasDisponiveis(List<Long> organizacoesSemVinculo, List<Long> instituicoesSemVinculo, List<EquipamentoSalaEnum> equipamentos, Date dataInicial, Date dataFinal,
			List<AdministracaoSalaEnum> administracoes, Long organizacaoUsuario, Long instituicaoUsuario) {

		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<Sala> criteria = cb.createQuery(Sala.class);
		Root<Sala> from = criteria.from(Sala.class);
		criteria.orderBy(cb.asc(from.get("nome")));
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (organizacoesSemVinculo != null && organizacoesSemVinculo.size() > 0) {
			Expression<Long> exp = from.get("organizacaoId");
			Expression<TipoUsuarioEnum> visibilidade = from.get("visibilidade");
			Predicate condition = cb.and(exp.in(organizacoesSemVinculo), visibilidade.in(Arrays.asList(TipoUsuarioEnum.EXTERNA_ORGANIZACAO)));
			conditions.add(condition);
		}

		if (instituicoesSemVinculo != null && instituicoesSemVinculo.size() > 0) {
			Expression<Long> exp = from.get("instituicaoId");
			Expression<TipoUsuarioEnum> visibilidade = from.get("visibilidade");
			Predicate condition = cb.and(exp.in(instituicoesSemVinculo), visibilidade.in(Arrays.asList(TipoUsuarioEnum.INTRA_INSTITUICOES)));
			conditions.add(condition);
		}

		if (organizacaoUsuario != null) {
			Expression<Long> exp = from.get("organizacaoId");
			Predicate condition = exp.in(organizacaoUsuario);
			conditions.add(condition);
		}

		if (instituicaoUsuario != null) {
			Expression<Long> exp = from.get("instituicaoId");
			Predicate condition = exp.in(instituicaoUsuario);
			conditions.add(condition);
		}

		if (equipamentos != null && equipamentos.size() > 0) {
			Join<Sala, EquipamentoSalaEnum> equipamentosJoin = from.join("equipamentos");
			conditions.add(equipamentosJoin.in(equipamentos));
		}

		if (dataInicial != null) {
			Expression<Integer> hour = cb.function("hour", Integer.class, from.<Date> get("horaInicial"));
			Expression<Integer> minutes = cb.function("minute", Integer.class, from.<Date> get("horaInicial"));
			conditions.add(cb.or(cb.lessThan(hour, new LocalTime(dataInicial).getHourOfDay()),
					cb.and(cb.equal(hour, new LocalTime(dataInicial).getHourOfDay()), cb.lessThanOrEqualTo(minutes, new LocalTime(dataInicial).getMinuteOfHour()))));
		}

		if (dataFinal != null) {
			Expression<Integer> hour = cb.function("hour", Integer.class, from.<Date> get("horaFinal"));
			Expression<Integer> minutes = cb.function("minute", Integer.class, from.<Date> get("horaFinal"));
			conditions.add(cb.or(cb.greaterThan(hour, new LocalTime(dataFinal).getHourOfDay()),
					cb.and(cb.equal(hour, new LocalTime(dataFinal).getHourOfDay()), cb.greaterThanOrEqualTo(minutes, new LocalTime(dataFinal).getMinuteOfHour()))));
		}

		if (administracoes != null && administracoes.size() > 0) {
			Expression<AdministracaoSalaEnum> exp = from.get("administracao");
			conditions.add(exp.in(administracoes));
		}

		Hours hora = Hours.hoursBetween(new LocalTime(dataInicial), new LocalTime(dataFinal));
	
		conditions.add(cb.greaterThanOrEqualTo(from.<Integer>get("limiteMaximo"), hora.getHours()));

		// somente salas ativas
		conditions.add(cb.equal(from.get("ativo"), true));

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		List<Sala> salas = (List<Sala>) query.getResultList();
		return salas;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Sala> findTodasSalasDisponiveis(List<Long> organizacoes, List<Long> instituicoes, List<EquipamentoSalaEnum> equipamentos, Date dataInicial, Date dataFinal,
			List<AdministracaoSalaEnum> administracoes) {

		CriteriaBuilder cb = em.getCriteriaBuilder();

		CriteriaQuery<Sala> criteria = cb.createQuery(Sala.class);
		Root<Sala> from = criteria.from(Sala.class);
		criteria.orderBy(cb.asc(from.get("nome")));
		List<Predicate> conditions = new ArrayList<Predicate>();

		if (organizacoes != null && organizacoes.size() > 0) {
			Expression<Long> exp = from.get("organizacaoId");
			Predicate condition = cb.and(exp.in(organizacoes));
			conditions.add(condition);
		}

		if (instituicoes != null && instituicoes.size() > 0) {
			Expression<Long> exp = from.get("instituicaoId");
			Predicate condition = cb.and(exp.in(instituicoes));
			conditions.add(condition);
		}

		if (equipamentos != null && equipamentos.size() > 0) {
			Join<Sala, EquipamentoSalaEnum> equipamentosJoin = from.join("equipamentos");
			conditions.add(equipamentosJoin.in(equipamentos));
		}

		if (dataInicial != null) {
			Expression<Integer> hour = cb.function("hour", Integer.class, from.<Date> get("horaInicial"));
			Expression<Integer> minutes = cb.function("minute", Integer.class, from.<Date> get("horaInicial"));
			conditions.add(cb.or(cb.lessThan(hour, new LocalTime(dataInicial).getHourOfDay()),
					cb.and(cb.equal(hour, new LocalTime(dataInicial).getHourOfDay()), cb.lessThanOrEqualTo(minutes, new LocalTime(dataInicial).getMinuteOfHour()))));
		}

		if (dataFinal != null) {
			Expression<Integer> hour = cb.function("hour", Integer.class, from.<Date> get("horaFinal"));
			Expression<Integer> minutes = cb.function("minute", Integer.class, from.<Date> get("horaFinal"));
			conditions.add(cb.or(cb.greaterThan(hour, new LocalTime(dataFinal).getHourOfDay()),
					cb.and(cb.equal(hour, new LocalTime(dataFinal).getHourOfDay()), cb.greaterThanOrEqualTo(minutes, new LocalTime(dataFinal).getMinuteOfHour()))));
		}

		if (administracoes != null && administracoes.size() > 0) {
			Expression<AdministracaoSalaEnum> exp = from.get("administracao");
			conditions.add(exp.in(administracoes));
		}

		Hours hora = Hours.hoursBetween(new LocalTime(dataInicial), new LocalTime(dataFinal));
			
		conditions.add(cb.greaterThanOrEqualTo(from.<Integer>get("limiteMaximo"), hora.getHours()));
		
		
		// somente salas ativas
		conditions.add(cb.equal(from.get("ativo"), true));

		Predicate condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		List<Sala> salas = (List<Sala>) query.getResultList();
		return salas;
	}

	@SuppressWarnings("unchecked")
	@Transactional(readOnly = true)
	@Override
	public List<Sala> listAll(Long instituicaoId, Pageable page, List<AdministracaoSalaEnum> administracoes) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Sala> criteria = cb.createQuery(Sala.class);
		Root<Sala> from = criteria.from(Sala.class);
		criteria.orderBy(cb.asc(from.get("nome")));

		List<Predicate> conditions = new ArrayList<Predicate>();

		if (instituicaoId != null) {
			Predicate condition = cb.equal(from.get("instituicaoId"), instituicaoId);
			conditions.add(condition);
		}

		if (administracoes != null && administracoes.size() > 0) {
			Expression<AdministracaoSalaEnum> exp = from.get("administracao");
			conditions.add(exp.in(administracoes));
		}

		Predicate condition = cb.equal(from.get("ativo"), true);
		conditions.add(condition);

		condition = cb.and(conditions.toArray(new Predicate[conditions.size()]));
		criteria.where(condition);

		Query query = em.createQuery(criteria);
		query.setFirstResult(page.getOffset());
		query.setMaxResults(page.getPageSize());
		List<Sala> salas = (List<Sala>) query.getResultList();
		return salas;
	}

	@Override
	public Boolean verificaDisponibilidadeDiaSemana(Sala sala, Date dataInicial, Date dataFinal) {

		List<Integer> diasRequeridos = new ArrayList<>();
		List<Integer> diasDisponiveis = new ArrayList<>();

		diasDisponiveis.addAll(recuperaDiasSemanaSala(sala));
		diasRequeridos.addAll(recuperaDiasSemanaPeriodo(dataInicial, dataFinal));

		boolean disponivel = true;

		for (Integer diaReq : diasRequeridos) {
			if (!diasDisponiveis.contains(diaReq)) {
				disponivel = false;
				break;
			}
		}

		return disponivel;
	}

	@Transactional(readOnly = true)
	@Override
	public List<Integer> recuperaDiasSemanaSala(Sala sala) {
		List<Integer> diasDisponiveis = new ArrayList<>();
		List<DiaEnum> dias = new ArrayList<>();
		dias.addAll(sala.getDias());

		for (DiaEnum d : dias) {
			switch (d) {
			case SEGUNDA:
				diasDisponiveis.add(DateTimeConstants.MONDAY);
				break;
			case TERCA:
				diasDisponiveis.add(DateTimeConstants.TUESDAY);
				break;
			case QUARTA:
				diasDisponiveis.add(DateTimeConstants.WEDNESDAY);
				break;
			case QUINTA:
				diasDisponiveis.add(DateTimeConstants.THURSDAY);
				break;
			case SEXTA:
				diasDisponiveis.add(DateTimeConstants.FRIDAY);
				break;
			case SABADO:
				diasDisponiveis.add(DateTimeConstants.SATURDAY);
				break;
			case DOMINGO:
				diasDisponiveis.add(DateTimeConstants.SUNDAY);
				break;
			default:
				break;
			}

		}

		return diasDisponiveis;
	}

	@Transactional(readOnly = true)
	@Override
	public List<Integer> recuperaDiasSemanaPeriodo(Date dataInicial, Date dataFinal) {

		List<Integer> diasRequeridos = new ArrayList<>();
		LocalDate datainicial = new LocalDate(dataInicial);
		LocalDate datafinal = new LocalDate(dataFinal);
		LocalDate data = new LocalDate();

		Integer diaSemana;
		int i = 0;
		Integer dias = Days.daysBetween(datainicial, datafinal).getDays();
		System.out.println(dias);
		if (dias > 0) {
			for (i = 0; i <= dias; i++) {
				data = datainicial.plusDays(i);
				diaSemana = data.dayOfWeek().get();
				diasRequeridos.add(diaSemana);
			}
		} else {
			diaSemana = datainicial.dayOfWeek().get();
			diasRequeridos.add(diaSemana);
		}

		return diasRequeridos;
	}

	@Override
	public List<Sala> verificaVisibilidade(Long idOrganizacaoUsuario, Long idInstituicaoUsuario, List<Sala> salas) {
		List<Sala> salasVisiveis = new ArrayList<Sala>();

		for (Sala s : salas) {
			if (s.getOrganizacaoId().equals(idOrganizacaoUsuario)) {
				if (s.getInstituicaoId().equals(idInstituicaoUsuario)) {
					salasVisiveis.add(s);

				} else if (!s.getInstituicaoId().equals(idInstituicaoUsuario)) {
					if (s.getVisibilidade().equals(TipoUsuarioEnum.INTRA_INSTITUICOES) || s.getVisibilidade().equals(TipoUsuarioEnum.EXTERNA_ORGANIZACAO)) {
						salasVisiveis.add(s);
					}
				}
			} else if (s.getVisibilidade().equals(TipoUsuarioEnum.EXTERNA_ORGANIZACAO)) {
				salasVisiveis.add(s);

			}

		}

		return salasVisiveis;
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<Long> getOrganizacoesIdsComSalasCadastradas(){
		return salaRepository.getOrganizacoesIdsComSalasCadastradas();
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<Long> getInstituicaoIdsComSalasCadastradas(){
		return salaRepository.getInstituicaoIdsComSalasCadastradas();
	}

	@Override
	public Long count() {
		return salaRepository.count();
	}
	
	@Transactional(readOnly = true)
	@Override
	public List<String> getCoresSalas(){
		return salaRepository.getCoresSalas();
	}

}
