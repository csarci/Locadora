package br.rnp.agendamento.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;

import br.rnp.agendamento.domain.Sala;
import br.rnp.agendamento.enums.AdministracaoSalaEnum;
import br.rnp.agendamento.enums.EquipamentoSalaEnum;
import br.rnp.agendamento.enums.TipoSalaEnum;

public interface SalaService {

	Sala save(Sala sala);

	Sala delete(Long id);
	
	Sala findById(Long id);

	List<Sala> listAll(TipoSalaEnum tipo, Long organizacaoId, Long instituicaoId, String nome, Boolean st, Pageable page, List<AdministracaoSalaEnum> administracoes);

	Long count(TipoSalaEnum tipo, Long organizacaoId, Long instituicaoId, String nome, Boolean st, List<AdministracaoSalaEnum> administracoes);

	Sala changeStatus(Long id, Boolean b);

	List<Sala> findSalasDisponiveis(List<Long> organizacoesSemVinculo, List<Long> instituicoesSemVinculo, List<EquipamentoSalaEnum> equipamentos, Date dataFinal, Date dataInicial, List<AdministracaoSalaEnum> administracoes, Long organizacaoUsuario, Long instituicaoUsuario);

	List<Sala> listAll(Long instituicaoId, Pageable page, List<AdministracaoSalaEnum> administracoes);

	Long count();

	Boolean verificaDisponibilidadeDiaSemana(Sala sala, Date dataInicial,
			Date dataFinal);

//	List<String> recuperaDiasSemanaSala(Sala sala);
	
//	List<String> recuperaDiasSemanaPeriodo(Date dataInicial, Date dataFinal);
	
	List<Integer> recuperaDiasSemanaSala(Sala sala);

	List<Integer> recuperaDiasSemanaPeriodo(Date dataInicial, Date dataFinal);

	List<Sala> verificaVisibilidade(Long idOrganizacaoUsuario, Long idInstituicaoUsuario, List<Sala> salas);

	List<Sala> findTodasSalasDisponiveis(List<Long> organizacoes, List<Long> instituicoes, List<EquipamentoSalaEnum> equipamentos, Date dataInicial, Date dataFinal,
			List<AdministracaoSalaEnum> administracoes);

	List<Long> getOrganizacoesIdsComSalasCadastradas();
	
	List<Long> getInstituicaoIdsComSalasCadastradas();

	List<String> getCoresSalas();
}
