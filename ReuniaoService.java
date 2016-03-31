package br.rnp.agendamento.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import br.rnp.agendamento.domain.Reuniao;
import br.rnp.agendamento.domain.Sala;
import br.rnp.agendamento.enums.EquipamentoSalaEnum;

@Transactional
public interface ReuniaoService {

	Reuniao save(Reuniao reuniao);

	Reuniao findById(Long id);

	Reuniao delete(Long id);

	Reuniao changeStatus(Long id, Boolean b);

	List<Reuniao> verificarConflitosDeHorarios(List<Sala> listSalas, Date dataInicial, Date dataFinal);

	List<Reuniao> list(Date dataInicial, Date dataFinal, Long id, Sala  sala);

	List<Reuniao> listRelatorio(Date dataInicial, Date dataFinal,EquipamentoSalaEnum equipamento, Long organizacaoId, Long instituicaoId);

	List<Reuniao> listTipo(EquipamentoSalaEnum tipoSala);

	List<Reuniao> listParaAprovacao(Long usuarioId, Pageable page);

	Reuniao changeAprovacao(Long id, Boolean aprovacao);

	Long countParaAprovacao();

	List<Reuniao> listTodas(Date dataInicial, Date dataFinal, Long id, Sala sala);

	List<Reuniao> listRelatorio2(Date dataInicial, Date dataFinal,EquipamentoSalaEnum equipamento, Long organizacaoId,Long instituicaoId);

	List<Reuniao> listImpressao(Date dataInicial, Date dataFinal, Long id);
	
	List<Reuniao> listAdminImpressao(Date dataInicial, Date dataFinal);

	List<Reuniao> listUserImpressao(Date dataInicial, Date dataFinal, Long id);

	Boolean verificaDataPassada(Date dataInicial, Date dataFinal);

	List<Reuniao> retornaReunioesSemEquipamento(List<Reuniao> reunioes);

	void setMailService(MailService mailService);

}
